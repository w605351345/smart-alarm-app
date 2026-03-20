package com.smartalarm.service;

import com.smartalarm.dto.AlarmDTO;
import com.smartalarm.model.Alarm;
import com.smartalarm.repository.AlarmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlarmService {

    private final AlarmRepository alarmRepository;

    public List<AlarmDTO> getAlarmsByUserId(Long userId) {
        return alarmRepository.findByUserIdOrderByTimeAsc(userId)
                .stream()
                .map(AlarmDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<AlarmDTO> getEnabledAlarms(Long userId) {
        return alarmRepository.findByUserIdAndEnabledTrueOrderByTimeAsc(userId)
                .stream()
                .map(AlarmDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public AlarmDTO getAlarmById(Long id) {
        return alarmRepository.findById(id)
                .map(AlarmDTO::fromEntity)
                .orElse(null);
    }

    public AlarmDTO createAlarm(AlarmDTO dto) {
        Alarm alarm = dto.toEntity();
        alarm = alarmRepository.save(alarm);
        return AlarmDTO.fromEntity(alarm);
    }

    public AlarmDTO updateAlarm(Long id, AlarmDTO dto) {
        return alarmRepository.findById(id)
                .map(alarm -> {
                    alarm.setTime(dto.getTime());
                    alarm.setName(dto.getName());
                    alarm.setEnabled(dto.getEnabled());
                    alarm.setRepeatDays(dto.getRepeatDays());
                    alarm.setRingtone(dto.getRingtone());
                    alarm.setVibration(dto.getVibration());
                    alarm.setSnoozeDuration(dto.getSnoozeDuration());
                    alarm.setWeatherAware(dto.getWeatherAware());
                    alarm = alarmRepository.save(alarm);
                    return AlarmDTO.fromEntity(alarm);
                })
                .orElse(null);
    }

    public void deleteAlarm(Long id) {
        alarmRepository.deleteById(id);
    }

    public AlarmDTO toggleAlarm(Long id) {
        return alarmRepository.findById(id)
                .map(alarm -> {
                    alarm.setEnabled(!alarm.getEnabled());
                    alarm = alarmRepository.save(alarm);
                    return AlarmDTO.fromEntity(alarm);
                })
                .orElse(null);
    }
}
