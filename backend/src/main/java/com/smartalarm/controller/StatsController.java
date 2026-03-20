package com.smartalarm.controller;

import com.smartalarm.dto.ApiResponse;
import com.smartalarm.dto.StatsDTO;
import com.smartalarm.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    /**
     * 获取用户统计信息
     * @param userId 用户 ID
     * @param period 统计周期 (day/week/month)
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     */
    @GetMapping
    public ResponseEntity<ApiResponse<StatsDTO>> getStats(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "week") String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        StatsDTO stats = statsService.getUserStats(userId, period, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * 获取睡眠统计
     */
    @GetMapping("/sleep")
    public ResponseEntity<ApiResponse<StatsDTO.SleepStats>> getSleepStats(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "week") String period) {
        
        StatsDTO.SleepStats sleepStats = statsService.getSleepStats(userId, period);
        return ResponseEntity.ok(ApiResponse.success(sleepStats));
    }

    /**
     * 获取闹钟使用统计
     */
    @GetMapping("/alarms")
    public ResponseEntity<ApiResponse<StatsDTO.AlarmStats>> getAlarmStats(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "week") String period) {
        
        StatsDTO.AlarmStats alarmStats = statsService.getAlarmStats(userId, period);
        return ResponseEntity.ok(ApiResponse.success(alarmStats));
    }
}
