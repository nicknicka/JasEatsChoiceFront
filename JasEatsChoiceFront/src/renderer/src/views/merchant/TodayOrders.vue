<script setup>
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import CommonBackButton from '../../components/common/CommonBackButton.vue'

const router = useRouter()

// 订单状态映射
const orderStatusMap = {
  pending: { text: '待处理', icon: '🔴', type: 'danger' },
  preparing: { text: '准备中', icon: '🟡', type: 'warning' },
  completed: { text: '已完成', icon: '✅', type: 'success' }
}

// 模拟全部订单数据
const orders = ref([
  {
    id: 1,
    orderNo: 'JD20241121001',
    status: 'pending',
    user: '小明',
    phone: '138XXXX8888',
    address: '公司地址',
    total: 78.0,
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
    total: 45.0,
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
    total: 62.0,
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
    total: 128.0,
    time: '2024-11-21 11:00',
    unread: true
  },
  {
    id: 5,
    orderNo: 'JD20241121005',
    status: 'preparing',
    user: '小王',
    phone: '135XXXX5555',
    address: '公园地址',
    total: 96.0,
    time: '2024-11-21 11:15',
    unread: false
  },
  {
    id: 6,
    orderNo: 'JD20241121006',
    status: 'pending',
    user: '小张',
    phone: '134XXXX4444',
    address: '医院地址',
    total: 58.5,
    time: '2024-11-21 11:30',
    unread: true
  },
  {
    id: 7,
    orderNo: 'JD20241121007',
    status: 'completed',
    user: '小赵',
    phone: '133XXXX3333',
    address: '车站地址',
    total: 156.0,
    time: '2024-11-21 10:25',
    unread: false
  },
  {
    id: 8,
    orderNo: 'JD20241121008',
    status: 'pending',
    user: '小钱',
    phone: '132XXXX2222',
    address: '商场地址',
    total: 32.8,
    time: '2024-11-21 11:45',
    unread: true
  },
  {
    id: 9,
    orderNo: 'JD20241121009',
    status: 'preparing',
    user: '小孙',
    phone: '131XXXX1111',
    address: '工厂地址',
    total: 89.0,
    time: '2024-11-21 12:00',
    unread: false
  }
])

// 当前选中的状态筛选
const activeStatusFilter = ref('all')

// 搜索关键词
const searchKeyword = ref('')

// 获取今天的日期（格式：YYYY-MM-DD）
const today = new Date().toISOString().split('T')[0]

// 筛选后的订单
const filteredOrders = ref([])
// 初始只显示今天的订单
filteredOrders.value = orders.value.filter((order) => order.time.startsWith(today))

// 订单概览统计
const orderOverview = computed(() => {
  const total = filteredOrders.value.length
  const totalAmount = filteredOrders.value.reduce((sum, order) => sum + order.total, 0)
  const pendingCount = filteredOrders.value.filter((order) => order.status === 'pending').length
  const preparingCount = filteredOrders.value.filter((order) => order.status === 'preparing').length
  const completedCount = filteredOrders.value.filter((order) => order.status === 'completed').length

  return {
    total,
    totalAmount,
    pendingCount,
    preparingCount,
    completedCount
  }
})

// 更新筛选
const updateFilter = () => {
  filteredOrders.value = orders.value.filter((order) => {
    // 日期筛选：仅今天
    const dateMatch = order.time.startsWith(today)

    // 状态筛选
    const statusMatch =
      activeStatusFilter.value === 'all' || order.status === activeStatusFilter.value

    // 搜索筛选
    const searchMatch =
      !searchKeyword.value ||
      order.orderNo.includes(searchKeyword.value) ||
      order.user.includes(searchKeyword.value)

    return dateMatch && statusMatch && searchMatch
  })
}

// 查看订单详情

const viewOrderDetails = (order) => {
  // 标记为已读
  if (order.unread) {
    order.unread = false
    updateFilter()
    ElMessage.success('订单已标记为已读')
  }

  // 导航到订单详情页
  router.push(`/merchant/home/order-detail/${order.id}`)
}

