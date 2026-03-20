# 微信小程序部署发布指南

## 📱 项目概述

本指南将帮助你将 SmartAlarm 智能闹钟项目发布到微信小程序平台。

### 项目结构

```
smart-alarm-wechat-miniprogram/
├── app.js                 # 小程序入口文件
├── app.json               # 小程序配置
├── app.wxss               # 全局样式
├── sitemap.json           # 索引配置
│
├── pages/
│   ├── index/             # 首页（闹钟列表）
│   │   ├── index.js
│   │   ├── index.json
│   │   ├── index.wxml
│   │   └── index.wxss
│   ├── add-alarm/         # 添加闹钟
│   │   └── ...
│   ├── alarm-detail/      # 闹钟详情
│   │   └── ...
│   ├── stats/             # 统计页面
│   │   └── ...
│   └── profile/           # 个人中心
│       └── ...
│
├── components/            # 自定义组件
│   ├── alarm-card/        # 闹钟卡片组件
│   ├── time-picker/       # 时间选择器
│   └── weather-widget/    # 天气小组件
│
├── utils/                 # 工具函数
│   ├── api.js             # API 请求封装
│   ├── auth.js            # 微信登录
│   └── util.js            # 通用工具
│
├── images/                # 图片资源
├── sounds/                # 铃声资源
│
└── project.config.json    # 项目配置
```

---

## 🚀 部署前准备

### 1. 注册微信小程序账号

