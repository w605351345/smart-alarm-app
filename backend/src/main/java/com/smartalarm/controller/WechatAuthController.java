package com.smartalarm.controller;

import com.smartalarm.dto.ApiResponse;
import com.smartalarm.dto.WechatLoginRequest;
import com.smartalarm.dto.WechatLoginResponse;
import com.smartalarm.service.WechatAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth/wechat")
@RequiredArgsConstructor
public class WechatAuthController {
    
    private final WechatAuthService wechatAuthService;
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<WechatLoginResponse>> wechatLogin(
            @RequestBody WechatLoginRequest request) {
        
        log.info("微信登录请求 - code: {}", request.getCode());
        
        Map<String, Object> result = wechatAuthService.login(request);
        
        WechatLoginResponse response = WechatLoginResponse.fromUser(
            (com.smartalarm.model.WechatUser) result.get("user"),
            (String) result.get("token"),
            (String) result.get("refreshToken")
        );
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<WechatLoginResponse>> refreshToken(
            @RequestHeader("Authorization") String refreshToken) {
        return ResponseEntity.ok(ApiResponse.error(501, "Not Implemented"));
    }
}
