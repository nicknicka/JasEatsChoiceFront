<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { API_CONFIG } from '../../config/index.js';

// 卡路里统计数据
const calorieData = ref({
  today: {
    consumed: 0,
    remaining: 0,
    target: 2000 // 默认值，将从API获取
  },
  weekly: [
    { day: '周一', consumed: 1850 },
    { day: '周二', consumed: 1920 },
    { day: '周三', consumed: 1780 },
    { day: '周四', consumed: 1950 },
    { day: '周五', consumed: 1680 },
    { day: '周六', consumed: 2150 },
    { day: '周日', consumed: 2080 }
  ],
  nutrition: [
    { name: '蛋白质', value: 0, unit: 'g' },
    { name: '碳水化合物', value: 0, unit: 'g' },
    { name: '脂肪', value: 0, unit: 'g' }
  ]
});

// 推荐营养目标（根据膳食指南）
const recommendedGoals = ref({
  '蛋白质': 90, // g
  '碳水化合物': 250, // g
  '脂肪': 70 // g
});

// 自定义营养目标（用户设置）
const customGoals = ref({});

// 从API获取数据
onMounted(() => {
  // 获取用户偏好设置（包含卡路里目标和营养目标）
  const userId = 1; // 临时用户ID，实际应用中应从登录信息获取
  axios.get(`${API_CONFIG.baseURL}${API_CONFIG.user.preferences.replace('{userId}', userId)}`)
    .then(response => {
      if (response.data && response.data.success) {
        // 设置卡路里目标
        if (response.data.data.calorieTarget) {
          calorieData.value.today.target = response.data.data.calorieTarget;
        }

        // 设置自定义营养目标
        if (response.data.data.nutritionGoals) {
          customGoals.value = response.data.data.nutritionGoals;
        }
      }
    })
    .catch(error => {
      console.error('加载用户偏好失败:', error);
    });

  // 获取今日食谱和营养数据
  axios.get(`${API_CONFIG.baseURL}${API_CONFIG.recipe.today}`)
    .then(response => {
      if (response.data && response.data.success && response.data.data.nutrition) {
        const nutrition = response.data.data.nutrition;
        // 更新今日卡路里消耗
        calorieData.value.today.consumed = nutrition.calories;
        calorieData.value.today.remaining = calorieData.value.today.target - nutrition.calories;

        // 更新营养数据
        calorieData.value.nutrition[0].value = nutrition.protein; // 蛋白质
        calorieData.value.nutrition[1].value = nutrition.carbs; // 碳水化合物
        calorieData.value.nutrition[2].value = nutrition.fat; // 脂肪
      }
    })
    .catch(error => {
      console.error('加载今日营养数据失败:', error);
    });
});

// 获取营养百分比
const getNutritionPercentage = (value, name) => {
  // 优先使用用户自定义目标，若无则使用推荐目标
  const goal = customGoals.value[name] || recommendedGoals.value[name] || 1;
  // 避免除以0
  let percentage = goal > 0 ? (value / goal) * 100 : 0;
  // 四舍五入保留两位小数
  return Math.round(percentage * 100) / 100;
};

// 获取营养颜色
const getNutritionColor = (name) => {
  switch (name) {
    case '蛋白质':
      return '#2196F3';
    case '碳水化合物':
      return '#4CAF50';
    case '脂肪':
      return '#FF9800';
    default:
      return '#FF6B6B';
  }
};
</script>

<template>
  <div class="calorie-container">
    <h2>卡路里统计</h2>

    <!-- 今日概览 -->
    <div class="today-overview">
      <el-card class="overview-card">
        <div class="overview-item">
          <div class="overview-label">今日已摄入</div>
          <div class="overview-value">{{ calorieData.today.consumed }} kcal</div>
        </div>
      </el-card>

      <el-card class="overview-card">
        <div class="overview-item">
          <div class="overview-label">今日剩余</div>
          <div class="overview-value remaining">{{ calorieData.today.remaining }} kcal</div>
        </div>
      </el-card>

      <el-card class="overview-card">
        <div class="overview-item">
          <div class="overview-label">今日目标</div>
          <div class="overview-value target">{{ calorieData.today.target }} kcal</div>
        </div>
      </el-card>
    </div>

    <!-- 营养比例 -->
    <el-card class="nutrition-card">
      <template #header>
        <div class="card-header">营养摄入比例</div>
      </template>
      <div class="nutrition-chart">
        <div
          v-for="item in calorieData.nutrition"
          :key="item.name"
          class="nutrition-item"
        >
          <div class="nutrition-info">
            <div class="nutrition-name">{{ item.name }}</div>
            <div class="nutrition-value">{{ item.value }}{{ item.unit }}</div>
          </div>
          <el-progress
            :percentage="getNutritionPercentage(item.value, item.name)"
            :color="getNutritionColor(item.name)"
            :format="() => getNutritionPercentage(item.value, item.name) + '%'"
          />
        </div>
      </div>
    </el-card>

    <!-- 周统计 -->
    <el-card class="weekly-card">
      <template #header>
        <div class="card-header">本周卡路里摄入</div>
      </template>
      <div class="weekly-chart">
        <div
          v-for="item in calorieData.weekly"
          :key="item.day"
          class="weekly-bar"
        >
          <div class="bar-label">{{ item.day }}</div>
          <div class="bar-container">
            <div
              class="bar-fill"
              :style="{ width: (item.consumed / calorieData.today.target * 100).toFixed(2) + '%' }"
              :class="{ 'over-target': item.consumed > calorieData.today.target }"
            >
              {{ item.consumed }}
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<style scoped lang="less">
.calorie-container {
  padding: 0 20px 20px 20px;

  h2 {
    font-size: 24px;
    margin: 0 0 20px 0;
  }

  .today-overview {
    display: flex;
    gap: 20px;
    margin-bottom: 20px;

    .overview-card {
      flex: 1;

      .overview-item {
        text-align: center;

        .overview-label {
          font-size: 14px;
          color: #666;
          margin-bottom: 5px;
        }

        .overview-value {
          font-size: 28px;
          font-weight: bold;
          color: #FF6B6B;

          &.remaining {
            color: #4CAF50;
          }

          &.target {
            color: #2196F3;
          }
        }
      }
    }
  }

  .nutrition-card {
    margin-bottom: 20px;

    .card-header {
      font-size: 18px;
      font-weight: bold;
    }

    .nutrition-chart {
      padding: 20px 0;
    }

    .nutrition-item {
      margin-bottom: 20px;

      .nutrition-info {
        display: flex;
        justify-content: space-between;
        margin-bottom: 10px;

        .nutrition-name {
          font-weight: bold;
        }
      }
    }
  }

  .weekly-card {
    .card-header {
      font-size: 18px;
      font-weight: bold;
    }

    .weekly-chart {
      padding: 20px 0;
    }

    .weekly-bar {
      margin-bottom: 20px;

      .bar-label {
        margin-bottom: 10px;
        font-weight: bold;
      }

      .bar-container {
        width: 100%;
        height: 30px;
        background-color: #E0E0E0;
        border-radius: 4px;
        overflow: hidden;
      }

      .bar-fill {
        height: 100%;
        background-color: #2196F3;
        text-align: center;
        line-height: 30px;
        color: white;
        font-weight: bold;
        transition: width 0.3s;
      }

      .bar-fill.over-target {
        background-color: #FF5252;
      }
    }
  }
}

// 进度条颜色
:deep(.el-progress-bar__inner) {
  background-color: #FF6B6B;
}
</style>