#### 1.1 注册流程
1. 访问 [微信公众平台](https://mp.weixin.qq.com/)
2. 点击"立即注册"
3. 选择"小程序"类型
4. 填写账号信息（邮箱、密码）
5. 邮箱激活
6. 信息登记（主体类型选择）

#### 1.2 主体类型选择
| 主体类型 | 适用场景 | 费用 | 所需材料 |
|----------|----------|------|----------|
| 个人 | 个人开发者 | 免费 | 身份证 |
| 企业 | 公司运营 | 免费 | 营业执照 |
| 政府 | 政府部门 | 免费 | 组织机构代码 |
| 其他组织 | 非营利组织 | 免费 | 组织机构代码 |

**建议**: 如果是商业用途，选择"企业"主体，支持更多功能（如支付）。

#### 1.3 小程序命名
- 名称：SmartAlarm 智能闹钟
- 建议准备 2-3 个备选名称（可能已被注册）
- 名称规则：4-30 个字符，不能包含品牌词

---

### 2. 获取 AppID 和 AppSecret

#### 2.1 获取步骤
1. 登录 [微信公众平台](https://mp.weixin.qq.com/)
2. 进入"开发" > "开发管理"
3. 点击"开发设置"
4. 复制 **AppID(小程序 ID)**
5. 生成并复制 **AppSecret**

#### 2.2 安全提示
⚠️ **重要**: 
- AppSecret 是敏感信息，不要提交到代码仓库
- 使用环境变量或配置文件管理
- 定期更换 AppSecret

---

### 3. 配置服务器域名

#### 3.1 配置步骤
1. 登录微信公众平台
2. 进入"开发" > "开发管理" > "开发设置"
3. 找到"服务器域名"
4. 配置以下域名：

```
request 合法域名：
  https://api.yourdomain.com

socket 合法域名：
  wss://api.yourdomain.com

uploadFile 合法域名：
  https://api.yourdomain.com

downloadFile 合法域名：
  https://api.yourdomain.com
```

#### 3.2 注意事项
- 域名必须使用 HTTPS
- 域名必须备案（中国大陆服务器）
- 每月可修改 5 次

---

### 4. 下载和安装开发工具

#### 4.1 微信开发者工具
1. 访问 [微信开发者工具官网](https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html)
2. 下载对应系统版本（macOS/Windows/Linux）
3. 安装并登录

#### 4.2 项目导入
1. 打开微信开发者工具
2. 点击"+"导入项目
3. 选择项目目录
4. 填写 AppID
5. 点击"导入"

---

## 📝 后端 API 适配

### 1. 微信小程序登录接口

#### 1.1 后端新增接口

```java
// WechatAuthController.java
@RestController
@RequestMapping("/api/v1/auth/wechat")
public class WechatAuthController {
    
    @Autowired
    private WechatAuthService wechatAuthService;
    
    /**
     * 微信小程序登录
     * POST /api/v1/auth/wechat/login
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> wechatLogin(
            @RequestBody WechatLoginRequest request) {
        
        // 1. 验证微信 code
        String openId = wechatAuthService.getOpenId(request.getCode());
        
        // 2. 创建或更新用户
        User user = wechatAuthService.createOrUpdateUser(openId, request.getUserInfo());
        
        // 3. 生成 JWT token
        String token = jwtTokenProvider.generateToken(user);
        
        return ResponseEntity.ok(new LoginResponse(token, user));
    }
    
    /**
     * 获取微信用户信息
     * GET /api/v1/auth/wechat/user-info
     */
    @GetMapping("/user-info")
    public ResponseEntity<UserInfo> getUserInfo(
            @RequestHeader("Authorization") String token) {
        // 返回用户信息
    }
}
```

#### 1.2 微信登录请求类

```java
// WechatLoginRequest.java
@Data
public class WechatLoginRequest {
    private String code;          // 微信登录 code
    private String encryptedData; // 加密的用户数据
    private String iv;            // 加密向量
    private UserInfo userInfo;    // 用户信息
    
    @Data
    public static class UserInfo {
        private String nickName;
        private String avatarUrl;
        private Integer gender;
    }
}
```

#### 1.3 微信服务实现

```java
// WechatAuthService.java
@Service
public class WechatAuthService {
    
    @Value("${wechat.appid}")
    private String appid;
    
    @Value("${wechat.secret}")
    private String secret;
    
    private static final String JS_CODE2SESSION_URL = 
        "https://api.weixin.qq.com/sns/jscode2session";
    
    /**
     * 获取微信 OpenID
     */
    public String getOpenId(String code) {
        String url = String.format("%s?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                JS_CODE2SESSION_URL, appid, secret, code);
        
        String response = restTemplate.getForObject(url, String.class);
        JSONObject json = JSON.parseObject(response);
        
        return json.getString("openid");
    }
    
    /**
     * 创建或更新用户
     */
    public User createOrUpdateUser(String openId, WechatLoginRequest.UserInfo userInfo) {
        User user = userRepository.findByOpenId(openId);
        
        if (user == null) {
            user = new User();
            user.setOpenId(openId);
            user.setNickName(userInfo.getNickName());
            user.setAvatarUrl(userInfo.getAvatarUrl());
            user.setGender(userInfo.getGender());
            userRepository.save(user);
        } else {
            // 更新用户信息
            user.setNickName(userInfo.getNickName());
            user.setAvatarUrl(userInfo.getAvatarUrl());
            userRepository.save(user);
        }
        
        return user;
    }
}
```

---

### 2. 订阅消息推送

#### 2.1 后端接口

```java
// WechatMessageController.java
@RestController
@RequestMapping("/api/v1/wechat/message")
public class WechatMessageController {
    
    @Autowired
    private WechatMessageService messageService;
    
    /**
     * 发送闹钟提醒
     * POST /api/v1/wechat/message/alarm-reminder
     */
    @PostMapping("/alarm-reminder")
    public ResponseEntity<Void> sendAlarmReminder(
            @RequestBody AlarmReminderRequest request) {
        
        messageService.sendAlarmReminder(
            request.getOpenId(),
            request.getAlarmTime(),
            request.getAlarmName()
        );
        
        return ResponseEntity.ok().build();
    }
}
```

#### 2.2 消息服务实现

```java
// WechatMessageService.java
@Service
public class WechatMessageService {
    
    @Value("${wechat.appid}")
    private String appid;
    
    @Value("${wechat.secret}")
    private String secret;
    
    private static final String ACCESS_TOKEN_URL = 
        "https://api.weixin.qq.com/cgi-bin/token";
    
    private static final String MESSAGE_SEND_URL = 
        "https://api.weixin.qq.com/cgi-bin/message/subscribe/send";
    
    /**
     * 获取访问令牌
     */
    private String getAccessToken() {
        String url = String.format("%s?grant_type=client_credential&appid=%s&secret=%s",
                ACCESS_TOKEN_URL, appid, secret);
        
        String response = restTemplate.getForObject(url, String.class);
        JSONObject json = JSON.parseObject(response);
        
        return json.getString("access_token");
    }
    
    /**
     * 发送订阅消息
     */
    public void sendSubscribeMessage(String openId, String templateId, 
                                     Map<String, String> data) {
        String accessToken = getAccessToken();
        
        JSONObject message = new JSONObject();
        message.put("touser", openId);
        message.put("template_id", templateId);
        
        JSONObject page = new JSONObject();
        page.put("page", "pages/index/index");
        message.put("page", page);
        
        JSONObject messageData = new JSONObject();
        data.forEach((key, value) -> {
            JSONObject item = new JSONObject();
            item.put("value", value);
            messageData.put(key, item);
        });
        message.put("data", messageData);
        
        String url = MESSAGE_SEND_URL + "?access_token=" + accessToken;
        restTemplate.postForObject(url, message.toString(), String.class);
    }
    
    /**
     * 发送闹钟提醒
     */
    public void sendAlarmReminder(String openId, String alarmTime, String alarmName) {
        Map<String, String> data = new HashMap<>();
        data.put("thing1", new JSONObject().put("value", alarmName).toString());
        data.put("time2", new JSONObject().put("value", alarmTime).toString());
        data.put("thing3", new JSONObject().put("value", "记得起床哦").toString());
        
        sendSubscribeMessage(openId, "ALARM_REMINDER_TEMPLATE_ID", data);
    }
}
```

---

### 3. 后端配置更新

#### 3.1 application.properties

```properties
# 微信小程序配置
wechat.appid=your_wechat_appid
wechat.secret=your_wechat_secret
wechat.token=your_wechat_token
wechat.encodingAesKey=your_encoding_aes_key

# 订阅消息模板 ID
wechat.template.alarm-reminder=YOUR_TEMPLATE_ID
```

---

## 🔧 小程序前端开发

### 1. 项目配置文件

#### 1.1 app.json

```json
{
  "pages": [
    "pages/index/index",
    "pages/add-alarm/add-alarm",
    "pages/alarm-detail/alarm-detail",
    "pages/stats/stats",
    "pages/profile/profile"
  ],
  "window": {
    "backgroundTextStyle": "light",
    "navigationBarBackgroundColor": "#4A90D9",
    "navigationBarTitleText": "SmartAlarm 智能闹钟",
    "navigationBarTextStyle": "white"
  },
  "tabBar": {
    "color": "#999999",
    "selectedColor": "#4A90D9",
    "backgroundColor": "#ffffff",
    "list": [
      {
        "pagePath": "pages/index/index",
        "text": "闹钟",
        "iconPath": "images/tab-alarm.png",
        "selectedIconPath": "images/tab-alarm-active.png"
      },
      {
        "pagePath": "pages/stats/stats",
        "text": "统计",
        "iconPath": "images/tab-stats.png",
        "selectedIconPath": "images/tab-stats-active.png"
      },
      {
        "pagePath": "pages/profile/profile",
        "text": "我的",
        "iconPath": "images/tab-profile.png",
        "selectedIconPath": "images/tab-profile-active.png"
      }
    ]
  },
  "permission": {
    "scope.userLocation": {
      "desc": "获取您的位置用于天气感知闹钟"
    }
  },
  "requiredPrivateInfos": [
    "getLocation"
  ],
  "sitemapLocation": "sitemap.json"
}
```

#### 1.2 project.config.json

```json
{
  "description": "SmartAlarm 智能闹钟小程序",
  "packOptions": {
    "ignore": [],
    "include": []
  },
  "setting": {
    "bundle": false,
    "userConfirmedBundleSwitch": false,
    "urlCheck": true,
    "scopeDataCheck": false,
    "coverView": true,
    "es6": true,
    "postcss": true,
    "compileHotReLoad": false,
    "lazyloadPlaceholderEnable": false,
    "preloadBackgroundData": false,
    "minified": true,
    "autoAudits": false,
    "newFeature": false,
    "uglifyFileName": false,
    "uploadWithSourceMap": true,
    "useIsolateContext": true,
    "nodeModules": false,
    "enhance": true,
    "useMultiFrameRuntime": true,
    "useApiHook": true,
    "useApiHostProcess": true,
    "showShadowRootInWxmlPanel": true,
    "packNpmManually": false,
    "enableEngineNative": false,
    "packNpmRelationList": [],
    "minifyWXSS": true,
    "showES6CompileOption": false,
    "minifyWXML": true,
    "babelSetting": {
      "ignore": [],
      "disablePlugins": [],
      "outputPath": ""
    }
  },
  "compileType": "miniprogram",
  "libVersion": "2.19.4",
  "appid": "YOUR_APPID",
  "projectname": "smart-alarm-miniprogram",
  "condition": {},
  "editorSetting": {
    "tabIndent": "insertSpaces",
    "tabSize": 2
  }
}
```

---

### 2. 核心页面代码

#### 2.1 首页（闹钟列表）

```javascript
// pages/index/index.js
Page({
  data: {
    alarms: [],
    loading: false
  },

  onLoad() {
    this.loadAlarms();
  },

  onShow() {
    this.loadAlarms();
  },

  // 下拉刷新
  onPullDownRefresh() {
    this.loadAlarms().then(() => {
      wx.stopPullDownRefresh();
    });
  },

  // 加载闹钟列表
  async loadAlarms() {
    this.setData({ loading: true });
    
    try {
      const token = wx.getStorageSync('token');
      const res = await wx.request({
        url: 'https://api.yourdomain.com/api/v1/alarms',
        header: {
          'Authorization': `Bearer ${token}`
        }
      });
      
      this.setData({ 
        alarms: res.data.data,
        loading: false 
      });
    } catch (error) {
      wx.showToast({
        title: '加载失败',
        icon: 'none'
      });
      this.setData({ loading: false });
    }
  },

  // 切换闹钟开关
  async toggleAlarm(e) {
    const alarmId = e.currentTarget.dataset.id;
    const enabled = e.detail.value;
    
    try {
      const token = wx.getStorageSync('token');
      await wx.request({
        url: `https://api.yourdomain.com/api/v1/alarms/${alarmId}`,
        method: 'PUT',
        header: {
          'Authorization': `Bearer ${token}`
        },
        data: { enabled }
      });
      
      wx.showToast({
        title: enabled ? '已开启' : '已关闭',
        icon: 'success'
      });
    } catch (error) {
      wx.showToast({
        title: '操作失败',
        icon: 'none'
      });
    }
  },

  // 添加闹钟
  addAlarm() {
    wx.navigateTo({
      url: '/pages/add-alarm/add-alarm'
    });
  },

  // 编辑闹钟
  editAlarm(e) {
    const alarmId = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/alarm-detail/alarm-detail?id=${alarmId}`
    });
  },

  // 删除闹钟
  deleteAlarm(e) {
    const alarmId = e.currentTarget.dataset.id;
    
    wx.showModal({
      title: '确认删除',
      content: '确定要删除这个闹钟吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            const token = wx.getStorageSync('token');
            await wx.request({
              url: `https://api.yourdomain.com/api/v1/alarms/${alarmId}`,
              method: 'DELETE',
              header: {
                'Authorization': `Bearer ${token}`
              }
            });
            
            wx.showToast({
              title: '删除成功',
              icon: 'success'
            });
            
            this.loadAlarms();
          } catch (error) {
            wx.showToast({
              title: '删除失败',
              icon: 'none'
            });
          }
        }
      }
    });
  }
});
```

#### 2.2 首页 WXML

```xml
<!--pages/index/index.wxml-->
<view class="container">
  <!-- 头部 -->
  <view class="header">
    <view class="date">{{currentDate}}</view>
    <view class="weather" wx:if="{{weather}}">
      <text class="weather-icon">{{weather.icon}}</text>
      <text class="weather-temp">{{weather.temperature}}°C</text>
    </view>
  </view>

  <!-- 闹钟列表 -->
  <view class="alarm-list">
    <view class="alarm-item" wx:for="{{alarms}}" wx:key="id">
      <view class="alarm-time" bindtap="editAlarm" data-id="{{item.id}}">
        {{item.time}}
      </view>
      <view class="alarm-info">
        <text class="alarm-name">{{item.name}}</text>
        <text class="alarm-repeat">{{item.repeatText}}</text>
      </view>
      <switch 
        checked="{{item.enabled}}" 
        bindchange="toggleAlarm"
        data-id="{{item.id}}"
        color="#4A90D9"
      />
    </view>

    <!-- 空状态 -->
    <view class="empty-state" wx:if="{{alarms.length === 0}}">
      <image src="/images/empty-alarm.png" class="empty-image" />
      <text class="empty-text">暂无闹钟</text>
      <text class="empty-hint">点击底部按钮添加第一个闹钟</text>
    </view>
  </view>

  <!-- 添加按钮 -->
  <view class="add-button" bindtap="addAlarm">
    <text class="add-icon">+</text>
  </view>
