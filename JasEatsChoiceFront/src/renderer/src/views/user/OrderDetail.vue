<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import axios from 'axios'
import { API_CONFIG } from '../../config'
import { ElMessage } from 'element-plus'
import CommonBackButton from '../../components/common/CommonBackButton.vue'

const route = useRoute()
const orderId = ref(route.params.id)
const order = ref(null)
const loading = ref(true)

// 订单状态映射
const orderStatusMap = {
  all: '全部订单',
  processing: '进行中',
  pending: '待确认',
  pendingComment: '待评价',
  delivered: '已送达',
  completed: '已完成',
  cancelled: '已取消'
}

// 订单状态标签样式映射
const statusTagTypeMap = {
  processing: 'warning',
  pending: 'primary',
  pendingComment: 'info',
  delivered: 'success',
  completed: 'success',
  cancelled: 'danger'
}

// 加载订单详情
const loadOrderDetail = () => {
  loading.value = true
  axios.get(API_CONFIG.baseURL + API_CONFIG.order.detail + orderId.value)
    .then((response) => {
      if (response.data.data) {
        order.value = response.data.data
      }
    })
    .catch((error) => {
      console.error('加载订单详情失败:', error)
      ElMessage.error('加载订单详情失败')
    })
    .finally(() => {
      loading.value = false
    })
}

// 组件挂载时加载订单详情
onMounted(() => {
  loadOrderDetail()
})

// 取消订单
const cancelOrder = (order) => {
  // 调用后端API取消订单
  axios.put(API_CONFIG.baseURL + API_CONFIG.order.detail + order.id + '/cancel')
    .then((response) => {
      if (response.data.success) {
        order.status = 'cancelled'
        ElMessage.success('订单已取消')
      } else {
        ElMessage.error(response.data.message || '取消订单失败')
      }
    })
    .catch((error) => {
      console.error('取消订单失败:', error)
      ElMessage.error('取消订单失败，请稍后重试')
    })
}
</script>

<template>
  <div class="order-detail-container">
    <div class="page-header">
      <CommonBackButton />
      <h2 style="margin-left: 15px">订单详情</h2>
    </div>

    <div class="order-detail-card" v-loading="loading" element-loading-text="加载中...">
      <div v-if="order" class="order-info">
        <!-- 订单基本信息 -->
        <div class="order-base-info">
          <div class="order-no">订单号: {{ order.orderNo }}</div>
          <div class="order-status">
            <el-tag :type="statusTagTypeMap[order.status]">
              {{ orderStatusMap[order.status] }}
            </el-tag>
          </div>
        </div>

        <el-divider />

        <!-- 商家信息 -->
        <div class="merchant-info">
          <div class="merchant-title">商家信息</div>
          <div class="merchant-name">{{ order.merchant }}</div>
        </div>

        <el-divider />

        <!-- 商品列表 -->
        <div class="product-list">
          <div class="product-title">商品列表</div>
          <div class="product-item" v-for="item in order.items" :key="item">
            <div class="product-name">{{ item }}</div>
          </div>
        </div>

        <el-divider />

        <!-- 订单金额 -->
        <div class="order-total">
          <div class="total-text">总金额:</div>
          <div class="total-amount">¥{{ order.total.toFixed(2) }}</div>
        </div>

        <el-divider />

        <!-- 订单时间 -->
        <div class="order-time">
          <div class="time-text">下单时间:</div>
          <div class="time-value">{{ order.time }}</div>
        </div>

        <div class="order-actions">
          <!-- 评价按钮 -->
          <el-button
            v-if="order.status === 'pendingComment'"
            type="success"
            size="small"
            @click="$router.push(`/user/home/evaluate-order/${order.id}`)"
          >
            去评价
          </el-button>

          <!-- 取消订单按钮 -->
          <el-button
            v-if="order.status === 'processing'"
            type="danger"
            size="small"
            @click="cancelOrder(order)"
          >
            取消订单
          </el-button>
        </div>
      </div>
    </div>

    <!-- 空数据提示 -->
    <el-empty v-if="!order && !loading" description="暂无订单详情"></el-empty>
  </div>
</template>

<style scoped lang="less">
.order-detail-container {
  padding: 0 20px 20px 20px;

  .page-header {
    display: flex;
    align-items: center;
    margin-bottom: 20px;
  }

  h2 {
    font-size: 24px;
    margin: 0;
  }

  .order-detail-card {
    .order-base-info {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;

      .order-no {
        font-size: 18px;
        font-weight: bold;
      }

      .order-status {
        margin-left: 20px;
      }
    }

    .merchant-info {
      margin-bottom: 20px;

      .merchant-title {
        font-weight: bold;
        margin-bottom: 10px;
      }

      .merchant-name {
        font-size: 16px;
        color: #606266;
      }
    }

    .product-list {
      margin-bottom: 20px;

      .product-title {
        font-weight: bold;
        margin-bottom: 10px;
      }

      .product-item {
        margin-bottom: 8px;
        padding: 8px 12px;
        background-color: #f5f7fa;
        border-radius: 4px;

        .product-name {
          font-size: 14px;
        }
      }
    }

    .order-total {
      display: flex;
      justify-content: flex-end;
      align-items: center;
      margin-bottom: 20px;

      .total-text {
        margin-right: 10px;
        color: #606266;
      }

      .total-amount {
        font-size: 20px;
        font-weight: bold;
        color: #ff6b6b;
      }
    }

    .order-time {
      display: flex;
      align-items: center;
      margin-bottom: 20px;

      .time-text {
        margin-right: 10px;
        color: #606266;
      }

      .time-value {
        font-size: 14px;
      }
    }

    .order-actions {
      display: flex;
      justify-content: flex-end;
      gap: 10px;
      margin-top: 20px;
    }
  }
}

@media (max-width: 768px) {
  .order-detail-container {
    padding: 0 10px 10px 10px;

    .order-detail-card {
      .order-base-info {
        flex-direction: column;
        align-items: flex-start !important;

        .order-status {
          margin-left: 0;
          margin-top: 10px;
        }
      }

      .order-actions {
        flex-direction: column;
        gap: 8px;

        el-button {
          width: 100%;
        }
      }
    }
  }
}
</style>
