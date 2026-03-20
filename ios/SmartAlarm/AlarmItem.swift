//
//  AlarmItem.swift
//  SmartAlarm
//
//  闹钟数据模型
//

import Foundation

struct Alarm: Codable, Identifiable {
    let id: Int
    var time: String
    var name: String
    var enabled: Bool
    var repeatDays: Int
    var vibration: Bool
    var sound: String
    var challengeEnabled: Bool
    
    /// 重复天数文本
    var repeatText: String {
        let days = [
            (1, "周日"), (2, "周一"), (4, "周二"), (8, "周三"),
            (16, "周四"), (32, "周五"), (64, "周六")
        ]
        
        var result: [String] = []
        for (value, text) in days {
            if repeatDays & value != 0 {
                result.append(text)
            }
        }
        
        if result.isEmpty {
            return "仅一次"
        } else if result.count == 7 {
            return "每天"
        } else if result.count == 5 {
            return "工作日"
        } else if result.count == 2 && repeatText.contains("周六") && repeatText.contains("周日") {
            return "周末"
        }
        
        return result.joined(separator: " ")
    }
    
    /// 格式化时间显示
    var timeDisplay: String {
        time
    }
}

/// 重复天数选项
enum RepeatOption: Int, CaseIterable {
    case once = 0       // 仅一次
    case everyday = 127 // 每天
    case weekday = 63   // 工作日 (周一 - 周五)
    case weekend = 96   // 周末 (周六、周日)
    case monday = 2
    case tuesday = 4
    case wednesday = 8
    case thursday = 16
    case friday = 32
    case saturday = 64
    case sunday = 1
    
    var label: String {
        switch self {
        case .once: return "仅一次"
        case .everyday: return "每天"
        case .weekday: return "工作日"
        case .weekend: return "周末"
        case .monday: return "周一"
        case .tuesday: return "周二"
        case .wednesday: return "周三"
        case .thursday: return "周四"
        case .friday: return "周五"
        case .saturday: return "周六"
        case .sunday: return "周日"
        }
    }
    
    var value: Int {
        return rawValue
    }
}
