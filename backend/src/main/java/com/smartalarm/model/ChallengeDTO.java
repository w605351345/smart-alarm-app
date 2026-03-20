package com.smartalarm.dto;

import com.smartalarm.model.AlarmChallenge;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 解密挑战 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeDTO {

    /**
     * 挑战 ID
     */
    private Long id;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 难度级别
     */
    private String difficulty;

    /**
     * 难度标签
     */
    private String difficultyLabel;

    /**
     * 题目类型
     */
    private String challengeType;

    /**
     * 题目内容
     */
    private String question;

    /**
     * 题目类型（用于前端渲染）
     */
    private String questionType;

    /**
     * 最大尝试次数
     */
    private Integer maxAttempts;

    /**
     * 超时时间（秒）
     */
    private Integer timeoutSeconds;

    /**
     * 难度配置（用于前端展示）
     */
    @Data
    @Builder
    public static class DifficultyConfig {
        private String value;
        private String label;
        private int level;
        private int timeout;
        private int maxAttempts;
        private String description;
    }

    /**
     * 获取所有难度配置
     */
    public static java.util.List<DifficultyConfig> getAllDifficulties() {
        java.util.List<DifficultyConfig> configs = new java.util.ArrayList<>();
        
        configs.add(DifficultyConfig.builder()
            .value("EASY")
            .label("简单")
            .level(1)
            .timeout(30)
            .maxAttempts(5)
            .description("10 以内加减法，3 位数字记忆")
            .build());
        
        configs.add(DifficultyConfig.builder()
            .value("MEDIUM")
            .label("中等")
            .level(2)
            .timeout(60)
            .maxAttempts(3)
            .description("50 以内混合运算，5 位数字记忆")
            .build());
        
        configs.add(DifficultyConfig.builder()
            .value("HARD")
            .label("困难")
            .level(3)
            .timeout(90)
            .maxAttempts(2)
            .description("100 以内复杂运算，7 位数字记忆")
            .build());
        
        return configs;
    }

    /**
     * 获取所有题目类型
     */
    public static java.util.List<ChallengeTypeConfig> getAllChallengeTypes() {
        java.util.List<ChallengeTypeConfig> types = new java.util.ArrayList<>();
        
        types.add(ChallengeTypeConfig.builder()
            .value("MATH")
            .label("数学计算")
            .icon("🔢")
            .description("加减乘除运算")
            .build());
        
        types.add(ChallengeTypeConfig.builder()
            .value("MEMORY")
            .label("记忆数字")
            .icon("🧠")
            .description("记住并输入数字")
            .build());
        
        types.add(ChallengeTypeConfig.builder()
            .value("SEQUENCE")
            .label("数字序列")
            .icon("📈")
            .description("找出数字规律")
            .build());
        
        types.add(ChallengeTypeConfig.builder()
            .value("ARITHMETIC")
            .label("算术应用")
            .icon("📝")
            .description("生活应用题")
            .build());
        
        return types;
    }

    @Data
    @Builder
    public static class ChallengeTypeConfig {
        private String value;
        private String label;
        private String icon;
        private String description;
    }
}
