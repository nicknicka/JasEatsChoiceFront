
<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';

// 合并的会话列表（包含单聊和群聊）
const conversations = ref([
  // 单聊会话
  {
    id: 1,
    type: 'private',
    name: '小明',
    avatar: 'https://picsum.photos/id/1/40/40',
    lastMessage: '这个麻辣香锅饭太好吃了！',
    time: '2024-11-21 14:30',
    unreadCount: 1,
    userId: 1
  },
  {
    id: 2,
    type: 'private',
    name: '小红',
    avatar: 'https://picsum.photos/id/2/40/40',
    lastMessage: '我想取消订单',
    time: '2024-11-21 14:15',
    unreadCount: 0,
    userId: 2
  },
  // 群聊会话
  {
    id: 3,
    type: 'group',
    name: '商家交流群',
    avatar: 'https://picsum.photos/id/100/40/40',
    lastMessage: '大家最近生意怎么样？',
    time: '2024-11-21 14:30',
    unreadCount: 2,
    memberCount: 25
  },
  {
    id: 4,
    type: 'group',
    name: '新品推广群',
    avatar: 'https://picsum.photos/id/101/40/40',
    lastMessage: '新品上线，欢迎大家体验！',
    time: '2024-11-21 14:15',
    unreadCount: 0,
    memberCount: 18
  }
]);

// 模拟聊天记录
const chatMessages = ref([
  {
    id: 1,
    sender: 'user',
    content: '这个麻辣香锅饭太好吃了！',
    time: '2024-11-21 14:30',
    isRead: false
  },
  {
    id: 2,
    sender: 'merchant',
    content: '感谢您的好评！',
    time: '2024-11-21 14:31',
    isRead: true
  }
]);

// 当前选中的会话
const selectedConversation = ref(null);

// 新消息内容
const newMessage = ref('');

// 页面加载
onMounted(() => {
  // 默认选中第一个会话
  if (conversations.value.length > 0) {
    selectedConversation.value = conversations.value[0];
  }
});

// 选择会话
const selectConversation = (conversation) => {
  selectedConversation.value = conversation;
  // 清空未读消息
  if (conversation.unreadCount > 0) {
    conversation.unreadCount = 0;
    ElMessage.success('消息已标记为已读');
  }
  // 这里可以根据会话类型加载对应的聊天记录
};

// 发送消息
const sendMessage = () => {
  if (!newMessage.value.trim() || !selectedConversation.value) {
    return;
  }

  // 创建新消息
  const message = {
    id: Date.now(),
    sender: selectedConversation.value.type === 'private' ? 'merchant' : '我',
    content: newMessage.value.trim(),
    time: new Date().toISOString().slice(0, 19).replace('T', ' '),
    isRead: true
  };

  // 添加到聊天记录
  chatMessages.value.push(message);

  // 更新会话列表的最后一条消息
  selectedConversation.value.lastMessage = message.content;
  selectedConversation.value.time = message.time;

  // 清空输入框
  newMessage.value = '';
};
</script>

<template>
  <div class="chat-container">
    <div class="chat-header">
      <h3 class="page-title">【商家消息】</h3>
    </div>

    <div class="chat-content">
      <!-- 左侧会话列表 -->
      <div class="conversation-list">
        <div
          v-for="conversation in conversations"
          :key="conversation.id"
          class="conversation-item"
          :class="{ active: selectedConversation?.id === conversation.id }"
          @click="selectConversation(conversation)"
        >
          <div class="conversation-avatar">
            <img :src="conversation.avatar" :alt="conversation.name" />
          </div>
          <div class="conversation-info">
            <div class="name-time">
              <span class="name">
                {{ conversation.name }}
                <span v-if="conversation.type === 'group'" class="member-count"> ({{ conversation.memberCount }}人)</span>
              </span>
              <span class="time">{{ conversation.time }}</span>
            </div>
            <div class="last-message">
              {{ conversation.lastMessage }}
            </div>
          </div>
          <div v-if="conversation.unreadCount > 0" class="unread-count">
            {{ conversation.unreadCount }}
          </div>
        </div>
      </div>

      <!-- 右侧聊天内容 -->
      <div class="chat-area" v-if="selectedConversation">
        <!-- 右侧上方：会话名称 -->
        <div class="chat-area-header">
          <div class="conversation-info">
            <div class="conversation-avatar">
              <img :src="selectedConversation.avatar" :alt="selectedConversation.name" />
            </div>
            <div class="name-info">
              <span class="name">{{ selectedConversation.name }}</span>
              <span v-if="selectedConversation.type === 'group'" class="member-count"> ({{ selectedConversation.memberCount }}人)</span>
            </div>
          </div>
        </div>

        <!-- 右侧下方：具体聊天内容 -->
        <div class="messages-container">
          <div
            v-for="message in chatMessages"
            :key="message.id"
            class="message-item"
            :class="{
              'others-message': (selectedConversation.type === 'private' && message.sender !== 'merchant') || (selectedConversation.type === 'group' && message.sender !== '我'),
              'merchant-message': selectedConversation.type === 'private' && message.sender === 'merchant',
              'my-message': selectedConversation.type === 'group' && message.sender === '我'
            }"
          >
            <div v-if="selectedConversation.type === 'group' && message.sender !== '我'" class="message-header">
              <span class="sender-name">{{ message.sender }}</span>
            </div>
            <div class="message-content">
              {{ message.content }}
              <div class="message-time">{{ message.time }}</div>
            </div>
          </div>
        </div>

        <!-- 消息输入框 -->
        <div class="message-input-container">
          <el-input
            v-model="newMessage"
            type="textarea"
            placeholder="输入消息内容..."
            :rows="2"
            @keyup.enter="sendMessage"
          />
          <el-button type="primary" @click="sendMessage">发送</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="less">
