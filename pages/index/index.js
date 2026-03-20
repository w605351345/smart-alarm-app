// pages/index/index.js
const app = getApp();
const api = require('../../utils/api').api;

Page({
  data: {
    alarms: [],
    loading: false,
    currentDate: '',
    weather: null,
    pullRefreshing: false
  },

  onLoad() {
    this.formatCurrentDate();
  },

  onShow() {
    if (app.globalData.isLoggedIn) {
      this.loadAlarms();
      this.loadWeather();
    }
  },

  onPullDownRefresh() {
    this.setData({ pullRefreshing: true });
    Promise.all([
      this.loadAlarms(),
      this.loadWeather()
    ]).finally(() => {
      this.setData({ pullRefreshing: false });
      wx.stopPullDownRefresh();
    });
  },

  // 格式化当前日期
  formatCurrentDate() {
    const now = new Date();
    const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
    const month = now.getMonth() + 1;
    const day = now.getDate();
    const weekDay = weekDays[now.getDay()];
    
    this.setData({
      currentDate: `${month}月${day}日 ${weekDay}`
    });
  },

  // 加载闹钟列表
  async loadAlarms() {
    this.setData({ loading: true });
    
    try {
      const res = await api.getAlarms();
      this.setData({ 
        alarms: res.data || [],
        loading: false 
      });
    } catch (error) {
      console.error('加载闹钟失败:', error);
      this.setData({ loading: false });
    }
  },

  // 加载天气
  async loadWeather() {
    try {
      const res = await api.getWeather();
      this.setData({ weather: res.data });
    } catch (error) {
      console.error('加载天气失败:', error);
    }
  },

  // 切换闹钟开关
  async toggleAlarm(e) {
    const alarmId = e.currentTarget.dataset.id;
    const enabled = e.detail.value;
    const alarm = this.data.alarms.find(a => a.id === alarmId);
    
    try {
      await api.toggleAlarm(alarmId, enabled);
      
      // 更新本地数据
      const alarms = this.data.alarms.map(a => 
        a.id === alarmId ? { ...a, enabled } : a
      );
      this.setData({ alarms });
      
      wx.showToast({
        title: enabled ? '已开启' : '已关闭',
        icon: 'success',
        duration: 1500
      });
      
      // 如果开启闹钟，请求订阅消息权限
      if (enabled) {
        this.requestSubscribeMessage();
      }
    } catch (error) {
      console.error('切换闹钟失败:', error);
      
      // 恢复状态
      const alarms = this.data.alarms.map(a => 
        a.id === alarmId ? { ...a, enabled: !enabled } : a
      );
      this.setData({ alarms });
      
      wx.showToast({
        title: '操作失败',
        icon: 'none'
      });
    }
  },

  // 请求订阅消息权限
  async requestSubscribeMessage() {
    try {
      const res = await api.requestSubscribe([
        'YOUR_ALARM_TEMPLATE_ID' // 替换为实际的模板 ID
      ]);
      
      if (res['YOUR_ALARM_TEMPLATE_ID'] === 'accept') {
        console.log('用户同意订阅消息');
      }
    } catch (error) {
      console.log('用户拒绝订阅消息');
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
      confirmColor: '#4A90D9',
      success: async (res) => {
        if (res.confirm) {
          try {
            await api.deleteAlarm(alarmId);
            
            wx.showToast({
              title: '删除成功',
              icon: 'success'
            });
            
            // 重新加载列表
            this.loadAlarms();
          } catch (error) {
            console.error('删除闹钟失败:', error);
            wx.showToast({
              title: '删除失败',
              icon: 'none'
            });
          }
        }
      }
    });
  },

  // 闹钟卡片点击
  onAlarmCardTap(e) {
    const alarmId = e.currentTarget.dataset.id;
    this.editAlarm({ currentTarget: { dataset: { id: alarmId } } });
  }
});
