//
//  ChallengeAnswerViewModel.swift
//  SmartAlarm
//
//  答题 ViewModel
//

import Foundation
import Combine

@MainActor
class ChallengeAnswerViewModel: ObservableObject {
    @Published var question: String = ""
    @Published var questionType: String = ""
    @Published var answer: String = ""
    @Published var remainingAttempts: Int = 3
    @Published var timeLeft: Int = 60
    @Published var showResult: Bool = false
    @Published var resultIcon: String = ""
    @Published var resultTitle: String = ""
    @Published var resultMessage: String = ""
    @Published var resultBtnText: String = ""
    @Published var challengeDismissed: Bool = false
    @Published var showAnswerInput: Bool = true
    
    private let alarmId: Int
    private let challengeService: ChallengeService
    private var timer: Timer?
    private var currentChallengeId: Int = 0
    
    init(alarmId: Int, challengeService: ChallengeService = .shared) {
        self.alarmId = alarmId
        self.challengeService = challengeService
    }
    
    func startChallenge() {
        Task {
            do {
                let challenge = try await challengeService.generateChallenge(alarmId: alarmId)
                self.currentChallengeId = challenge.id
                self.question = challenge.question
                self.questionType = challenge.questionType
                self.remainingAttempts = challenge.remainingAttempts
                self.timeLeft = challenge.timeoutSeconds
                
                // 记忆题特殊处理
                if challenge.questionType == "memory" {
                    self.showAnswerInput = false
                    // 3 秒后显示输入框
                    DispatchQueue.main.asyncAfter(deadline: .now() + 3) {
                        self.showAnswerInput = true
                    }
                }
                
                startTimer()
            } catch {
                print("开始挑战失败：\(error)")
            }
        }
    }
    
    private func startTimer() {
        timer = Timer.scheduledTimer(withTimeInterval: 1, repeats: true) { [weak self] _ in
            Task { @MainActor in
                guard let self = self else { return }
                self.timeLeft -= 1
                
                if self.timeLeft <= 0 {
                    self.stopTimer()
                    self.showResult(
                        success: false,
                        icon: "⏰",
                        title: "时间已到",
                        message: "挑战失败，闹钟将继续响铃",
                        btnText: "我知道了"
                    )
                }
            }
        }
    }
    
    func stopTimer() {
        timer?.invalidate()
        timer = nil
    }
    
    func submitAnswer(completion: @escaping (Bool) -> Void) {
        Task {
            do {
                let result = try await challengeService.verifyAnswer(
                    challengeId: currentChallengeId,
                    answer: answer.trimmingCharacters(in: .whitespaces)
                )
                
                if result.correct {
                    stopTimer()
                    showResult(
                        success: true,
                        icon: "✅",
                        title: "回答正确！",
                        message: result.message,
                        btnText: "关闭闹钟"
                    )
                    challengeDismissed = true
                    completion(true)
                } else {
                    remainingAttempts = result.remainingAttempts
                    
                    if remainingAttempts <= 0 {
                        stopTimer()
                        showResult(
                            success: false,
                            icon: "❌",
                            title: "挑战失败",
                            message: result.message,
                            btnText: "我知道了"
                        )
                    } else {
                        answer = ""
                    }
                    completion(false)
                }
            } catch {
                print("验证失败：\(error)")
            }
        }
    }
    
    private func showResult(success: Bool, icon: String, title: String, message: String, btnText: String) {
        resultIcon = icon
        resultTitle = title
        resultMessage = message
        resultBtnText = btnText
        showResult = true
    }
    
    func closeResult() {
        showResult = false
    }
}
