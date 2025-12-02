
<script setup>
import { ref, onMounted } from 'vue';
import { ChatRound, Camera, Document, Loading } from '@element-plus/icons-vue';
import axios from 'axios';

// 从配置中导入API地址
import { API_CONFIG } from '../../config/index.js';

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

// User input for chat
const inputMessage = ref('');
const inputMaxLength = 500; // Maximum message length for chat

// Loading state for chat
const isLoading = ref(false);

// Tab selection - AI聊天已设置为默认
const activeTab = ref('chat');

// AI Dish Recognition
const recognitionResult = ref(null);
const recognitionLoading = ref(false);
const selectedImage = ref(null);
const imageMaxSize = 10 * 1024 * 1024; // 10MB maximum image size

// AI Recipe Optimization
const originalRecipe = ref('');
const optimizedRecipe = ref(null);
const optimizationLoading = ref(false);
const recipeMinLength = 20; // Minimum recipe length
const recipeMaxLength = 10000; // Maximum recipe length

// Image upload handling
const handleImageUpload = (event) => {
  const file = event.target.files[0];
  if (file) {
    // Validate file type
    if (!file.type.startsWith('image/')) {
      ElMessage.error('请选择图片文件');
      event.target.value = ''; // Clear the input to allow reselect
      return;
    }

    // Validate file size
    if (file.size > imageMaxSize) {
      ElMessage.error('图片大小不能超过10MB');
      event.target.value = ''; // Clear the input to allow reselect
      return;
    }

    selectedImage.value = URL.createObjectURL(file);
    recognitionResult.value = null; // Clear previous result
    ElMessage.success('图片上传成功');
  }
};

// New method to handle image upload click
const handleUploadClick = () => {
  const input = document.getElementById('image-upload');
  if (input) {
    input.click();
  }
};

// Simulate AI dish recognition
const recognizeDish = () => {
  if (!selectedImage.value) {
    return;
  }

  recognitionLoading.value = true;

  // Mock AI recognition
  setTimeout(() => {
    recognitionResult.value = {
      name: '宫保鸡丁',
      ingredients: ['鸡肉', '花生米', '辣椒', '黄瓜', '胡萝卜'],
      calories: 450,
      difficulty: '中等',
      preparationTime: '25分钟',
      tags: ['川菜', '经典', '蛋白质丰富']
    };
    recognitionLoading.value = false;
  }, 1500);
};

// Simulate AI recipe optimization
const optimizeRecipe = () => {
  // Validate recipe content
  const trimmedRecipe = originalRecipe.value.trim();
  if (!trimmedRecipe) {
    ElMessage.warning('请输入食谱');
    return;
  }
  if (trimmedRecipe.length < recipeMinLength) {
    ElMessage.warning(`食谱长度不能少于${recipeMinLength}个字符`);
    return;
  }
  if (trimmedRecipe.length > recipeMaxLength) {
    ElMessage.warning(`食谱长度不能超过${recipeMaxLength}个字符`);
    return;
  }

  optimizationLoading.value = true;

  // Call backend API for recipe optimization
  axios.post(API_CONFIG.baseURL + API_CONFIG.ai.recipe, { foodName: originalRecipe.value })
    .then(response => {
      // Format the backend response into the expected structure
      const backendRecipes = response.data.data;
      // For simplicity, take the first recipe as the optimized result
      if (backendRecipes && backendRecipes.length > 0) {
        const firstRecipe = backendRecipes[0];
        optimizedRecipe.value = {
          original: originalRecipe.value,
          optimized: `推荐食谱：${firstRecipe.name}
难度：${firstRecipe.difficulty}
卡路里：${firstRecipe.calorie}大卡
食材：${firstRecipe.ingredients}
步骤：${firstRecipe.steps}`,
          improvements: ['营养均衡', '口味优化', '步骤简化']
        };
      } else {
        // No recipes returned from backend
        optimizedRecipe.value = {
          original: originalRecipe.value,
          optimized: `优化失败：没有找到合适的优化食谱。`,
          improvements: []
        };
      }
    })
    .catch(error => {
      console.error('食谱优化接口调用失败:', error);
      let errorMsg = `优化失败：无法获取AI优化建议。`;

      // Add more specific error messages
      if (error.response) {
        // Server responded with error status code
        if (error.response.status === 404) {
          errorMsg = '食谱优化服务暂时不可用，请稍后重试。';
        } else if (error.response.status === 500) {
          errorMsg = '服务器内部错误，请稍后重试。';
        }
      } else if (error.request) {
        // No response received from server
        errorMsg = '网络连接超时，请检查网络设置。';
      }

      optimizedRecipe.value = {
        original: originalRecipe.value,
        optimized: errorMsg,
        improvements: []
      };
      ElMessage.error(errorMsg);
    })
    .finally(() => {
      optimizationLoading.value = false;
    });
};

