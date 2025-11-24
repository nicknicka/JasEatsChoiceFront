
<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';

// 统计时间范围选项
const timeRangeOptions = ['today', 'yesterday', 'week', 'month'];
const activeTimeRange = ref('today');

// 基本统计数据
const basicStats = ref({
  orders: 125,
  totalAmount: 1895.50,
  avgAmount: 15.17,
  newCustomers: 30
});

// 订单趋势数据
const orderTrend = ref([
  { time: '00:00', orders: 5 },
  { time: '04:00', orders: 0 },
  { time: '08:00', orders: 20 },
  { time: '12:00', orders: 50 },
  { time: '16:00', orders: 30 },
  { time: '20:00', orders: 20 }
]);

// 菜品销量排行
const dishSalesRank = ref([
  { name: '麻辣香锅饭', sales: 35, revenue: 630 },
  { name: '鱼香肉丝面', sales: 28, revenue: 448 },
  { name: '宫保鸡丁饭', sales: 22, revenue: 396 },
  { name: '酸辣汤', sales: 45, revenue: 360 },
  { name: '可乐', sales: 60, revenue: 180 }
]);

// 页面加载
onMounted(() => {
  // 模拟数据加载
});

// 切换时间范围
const changeTimeRange = (range) => {
  activeTimeRange.value = range;
  // 模拟更新统计数据
  ElMessage.info(`切换到${range === 'today' ? '今日' : range === 'yesterday' ? '昨日' : range === 'week' ? '本周' : '本月'}统计`);
};
</script>

<template>
  <div class="statistics-container">
    <div class="stats-header">
      <h3 class="page-title">【经营统计】</h3>
      <div class="time-range-selector">
        <el-tag
          v-for="range in timeRangeOptions"
          :key="range"
          :type="activeTimeRange === range ? 'primary' : 'info'"
          effect="plain"
          @click="changeTimeRange(range)"
          class="time-range-tag"
        >
          {{ range === 'today' ? '今日' : range === 'yesterday' ? '昨日' : range === 'week' ? '本周' : '本月' }}
        </el-tag>
      </div>
    </div>

    <div class="stats-content">
      <!-- 基本统计卡片 -->
      <div class="basic-stats-section">
        <div class="stat-card">
          <div class="stat-icon orders-icon">🍽️</div>
          <div class="stat-info">
            <div class="stat-label">总订单数</div>
            <div class="stat-value">{{ basicStats.orders }}</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon revenue-icon">💰</div>
          <div class="stat-info">
            <div class="stat-label">总销售额</div>
            <div class="stat-value">¥{{ basicStats.totalAmount.toFixed(2) }}</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon avg-icon">📊</div>
          <div class="stat-info">
            <div class="stat-label">客单价</div>
            <div class="stat-value">¥{{ basicStats.avgAmount.toFixed(2) }}</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon new-customers-icon">👤</div>
          <div class="stat-info">
            <div class="stat-label">新客户数</div>
            <div class="stat-value">{{ basicStats.newCustomers }}</div>
          </div>
        </div>
      </div>

      <!-- 订单趋势图表 -->
      <div class="order-trend-section">
        <h4 class="section-title">📈 订单趋势</h4>
        <div class="chart-placeholder">
          <div class="chart-line-container">
            <div
              v-for="item in orderTrend"
              :key="item.time"
              class="chart-item"
              :style="{ left: `${orderTrend.indexOf(item) * 16.66}%`, height: `${(item.orders / 50) * 100}%` }"
            >
              <div class="chart-point"></div>
              <div class="chart-value">{{ item.orders }}</div>
              <div class="chart-time">{{ item.time }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 菜品销量排行 -->
      <div class="dish-sales-section">
        <h4 class="section-title">🏆 菜品销量排行</h4>
        <div class="sales-rank-list">
          <div
            v-for="(dish, index) in dishSalesRank"
            :key="dish.name"
            class="sales-rank-item"
          >
            <div class="rank-number">{{ index + 1 }}</div>
            <div class="dish-info">
              <div class="dish-name">{{ dish.name }}</div>
              <div class="dish-sales">销量: {{ dish.sales }} 份</div>
            </div>
            <div class="dish-revenue">¥{{ dish.revenue }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="less">
.statistics-container {
  padding: 0 20px 20px 20px;

  .stats-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    .page-title {
      font-size: 18px;
      font-weight: 600;
      margin: 0;
    }

    .time-range-selector {
      display: flex;
      gap: 8px;

      .time-range-tag {
        cursor: pointer;
        &:hover {
          opacity: 0.8;
        }
      }
    }
  }

  .stats-content {
    .basic-stats-section {
      display: flex;
      gap: 20px;
      margin-bottom: 24px;
      flex-wrap: wrap;

      .stat-card {
        display: flex;
        align-items: center;
        gap: 16px;
        background-color: #fff;
        padding: 20px;
        border-radius: 8px;
        box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
        min-width: 200px;

        .stat-icon {
          font-size: 32px;
        }

        .stat-info {
          .stat-label {
            font-size: 14px;
            color: #606266;
            margin-bottom: 4px;
          }

          .stat-value {
            font-size: 20px;
            font-weight: 600;
            color: #303133;
          }
        }
      }
    }

    .order-trend-section {
      background-color: #fff;
      border-radius: 8px;
      padding: 16px;
      margin-bottom: 20px;
      box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);

      .section-title {
        font-size: 16px;
        font-weight: 600;
        margin-bottom: 20px;
      }

      .chart-placeholder {
        height: 250px;
        background-color: #f5f7fa;
        border-radius: 4px;
        padding: 20px;
        position: relative;

        .chart-line-container {
          position: absolute;
          bottom: 40px;
          left: 20px;
          right: 20px;
          top: 20px;
          display: flex;
          align-items: flex-end;
          justify-content: space-between;

          .chart-item {
            position: relative;
            width: 10%;
            height: 100%;
            display: flex;
            flex-direction: column;
            justify-content: flex-end;
            align-items: center;

            .chart-point {
              width: 8px;
              height: 8px;
              background-color: #67c23a;
              border-radius: 50%;
            }

            .chart-value {
              margin: 8px 0;
              font-size: 12px;
              color: #606266;
            }

            .chart-time {
              font-size: 12px;
              color: #909399;
            }
          }
        }
      }
    }

    .dish-sales-section {
      background-color: #fff;
      border-radius: 8px;
      padding: 16px;
      box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);

      .section-title {
        font-size: 16px;
        font-weight: 600;
        margin-bottom: 20px;
      }

      .sales-rank-list {
        .sales-rank-item {
          display: flex;
          align-items: center;
          gap: 16px;
          padding: 12px 0;
          border-bottom: 1px solid #eee;

          &:last-child {
            border-bottom: none;
          }

          .rank-number {
            font-size: 18px;
            font-weight: 600;
            width: 30px;
            text-align: center;
          }

          .dish-info {
            flex: 1;

            .dish-name {
              font-size: 14px;
              font-weight: 500;
              margin-bottom: 4px;
            }

            .dish-sales {
              font-size: 12px;
              color: #606266;
            }
          }

          .dish-revenue {
            font-size: 16px;
            font-weight: 600;
            color: #67c23a;
          }
        }
      }
    }
  }
}
</style>
