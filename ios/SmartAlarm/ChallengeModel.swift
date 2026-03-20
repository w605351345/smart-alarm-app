//
//  ChallengeModel.swift
//  SmartAlarm
//
//  闹钟解密挑战数据模型
//

import Foundation

/// 难度级别
enum ChallengeDifficulty: String, Codable, CaseIterable {
    case easy = "EASY"
    case medium = "MEDIUM"
    case hard = "HARD"
    
    var label: String {
        switch self {
        case .easy: return "简单"
        case .medium: return "中等"
        case .hard: return "困难"
        }
    }
    
    var stars: String {
        switch self {
        case .easy: return "⭐"
        case .medium: return "⭐⭐"
        case .hard: return "⭐⭐⭐"
        }
    }
    
    var timeoutSeconds: Int {
        switch self {
        case .easy: return 30
        case .medium: return 60
        case .hard: return 90
        }
    }
    
    var maxAttempts: Int {
        switch self {
        case .easy: return 5
        case .medium: return 3
        case .hard: return 2
        }
    }
    
    var description: String {
        switch self {
        case .easy: return "10 以内加减法，3 位数字记忆"
        case .medium: return "50 以内混合运算，5 位数字记忆"
        case .hard: return "100 以内复杂运算，7 位数字记忆"
        }
    }
}

/// 题目类型
enum ChallengeType: String, Codable, CaseIterable {
    case math = "MATH"
    case memory = "MEMORY"
    case sequence = "SEQUENCE"
    case arithmetic = "ARITHMETIC"
    
    var label: String {
        switch self {
        case .math: return "数学计算"
        case .memory: return "记忆数字"
        case .sequence: return "数字序列"
        case .arithmetic: return "算术应用"
        }
    }
    
    var icon: String {
        switch self {
        case .math: return "🔢"
        case .memory: return "🧠"
        case .sequence: return "📈"
        case .arithmetic: return "📝"
        }
    }
    
    var description: String {
        switch self {
        case .math: return "加减乘除运算"
        case .memory: return "记住并输入数字"
        case .sequence: return "找出数字规律"
        case .arithmetic: return "生活应用题"
        }
    }
}

/// 解密挑战配置
struct ChallengeConfig: Codable {
    var enabled: Bool
    var difficulty: ChallengeDifficulty
    var challengeType: ChallengeType
    
    static var `default`: ChallengeConfig {
        ChallengeConfig(
            enabled: false,
            difficulty: .easy,
            challengeType: .math
        )
    }
}

/// 活跃的挑战题目
struct ActiveChallenge: Codable {
    let id: Int
    let question: String
    let questionType: String
    let remainingAttempts: Int
    let timeoutSeconds: Int
    let expiresAt: Int64
}

/// 验证结果
struct VerifyResult: Codable {
    let correct: Bool
    let remainingAttempts: Int
    let message: String
    let alarmDismissed: Bool
}

/// API 响应包装器
struct APIResponse<T: Codable>: Codable {
    let code: Int
    let data: T?
    let message: String?
}
