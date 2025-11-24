<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';

// 消息分类映射
const messageCategories = {
  all: '所有消息',
  system: '系统通知',
  order: '订单消息',
  comment: '评价消息'
};

// 模拟消息数据
const messages = ref([
  {
    id: 1,
    type: 'order',
    sender: '系统',
    title: '新订单提醒：订单号JD20241121001',
    content: '您有一个新的订单需要处理，订单号：JD20241121001',
    time: '2024-11-21 14:30',
    isRead: false
  },
  {
    id: 2,
    type: 'system',
    sender: '系统',
    title: '系统通知：新功能上线',
    content: '商家后台已新增菜单批量导入功能，欢迎使用！',
    time: '2024-11-21 14:15',
    isRead: false
  },
  {
    id: 3,
    type: 'comment',
    sender: '系统',
    title: '评价消息：用户给了五星好评',
    content: '用户张三对您的餐厅给出了五星好评，感谢您的服务！',
    time: '2024-11-21 13:45',
    isRead: true
  },
  {
    id: 4,
    type: 'order',
    sender: '系统',
    title: '订单已完成：订单号JD20241120005',
    content: '订单JD20241120005已完成配送，感谢您的服务！',
    time: '2024-11-21 10:30',
    isRead: true
  }
]);

const selectedMessage = ref(null);
const activeCategory = ref('all');

// 筛选后的消息
const filteredMessages = ref([]);
filteredMessages.value = [...messages.value];

// 未读消息统计
const unreadCounts = ref({
  system: 0,
  order: 0,
  comment: 0
});

// 计算未读消息数量
const calculateUnreadCounts = () => {
  unreadCounts.value = {
    system: messages.value.filter(msg => msg.type === 'system' && !msg.isRead).length,
    order: messages.value.filter(msg => msg.type === 'order' && !msg.isRead).length,
    comment: messages.value.filter(msg => msg.type === 'comment' && !msg.isRead).length
  };
};

// 更新筛选
const updateFilter = () => {
  filteredMessages.value = messages.value.filter(message => {
    return activeCategory.value === 'all' || message.type === activeCategory.value;
  });
  calculateUnreadCounts(); // 更新未读消息统计
};

// 页面加载时初始化
onMounted(() => {
  // 可以在这里加载实际数据
  calculateUnreadCounts(); // 初始化未读消息统计
});

// 查看消息详情
const viewMessageDetail = (message) => {
  selectedMessage.value = message;
  // 自动标记为已读
  if (!message.isRead) {
    message.isRead = true;
    ElMessage.success('消息已标记为已读');
    updateFilter(); // 刷新筛选后的列表以更新状态
  }
};

// 返回消息列表
const backToList = () => {
  selectedMessage.value = null;
};

// 标记为已读
const markAsRead = (message) => {
  message.isRead = true;
  ElMessage.success('消息已标记为已读');
  updateFilter();
};

// 全部标记为已读
const markAllAsRead = () => {
  filteredMessages.value.forEach(message => {
    message.isRead = true;
  });
  ElMessage.success('所有消息已标记为已读');
  updateFilter();
};
</script>

