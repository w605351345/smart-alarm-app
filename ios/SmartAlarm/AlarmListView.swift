//
//  AlarmListView.swift
//  SmartAlarm
//
//  闹钟列表主页面
//

import SwiftUI

struct AlarmListView: View {
    @StateObject private var viewModel = AlarmListViewModel()
    @State private var showingAddAlarm = false
    @State private var selectedAlarm: Alarm?
    
    var body: some View {
        NavigationView {
            ZStack {
                // 背景渐变
                LinearGradient(
                    gradient: Gradient(colors: [Color.blue.opacity(0.1), Color.white]),
                    startPoint: .top,
                    endPoint: .bottom
                )
                .ignoresSafeArea()
                
                VStack(spacing: 0) {
                    // 头部信息
                    headerSection
                    
                    // 闹钟列表
                    if viewModel.alarms.isEmpty {
                        emptyStateView
                    } else {
                        alarmList
                    }
                }
                
                // 添加按钮
                addButton
            }
            .navigationTitle("SmartAlarm")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button(action: {
                        // 统计页面
                    }) {
                        Image(systemName: "chart.bar.fill")
                            .foregroundColor(.blue)
                    }
                }
                
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: {
                        // 设置页面
                    }) {
                        Image(systemName: "gearshape.fill")
                            .foregroundColor(.blue)
                    }
                }
            }
            .sheet(isPresented: $showingAddAlarm) {
                AddAlarmView(viewModel: viewModel)
            }
            .sheet(item: $selectedAlarm) { alarm in
                EditAlarmView(alarm: alarm, viewModel: viewModel)
            }
            .onAppear {
                viewModel.loadAlarms()
            }
        }
    }
    
    // MARK: - 头部信息
    
    private var headerSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            // 日期
            Text(viewModel.currentDate)
                .font(.subheadline)
                .foregroundColor(.secondary)
            
            // 问候语
            Text(viewModel.greeting)
                .font(.title)
                .fontWeight(.bold)
            
            // 天气卡片
            if let weather = viewModel.weather {
                HStack {
                    HStack(spacing: 8) {
                        Text(weather.icon)
                            .font(.system(size: 32))
                        VStack(alignment: .leading, spacing: 2) {
                            Text("\(weather.temperature)°C")
                                .font(.title2)
                                .fontWeight(.semibold)
                            Text(weather.description)
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                    }
                    Spacer()
                }
                .padding()
                .background(Color.white)
                .cornerRadius(16)
                .shadow(color: .black.opacity(0.05), radius: 8, x: 0, y: 4)
            }
        }
        .padding()
        .padding(.top, 8)
    }
    
    // MARK: - 空状态
    
    private var emptyStateView: some View {
        VStack(spacing: 20) {
            Spacer()
            
            Image(systemName: "alarm.fill")
                .font(.system(size: 80))
                .foregroundColor(.gray.opacity(0.3))
            
            Text("暂无闹钟")
                .font(.title2)
                .fontWeight(.medium)
                .foregroundColor(.secondary)
            
            Text("点击下方按钮添加第一个闹钟")
                .font(.subheadline)
                .foregroundColor(.secondary)
            
            Spacer()
        }
    }
    
    // MARK: - 闹钟列表
    
    private var alarmList: some View {
        ScrollView {
            LazyVStack(spacing: 16) {
                ForEach(viewModel.alarms) { alarm in
                    AlarmCardView(
                        alarm: alarm,
                        onToggle: { viewModel.toggleAlarm(alarm: alarm) },
                        onEdit: { selectedAlarm = alarm },
                        onDelete: { viewModel.deleteAlarm(alarm: alarm) }
                    )
                }
            }
            .padding()
            .padding(.bottom, 100)
        }
    }
    
    // MARK: - 添加按钮
    
    private var addButton: some View {
        Button(action: {
            showingAddAlarm = true
        }) {
            ZStack {
                Circle()
                    .fill(
                        LinearGradient(
                            gradient: Gradient(colors: [.blue, Color(red: 0.23, green: 0.61, blue: 0.79)]),
                            startPoint: .leading,
                            endPoint: .trailing
                        )
                    )
                    .frame(width: 64, height: 64)
                    .shadow(color: .blue.opacity(0.4), radius: 12, x: 0, y: 6)
                
                Image(systemName: "plus")
                    .font(.system(size: 32, weight: .light))
                    .foregroundColor(.white)
            }
        }
        .position(x: UIScreen.main.bounds.width - 50, y: UIScreen.main.bounds.height - 100)
    }
}

// MARK: - 闹钟卡片

struct AlarmCardView: View {
    let alarm: Alarm
    let onToggle: () -> Void
    let onEdit: () -> Void
    let onDelete: () -> Void
    @State private var showingDeleteConfirm = false
    
    var body: some View {
        VStack(spacing: 0) {
            HStack(spacing: 16) {
                // 时间
                VStack(alignment: .leading, spacing: 4) {
                    Text(alarm.timeDisplay)
                        .font(.system(size: 42, weight: .semibold, design: .rounded))
                        .foregroundColor(.primary)
                    
                    Text(alarm.name.isEmpty ? "闹钟" : alarm.name)
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                    
                    Text(alarm.repeatText)
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
                
                Spacer()
                
                // 开关
                VStack(spacing: 8) {
                    Toggle("", isOn: Binding(
                        get: { alarm.enabled },
                        set: { _ in onToggle() }
                    ))
                    .toggleStyle(SwitchToggleStyle(tint: .blue))
                    .labelsHidden()
                    .scaleEffect(1.2)
                    
                    // 删除按钮
                    Button(action: { showingDeleteConfirm = true }) {
                        Image(systemName: "trash.fill")
                            .font(.system(size: 14))
                            .foregroundColor(.red.opacity(0.6))
                    }
                }
            }
            .padding()
            .contentShape(Rectangle())
            .onTapGesture {
                onEdit()
            }
        }
        .background(Color.white)
        .cornerRadius(16)
        .shadow(color: .black.opacity(0.05), radius: 8, x: 0, y: 4)
        .confirmationDialog("删除闹钟", isPresented: $showingDeleteConfirm) {
            Button("删除", role: .destructive) {
                onDelete()
            }
            Button("取消", role: .cancel) {}
        } message: {
            Text("确定要删除这个闹钟吗？")
        }
    }
}

#Preview {
    AlarmListView()
}
