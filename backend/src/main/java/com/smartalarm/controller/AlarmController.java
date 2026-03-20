package com.smartalarm.controller;

import com.smartalarm.dto.AlarmDTO;
import com.smartalarm.dto.ApiResponse;
import com.smartalarm.service.AlarmService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/alarms")
@RequiredArgsConstructor
public class AlarmController {

    private final AlarmService alarmService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AlarmDTO>>> getAlarms(
            @RequestHeader("X-User-Id") Long userId) {
        List<AlarmDTO> alarms = alarmService.getAlarmsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(alarms));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AlarmDTO>> getAlarm(
            @PathVariable Long id) {
        AlarmDTO alarm = alarmService.getAlarmById(id);
        if (alarm == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ApiResponse.success(alarm));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AlarmDTO>> createAlarm(
            @RequestBody AlarmDTO dto,
            @RequestHeader("X-User-Id") Long userId) {
        dto.setUserId(userId);
        AlarmDTO created = alarmService.createAlarm(dto);
        return ResponseEntity.ok(ApiResponse.success(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AlarmDTO>> updateAlarm(
            @PathVariable Long id,
            @RequestBody AlarmDTO dto) {
        AlarmDTO updated = alarmService.updateAlarm(id, dto);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAlarm(
            @PathVariable Long id) {
        alarmService.deleteAlarm(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PutMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<AlarmDTO>> toggleAlarm(
            @PathVariable Long id) {
        AlarmDTO toggled = alarmService.toggleAlarm(id);
        if (toggled == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ApiResponse.success(toggled));
    }
}
