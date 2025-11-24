<template>
  <view class="today-recipe-container">
    <view class="date-header">
      <text class="date">{{ todayDate }}</text>
      <text class="weekday">{{ todayWeekday }}</text>
    </view>

    <view class="recipe-section">
      <view class="meal-type">早餐</view>
      <view class="recipe-card">
        <view class="recipe-image breakfast"></view>
        <view class="recipe-content">
          <view class="recipe-title">营养早餐套餐</view>
          <view class="recipe-details">
            <view class="detail-item">
              <text class="detail-label">卡路里：</text>
              <text class="detail-value">450 kcal</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">食材：</text>
              <text class="detail-value">牛奶、鸡蛋、面包、水果</text>
            </view>
          </view>
          <button class="add-btn" type="primary">添加到我的食谱</button>
        </view>
      </view>
    </view>

    <view class="recipe-section">
      <view class="meal-type">午餐</view>
      <view class="recipe-card">
        <view class="recipe-image lunch"></view>
        <view class="recipe-content">
          <view class="recipe-title">健康午餐</view>
          <view class="recipe-details">
            <view class="detail-item">
              <text class="detail-label">卡路里：</text>
              <text class="detail-value">800 kcal</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">食材：</text>
              <text class="detail-value">米饭、蔬菜、瘦肉</text>
            </view>
          </view>
          <button class="add-btn" type="primary">添加到我的食谱</button>
        </view>
      </view>
    </view>

    <view class="recipe-section">
      <view class="meal-type">晚餐</view>
      <view class="recipe-card">
        <view class="recipe-image dinner"></view>
        <view class="recipe-content">
          <view class="recipe-title">轻食晚餐</view>
          <view class="recipe-details">
            <view class="detail-item">
              <text class="detail-label">卡路里：</text>
              <text class="detail-value">550 kcal</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">食材：</text>
              <text class="detail-value">蔬菜沙拉、鸡肉、杂粮</text>
            </view>
          </view>
          <button class="add-btn" type="primary">添加到我的食谱</button>
        </view>
      </view>
    </view>

    <view class="total-section">
      <text class="total-label">今日总卡路里：</text>
      <text class="total-value">1800 kcal</text>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const todayDate = ref('')
const todayWeekday = ref('')

onMounted(() => {
  const date = new Date()
  const year = date.getFullYear()
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')
  const weekdays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']

  todayDate.value = `${year}-${month}-${day}`
  todayWeekday.value = weekdays[date.getDay()]
})
</script>

<style scoped>
.today-recipe-container {
  background-color: #f5f5f5;
  min-height: 100vh;
  padding: 15px;
}

.date-header {
  background-color: #fff;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.date {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 5px;
}

.weekday {
  font-size: 16px;
  color: #666;
}

.recipe-section {
  margin-bottom: 20px;
}

.meal-type {
  font-size: 18px;
  font-weight: bold;
  color: #FF6B35;
  margin-bottom: 10px;
  padding-left: 5px;
}

.recipe-card {
  background-color: #fff;
  border-radius: 8px;
  padding: 15px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  display: flex;
  gap: 15px;
}

.recipe-image {
  width: 120px;
  height: 120px;
  border-radius: 6px;
}

.recipe-image.breakfast {
  background-color: #FFE0B2;
}

.recipe-image.lunch {
  background-color: #FFCCBC;
}

.recipe-image.dinner {
  background-color: #C8E6C9;
}

.recipe-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.recipe-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.recipe-details {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.detail-item {
  font-size: 14px;
  color: #666;
}

.detail-label {
  font-weight: 500;
}

.detail-value {
  color: #999;
}

.add-btn {
  margin-top: auto;
  background-color: #FF6B35;
  border: none;
  border-radius: 4px;
  color: #fff;
  padding: 10px;
  font-size: 14px;
  cursor: pointer;
}

.total-section {
  background-color: #fff;
  padding: 20px;
  border-radius: 8px;
  margin-top: 30px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.total-label {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.total-value {
  font-size: 24px;
  font-weight: bold;
  color: #FF6B35;
}
</style>