package com.smartalarm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatsDTO {
    
    private Summary summary;
    private SleepStats sleep;
    private AlarmStats alarms;
    private List<DailyStats> dailyStats;
    
    @Data
    @Builder
    public static class Summary {
        private Integer totalAlarms;
        private Integer activeAlarms;
        private Integer alarmsTriggered;
        private Double averageWakeUpTime;
        private Integer snoozeCount;
    }
    
    @Data
    @Builder
    public static class SleepStats {
        private Double averageSleepDuration;
        private Double averageWakeUpTime;
        private Integer earlyWakeups;
        private Integer lateWakeups;
        private List<String> sleepTrend;
    }
    
    @Data
    @Builder
    public static class AlarmStats {
        private Integer totalTriggers;
        private Integer onTimeWakeups;
        private Integer snoozeTriggers;
        private Double averageSnoozeCount;
        private String mostUsedAlarm;
    }
    
    @Data
    @Builder
    public static class DailyStats {
        private String date;
        private String wakeUpTime;
        private Boolean onTime;
        private Integer snoozeCount;
    }
}
