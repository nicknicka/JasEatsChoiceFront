
<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import router from '../../router/index.js';
import api from '../../utils/api.js';
import { decodeJwt } from '../../utils/api.js';

// 合并的会话列表（包含单聊和群聊）
const conversations = ref([]);

// 页面加载
onMounted(() => {
  // 从JWT令牌中获取用户ID
  const token = localStorage.getItem('token');
  let userId = '1'; // 默认值

  if (token) {
    const decodedToken = decodeJwt(token);
    if (decodedToken && decodedToken.userId) {
      userId = decodedToken.userId;
    }
  } else {
    // 无法获取用户ID，弹出提示框要求重新登录
    ElMessageBox.alert('无法获取用户ID，请重新登录', '身份验证失败', {
      confirmButtonText: '重新登录',
      type: 'error',
      closeOnClickModal: false,
      closeOnPressEscape: false,
    })
    .then(() => {
      // 用户点击重新登录按钮，清除本地存储并跳转到登录页面
      localStorage.removeItem('token');
      localStorage.removeItem('currentRole');
      router.push('/login');
    })
    .catch(() => {
      // 点击取消按钮的处理，也可以跳转到登录页面
      localStorage.removeItem('token');
      localStorage.removeItem('currentRole');
      router.push('/login');
    });
  }

  // 从后端API获取会话列表
  api.get(`/v1/users/${userId}/chat-sessions`)
    .then(response => {
      if (response.data && response.data.success) {
        // 转换后端返回的数据格式以匹配前端期望的字段
        const formattedConversations = response.data.data.map(session => {
          // 根据最后一条消息判断是单聊还是群聊
          const isGroupChat = session.msgType === 'group';

          return {
            id: isGroupChat ? session.toId : session.fromId === userId ? session.toId : session.fromId,
            type: isGroupChat ? 'group' : 'private',
            name: isGroupChat ? session.toId : `用户${session.fromId === userId ? session.toId : session.fromId}`,
            avatar: isGroupChat ? '👥' : '👤',
            lastMessage: session.content,
            time: session.createTime,
            unreadCount: 0,
            memberCount: isGroupChat ? Math.floor(Math.random() * 50) + 10 : undefined,
            userId: isGroupChat ? undefined : (session.fromId === userId ? session.toId : session.fromId)
          };
        });

        // 将会话按最后消息时间排序（从最新到最旧）
        formattedConversations.sort((a, b) => {
          return new Date(b.time) - new Date(a.time);
        });

        conversations.value = formattedConversations;

        // 默认选中第一个会话
        if (conversations.value.length > 0) {
          selectedConversation.value = conversations.value[0];
        }
      }
    })
    .catch(error => {
      console.error('加载会话列表失败:', error);
      ElMessage.error('加载会话列表失败，请稍后重试');

      // 如果后端请求失败，使用默认模拟数据
      conversations.value = [
        {
          id: 1,
          type: 'private',
          name: '小明',
          avatar: '👤',
          lastMessage: '这个麻辣香锅饭太好吃了！',
          time: '2024-11-21 14:30',
          unreadCount: 1,
          userId: 1
        },
        {
          id: 3,
          type: 'group',
          name: '商家交流群',
          avatar: '👥',
          lastMessage: '大家最近生意怎么样？',
          time: '2024-11-21 14:30',
          unreadCount: 2,
          memberCount: 25
        }
      ];

      // 默认选中第一个会话
      if (conversations.value.length > 0) {
        selectedConversation.value = conversations.value[0];
      }
    });
});

// 聊天记录
const chatMessages = ref([]);

// 当前选中的会话
const selectedConversation = ref(null);

// 新消息内容
const newMessage = ref('');

// 同步至群聊开关
const syncToGroup = ref(false);

