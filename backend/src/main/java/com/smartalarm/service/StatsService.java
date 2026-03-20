package com.smartalarm.service;

import com.smartalarm.dto.StatsDTO;
import com.smartalarm.repository.AlarmRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsService {

    private final AlarmRepository alarmRepository;
    private final Random random = new Random();

    /**
     * 获取用户统计信息
     */
    public StatsDTO getUserStats(Long userId, String period, LocalDate startDate, LocalDate endDate) {
        // 设置默认日期范围
        if (startDate == null) {
            startDate = getStartDateByPeriod(period);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        // 生成模拟统计数据（实际应该从数据库查询）
        return StatsDTO.builder()
                .summary(generateSummary(userId))
                .sleep(generateSleepStats(userId, period))
                .alarms(generateAlarmStats(userId, period))
                .dailyStats(generateDailyStats(period))
                .build();
    }

    /**
     * 获取睡眠统计
     */
    public StatsDTO.SleepStats getSleepStats(Long userId, String period) {
        return generateSleepStats(userId, period);
    }

    /**
     * 获取闹钟使用统计
     */
    public StatsDTO.AlarmStats getAlarmStats(Long userId, String period) {
        return generateAlarmStats(userId, period);
    }

    private LocalDate getStartDateByPeriod(String period) {
        LocalDate now = LocalDate.now();
        return switch (period) {
            case "day" -> now.minusDays(1);
            case "week" -> now.minusWeeks(1);
            case "month" -> now.minusMonths(1);
            case "year" -> now.minusYears(1);
            default -> now.minusWeeks(1);
        };
    }

    private StatsDTO.Summary generateSummary(Long userId) {
        int totalAlarms = alarmRepository.findByUserIdOrderByTimeAsc(userId).size();
        int activeAlarms = alarmRepository.findByUserIdAndEnabledTrueOrderByTimeAsc(userId).size();
        
        return StatsDTO.Summary.builder()
                .totalAlarms(totalAlarms)
                .activeAlarms(activeAlarms)
                .alarmsTriggered(random.nextInt(50) + 20)
                .averageWakeUpTime(7.5)
                .snoozeCount(random.nextInt(10))
                .build();
    }

    private StatsDTO.SleepStats generateSleepStats(Long userId, String period) {
        List<String> trend = new ArrayList<>();
        int days = getDaysByPeriod(period);
        for (int i = 0; i < days; i++) {
            trend.add(String.format("%.1f", 7.0 + random.nextDouble() * 2));
        }
        
        return StatsDTO.SleepStats.builder()
                .averageSleepDuration(7.5)
                .averageWakeUpTime(7.3)
                .earlyWakeups(random.nextInt(5))
                .lateWakeups(random.nextInt(3))
                .sleepTrend(trend)
                .build();
    }

    private StatsDTO.AlarmStats generateAlarmStats(Long userId, String period) {
        return StatsDTO.AlarmStats.builder()
                .totalTriggers(random.nextInt(50) + 20)
                .onTimeWakeups(random.nextInt(40) + 15)
                .snoozeTriggers(random.nextInt(20))
                .averageSnoozeCount(1.5)
                .mostUsedAlarm("起床闹钟")
                .build();
    }

    private List<StatsDTO.DailyStats> generateDailyStats(String period) {
        List<StatsDTO.DailyStats> dailyStats = new ArrayList<>();
        int days = getDaysByPeriod(period);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        for (int i = days - 1; i >= 0; i--) {
            String date = LocalDate.now().minusDays(i).format(formatter);
            dailyStats.add(StatsDTO.DailyStats.builder()
                    .date(date)
                    .wakeUpTime(String.format("%02d:%02d", 7 + random.nextInt(2), random.nextInt(60)))
                    .onTime(random.nextBoolean())
                    .snoozeCount(random.nextInt(3))
                    .build());
        }
        
        return dailyStats;
    }

    private int getDaysByPeriod(String period) {
        return switch (period) {
            case "day" -> 1;
            case "week" -> 7;
            case "month" -> 30;
            case "year" -> 365;
            default -> 7;
        };
    }
}