</view>
```

---

### 3. 微信登录

```javascript
// utils/auth.js
const API_BASE = 'https://api.yourdomain.com';

/**
 * 微信登录
 */
export async function login() {
  try {
    // 1. 获取微信登录 code
    const { code } = await wx.login();
    
    // 2. 获取用户信息
    const userInfo = await getUserProfile();
    
    // 3. 调用后端登录接口
    const res = await wx.request({
      url: `${API_BASE}/api/v1/auth/wechat/login`,
      method: 'POST',
      data: {
        code,
        userInfo
      }
    });
    
    // 4. 保存 token
    wx.setStorageSync('token', res.data.data.token);
    wx.setStorageSync('userInfo', res.data.data.user);
    
    return res.data.data;
  } catch (error) {
    console.error('登录失败', error);
    throw error;
  }
}

/**
 * 获取用户信息
 */
export function getUserProfile() {
  return new Promise((resolve, reject) => {
    wx.getUserProfile({
      desc: '用于完善用户资料',
      success: (res) => {
        resolve(res.userInfo);
      },
      fail: (reject)
    });
  });
}

/**
 * 检查登录状态
 */
export function checkLogin() {
  const token = wx.getStorageSync('token');
  return !!token;
}

/**
 * 退出登录
 */
export function logout() {
  wx.removeStorageSync('token');
  wx.removeStorageSync('userInfo');
}
```

---

## 📤 提交审核

### 1. 代码上传

#### 1.1 使用开发者工具上传
1. 打开微信开发者工具
2. 点击右上角"上传"按钮
3. 填写版本号和项目备注
4. 点击"上传"

#### 1.2 使用命令行上传（CI/CD）

```bash
# 安装微信 CLI 工具
npm install -g miniprogram-ci

