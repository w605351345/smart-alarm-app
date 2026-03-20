// pages/challenge/challenge.js - 解密挑战页面逻辑

const app = getApp();
const api = require('../../utils/api.js');

Page({
  data: {
    alarmId: null,
    enabled: false,
    difficulty: 'EASY',
    challengeType: 'MATH',
    difficulties: [],
    challengeTypes: []
  },

  onLoad(options) {
    this.setData({
      alarmId: options.alarmId ? parseInt(options.alarmId) : null
    });
    this.loadData();
  },

  // 加载数据
  async loadData() {
    try {
      // 加载难度和类型配置
      const [difficultiesRes, typesRes] = await Promise.all([
        api.get('/api/v1/challenges/difficulties'),
        api.get('/api/v1/challenges/types')
      ]);

      this.setData({
        difficulties: difficultiesRes.data || [],
        challengeTypes: typesRes.data || []
      });

      // 加载当前闹钟的挑战配置
      if (this.data.alarmId) {
        const configRes = await api.get(`/api/v1/challenges/alarms/${this.data.alarmId}`);
        const config = configRes.data;
        
        if (config) {
          this.setData({
            enabled: config.enabled || false,
            difficulty: config.difficulty || 'EASY',
            challengeType: config.challengeType || 'MATH'
          });
        }
      }
    } catch (error) {
      console.error('加载数据失败', error);
      wx.showToast({
        title: '加载失败',
        icon: 'none'
      });
    }
  },

  // 选择难度
  selectDifficulty(e) {
    const value = e.currentTarget.dataset.value;
    this.setData({ difficulty: value });
  },

  // 选择题目类型
  selectType(e) {
    const value = e.currentTarget.dataset.value;
    this.setData({ challengeType: value });
  },

  // 切换启用状态
  toggleEnabled(e) {
    this.setData({
      enabled: e.detail.value
    });
  },

  // 保存设置
  async saveChallenge() {
    if (!this.data.alarmId) {
      wx.showToast({
        title: '闹钟 ID 缺失',
        icon: 'none'
      });
      return;
    }

    try {
      await api.post(`/api/v1/challenges/alarms/${this.data.alarmId}`, {
        enabled: this.data.enabled,
        difficulty: this.data.difficulty,
        challengeType: this.data.challengeType
      });

      wx.showToast({
        title: '保存成功',
        icon: 'success'
      });

      // 延迟返回
      setTimeout(() => {
        wx.navigateBack();
      }, 1500);
    } catch (error) {
      console.error('保存失败', error);
      wx.showToast({
        title: error.message || '保存失败',
        icon: 'none'
      });
    }
  }
});
