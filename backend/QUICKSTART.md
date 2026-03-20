# SmartAlarm 后端 - 快速启动指南

## ✅ 代码完整性确认

**检查时间**: 2026-03-20  
**状态**: ✅ 代码完整，可以启动

### 已上传文件 (24 个)

```
backend/
├── pom.xml                              ✅
├── README.md                            ✅
├── CODE_CHECKLIST.md                    ✅
└── src/main/
    ├── java/com/smartalarm/
    │   ├── SmartAlarmApplication.java   ✅
    │   ├── model/
    │   │   ├── Alarm.java               ✅
    │   │   ├── User.java                ✅
    │   │   └── WechatUser.java          ✅
    │   ├── repository/
    │   │   ├── AlarmRepository.java     ✅
    │   │   ├── UserRepository.java      ✅
    │   │   └── WechatUserRepository.java✅
    │   ├── service/
    │   │   ├── AlarmService.java        ✅
    │   │   ├── UserService.java         ✅
    │   │   ├── WechatAuthService.java   ✅
    │   │   └── WechatMessageService.java✅
    │   ├── controller/
    │   │   ├── AlarmController.java     ✅
    │   │   ├── UserController.java      ✅
    │   │   └── WechatAuthController.java✅
    │   ├── dto/
    │   │   ├── AlarmDTO.java            ✅
    │   │   ├── UserDTO.java             ✅
    │   │   ├── ApiResponse.java         ✅
    │   │   ├── WechatLoginRequest.java  ✅
    │   │   └── WechatLoginResponse.java ✅
    │   └── config/
    │       ├── SecurityConfig.java      ✅
    │       └── WechatConfig.java        ✅
    └── resources/
        └── application.properties       ✅
```

---

## 🚀 5 分钟快速启动

### 1. 环境准备

```bash
# 检查 Java 版本 (需要 11+)
java -version

# 检查 Maven 版本 (需要 3.6+)
mvn -version

# 检查数据库 (PostgreSQL 或 MySQL)
# PostgreSQL:
psql --version
# MySQL:
mysql --version
```

### 2. 创建数据库

**PostgreSQL**:
```sql
CREATE DATABASE smartalarm;
CREATE USER smartalarm WITH PASSWORD 'smartalarm123';
GRANT ALL PRIVILEGES ON DATABASE smartalarm TO smartalarm;
```

**MySQL**:
```sql
CREATE DATABASE smartalarm CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'smartalarm'@'localhost' IDENTIFIED BY 'smartalarm123';
GRANT ALL PRIVILEGES ON smartalarm.* TO 'smartalarm'@'localhost';
FLUSH PRIVILEGES;
```

### 3. 克隆代码

```bash
git clone https://github.com/w605351345/smart-alarm-app.git
cd smart-alarm-app/backend
```

### 4. 修改配置

编辑 `src/main/resources/application.properties`:

```properties
# 数据库配置 (选择一种)
# PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/smartalarm
spring.datasource.username=smartalarm
spring.datasource.password=smartalarm123

# MySQL
# spring.datasource.url=jdbc:mysql://localhost:3306/smartalarm?useSSL=false&serverTimezone=UTC
# spring.datasource.username=root
# spring.datasource.password=your_password

# JWT Secret (必须修改为随机字符串)
jwt.secret=your-random-secret-key-min-32-characters-long-please-change

# 微信小程序配置 (可选，不配置则微信登录不可用)
wechat.appid=YOUR_WECHAT_APPID
wechat.secret=YOUR_WECHAT_SECRET
```

### 5. 启动应用

```bash
# 方法 1: Maven 运行
mvn spring-boot:run

# 方法 2: 打包后运行
mvn clean package -DskipTests
java -jar target/smart-alarm-backend-1.0.0.jar
```

### 6. 验证启动

看到以下日志表示启动成功：
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v2.7.0)

...
Started SmartAlarmApplication in 5.123 seconds
```

访问 Swagger UI: http://localhost:8080/api/swagger-ui.html

---

## 📋 API 测试

### 1. 测试微信登录接口

```bash
curl -X POST http://localhost:8080/api/v1/auth/wechat/login \
  -H "Content-Type: application/json" \
  -d '{
    "code": "test_code_123",
    "userInfo": {
      "nickName": "测试用户",
      "avatarUrl": "https://example.com/avatar.png",
      "gender": 1
    }
  }'
```

### 2. 测试闹钟接口

```bash
# 获取闹钟列表
curl -X GET http://localhost:8080/api/v1/alarms \
  -H "X-User-Id: 1"

# 创建闹钟
curl -X POST http://localhost:8080/api/v1/alarms \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 1" \
  -d '{
    "time": "08:00",
    "name": "起床闹钟",
    "enabled": true,
    "repeatDays": 127,
    "vibration": true,
    "snoozeDuration": 5
  }'
```

---

## 🔧 常见问题

### 1. 端口被占用

**错误**: `Port 8080 was already in use`

**解决**: 修改 `application.properties`:
```properties
server.port=8081
```

### 2. 数据库连接失败

**错误**: `Connection refused`

**解决**:
- 检查数据库是否运行
- 检查用户名密码是否正确
- 检查数据库是否已创建

### 3. Maven 依赖下载失败

**解决**:
```bash
# 清理本地缓存
rm -rf ~/.m2/repository/com/smartalarm

# 重新下载
mvn clean install -U
```

### 4. Java 版本不匹配

**错误**: `Unsupported class file major version`

**解决**: 确保使用 Java 11+
```bash
export JAVA_HOME=/path/to/jdk-11
java -version
```

---

## 📦 部署到服务器

### Docker 部署

创建 `Dockerfile`:
```dockerfile
FROM openjdk:11-jre-slim
WORKDIR /app
COPY target/smart-alarm-backend-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

构建和运行:
```bash
docker build -t smart-alarm-backend .
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://db-host:5432/smartalarm \
  -e SPRING_DATASOURCE_USERNAME=smartalarm \
  -e SPRING_DATASOURCE_PASSWORD=smartalarm123 \
  smart-alarm-backend
```

---

## 📞 技术支持

- GitHub Issues: https://github.com/w605351345/smart-alarm-app/issues
- 邮箱：support@smartalarm.com

---

*最后更新：2026-03-20*  
*版本：1.0.0*