// Send message to AI
const sendMessage = () => {
  // Validate message content
  const trimmedMsg = inputMessage.value.trim();
  if (!trimmedMsg) {
    ElMessage.warning('请输入问题');
    return;
  }
  if (trimmedMsg.length > inputMaxLength) {
    ElMessage.warning(`消息长度不能超过${inputMaxLength}个字符`);
    return;
  }

  // Add user message
  const userMsg = {
    id: messages.value.length + 1,
    sender: 'user',
    content: trimmedMsg,
    time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    avatar: '👤'
  };
  messages.value.push(userMsg);
  const userInput = trimmedMsg;
  inputMessage.value = '';

  // Call backend AI API
  isLoading.value = true;

  // 使用后端API获取AI回复
  axios.post(API_CONFIG.baseURL + API_CONFIG.ai.chat, { message: userInput })
    .then(response => {
      // Check if response is valid
      if (response.data && response.data.data && response.data.data.content) {
        const aiResponse = {
          id: messages.value.length + 1,
          sender: 'ai',
          content: response.data.data.content, // 根据后端返回的结构调整
          time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
          avatar: '🤖'
        };
        messages.value.push(aiResponse);
      } else {
        throw new Error('Invalid response format');
      }
    })
    .catch(error => {
      console.error('AI聊天接口调用失败:', error);
      let errorMsg = '对不起，暂时无法获取AI回复，请稍后重试。';

      // Add more specific error messages
      if (error.response) {
        // Server responded with error status code
        if (error.response.status === 404) {
          errorMsg = 'AI聊天服务暂时不可用，请稍后重试。';
        } else if (error.response.status === 500) {
          errorMsg = '服务器内部错误，请稍后重试。';
        }
      } else if (error.request) {
        // No response received from server
        errorMsg = '网络连接超时，请检查网络设置。';
      }

      const aiResponse = {
        id: messages.value.length + 1,
        sender: 'ai',
        content: errorMsg,
        time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
        avatar: '🤖'
      };
      messages.value.push(aiResponse);
    })
    .finally(() => {
      isLoading.value = false;

      // Scroll to bottom of chat
      setTimeout(() => {
        const chatContainer = document.querySelector('.chat-messages');
        if (chatContainer) {
          chatContainer.scrollTop = chatContainer.scrollHeight;
        }
      }, 100);
    });
};

