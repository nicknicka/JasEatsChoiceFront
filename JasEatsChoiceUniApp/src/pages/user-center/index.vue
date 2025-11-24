<template>
  <view class="user-center-container">
    <view class="user-info-card">
      <view class="avatar">👤</view>
      <view class="user-info">
        <view class="user-name">佳食宜选用户</view>
        <view class="user-level">普通会员</view>
      </view>
      <view class="membership-btn" @click="goToMembership">
        <text>会员中心</text>
        <text class="arrow">→</text>
      </view>
    </view>

    <view class="info-section">
      <view class="section-title">个人信息</view>
      <view class="info-item" @click="goToProfile">
        <view class="item-label">基本信息</view>
        <view class="item-value">完善个人资料</view>
        <view class="item-arrow">→</view>
      </view>
      <view class="info-item" @click="goToSafety">
        <view class="item-label">账号安全</view>
        <view class="item-value">修改密码</view>
        <view class="item-arrow">→</view>
      </view>
    </view>

    <view class="order-section">
      <view class="section-title">我的订单</view>
      <view class="order-status">
        <view class="status-item" @click="goToOrders('all')">
          <view class="status-icon">📝</view>
          <view class="status-text">全部</view>
        </view>
        <view class="status-item" @click="goToOrders('pending')">
          <view class="status-icon">⏳</view>
          <view class="status-text">待支付</view>
        </view>
        <view class="status-item" @click="goToOrders('processing')">
          <view class="status-icon">👨🍳</view>
          <view class="status-text">处理中</view>
        </view>
        <view class="status-item" @click="goToOrders('completed')">
          <view class="status-icon">✅</view>
          <view class="status-text">已完成</view>
        </view>
      </view>
    </view>

    <view class="service-section">
      <view class="section-title">服务与帮助</view>
      <view class="info-item" @click="goToFeedback">
        <view class="item-label">意见反馈</view>
        <view class="item-arrow">→</view>
      </view>
      <view class="info-item" @click="goToAbout">
        <view class="item-label">关于我们</view>
        <view class="item-arrow">→</view>
      </view>
    </view>
  </view>
</template>

<script setup>
const goToMembership = () => {
  uni.showToast({ title: '会员中心开发中', icon: 'none' })
}

const goToProfile = () => {
  uni.showToast({ title: '基本信息开发中', icon: 'none' })
}

const goToSafety = () => {
  uni.showToast({ title: '账号安全开发中', icon: 'none' })
}

const goToOrders = (status) => {
  uni.navigateTo({
    url: `/pages/orders/index?status=${status}`
  })
}

const goToFeedback = () => {
  uni.showToast({ title: '意见反馈开发中', icon: 'none' })
}

const goToAbout = () => {
  uni.showToast({ title: '关于我们开发中', icon: 'none' })
}
</script>

<style scoped>
.user-center-container {
  background-color: #f5f5f5;
  min-height: 100vh;
  padding: 15px;
}

.user-info-card {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  display: flex;
  align-items: center;
  gap: 20px;
}

.avatar {
  font-size: 60px;
}

.user-info {
  flex: 1;
}

.user-name {
  font-size: 20px;
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}

.user-level {
  font-size: 14px;
  color: #999;
}

.membership-btn {
  color: #FF6B35;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 5px;
}

.arrow {
  font-size: 12px;
}

.info-section, .order-section, .service-section {
  background-color: #fff;
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 15px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 15px;
  padding-left: 5px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid #eee;
}

.info-item:last-child {
  border-bottom: none;
}

.item-label {
  font-size: 15px;
  color: #333;
}

.item-value {
  font-size: 14px;
  color: #999;
}

.item-arrow {
  font-size: 16px;
  color: #ccc;
}

.order-status {
  display: flex;
  justify-content: space-around;
}

.status-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 15px;
  cursor: pointer;
}

.status-icon {
  font-size: 28px;
}

.status-text {
  font-size: 12px;
  color: #666;
}
</style>