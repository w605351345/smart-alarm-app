package com.smartalarm.dto;

import com.smartalarm.model.WechatUser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WechatLoginResponse {
    
    private String token;
    private String refreshToken;
    private UserDTO user;
    
    @Data
    @Builder
    public static class UserDTO {
        private Long id;
        private String openId;
        private String nickName;
        private String avatarUrl;
        private Integer gender;
        private String phone;
    }
    
    public static WechatLoginResponse fromUser(WechatUser user, String token, String refreshToken) {
        return WechatLoginResponse.builder()
            .token(token)
            .refreshToken(refreshToken)
            .user(UserDTO.builder()
                .id(user.getId())
                .openId(user.getOpenId())
                .nickName(user.getNickName())
                .avatarUrl(user.getAvatarUrl())
                .gender(user.getGender())
                .phone(user.getPhone())
                .build())
            .build();
    }
}
