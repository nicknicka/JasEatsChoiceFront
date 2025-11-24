<script setup>
import { ref, computed } from 'vue';
import { ElMessage } from 'element-plus';

// 订单状态映射
const orderStatusMap = {
  'pending': { text: '待处理', icon: '🔴', type: 'danger' },
  'preparing': { text: '准备中', icon: '🟡', type: 'warning' },
  'completed': { text: '已完成', icon: '✅', type: 'success' }
};

// 模拟今日订单数据
const orders = ref([
  {
    id: 1,
    orderNo: 'JD20241121001',
    status: 'pending',
    user: '小明',
    phone: '138XXXX8888',
    address: '公司地址',
    total: 78.00,
    time: '2024-11-21 10:30',
    unread: true
  },
  {
    id: 2,
    orderNo: 'JD20241121002',
    status: 'preparing',
    user: '小红',
    phone: '139XXXX9999',
    address: '家庭地址',
    total: 45.00,
    time: '2024-11-21 10:35',
    unread: false
  },
  {
    id: 3,
    orderNo: 'JD20241121003',
    status: 'completed',
    user: '小刚',
    phone: '137XXXX7777',
    address: '学校地址',
    total: 62.00,
    time: '2024-11-21 10:40',
    unread: false
  },
  {
    id: 4,
    orderNo: 'JD20241121004',
    status: 'pending',
    user: '小李',
    phone: '136XXXX6666',
    address: '酒店地址',
    total: 128.00,
    time: '2024-11-21 11:00',
    unread: true
  }
]);

// 当前选中的状态筛选
const activeStatusFilter = ref('all');

// 搜索关键词
const searchKeyword = ref('');

// 筛选后的订单
const filteredOrders = ref([]);
filteredOrders.value = [...orders.value];

// 订单概览统计
const orderOverview = computed(() => {
  const total = filteredOrders.value.length;
  const totalAmount = filteredOrders.value.reduce((sum, order) => sum + order.total, 0);
  const pendingCount = filteredOrders.value.filter(order => order.status === 'pending').length;
  const preparingCount = filteredOrders.value.filter(order => order.status === 'preparing').length;
  const completedCount = filteredOrders.value.filter(order => order.status === 'completed').length;

  return {
    total,
    totalAmount,
    pendingCount,
    preparingCount,
    completedCount
  };
});

// 更新筛选
const updateFilter = () => {
  filteredOrders.value = orders.value.filter(order => {
    // 状态筛选
    const statusMatch = activeStatusFilter.value === 'all' || order.status === activeStatusFilter.value;

    // 搜索筛选
    const searchMatch = !searchKeyword.value ||
      order.orderNo.includes(searchKeyword.value) ||
      order.user.includes(searchKeyword.value);

    return statusMatch && searchMatch;
  });
};

// 查看订单详情
const viewOrderDetails = (order) => {
  // 实际应用中可以导航到订单详情页
  console.log('查看订单详情:', order);

  // 标记为已读
  if (order.unread) {
    order.unread = false;
    updateFilter();
    ElMessage.success('订单已标记为已读');
  }
};

// 更新订单状态
const updateOrderStatus = (order, newStatus) => {
  order.status = newStatus;
  updateFilter();
  ElMessage.success(`订单状态已更新为${orderStatusMap[newStatus].text}`);
};

// 页面加载时初始化筛选
updateFilter();
</script>

