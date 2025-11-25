
<script setup>
import { ref, onMounted, computed } from 'vue';
import { ElMessage } from 'element-plus';

// 用户聊天会话列表
const conversations = ref([
  // 商家单聊会话
  {
    id: 1,
    type: 'merchant',
    name: '佳食餐馆',
    avatar: '🏪',
    lastMessage: '您的订单已准备好，请前往取餐',
    time: '2024-11-21 14:30',
    unreadCount: 1,
    merchantId: 101,
    pinned: false
  },
  {
    id: 2,
    type: 'merchant',
    name: '美味小吃店',
    avatar: '🏪',
    lastMessage: '您点的奶茶已完成',
    time: '2024-11-21 14:15',
    unreadCount: 0,
    merchantId: 102,
    pinned: false
  },
  // 系统消息
  {
    id: 3,
    type: 'system',
    name: '系统通知',
    avatar: '📢',
    lastMessage: '您的账户已成功充值',
    time: '2024-11-21 10:00',
    unreadCount: 0,
    pinned: false
  }
]);

// 模拟聊天记录 - 根据不同会话存储不同的聊天记录
const chatHistory = {
  1: [ // 佳食餐馆的聊天记录
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
  ],
  2: [ // 美味小吃店的聊天记录
    {
      id: 1,
      sender: 'merchant',
      content: '您点的奶茶已完成',
      time: '2024-11-21 14:15',
      isRead: true
    }
  ],
  3: [ // 系统通知的聊天记录
    {
      id: 1,
      sender: 'system',
      content: '您的账户已成功充值',
      time: '2024-11-21 10:00',
      isRead: true
    }
  ]
};

// 当前显示的聊天记录
const chatMessages = ref([]);

// 当前选中的会话
const selectedConversation = ref(null);

// 排序后的会话列表 - 置顶会话在前，然后按时间降序排列
const sortedConversations = computed(() => {
  return [...conversations.value].sort((a, b) => {
    // 置顶会话在前
    if (a.pinned && !b.pinned) return -1;
    if (!a.pinned && b.pinned) return 1;

    // 按时间降序排列
    return new Date(b.time) - new Date(a.time);
  });
});

// 新消息内容
const newMessage = ref('');

// 切换会话置顶状态
const togglePin = (conversation) => {
  conversation.pinned = !conversation.pinned;
  ElMessage.success(conversation.pinned ? '会话已置顶' : '会话已取消置顶');
};

