package com.smartalarm.dto;

import com.smartalarm.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String openId;
    private String nickName;
    private String avatarUrl;
    private Integer gender;
    private String phone;
    private String email;

    public static UserDTO fromEntity(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .openId(user.getOpenId())
                .nickName(user.getNickName())
                .avatarUrl(user.getAvatarUrl())
                .gender(user.getGender())
                .phone(user.getPhone())
                .email(user.getEmail())
                .build();
    }
}