// Ensure AI聊天 is the default tab on component mount
onMounted(() => {
  activeTab.value = 'chat';
});
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

          <!-- Tab Menu -->
          <el-tabs v-model="activeTab" type="border-card" class="ai-tabs">
            <el-tab-pane label="AI聊天" name="chat" :icon="ChatRound">
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
                >
                  <template #append>
                    <div class="input-counter">{{ inputMessage.trim().length }}/{{ inputMaxLength }}</div>
                  </template>
                </el-input>
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
            </el-tab-pane>

            <el-tab-pane label="菜品识别" name="recognition" :icon="Camera">
              <div class="recognition-section">
                <div class="upload-area">
                  <input
                    type="file"
                    accept="image/*"
                    style="display: none;"
                    id="image-upload"
                    @change="handleImageUpload"
                  />
                  <el-button type="primary" @click="handleUploadClick">
                    <el-icon><Camera /></el-icon>
                    上传菜品图片
                  </el-button>

                  <div v-if="selectedImage" class="image-preview">
                    <img :src="selectedImage" alt="菜品图片" />
                    <el-button type="danger" size="small" @click="selectedImage = null">删除</el-button>
                  </div>
                </div>

                <el-button
                  type="success"
                  class="recognize-btn"
                  @click="recognizeDish"
                  :disabled="!selectedImage || recognitionLoading"
                >
                  <el-icon v-if="recognitionLoading"><Loading /></el-icon>
                  {{ recognitionLoading ? '识别中...' : '识别菜品' }}
                </el-button>

                <div v-if="recognitionResult" class="recognition-result">
                  <h4>识别结果</h4>
                  <div class="result-item"><strong>菜品名称:</strong> {{ recognitionResult.name }}</div>
                  <div class="result-item"><strong>主要食材:</strong> {{ recognitionResult.ingredients.join(', ') }}</div>
                  <div class="result-item"><strong>卡路里:</strong> {{ recognitionResult.calories }} kcal</div>
                  <div class="result-item"><strong>难度:</strong> {{ recognitionResult.difficulty }}</div>
                  <div class="result-item"><strong>烹饪时间:</strong> {{ recognitionResult.preparationTime }}</div>
                  <div class="result-item">
                    <strong>标签:</strong>
                    <el-tag v-for="tag in recognitionResult.tags" :key="tag" size="small">
                      {{ tag }}
                    </el-tag>
                  </div>
                </div>
              </div>
            </el-tab-pane>

            <el-tab-pane label="食谱优化" name="recipe" :icon="Document">
              <div class="recipe-section">
                <div class="recipe-input">
                  <el-input
                    v-model="originalRecipe"
                    placeholder="请输入您的食谱...（例如：西红柿鸡蛋的做法：1. 准备西红柿2个，鸡蛋2个；2. 煎鸡蛋；3. 炒西红柿；4. 混合翻炒）"
                    clearable
                    resize="vertical"
                    :rows="6"
                    type="textarea"
                  >
                    <template #append>
                      <div class="input-counter">{{ originalRecipe.trim().length }}/{{ recipeMaxLength }}</div>
                    </template>
                  </el-input>
                </div>

                <el-button
                  type="success"
                  class="optimize-btn"
                  @click="optimizeRecipe"
                  :disabled="!originalRecipe || optimizationLoading"
                >
                  <el-icon v-if="optimizationLoading"><Loading /></el-icon>
                  {{ optimizationLoading ? '优化中...' : '优化食谱' }}
                </el-button>

                <div v-if="optimizedRecipe" class="recipe-result">
                  <h4>优化结果</h4>

                  <div class="original-recipe">
                    <strong>原食谱:</strong>
                    <pre>{{ optimizedRecipe.original }}</pre>
                  </div>

                  <div class="optimized-recipe">
                    <strong>优化后食谱:</strong>
                    <pre>{{ optimizedRecipe.optimized }}</pre>
                  </div>

                  <div class="improvements">
                    <strong>优化点:</strong>
                    <el-tag v-for="improvement in optimizedRecipe.improvements" :key="improvement" size="small" type="primary">
                      {{ improvement }}
                    </el-tag>
                  </div>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
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
  overflow-y: auto;
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

  .ai-tabs {
    flex: 1;
    display: flex;
    flex-direction: column;

    :deep(.el-tabs__content) {
      flex: 1;
      overflow-y: auto;
      padding: 20px 0;
    }

    :deep(.el-tabs__content-item) {
      height: 100%;
    }
  }

  .chat-messages {
    /* 固定聊天框高度 */
    height: 400px;
    max-height: 400px;
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
    /* 固定发送消息区域在页面底部 */
    position: sticky;
    bottom: 0;
    background-color: #fafafa;
    padding: 10px 0 20px 0;
    z-index: 100;
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

    .input-counter {
      padding: 8px 12px;
      font-size: 12px;
      color: #909399;
      align-self: flex-end;
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

  /* Dish Recognition Section */
  .recognition-section {
    padding: 20px;
    background-color: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);

    .upload-area {
      margin-bottom: 20px;

      .image-preview {
        margin-top: 15px;
        display: flex;
        align-items: center;
        gap: 10px;

        img {
          max-width: 200px;
          max-height: 200px;
          border-radius: 8px;
        }
      }
    }

    .recognize-btn {
      margin-bottom: 20px;
    }

    .recognition-result {
      padding: 20px;
      background-color: #f5f7fa;
      border-radius: 8px;

      h4 {
        margin-top: 0;
        margin-bottom: 20px;
        color: #303133;
      }

      .result-item {
        margin-bottom: 10px;
        color: #606266;
      }
    }
  }

  /* Recipe Optimization Section */
  .recipe-section {
    padding: 20px;
    background-color: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);

    .recipe-input {
      margin-bottom: 20px;
    }

    .optimize-btn {
      margin-bottom: 20px;
    }

    .recipe-result {
      padding: 20px;
      background-color: #f5f7fa;
      border-radius: 8px;

      h4 {
        margin-top: 0;
        margin-bottom: 20px;
        color: #303133;
      }

      .original-recipe,
      .optimized-recipe {
        margin-bottom: 20px;

        pre {
          margin-top: 5px;
          background-color: #fff;
          padding: 10px;
          border-radius: 4px;
          overflow-x: auto;
        }
      }

      .improvements {
        margin-top: 20px;
      }
    }
  }
}
</style>
