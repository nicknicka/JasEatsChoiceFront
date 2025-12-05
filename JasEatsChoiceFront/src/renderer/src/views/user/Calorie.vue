<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { API_CONFIG } from '../../config/index.js';
import { ElMessage } from 'element-plus';

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
  // 添加模拟用户信息到localStorage（仅用于测试）
  let userInfo = JSON.parse(localStorage.getItem('userInfo'));
  if (!userInfo || !userInfo.userId) {
    userInfo = {
      userId: 1,
      username: 'testuser',
      email: 'test@example.com'
    };
    localStorage.setItem('userInfo', JSON.stringify(userInfo));
  }
  // 获取用户信息
  userInfo = JSON.parse(localStorage.getItem('userInfo'));
  const userId = userInfo.userId;

  // 获取用户偏好设置（包含卡路里目标和营养目标）
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

  // 直接从后端获取本周卡路里统计
  axios.get(`${API_CONFIG.baseURL}${API_CONFIG.diet.week.replace('{userId}', userId)}`)
    .then(response => {
      if (response.data && response.data.success) {
        // 直接使用后端返回的每周数据
        calorieData.value.weekly = response.data.data;
      }
    })
    .catch(error => {
      console.error('加载本周卡路里记录失败:', error);
      ElMessage.error('加载本周卡路里记录失败，请稍后重试');
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
    <div class="today-overview-section">
      <h3>今日概览</h3>
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
    </div>

    <!-- 周统计 -->
    <div class="weekly-statistics-section">
      <h3>本周统计</h3>
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
</div>
</template>

<style scoped lang="less">
.calorie-container {
  padding: 24px;
  min-height: 100vh;
  background: #f5f7fa;

  h2 {
    font-size: 28px;
    margin: 0 0 32px 0;
    text-align: center;
    color: #333;
    font-weight: 700;
  }

  .today-overview {
    display: flex;
    gap: 24px;
    margin-bottom: 32px;
    flex-wrap: wrap;

    .overview-card {
      flex: 1;
      min-width: 250px;
      background: rgba(255, 255, 255, 0.95) !important;
      border-radius: 16px !important;
      padding: 24px !important;
      box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
      transition: all 0.3s ease;

      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
      }

      .overview-item {
        text-align: center;

        .overview-label {
          font-size: 14px;
          color: #666;
          margin-bottom: 12px;
          text-transform: uppercase;
          letter-spacing: 1px;
        }

        .overview-value {
          font-size: 32px;
          font-weight: 700;
          background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%);
          -webkit-background-clip: text;
          -webkit-text-fill-color: transparent;
          background-clip: text;

          &.remaining {
            background: linear-gradient(135deg, #4CAF50 0%, #45a049 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
          }

          &.target {
            background: linear-gradient(135deg, #2196F3 0%, #0b7dda 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
          }
        }
      }
    }
  }

  .nutrition-card {
    margin-bottom: 32px;
    background: rgba(255, 255, 255, 0.95) !important;
    border-radius: 16px !important;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);

    .card-header {
      font-size: 18px;
      font-weight: 700;
      color: #333;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }

    .nutrition-chart {
      padding: 28px;
    }

    .nutrition-item {
      margin-bottom: 28px;

      &:last-child {
        margin-bottom: 0;
      }

      .nutrition-info {
        display: flex;
        justify-content: space-between;
        margin-bottom: 12px;
        font-size: 14px;
        font-weight: 600;
      }
    }
  }

  /* Section styles */
  .today-overview-section,
  .weekly-statistics-section {
    margin-bottom: 32px;
  }

  .today-overview-section h3,
  .weekly-statistics-section h3 {
    font-size: 22px;
    font-weight: 700;
    margin: 0 0 24px 0;
    color: #333;
  }

  .weekly-card {
    background: rgba(255, 255, 255, 0.95) !important;
    border-radius: 16px !important;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);

    .card-header {
      font-size: 18px;
      font-weight: 700;
      color: #333;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }

    .weekly-chart {
      padding: 32px;
    }

    .weekly-bar {
      margin-bottom: 32px;

      &:last-child {
        margin-bottom: 0;
      }

      .bar-label {
        margin-bottom: 16px;
        font-weight: 600;
        font-size: 14px;
        color: #555;
        text-transform: uppercase;
        letter-spacing: 0.5px;
      }

      .bar-container {
        width: 100%;
        height: 36px;
        background-color: #f0f0f0;
        border-radius: 18px;
        overflow: hidden;
        box-shadow: inset 0 2px 8px rgba(0, 0, 0, 0.08);
      }

      .bar-fill {
        height: 100%;
        background: linear-gradient(90deg, #2196F3 0%, #1976d2 100%);
        text-align: center;
        line-height: 36px;
        color: white;
        font-weight: 700;
        font-size: 14px;
        transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
      }

      .bar-fill:hover {
        background: linear-gradient(90deg, #1976d2 0%, #1565c0 100%);
        box-shadow: 0 0 12px rgba(33, 150, 243, 0.4);
      }

      .bar-fill.over-target {
        background: linear-gradient(90deg, #FF5252 0%, #f44336 100%);
      }

      .bar-fill.over-target:hover {
        background: linear-gradient(90deg, #f44336 0%, #d32f2f 100%);
        box-shadow: 0 0 12px rgba(244, 67, 54, 0.4);
      }
    }
  }
}

// 进度条样式
:deep(.el-progress) {
  .el-progress-bar__inner {
    background: linear-gradient(90deg, #FF6B6B 0%, #FF8E53 100%);
    box-shadow: 0 0 10px rgba(255, 107, 107, 0.4);
    transition: width 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
  }
}

</style>