# 上传代码
npx miniprogram-ci \
  --project-path ./smart-alarm-wechat-miniprogram \
  --appid YOUR_APPID \
  --private-key YOUR_PRIVATE_KEY \
  --version 1.0.0 \
  --desc "初始版本" \
  --upload
```

---

### 2. 版本管理

#### 2.1 版本流程
1. **开发版本**: 上传后自动创建
2. **体验版本**: 添加体验成员测试
3. **审核版本**: 提交审核
4. **线上版本**: 审核通过后发布

#### 2.2 添加体验成员
1. 登录微信公众平台
2. 进入"管理" > "成员管理"
3. 添加体验成员（微信号）
4. 体验成员可在开发者工具中预览

---

### 3. 提交审核材料

#### 3.1 基本信息
- 服务类目：工具 > 效率
- 标签：闹钟、提醒、效率、时间管理
- 简介：智能闹钟，助你准时起床

#### 3.2 功能页面截图
准备以下页面截图（1920x1080）：
1. 首页（闹钟列表）
2. 添加闹钟页面
3. 闹钟详情页面
4. 统计页面
5. 个人中心页面

#### 3.3 隐私配置
1. 进入"设置" > "基本设置" > "隐私保护"
2. 填写用户隐私保护指引
3. 配置用户信息收集说明

#### 3.4 测试账号
如需要登录，提供测试账号：
- 账号：test@smartalarm.com
- 密码：Test123456

---

### 4. 审核注意事项

#### 4.1 常见拒绝原因
| 原因 | 解决方案 |
|------|----------|
| 功能不完整 | 确保所有按钮可点击，功能可用 |
| 闪退或 Bug | 充分测试，修复所有已知问题 |
| 隐私问题 | 完善隐私政策，明确数据用途 |
| 类目不符 | 选择正确的服务类目 |
| 诱导分享 | 不要强制用户分享 |

#### 4.2 审核时间
- 通常 1-3 个工作日
- 节假日可能延长
- 可在后台查看审核进度

---

## 🚀 发布上线

### 1. 发布流程

#### 1.1 手动发布
1. 审核通过后，登录微信公众平台
2. 进入"版本管理"
3. 点击"提交发布"
4. 确认发布

#### 1.2 定时发布
1. 进入"版本管理"
2. 点击"定时发布"
3. 选择发布时间
4. 确认设置

#### 1.3 分阶段发布
1. 进入"版本管理"
2. 点击"分阶段发布"
3. 设置灰度比例（如 10%）
4. 监控无问题后全量发布

---

### 2. 发布后监控

#### 2.1 数据分析
1. 登录微信公众平台
2. 进入"统计" > "访问分析"
3. 关注以下指标：
   - 新增用户
   - 活跃用户
   - 留存率
   - 使用时长

#### 2.2 错误监控
1. 进入"开发" > "开发管理" > "运维中心"
2. 查看错误日志
3. 及时修复线上问题

#### 2.3 用户反馈
1. 进入"管理" > "用户反馈"
2. 及时回复用户问题
3. 收集改进建议

---

## 🔄 版本更新

### 1. 更新流程

#### 1.1 代码更新
```bash
# 1. 修改代码
# 2. 本地测试
# 3. 上传新版本
npx miniprogram-ci \
  --project-path ./smart-alarm-wechat-miniprogram \
  --appid YOUR_APPID \
  --private-key YOUR_PRIVATE_KEY \
  --version 1.0.1 \
  --desc "修复闹钟重复提醒问题" \
  --upload
