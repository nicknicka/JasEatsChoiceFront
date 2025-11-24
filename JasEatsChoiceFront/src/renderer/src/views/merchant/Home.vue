<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';

const router = useRouter();

// 页面跳转
const navigateToOrders = () => {
  router.push('/merchant/orders');
};

const navigateToMenu = () => {
  router.push('/merchant/menu');
};

const navigateToMessages = () => {
  router.push('/merchant/messages');
};

// 商家信息
const merchantInfo = ref({
  name: 'XX餐厅',
  rating: '4.8/5.0',
  phone: '138XXXX8888',
  email: 'xx@jaseats.com',
  address: '北京市朝阳区XX路123号'
});

// 营业概览
const businessOverview = ref({
  sales: 1234.00,
  orders: 28,
  newComments: 5,
  unreadMessages: 3
});

// 今日订单
const todayOrders = ref([
  { orderId: 'JD20241121001', items: 3, amount: 78.00, time: '2024-11-21 12:30', status: '制作中' },
  { orderId: 'JD20241121002', items: 2, amount: 45.00, time: '2024-11-21 12:45', status: '待配送' }
]);

// 页面加载
onMounted(() => {
  ElMessage.success('欢迎进入商家中心');
});
</script>

<template>
  <div class="merchant-home-container">
    <div class="merchant-content">
      <!-- 商家信息 -->
      <div class="merchant-info-card">
        <div class="info-header">
          <div class="avatar-section">
            <span class="avatar">📸</span>
            <!-- <el-button type="primary" size="small" class="edit-btn">🔧 编辑资料</el-button> -->
          </div>
          <div class="detail-section">
            <div class="merchant-name">🏪 {{ merchantInfo.name }}</div>
            <div class="merchant-rating">🌟 {{ merchantInfo.rating }}</div>
            <div class="contact-info">
              <span class="phone">📞 {{ merchantInfo.phone }}</span>
              <span class="email">📧 {{ merchantInfo.email }}</span>
              <span class="address">📍 {{ merchantInfo.address }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 今日营业概览 -->
      <div class="overview-card">
        <h3 class="card-title">📈 今日营业概览：</h3>
        <div class="overview-grid">
          <div class="overview-item">
            <span class="overview-label">💰 营业额：</span>
            <span class="overview-value">¥{{ businessOverview.sales.toFixed(0) }}</span>
          </div>
          <div class="overview-item">
            <span class="overview-label">🍽️ 订单数：</span>
            <span class="overview-value">{{ businessOverview.orders }}</span>
          </div>
          <div class="overview-item">
            <span class="overview-label">🌟 新增评价：</span>
            <span class="overview-value">{{ businessOverview.newComments }}</span>
          </div>
          <div class="overview-item">
            <span class="overview-label">📞 未读消息：</span>
            <span class="overview-value">{{ businessOverview.unreadMessages }}</span>
          </div>
        </div>
      </div>

      <!-- 订单中心 -->
      <div class="orders-card">
        <div class="orders-header">
          <h3 class="card-title">📋 订单中心</h3>
          <div class="filter-section">
            <el-tag type="primary" effect="plain" class="filter-tag active">今日订单</el-tag>
            <el-tag type="primary" effect="plain" class="filter-tag">本周订单</el-tag>
            <el-tag type="primary" effect="plain" class="filter-tag">本月订单</el-tag>
            <el-tag type="primary" effect="plain" class="filter-tag">全部订单</el-tag>
          </div>
        </div>

        <div class="orders-list">
          <div class="order-item" v-for="order in todayOrders" :key="order.orderId">
            <div class="order-info">
              <div class="order-no">🍽️ 订单号：{{ order.orderId }}</div>
              <div class="order-details">
                <span class="items-count">🍴 {{ order.items }} 菜品</span>
                <span class="amount">¥{{ order.amount.toFixed(2) }}</span>
                <span class="time">⏱️ {{ order.time }}</span>
                <el-tag :type="order.status === '制作中' ? 'info' : 'warning'">{{ order.status }}</el-tag>
              </div>
            </div>
            <div class="order-actions">
              <el-button type="primary" size="small">🔍 详情</el-button>
              <el-button type="success" size="small">⏱️ 更新状态</el-button>
              <el-button type="warning" size="small">🔔 通知用户</el-button>
            </div>
          </div>
        </div>

        <div class="view-all">
          <el-button type="text" @click="navigateToOrders">📤 查看全部订单</el-button>
        </div>
      </div>

      <!-- 快捷操作 -->
      <div class="quick-actions-card">
        <h3 class="card-title">🎯 快捷操作：</h3>
        <div class="actions-grid">
          <div class="action-item" @click="navigateToMenu">
            <div class="action-icon">➕</div>
            <div class="action-label">新增菜品</div>
          </div>
          <div class="action-item">
            <div class="action-icon">💰</div>
            <div class="action-label">设置优惠</div>
          </div>
          <div class="action-item">
            <div class="action-icon">⏱️</div>
            <div class="action-label">调整营业时间</div>
          </div>
          <div class="action-item">
            <div class="action-icon">📞</div>
            <div class="action-label">联系客服</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="less">
.merchant-home-container {
  padding: 0 20px 20px 20px;

  .merchant-info-card {
    margin-bottom: 24px;

    .info-header {
      display: flex;
      align-items: center;
      gap: 20px;

      .avatar-section {
        .avatar {
          font-size: 64px;
        }
        .edit-btn {
          margin-top: 10px;
        }
      }

      .detail-section {
        flex: 1;

        .merchant-name {
          font-size: 20px;
          font-weight: 600;
          margin-bottom: 8px;
        }

        .merchant-rating {
          margin-bottom: 8px;
        }

        .contact-info {
          display: flex;
          flex-wrap: wrap;
          gap: 20px;
          font-size: 14px;
          color: #606266;
        }
      }
    }
  }

  .overview-card {
    margin-bottom: 24px;

    .card-title {
      font-size: 18px;
      font-weight: 600;
      margin-bottom: 16px;
    }

    .overview-grid {
      display: flex;
      flex-wrap: wrap;
      gap: 30px;

      .overview-item {
        display: flex;
        align-items: center;
        gap: 8px;

        .overview-value {
          font-size: 20px;
          font-weight: 600;
        }
      }
    }
  }

  .orders-card {
    margin-bottom: 24px;

    .orders-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;

      .card-title {
        font-size: 18px;
        font-weight: 600;
        margin: 0;
      }

      .filter-section {
        .filter-tag {
          margin-right: 10px;

          &.active {
            color: #409EFF;
            background-color: rgba(64, 158, 255, 0.1);
          }
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

        .order-info {
          .order-no {
            font-weight: 600;
            margin-bottom: 8px;
          }

          .order-details {
            display: flex;
            flex-wrap: wrap;
            gap: 16px;
            font-size: 14px;

            .amount {
              font-weight: 600;
            }
          }
        }

        .order-actions {
          display: flex;
          gap: 8px;
          flex-wrap: wrap;
        }
      }
    }

    .view-all {
      text-align: right;
      margin-top: 12px;
    }
  }

  .quick-actions-card {
    margin-bottom: 24px;

    .card-title {
      font-size: 18px;
      font-weight: 600;
      margin-bottom: 16px;
    }

    .actions-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
      gap: 20px;

      .action-item {
        display: flex;
        flex-direction: column;
        align-items: center;
        padding: 24px;
        border: 1px solid #e4e7ed;
        border-radius: 4px;
        cursor: pointer;
        transition: all 0.3s;

        &:hover {
          box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
        }

        .action-icon {
          font-size: 48px;
          margin-bottom: 8px;
        }

        .action-label {
          font-size: 14px;
          font-weight: 500;
        }
      }
    }
  }
}
</style>