<script setup>
import { ref } from 'vue';
import { ChatRound } from '@element-plus/icons-vue';

// Chat messages
const messages = ref([
  {
    id: 1,
    sender: 'ai',
    content: '您好！我是您的AI饮食助手。有什么可以帮您的吗？',
    time: '10:30',
    avatar: '🤖'
  }
]);

// User input
const inputMessage = ref('');

// Loading state
const isLoading = ref(false);


// Send message to AI
const sendMessage = () => {
  if (!inputMessage.value.trim()) return;

  // Add user message
  const userMsg = {
    id: messages.value.length + 1,
    sender: 'user',
    content: inputMessage.value,
    time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    avatar: '👤'
  };
  messages.value.push(userMsg);
  const userInput = inputMessage.value;
  inputMessage.value = '';

  // Simulate AI response
  isLoading.value = true;

  // Mock AI response
  setTimeout(() => {
    const aiResponse = {
      id: messages.value.length + 1,
      sender: 'ai',
      content: `我已经收到您的问题："${userInput}"。这是一个模拟的AI回复，实际应用中将连接后端API获取智能饮食建议。`,
      time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      avatar: '🤖'
    };
    messages.value.push(aiResponse);
    isLoading.value = false;

    // Scroll to bottom of chat
    setTimeout(() => {
      const chatContainer = document.querySelector('.chat-messages');
      if (chatContainer) {
        chatContainer.scrollTop = chatContainer.scrollHeight;
      }
    }, 100);
  }, 1000);
};
</script>

<template>
  <div class="app-container">

    <div class="main-content">
      <!-- Right Content Area -->
      <el-main class="content-area">
        <div class="ai-chat-container">
          <div class="chat-header">
            <h2>AI饮食助手</h2>
            <div class="chat-info">
              <el-tag type="success">在线</el-tag>
            </div>
          </div>

          <div class="chat-messages">
            <div
              v-for="message in messages"
              :key="message.id"
              class="chat-message"
              :class="{ 'user-message': message.sender === 'user', 'ai-message': message.sender === 'ai' }"
            >
              <div class="message-avatar">{{ message.avatar }}</div>
              <div class="message-content">
                <div class="message-text">{{ message.content }}</div>
                <div class="message-time">{{ message.time }}</div>
              </div>
            </div>

            <div v-if="isLoading" class="chat-message ai-message loading">
              <div class="message-avatar">🤖</div>
              <div class="message-content">
                <el-skeleton :rows="2" style="width: 200px"></el-skeleton>
              </div>
            </div>
          </div>

          <div class="chat-input-area">
            <el-input
              v-model="inputMessage"
              placeholder="请输入您的问题...（例如：推荐适合减肥的食谱）"
              clearable
              resize="none"
              :rows="2"
              type="textarea"
              @keyup.enter="sendMessage"
            ></el-input>
            <el-button
              type="primary"
              size="large"
              class="send-btn"
              @click="sendMessage"
              :disabled="isLoading"
            >
              <el-icon><ChatRound /></el-icon>
              发送
            </el-button>
          </div>

          <div class="quick-questions">
            <el-divider>快速提问</el-divider>
            <div class="quick-question-grid">
              <el-button type="text" @click="inputMessage = '推荐适合糖尿病患者的食谱'">糖尿病患者食谱</el-button>
              <el-button type="text" @click="inputMessage = '减肥期间能吃什么？'">减肥期间饮食</el-button>
              <el-button type="text" @click="inputMessage = '高血压患者的饮食注意事项'">高血压饮食</el-button>
              <el-button type="text" @click="inputMessage = '健身后怎么补充营养？'">健身后营养</el-button>
            </div>
          </div>
        </div>
      </el-main>
    </div>
  </div>
</template>

<style scoped lang="less">
.app-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.top-nav-bar {
  background-color: #fff;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.logo {
  font-size: 24px;
  font-weight: bold;
  color: #FF6B6B;
}

.search-input {
  width: 400px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
}

.main-content {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.sidebar-menu {
  background-color: #f0f2f5;
  border-right: 1px solid #e6e8eb;
  padding: 20px 0;
  display: flex;
  flex-direction: column;

  .avatar-section {
    text-align: center;
    padding-bottom: 20px;
    border-bottom: 1px solid #e6e8eb;
    margin-bottom: 20px;
  }

  .menu-list {
    border: none;
    flex: 1;
  }

  .setting-menu {
    border-top: 1px solid #e6e8eb;
    margin-top: auto;
    width: 100%;
  }
}

.content-area {
  padding: 20px;
  background-color: #fafafa;
  overflow-y: hidden;
}

.ai-chat-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  max-width: 900px;
  margin: 0 auto;

  .chat-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h2 {
      font-size: 24px;
      font-weight: bold;
      margin: 0;
    }

    .chat-info {
      display: flex;
      gap: 10px;
    }
  }

  .chat-messages {
    flex: 1;
    overflow-y: auto;
    background-color: #fff;
    border-radius: 8px;
    padding: 20px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
    margin-bottom: 20px;

    .chat-message {
      display: flex;
      gap: 15px;
      margin-bottom: 20px;

      &.user-message {
        flex-direction: row-reverse;
        justify-content: flex-start;

        .message-content {
          align-items: flex-end;

          .message-text {
            background-color: #67c23a;
            color: #fff;
            border-radius: 18px 18px 0 18px;
          }
        }
      }

      &.ai-message {
        flex-direction: row;
        justify-content: flex-start;

        .message-content {
          align-items: flex-start;

          .message-text {
            background-color: #ecf5ff;
            color: #409eff;
            border-radius: 18px 18px 18px 0;
          }
        }
      }

      &.loading {
        .message-text {
          background-color: #f5f7fa;
        }
      }

      .message-avatar {
        font-size: 40px;
        flex-shrink: 0;
      }

      .message-content {
        display: flex;
        flex-direction: column;
        gap: 5px;

        .message-text {
          max-width: 70%;
          padding: 12px 16px;
          border-radius: 18px;
          line-height: 1.5;
        }

        .message-time {
          font-size: 12px;
          color: #909399;
        }
      }
    }
  }

  .chat-input-area {
    display: flex;
    gap: 10px;
    margin-bottom: 20px;

    .el-input {
      flex: 1;

      textarea {
        min-height: 80px;
      }
    }

    .send-btn {
      align-self: flex-end;
      background-color: #FF6B6B;
      border: none;

      &:hover {
        background-color: #ff5252;
      }
    }
  }

  .quick-questions {
    .quick-question-grid {
      display: flex;
      flex-wrap: wrap;
      gap: 10px;

      .el-button {
        border-radius: 20px;
        border: 1px solid #dcdfe6;
        color: #606266;
      }
    }
  }
}
</style>
