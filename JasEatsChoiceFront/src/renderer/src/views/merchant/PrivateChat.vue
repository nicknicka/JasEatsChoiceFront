
<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';

// 模拟对话列表
const conversationList = ref([
  {
    id: 1,
    userId: 1,
    userName: '小明',
    userAvatar: 'https://picsum.photos/id/1/40/40',
    lastMessage: '这个麻辣香锅饭太好吃了！',
    time: '2024-11-21 14:30',
    unreadCount: 1
  },
  {
    id: 2,
    userId: 2,
    userName: '小红',
    userAvatar: 'https://picsum.photos/id/2/40/40',
    lastMessage: '我想取消订单',
    time: '2024-11-21 14:15',
    unreadCount: 0
  },
  {
    id: 3,
    userId: 3,
    userName: '小刚',
    userAvatar: 'https://picsum.photos/id/3/40/40',
    lastMessage: '什么时候能配送？',
    time: '2024-11-21 13:45',
    unreadCount: 0
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

// 当前选中的对话
const selectedConversation = ref(null);

// 新消息内容
const newMessage = ref('');

// 页面加载
onMounted(() => {
  // 默认选中第一个对话
  if (conversationList.value.length > 0) {
    selectedConversation.value = conversationList.value[0];
  }
});

// 选择对话
const selectConversation = (conversation) => {
  selectedConversation.value = conversation;
  // 清空未读消息
  if (conversation.unreadCount > 0) {
    conversation.unreadCount = 0;
    ElMessage.success('消息已标记为已读');
  }
};

// 发送消息
const sendMessage = () => {
  if (!newMessage.value.trim() || !selectedConversation.value) {
    return;
  }

  // 创建新消息
  const message = {
    id: Date.now(),
    sender: 'merchant',
    content: newMessage.value.trim(),
    time: new Date().toISOString().slice(0, 19).replace('T', ' '),
    isRead: true
  };

  // 添加到聊天记录
  chatMessages.value.push(message);

  // 更新对话列表的最后一条消息
  selectedConversation.value.lastMessage = message.content;
  selectedConversation.value.time = message.time;

  // 清空输入框
  newMessage.value = '';
};
</script>

<template>
  <div class="private-chat-container">
    <div class="chat-header">
      <h3 class="page-title">【商家单聊】</h3>
    </div>

    <div class="chat-content">
      <!-- 对话列表 -->
      <div class="conversation-list">
        <div
          v-for="conversation in conversationList"
          :key="conversation.id"
          class="conversation-item"
          :class="{ active: selectedConversation?.id === conversation.id }"
          @click="selectConversation(conversation)"
        >
          <div class="user-avatar">
            <img :src="conversation.userAvatar" :alt="conversation.userName" />
          </div>
          <div class="conversation-info">
            <div class="user-name-time">
              <span class="user-name">{{ conversation.userName }}</span>
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

      <!-- 聊天内容 -->
      <div class="chat-area" v-if="selectedConversation">
        <div class="chat-header">
          <div class="user-info">
            <div class="user-avatar">
              <img :src="selectedConversation.userAvatar" :alt="selectedConversation.userName" />
            </div>
            <span class="user-name">{{ selectedConversation.userName }}</span>
          </div>
        </div>

        <div class="messages-container">
          <div
            v-for="message in chatMessages"
            :key="message.id"
            class="message-item"
            :class="{ 'user-message': message.sender === 'user', 'merchant-message': message.sender === 'merchant' }"
          >
            <div class="message-content">
              {{ message.content }}
              <div class="message-time">{{ message.time }}</div>
            </div>
          </div>
        </div>

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
.private-chat-container {
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

        .user-avatar {
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

          .user-name-time {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 4px;

            .user-name {
              font-weight: 500;
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

      .chat-header {
        padding: 12px;
        border-bottom: 1px solid #e4e7ed;
        .user-info {
          display: flex;
          align-items: center;

          .user-avatar {
            margin-right: 8px;
            img {
              width: 32px;
              height: 32px;
              border-radius: 50%;
              object-fit: cover;
            }
          }

          .user-name {
            font-weight: 500;
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

          &.user-message {
            align-self: flex-start;

            .message-content {
              background-color: #fff;
              border: 1px solid #ddd;
              border-radius: 10px;
              padding: 10px;

              .message-time {
                text-align: right;
                font-size: 12px;
                color: #909399;
                margin-top: 4px;
              }
            }
          }

          &.merchant-message {
            align-self: flex-end;

            .message-content {
              background-color: #67c23a;
              color: #fff;
              border-radius: 10px;
              padding: 10px;

              .message-time {
                text-align: right;
                font-size: 12px;
                opacity: 0.8;
                margin-top: 4px;
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
