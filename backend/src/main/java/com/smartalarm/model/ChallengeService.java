package com.smartalarm.service;

import com.alibaba.fastjson.JSON;
import com.smartalarm.controller.ChallengeController;
import com.smartalarm.model.Alarm;
import com.smartalarm.model.AlarmChallenge;
import com.smartalarm.repository.AlarmChallengeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 解密挑战服务
 */
@Slf4j
@Service
public class ChallengeService {

    @Autowired
    private ChallengeGeneratorService challengeGenerator;

    @Autowired
    private AlarmChallengeRepository challengeRepository;

    /**
     * 活跃的挑战（内存存储，生产环境应该用 Redis）
     * key: challengeId, value: ActiveChallenge
     */
    private final Map<Long, ActiveChallenge> activeChallenges = new ConcurrentHashMap<>();

    /**
     * 设置闹钟的解密挑战配置
     */
    @Transactional
    public AlarmChallenge setChallenge(Alarm alarm, Boolean enabled, 
                                        String difficultyStr, String typeStr) {
        
        AlarmChallenge challenge = challengeRepository.findByAlarmId(alarm.getId());
        
        if (challenge == null) {
            challenge = AlarmChallenge.builder()
                .alarm(alarm)
                .enabled(enabled != null && enabled)
                .build();
        }

        // 设置难度
        if (difficultyStr != null) {
            try {
                challenge.setDifficulty(AlarmChallenge.DifficultyLevel.valueOf(difficultyStr));
            } catch (IllegalArgumentException e) {
                challenge.setDifficulty(AlarmChallenge.DifficultyLevel.EASY);
            }
        } else {
            challenge.setDifficulty(AlarmChallenge.DifficultyLevel.EASY);
        }

        // 设置题目类型
        if (typeStr != null) {
            try {
                challenge.setChallengeType(AlarmChallenge.ChallengeType.valueOf(typeStr));
            } catch (IllegalArgumentException e) {
                challenge.setChallengeType(AlarmChallenge.ChallengeType.MATH);
            }
        } else {
            challenge.setChallengeType(AlarmChallenge.ChallengeType.MATH);
        }

        // 根据难度设置默认值
        AlarmChallenge.DifficultyLevel difficulty = challenge.getDifficulty();
        challenge.setMaxAttempts(difficulty.getMaxAttempts());
        challenge.setTimeoutSeconds(difficulty.getTimeout());

        // 生成题目数据
        if (challenge.getEnabled()) {
            ChallengeGeneratorService.ChallengeQuestion question = 
                challengeGenerator.generateQuestion(difficulty, challenge.getChallengeType());
            challenge.setQuestionData(challengeGenerator.questionToJson(question));
            challenge.setAnswer(question.getAnswer());
        }

        return challengeRepository.save(challenge);
    }

    /**
     * 生成活跃的挑战（闹钟触发时调用）
     */
    @Transactional
    public ChallengeController.ActiveChallengeDTO generateActiveChallenge(
            Long alarmId, String difficultyStr, String typeStr) {
        
        AlarmChallenge challenge = challengeRepository.findByAlarmId(alarmId);
        
        if (challenge == null || !challenge.getEnabled()) {
            throw new RuntimeException("该闹钟未启用解密功能");
        }

        // 生成新题目
        AlarmChallenge.DifficultyLevel difficulty = challenge.getDifficulty();
        AlarmChallenge.ChallengeType type = challenge.getChallengeType();
        
        ChallengeGeneratorService.ChallengeQuestion question = 
            challengeGenerator.generateQuestion(difficulty, type);

        // 创建活跃挑战
        ActiveChallenge activeChallenge = new ActiveChallenge(
            challenge.getId(),
            question,
            challenge.getMaxAttempts(),
            challenge.getTimeoutSeconds(),
            System.currentTimeMillis() + challenge.getTimeoutSeconds() * 1000
        );

        activeChallenges.put(challenge.getId(), activeChallenge);

        return ChallengeController.ActiveChallengeDTO.builder()
            .id(challenge.getId())
            .question(question.getQuestion())
            .questionType(question.getType())
            .remainingAttempts(activeChallenge.remainingAttempts)
            .timeoutSeconds(challenge.getTimeoutSeconds())
            .expiresAt(activeChallenge.expiresAt)
            .build();
    }

    /**
     * 验证答案
     */
    @Transactional
    public ChallengeController.VerifyResultDTO verifyAnswer(Long challengeId, String answer) {
        ActiveChallenge activeChallenge = activeChallenges.get(challengeId);
        
        if (activeChallenge == null) {
            throw new RuntimeException("挑战已过期或不存在");
        }

        // 检查超时
        if (System.currentTimeMillis() > activeChallenge.expiresAt) {
            activeChallenges.remove(challengeId);
            return ChallengeController.VerifyResultDTO.builder()
                .correct(false)
                .remainingAttempts(0)
                .message("答题超时")
                .alarmDismissed(false)
                .build();
        }

        // 检查尝试次数
        if (activeChallenge.remainingAttempts <= 0) {
            activeChallenges.remove(challengeId);
            return ChallengeController.VerifyResultDTO.builder()
                .correct(false)
                .remainingAttempts(0)
                .message("尝试次数已用完")
                .alarmDismissed(false)
                .build();
        }

        // 验证答案
        boolean correct = activeChallenge.question.getAnswer().trim().equals(answer.trim());
        
        if (correct) {
            activeChallenges.remove(challengeId);
            return ChallengeController.VerifyResultDTO.builder()
                .correct(true)
                .remainingAttempts(activeChallenge.remainingAttempts)
                .message("回答正确！闹钟已关闭")
                .alarmDismissed(true)
                .build();
        } else {
            activeChallenge.remainingAttempts--;
            
            String message = activeChallenge.remainingAttempts > 0 
                ? "回答错误，还剩 " + activeChallenge.remainingAttempts + " 次机会"
                : "回答错误，尝试次数已用完";
            
            if (activeChallenge.remainingAttempts <= 0) {
                activeChallenges.remove(challengeId);
            }

            return ChallengeController.VerifyResultDTO.builder()
                .correct(false)
                .remainingAttempts(activeChallenge.remainingAttempts)
                .message(message)
                .alarmDismissed(false)
                .build();
        }
    }

    /**
     * 活跃挑战（内存中的临时数据）
     */
    static class ActiveChallenge {
        Long challengeId;
        ChallengeGeneratorService.ChallengeQuestion question;
        int remainingAttempts;
        int timeoutSeconds;
        long expiresAt;

        ActiveChallenge(Long challengeId, 
                       ChallengeGeneratorService.ChallengeQuestion question,
                       int remainingAttempts,
                       int timeoutSeconds,
                       long expiresAt) {
            this.challengeId = challengeId;
            this.question = question;
            this.remainingAttempts = remainingAttempts;
            this.timeoutSeconds = timeoutSeconds;
            this.expiresAt = expiresAt;
        }
    }
}