```

#### 1.2 提交审核
1. 登录微信公众平台
2. 进入"版本管理"
3. 选择开发版本
4. 提交审核

#### 1.3 发布更新
审核通过后发布（同首次发布流程）

---

### 2. 版本管理建议

#### 2.1 版本号规范
```
主版本号。次版本号。修订号
例如：1.0.0

- 主版本号：重大功能更新
- 次版本号：功能增加
- 修订号：Bug 修复
```

#### 2.2 更新日志
每次更新填写清晰的更新日志：
```
v1.0.1 (2026-03-20)
- 修复：闹钟重复提醒问题
- 优化：启动速度提升 30%
- 新增：支持农历日期显示
```

---

## 📊 运营推广

### 1. 小程序码

#### 1.1 生成小程序码
1. 登录微信公众平台
2. 进入"工具" > "小程序码"
3. 选择页面和样式
4. 下载小程序码

#### 1.2 应用场景
- 海报宣传
- 社交媒体分享
- 线下活动
- 产品包装

---

### 2. 社交分享

#### 2.1 分享配置
```javascript
// pages/index/index.js
Page({
  onShareAppMessage() {
    return {
      title: 'SmartAlarm 智能闹钟 - 助你准时起床',
      path: '/pages/index/index?from=share',
      imageUrl: '/images/share-card.png'
    };
  }
});
```

#### 2.2 分享场景
- 好友分享
- 微信群分享
- 朋友圈（需生成海报）

---

### 3. 搜索优化

#### 3.1 小程序名称
- 包含关键词（闹钟、提醒）
- 易于记忆
- 品牌识别度高

#### 3.2 关键词配置
1. 登录微信公众平台
2. 进入"推广" > "搜索优化"
3. 配置关键词：闹钟、起床、提醒、时间管理

---

## 🛠️ 常见问题

### 1. 登录问题

**问题**: 微信登录失败
**解决方案**:
- 检查 AppID 和 AppSecret 是否正确
- 确认服务器域名已配置
- 检查后端接口是否正常

### 2. 推送问题

**问题**: 订阅消息不推送
**解决方案**:
- 确认用户已订阅消息模板
- 检查模板 ID 是否正确
- 验证 access_token 是否有效

### 3. 审核问题

**问题**: 审核被拒绝
**解决方案**:
- 仔细阅读拒绝原因
- 按要求修改后重新提交
- 可在"站内信"查看详细信息

### 4. 性能问题

**问题**: 小程序加载慢
**解决方案**:
- 启用分包加载
- 压缩图片和资源
- 减少首屏数据量
- 使用 CDN 加速

---

## 📞 技术支持

### 官方文档
- [微信小程序官方文档](https://developers.weixin.qq.com/miniprogram/dev/framework/)
- [微信开放社区](https://developers.weixin.qq.com/community/)

### 开发工具
- [微信开发者工具](https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html)
- [小程序 CLI](https://developers.weixin.qq.com/miniprogram/dev/devtools/miniprogram-ci.html)

### 联系方式
- 技术支持邮箱：support@smartalarm.com
- GitHub Issues: https://github.com/w605351345/smart-alarm-app/issues

---

## ✅ 发布检查清单

### 开发阶段
- [ ] 代码完成并本地测试通过
- [ ] 所有功能可用
- [ ] 无控制台错误
- [ ] 适配不同屏幕尺寸
- [ ] 隐私配置完成

### 提审前
- [ ] 服务类目选择正确
- [ ] 隐私政策完善
- [ ] 截图准备完成
- [ ] 测试账号准备（如需要）
- [ ] 更新日志填写

### 发布后
- [ ] 监控错误日志
- [ ] 回复用户反馈
- [ ] 分析用户数据
- [ ] 规划下版本

---

*最后更新：2026-03-20*
*版本：1.0.0*
