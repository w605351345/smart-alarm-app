//
//  EditAlarmView.swift
//  SmartAlarm
//
//  编辑闹钟页面
//

import SwiftUI

struct EditAlarmView: View {
    @Environment(\.dismiss) private var dismiss
    @ObservedObject var viewModel: AlarmListViewModel
    let alarm: Alarm
    
    @State private var time: Date
    @State private var name: String
    @State private var repeatDays: RepeatOption
    @State private var vibration: Bool
    @State private var sound: String
    @State private var challengeEnabled: Bool
    @State private var showingChallengeConfig = false
    
    init(alarm: Alarm, viewModel: AlarmListViewModel) {
        self.alarm = alarm
        self.viewModel = viewModel
        
        // 解析时间
        let components = alarm.time.split(separator: ":")
        let hour = Int(components[0]) ?? 7
        let minute = Int(components[1]) ?? 30
        
        var dateComponents = DateComponents()
        dateComponents.hour = hour
        dateComponents.minute = minute
        _time = State(initialValue: Calendar.current.date(from: dateComponents) ?? Date())
        
        _name = State(initialValue: alarm.name)
        _repeatDays = State(initialValue: RepeatOption(rawValue: alarm.repeatDays) ?? .weekday)
        _vibration = State(initialValue: alarm.vibration)
        _sound = State(initialValue: alarm.sound)
        _challengeEnabled = State(initialValue: alarm.challengeEnabled)
    }
    
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
                            ChallengeConfigView(alarmId: alarm.id)
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
                
                // 操作按钮
                Section {
                    Button(action: updateAlarm) {
                        HStack {
                            Spacer()
                            Text("保存修改")
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
            .navigationTitle("编辑闹钟")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("取消") {
                        dismiss()
                    }
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("删除") {
                        viewModel.deleteAlarm(alarm: alarm)
                        dismiss()
                    }
                    .foregroundColor(.red)
                }
            }
        }
    }
    
    private func updateAlarm() {
        let formatter = DateFormatter()
        formatter.dateFormat = "HH:mm"
        
        var updatedAlarm = alarm
        updatedAlarm.time = formatter.string(from: time)
        updatedAlarm.name = name
        updatedAlarm.repeatDays = repeatDays.value
        updatedAlarm.vibration = vibration
        updatedAlarm.sound = sound
        updatedAlarm.challengeEnabled = challengeEnabled
        
        Task {
            try await viewModel.updateAlarm(alarm: updatedAlarm)
            dismiss()
        }
    }
}

#Preview {
    EditAlarmView(
        alarm: Alarm(
            id: 1,
            time: "07:30",
            name: "起床闹钟",
            enabled: true,
            repeatDays: 63,
            vibration: true,
            sound: "default",
            challengeEnabled: false
        ),
        viewModel: AlarmListViewModel()
    )
}
