<template>
  <view class="my-recipe-container">
    <view class="filter-bar">
      <button class="filter-btn" :class="{ active: selectedFilter === 'all' }" @click="selectedFilter = 'all'">全部</button>
      <button class="filter-btn" :class="{ active: selectedFilter === 'breakfast' }" @click="selectedFilter = 'breakfast'">早餐</button>
      <button class="filter-btn" :class="{ active: selectedFilter === 'lunch' }" @click="selectedFilter = 'lunch'">午餐</button>
      <button class="filter-btn" :class="{ active: selectedFilter === 'dinner' }" @click="selectedFilter = 'dinner'">晚餐</button>
    </view>

    <view class="recipe-list">
      <view class="recipe-item" v-for="recipe in filteredRecipes" :key="recipe.id">
        <view class="recipe-header">
          <view class="recipe-date">{{ recipe.date }}</view>
          <view class="meal-tag" :class="recipe.mealType">{{ recipe.mealTypeText }}</view>
        </view>
        <view class="recipe-main">
          <view class="recipe-image" :class="recipe.mealType"></view>
          <view class="recipe-info">
            <view class="recipe-title">{{ recipe.title }}</view>
            <view class="recipe-meta">
              <view class="meta-item">
                <text class="meta-icon">🔥</text>
                <text class="meta-text">{{ recipe.calories }} kcal</text>
              </view>
              <view class="meta-item">
                <text class="meta-icon">⏱️</text>
                <text class="meta-text">{{ recipe.time }}</text>
              </view>
            </view>
            <view class="recipe-ingredients">{{ recipe.ingredients }}</view>
          </view>
        </view>
        <view class="recipe-actions">
          <button class="action-btn edit">编辑</button>
          <button class="action-btn delete">删除</button>
        </view>
      </view>
    </view>

    <button class="add-recipe-btn" type="primary" @click="addRecipe">
      + 添加新食谱
    </button>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'

const selectedFilter = ref('all')

const recipes = ref([
  {
    id: 1,
    date: '2025-11-23',
    mealType: 'breakfast',
    mealTypeText: '早餐',
    title: '营养早餐',
    calories: 450,
    time: '15分钟',
    ingredients: '牛奶、鸡蛋、面包'
  },
  {
    id: 2,
    date: '2025-11-23',
    mealType: 'lunch',
    mealTypeText: '午餐',
    title: '健康午餐',
    calories: 800,
    time: '30分钟',
    ingredients: '米饭、蔬菜、瘦肉'
  },
  {
    id: 3,
    date: '2025-11-22',
    mealType: 'dinner',
    mealTypeText: '晚餐',
    title: '轻食晚餐',
    calories: 550,
    time: '20分钟',
    ingredients: '蔬菜沙拉、鸡肉'
  }
])

const filteredRecipes = computed(() => {
  if (selectedFilter.value === 'all') {
    return recipes.value
  }
  return recipes.value.filter(recipe => recipe.mealType === selectedFilter.value)
})

const addRecipe = () => {
  uni.showToast({
    title: '添加功能开发中...',
    icon: 'none'
  })
}
</script>

<style scoped>
.my-recipe-container {
  background-color: #f5f5f5;
  min-height: 100vh;
  padding: 15px;
}

.filter-bar {
  background-color: #fff;
  border-radius: 8px;
  padding: 10px;
  margin-bottom: 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  display: flex;
  gap: 10px;
}

.filter-btn {
  padding: 8px 15px;
  border: 1px solid #ddd;
  border-radius: 20px;
  background-color: #fff;
  color: #666;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.filter-btn.active {
  border-color: #FF6B35;
  background-color: #FF6B35;
  color: #fff;
}

.recipe-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.recipe-item {
  background-color: #fff;
  border-radius: 8px;
  padding: 15px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.recipe-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.recipe-date {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.meal-tag {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  color: #fff;
}

.meal-tag.breakfast {
  background-color: #FFB74D;
}

.meal-tag.lunch {
  background-color: #FF7043;
}

.meal-tag.dinner {
  background-color: #81C784;
}

.recipe-main {
  display: flex;
  gap: 15px;
  margin-bottom: 15px;
}

.recipe-image {
  width: 100px;
  height: 100px;
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

.recipe-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.recipe-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.recipe-meta {
  display: flex;
  gap: 20px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 14px;
  color: #666;
}

.recipe-ingredients {
  font-size: 13px;
  color: #999;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.recipe-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.action-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
}

.action-btn.edit {
  background-color: #E3F2FD;
  color: #1E88E5;
}

.action-btn.delete {
  background-color: #FFEBEE;
  color: #E53935;
}

.add-recipe-btn {
  position: fixed;
  bottom: 20px;
  right: 20px;
  width: calc(100% - 40px);
  height: 50px;
  background-color: #FF6B35;
  border: none;
  border-radius: 25px;
  color: #fff;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(255, 107, 53, 0.3);
}
</style>