// 页面加载
onMounted(() => {
  // 默认选中第一个会话
  if (sortedConversations.value.length > 0) {
    selectedConversation.value = sortedConversations.value[0];
    // 加载对应的聊天记录
    chatMessages.value = chatHistory[selectedConversation.value.id] || [];
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
  // 根据会话ID加载对应的聊天记录
  chatMessages.value = chatHistory[conversation.id] || [];
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

  // 将消息保存到对应的聊天历史中
  chatHistory[selectedConversation.value.id] = chatMessages.value;

  // 清空输入框
  newMessage.value = '';
};
</script>

<template>
  <div class="chat-container">
    <div class="chat-header">
      <h3 class="page-title">【用户聊天】</h3>
    </div>

    <div class="chat-content">
      <!-- 左侧会话列表 -->
      <div class="conversation-list">
        <div
          v-for="conversation in sortedConversations"
          :key="conversation.id"
          class="conversation-item"
          :class="{ active: selectedConversation?.id === conversation.id, 'pinned-conversation': conversation.pinned }"
          @click="selectConversation(conversation)"
        >
          <div class="conversation-avatar">
            <div v-if="conversation.avatar.match(/^https?:/)">
              <img :src="conversation.avatar" alt="" />
            </div>
            <div v-else class="emoji-avatar">
              {{ conversation.avatar }}
            </div>
            <div v-if="conversation.unreadCount > 0" class="unread-count">
              {{ conversation.unreadCount }}
            </div>
          </div>
          <!-- 置顶按钮 -->
          <div
            class="pin-btn"
            @click.stop="togglePin(conversation)"
            title="{{ conversation.pinned ? '取消置顶' : '置顶会话' }}"
          >
            {{ conversation.pinned ? '📌' : '📌' }}
          </div>
          <div class="conversation-info">
            <div class="name-time">
              <span class="name">
                {{ conversation.name }}
              </span>
              <span class="time">{{ conversation.time }}</span>
            </div>
            <div class="last-message">
              {{ conversation.lastMessage }}
            </div>
          </div>
          
        </div>
      </div>

      <!-- 右侧聊天内容 -->
      <div class="chat-area" v-if="selectedConversation">
        <!-- 右侧上方：会话名称 -->
        <div class="chat-area-header">
          <div class="conversation-info">
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
              'others-message': message.sender === 'merchant' || message.sender === 'system',
              'my-message': message.sender === 'user' || message.sender === '我'
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
      width: 37%; /* 固定宽度 */
      border: 1px solid #e4e7ed;
      border-radius: 4px;
      overflow : hidden;
      white-space: nowrap;
      text-overflow: ellipsis;

      .conversation-item {
        display: flex;
        align-items: center;
        padding: 16px; /* 调整内边距 */
        cursor: pointer;
        border-bottom: 1px solid #e4e7ed;
        transition: background-color 0.3s;
        position: relative; /* 为未读消息红点定位提供参考 */

        &:hover {
          background-color: #f5f7fa;
        }

        &.active {
          background-color: #ecf5ff;
        }

        &.pinned-conversation {
          background-color: #fffbe6; /* 置顶会话背景色 */
          border-left: 3px solid #ffd591; /* 左侧标记条 */
        }

        .pin-btn {
          position: absolute;
          top: 8px;
          right: 8px;
          font-size: 14px;
          cursor: pointer;
          opacity: 0.5;
          transition: opacity 0.2s;

          &:hover {
            opacity: 1;
          }
        }

        .conversation-avatar {
          margin-right: 11px; /* 调整头像右侧间距 */
          position: relative; /* 为未读消息红点定位提供参考 */

          img {
            width: 35px; /* 调整头像大小 */
            height: 35px; /* 调整头像大小 */
            border-radius: 7px;
            object-fit: contain;
            aspect-ratio: 1 / 1; /* 确保长宽比为1:1 */
          }

          .emoji-avatar {
            width: 35px;
            height: 35px;
            border-radius: 7px;
            background-color: #f0f0f0;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 24px; /* 调整emoji大小 */
            text-align: center;
          }
        }

        .conversation-info {
          flex: 1;
          min-width: 0; /* 确保flex元素能正确收缩，让省略号生效 */

          .name-time {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 4px;
            font-size: 14px;

            .name {
              font-weight: 500;
              white-space: nowrap;
              overflow: hidden;
              text-overflow: ellipsis;
              flex: 1; /* 让名称占据剩余空间 */
              margin-right: 8px; /* 与时间保持一定距离 */

              .member-count {
                font-size: 8px;
                color: #909399;
              }
            }

            .time {
              font-size: 8px;
              white-space: nowrap; /* 时间不换行 */
              color: #909399;
            }
          }

          .last-message {
            font-size: 10px;
            color: #606266;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
          }
        }

        .unread-count {
          background-color: #f56c6c;
          // height: auto;
          width: 10px ;
          height: 10px;
          color: #fff;
          border-radius: 50%;
          padding: 2px; /* 减小内边距，使红点更紧凑 */
          font-size: 7px; /* 减小字体大小 */
          position: absolute; /* 绝对定位 */
          top: 0; /* 根据头像大小精确计算位置 */
          right: 0; /* 根据头像大小精确计算位置 */
          transform: translate(50%, -50%); /* 使红点中心对齐到头像右上角 */
          z-index: 1; /* 确保红点在最上层 */
          min-height: 7px; /* 设置最小高度，确保单个数字也能显示为圆形 */
          min-width: 7px; /* 设置最小宽度，确保单个数字也能显示为圆形 */
          text-align: center; /* 文字居中 */
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
              border-radius: 7px;
              object-fit: contain;
              aspect-ratio: 1 / 1; /* 确保长宽比为1:1 */
            }
          }

          .name-info {
            .name {
              font-weight: 500;
              white-space: nowrap;
              overflow: hidden;
              text-overflow: ellipsis;

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
        padding: 11px;
        overflow-y: auto;
        display: flex;
        flex-direction: column;
        

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
            padding: 7px;
            font-size: 12px;

            .message-time {
              text-align: right;
              font-size: 10px;
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
