//
//  ChallengeAnswerView.swift
//  SmartAlarm
//
//  答题页面
//

import SwiftUI

struct ChallengeAnswerView: View {
    @StateObject private var viewModel: ChallengeAnswerViewModel
    @Environment(\.dismiss) private var dismiss
    
    let alarmId: Int
    let onChallengeSuccess: () -> Void
    
    init(alarmId: Int, onChallengeSuccess: @escaping () -> Void) {
        self.alarmId = alarmId
        _viewModel = StateObject(wrappedValue: ChallengeAnswerViewModel(alarmId: alarmId))
        self.onChallengeSuccess = onChallengeSuccess
    }
    
    var body: some View {
        ZStack {
            // 渐变背景
            LinearGradient(
                gradient: Gradient(colors: [Color.purple.opacity(0.7), Color.blue.opacity(0.7)]),
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
            .ignoresSafeArea()
            
            VStack(spacing: 24) {
                // 头部
                VStack(spacing: 12) {
                    Text("解密挑战")
                        .font(.title)
                        .fontWeight(.bold)
                        .foregroundColor(.white)
                    
                    if viewModel.timeLeft > 0 {
                        HStack {
                            Image(systemName: "clock.fill")
                            Text("\(viewModel.timeLeft)秒")
                        }
                        .font(.headline)
                        .padding(.horizontal, 20)
                        .padding(.vertical, 8)
                        .background(Color.white.opacity(0.2))
                        .cornerRadius(20)
                    }
                }
                .padding(.top, 40)
                
                Spacer()
                
                // 题目卡片
                VStack(spacing: 24) {
                    Text(viewModel.question)
                        .font(.title2)
                        .fontWeight(.medium)
                        .multilineTextAlignment(.center)
                        .foregroundColor(.primary)
                        .padding()
                    
                    // 输入框
                    if viewModel.questionType != "memory" || viewModel.showAnswerInput {
                        TextField("请输入答案", text: $viewModel.answer)
                            .font(.title2)
                            .keyboardType(.numberPad)
                            .textFieldStyle(RoundedBorderTextFieldStyle())
                            .padding(.horizontal)
                            .onSubmit {
                                submitAnswer()
                            }
                    }
                    
                    // 剩余次数
                    Text("剩余机会：\(viewModel.remainingAttempts)次")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                    
                    // 提交按钮
                    Button(action: submitAnswer) {
                        Text("提交答案")
                            .font(.headline)
                            .foregroundColor(.white)
                            .frame(maxWidth: .infinity)
                            .padding()
                            .background(
                                LinearGradient(
                                    gradient: Gradient(colors: [.blue, Color(red: 0.23, green: 0.61, blue: 0.79)]),
                                    startPoint: .leading,
                                    endPoint: .trailing
                                )
                            )
                            .cornerRadius(12)
                            .shadow(color: .blue.opacity(0.4), radius: 8, x: 0, y: 4)
                    }
                    .disabled(viewModel.answer.isEmpty)
                    .padding(.horizontal)
                }
                .padding()
                .background(Color.white)
                .cornerRadius(24)
                .shadow(color: .black.opacity(15), radius: 20, x: 0, y: 10)
                .padding(.horizontal)
                
                Spacer()
                
                // 提示
                Text("💡 答对题目才能关闭闹钟哦~")
                    .font(.subheadline)
                    .foregroundColor(.white.opacity(0.8))
                    .padding(.bottom, 40)
            }
        }
        .overlay {
            // 结果弹窗
            if viewModel.showResult {
                ZStack {
                    Color.black.opacity(0.6)
                        .ignoresSafeArea()
                    
                    VStack(spacing: 20) {
                        Text(viewModel.resultIcon)
                            .font(.system(size: 80))
                        
                        Text(viewModel.resultTitle)
                            .font(.title2)
                            .fontWeight(.bold)
                        
                        Text(viewModel.resultMessage)
                            .font(.body)
                            .foregroundColor(.secondary)
                            .multilineTextAlignment(.center)
                        
                        Button(action: closeResult) {
                            Text(viewModel.resultBtnText)
                                .font(.headline)
                                .foregroundColor(.white)
                                .padding(.horizontal, 40)
                                .padding(.vertical, 12)
                                .background(
                                    LinearGradient(
                                        gradient: Gradient(colors: [.blue, Color(red: 0.23, green: 0.61, blue: 0.79)]),
                                        startPoint: .leading,
                                        endPoint: .trailing
                                    )
                                )
                                .cornerRadius(12)
                        }
                    }
                    .padding(40)
                    .background(Color.white)
                    .cornerRadius(24)
                    .padding(.horizontal, 40)
                }
                .transition(.opacity)
            }
        }
        .onAppear {
            viewModel.startChallenge()
        }
        .onDisappear {
            viewModel.stopTimer()
        }
    }
    
    private func submitAnswer() {
        viewModel.submitAnswer { success in
            if success {
                onChallengeSuccess()
            }
        }
    }
    
    private func closeResult() {
        viewModel.closeResult()
        if viewModel.challengeDismissed {
            dismiss()
        }
    }
}

#Preview {
    ChallengeAnswerView(alarmId: 1) {
        print("挑战成功")
    }
}
