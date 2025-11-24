<template>
  <view class="settings-container">
    <view class="user-profile">
      <view class="avatar">👤</view>
      <view class="user-info">
        <view class="user-name">佳食宜选用户</view>
        <view class="user-id">ID: 123456</view>
      </view>
      <button class="edit-btn">编辑资料</button>
    </view>

    <view class="settings-list">
      <view class="setting-section">
        <view class="setting-item" @click="navigateTo('user-center')">
          <view class="item-icon">👤</view>
          <view class="item-text">用户中心</view>
          <view class="item-arrow">→</view>
        </view>

        <view class="setting-item" @click="navigateTo('orders')">
          <view class="item-icon">📝</view>
          <view class="item-text">查看所有订单</view>
          <view class="item-arrow">→</view>
        </view>

        <view class="setting-item" @click="navigateTo('address')">
          <view class="item-icon">📍</view>
          <view class="item-text">管理地址</view>
          <view class="item-arrow">→</view>
        </view>
      </view>

      <view class="setting-section">
        <view class="setting-item" @click="navigateTo('calorie')">
          <view class="item-icon">📊</view>
          <view class="item-text">卡路里统计</view>
          <view class="item-arrow">→</view>
        </view>

        <view class="setting-item" @click="navigateTo('message')">
          <view class="item-icon">💬</view>
          <view class="item-text">消息中心</view>
          <view class="item-arrow">→</view>
        </view>

        <view class="setting-item" @click="navigateTo('service')">
          <view class="item-icon">📞</view>
          <view class="item-text">联系客服</view>
          <view class="item-arrow">→</view>
        </view>
      </view>

      <view class="setting-section">
        <view class="setting-item" @click="toggleNotification">
          <view class="item-icon">🔔</view>
          <view class="item-text">消息通知</view>
          <switch v-model="notificationEnabled" color="#FF6B35" />
        </view>

        <view class="setting-item" @click="toggleDarkMode">
          <view class="item-icon">🌙</view>
          <view class="item-text">深色模式</view>
          <switch v-model="darkModeEnabled" color="#FF6B35" />
        </view>
      </view>
    </view>

    <button class="logout-btn" type="warn" @click="logout">退出登录</button>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const notificationEnabled = ref(true)
const darkModeEnabled = ref(false)

const navigateTo = (page) => {
  const pageMap = {
    'user-center': '/pages/user-center/index',
    'orders': '/pages/orders/index',
    'address': '/pages/address/index',
    'calorie': '/pages/calorie/index',
    'message': '/pages/message/index',
    'service': '/pages/service/index'
  }

  const path = pageMap[page]
  if (path) {
    uni.navigateTo({
      url: path
    })
  } else {
    uni.showToast({
      title: '页面开发中...',
      icon: 'none'
    })
  }
}

const toggleNotification = () => {
  uni.showToast({
    title: notificationEnabled.value ? '通知已开启' : '通知已关闭',
    icon: 'none'
  })
}

const toggleDarkMode = () => {
  uni.showToast({
    title: darkModeEnabled.value ? '深色模式已开启' : '深色模式已关闭',
    icon: 'none'
  })
}

const logout = () => {
  uni.showModal({
    title: '退出登录',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        uni.showToast({
          title: '已退出登录',
          icon: 'success'
        })
      }
    }
  })
}
</script>

<style scoped>
.settings-container {
  background-color: #f5f5f5;
  min-height: 100vh;
  padding: 15px;
}

.user-profile {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  display: flex;
  align-items: center;
  gap: 15px;
}

.avatar {
  font-size: 60px;
}

.user-info {
  flex: 1;
}

.user-name {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}

.user-id {
  font-size: 14px;
  color: #999;
}

.edit-btn {
  padding: 8px 16px;
  border: 1px solid #FF6B35;
  border-radius: 20px;
  background-color: #fff;
  color: #FF6B35;
  font-size: 14px;
  cursor: pointer;
}

.settings-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.setting-section {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.setting-item {
  display: flex;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #eee;
  cursor: pointer;
}

.setting-item:last-child {
  border-bottom: none;
}

.item-icon {
  font-size: 20px;
  margin-right: 15px;
  width: 24px;
}

.item-text {
  flex: 1;
  font-size: 15px;
  color: #333;
}

.item-arrow {
  font-size: 16px;
  color: #ccc;
}

.logout-btn {
  width: 100%;
  margin-top: 40px;
  padding: 15px;
  background-color: #fff;
  border: 1px solid #FF6B35;
  border-radius: 8px;
  color: #FF6B35;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
}
</style>