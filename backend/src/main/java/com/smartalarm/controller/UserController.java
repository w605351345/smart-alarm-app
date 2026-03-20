package com.smartalarm.controller;

import com.smartalarm.dto.ApiResponse;
import com.smartalarm.dto.UserDTO;
import com.smartalarm.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserDTO>> getUserProfile(
            @RequestHeader("X-User-Id") Long userId) {
        UserDTO user = userService.getUserById(userId);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserDTO>> updateUserProfile(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody UserDTO dto) {
        UserDTO updated = userService.updateUser(userId, dto);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ApiResponse.success(updated));
    }
}
