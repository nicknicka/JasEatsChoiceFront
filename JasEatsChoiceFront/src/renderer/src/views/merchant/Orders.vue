<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'
import { API_CONFIG } from '../../config/index.js'
import CommonBackButton from '../../components/common/CommonBackButton.vue'

// 订单状态映射
const orderStatusMap = {
  1: { text: '待处理', icon: '🔴', type: 'danger' },
  2: { text: '准备中', icon: '🟡', type: 'warning' },
  5: { text: '已完成', icon: '✅', type: 'success' }
}

// 订单数据
const orders = ref([])

// 当前选中的状态筛选
const activeStatusFilter = ref('all')

// 搜索关键词
const searchKeyword = ref('')

// 筛选后的订单
const filteredOrders = ref([])

// 订单概览统计
const orderOverview = computed(() => {
  const total = filteredOrders.value.length
  const totalAmount = filteredOrders.value.reduce(
    (sum, order) => sum + (order.totalAmount ? order.totalAmount : 0),
    0
  )
  const pendingCount = filteredOrders.value.filter((order) => order.status === 1).length
  const preparingCount = filteredOrders.value.filter((order) => order.status === 2).length
  const completedCount = filteredOrders.value.filter((order) => order.status === 5).length

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
    // 状态筛选
    const statusMatch =
      activeStatusFilter.value === 'all' || order.status === parseInt(activeStatusFilter.value)

    // 搜索筛选
    const searchMatch = !searchKeyword.value || order.id.toString().includes(searchKeyword.value)

    return statusMatch && searchMatch
  })
}

// 查看订单详情
import { useRouter, useRoute } from 'vue-router'
const router = useRouter()
const route = useRoute()

// 初始化时从URL获取搜索参数并加载订单数据
onMounted(() => {
  // 模拟商家ID，实际应用中应从登录信息获取
  const merchantId = 1

  // 从API获取订单数据
  axios
    .get(`${API_CONFIG.baseURL}/v1/orders/merchant/${merchantId}`)
    .then((response) => {
      if (response.data && response.data.success) {
        orders.value = response.data.data
        updateFilter()
      }
    })
    .catch((error) => {
      console.error('加载订单失败:', error)
      ElMessage.error('加载订单失败')
    })

  const searchParam = route.query.search
  if (searchParam) {
    searchKeyword.value = searchParam
    updateFilter()
  }
})

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
  // 调用后端API更新状态
  axios
    .put(`${API_CONFIG.baseURL}/api/v1/orders/${order.id}/status`, null, {
      params: {
        status: newStatus
      }
    })
    .then((response) => {
      if (response.data && response.data.success) {
        order.status = newStatus
        updateFilter()
        ElMessage.success(`订单状态已更新为${orderStatusMap[newStatus].text}`)
      } else {
        ElMessage.error('更新订单状态失败')
      }
    })
    .catch((error) => {
      console.error('更新订单状态失败:', error)
      ElMessage.error('更新订单状态失败')
    })
}

// 取消订单前添加确认
const cancelOrder = (order) => {
  ElMessageBox.confirm('确定要取消此订单吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => {
      // 调用更新订单状态API，6表示已取消
      updateOrderStatus(order, 6)
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
        <h3 class="page-title">【全部订单】</h3>
      </div>
      <div class="header-right">
        <CommonBackButton type="default" text="🔙 返回" />
      </div>
    </div>

    <!-- 全部订单概览 -->
    <div class="overview-section">
      <div class="overview-info">
        <div class="overview-item">
          <span class="label">📊 全部订单概览：</span>
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
          v-for="status in ['all', 1, 2, 5]"
          :key="status"
          :type="activeStatusFilter === status ? 'primary' : 'info'"
          effect="plain"
          @click="
            () => {
              activeStatusFilter = status
              updateFilter()
            }
          "
          class="status-tag"
        >
          {{ status === 'all' ? '全部' : orderStatusMap[status].text }}
        </el-tag>
        <span>)</span>
      </div>

      <div class="orders-list">
        <div v-for="order in filteredOrders" :key="order.id" class="order-item">
          <div class="order-left">
            <div class="order-basic-info">
              <div class="order-no">订单号：{{ order.id }}</div>
              <div class="order-amount">💰 ¥{{ order.totalAmount?.toFixed(2) || '0.00' }}</div>
              <div class="order-time">⏰ {{ order.createTime }}</div>
            </div>

            <div class="order-user-info">
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
              <el-button type="primary" size="small" @click="viewOrderDetails(order)">
                📝 查看详情
              </el-button>

              <!-- 状态转换按钮 -->
              <el-button
                v-if="order.status === 1"
                type="success"
                size="small"
                @click="updateOrderStatus(order, 2)"
              >
                🟡 标记为准备中
              </el-button>

              <el-button
                v-if="order.status === 2"
                type="success"
                size="small"
                @click="updateOrderStatus(order, 5)"
              >
                ✅ 标记为已完成
              </el-button>

              <el-button
                v-if="order.status !== 5"
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
