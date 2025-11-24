<template>
  <view class="message-container">
    <view class="filter-bar">
      <button class="filter-btn" :class="{ active: selectedFilter === 'all' }" @click="selectedFilter = 'all'">全部</button>
      <button class="filter-btn" :class="{ active: selectedFilter === 'system' }" @click="selectedFilter = 'system'">系统通知</button>
      <button class="filter-btn" :class="{ active: selectedFilter === 'chat' }" @click="selectedFilter = 'chat'">商家聊天</button>
    </view>

    <view class="message-list">
      <view class="message-item" v-for="msg in filteredMessages" :key="msg.id">
        <view class="message-avatar">
          {{ msg.type === 'system' ? '📢' : '👨🍳' }}
        </view>
        <view class="message-content">
          <view class="message-header">
            <view class="message-title">{{ msg.title }}</view>
            <view class="message-time">{{ msg.time }}</view>
          </view>
          <view class="message-preview">{{ msg.content }}</view>
          <view class="unread-badge" v-if="msg.unread">
            {{ msg.unread }}
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'

const selectedFilter = ref('all')

const messages = ref([
  {
    id: 1,
    title: '系统通知',
    content: '新的活动已经开始，快来参加吧！',
    time: '12:34',
    type: 'system',
    unread: 1
  },
  {
    id: 2,
    title: '商家消息',
    content: '您的订单已经开始处理，请耐心等待。',
    time: '10:23',
    type: 'chat',
    unread: 0
  },
  {
    id: 3,
    title: '系统通知',
    content: '您的账户已经升级为会员。',
    time: '昨天',
    type: 'system',
    unread: 0
  },
  {
    id: 4,
    title: '商家消息',
    content: '您的评价已经收到，感谢您的反馈！',
    time: '2025-11-21',
    type: 'chat',
    unread: 2
  }
])

const filteredMessages = computed(() => {
  if (selectedFilter.value === 'all') {
    return messages.value
  }
  return messages.value.filter(msg => msg.type === selectedFilter.value)
})
</script>

<style scoped>
.message-container {
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

.message-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.message-item {
  background-color: #fff;
  border-radius: 8px;
  padding: 15px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  display: flex;
  gap: 15px;
  align-items: flex-start;
}

.message-avatar {
  font-size: 30px;
  padding: 10px;
  background-color: #f0f0f0;
  border-radius: 50%;
  flex-shrink: 0;
}

.message-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
  position: relative;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.message-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.message-time {
  font-size: 12px;
  color: #999;
}

.message-preview {
  font-size: 14px;
  color: #666;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.unread-badge {
  position: absolute;
  top: 0;
  right: 0;
  background-color: #FF6B35;
  color: #fff;
  font-size: 12px;
  font-weight: bold;
  padding: 2px 6px;
  border-radius: 10px;
  min-width: 24px;
  text-align: center;
}
</style>