<template>
  <div class="merchant-orders-container">
    <div class="orders-header">
      <div class="header-left">
        <h3 class="page-title">【今日订单】</h3>
        <el-button type="text" class="back-btn">↩ 返回</el-button>
      </div>
    </div>

    <!-- 今日订单概览 -->
    <div class="overview-section">
      <div class="overview-info">
        <div class="overview-item">
          <span class="label">📊 今日订单概览：</span>
        </div>
        <div class="overview-stats">
          <span class="stat-item">🍽️ 总订单数：{{ orderOverview.total }}</span>
          <span class="stat-item">💰 总金额：¥{{ orderOverview.totalAmount.toFixed(2) }}</span>
        </div>
        <div class="status-stats">
          <span class="stat-item">🔴 待处理：{{ orderOverview.pendingCount }}</span>
          <span class="stat-item">🟡 准备中：{{ orderOverview.preparingCount }}</span>
          <span class="stat-item">✅ 已完成：{{ orderOverview.completedCount }}</span>
        </div>
      </div>

      <div class="search-section">
        <el-input
          v-model="searchKeyword"
          placeholder="输入订单号/用户名称..."
          style="width: 300px;"
          @input="updateFilter"
        />
      </div>
    </div>

    <!-- 订单列表 -->
    <div class="orders-list-section">
      <div class="orders-filter">
        <span class="filter-label">📋 订单列表 (状态筛选：</span>
        <el-tag
          v-for="status in ['all', 'pending', 'preparing', 'completed']"
          :key="status"
          :type="activeStatusFilter === status ? 'primary' : 'info'"
          effect="plain"
          @click="activeStatusFilter = status; updateFilter()"
          class="status-tag"
        >
          {{ status === 'all' ? '全部' : orderStatusMap[status].text }}
        </el-tag>
        <span>)</span>
      </div>

      <div class="orders-list">
        <div
          v-for="order in filteredOrders"
          :key="order.id"
          class="order-item"
        >
          <div class="order-left">
            <div class="order-basic-info">
              <div class="order-no">订单号：{{ order.orderNo }}</div>
              <div class="order-amount">💰 ¥{{ order.total.toFixed(2) }}</div>
              <div class="order-time">⏰ {{ order.time }}</div>
            </div>

            <div class="order-user-info">
              <div class="user-name">👤 用户：{{ order.user }}</div>
              <div class="user-phone">📞 {{ order.phone }}</div>
              <div class="user-address">📍 {{ order.address }}</div>
            </div>
          </div>

          <div class="order-right">
            <div class="order-status">
              <el-tag :type="orderStatusMap[order.status].type">
                {{ orderStatusMap[order.status].icon }} {{ orderStatusMap[order.status].text }}
              </el-tag>
              <el-badge v-if="order.unread" :value="''" type="danger" class="unread-badge" />
            </div>

            <div class="order-actions">
              <el-button
                type="primary"
                size="small"
                @click="viewOrderDetails(order)"
              >
                📝 查看详情
              </el-button>

              <!-- 状态转换按钮 -->
              <el-button
                v-if="order.status === 'pending'"
                type="success"
                size="small"
                @click="updateOrderStatus(order, 'preparing')"
              >
                🟡 标记为准备中
              </el-button>

              <el-button
                v-if="order.status === 'preparing'"
                type="success"
                size="small"
                @click="updateOrderStatus(order, 'completed')"
              >
                ✅ 标记为已完成
              </el-button>

              <el-button
                v-if="order.status !== 'completed'"
                type="danger"
                size="small"
                @click="updateOrderStatus(order, 'completed')"
              >
                🗑️ 取消订单
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 空数据提示 -->
      <div v-if="filteredOrders.length === 0" class="empty-orders">
        <el-empty description="暂无今日订单"></el-empty>
      </div>
    </div>
  </div>
</template>

<style scoped lang="less">
.merchant-orders-container {
  padding: 0 20px 20px 20px;

  .orders-header {
    margin-bottom: 20px;

    .page-title {
      font-size: 18px;
      font-weight: 600;
      margin: 0;
    }
  }

  .overview-section {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    padding: 16px;
    background-color: #f5f7fa;
    border-radius: 4px;
    margin-bottom: 24px;
    flex-wrap: wrap;
    gap: 20px;

    .overview-info {
      .overview-item {
        font-weight: 600;
        margin-bottom: 8px;
      }

      .overview-stats, .status-stats {
        display: flex;
        flex-wrap: wrap;
        gap: 24px;
        font-size: 14px;
        margin-bottom: 8px;
      }
    }
  }

  .orders-list-section {
    .orders-filter {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 24px;
      font-size: 14px;

      .status-tag {
        cursor: pointer;
        &:hover {
          opacity: 0.8;
        }
      }
    }

    .orders-list {
      .order-item {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        padding: 16px;
        border: 1px solid #e4e7ed;
        border-radius: 4px;
        margin-bottom: 12px;
        background-color: #fff;
        transition: box-shadow 0.3s;

        &:hover {
          box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
        }

        .order-left {
          flex: 1;
          margin-right: 20px;

          .order-basic-info {
            display: flex;
            flex-wrap: wrap;
            gap: 24px;
            margin-bottom: 12px;
            font-size: 14px;
          }

          .order-user-info {
            display: flex;
            flex-wrap: wrap;
            gap: 24px;
            font-size: 14px;
            color: #606266;
          }
        }

        .order-right {
          display: flex;
          flex-direction: column;
          align-items: flex-end;
          gap: 12px;

          .order-status {
            position: relative;

            .unread-badge {
              position: absolute;
              top: -5px;
              right: -5px;
            }
          }
        }
      }
    }

    .empty-orders {
      text-align: center;
      margin-top: 50px;
    }
  }
}
</style>
