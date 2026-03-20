package com.smartalarm.service;

import com.smartalarm.dto.UserDTO;
import com.smartalarm.model.User;
import com.smartalarm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserDTO getUserByOpenId(String openId) {
        return userRepository.findByOpenId(openId)
                .map(UserDTO::fromEntity)
                .orElse(null);
    }

    public UserDTO getUserById(Long id) {
        return userRepository.findById(id)
                .map(UserDTO::fromEntity)
                .orElse(null);
    }

    public UserDTO updateUser(Long id, UserDTO dto) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setNickName(dto.getNickName());
                    user.setAvatarUrl(dto.getAvatarUrl());
                    user.setGender(dto.getGender());
                    user.setPhone(dto.getPhone());
                    user.setEmail(dto.getEmail());
                    user = userRepository.save(user);
                    return UserDTO.fromEntity(user);
                })
                .orElse(null);
    }
}
