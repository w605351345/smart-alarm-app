package com.smartalarm.controller;

import com.smartalarm.dto.ApiResponse;
import com.smartalarm.dto.ChallengeDTO;
import com.smartalarm.model.Alarm;
import com.smartalarm.model.AlarmChallenge;
import com.smartalarm.repository.AlarmChallengeRepository;
import com.smartalarm.repository.AlarmRepository;
import com.smartalarm.service.ChallengeGeneratorService;
import com.smartalarm.service.ChallengeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 闹钟解密挑战控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/challenges")
@Api(tags = "闹钟解密挑战")
@CrossOrigin(origins = "*")
public class ChallengeController {

    @Autowired
    private ChallengeGeneratorService challengeGenerator;

    @Autowired
    private ChallengeService challengeService;

    @Autowired
    private AlarmRepository alarmRepository;

    @Autowired
    private AlarmChallengeRepository challengeRepository;

    @GetMapping("/difficulties")
    @ApiOperation("获取所有难度配置")
    public ResponseEntity<ApiResponse<List<ChallengeDTO.DifficultyConfig>>> getDifficulties() {
        return ResponseEntity.ok(ApiResponse.success(ChallengeDTO.getAllDifficulties()));
    }

    @GetMapping("/types")
    @ApiOperation("获取所有题目类型")
    public ResponseEntity<ApiResponse<List<ChallengeDTO.ChallengeTypeConfig>>> getChallengeTypes() {
        return ResponseEntity.ok(ApiResponse.success(ChallengeDTO.getAllChallengeTypes()));
    }

    @PostMapping("/alarms/{alarmId}")
    @ApiOperation("为闹钟设置解密挑战")
    public ResponseEntity<ApiResponse<ChallengeDTO>> setChallenge(
            @PathVariable Long alarmId,
            @RequestBody ChallengeRequest request) {
        
        try {
            Alarm alarm = alarmRepository.findById(alarmId)
                .orElseThrow(() -> new RuntimeException("闹钟不存在"));

            AlarmChallenge challenge = challengeService.setChallenge(
                alarm,
                request.getEnabled(),
                request.getDifficulty(),
                request.getChallengeType()
            );

            ChallengeDTO dto = convertToDTO(challenge);
            return ResponseEntity.ok(ApiResponse.success(dto));
        } catch (Exception e) {
            log.error("设置解密挑战失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/alarms/{alarmId}")
    @ApiOperation("获取闹钟的解密挑战配置")
    public ResponseEntity<ApiResponse<ChallengeDTO>> getAlarmChallenge(
            @PathVariable Long alarmId) {
        
        try {
            AlarmChallenge challenge = challengeRepository.findByAlarmId(alarmId);
            
            if (challenge == null) {
                // 返回默认配置
                ChallengeDTO dto = ChallengeDTO.builder()
                    .enabled(false)
                    .difficulty("EASY")
                    .build();
                return ResponseEntity.ok(ApiResponse.success(dto));
            }

            return ResponseEntity.ok(ApiResponse.success(convertToDTO(challenge)));
        } catch (Exception e) {
            log.error("获取解密挑战失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/generate")
    @ApiOperation("生成解密题目（闹钟触发时调用）")
    public ResponseEntity<ApiResponse<ActiveChallengeDTO>> generateChallenge(
            @RequestBody GenerateChallengeRequest request) {
        
        try {
            ActiveChallengeDTO activeChallenge = challengeService.generateActiveChallenge(
                request.getAlarmId(),
                request.getDifficulty(),
                request.getChallengeType()
            );
            
            return ResponseEntity.ok(ApiResponse.success(activeChallenge));
        } catch (Exception e) {
            log.error("生成解密题目失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/verify")
    @ApiOperation("验证答案")
    public ResponseEntity<ApiResponse<VerifyResultDTO>> verifyAnswer(
            @RequestBody VerifyAnswerRequest request) {
        
        try {
            VerifyResultDTO result = challengeService.verifyAnswer(
                request.getChallengeId(),
                request.getAnswer()
            );
            
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("验证答案失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    private ChallengeDTO convertToDTO(AlarmChallenge challenge) {
        ChallengeDTO.DifficultyConfig diffConfig = null;
        for (ChallengeDTO.DifficultyConfig config : ChallengeDTO.getAllDifficulties()) {
            if (config.getValue().equals(challenge.getDifficulty().name())) {
                diffConfig = config;
                break;
            }
        }

        return ChallengeDTO.builder()
            .id(challenge.getId())
            .enabled(challenge.getEnabled())
            .difficulty(challenge.getDifficulty() != null ? challenge.getDifficulty().name() : "EASY")
            .difficultyLabel(diffConfig != null ? diffConfig.getLabel() : "简单")
            .challengeType(challenge.getChallengeType() != null ? challenge.getChallengeType().name() : "MATH")
            .maxAttempts(challenge.getMaxAttempts())
            .timeoutSeconds(challenge.getTimeoutSeconds())
            .build();
    }

    @lombok.Data
    public static class ChallengeRequest {
        private Boolean enabled;
        private String difficulty;
        private String challengeType;
    }

    @lombok.Data
    public static class GenerateChallengeRequest {
        private Long alarmId;
        private String difficulty;
        private String challengeType;
    }

    @lombok.Data
    public static class VerifyAnswerRequest {
        private Long challengeId;
        private String answer;
    }

    @lombok.Data
    @lombok.Builder
    public static class ActiveChallengeDTO {
        private Long id;
        private String question;
        private String questionType;
        private Integer remainingAttempts;
        private Integer timeoutSeconds;
        private Long expiresAt;
    }

    @lombok.Data
    @lombok.Builder
    public static class VerifyResultDTO {
        private Boolean correct;
        private Integer remainingAttempts;
        private String message;
        private Boolean alarmDismissed;
    }
}
