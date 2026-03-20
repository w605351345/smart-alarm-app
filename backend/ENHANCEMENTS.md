# SmartAlarm 后端 - 增强功能说明

## ✅ 新增增强功能

**更新时间**: 2026-03-20

本次更新添加了以下增强功能，使后端服务更加完善和安全。

---

## 🔐 安全认证增强

### 1. JWT Token 管理 (JwtTokenProvider)

**功能**:
- ✅ 生成访问令牌（Access Token）
- ✅ 生成刷新令牌（Refresh Token）
- ✅ 验证令牌有效性
- ✅ 令牌刷新
- ✅ 自动密钥长度检查

**使用示例**:
```java
@Autowired
private JwtTokenProvider jwtTokenProvider;

// 生成 token
String token = jwtTokenProvider.createToken(userDetails);
String refreshToken = jwtTokenProvider.createRefreshToken(userId);

// 验证 token
boolean isValid = jwtTokenProvider.validateToken(token);

// 刷新 token
String newToken = jwtTokenProvider.refreshToken(token);
```

### 2. JWT 认证过滤器 (JwtAuthenticationFilter)

**功能**:
- ✅ 从请求头提取 JWT（支持 `Authorization: Bearer` 和 `X-Auth-Token`）
- ✅ 自动验证 token 有效性
- ✅ 设置 Spring Security 上下文
- ✅ 无状态认证

**配置**:
```java
// SecurityConfig 中已自动配置
http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
```

### 3. 自定义用户详情服务 (CustomUserDetailsService)

**功能**:
- ✅ 实现 Spring Security UserDetailsService
- ✅ 支持按 ID 加载用户
- ✅ 支持按 OpenID 加载用户
- ✅ 自动权限分配

---

## ⚠️ 全局异常处理

### GlobalExceptionHandler

**处理的异常类型**:

| 异常类型 | HTTP 状态码 | 说明 |
|----------|-----------|------|
| ResourceNotFoundException | 404 | 资源未找到 |
| BadRequestException | 400 | 参数错误 |
| MethodArgumentNotValidException | 400 | 参数验证失败 |
| ExpiredJwtException | 401 | Token 过期 |
| SignatureException | 401 | Token 签名错误 |
| MalformedJwtException | 401 | Token 格式错误 |
| BadCredentialsException | 401 | 认证失败 |
| AccessDeniedException | 403 | 访问拒绝 |
| Exception | 500 | 其他异常 |

**统一响应格式**:
```json
{
  "code": 404,
  "message": "Alarm not found with id: '123'",
  "data": null
}
```

---

## 📊 统计功能

### StatsController

**API 接口**:

| 接口 | 说明 | 参数 |
|------|------|------|
| `GET /api/v1/stats` | 获取用户统计 | period (day/week/month/year) |
| `GET /api/v1/stats/sleep` | 睡眠统计 | period |
| `GET /api/v1/stats/alarms` | 闹钟使用统计 | period |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "summary": {
      "totalAlarms": 5,
      "activeAlarms": 3,
      "alarmsTriggered": 35,
      "averageWakeUpTime": 7.5,
      "snoozeCount": 5
    },
    "sleep": {
      "averageSleepDuration": 7.5,
      "averageWakeUpTime": 7.3,
      "earlyWakeups": 2,
      "lateWakeups": 1,
      "sleepTrend": ["7.2", "7.5", "7.8", ...]
    },
    "alarms": {
      "totalTriggers": 35,
      "onTimeWakeups": 28,
      "snoozeTriggers": 7,
      "averageSnoozeCount": 1.5,
      "mostUsedAlarm": "起床闹钟"
    },
    "dailyStats": [...]
  }
}
```

### StatsService

**功能**:
- ✅ 用户统计汇总
- ✅ 睡眠数据分析
- ✅ 闹钟使用统计
- ✅ 每日趋势统计
- ✅ 支持多种周期（日/周/月/年）

---

## 🌤️ 天气功能

### WeatherController

**API 接口**:

| 接口 | 说明 | 参数 |
|------|------|------|
| `GET /api/v1/weather` | 当前天气 | city, latitude, longitude |
| `GET /api/v1/weather/forecast` | 天气预报 | city, days |
| `GET /api/v1/weather/air-quality` | 空气质量 | city |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "city": "北京市",
    "description": "晴",
    "temperature": 26.5,
    "feelsLike": 27.0,
    "humidity": 55,
    "weatherIcon": "☀️",
    "timestamp": 1711008000000,
    "forecast": {
      "daily": [
        {
          "date": "2026-03-20",
          "description": "晴",
          "tempMin": 22.5,
          "tempMax": 29.0,
          "humidity": 50,
          "weatherIcon": "☀️"
        }
      ]
    },
    "airQuality": {
      "aqi": 65,
      "level": "良",
      "pm25": 35.5,
      "pm10": 58.0
    }
  }
}
```

