<script setup>
import { ref, onMounted ,onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';

const router = useRouter();

// 页面跳转
const navigateToOrders = () => {
  router.push('/merchant/home/orders');
};

// 查看订单详情
const viewOrderDetails = (order) => {
  // 跳转到订单详情页面
  router.push(`/merchant/home/orders/details?orderId=${order.orderId}`);
};

// 更新订单状态
const updateOrderStatus = (order) => {
  // 模拟状态更新逻辑
  const statusFlow = {
    '待处理': '制作中',
    '制作中': '待配送',
    '待配送': '已完成',
    '已完成': '已完成'
  };

  const nextStatus = statusFlow[order.status] || '已完成';
  order.status = nextStatus;

  // 发送WebSocket通知或API请求

  ElMessage.success(`订单 ${order.orderId} 状态已更新为 ${nextStatus}`);
};

// 通知用户
const notifyUser = (order) => {
  // 模拟通知用户逻辑
  // 这里可以通过WebSocket或推送服务发送通知

  ElMessage.success(`已通知用户订单 ${order.orderId} 的最新状态`);
};

// 概览项导航
const navigateToStatistics = () => {
  router.push('/merchant/home/statistics');
};

const navigateToComments = () => {
  router.push('/merchant/home/comments');
};

const navigateToMessages = () => {
  router.push('/merchant/home/messages');
};

// 筛选功能
const activeFilter = ref('today');

// 所有订单数据
const allOrders = ref([
  { orderId: 'JD20241121001', items: 3, amount: 78.00, time: '2024-11-21 12:30', status: '制作中' },
  { orderId: 'JD20241121002', items: 2, amount: 45.00, time: '2024-11-21 12:45', status: '待配送' },
  { orderId: 'JD20241121003', items: 1, amount: 62.00, time: '2024-11-21 10:40', status: '已完成' },
  { orderId: 'JD20241121004', items: 5, amount: 128.00, time: '2024-11-21 11:00', status: '制作中' },
  { orderId: 'JD20241121005', items: 4, amount: 96.00, time: '2024-11-21 11:15', status: '待配送' },
  { orderId: 'JD20241121006', items: 2, amount: 58.50, time: '2024-11-21 11:30', status: '制作中' },
  { orderId: 'JD20241121007', items: 6, amount: 156.00, time: '2024-11-21 10:25', status: '已完成' },
  { orderId: 'JD20241121008', items: 1, amount: 32.80, time: '2024-11-21 11:45', status: '制作中' },
  { orderId: 'JD20241121009', items: 3, amount: 89.00, time: '2024-11-21 12:00', status: '待配送' },
  { orderId: 'JD20241120005', items: 1, amount: 28.00, time: '2024-11-20 20:15', status: '已完成' },
  { orderId: 'JD20241119003', items: 4, amount: 98.00, time: '2024-11-19 18:30', status: '已完成' },
  { orderId: 'JD20241115010', items: 2, amount: 55.00, time: '2024-11-15 13:20', status: '已完成' }
]);

// 筛选后的订单
const filteredOrders = ref([...allOrders.value]);

// 筛选订单
const filterOrders = (filterType) => {
  activeFilter.value = filterType;

  // 简单的筛选逻辑，根据实际时间处理
  switch (filterType) {
    case 'today':
      filteredOrders.value = allOrders.value.filter(order => order.time.startsWith('2024-11-21'));
      break;
    case 'week':
      filteredOrders.value = allOrders.value.filter(order =>
        order.time.startsWith('2024-11-18') || // 模拟本周
        order.time.startsWith('2024-11-19') ||
        order.time.startsWith('2024-11-20') ||
        order.time.startsWith('2024-11-21')
      );
      break;
    case 'month':
      filteredOrders.value = allOrders.value.filter(order => order.time.startsWith('2024-11'));
      break;
    case 'all':
      filteredOrders.value = [...allOrders.value];
      break;
  }
};

const navigateToMenu = () => {
  router.push('/merchant/home/menu');
};

// 快捷操作函数 - 设置优惠
const setDiscount = () => {
  ElMessage.info('设置优惠功能已触发');
  // 可以在此处添加具体的实现逻辑
};

// 快捷操作函数 - 调整营业时间
const adjustBusinessHours = () => {
  ElMessage.info('调整营业时间功能已触发');
  // 可以在此处添加具体的实现逻辑
};

// 快捷操作函数 - 联系客服
const contactCustomerService = () => {
  ElMessage.info('联系客服功能已触发');
  // 可以在此处添加具体的实现逻辑
};

// 菜单状态映射
const menuStatusMap = {
  online: { text: '上架中', icon: '🟢', type: 'success' },
  draft: { text: '草稿', icon: '🟡', type: 'warning' },
  offline: { text: '下架中', icon: '🔴', type: 'danger' }
};

// 今日菜单数据
const todayMenus = ref([
  {
    id: 1,
    name: '午餐菜单',
    dishes: 12,
    status: 'online',
    updateTime: '2024-11-21 10:00',
    autoOnline: '2024-11-22 11:00',
    autoOffline: '2024-11-22 14:00'
  },
  {
    id: 4,
    name: '今日特色菜单',
    dishes: 5,
    status: 'online',
    updateTime: '2024-11-21 09:00',
    autoOnline: '',
    autoOffline: ''
  }
]);

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


// // 页面加载
// onMounted(() => {
//   ElMessage.success('欢迎进入商家中心');
// });

// onUnmounted(() => {
//   ElMessage.success('欢迎下次再来');
// });
</script>

<template>
  <div class="merchant-home-container" v-if="$route.path === '/merchant/home'">
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
          <div class="overview-item" @click="navigateToStatistics">
            <span class="overview-label">💰 营业额：</span>
            <span class="overview-value">¥{{ businessOverview.sales.toFixed(0) }}</span>
          </div>
          <div class="overview-item" @click="navigateToOrders">
            <span class="overview-label">🍽️ 订单数：</span>
            <span class="overview-value">{{ businessOverview.orders }}</span>
          </div>
          <div class="overview-item" @click="navigateToComments">
            <span class="overview-label">🌟 新增评价：</span>
            <span class="overview-value">{{ businessOverview.newComments }}</span>
          </div>
          <div class="overview-item" @click="navigateToMessages">
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
            <el-tag
              type="primary"
              effect="plain"
              class="filter-tag"
              :class="{ active: activeFilter === 'today' }"
              @click="filterOrders('today')"
            >今日订单</el-tag>
            <el-tag
              type="primary"
              effect="plain"
              class="filter-tag"
              :class="{ active: activeFilter === 'week' }"
              @click="filterOrders('week')"
            >本周订单</el-tag>
            <el-tag
              type="primary"
              effect="plain"
              class="filter-tag"
              :class="{ active: activeFilter === 'month' }"
              @click="filterOrders('month')"
            >本月订单</el-tag>
            <el-tag
              type="primary"
              effect="plain"
              class="filter-tag"
              :class="{ active: activeFilter === 'all' }"
              @click="filterOrders('all')"
            >全部订单</el-tag>
          </div>
        </div>

        <div class="orders-list">
          <div class="order-item" v-for="order in filteredOrders" :key="order.orderId">
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
              <el-button type="primary" size="small" @click="viewOrderDetails(order)">🔍 详情</el-button>
              <el-button type="success" size="small" @click="updateOrderStatus(order)">⏱️ 更新状态</el-button>
              <el-button type="warning" size="small" @click="notifyUser(order)">🔔 通知用户</el-button>
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
          <div class="action-item" @click="setDiscount">
            <div class="action-icon">💰</div>
            <div class="action-label">设置优惠</div>
          </div>
          <div class="action-item" @click="adjustBusinessHours">
            <div class="action-icon">⏱️</div>
            <div class="action-label">调整营业时间</div>
          </div>
          <div class="action-item" @click="contactCustomerService">
            <div class="action-icon">📞</div>
            <div class="action-label">联系客服</div>
          </div>
        </div>
      </div>
      <!-- 今日菜单 -->
      <div class="today-menu-card">
        <div class="menu-header">
          <h3 class="card-title">🍽️ 今日菜单</h3>
          <div class="view-all">
            <el-button type="text" @click="navigateToMenu">📤 管理全部菜单</el-button>
          </div>
        </div>

        <div class="menu-list">
          <div class="menu-item" v-for="menu in todayMenus" :key="menu.id">
            <div class="menu-info">
              <div class="menu-name">
                <span class="name">{{ menu.name }}</span>
                <el-tag :type="menuStatusMap[menu.status].type">
                  {{ menuStatusMap[menu.status].icon }} {{ menuStatusMap[menu.status].text }}
                </el-tag>
              </div>

              <div class="menu-stats">
                <span class="dishes-count">🍴 {{ menu.dishes }} 菜品</span>
                <span class="update-time">⏰ 更新时间：{{ menu.updateTime }}</span>
              </div>

              <div class="auto-times" v-if="menu.autoOnline || menu.autoOffline">
                <span v-if="menu.autoOnline" class="auto-online">
                  ⏰ 自动上架：{{ menu.autoOnline }}
                </span>
                <span v-if="menu.autoOffline" class="auto-offline">
                  ⏰ 自动下架：{{ menu.autoOffline }}
                </span>
              </div>
            </div>
          </div>
        </div>

        <!-- 空数据提示 -->
        <el-empty v-if="todayMenus.length === 0" description="暂无今日菜单"></el-empty>
      </div>
    </div>
  </div>
</template>

<style scoped lang="less">
.merchant-home-container {
  padding: 0 20px 20px 20px;

  .merchant-info-card {
    margin-bottom: 24px;
    padding: 24px; /* 添加内边距 */
    border: 2px solid #67c23a; /* 添加绿色边框 */
    border-radius: 8px; /* 圆角边框 */
    background-color: #ffffff; /* 白色背景 */
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05); /* 添加阴影效果 */

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
    padding: 24px; /* 添加内边距 */
    border: 2px solid #e6a23c; /* 添加橙色边框 */
    border-radius: 8px; /* 圆角边框 */
    background-color: #ffffff; /* 白色背景 */
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05); /* 添加阴影效果 */

    .card-title {
      font-size: 18px;
      font-weight: 600;
      margin-bottom: 16px;
    }

    .overview-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 20px;
      row-gap: 20px;

      .overview-item {
        display: flex;
        flex-direction: column;
        align-items: flex-start;
        gap: 4px;
        cursor: pointer;
        padding: 12px;
        border-radius: 8px;
        transition: background-color 0.3s;

        &:hover {
          background-color: #f8fafc;
        }

        .overview-value {
          font-size: 20px;
          font-weight: 600;
        }
      }
    }
  }

  .orders-card {
    margin-bottom: 24px;
    padding: 24px; /* 添加内边距 */
    border: 2px solid #409EFF; /* 加强边框 */
    border-radius: 8px; /* 圆角边框 */
    background-color: #ffffff; /* 白色背景 */
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05); /* 添加阴影效果 */

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
          cursor: pointer; // 添加鼠标悬浮点击样式

          &.active {
            color: #409EFF;
            background-color: rgba(64, 158, 255, 0.1);
          }
        }
      }
    }

    .orders-list {
      max-height: 400px;
      overflow-y: auto;
      padding-right: 8px;

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
    padding: 24px; /* 添加内边距 */
    border: 2px solid #f56c6c; /* 添加红色边框 */
    border-radius: 8px; /* 圆角边框 */
    background-color: #ffffff; /* 白色背景 */
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05); /* 添加阴影效果 */

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

    // 今日菜单
    .today-menu-card {
      margin-bottom: 24px;
      padding: 24px; /* 添加内边距 */
      border: 2px solid #909399; /* 灰色边框 */
      border-radius: 8px; /* 圆角边框 */
      background-color: #ffffff; /* 白色背景 */
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05); /* 添加阴影效果 */

      .menu-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 20px;

        .card-title {
          font-size: 18px;
          font-weight: 600;
          margin: 0;
        }
      }

      .menu-list {
        margin-bottom: 20px;

        .menu-item {
          padding: 16px;
          border: 1px solid #e4e7ed;
          border-radius: 4px;
          margin-bottom: 12px;
          background-color: #fff;
          transition: box-shadow 0.3s;

          &:hover {
            box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
          }

          .menu-info {
            .menu-name {
              display: flex;
              align-items: center;
              gap: 10px;
              margin-bottom: 12px;

              .name {
                font-size: 16px;
                font-weight: 600;
              }
            }

            .menu-stats, .auto-times {
              display: flex;
              flex-wrap: wrap;
              gap: 24px;
              margin-bottom: 8px;
              font-size: 14px;

              .dishes-count {
                color: #606266;
              }
            }

            .auto-times {
              font-size: 13px;
              color: #909399;
            }
          }
        }
      }

      .view-all {
        text-align: right;
        margin-top: 12px;
      }
    }
  }
}
</style>