//
//  ChallengeConfigViewModel.swift
//  SmartAlarm
//
//  解密配置 ViewModel
//

import Foundation
import Combine

@MainActor
class ChallengeConfigViewModel: ObservableObject {
    @Published var enabled: Bool = false
    @Published var selectedDifficulty: ChallengeDifficulty = .easy
    @Published var selectedType: ChallengeType = .math
    @Published var isLoading: Bool = false
    @Published var error: String?
    
    private let alarmId: Int
    private let challengeService: ChallengeService
    private var cancellables = Set<AnyCancellable>()
    
    init(alarmId: Int, challengeService: ChallengeService = .shared) {
        self.alarmId = alarmId
        self.challengeService = challengeService
        loadConfig()
    }
    
    private func loadConfig() {
        Task {
            do {
                let config = try await challengeService.getChallengeConfig(alarmId: alarmId)
                self.enabled = config.enabled
                self.selectedDifficulty = config.difficulty
                self.selectedType = config.challengeType
            } catch {
                print("加载配置失败：\(error)")
            }
        }
    }
    
    func saveConfig(config: ChallengeConfig) async throws {
        isLoading = true
        defer { isLoading = false }
        
        try await challengeService.setChallengeConfig(alarmId: alarmId, config: config)
    }
}
