// utils/api.js - API 请求封装

const app = getApp();

/**
 * 请求封装
 */
export function request(options) {
  return new Promise((resolve, reject) => {
    const token = wx.getStorageSync('token');
    
    wx.request({
      url: options.url.startsWith('http') ? options.url : `${app.globalData.apiBase}${options.url}`,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : '',
        ...options.header
      },
      timeout: options.timeout || 30000,
      success: (res) => {
        if (res.statusCode === 200) {
          resolve(res.data);
        } else if (res.statusCode === 401) {
          // Token 过期，重新登录
          wx.removeStorageSync('token');
          app.globalData.isLoggedIn = false;
          
          wx.showModal({
            title: '登录过期',
            content: '请重新登录',
            showCancel: false,
            success: () => {
              wx.reLaunch({
                url: '/pages/profile/profile'
              });
            }
          });
          
          reject(new Error('登录过期'));
        } else {
          wx.showToast({
            title: res.data.message || '请求失败',
            icon: 'none'
          });
          reject(new Error(res.data.message || '请求失败'));
        }
      },
      fail: (error) => {
        console.error('请求失败:', error);
        wx.showToast({
          title: '网络错误',
          icon: 'none'
        });
        reject(error);
      }
    });
  });
}

/**
 * GET 请求
 */
export function get(url, data, options = {}) {
  return request({
    url,
    method: 'GET',
    data,
    ...options
  });
}

/**
 * POST 请求
 */
export function post(url, data, options = {}) {
  return request({
    url,
    method: 'POST',
    data,
    ...options
  });
}

/**
 * PUT 请求
 */
export function put(url, data, options = {}) {
  return request({
    url,
    method: 'PUT',
    data,
    ...options
  });
}

/**
 * DELETE 请求
 */
export function del(url, data, options = {}) {
  return request({
    url,
    method: 'DELETE',
    data,
    ...options
  });
}

// API 接口定义
export const api = {
  // 认证
  login: (data) => post('/api/v1/auth/wechat/login', data),
  
  // 闹钟
  getAlarms: () => get('/api/v1/alarms'),
  getAlarm: (id) => get(`/api/v1/alarms/${id}`),
  createAlarm: (data) => post('/api/v1/alarms', data),
  updateAlarm: (id, data) => put(`/api/v1/alarms/${id}`, data),
  deleteAlarm: (id) => del(`/api/v1/alarms/${id}`),
  toggleAlarm: (id, enabled) => put(`/api/v1/alarms/${id}/toggle`, { enabled }),
  
  // 统计
  getStats: (period = 'week') => get(`/api/v1/stats?period=${period}`),
  
  // 用户
  getUserInfo: () => get('/api/v1/user/profile'),
  updateUserInfo: (data) => put('/api/v1/user/profile', data),
  
  // 天气
  getWeather: () => get('/api/v1/weather'),
  
  // 铃声
  getRingtones: () => get('/api/v1/ringtones'),
  
  // 订阅消息
  requestSubscribe: (templateIds) => {
    return new Promise((resolve, reject) => {
      wx.requestSubscribeMessage({
        tmplIds: templateIds,
        success: resolve,
        fail: reject
      });
    });
  }
};

export default api;
