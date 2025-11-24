<template>
  <view class="orders-container">
    <view class="filter-bar">
      <button class="filter-btn" :class="{ active: selectedFilter === 'all' }" @click="selectedFilter = 'all'">全部</button>
      <button class="filter-btn" :class="{ active: selectedFilter === 'pending' }" @click="selectedFilter = 'pending'">待支付</button>
      <button class="filter-btn" :class="{ active: selectedFilter === 'processing' }" @click="selectedFilter = 'processing'">处理中</button>
      <button class="filter-btn" :class="{ active: selectedFilter === 'completed' }" @click="selectedFilter = 'completed'">已完成</button>
    </view>

    <view class="orders-list">
      <view class="order-item" v-for="order in filteredOrders" :key="order.id">
        <view class="order-header">
          <view class="order-info">
            <view class="order-no">订单号: {{ order.orderNo }}</view>
            <view class="order-date">{{ order.date }}</view>
          </view>
          <view class="order-status" :class="order.status">{{ order.statusText }}</view>
        </view>
        <view class="order-details">
          <view class="order-item-row" v-for="item in order.items" :key="item.id">
            <view class="item-name">{{ item.name }}</view>
            <view class="item-price">¥{{ item.price.toFixed(2) }} x{{ item.quantity }}</view>
          </view>
        </view>
        <view class="order-total">
          <view class="total-label">总计:</view>
          <view class="total-amount">¥{{ order.total.toFixed(2) }}</view>
        </view>
        <view class="order-actions">
          <button
            class="action-btn"
            v-if="order.status === 'pending'"
            type="primary"
            @click="payOrder(order)"
          >
            立即支付
          </button>
          <button
            class="action-btn"
            v-if="order.status === 'completed'"
            @click="reviewOrder(order)"
          >
            评价
          </button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'

const selectedFilter = ref('all')

const orders = ref([
  {
    id: 1,
    orderNo: 'JSCY202511230001',
    date: '2025-11-23 12:34:56',
    status: 'pending',
    statusText: '待支付',
    items: [
      { id: 1, name: '宫保鸡丁', price: 28.00, quantity: 1 },
      { id: 2, name: '米饭', price: 2.00, quantity: 2 }
    ],
    total: 32.00
  },
  {
    id: 2,
    orderNo: 'JSCY202511220002',
    date: '2025-11-22 18:23:45',
    status: 'processing',
    statusText: '处理中',
    items: [
      { id: 1, name: '鱼香肉丝', price: 26.00, quantity: 1 },
      { id: 2, name: '麻婆豆腐', price: 18.00, quantity: 1 }
    ],
    total: 44.00
  },
  {
    id: 3,
    orderNo: 'JSCY202511210003',
    date: '2025-11-21 19:56:12',
    status: 'completed',
    statusText: '已完成',
    items: [
      { id: 1, name: '回锅肉', price: 32.00, quantity: 1 },
      { id: 2, name: '米饭', price: 2.00, quantity: 1 }
    ],
    total: 34.00
  }
])

const filteredOrders = computed(() => {
  if (selectedFilter.value === 'all') {
    return orders.value
  }
  return orders.value.filter(order => order.status === selectedFilter.value)
})

const payOrder = (order) => {
  uni.showToast({
    title: `支付订单 ${order.orderNo}`,
    icon: 'none'
  })
}

const reviewOrder = (order) => {
  uni.showToast({
    title: `评价订单 ${order.orderNo}`,
    icon: 'none'
  })
}
</script>

<style scoped>
.orders-container {
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

.orders-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.order-item {
  background-color: #fff;
  border-radius: 8px;
  padding: 15px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 15px;
}

.order-info {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.order-no {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.order-date {
  font-size: 12px;
  color: #999;
}

.order-status {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  color: #fff;
}

.order-status.pending {
  background-color: #FFB74D;
}

.order-status.processing {
  background-color: #2196F3;
}

.order-status.completed {
  background-color: #81C784;
}

.order-details {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.order-item-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
}

.item-name {
  color: #333;
}

.item-price {
  color: #666;
  font-weight: 500;
}

.order-total {
  display: flex;
  justify-content: flex-end;
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-bottom: 15px;
}

.order-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.action-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
}

.action-btn.primary {
  background-color: #FF6B35;
  color: #fff;
}
</style>