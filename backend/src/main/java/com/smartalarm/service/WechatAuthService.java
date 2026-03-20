package com.smartalarm.service;

import com.smartalarm.config.WechatConfig;
import com.smartalarm.dto.WechatLoginRequest;
import com.smartalarm.model.WechatUser;
import com.smartalarm.repository.WechatUserRepository;
import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WechatAuthService {
    
    private final WechatConfig wechatConfig;
    private final WechatUserRepository wechatUserRepository;
    private final RestTemplate restTemplate;
    
    private static final String JS_CODE2SESSION_URL = 
        "https://api.weixin.qq.com/sns/jscode2session";
    
    public Map<String, Object> login(WechatLoginRequest request) {
        String code = request.getCode();
        WechatSession session = getWechatSession(code);
        
        log.info("微信登录 - OpenID: {}", session.getOpenid());
        
        WechatUser user = createOrUpdateUser(session.getOpenid(), session.getUnionid(), request.getUserInfo());
        
        Map<String, Object> response = new HashMap<>();
        response.put("openId", session.getOpenid());
        response.put("user", user);
        response.put("token", "mock_token_" + user.getOpenId());
        response.put("refreshToken", "mock_refresh_token_" + user.getOpenId());
        
        return response;
    }
    
    private WechatSession getWechatSession(String code) {
        String url = String.format("%s?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                JS_CODE2SESSION_URL, 
                wechatConfig.getAppid(), 
                wechatConfig.getSecret(), 
                code);
        
        String response = restTemplate.getForObject(url, String.class);
        JSONObject json = JSONObject.parseObject(response);
        
        if (json.containsKey("errcode")) {
            throw new RuntimeException("微信登录失败：" + json.getString("errmsg"));
        }
        
        WechatSession session = new WechatSession();
        session.setOpenid(json.getString("openid"));
        session.setSessionKey(json.getString("session_key"));
        session.setUnionid(json.getString("unionid"));
        
        return session;
    }
    
    private WechatUser createOrUpdateUser(String openId, String unionId, WechatLoginRequest.UserInfo userInfo) {
        return wechatUserRepository.findByOpenId(openId)
            .map(user -> updateUser(user, userInfo))
            .orElseGet(() -> createUser(openId, unionId, userInfo));
    }
    
    private WechatUser createUser(String openId, String unionId, WechatLoginRequest.UserInfo userInfo) {
        WechatUser user = new WechatUser();
        user.setOpenId(openId);
        user.setUnionId(unionId);
        user.setNickName(userInfo != null ? userInfo.getNickName() : "微信用户");
        user.setAvatarUrl(userInfo != null ? userInfo.getAvatarUrl() : "");
        user.setGender(userInfo != null ? userInfo.getGender() : 0);
        user.setLanguage(userInfo != null ? userInfo.getLanguage() : "zh_CN");
        user.setLastLoginAt(LocalDateTime.now());
        
        return wechatUserRepository.save(user);
    }
    
    private WechatUser updateUser(WechatUser user, WechatLoginRequest.UserInfo userInfo) {
        if (userInfo != null) {
            user.setNickName(userInfo.getNickName());
            user.setAvatarUrl(userInfo.getAvatarUrl());
            user.setGender(userInfo.getGender());
            user.setLanguage(userInfo.getLanguage());
        }
        user.setLastLoginAt(LocalDateTime.now());
        
        return wechatUserRepository.save(user);
    }
    
    public static class WechatSession {
        private String openid;
        private String sessionKey;
        private String unionid;
        
        public String getOpenid() { return openid; }
        public void setOpenid(String openid) { this.openid = openid; }
        public String getSessionKey() { return sessionKey; }
        public void setSessionKey(String sessionKey) { this.sessionKey = sessionKey; }
        public String getUnionid() { return unionid; }
        public void setUnionid(String unionid) { this.unionid = unionid; }
    }
}
