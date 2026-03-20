# 后端微信集成代码示例

## 📁 文件结构

将以下文件添加到后端项目中：

```
backend/src/main/java/com/smartalarm/
├── controller/
│   └── WechatAuthController.java      # 微信认证控制器
├── service/
│   └── WechatAuthService.java         # 微信认证服务
├── model/
│   └── WechatUser.java                # 微信用户模型
├── dto/
│   ├── WechatLoginRequest.java        # 微信登录请求
│   ├── WechatLoginResponse.java       # 微信登录响应
│   └── WechatUserInfo.java            # 微信用户信息
└── config/
    └── WechatConfig.java              # 微信配置类
```

---

## 1. 微信配置类

```java
// config/WechatConfig.java
package com.smartalarm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "wechat")
public class WechatConfig {
    
    private String appid;
    private String secret;
    private String token;
    private String encodingAesKey;
    
    // Getters and Setters
    public String getAppid() { return appid; }
    public void setAppid(String appid) { this.appid = appid; }
    
    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
    
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    
    public String getEncodingAesKey() { return encodingAesKey; }
    public void setEncodingAesKey(String encodingAesKey) { this.encodingAesKey = encodingAesKey; }
}
```

---

## 2. 微信登录请求 DTO

```java
// dto/WechatLoginRequest.java
package com.smartalarm.dto;

import lombok.Data;

@Data
public class WechatLoginRequest {
    
    /**
     * 微信登录 code（有效期 5 分钟）
     */
    private String code;
    
    /**
     * 加密的用户数据
     */
    private String encryptedData;
    
    /**
     * 加密向量
     */
    private String iv;
    
    /**
     * 用户信息（前端传递）
     */
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
```

---

## 3. 微信登录响应 DTO

```java
// dto/WechatLoginResponse.java
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
    
    public static WechatLoginResponse fromUser(User user, String token, String refreshToken) {
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
```

---

## 4. 微信用户模型

```java
// model/WechatUser.java
package com.smartalarm.model;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "wechat_users")
public class WechatUser {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 微信 OpenID
     */
    @Column(unique = true, nullable = false)
    private String openId;
    
    /**
     * 微信 UnionID（同一开放平台下唯一）
     */
    @Column(unique = true)
    private String unionId;
    
    /**
     * 昵称
     */
    private String nickName;
    
    /**
     * 头像 URL
     */
    private String avatarUrl;
    
    /**
     * 性别 (0:未知 1:男 2:女)
     */
    private Integer gender;
    
    /**
     * 语言
     */
    private String language;
    
    /**
     * 城市
     */
    private String city;
    
    /**
     * 省份
     */
    private String province;
    
    /**
     * 国家
     */
    private String country;
    
    /**
     * 手机号（需用户授权获取）
     */
    private String phone;
    
    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginAt;
    
    /**
     * 创建时间
     */
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
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
```

---

## 5. 微信认证服务

```java
// service/WechatAuthService.java
package com.smartalarm.service;

import com.smartalarm.config.WechatConfig;
import com.smartalarm.dto.WechatLoginRequest;
import com.smartalarm.model.WechatUser;
import com.smartalarm.repository.WechatUserRepository;
import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
    
    /**
     * 微信登录
     * 1. 使用 code 获取 openId 和 session_key
     * 2. 创建或更新用户
     * 3. 返回用户信息和 token
     */
    public Map<String, Object> login(WechatLoginRequest request) {
        // 1. 获取 OpenID
        String code = request.getCode();
        WechatSession session = getWechatSession(code);
        
        log.info("微信登录 - OpenID: {}", session.getOpenid());
        
        // 2. 创建或更新用户
        WechatUser user = createOrUpdateUser(session.getOpenid(), request.getUserInfo());
        
        // 3. 生成 token（使用现有的 JWT 服务）
        // 这里假设你有 JwtTokenProvider
        // String token = jwtTokenProvider.generateToken(user);
        // String refreshToken = jwtTokenProvider.generateRefreshToken(user);
        
        Map<String, Object> response = new HashMap<>();
        response.put("openId", session.getOpenid());
        response.put("user", user);
        // response.put("token", token);
        // response.put("refreshToken", refreshToken);
        
        return response;
    }
    
    /**
     * 获取微信会话信息
     */
    private WechatSession getWechatSession(String code) {
        String url = String.format("%s?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                JS_CODE2SESSION_URL, 
                wechatConfig.getAppid(), 
                wechatConfig.getSecret(), 
                code);
        
        log.debug("请求微信接口：{}", url);
        
        String response = restTemplate.getForObject(url, String.class);
        JSONObject json = JSONObject.parseObject(response);
        
        if (json.containsKey("errcode")) {
            log.error("微信登录失败：{}", json.getString("errmsg"));
            throw new RuntimeException("微信登录失败：" + json.getString("errmsg"));
        }
        
        WechatSession session = new WechatSession();
        session.setOpenid(json.getString("openid"));
        session.setSessionKey(json.getString("session_key"));
        session.setUnionid(json.getString("unionid"));
        
        return session;
    }
    
    /**
     * 创建或更新用户
     */
    private WechatUser createOrUpdateUser(String openId, WechatLoginRequest.UserInfo userInfo) {
        return wechatUserRepository.findByOpenId(openId)
            .map(user -> updateUser(user, userInfo))
            .orElseGet(() -> createUser(openId, userInfo));
    }
    
    /**
     * 创建新用户
     */
    private WechatUser createUser(String openId, WechatLoginRequest.UserInfo userInfo) {
        WechatUser user = new WechatUser();
        user.setOpenId(openId);
        user.setNickName(userInfo != null ? userInfo.getNickName() : "微信用户");
        user.setAvatarUrl(userInfo != null ? userInfo.getAvatarUrl() : "");
        user.setGender(userInfo != null ? userInfo.getGender() : 0);
        user.setLanguage(userInfo != null ? userInfo.getLanguage() : "zh_CN");
        user.setLastLoginAt(LocalDateTime.now());
        
        return wechatUserRepository.save(user);
    }
    
    /**
     * 更新用户信息
     */
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
    
    /**
     * 微信会话信息
     */
    @Data
    public static class WechatSession {
        private String openid;
        private String sessionKey;
        private String unionid;
    }
}
```

