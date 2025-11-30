
<script setup>
import { ref, onMounted, watch, nextTick, onUnmounted } from 'vue';
import { ElMessage } from 'element-plus';
import { use } from 'echarts/core';
import { LineChart } from 'echarts/charts';
import {
  TitleComponent,
  TooltipComponent,
  GridComponent,
  DataZoomComponent,
  LegendComponent
} from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';
import VChart from 'vue-echarts';

// 注册所需组件
use([
  TitleComponent,
  TooltipComponent,
  GridComponent,
  DataZoomComponent,
  LegendComponent,
  LineChart,
  CanvasRenderer
]);

// 统计时间范围选项
const timeRangeOptions = ['today', 'yesterday', 'week', 'month'];
const activeTimeRange = ref('today');

// 图表容器宽度
const chartContainerWidth = ref(0);

// 图表引用
const chartRef = ref(null);

// 模拟销售额数据
const salesData = ref({
  today: [
    { time: '00:00', value: 120 },
    { time: '01:00', value: 100 },
    { time: '02:00', value: 80 },
    { time: '03:00', value: 150 },
    { time: '04:00', value: 200 },
    { time: '05:00', value: 180 },
    { time: '06:00', value: 250 },
    { time: '07:00', value: 300 },
    { time: '08:00', value: 400 },
    { time: '09:00', value: 350 }
  ],
  yesterday: [
    { time: '00:00', value: 100 },
    { time: '01:00', value: 90 },
    { time: '02:00', value: 70 },
    { time: '03:00', value: 130 },
    { time: '04:00', value: 180 },
    { time: '05:00', value: 160 },
    { time: '06:00', value: 230 },
    { time: '07:00', value: 280 },
    { time: '08:00', value: 380 },
    { time: '09:00', value: 330 }
  ],
  week: [
    { time: '周一', value: 2000 },
    { time: '周二', value: 2200 },
    { time: '周三', value: 1800 },
    { time: '周四', value: 2500 },
    { time: '周五', value: 3000 },
    { time: '周六', value: 3500 },
    { time: '周日', value: 2800 }
  ],
  month: [
    { time: '1日', value: 8000 },
    { time: '2日', value: 7500 },
    { time: '3日', value: 9000 },
    { time: '4日', value: 8500 },
    { time: '5日', value: 10000 }
  ]
});

// 模拟菜品销售数据
const dishSalesData = ref([
  { name: '宫保鸡丁', sales: 120, revenue: 3360 },
  { name: '麻婆豆腐', sales: 150, revenue: 2700 },
  { name: '鱼香肉丝', sales: 180, revenue: 5040 },
  { name: '糖醋排骨', sales: 90, revenue: 3780 },
  { name: '回锅肉', sales: 110, revenue: 3960 }
]);

// 当前显示的销售额数据
const currentSalesData = ref([]);

// 更新当前显示的销售额数据
const updateSalesData = () => {
  currentSalesData.value = salesData.value[activeTimeRange.value];
  currentBasicStats.value = basicStats.value[activeTimeRange.value];
  currentOrderTrend.value = orderTrend.value[activeTimeRange.value];

  // 更新图表数据
  updateChartData();
};

// 从后端获取统计数据
const fetchStatisticsData = () => {
  const merchantId = 1; // 假设商家ID为1，可以根据实际情况从登录信息或路由参数中获取
  api.get(`/api/v1/merchant/${merchantId}/statistics`, { params: { timeRange: activeTimeRange.value } })
    .then(response => {
      if (response.code === '200' && response.data) {
        // 更新基本统计数据
        currentBasicStats.value = response.data.basicStats;

        // 更新订单趋势数据
        if (response.data.orderTrend) {
          currentOrderTrend.value = response.data.orderTrend;
          // 更新图表数据
          updateChartData();
        }

        // 更新菜品销量排行数据
        if (response.data.dishSalesRank) {
          dishSalesRank.value = response.data.dishSalesRank;
        }
      }
    })
    .catch(error => {
      console.error('获取统计数据失败:', error);
      // 如果获取失败，保留模拟数据
      updateSalesData();
    });
};

// 时间范围变化时调用的方法
const changeTimeRange = (range) => {
  activeTimeRange.value = range;
  fetchStatisticsData();
};

// 监听时间范围变化更新数据
watch(() => activeTimeRange.value, fetchStatisticsData);

// 页面加载时初始化数据
onMounted(() => {
  fetchStatisticsData();
  // 初始化图表容器宽度
  nextTick(() => {
    updateChartContainerWidth();
  });

  // 监听窗口大小变化
  window.addEventListener('resize', updateChartContainerWidth);
});

// 在组件卸载时移除事件监听器
onUnmounted(() => {
  window.removeEventListener('resize', updateChartContainerWidth);
});

// 更新图表容器宽度
const updateChartContainerWidth = () => {
  nextTick(() => {
    if (chartRef.value && chartRef.value.$el) {
      chartContainerWidth.value = chartRef.value.$el.clientWidth;
    } else if (chartRef.value && chartRef.value.$el === undefined) {
      // 如果 $el 不存在，尝试使用元素本身
      chartContainerWidth.value = chartRef.value.clientWidth || 0;
    }
  });
};

