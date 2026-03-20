# Java 8 兼容性指南

## ✅ 已完成的兼容性修改

**更新时间**: 2026-03-20

本项目已完全兼容 Java 8 (1.8)，可以在 Java 8 环境下正常编译和运行。

---

## 🔧 主要修改内容

### 1. pom.xml 配置

**修改前** (Java 11):
```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.7.0</version>
</parent>
<properties>
    <java.version>11</java.version>
</properties>
```

**修改后** (Java 8):
```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.3.12.RELEASE</version>
</parent>
<properties>
    <java.version>1.8</java.version>
    <maven.compiler.source>1.8</maven.compiler.source>
    <maven.compiler.target>1.8</maven.compiler.target>
</properties>
```

### 2. 依赖版本调整

| 依赖 | Java 11 版本 | Java 8 版本 |
|------|-------------|-----------|
| Spring Boot | 2.7.0 | 2.3.12.RELEASE |
| JWT (jjwt) | 0.11.5 | 0.9.1 |
| FastJSON | 2.0.32 | 1.2.83 |
| MySQL Connector | 8.0.33 (mysql-connector-j) | 8.0.33 (mysql-connector-java) |
| Swagger | springdoc-openapi 1.7.0 | springfox-swagger2 2.9.2 |

### 3. 代码语法修改

#### Switch 表达式 → Switch-Case

**修改前** (Java 14+):
```java
return switch (period) {
    case "day" -> now.minusDays(1);
    case "week" -> now.minusWeeks(1);
    case "month" -> now.minusMonths(1);
    default -> now.minusWeeks(1);
};
```

**修改后** (Java 8):
```java
if ("day".equals(period)) {
    return now.minusDays(1);
} else if ("week".equals(period)) {
    return now.minusWeeks(1);
} else if ("month".equals(period)) {
    return now.minusMonths(1);
} else {
    return now.minusWeeks(1);
}
```

#### List.of() → Arrays.asList() 或 Collections.singletonList()

**修改前** (Java 9+):
```java
configuration.setAllowedOrigins(List.of("*"));
```

**修改后** (Java 8):
```java
configuration.setAllowedOrigins(Collections.singletonList("*"));
```

#### var 关键字 → 显式类型

**修改前** (Java 10+):
```java
var list = new ArrayList<String>();
```

**修改后** (Java 8):
```java
List<String> list = new ArrayList<String>();
```

### 4. SecurityConfig 修改

**修改前** (Spring Security 5.7+):
```java
.authorizeHttpRequests()
```

**修改后** (Spring Security 5.3):
```java
.authorizeRequests()
```

---

## 📦 修改的文件清单

| 文件 | 修改内容 |
|------|---------|
| pom.xml | Spring Boot 版本、Java 版本、依赖版本 |
| SecurityConfig.java | authorizeHttpRequests → authorizeRequests, List.of → singletonList |
| StatsService.java | switch 表达式 → if-else |
| WeatherService.java | switch 表达式 → if-else |

---

## 🚀 Java 8 环境启动指南

### 1. 确认 Java 版本

```bash
java -version
# 应该显示：java version "1.8.0_xxx"
```

### 2. 设置 JAVA_HOME (如果需要)

**Linux/Mac**:
```bash
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH
```

**Windows**:
```cmd
set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_xxx
set PATH=%JAVA_HOME%\bin;%PATH%
```

### 3. 克隆项目

```bash
git clone https://github.com/w605351345/smart-alarm-app.git
cd smart-alarm-app/backend
```

### 4. 清理并重新编译

```bash
# 清理旧的编译产物
mvn clean

# 重新编译
mvn clean package -DskipTests
```

### 5. 启动应用

```bash
# 方式 1: Maven 运行
mvn spring-boot:run

# 方式 2: 直接运行 jar
java -jar target/smart-alarm-backend-1.0.0.jar
```

---

## ✅ 兼容性验证

### 编译验证

```bash
mvn clean compile
```

应该看到：
```
[INFO] Compiling 32 source files to /path/to/target/classes
[INFO] Nothing to compile - all classes are up to date
[INFO] BUILD SUCCESS
```

### 运行验证

启动后查看日志：
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::       (v2.3.12.RELEASE)

...
Started SmartAlarmApplication in X.XXX seconds
```

### API 验证

```bash
# 访问 Swagger UI
curl http://localhost:8080/api/swagger-ui.html

# 健康检查
curl http://localhost:8080/api/actuator/health
```

---

## ⚠️ 注意事项

### 1. Lombok 配置

Java 8 使用 Lombok 需要在 `pom.xml` 中添加：

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.8.1</version>
            <configuration>
                <source>1.8</source>
                <target>1.8</target>
                <annotationProcessorPaths>
                    <path>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                        <version>${lombok.version}</version>
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### 2. IDE 配置

**IntelliJ IDEA**:
1. File → Project Structure → Project
2. Project SDK: 选择 Java 1.8
3. Project language level: 选择 8

**Eclipse**:
1. Right-click project → Properties
2. Java Compiler → Compiler compliance level: 1.8

### 3. Maven 版本

确保 Maven 版本 >= 3.6:
```bash
mvn -version
```

---

## 📊 版本对比

| 特性 | Java 8 版本 | Java 11 版本 |
|------|-----------|------------|
| Spring Boot | 2.3.12.RELEASE | 2.7.0 |
| JWT 库 | jjwt 0.9.1 | jjwt 0.11.5 |
| Swagger | springfox 2.9.2 | springdoc 1.7.0 |
| 语法特性 | 传统语法 | Switch 表达式、var 等 |
| 性能 | 稳定 | 略优 |
| 兼容性 | 最广泛 | 需要 Java 11+ |

---

## 🎯 推荐使用场景

**使用 Java 8**:
- ✅ 生产环境稳定性要求高
- ✅ 服务器已部署 Java 8
- ✅ 团队熟悉 Java 8
- ✅ 不需要最新 Java 特性

**使用 Java 11+**:
- ✅ 新项目，无历史包袱
- ✅ 需要最新 Java 特性
- ✅ 追求更好性能
- ✅ 容器化部署（Docker）

---

## 📞 常见问题

### Q: 为什么选择 Spring Boot 2.3.12？

A: 这是最后一个完全兼容 Java 8 的 Spring Boot 2.3.x 版本，稳定性好，社区支持完善。

### Q: JWT 为什么降级到 0.9.1？

A: jjwt 0.10+ 需要 Java 11+，0.9.1 是最后一个支持 Java 8 的稳定版本。

### Q: 可以用 Java 8 编译但 Java 11 运行吗？

A: 可以，但建议编译和运行使用相同版本。

### Q: 性能会受影响吗？

A: Java 8 性能已经非常成熟，对于本项目的负载场景，性能差异可以忽略。

---

## ✅ 验证清单

- [x] pom.xml Java 版本配置为 1.8
- [x] Spring Boot 版本降级为 2.3.12.RELEASE
- [x] JWT 库降级为 0.9.1
- [x] Swagger 改为 springfox 2.9.2
- [x] switch 表达式改为 if-else
- [x] List.of() 改为 Collections.singletonList()
- [x] authorizeHttpRequests 改为 authorizeRequests
- [x] Java 8 环境编译通过
- [x] Java 8 环境运行正常

---

*最后更新：2026-03-20*  
*Java 版本：1.8*  
*Spring Boot 版本：2.3.12.RELEASE*
