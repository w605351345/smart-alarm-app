# 数字解密闹钟功能文档

## 📋 功能概述

数字解密闹钟是一个防赖床功能，用户开启后，关闭闹钟时需要完成相应的解密题目，答对才能关闭闹钟。

### 难度级别

| 难度 | 标识 | 超时时间 | 最大尝试 | 题目示例 |
|------|------|----------|----------|----------|
| 简单 | ⭐ | 30 秒 | 5 次 | 10 以内加减法，3 位数字记忆 |
| 中等 | ⭐⭐ | 60 秒 | 3 次 | 50 以内混合运算，5 位数字记忆 |
| 困难 | ⭐⭐⭐ | 90 秒 | 2 次 | 100 以内复杂运算，7 位数字记忆 |

### 题目类型

| 类型 | 图标 | 说明 | 示例 |
|------|------|------|------|
| 数学计算 | 🔢 | 加减乘除运算 | 25 + 17 - 8 = ? |
| 记忆数字 | 🧠 | 记住并输入数字 | 请记住：73529 |
| 数字序列 | 📈 | 找出数字规律 | 2, 4, 8, 16, ? |
| 算术应用 | 📝 | 生活应用题 | 小明有 5 个苹果... |

---

## 🎯 使用流程

### 1. 设置解密闹钟

```
闹钟列表 → 编辑闹钟 → 解密挑战 → 选择难度和类型 → 保存
```

### 2. 闹钟触发

```
闹钟响起 → 显示解密题目 → 输入答案 → 验证 → 成功关闭/继续响铃
```

### 3. 答题界面

- **倒计时显示**: 剩余时间
- **题目展示**: 当前题目内容
- **答案输入**: 数字/文本输入框
- **剩余次数**: 还可尝试次数
- **结果反馈**: 正确/错误提示

---

## 📱 小程序页面

### 配置页面 (pages/challenge/challenge)

**功能**:
- 选择难度（简单/中等/困难）
- 选择题目类型（数学/记忆/序列/算术）
- 启用/禁用解密功能

**UI 元素**:
- 难度卡片（带星级和说明）
- 题目类型卡片（带图标）
- 启用开关
- 保存按钮

### 答题页面 (pages/challenge/answer)

**功能**:
- 显示题目
- 倒计时
- 答案输入
- 提交验证
- 结果反馈

**UI 元素**:
- 渐变背景
- 题目卡片
- 输入框
- 提交按钮
- 结果弹窗

---

## 🔧 API 接口

### 获取难度配置

```http
GET /api/v1/challenges/difficulties
```

**响应**:
```json
{
  "code": 200,
  "data": [
    {
      "value": "EASY",
      "label": "简单",
      "level": 1,
      "timeout": 30,
      "maxAttempts": 5,
      "description": "10 以内加减法，3 位数字记忆"
    }
  ]
}
```

### 获取题目类型

```http
GET /api/v1/challenges/types
```

**响应**:
```json
{
  "code": 200,
  "data": [
    {
      "value": "MATH",
      "label": "数学计算",
      "icon": "🔢",
      "description": "加减乘除运算"
    }
  ]
}
```

### 设置闹钟解密

```http
POST /api/v1/challenges/alarms/{alarmId}
Content-Type: application/json

{
  "enabled": true,
  "difficulty": "MEDIUM",
  "challengeType": "MATH"
}
```

### 获取闹钟解密配置

```http
GET /api/v1/challenges/alarms/{alarmId}
```

### 生成挑战题目

```http
POST /api/v1/challenges/generate
Content-Type: application/json

{
  "alarmId": 1,
  "difficulty": "MEDIUM",
  "challengeType": "MATH"
}
```

**响应**:
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "question": "25 + 17 - 8 = ?",
    "questionType": "math",
    "remainingAttempts": 3,
    "timeoutSeconds": 60,
    "expiresAt": 1711000000000
  }
}
```

### 验证答案

```http
POST /api/v1/challenges/verify
Content-Type: application/json