---

## 6. 微信认证控制器

```java
// controller/WechatAuthController.java
package com.smartalarm.controller;

import com.smartalarm.dto.WechatLoginRequest;
import com.smartalarm.dto.WechatLoginResponse;
import com.smartalarm.model.WechatUser;
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
    // private final JwtTokenProvider jwtTokenProvider;
    
    /**
     * 微信小程序登录
     * POST /api/v1/auth/wechat/login
     */
    @PostMapping("/login")
    public ResponseEntity<WechatLoginResponse> wechatLogin(
            @RequestBody WechatLoginRequest request) {
        
        log.info("微信登录请求 - code: {}", request.getCode());
        
        // 1. 验证 code 并获取用户信息
        Map<String, Object> result = wechatAuthService.login(request);
        WechatUser user = (WechatUser) result.get("user");
        
        // 2. 生成 JWT token
        // String token = jwtTokenProvider.generateToken(user);
        // String refreshToken = jwtTokenProvider.generateRefreshToken(user);
        
        // 临时实现（实际应该用 JWT）
        String token = "mock_token_" + user.getOpenId();
        String refreshToken = "mock_refresh_token_" + user.getOpenId();
        
        // 3. 返回响应
        WechatLoginResponse response = WechatLoginResponse.fromUser(user, token, refreshToken);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 刷新 Token
     * POST /api/v1/auth/wechat/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<WechatLoginResponse> refreshToken(
            @RequestHeader("Authorization") String refreshToken) {
        
        // 实现 token 刷新逻辑
        // ...
        
        return ResponseEntity.ok(null);
    }
    
    /**
     * 获取当前用户信息
     * GET /api/v1/auth/wechat/user-info
     */
    @GetMapping("/user-info")
    public ResponseEntity<Map<String, Object>> getUserInfo(
            @RequestHeader("Authorization") String token) {
        
        // 从 token 中解析用户信息
        // ...
        
        return ResponseEntity.ok(null);
    }
}
```

---

## 7. 微信用户 Repository

```java
// repository/WechatUserRepository.java
package com.smartalarm.repository;

import com.smartalarm.model.WechatUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WechatUserRepository extends JpaRepository<WechatUser, Long> {
    
    Optional<WechatUser> findByOpenId(String openId);
    
    Optional<WechatUser> findByUnionId(String unionId);
    
    boolean existsByOpenId(String openId);
}
```

---

## 8. 配置文件更新

```properties
# application.properties

# ==================== 微信小程序配置 ====================
wechat.appid=YOUR_WECHAT_APPID
wechat.secret=YOUR_WECHAT_SECRET
wechat.token=YOUR_WECHAT_TOKEN
wechat.encodingAesKey=YOUR_ENCODING_AES_KEY

# ==================== JWT 配置 ====================
jwt.secret=YOUR_JWT_SECRET_KEY_MIN_32_CHARS
jwt.expiration=86400000
jwt.refresh-expiration=604800000
```

---

## 9. 订阅消息推送服务

