//
//  AddAlarmView.swift
//  SmartAlarm
//
//  添加闹钟页面
//

import SwiftUI

struct AddAlarmView: View {
    @Environment(\.dismiss) private var dismiss
    @ObservedObject var viewModel: AlarmListViewModel
    
    @State private var time = Date()
    @State private var name = ""
    @State private var repeatDays: RepeatOption = .weekday
    @State private var vibration = true
    @State private var sound = "default"
    @State private var challengeEnabled = false
    @State private var showingChallengeConfig = false
    
    var body: some View {
        NavigationView {
            Form {
                // 时间选择
                Section(header: Text("时间")) {
                    DatePicker(
                        "时间",
                        selection: $time,
                        displayedComponents: [.hourAndMinute]
                    )
                    .labelsHidden()
                }
                
                // 闹钟名称
                Section(header: Text("名称")) {
                    TextField("例如：起床闹钟", text: $name)
                }
                
                // 重复设置
                Section(header: Text("重复")) {
                    Picker("重复", selection: $repeatDays) {
                        ForEach(RepeatOption.allCases, id: \.self) { option in
                            Text(option.label).tag(option)
                        }
                    }
                    .pickerStyle(.menu)
                    
                    if repeatDays != .once {
                        Text(repeatDays.value == 63 ? "工作日 (周一到周五)" :
                             repeatDays.value == 96 ? "周末 (周六、周日)" :
                             repeatDays.value == 127 ? "每天" : "仅一次")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                }
                
                // 提醒方式
                Section(header: Text("提醒方式")) {
                    Toggle("振动", isOn: $vibration)
                    
                    NavigationLink("铃声") {
                        SoundSelectionView(selectedSound: $sound)
                    }
                }
                
                // 解密挑战
                Section(header: Text("解密挑战")) {
                    Toggle("启用解密", isOn: $challengeEnabled)
                    
                    if challengeEnabled {
                        NavigationLink("解密设置") {
                            ChallengeConfigView(alarmId: -1) // -1 表示新闹钟
                        }
                        .foregroundColor(.blue)
                        
                        HStack {
                            Image(systemName: "info.circle")
                                .foregroundColor(.secondary)
                            Text("启用后，关闭闹钟时需要答题")
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                    }
                }
                
                // 保存按钮
                Section {
                    Button(action: saveAlarm) {
                        HStack {
                            Spacer()
                            Text("保存")
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
                            Spacer()
                        }
                    }
                }
            }
            .navigationTitle("添加闹钟")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("取消") {
                        dismiss()
                    }
                }
            }
        }
    }
    
    private func saveAlarm() {
        let formatter = DateFormatter()
        formatter.dateFormat = "HH:mm"
        
        let alarm = Alarm(
            id: 0,
            time: formatter.string(from: time),
            name: name,
            enabled: true,
            repeatDays: repeatDays.value,
            vibration: vibration,
            sound: sound,
            challengeEnabled: challengeEnabled
        )
        
        Task {
            try await viewModel.addAlarm(alarm: alarm)
            dismiss()
        }
    }
}

// MARK: - 铃声选择

struct SoundSelectionView: View {
    @Environment(\.dismiss) private var dismiss
    @Binding var selectedSound: String
    
    let sounds = [
        ("default", "默认铃声", "🎵"),
        ("radar", "雷达", "📡"),
        ("alarm_clock", "闹钟", "⏰"),
        ("birdsong", "鸟鸣", "🐦"),
        ("piano", "钢琴", "🎹")
    ]
    
    var body: some View {
        List {
            ForEach(sounds, id: \.0) { sound in
                Button(action: {
                    selectedSound = sound.0
                    dismiss()
                }) {
                    HStack {
                        Text(sound.2)
                            .font(.title2)
                        VStack(alignment: .leading) {
                            Text(sound.1)
                                .font(.body)
                            if sound.0 == selectedSound {
                                Text("已选择")
                                    .font(.caption)
                                    .foregroundColor(.blue)
                            }
                        }
                        Spacer()
                        if sound.0 == selectedSound {
                            Image(systemName: "checkmark")
                                .foregroundColor(.blue)
                        }
                    }
                }
            }
        }
        .navigationTitle("铃声")
        .navigationBarTitleDisplayMode(.inline)
    }
}

#Preview {
    AddAlarmView(viewModel: AlarmListViewModel())
}