### WeatherService

**功能**:
- ✅ 当前天气查询
- ✅ 多日天气预报
- ✅ 空气质量查询
- ✅ 支持城市名和经纬度
- ⚠️ 当前使用模拟数据（需接入真实天气 API）

**接入真实天气 API**:
```java
// 修改 WeatherService 中的方法
// 推荐使用：
// - 和风天气 API (https://dev.qweather.com/)
// - OpenWeatherMap (https://openweathermap.org/api)
// - 心知天气 (https://www.seniverse.com/)
```

---

## 🔄 更新后的 SecurityConfig

**主要变更**:
- ✅ 集成 JwtTokenProvider
- ✅ 集成 JwtAuthenticationFilter
- ✅ 自动配置 CORS
- ✅ 支持多种认证头格式

**公开接口** (无需认证):
- `/api/v1/auth/**` - 认证接口
- `/api-docs/**`, `/swagger-ui/**` - API 文档
- `/api/actuator/**` - 健康检查

**需要认证的接口**:
- `/api/v1/**` - 所有业务接口

---

## 📦 完整文件清单

### 新增文件 (13 个)

```
backend/src/main/java/com/smartalarm/
├── security/
│   ├── JwtTokenProvider.java           ✅ JWT 令牌管理
│   └── JwtAuthenticationFilter.java    ✅ JWT 认证过滤器
├── service/
│   ├── CustomUserDetailsService.java   ✅ 用户详情服务
│   ├── StatsService.java               ✅ 统计服务
│   └── WeatherService.java             ✅ 天气服务
├── exception/
│   ├── ResourceNotFoundException.java  ✅ 资源未找到异常
│   ├── BadRequestException.java        ✅ 参数错误异常
│   └── GlobalExceptionHandler.java     ✅ 全局异常处理
├── controller/
│   ├── StatsController.java            ✅ 统计接口
│   └── WeatherController.java          ✅ 天气接口
├── dto/
│   ├── StatsDTO.java                   ✅ 统计数据传输对象
│   └── WeatherDTO.java                 ✅ 天气数据传输对象
└── config/
    └── SecurityConfig.java             ✅ 安全配置（已更新）
```

### 总计文件数

| 类别 | 数量 |
|------|------|
| Model 层 | 3 |
| Repository 层 | 3 |
| Service 层 | 7 |
| Controller 层 | 5 |
| DTO 层 | 8 |
| Config 层 | 2 |
| Security 层 | 2 |
| Exception 层 | 3 |
| **总计** | **33 个 Java 文件** |

---

## 🚀 使用指南

### 1. JWT 认证

**登录获取 Token**:
```bash
POST /api/v1/auth/wechat/login
{
  "code": "wechat_code",
  "userInfo": {...}
}

Response:
{
  "code": 200,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

**使用 Token 访问接口**:
```bash
GET /api/v1/alarms
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 2. 统计接口

```bash
# 获取周统计
GET /api/v1/stats?period=week
X-User-Id: 1

# 获取睡眠统计
GET /api/v1/stats/sleep?period=month
X-User-Id: 1
```

### 3. 天气接口

```bash
# 获取当前天气
GET /api/v1/weather?city=北京市

# 获取 7 天预报
GET /api/v1/weather/forecast?days=7

# 获取空气质量
GET /api/v1/weather/air-quality?city=北京市
```

---

## ⚙️ 配置说明

### application.properties

```properties
# JWT 配置
jwt.secret=your-secret-key-must-be-at-least-32-characters-long
jwt.expiration=86400000          # 24 小时
jwt.refresh-expiration=604800000  # 7 天

# 天气 API 配置（可选）
weather.api.key=YOUR_API_KEY
weather.api.provider=lewu
```

---

## 📝 待办事项

### 下一步优化

- [ ] 接入真实天气 API（和风天气/OpenWeatherMap）
- [ ] 实现真实的统计数据持久化
- [ ] 添加统计定时任务（每日汇总）
- [ ] 实现闹钟触发记录
- [ ] 添加睡眠分析算法
- [ ] 实现智能唤醒建议

---

*最后更新：2026-03-20*
*版本：2.0.0*
