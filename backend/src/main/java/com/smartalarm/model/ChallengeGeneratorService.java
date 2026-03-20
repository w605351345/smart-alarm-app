package com.smartalarm.service;

import com.smartalarm.model.AlarmChallenge;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * 解密题目生成服务
 */
@Slf4j
@Service
public class ChallengeGeneratorService {

    private final Random random = new Random();

    /**
     * 生成解密题目
     */
    public ChallengeQuestion generateQuestion(AlarmChallenge.DifficultyLevel difficulty, 
                                               AlarmChallenge.ChallengeType type) {
        switch (type) {
            case MATH:
                return generateMathQuestion(difficulty);
            case MEMORY:
                return generateMemoryQuestion(difficulty);
            case SEQUENCE:
                return generateSequenceQuestion(difficulty);
            case ARITHMETIC:
                return generateArithmeticQuestion(difficulty);
            default:
                return generateMathQuestion(difficulty);
        }
    }

    /**
     * 生成数学计算题
     * 简单：10 以内加减法
     * 中等：50 以内混合运算
     * 困难：100 以内混合运算 + 乘除
     */
    private ChallengeQuestion generateMathQuestion(AlarmChallenge.DifficultyLevel difficulty) {
        int num1, num2, result;
        String operator, question;

        switch (difficulty) {
            case EASY:
                // 10 以内加减法
                num1 = random.nextInt(10) + 1;
                num2 = random.nextInt(10) + 1;
                if (random.nextBoolean()) {
                    operator = "+";
                    result = num1 + num2;
                } else {
                    // 确保结果不为负数
                    if (num1 < num2) {
                        int temp = num1;
                        num1 = num2;
                        num2 = temp;
                    }
                    operator = "-";
                    result = num1 - num2;
                }
                question = num1 + " " + operator + " " + num2 + " = ?";
                break;

            case MEDIUM:
                // 50 以内混合运算（两步）
                num1 = random.nextInt(30) + 10;
                num2 = random.nextInt(20) + 5;
                num2 = Math.min(num2, num1); // 确保减法不为负
                int num3 = random.nextInt(10) + 1;
                
                String op1 = random.nextBoolean() ? "+" : "-";
                String op2 = random.nextBoolean() ? "+" : "-";
                
                if (op1.equals("+") && op2.equals("+")) {
                    result = num1 + num2 + num3;
                } else if (op1.equals("+") && op2.equals("-")) {
                    result = num1 + num2 - num3;
                } else if (op1.equals("-") && op2.equals("+")) {
                    result = num1 - num2 + num3;
                } else {
                    result = num1 - num2 - num3;
                }
                
                question = num1 + " " + op1 + " " + num2 + " " + op2 + " " + num3 + " = ?";
                break;

            case HARD:
                // 100 以内混合运算 + 乘除
                num1 = random.nextInt(50) + 20;
                num2 = random.nextInt(10) + 2;
                num3 = random.nextInt(10) + 1;
                
                int opType = random.nextInt(4);
                switch (opType) {
                    case 0: // 加法 + 乘法
                        result = num1 + num2 * num3;
                        question = num1 + " + " + num2 + " × " + num3 + " = ?";
                        break;
                    case 1: // 减法 + 乘法
                        result = num1 - num2 * num3;
                        question = num1 + " - " + num2 + " × " + num3 + " = ?";
                        break;
                    case 2: // 乘法
                        result = num1 * num2;
                        question = num1 + " × " + num2 + " = ?";
                        break;
                    default: // 除法（确保整除）
                        result = num1;
                        int dividend = num1 * num2;
                        question = dividend + " ÷ " + num2 + " = ?";
                        break;
                }
                break;

            default:
                return generateMathQuestion(AlarmChallenge.DifficultyLevel.MEDIUM);
        }

        return new ChallengeQuestion(question, String.valueOf(result), "math");
    }

    /**
     * 生成记忆数字题
     * 简单：3 位数字
     * 中等：5 位数字
     * 困难：7 位数字
     */
    private ChallengeQuestion generateMemoryQuestion(AlarmChallenge.DifficultyLevel difficulty) {
        int digits;
        switch (difficulty) {
            case EASY:
                digits = 3;
                break;
            case MEDIUM:
                digits = 5;
                break;
            case HARD:
                digits = 7;
                break;
            default:
                digits = 4;
        }

        StringBuilder number = new StringBuilder();
        for (int i = 0; i < digits; i++) {
            number.append(random.nextInt(10));
        }

        String question = "请记住这个数字：" + number.toString();
        return new ChallengeQuestion(question, number.toString(), "memory");
    }

