// pages/challenge/answer.js - 答题页面逻辑

const api = require('../../utils/api.js');

Page({
  data: {
    challengeId: null,
    question: '',
    questionType: '',
    answer: '',
    remainingAttempts: 3,
    timeoutSeconds: 60,
    timeLeft: 60,
    timerInterval: null,
    showResult: false,
    resultIcon: '',
    resultTitle: '',
    resultMessage: '',
    resultBtnText: ''
  },

  onLoad(options) {
    this.setData({
      challengeId: options.challengeId
    });
    this.startChallenge();
  },

  onUnload() {
    // 清理定时器
    if (this.data.timerInterval) {
      clearInterval(this.data.timerInterval);
    }
  },

  // 开始挑战
  async startChallenge() {
    try {
      const res = await api.post('/api/v1/challenges/generate', {
        alarmId: this.data.challengeId
      });

      const challenge = res.data;
      this.setData({
        question: challenge.question,
        questionType: challenge.questionType,
        remainingAttempts: challenge.remainingAttempts,
        timeoutSeconds: challenge.timeoutSeconds,
        timeLeft: challenge.timeoutSeconds
      });

      // 启动倒计时
      this.startTimer();
    } catch (error) {
      console.error('开始挑战失败', error);
      wx.showToast({
        title: error.message || '加载失败',
        icon: 'none'
      });
    }
  },

  // 启动倒计时
  startTimer() {
    const timer = setInterval(() => {
      const timeLeft = this.data.timeLeft - 1;
      
      if (timeLeft <= 0) {
        clearInterval(timer);
        this.showResult(false, '⏰', '时间已到', '挑战失败，闹钟将继续响铃', '我知道了');
      } else {
        this.setData({ timeLeft });
      }
    }, 1000);

    this.setData({ timerInterval: timer });
  },

  // 输入答案
  onAnswerInput(e) {
    this.setData({
      answer: e.detail.value
    });
  },

  // 提交答案
  async submitAnswer() {
    const answer = this.data.answer.trim();
    
    if (!answer) {
      wx.showToast({
        title: '请输入答案',
        icon: 'none'
      });
      return;
    }

    try {
      const res = await api.post('/api/v1/challenges/verify', {
        challengeId: this.data.challengeId,
        answer: answer
      });

      const result = res.data;

      if (result.correct) {
        // 停止定时器
        if (this.data.timerInterval) {
          clearInterval(this.data.timerInterval);
        }
        
        // 显示成功结果
        this.showResult(true, '✅', '回答正确！', result.message, '关闭闹钟', true);
      } else {
        // 更新剩余次数
        this.setData({
          remainingAttempts: result.remainingAttempts,
          answer: ''
        });

        if (result.remainingAttempts <= 0) {
          // 停止定时器
          if (this.data.timerInterval) {
            clearInterval(this.data.timerInterval);
          }
          
          // 显示失败结果
          this.showResult(false, '❌', '挑战失败', result.message, '我知道了');
        } else {
          wx.showToast({
            title: result.message,
            icon: 'none'
          });
        }
      }
    } catch (error) {
      console.error('验证答案失败', error);
      wx.showToast({
        title: error.message || '验证失败',
        icon: 'none'
      });
    }
  },

  // 显示结果
  showResult(success, icon, title, message, btnText, dismissed = false) {
    this.setData({
      showResult: true,
      resultIcon: icon,
      resultTitle: title,
      resultMessage: message,
      resultBtnText: btnText,
      challengeDismissed: dismissed
    });
  },

  // 关闭结果弹窗
  closeResult() {
    if (this.data.challengeDismissed) {
      // 挑战成功，关闭闹钟并返回
      wx.navigateBack({
        delta: 2 // 返回到闹钟列表
      });
    } else {
      // 挑战失败，继续响铃
      wx.navigateBack();
    }
  }
});
