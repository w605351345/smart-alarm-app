//
//  AlarmListViewModel.swift
//  SmartAlarm
//
//  闹钟列表 ViewModel
//

import Foundation
import Combine

@MainActor
class AlarmListViewModel: ObservableObject {
    @Published var alarms: [Alarm] = []
    @Published var isLoading: Bool = false
    @Published var error: String?
    
    // 日期和问候语
    var currentDate: String {
        let formatter = DateFormatter()
        formatter.dateFormat = "M 月 d 日 EEEE"
        formatter.locale = Locale(identifier: "zh_CN")
        return formatter.string(from: Date())
    }
    
    var greeting: String {
        let hour = Calendar.current.component(.hour, from: Date())
        if hour < 6 {
            return "夜深了，早点休息 🌙"
        } else if hour < 12 {
            return "早上好！☀️"
        } else if hour < 14 {
            return "中午好！🍚"
        } else if hour < 18 {
            return "下午好！☕"
        } else {
            return "晚上好！🌆"
        }
    }
    
    // 天气信息
    var weather: WeatherInfo? = WeatherInfo(
        temperature: 25,
        description: "晴朗",
        icon: "☀️"
    )
    
    private let apiService: APIService
    private var cancellables = Set<AnyCancellable>()
    
    init(apiService: APIService = .shared) {
        self.apiService = apiService
    }
    
    /// 加载闹钟列表
    func loadAlarms() {
        isLoading = true
        error = nil
        
        Task {
            do {
                let loadedAlarms = try await apiService.getAlarms()
                self.alarms = loadedAlarms
            } catch {
                self.error = "加载失败：\(error.localizedDescription)"
            }
            self.isLoading = false
        }
    }
    
    /// 切换闹钟开关
    func toggleAlarm(alarm: Alarm) {
        Task {
            do {
                var updatedAlarm = alarm
                updatedAlarm.enabled.toggle()
                try await apiService.updateAlarm(alarm: updatedAlarm)
                
                if let index = alarms.firstIndex(where: { $0.id == alarm.id }) {
                    alarms[index].enabled = updatedAlarm.enabled
                }
            } catch {
                self.error = "操作失败"
            }
        }
    }
    
    /// 删除闹钟
    func deleteAlarm(alarm: Alarm) {
        Task {
            do {
                try await apiService.deleteAlarm(id: alarm.id)
                alarms.removeAll { $0.id == alarm.id }
            } catch {
                self.error = "删除失败"
            }
        }
    }
    
    /// 添加闹钟
    func addAlarm(alarm: Alarm) async throws {
        let newAlarm = try await apiService.createAlarm(alarm: alarm)
        alarms.append(newAlarm)
        alarms.sort { $0.time < $1.time }
    }
    
    /// 更新闹钟
    func updateAlarm(alarm: Alarm) async throws {
        let updatedAlarm = try await apiService.updateAlarm(alarm: alarm)
        if let index = alarms.firstIndex(where: { $0.id == alarm.id }) {
            alarms[index] = updatedAlarm
            alarms.sort { $0.time < $1.time }
        }
    }
}

// MARK: - 天气信息

struct WeatherInfo {
    let temperature: Int
    let description: String
    let icon: String
}

// MARK: - API 服务

class APIService {
    static let shared = APIService()
    let baseURL = "https://api.yourdomain.com"
    
    private init() {}
    
    func getAlarms() async throws -> [Alarm] {
        // TODO: 实现 API 调用
        // 模拟数据
        return [
            Alarm(id: 1, time: "07:30", name: "起床闹钟", enabled: true, repeatDays: 63, vibration: true, sound: "default", challengeEnabled: false),
            Alarm(id: 2, time: "08:00", name: "晨跑提醒", enabled: false, repeatDays: 18, vibration: true, sound: "default", challengeEnabled: true),
            Alarm(id: 3, time: "09:00", name: "周末懒觉", enabled: true, repeatDays: 96, vibration: false, sound: "default", challengeEnabled: false)
        ]
    }
    
    func createAlarm(alarm: Alarm) async throws -> Alarm {
        // TODO: 实现 API 调用
        var newAlarm = alarm
        newAlarm.id = Int.random(in: 100...999)
        return newAlarm
    }
    
    func updateAlarm(alarm: Alarm) async throws -> Alarm {
        // TODO: 实现 API 调用
        return alarm
    }
    
    func deleteAlarm(id: Int) async throws {
        // TODO: 实现 API 调用
    }
}
