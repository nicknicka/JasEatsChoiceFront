<script setup>
import { ref } from 'vue';

// 卡路里统计数据
const calorieData = ref({
  today: {
    consumed: 1560,
    remaining: 440,
    target: 2000
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
    { name: '蛋白质', value: 75, unit: 'g' },
    { name: '碳水化合物', value: 210, unit: 'g' },
    { name: '脂肪', value: 52, unit: 'g' }
  ]
});

// 获取营养百分比（模拟）
const getNutritionPercentage = (value, name) => {
  switch (name) {
    case '蛋白质':
      return (value / 90) * 100; // 目标90g
    case '碳水化合物':
      return (value / 250) * 100; // 目标250g
    case '脂肪':
      return (value / 70) * 100; // 目标70g
    default:
      return 0;
  }
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
              :style="{ width: (item.consumed / calorieData.today.target * 100) + '%' }"
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
