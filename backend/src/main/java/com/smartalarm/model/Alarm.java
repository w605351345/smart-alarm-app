package com.smartalarm.model;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 闹钟实体类
 */
@Data
@Entity
@Table(name = "alarms")
public class Alarm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 5)
    private String time;

    private String name;

    @Column(nullable = false)
    private Boolean enabled = true;

    /**
     * 重复设置 (位掩码：周日=1, 周一=2, ..., 周六=64)
     * 0=仅一次，127=每天
     */
    private Integer repeatDays = 0;

    private String ringtone;

    private Boolean vibration = true;

    private Integer snoozeDuration = 5;

    private Boolean weatherAware = false;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