.chat-container {
  padding: 0 20px 20px 20px;
  height: calc(100vh - 60px);

  .chat-header {
    margin-bottom: 20px;
    .page-title {
      font-size: 18px;
      font-weight: 600;
      margin: 0;
    }
  }

  .chat-content {
    display: flex;
    gap: 20px;
    height: calc(100vh - 120px);

    .conversation-list {
      width: 300px;
      border: 1px solid #e4e7ed;
      border-radius: 4px;
      overflow-y: auto;

      .conversation-item {
        display: flex;
        align-items: center;
        padding: 12px;
        cursor: pointer;
        border-bottom: 1px solid #e4e7ed;
        transition: background-color 0.3s;

        &:hover {
          background-color: #f5f7fa;
        }

        &.active {
          background-color: #ecf5ff;
        }

        .conversation-avatar {
          margin-right: 12px;
          img {
            width: 48px;
            height: 48px;
            border-radius: 50%;
            object-fit: cover;
          }
        }

        .conversation-info {
          flex: 1;

          .name-time {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 4px;

            .name {
              font-weight: 500;

              .member-count {
                font-size: 12px;
                color: #909399;
              }
            }

            .time {
              font-size: 12px;
              color: #909399;
            }
          }

          .last-message {
            font-size: 14px;
            color: #606266;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
          }
        }

        .unread-count {
          background-color: #f56c6c;
          color: #fff;
          border-radius: 10px;
          padding: 0 6px;
          font-size: 12px;
        }
      }
    }

    .chat-area {
      flex: 1;
      border: 1px solid #e4e7ed;
      border-radius: 4px;
      display: flex;
      flex-direction: column;

      .chat-area-header {
        padding: 12px;
        border-bottom: 1px solid #e4e7ed;
        .conversation-info {
          display: flex;
          align-items: center;

          .conversation-avatar {
            margin-right: 8px;
            img {
              width: 32px;
              height: 32px;
              border-radius: 50%;
              object-fit: cover;
            }
          }

          .name-info {
            .name {
              font-weight: 500;

              .member-count {
                font-size: 12px;
                color: #909399;
              }
            }
          }
        }
      }

      .messages-container {
        flex: 1;
        padding: 16px;
        overflow-y: auto;

        .message-item {
          margin-bottom: 16px;
          max-width: 70%;

          .message-header {
            margin-bottom: 4px;
            .sender-name {
              font-size: 12px;
              color: #666;
            }
          }

          .message-content {
            border-radius: 10px;
            padding: 10px;

            .message-time {
              text-align: right;
              font-size: 12px;
              margin-top: 4px;
            }
          }

          &.others-message {
            align-self: flex-start;

            .message-content {
              background-color: #fff;
              border: 1px solid #ddd;

              .message-time {
                color: #909399;
              }
            }
          }

          &.merchant-message,
          &.my-message {
            align-self: flex-end;

            .message-content {
              background-color: #67c23a;
              color: #fff;

              .message-time {
                opacity: 0.8;
              }
            }
          }
        }
      }

      .message-input-container {
        padding: 12px;
        border-top: 1px solid #e4e7ed;
        display: flex;
        gap: 12px;

        .el-input {
          flex: 1;
        }

        button {
          align-self: flex-end;
        }
      }
    }
  }
}
</style>
