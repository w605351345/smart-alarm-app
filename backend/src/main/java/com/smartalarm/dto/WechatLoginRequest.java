package com.smartalarm.dto;

import lombok.Data;

@Data
public class WechatLoginRequest {
    
    private String code;
    private String encryptedData;
    private String iv;
    private UserInfo userInfo;
    
    @Data
    public static class UserInfo {
        private String nickName;
        private String avatarUrl;
        private Integer gender;
        private String language;
        private String city;
        private String province;
        private String country;
    }
}