{
  "challengeId": 1,
  "answer": "34"
}
```

**响应（正确）**:
```json
{
  "code": 200,
  "data": {
    "correct": true,
    "remainingAttempts": 2,
    "message": "回答正确！闹钟已关闭",
    "alarmDismissed": true
  }
}
```

**响应（错误）**:
```json
{
  "code": 200,
  "data": {
    "correct": false,
    "remainingAttempts": 2,
    "message": "回答错误，还剩 2 次机会",
    "alarmDismissed": false
  }
}
```

---

## 💾 数据库设计

### alarm_challenge 表

```sql
CREATE TABLE alarm_challenge (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    alarm_id BIGINT NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    difficulty VARCHAR(20),
    challenge_type VARCHAR(20),
    question_data TEXT,
    answer VARCHAR(100),
    max_attempts INT DEFAULT 3,
    timeout_seconds INT DEFAULT 60,
    FOREIGN KEY (alarm_id) REFERENCES alarm(id),
    INDEX idx_alarm_id (alarm_id)
);
```

**字段说明**:
- `alarm_id`: 关联的闹钟 ID
- `enabled`: 是否启用解密
- `difficulty`: 难度级别 (EASY/MEDIUM/HARD)
- `challenge_type`: 题目类型 (MATH/MEMORY/SEQUENCE/ARITHMETIC)
- `question_data`: 题目数据（JSON 格式）
- `answer`: 正确答案
- `max_attempts`: 最大尝试次数
- `timeout_seconds`: 超时时间（秒）

---

## 🎨 UI 设计

### 配置页面

```
┌─────────────────────────┐
│      闹钟解密            │
│  答题成功后才能关闭闹钟   │
├─────────────────────────┤
│  选择难度               │
│  ┌─────────────────┐   │
│  │ ⭐ 简单          │   │
│  │ 10 以内加减法    │   │
│  │ ⏱️30 秒 ✏️5 次    │   │
│  └─────────────────┘   │
│  ┌─────────────────┐   │
│  │ ⭐⭐ 中等         │   │
│  │ 50 以内混合运算  │   │
│  │ ⏱️60 秒 ✏️3 次    │   │
│  └─────────────────┘   │
│  ┌─────────────────┐   │
│  │ ⭐⭐⭐ 困难        │   │
│  │ 100 以内复杂运算 │   │
│  │ ⏱️90 秒 ✏️2 次    │   │
│  └─────────────────┘   │
├─────────────────────────┤
│  题目类型               │
│  ┌──────┐ ┌──────┐    │
│  │ 🔢   │ │ 🧠   │    │
│  │数学  │ │记忆  │    │
│  └──────┘ └──────┘    │
│  ┌──────┐ ┌──────┐    │
│  │ 📈   │ │ 📝   │    │
│  │序列  │ │算术  │    │
│  └──────┘ └──────┘    │
├─────────────────────────┤
│  启用解密功能  [开关]   │
│  开启后，关闭闹钟需答题  │
├─────────────────────────┤
│      [ 保存设置 ]       │
└─────────────────────────┘
```

### 答题页面

```
┌─────────────────────────┐
│      解密挑战           │
│    ⏱️ 45 秒             │
├─────────────────────────┤
│  ┌─────────────────┐   │
│  │                 │   │
│  │  25 + 17 - 8 = ?│   │
│  │                 │   │
│  │  [__________]   │   │
│  │                 │   │
│  │  剩余机会：2 次   │   │
│  │                 │   │
│  │  [ 提交答案 ]   │   │
│  └─────────────────┘   │
│                         │
│  💡 答对题目才能关闭闹钟 │
└─────────────────────────┘
```

### 结果弹窗

```
┌─────────────────────────┐
│                         │
│         ✅              │
│      回答正确！         │
│   闹钟已关闭            │
│                         │
│    [ 关闭闹钟 ]         │
│                         │
└─────────────────────────┘
```

---

## 📦 文件清单

### 后端文件

```
backend/src/main/java/com/smartalarm/
├── model/
│   └── AlarmChallenge.java          # 解密挑战模型
├── repository/
│   └── AlarmChallengeRepository.java # 数据访问层
├── service/
│   ├── ChallengeGeneratorService.java # 题目生成服务
│   └── ChallengeService.java         # 挑战业务逻辑
├── controller/
│   └── ChallengeController.java      # API 控制器
└── dto/
    └── ChallengeDTO.java             # 数据传输对象
```

### 小程序文件

```
miniprogram/pages/challenge/
├── challenge.wxml    # 配置页面结构
├── challenge.wxss    # 配置页面样式
├── challenge.js      # 配置页面逻辑
├── answer.wxml       # 答题页面结构
├── answer.wxss       # 答题页面样式
└── answer.js         # 答题页面逻辑
```

---

## 🔐 安全考虑

1. **答案验证**: 服务端验证，防止客户端作弊
2. **超时控制**: 服务端记录过期时间
3. **尝试限制**: 防止暴力破解
4. **题目随机**: 每次生成新题目

---

## 🚀 扩展建议

### 未来功能

1. **更多题型**: 
   - 图形识别
   - 颜色匹配
   - 反应测试

2. **难度自定义**:
   - 用户自定义数字范围
   - 自定义运算类型

3. **成就系统**:
   - 连续答对奖励
   - 难度解锁

4. **统计功能**:
   - 答题正确率
   - 平均答题时间

5. **语音答题**:
   - 语音识别答案
   - 防止手机放远处

---

## 📝 注意事项

1. **内存存储**: 当前活跃挑战使用内存存储，重启后丢失，生产环境建议使用 Redis
2. **并发处理**: 同一闹钟同时只能有一个活跃挑战
3. **时区处理**: 超时时间使用 UTC 时间戳
4. **输入验证**: 答案需要去除首尾空格

---

*最后更新：2026-03-20*  
*版本：1.0.0*
