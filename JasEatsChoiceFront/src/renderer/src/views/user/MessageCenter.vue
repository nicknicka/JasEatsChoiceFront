<script setup>
import { ref } from 'vue';

// 消息中心数据
const messages = ref([
  {
    id: 1,
    type: 'order',
    title: '您的订单已送达',
    content: '订单号：JD20231123001 已送达，请注意查收。',
    time: '2023-11-23 12:30',
    read: false
  },
  {
    id: 2,
    type: 'system',
    title: '系统更新通知',
    content: '佳食宜选系统已完成更新，新增多项功能，欢迎体验。',
    time: '2023-11-22 18:00',
    read: true
  },
  {
    id: 3,
    type: 'promotion',
    title: '双11优惠活动',
    content: '双11期间，所有订单享受8折优惠，快来下单吧！',
    time: '2023-11-20 10:00',
    read: true
  },
  {
    id: 4,
    type: 'order',
    title: '您的订单已接单',
    content: '商家已接单，正在为您准备美食。',
    time: '2023-11-23 11:00',
    read: false
  }
]);

// 切换消息分类
const activeTab = ref('all');

// 查看消息详情
const viewMessage = (message) => {
  message.read = true;
  // 实际应用中可以导航到消息详情页
  console.log('查看消息:', message);
};

// 批量删除消息
const deleteSelected = () => {
  // 实际应用中实现批量删除功能
  console.log('批量删除消息');
};
</script>

<template>
  <div class="message-center-container">
    <h2>消息中心</h2>

    <!-- 消息分类标签页 -->
    <el-tabs v-model:active-name="activeTab" class="message-tabs">
      <el-tab-pane label="全部消息" name="all"></el-tab-pane>
      <el-tab-pane label="订单消息" name="order"></el-tab-pane>
      <el-tab-pane label="系统通知" name="system"></el-tab-pane>
      <el-tab-pane label="优惠活动" name="promotion"></el-tab-pane>
    </el-tabs>

    <!-- 消息列表 -->
    <div class="message-list">
      <el-card
        v-for="message in messages"
        :key="message.id"
        class="message-card"
        :class="{ 'unread': !message.read }"
      >
        <div class="message-header">
          <div class="message-type">
            <el-tag
              :type="message.type === 'order' ? 'primary' : message.type === 'system' ? 'warning' : 'success'"
              size="small"
            >
              {{ message.type === 'order' ? '订单消息' : message.type === 'system' ? '系统通知' : '优惠活动' }}
            </el-tag>
          </div>
          <div class="message-time">{{ message.time }}</div>
        </div>

        <div class="message-content">
          <h3 class="message-title">{{ message.title }}</h3>
          <p class="message-text">{{ message.content }}</p>
        </div>

        <div class="message-actions">
          <el-button
            type="text"
            size="small"
            @click="viewMessage(message)"
          >
            查看详情
          </el-button>
          <el-button type="text" size="small" danger>
            删除
          </el-button>
        </div>
      </el-card>
    </div>

    <!-- 空数据提示 -->
    <el-empty
      v-if="messages.length === 0"
      description="暂无消息"
    ></el-empty>
  </div>
</template>

<style scoped lang="less">
.message-center-container {
  padding: 0 20px 20px 20px;

  h2 {
    font-size: 24px;
    margin: 0 0 20px 0;
  }

  .message-tabs {
    margin-bottom: 20px;
  }

  .message-list {
    display: flex;
    flex-direction: column;
    gap: 15px;
  }

  .message-card {
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }

    &.unread {
      border-left: 4px solid #409eff;
    }

    .message-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 10px;

      .message-time {
        font-size: 14px;
        color: #909399;
      }
    }

    .message-content {
      margin-bottom: 15px;

      .message-title {
        font-size: 16px;
        font-weight: bold;
        margin-bottom: 5px;
        margin: 0;
      }

      .message-text {
        color: #606266;
        font-size: 14px;
        margin: 0;
      }
    }

    .message-actions {
      text-align: right;
    }
  }
}
</style>
