//
//  ChallengeConfigView.swift
//  SmartAlarm
//
//  解密挑战配置页面
//

import SwiftUI

struct ChallengeConfigView: View {
    @StateObject private var viewModel: ChallengeConfigViewModel
    @Environment(\.dismiss) private var dismiss
    
    let alarmId: Int
    
    init(alarmId: Int) {
        self.alarmId = alarmId
        _viewModel = StateObject(wrappedValue: ChallengeConfigViewModel(alarmId: alarmId))
    }
    
    var body: some View {
        NavigationView {
            Form {
                // 难度选择
                Section(header: Text("选择难度")) {
                    ForEach(ChallengeDifficulty.allCases, id: \.self) { difficulty in
                        DifficultyCard(
                            difficulty: difficulty,
                            isSelected: viewModel.selectedDifficulty == difficulty
                        ) {
                            viewModel.selectedDifficulty = difficulty
                        }
                    }
                }
                
                // 题目类型选择
                Section(header: Text("题目类型")) {
                    LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 16) {
                        ForEach(ChallengeType.allCases, id: \.self) { type in
                            TypeCard(
                                type: type,
                                isSelected: viewModel.selectedType == type
                            ) {
                                viewModel.selectedType = type
                            }
                        }
                    }
                    .padding(.vertical, 8)
                }
                
                // 启用开关
                Section {
                    Toggle("启用解密功能", isOn: $viewModel.enabled)
                    Text("开启后，关闭闹钟时需要答题")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
                
                // 保存按钮
                Section {
                    Button(action: saveChallenge) {
                        HStack {
                            Spacer()
                            Text("保存设置")
                                .font(.headline)
                                .foregroundColor(.white)
                                .frame(maxWidth: .infinity)
                                .padding()
                                .background(
                                    viewModel.enabled ?
                                    LinearGradient(gradient: Gradient(colors: [.blue, Color(red: 0.23, green: 0.61, blue: 0.79)]), startPoint: .leading, endPoint: .trailing) :
                                    LinearGradient(gradient: Gradient(colors: [.gray, .gray]), startPoint: .leading, endPoint: .trailing)
                                )
                                .cornerRadius(12)
                            Spacer()
                        }
                    }
                    .disabled(!viewModel.enabled)
                }
            }
            .navigationTitle("闹钟解密")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("完成") {
                        dismiss()
                    }
                }
            }
            .overlay {
                if viewModel.isLoading {
                    ProgressView("保存中...")
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                        .background(Color.black.opacity(0.3))
                }
            }
        }
    }
    
    private func saveChallenge() {
        Task {
            do {
                let config = ChallengeConfig(
                    enabled: viewModel.enabled,
                    difficulty: viewModel.selectedDifficulty,
                    challengeType: viewModel.selectedType
                )
                try await viewModel.saveConfig(config: config)
                dismiss()
            } catch {
                print("保存失败：\(error)")
            }
        }
    }
}

// MARK: - 难度卡片

struct DifficultyCard: View {
    let difficulty: ChallengeDifficulty
    let isSelected: Bool
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            HStack {
                Text(difficulty.stars)
                    .font(.title2)
                VStack(alignment: .leading, spacing: 4) {
                    Text(difficulty.label)
                        .font(.headline)
                    Text(difficulty.description)
                        .font(.caption)
                        .foregroundColor(.secondary)
                    HStack(spacing: 12) {
                        Label("\(difficulty.timeoutSeconds)秒", systemImage: "clock")
                        Label("\(difficulty.maxAttempts)次", systemImage: "pencil")
                    }
                    .font(.caption2)
                    .foregroundColor(.secondary)
                }
                Spacer()
                if isSelected {
                    Image(systemName: "checkmark.circle.fill")
                        .foregroundColor(.blue)
                        .font(.title2)
                }
            }
            .padding()
            .background(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(isSelected ? Color.blue : Color.gray.opacity(0.3), lineWidth: 2)
                    .background(isSelected ? Color.blue.opacity(0.05) : Color.clear)
            )
        }
        .buttonStyle(PlainButtonStyle())
    }
}

// MARK: - 类型卡片

struct TypeCard: View {
    let type: ChallengeType
    let isSelected: Bool
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            VStack(spacing: 8) {
                Text(type.icon)
                    .font(.system(size: 40))
                Text(type.label)
                    .font(.caption)
                    .fontWeight(.medium)
                Text(type.description)
                    .font(.caption2)
                    .foregroundColor(.secondary)
                    .multilineTextAlignment(.center)
            }
            .frame(maxWidth: .infinity)
            .padding()
            .background(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(isSelected ? Color.blue : Color.gray.opacity(0.3), lineWidth: 2)
                    .background(isSelected ? Color.blue.opacity(0.05) : Color.clear)
            )
        }
        .buttonStyle(PlainButtonStyle())
    }
}

#Preview {
    ChallengeConfigView(alarmId: 1)
}