    /**
     * 生成数字序列题
     * 简单：等差数列（差 1-5）
     * 中等：等比数列或复杂等差
     * 困难：斐波那契或复合序列
     */
    private ChallengeQuestion generateSequenceQuestion(AlarmChallenge.DifficultyLevel difficulty) {
        int[] sequence;
        String question;
        String answer;

        switch (difficulty) {
            case EASY:
                // 简单等差数列
                int start = random.nextInt(10) + 1;
                int diff = random.nextInt(5) + 1;
                sequence = new int[]{start, start + diff, start + diff * 2, start + diff * 3};
                question = "找出规律，填写下一个数字：" + 
                          sequence[0] + ", " + sequence[1] + ", " + sequence[2] + ", " + sequence[3] + ", ?";
                answer = String.valueOf(start + diff * 4);
                break;

            case MEDIUM:
                // 等比数列或二级等差
                if (random.nextBoolean()) {
                    // 等比数列
                    int base = random.nextInt(3) + 2;
                    sequence = new int[]{base, base * base, base * base * base, base * base * base * base};
                    question = "找出规律，填写下一个数字：" + 
                              sequence[0] + ", " + sequence[1] + ", " + sequence[2] + ", " + sequence[3] + ", ?";
                    answer = String.valueOf(sequence[3] * base);
                } else {
                    // 二级等差
                    start = random.nextInt(5) + 1;
                    int firstDiff = random.nextInt(3) + 2;
                    int secondDiff = random.nextInt(2) + 1;
                    int a = start;
                    int b = a + firstDiff;
                    int c = b + firstDiff + secondDiff;
                    int d = c + firstDiff + secondDiff * 2;
                    question = "找出规律，填写下一个数字：" + a + ", " + b + ", " + c + ", " + d + ", ?";
                    answer = String.valueOf(d + firstDiff + secondDiff * 3);
                }
                break;

            case HARD:
                // 斐波那契数列
                int f1 = random.nextInt(3) + 1;
                int f2 = random.nextInt(3) + 1;
                int f3 = f1 + f2;
                int f4 = f2 + f3;
                int f5 = f3 + f4;
                question = "找出规律，填写下一个数字：" + f1 + ", " + f2 + ", " + f3 + ", " + f4 + ", " + f5 + ", ?";
                answer = String.valueOf(f4 + f5);
                break;

            default:
                return generateSequenceQuestion(AlarmChallenge.DifficultyLevel.MEDIUM);
        }

        return new ChallengeQuestion(question, answer, "sequence");
    }

    /**
     * 生成算术题（应用题）
     */
    private ChallengeQuestion generateArithmeticQuestion(AlarmChallenge.DifficultyLevel difficulty) {
        String question, answer;

        switch (difficulty) {
            case EASY:
                int apples = random.nextInt(5) + 3;
                int oranges = random.nextInt(5) + 2;
                question = "小明有" + apples + "个苹果，又买了" + oranges + "个橘子，一共有多少个水果？";
                answer = String.valueOf(apples + oranges);
                break;

            case MEDIUM:
                int books = random.nextInt(10) + 5;
                int given = random.nextInt(3) + 1;
                int bought = random.nextInt(5) + 2;
                question = "书架上有" + books + "本书，借给小红" + given + "本，又买了" + bought + "本，现在有多少本？";
                answer = String.valueOf(books - given + bought);
                break;

            case HARD:
                int money = random.nextInt(50) + 50;
                int spent1 = random.nextInt(20) + 10;
                int spent2 = random.nextInt(15) + 5;
                question = "你有" + money + "元钱，买书花了" + spent1 + "元，买文具花了" + spent2 + "元，还剩多少元？";
                answer = String.valueOf(money - spent1 - spent2);
                break;

            default:
                return generateArithmeticQuestion(AlarmChallenge.DifficultyLevel.MEDIUM);
        }

        return new ChallengeQuestion(question, answer, "arithmetic");
    }

    /**
     * 题目数据类
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ChallengeQuestion {
        private String question;
        private String answer;
        private String type;
    }

    /**
     * 将题目转换为 JSON 字符串存储
     */
    public String questionToJson(ChallengeQuestion question) {
        JSONObject json = new JSONObject();
        json.put("question", question.getQuestion());
        json.put("type", question.getType());
        json.put("displayData", question.getQuestion());
        return json.toJSONString();
    }

    /**
     * 从 JSON 解析题目
     */
    public ChallengeQuestion jsonToQuestion(String jsonStr, String answer) {
        JSONObject json = JSON.parseObject(jsonStr);
        return new ChallengeQuestion(
            json.getString("displayData"),
            answer,
            json.getString("type")
        );
    }
}
