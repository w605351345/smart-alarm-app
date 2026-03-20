package com.smartalarm.dto;

import com.smartalarm.model.Alarm;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlarmDTO {
    private Long id;
    private Long userId;
    private String time;
    private String name;
    private Boolean enabled;
    private Integer repeatDays;
    private String repeatText;
    private String ringtone;
    private Boolean vibration;
    private Integer snoozeDuration;
    private Boolean weatherAware;

    public static AlarmDTO fromEntity(Alarm alarm) {
        return AlarmDTO.builder()
                .id(alarm.getId())
                .userId(alarm.getUserId())
                .time(alarm.getTime())
                .name(alarm.getName())
                .enabled(alarm.getEnabled())
                .repeatDays(alarm.getRepeatDays())
                .repeatText(getRepeatText(alarm.getRepeatDays()))
                .ringtone(alarm.getRingtone())
                .vibration(alarm.getVibration())
                .snoozeDuration(alarm.getSnoozeDuration())
                .weatherAware(alarm.getWeatherAware())
                .build();
    }

    public Alarm toEntity() {
        Alarm alarm = new Alarm();
        alarm.setId(id);
        alarm.setUserId(userId);
        alarm.setTime(time);
        alarm.setName(name);
        alarm.setEnabled(enabled);
        alarm.setRepeatDays(repeatDays);
        alarm.setRingtone(ringtone);
        alarm.setVibration(vibration);
        alarm.setSnoozeDuration(snoozeDuration);
        alarm.setWeatherAware(weatherAware);
        return alarm;
    }

    private static String getRepeatText(Integer repeatDays) {
        if (repeatDays == null || repeatDays == 0) return "仅一次";
        if (repeatDays == 127) return "每天";
        if (repeatDays == 62) return "工作日";
        if (repeatDays == 65) return "周末";
        return "自定义";
    }
}
