<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

// 用户订单数据
const orders = ref([
  {
    id: 1,
    orderNo: 'JD20231123001',
    status: 'delivered',
    merchant: '健康轻食馆',
    total: 28.8,
    time: '2023-11-23 12:30',
    items: ['健康轻食套餐', '矿泉水']
  },
  {
    id: 2,
    orderNo: 'JD20231123002',
    status: 'processing',
    merchant: '营养早餐店',
    total: 15.5,
    time: '2023-11-23 10:15',
    items: ['营养早餐组合']
  },
  {
    id: 3,
    orderNo: 'JD20231122001',
    status: 'completed',
    merchant: '美食天地',
    total: 42.0,
    time: '2023-11-22 18:45',
    items: ['宫保鸡丁', '麻婆豆腐', '米饭']
  },
  {
    id: 4,
    orderNo: 'JD20231121001',
    status: 'cancelled',
    merchant: '健身餐厅',
    total: 35.0,
    time: '2023-11-21 19:30',
    items: ['健身餐套餐']
  }
]);

// 订单状态筛选
const activeStatus = ref('all');

// 订单状态映射
const orderStatusMap = {
  'all': '全部订单',
  'processing': '处理中',
  'delivered': '已送达',
  'completed': '已完成',
  'cancelled': '已取消'
};

// 查看订单详情
const viewOrderDetails = (order) => {
  // 实际应用中可以导航到订单详情页
  console.log('查看订单详情:', order);
};

// 取消订单
const cancelOrder = (order) => {
  // 实际应用中实现取消订单功能
  console.log('取消订单:', order);
  order.status = 'cancelled';
};
</script>

<template>
  <div class="orders-container">
    <h2>查看订单</h2>

    <!-- 订单筛选 -->
    <div class="order-filters">
      <el-button
        v-for="status in ['all', 'processing', 'delivered', 'completed', 'cancelled']"
        :key="status"
        type="primary"
        :plain="activeStatus !== status"
        @click="activeStatus = status"
        size="small"
      >
        {{ orderStatusMap[status] }}
      </el-button>
    </div>

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
            <div class="order-merchant">商家: {{ order.merchant }}</div>
            <div class="order-time">时间: {{ order.time }}</div>
          </div>
          <div class="order-status">
            <el-tag
              :type="order.status === 'processing' ? 'warning' : order.status === 'delivered' ? 'success' : order.status === 'completed' ? 'info' : 'danger'"
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
            v-if="order.status === 'processing'"
            type="danger"
            size="small"
            @click="cancelOrder(order)"
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
.orders-container {
  padding: 0 20px 20px 20px;

  h2 {
    font-size: 24px;
    margin: 0 0 20px 0;
  }

  .order-filters {
    display: flex;
    gap: 10px;
    margin-bottom: 20px;
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
        .order-merchant,
        .order-time {
          font-size: 14px;
          color: #606266;
          margin-bottom: 3px;
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
    }
  }
}
</style>