<template>
  <div class="messages-management-container">
    <div class="messages-header">
      <div class="header-left">
        <h3 class="page-title">【消息中心】</h3>
        <el-button type="text" class="back-btn" v-if="!selectedMessage">↩ 返回</el-button>
      </div>
      <div class="header-right" v-if="!selectedMessage">
        <el-button type="success" @click="markAllAsRead">
          全部标记为已读
        </el-button>
      </div>
    </div>

    <div class="messages-content">
      <!-- 消息分类与列表 -->
      <div class="messages-list-container" v-if="!selectedMessage">
        <div class="category-section">
          <span class="category-label">📋 消息分类：</span>
          <el-tag
            v-for="category in ['all', 'system', 'order', 'comment']"
            :key="category"
            :type="activeCategory === category ? 'primary' : 'info'"
            effect="plain"
            @click="activeCategory = category; updateFilter()"
            class="category-tag"
          >
            {{ messageCategories[category] }}
            <el-badge v-if="category !== 'all' && unreadCounts[category]" :value="unreadCounts[category]" type="danger" />
          </el-tag>
        </div>

        <div class="messages-list">
          <div
            v-for="message in filteredMessages"
            :key="message.id"
            class="message-item"
            :class="{ 'unread-message': !message.isRead }"
            @click="viewMessageDetail(message)"
          >
            <div class="message-icon">🔔</div>
            <div class="message-content">
              <div class="message-title">{{ message.title }}</div>
              <div class="message-meta">
                <span class="message-time">{{ message.time }}</span>
                <el-tag :type="message.isRead ? 'success' : 'warning'">
                  {{ message.isRead ? '🔘 已读' : '✅ 未读' }}
                </el-tag>
              </div>
            </div>
          </div>
        </div>

        <!-- 空数据提示 -->
        <div v-if="filteredMessages.length === 0" class="empty-messages">
          <el-empty description="暂无消息"></el-empty>
        </div>
      </div>

      <!-- 消息详情 -->
      <div class="message-detail-container" v-if="selectedMessage">
        <div class="detail-header">
          <div class="detail-title">
            <h3>{{ selectedMessage.title }}</h3>
            <el-tag :type="selectedMessage.isRead ? 'success' : 'warning'">
              {{ selectedMessage.isRead ? '已读' : '未读' }}
            </el-tag>
          </div>
          <div class="detail-meta">
            <div class="sender-info">
              <span class="sender-label">发送者:</span>
              <span>{{ selectedMessage.sender }}</span>
            </div>
            <div class="time-info">
              <span class="time-label">时间:</span>
              <span>{{ selectedMessage.time }}</span>
            </div>
          </div>
        </div>
        <div class="detail-content">
          {{ selectedMessage.content }}
        </div>
        <div class="detail-actions">
          <el-button @click="backToList">返回列表</el-button>
          <el-button v-if="!selectedMessage.isRead" type="success" @click="markAsRead(selectedMessage)">
            标记为已读
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="less">
.messages-management-container {
  padding: 0 20px 20px 20px;

  .messages-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    .page-title {
      font-size: 18px;
      font-weight: 600;
      margin: 0;
    }
  }

  .messages-content {
    .messages-list-container {
      .category-section {
        margin-bottom: 24px;
        display: flex;
        align-items: center;
        gap: 12px;

        .category-label {
          font-weight: 500;
        }

        .category-tag {
          cursor: pointer;

          &:hover {
            opacity: 0.8;
          }
        }
      }

      .messages-list {
        .message-item {
          display: flex;
          align-items: flex-start;
          padding: 16px;
          border: 1px solid #e4e7ed;
          border-radius: 4px;
          margin-bottom: 12px;
          background-color: #fff;
          cursor: pointer;
          transition: box-shadow 0.3s;

          &:hover {
            box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
          }

          &.unread-message {
            border-left: 4px solid #409EFF;
            background-color: rgba(64, 158, 255, 0.05);
          }

          .message-icon {
            font-size: 24px;
            margin: 4px 16px 0 0;
          }

          .message-content {
            flex: 1;

            .message-title {
              font-size: 14px;
              font-weight: 500;
              margin-bottom: 8px;
            }

            .message-meta {
              display: flex;
              justify-content: space-between;
              align-items: center;
              font-size: 12px;
              color: #909399;

              .message-time {
                flex: 1;
              }
            }
          }
        }
      }

      .empty-messages {
        text-align: center;
        margin-top: 50px;
      }
    }

    .message-detail-container {
      padding: 0;

      .detail-header {
        margin-bottom: 20px;

        .detail-title {
          display: flex;
          align-items: center;
          gap: 12px;
          margin-bottom: 15px;

          h3 {
            font-size: 20px;
            margin: 0;
          }
        }

        .detail-meta {
          display: flex;
          flex-direction: column;
          gap: 10px;
          font-size: 14px;

          .sender-info, .time-info {
            display: flex;
            align-items: center;
            gap: 8px;
          }

          .sender-label, .time-label {
            color: #909399;
            font-weight: 500;
          }
        }
      }

      .detail-content {
        font-size: 16px;
        line-height: 1.8;
        margin-bottom: 30px;
        padding: 20px;
        background-color: #f5f7fa;
        border-radius: 4px;
      }

      .detail-actions {
        display: flex;
        justify-content: flex-end;
        gap: 10px;
      }
    }
  }
}
</style>
