<script setup>
import { ref } from 'vue';

// 商家订单数据
const orders = ref([
  {
    id: 1,
    orderNo: 'JD20231123001',
    status: 'delivered',
    user: '张三',
    total: 28.8,
    time: '2023-11-23 12:30',
    items: ['健康轻食套餐', '矿泉水'],
    address: '北京市朝阳区建国路88号'
  },
  {
    id: 2,
    orderNo: 'JD20231123002',
    status: 'pending',
    user: '李四',
    total: 15.5,
    time: '2023-11-23 10:15',
    items: ['营养早餐组合'],
    address: '北京市海淀区中关村大街1号'
  },
  {
    id: 3,
    orderNo: 'JD20231122001',
    status: 'processing',
    user: '王五',
    total: 42.0,
    time: '2023-11-22 18:45',
    items: ['宫保鸡丁', '麻婆豆腐', '米饭'],
    address: '北京市东城区王府井大街100号'
  },
  {
    id: 4,
    orderNo: 'JD20231121001',
    status: 'completed',
    user: '赵六',
    total: 35.0,
    time: '2023-11-21 19:30',
    items: ['健身餐套餐'],
    address: '北京市西城区西单北大街180号'
  }
]);

// 订单状态映射
const orderStatusMap = {
  'pending': '待接单',
  'processing': '处理中',
  'delivered': '已送达',
  'completed': '已完成',
  'cancelled': '已取消'
};

// 处理订单
const processOrder = (order, newStatus) => {
  // 实际应用中实现订单状态变更功能
  console.log('处理订单:', order, newStatus);
  order.status = newStatus;
};

// 查看订单详情
const viewOrderDetails = (order) => {
  // 实际应用中可以导航到订单详情页
  console.log('查看订单详情:', order);
};
</script>

<template>
  <div class="merchant-orders-container">
    <h2>商家订单管理</h2>

    <!-- 订单列表 -->
    <div class="order-list">
      <el-card
        v-for="order in orders"
        :key="order.id"
        class="order-card"
      >
        <div class="order-header">
          <div class="order-info">
            <div class="order-no">订单号: {{ order.orderNo }}</div>
            <div class="order-user">用户: {{ order.user }}</div>
            <div class="order-time">时间: {{ order.time }}</div>
            <div class="order-address">地址: {{ order.address }}</div>
          </div>
          <div class="order-status">
            <el-tag
              :type="order.status === 'pending' ? 'warning' : order.status === 'processing' ? 'info' : order.status === 'delivered' ? 'success' : 'danger'"
            >
              {{ orderStatusMap[order.status] }}
            </el-tag>
          </div>
        </div>

        <div class="order-items">
          <div class="item-title">商品:</div>
          <div class="item-list">
            <el-tag v-for="item in order.items" :key="item" type="info" size="small">{{ item }}</el-tag>
          </div>
        </div>

        <div class="order-total">
          <div class="total-text">总金额:</div>
          <div class="total-amount">¥{{ order.total.toFixed(2) }}</div>
        </div>

        <div class="order-actions">
          <el-button
            type="primary"
            size="small"
            @click="viewOrderDetails(order)"
          >
            查看详情
          </el-button>

          <el-button
            v-if="order.status === 'pending'"
            type="success"
            size="small"
            @click="processOrder(order, 'processing')"
          >
            接单
          </el-button>

          <el-button
            v-if="order.status === 'processing'"
            type="success"
            size="small"
            @click="processOrder(order, 'delivered')"
          >
            已送达
          </el-button>

          <el-button
            v-if="order.status === 'delivered'"
            type="success"
            size="small"
            @click="processOrder(order, 'completed')"
          >
            完成订单
          </el-button>

          <el-button
            v-if="order.status === 'pending' || order.status === 'processing'"
            type="danger"
            size="small"
            @click="processOrder(order, 'cancelled')"
          >
            取消订单
          </el-button>
        </div>
      </el-card>
    </div>

    <!-- 空数据提示 -->
    <el-empty
      v-if="orders.length === 0"
      description="暂无订单"
    ></el-empty>
  </div>
</template>

<style scoped lang="less">
.merchant-orders-container {
  padding: 0 20px 20px 20px;

  h2 {
    font-size: 24px;
    margin: 0 0 20px 0;
  }

  .order-list {
    display: flex;
    flex-direction: column;
    gap: 15px;
  }

  .order-card {
    .order-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 15px;

      .order-info {
        .order-no {
          font-weight: bold;
          margin-bottom: 5px;
        }
        .order-user,
        .order-time,
        .order-address {
          font-size: 14px;
          color: #606266;
          margin-bottom: 3px;
        }

        .order-address {
          max-width: 400px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }
    }

    .order-items {
      margin-bottom: 15px;

      .item-title {
        font-weight: bold;
        margin-bottom: 10px;
      }

      .item-list {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;
      }
    }

    .order-total {
      display: flex;
      justify-content: flex-end;
      align-items: center;
      margin-bottom: 15px;

      .total-text {
        margin-right: 10px;
        color: #606266;
      }

      .total-amount {
        font-size: 18px;
        font-weight: bold;
        color: #ff6b6b;
      }
    }

    .order-actions {
      display: flex;
      justify-content: flex-end;
      gap: 10px;
      flex-wrap: wrap;
    }
  }
}
</style>
