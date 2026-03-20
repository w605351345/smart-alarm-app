package com.smartalarm.model;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 微信用户实体类
 */
@Data
@Entity
@Table(name = "wechat_users")
public class WechatUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String openId;

    @Column(unique = true)
    private String unionId;

    private String nickName;

    private String avatarUrl;

    private Integer gender;

    private String language;

    private String city;

    private String province;

    private String country;

    private String phone;

    private LocalDateTime lastLoginAt;

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
