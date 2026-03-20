// app.js
App({
  onLaunch() {
    // 初始化登录状态
    this.checkLogin();
    
    // 获取系统信息
    const systemInfo = wx.getSystemInfoSync();
    this.globalData.systemInfo = systemInfo;
    
    // 检查更新
    this.checkUpdate();
  },

  // 检查登录状态
  checkLogin() {
    const token = wx.getStorageSync('token');
    if (token) {
      this.globalData.isLoggedIn = true;
      this.globalData.token = token;
    } else {
      this.globalData.isLoggedIn = false;
    }
  },

  // 微信登录
  async login() {
    try {
      const { code } = await wx.login();
      
      // 获取用户信息
      const userInfo = await this.getUserProfile();
      
      // 调用后端登录
      const res = await wx.request({
        url: `${this.globalData.apiBase}/api/v1/auth/wechat/login`,
        method: 'POST',
        data: {
          code,
          userInfo
        }
      });
      
      if (res.data.code === 200) {
        wx.setStorageSync('token', res.data.data.token);
        wx.setStorageSync('userInfo', res.data.data.user);
        this.globalData.isLoggedIn = true;
        this.globalData.token = res.data.data.token;
        this.globalData.userInfo = res.data.data.user;
        
        return res.data.data;
      } else {
        throw new Error(res.data.message || '登录失败');
      }
    } catch (error) {
      console.error('登录失败:', error);
      wx.showToast({
        title: '登录失败',
        icon: 'none'
      });
      throw error;
    }
  },

  // 获取用户信息
  getUserProfile() {
    return new Promise((resolve, reject) => {
      wx.getUserProfile({
        desc: '用于完善用户资料',
        success: (res) => {
          resolve(res.userInfo);
        },
        fail: (reject)
      });
    });
  },

  // 退出登录
  logout() {
    wx.removeStorageSync('token');
    wx.removeStorageSync('userInfo');
    this.globalData.isLoggedIn = false;
    this.globalData.token = null;
    this.globalData.userInfo = null;
  },

  // 检查小程序更新
  checkUpdate() {
    if (wx.canIUse('getUpdateManager')) {
      const updateManager = wx.getUpdateManager();
      
      updateManager.onCheckForUpdate((res) => {
        if (res.hasUpdate) {
          updateManager.onUpdateReady(() => {
            wx.showModal({
              title: '更新提示',
              content: '新版本已经准备好，是否重启应用？',
              success: (res) => {
                if (res.confirm) {
                  updateManager.applyUpdate();
                }
              }
            });
          });
        }
      });
    }
  },

  globalData: {
    apiBase: 'https://api.yourdomain.com',
    isLoggedIn: false,
    token: null,
    userInfo: null,
    systemInfo: null
  }
});
