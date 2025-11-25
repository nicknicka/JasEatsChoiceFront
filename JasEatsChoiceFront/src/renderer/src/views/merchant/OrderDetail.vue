<script setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';

const route = useRoute();
const orderId = ref(null);
// 模拟订单详情数据
const orderDetail = ref({
  id: 1,
  orderNo: 'JD20241121001',
  status: 'pending',
  user: '小明',
  phone: '138XXXX8888',
  address: '公司地址',
  total: 78.00,
  time: '2024-11-21 10:30',
  items: [
    { name: '宫保鸡丁', price: 28, quantity: 1 },
    { name: '麻婆豆腐', price: 18, quantity: 1 },
    { name: '米饭', price: 2, quantity: 2 }
  ]
});

// 订单状态映射
const orderStatusMap = {
  'pending': { text: '待处理', icon: '🔴', type: 'danger' },
  'preparing': { text: '准备中', icon: '🟡', type: 'warning' },
  'completed': { text: '已完成', icon: '✅', type: 'success' }
};

// 页面加载时获取订单详情
onMounted(() => {
  // 实际应用中可以根据orderId从API获取真实数据
  orderId.value = route.params.id;
});
</script>

<template>
  <div class="order-detail-container">
    <div class="order-detail-header">
      <h3 class="page-title">【订单详情】</h3>
      <el-button type="text" class="back-btn" @click="$router.back()">↩ 返回</el-button>
    </div>

    <!-- 订单基本信息 -->
    <div class="order-info-section">
      <div class="info-item">
        <label class="info-label">订单号：</label>
        <span class="info-value">{{ orderDetail.orderNo }}</span>
      </div>
      <div class="info-item">
        <label class="info-label">订单状态：</label>
        <el-tag :type="orderStatusMap[orderDetail.status].type">
          {{ orderStatusMap[orderDetail.status].icon }} {{ orderStatusMap[orderDetail.status].text }}
        </el-tag>
      </div>
      <div class="info-item">
        <label class="info-label">下单时间：</label>
        <span class="info-value">{{ orderDetail.time }}</span>
      </div>
      <div class="info-item">
        <label class="info-label">顾客姓名：</label>
        <span class="info-value">{{ orderDetail.user }}</span>
      </div>
      <div class="info-item">
        <label class="info-label">联系电话：</label>
        <span class="info-value">{{ orderDetail.phone }}</span>
      </div>
      <div class="info-item">
        <label class="info-label">配送地址：</label>
        <span class="info-value">{{ orderDetail.address }}</span>
      </div>
    </div>

    <!-- 订单商品 -->
    <div class="order-items-section">
      <h4 class="section-title">📦 订单商品</h4>
      <div class="items-table">
        <div class="table-header">
          <div class="table-col name-col">商品名称</div>
          <div class="table-col price-col">单价</div>
          <div class="table-col quantity-col">数量</div>
          <div class="table-col total-col">小计</div>
        </div>
        <div class="table-body">
          <div v-for="(item, index) in orderDetail.items" :key="index" class="table-row">
            <div class="table-col name-col">{{ item.name }}</div>
            <div class="table-col price-col">¥{{ item.price.toFixed(2) }}</div>
            <div class="table-col quantity-col">{{ item.quantity }}</div>
            <div class="table-col total-col">¥{{ (item.price * item.quantity).toFixed(2) }}</div>
          </div>
        </div>
      </div>
      <div class="order-total">
        <div class="total-label">订单总计：</div>
        <div class="total-value">¥{{ orderDetail.total.toFixed(2) }}</div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="less">
.order-detail-container {
  padding: 0 20px 20px 20px;

  .order-detail-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;

    .page-title {
      font-size: 18px;
      font-weight: 600;
      margin: 0;
    }
  }

  .order-info-section {
    background-color: #fff;
    border-radius: 8px;
    padding: 20px;
    margin-bottom: 24px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);

    .info-item {
      display: flex;
      align-items: center;
      margin-bottom: 12px;
      gap: 12px;

      &:last-child {
        margin-bottom: 0;
      }

      .info-label {
        width: 100px;
        font-weight: 500;
        color: #303133;
      }

      .info-value {
        flex: 1;
        color: #606266;
      }
    }
  }

  .order-items-section {
    background-color: #fff;
    border-radius: 8px;
    padding: 20px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);

    .section-title {
      font-size: 16px;
      font-weight: 600;
      margin-bottom: 20px;
    }

    .items-table {
      border: 1px solid #ebeef5;
      border-radius: 4px;
      margin-bottom: 20px;
      overflow: hidden;

      .table-header {
        display: flex;
        background-color: #f5f7fa;
        padding: 12px;
        font-weight: 500;
        border-bottom: 1px solid #ebeef5;
      }

      .table-body {
        .table-row {
          display: flex;
          padding: 12px;
          border-bottom: 1px solid #ebeef5;

          &:last-child {
            border-bottom: none;
          }
        }
      }

      .table-col {
        &.name-col {
          flex: 1;
        }

        &.price-col,
        &.quantity-col,
        &.total-col {
          width: 100px;
          text-align: center;
        }
      }
    }

    .order-total {
      display: flex;
      justify-content: flex-end;
      align-items: center;
      font-size: 18px;
      font-weight: 600;

      .total-label {
        margin-right: 12px;
      }

      .total-value {
        color: #e6a23c;
      }
    }
  }
}
</style>