// 模拟基础统计数据 - 按时间范围
const basicStats = ref({
  today: {
    orders: 156,
    totalAmount: 8900.00,
    avgAmount: 57.05,
    newCustomers: 35
  },
  yesterday: {
    orders: 142,
    totalAmount: 8200.50,
    avgAmount: 57.75,
    newCustomers: 28
  },
  week: {
    orders: 890,
    totalAmount: 51200.00,
    avgAmount: 57.53,
    newCustomers: 165
  },
  month: {
    orders: 3560,
    totalAmount: 204300.75,
    avgAmount: 57.40,
    newCustomers: 680
  }
});

// 当前显示的基础统计数据
const currentBasicStats = ref({ ...basicStats.value.today });

// 模拟订单趋势数据 - 按时间范围
const orderTrend = ref({
  today: [
    { time: '00:00', orders: 12 },
    { time: '03:00', orders: 8 },
    { time: '06:00', orders: 25 },
    { time: '09:00', orders: 40 },
    { time: '12:00', orders: 55 },
    { time: '15:00', orders: 60 }
  ],
  yesterday: [
    { time: '00:00', orders: 10 },
    { time: '03:00', orders: 7 },
    { time: '06:00', orders: 22 },
    { time: '09:00', orders: 38 },
    { time: '12:00', orders: 52 },
    { time: '15:00', orders: 58 }
  ],
  week: [
    { time: '周一', orders: 125 },
    { time: '周二', orders: 130 },
    { time: '周三', orders: 145 },
    { time: '周四', orders: 160 },
    { time: '周五', orders: 180 },
    { time: '周六', orders: 210 },
    { time: '周日', orders: 195 }
  ],
  month: [
    { time: '第一周', orders: 680 },
    { time: '第二周', orders: 850 },
    { time: '第三周', orders: 1020 },
    { time: '第四周', orders: 1210 }
  ]
});

// 当前显示的订单趋势数据
const currentOrderTrend = ref([...orderTrend.value.today]);

// 模拟菜品销量排行数据
const dishSalesRank = ref([
  { name: '宫保鸡丁', sales: 120, revenue: 3360 },
  { name: '麻婆豆腐', sales: 150, revenue: 2700 },
  { name: '鱼香肉丝', sales: 180, revenue: 5040 },
  { name: '糖醋排骨', sales: 90, revenue: 3780 },
  { name: '回锅肉', sales: 110, revenue: 3960 }
]);

// 配置订单趋势图表
const orderChartOptions = ref({
  title: {
    text: '订单趋势',
    textStyle: {
      fontSize: 14
    }
  },
  tooltip: {
    trigger: 'axis',
    formatter: '{b}: {c} 单'
  },
  xAxis: {
    type: 'category',
    data: orderTrend.value.today.map(item => item.time)
  },
  yAxis: {
    type: 'value',
    axisLabel: {
      formatter: '{value} 单'
    }
  },
  series: [
    {
      name: '订单数',
      data: orderTrend.value.today.map(item => item.orders),
      type: 'line',
      smooth: true,
      lineStyle: {
        color: '#67c23a'
      },
      itemStyle: {
        color: '#67c23a'
      }
    }
  ]
});

// 更新图表数据
const updateChartData = () => {
  orderChartOptions.value.xAxis.data = currentOrderTrend.value.map(item => item.time);
  orderChartOptions.value.series[0].data = currentOrderTrend.value.map(item => item.orders);
};

// 监听数据变化并更新图表
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
            <div class="stat-value">{{ currentBasicStats.orders }}</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon revenue-icon">💰</div>
          <div class="stat-info">
            <div class="stat-label">总销售额</div>
            <div class="stat-value">¥{{ currentBasicStats.totalAmount.toFixed(2) }}</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon avg-icon">📊</div>
          <div class="stat-info">
            <div class="stat-label">客单价</div>
            <div class="stat-value">¥{{ currentBasicStats.avgAmount.toFixed(2) }}</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon new-customers-icon">👤</div>
          <div class="stat-info">
            <div class="stat-label">新客户数</div>
            <div class="stat-value">{{ currentBasicStats.newCustomers }}</div>
          </div>
        </div>
      </div>

      <!-- 订单趋势图表 -->
      <div class="order-trend-section">
        <h4 class="section-title">📈 订单趋势</h4>
        <div class="chart-container" v-show="true">
          <v-chart
            v-if="chartContainerWidth > 0"
            :options="orderChartOptions"
            style="height: 250px; width: 100%"
            :autoresize="true"
            ref="chartRef"
          />
          <div v-else class="chart-placeholder">
            图表加载中...
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
        flex: 1;

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

      .chart-container {
        min-height: 250px;
        display: flex;
        align-items: center;
        justify-content: center;
        width: 100%;
        
        .chart-placeholder {
          color: #909399;
          font-size: 14px;
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