```java
// service/WechatMessageService.java
package com.smartalarm.service;

import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class WechatMessageService {
    
    private final WechatConfig wechatConfig;
    private final RestTemplate restTemplate;
    private final StringRedisTemplate redisTemplate;
    
    private static final String ACCESS_TOKEN_URL = 
        "https://api.weixin.qq.com/cgi-bin/token";
    
    private static final String MESSAGE_SEND_URL = 
        "https://api.weixin.qq.com/cgi-bin/message/subscribe/send";
    
    /**
     * 获取访问令牌（带缓存）
     */
    private String getAccessToken() {
        String cacheKey = "wechat:access_token";
        
        // 从缓存获取
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }
        
        // 请求新的 token
        String url = String.format("%s?grant_type=client_credential&appid=%s&secret=%s",
                ACCESS_TOKEN_URL, wechatConfig.getAppid(), wechatConfig.getSecret());
        
        String response = restTemplate.getForObject(url, String.class);
        JSONObject json = JSONObject.parseObject(response);
        
        String accessToken = json.getString("access_token");
        int expiresIn = json.getIntValue("expires_in");
        
        // 缓存 token（提前 5 分钟过期）
        redisTemplate.opsForValue().set(cacheKey, accessToken, expiresIn - 300, TimeUnit.SECONDS);
        
        log.info("获取微信 access_token: {}", accessToken.substring(0, 10) + "...");
        
        return accessToken;
    }
    
    /**
     * 发送订阅消息
     */
    public void sendSubscribeMessage(String openId, String templateId, 
                                     Map<String, MessageData> data, String page) {
        String accessToken = getAccessToken();
        
        JSONObject message = new JSONObject();
        message.put("touser", openId);
        message.put("template_id", templateId);
        message.put("page", page != null ? page : "pages/index/index");
        
        JSONObject messageData = new JSONObject();
        data.forEach((key, value) -> {
            JSONObject item = new JSONObject();
            item.put("value", value.value);
            if (value.color != null) {
                item.put("color", value.color);
            }
            messageData.put(key, item);
        });
        message.put("data", messageData);
        
        String url = MESSAGE_SEND_URL + "?access_token=" + accessToken;
        
        log.info("发送订阅消息：{}", message.toJSONString());
        
        ResponseEntity<String> response = restTemplate.postForEntity(url, message.toString(), String.class);
        
        JSONObject result = JSONObject.parseObject(response.getBody());
        if (result.getIntValue("errcode") != 0) {
            log.error("发送订阅消息失败：{}", result.getString("errmsg"));
        }
    }
    
    /**
     * 发送闹钟提醒
     */
    public void sendAlarmReminder(String openId, String alarmTime, String alarmName) {
        Map<String, MessageData> data = new HashMap<>();
        data.put("thing1", new MessageData(alarmName)); // 闹钟名称
        data.put("time2", new MessageData(alarmTime));  // 闹钟时间
        data.put("thing3", new MessageData("记得起床哦")); // 提醒内容
        
        sendSubscribeMessage(openId, "YOUR_TEMPLATE_ID", data, "pages/index/index");
    }
    
    /**
     * 消息数据
     */
    @Data
    @RequiredArgsConstructor
    public static class MessageData {
        private final String value;
        private String color;
    }
}
```

---

## 10. 测试接口

```bash
# 测试微信登录
curl -X POST http://localhost:8080/api/v1/auth/wechat/login \
  -H "Content-Type: application/json" \
  -d '{
    "code": "YOUR_TEST_CODE",
    "userInfo": {
      "nickName": "测试用户",
      "avatarUrl": "https://example.com/avatar.png",
      "gender": 1
    }
  }'

# 预期响应
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "openId": "oXXXX-XXXXXXXXXXXXXXXX",
    "nickName": "测试用户",
    "avatarUrl": "https://example.com/avatar.png",
    "gender": 1
  }
}
```

---

## 11. 依赖添加

```xml
<!-- pom.xml -->
<dependencies>
    <!-- FastJSON -->
    <dependency>
        <groupId>com.alibaba</groupId>
        <artifactId>fastjson</artifactId>
        <version>2.0.32</version>
    </dependency>
    
    <!-- Redis -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
</dependencies>
```

---

## ✅ 完成检查清单

- [ ] 添加微信配置类
- [ ] 添加微信用户模型和 Repository
- [ ] 添加微信登录请求/响应 DTO
- [ ] 添加微信认证服务
- [ ] 添加微信认证控制器
- [ ] 添加订阅消息推送服务
- [ ] 更新 application.properties 配置
- [ ] 添加 FastJSON 和 Redis 依赖
- [ ] 测试微信登录接口
- [ ] 测试订阅消息推送

---

*最后更新：2026-03-20*
