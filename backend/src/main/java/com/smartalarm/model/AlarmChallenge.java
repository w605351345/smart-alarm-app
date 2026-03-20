package com.smartalarm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * 闹钟解密配置
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "alarm_challenge")
public class AlarmChallenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alarm_id")
    private Alarm alarm;

    /**
     * 是否启用解密
     */
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = false;

    /**
     * 难度级别：EASY, MEDIUM, HARD
     */
    @Column(name = "difficulty", length = 20)
    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficulty;

    /**
     * 题目类型：MATH, MEMORY, SEQUENCE
     */
    @Column(name = "challenge_type", length = 20)
    @Enumerated(EnumType.STRING)
    private ChallengeType challengeType;

    /**
     * 题目内容（JSON 格式存储题目数据）
     */
    @Column(name = "question_data", columnDefinition = "TEXT")
    private String questionData;

    /**
     * 正确答案
     */
    @Column(name = "answer", length = 100)
    private String answer;

    /**
     * 最大尝试次数
     */
    @Column(name = "max_attempts")
    private Integer maxAttempts = 3;

    /**
     * 超时时间（秒）
     */
    @Column(name = "timeout_seconds")
    private Integer timeoutSeconds = 60;

    public enum DifficultyLevel {
        EASY("简单", 1, 30, 5),      // 难度，超时秒数，最大尝试
        MEDIUM("中等", 2, 60, 3),
        HARD("困难", 3, 90, 2);

        private final String label;
        private final int level;
        private final int timeout;
        private final int maxAttempts;

        DifficultyLevel(String label, int level, int timeout, int maxAttempts) {
            this.label = label;
            this.level = level;
            this.timeout = timeout;
            this.maxAttempts = maxAttempts;
        }

        public String getLabel() {
            return label;
        }

        public int getLevel() {
            return level;
        }

        public int getTimeout() {
            return timeout;
        }

        public int getMaxAttempts() {
            return maxAttempts;
        }
    }

    public enum ChallengeType {
        MATH("数学计算"),
        MEMORY("记忆数字"),
        SEQUENCE("数字序列"),
        ARITHMETIC("算术题");

        private final String label;

        ChallengeType(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }
}
