//
//  ChallengeService.swift
//  SmartAlarm
//
//  解密挑战服务
//

import Foundation
import Combine

class ChallengeService: ObservableObject {
    static let shared = ChallengeService()
    
    @Published var activeChallenge: ActiveChallenge?
    @Published var isLoading = false
    @Published var error: String?
    
    private let baseURL = APIManager.shared.baseURL
    private var cancellables = Set<AnyCancellable>()
    
    private init() {}
    
    /// 获取难度配置
    func getDifficulties() async throws -> [DifficultyConfig] {
        let url = URL(string: "\(baseURL)/api/v1/challenges/difficulties")!
        let response: APIResponse<[DifficultyConfig]> = try await APIManager.shared.request(url: url)
        return response.data ?? []
    }
    
    /// 获取题目类型
    func getChallengeTypes() async throws -> [ChallengeTypeConfig] {
        let url = URL(string: "\(baseURL)/api/v1/challenges/types")!
        let response: APIResponse<[ChallengeTypeConfig]> = try await APIManager.shared.request(url: url)
        return response.data ?? []
    }
    
    /// 获取闹钟的解密配置
    func getChallengeConfig(alarmId: Int) async throws -> ChallengeConfig {
        let url = URL(string: "\(baseURL)/api/v1/challenges/alarms/\(alarmId)")!
        let response: APIResponse<ChallengeConfig> = try await APIManager.shared.request(url: url)
        return response.data ?? .default
    }
    
    /// 设置闹钟解密配置
    func setChallengeConfig(alarmId: Int, config: ChallengeConfig) async throws {
        let url = URL(string: "\(baseURL)/api/v1/challenges/alarms/\(alarmId)")!
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        
        let body: [String: Any] = [
            "enabled": config.enabled,
            "difficulty": config.difficulty.rawValue,
            "challengeType": config.challengeType.rawValue
        ]
        request.httpBody = try JSONSerialization.data(withJSONObject: body)
        
        let _: APIResponse<ChallengeConfig> = try await APIManager.shared.request(request: request)
    }
    
    /// 生成挑战题目
    func generateChallenge(alarmId: Int) async throws -> ActiveChallenge {
        let url = URL(string: "\(baseURL)/api/v1/challenges/generate")!
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        
        let body: [String: Any] = [
            "alarmId": alarmId
        ]
        request.httpBody = try JSONSerialization.data(withJSONObject: body)
        
        let response: APIResponse<ActiveChallenge> = try await APIManager.shared.request(request: request)
        
        if let challenge = response.data {
            DispatchQueue.main.async {
                self.activeChallenge = challenge
            }
            return challenge
        } else {
            throw NSError(domain: "ChallengeService", code: 400, userInfo: [NSLocalizedDescriptionKey: "生成挑战失败"])
        }
    }
    
    /// 验证答案
    func verifyAnswer(challengeId: Int, answer: String) async throws -> VerifyResult {
        let url = URL(string: "\(baseURL)/api/v1/challenges/verify")!
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        
        let body: [String: Any] = [
            "challengeId": challengeId,
            "answer": answer
        ]
        request.httpBody = try JSONSerialization.data(withJSONObject: body)
        
        let response: APIResponse<VerifyResult> = try await APIManager.shared.request(request: request)
        
        if let result = response.data {
            return result
        } else {
            throw NSError(domain: "ChallengeService", code: 400, userInfo: [NSLocalizedDescriptionKey: "验证失败"])
        }
    }
    
    /// 本地生成题目（离线模式）
    func generateLocalChallenge(difficulty: ChallengeDifficulty, type: ChallengeType) -> ChallengeQuestion {
        return ChallengeGenerator.shared.generateQuestion(difficulty: difficulty, type: type)
    }
}

// MARK: - 配置模型

struct DifficultyConfig: Codable {
    let value: String
    let label: String
    let level: Int
    let timeout: Int
    let maxAttempts: Int
    let description: String
}

struct ChallengeTypeConfig: Codable {
    let value: String
    let label: String
    let icon: String
    let description: String
}