// 更新订单状态
const updateOrderStatus = (order, newStatus) => {
  order.status = newStatus
  updateFilter()
  ElMessage.success(`订单状态已更新为${orderStatusMap[newStatus].text}`)
}

// 取消订单前添加确认
const cancelOrder = (order) => {
  ElMessageBox.confirm('确定要取消此订单吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => {
      // 假设取消订单后状态变为'cancelled'，如果需要其他状态请修改
      updateOrderStatus(order, 'completed') // 当前代码中取消订单也设置为已完成，保持一致
      ElMessage.success('订单已取消')
    })
    .catch(() => {
      ElMessage.info('已取消订单取消操作')
    })
}

// 删除订单前添加确认
const deleteOrder = (order) => {
  ElMessageBox.confirm('确定要删除此订单吗?', '删除确认', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'error'
  })
    .then(() => {
      // 从订单列表中删除
      const index = orders.value.findIndex((item) => item.id === order.id)
      if (index !== -1) {
        orders.value.splice(index, 1)
        updateFilter()
        ElMessage.success('订单已删除')
      }
    })
    .catch(() => {
      ElMessage.info('已取消订单删除操作')
    })
}

// 页面加载时初始化筛选
updateFilter()
</script>

<template>
  <div class="merchant-orders-container">
    <div class="orders-header">
      <div class="header-left">
        <h3 class="page-title">【今日订单】</h3>
      </div>
      <div class="header-right">
        <common-back-button type="default" />
      </div>
    </div>

    <!-- 全部订单概览 -->
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
          style="width: 300px"
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
          class="status-tag"
          @click="((activeStatusFilter = status), updateFilter())"
        >
          {{ status === 'all' ? '全部' : orderStatusMap[status].text }}
        </el-tag>
        <span>)</span>
      </div>

      <div class="orders-list">
        <div v-for="order in filteredOrders" :key="order.id" class="order-item">
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
                {{ orderStatusMap[order.status].icon }}
                {{ orderStatusMap[order.status].text }}
              </el-tag>
              <el-badge v-if="order.unread" :value="''" type="danger" class="unread-badge" />
            </div>

            <div class="order-actions">
              <el-button type="primary" size="small" @click="viewOrderDetails(order)">
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
                @click="cancelOrder(order)"
              >
                🗑️ 取消订单
              </el-button>
              <el-button type="danger" size="small" @click="deleteOrder(order)">
                🗑️ 删除订单
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 空数据提示 -->
      <div v-if="filteredOrders.length === 0" class="empty-orders">
        <el-empty description="今日暂无订单"></el-empty>
      </div>
    </div>
  </div>
</template>

<style scoped lang="less">
.merchant-orders-container {
  padding: 0 20px 20px 20px;

  .orders-header {
    margin-bottom: 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;

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
    padding: 24px;
    background-color: #f8fafc;
    border: 2px solid #e2e8f0;
    border-radius: 8px;
    margin-bottom: 24px;
    flex-wrap: wrap;
    gap: 20px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);

    .overview-info {
      width: 100%;

      .overview-item {
        font-weight: 600;
        margin-bottom: 16px;
      }

      .overview-stats,
      .status-stats {
        display: grid;
        grid-template-columns: repeat(
          auto-fill,
          minmax(30%, 1fr)
        ); // 网格布局，每行自动填充，最小宽度200px
        gap: 24px;
        font-size: 14px;
        margin-bottom: 16px;
      }

      .stat-item {
        display: block; // 重置为块级元素以适应网格布局
        margin-right: 0; // 清除之前的右边距
        margin-bottom: 0; // 清除之前的下边距
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
        padding: 20px;
        border: 2px solid #e2e8f0;
        border-radius: 8px;
        margin-bottom: 16px;
        background-color: #fff;
        transition: all 0.3s ease;
        box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);

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