// 选择会话
const selectConversation = (conversation) => {
  selectedConversation.value = conversation;
  // 清空未读消息
  if (conversation.unreadCount > 0) {
    conversation.unreadCount = 0;
    ElMessage.success('消息已标记为已读');
  }

  // 从JWT令牌中获取用户ID
  const token = localStorage.getItem('token');
  let userId = '1'; // 默认值

  if (token) {
    const decodedToken = decodeJwt(token);
    if (decodedToken && decodedToken.userId) {
      userId = decodedToken.userId;
    }
  }

  // 构建会话ID
  let sessionId = '';
  if (conversation.type === 'group') {
    // 群聊会话ID就是群ID
    sessionId = conversation.id;
  } else {
    // 单聊会话ID格式：fromId_toId
    // 确保会话ID唯一，按字典序排列
    const ids = [userId, conversation.id];
    ids.sort();
    sessionId = ids.join('_');
  }

  // 从后端API获取聊天记录
  api.get(`/v1/chat/${sessionId}/messages`)
    .then(response => {
      if (response.data && response.data.success) {
        // 转换后端返回的数据格式以匹配前端期望的字段
        const formattedMessages = response.data.data.records.map(message => ({
          id: message.id,
          sender: message.fromId === userId ? (conversation.type === 'private' ? 'merchant' : '我') : message.fromId,
          content: message.content,
          time: message.createTime,
          isRead: message.readStatus
        }));

        chatMessages.value = formattedMessages;
      }
    })
    .catch(error => {
      console.error('加载聊天记录失败:', error);

      // 如果后端请求失败，使用默认模拟数据
      chatMessages.value = [
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
      ];
    });
};

// 发送消息
const sendMessage = () => {
  if (!newMessage.value.trim() || !selectedConversation.value) {
    return;
  }

  // 从JWT令牌中获取用户ID
  const token = localStorage.getItem('token');
  let fromId = '1'; // 默认值

  if (token) {
    const decodedToken = decodeJwt(token);
    if (decodedToken && decodedToken.userId) {
      fromId = decodedToken.userId;
    }
  }

  // 构建消息对象
  const messageData = {
    fromId: fromId,
    toId: selectedConversation.value.id,
    content: newMessage.value.trim(),
    msgType: selectedConversation.value.type === 'group' ? 'group' : 'private'
  };

  // 发送消息到后端API
  api.post('/api/v1/chat/messages', messageData)
    .then(response => {
      if (response.data && response.data.success) {
        // 创建新消息对象
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

        // 将当前会话移到最前面
        const index = conversations.value.indexOf(selectedConversation.value);
        if (index > -1) {
          conversations.value.splice(index, 1);
          conversations.value.unshift(selectedConversation.value);
        }

        // 同步消息到所有群聊
        if (syncToGroup.value && selectedConversation.value.type === 'private') {
          const syncMessageContent = `【订单同步】${message.content}`;

          // 更新所有群聊的最后消息
          conversations.value.forEach(conversation => {
            if (conversation.type === 'group') {
              conversation.lastMessage = syncMessageContent;
              conversation.time = message.time;
              conversation.unreadCount++;

              // 将群聊会话移到前面
              const groupIndex = conversations.value.indexOf(conversation);
              if (groupIndex > -1) {
                conversations.value.splice(groupIndex, 1);
                conversations.value.unshift(conversation);
              }
            }
          });

          // 重置同步开关
          syncToGroup.value = false;

          // 提示用户消息已同步
          ElMessage.info('消息已同步至所有群聊');
        }

        // 清空输入框
        newMessage.value = '';
        ElMessage.success('消息发送成功');
      }
    })
    .catch(error => {
      console.error('发送消息失败:', error);
      ElMessage.error('发送消息失败，请稍后重试');
    });
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

        <!-- 会话列表空数据提示 -->
        <div v-if="conversations.length === 0" class="empty-conversations">
          <el-empty description="暂无会话"></el-empty>
        </div>
      </div>

      <!-- 右侧聊天内容或空提示 -->
      <div v-if="selectedConversation" class="chat-area">
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

          <!-- 空数据提示 -->
          <div v-if="chatMessages.length === 0" class="empty-chat">
            <el-empty description="暂无聊天记录"></el-empty>
          </div>
        </div>

        <!-- 消息输入框 -->
        <div class="message-input-container">
          <div style="width: 100%; margin-bottom: 8px;">
            <el-checkbox v-model="syncToGroup" style="font-size: 12px;">同步至群聊</el-checkbox>
          </div>
          <div style="display: flex; gap: 12px;">
            <el-input
              v-model="newMessage"
              type="textarea"
              placeholder="输入消息内容..."
              :rows="2"
              @keyup.enter="sendMessage"
              style="flex: 1;"
            />
            <el-button type="primary" @click="sendMessage">发送</el-button>
          </div>
        </div>
      </div>

      <!-- 未选择会话时的提示 -->
      <div v-else class="chat-area">
        <div class="chat-area-empty">
          <el-empty
            :description="conversations.length === 0 ? '暂无会话' : '请先选择一个会话'"
          ></el-empty>
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
