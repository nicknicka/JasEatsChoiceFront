<template>
  <view class="index-container">
    <view class="search-bar">
      <input type="text" placeholder="搜索菜品、商家或食谱..." class="search-input" />
      <view class="search-icon">🔍</view>
    </view>

    <view class="banner">
      <swiper autoplay interval="3000" circular indicator-dots indicator-color="rgba(255,255,255,0.5)" indicator-active-color="#fff">
        <swiper-item>
          <view class="banner-item">
            <text class="banner-text">今日推荐</text>
          </view>
        </swiper-item>
        <swiper-item>
          <view class="banner-item">
            <text class="banner-text">美食特惠</text>
          </view>
        </swiper-item>
        <swiper-item>
          <view class="banner-item">
            <text class="banner-text">新品上市</text>
          </view>
        </swiper-item>
      </swiper>
    </view>

    <view class="recommendation-section">
      <view class="section-title">个性化推荐</view>
      <view class="recommend-grid">
        <view class="recommend-item" v-for="item in recommendations" :key="item.id">
          <view class="food-image"></view>
          <view class="food-info">
            <view class="food-name">{{ item.name }}</view>
            <view class="food-price">¥{{ item.price.toFixed(2) }}</view>
          </view>
        </view>
      </view>
    </view>

    <view class="quick-menu">
      <view class="menu-item" v-for="menu in quickMenus" :key="menu.id">
        <view class="menu-icon">{{ menu.icon }}</view>
        <view class="menu-text">{{ menu.text }}</view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const recommendations = ref([
  { id: 1, name: '宫保鸡丁', price: 28.00 },
  { id: 2, name: '鱼香肉丝', price: 26.00 },
  { id: 3, name: '回锅肉', price: 32.00 },
  { id: 4, name: '麻婆豆腐', price: 18.00 }
])

const quickMenus = ref([
  { id: 1, icon: '🍳', text: '今日食谱' },
  { id: 2, icon: '📊', text: '卡路里' },
  { id: 3, icon: '📝', text: '我的食谱' },
  { id: 4, icon: '💬', text: 'AI助手' }
])
</script>

<style scoped>
.index-container {
  background-color: #f5f5f5;
  min-height: 100vh;
}

.search-bar {
  display: flex;
  align-items: center;
  background-color: #fff;
  padding: 10px 15px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  position: relative;
}

.search-input {
  flex: 1;
  height: 40px;
  padding: 0 15px;
  border: 1px solid #eee;
  border-radius: 20px;
  font-size: 14px;
}

.search-icon {
  position: absolute;
  right: 30px;
  font-size: 18px;
  color: #999;
}

.banner {
  height: 200px;
  margin: 15px 0;
}

.banner-item {
  height: 100%;
  background-color: #FF6B35;
  display: flex;
  justify-content: center;
  align-items: center;
}

.banner-text {
  color: #fff;
  font-size: 24px;
  font-weight: bold;
}

.recommendation-section {
  background-color: #fff;
  padding: 15px;
  margin-bottom: 15px;
}

.section-title {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 15px;
  color: #333;
}

.recommend-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
}

.recommend-item {
  background-color: #fafafa;
  border-radius: 8px;
  padding: 10px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.food-image {
  height: 120px;
  background-color: #e9ecef;
  border-radius: 6px;
  margin-bottom: 10px;
}

.food-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.food-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.food-price {
  font-size: 14px;
  font-weight: bold;
  color: #FF6B35;
}

.quick-menu {
  background-color: #fff;
  padding: 15px;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.menu-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.menu-icon {
  font-size: 28px;
}

.menu-text {
  font-size: 12px;
  color: #666;
}
</style>