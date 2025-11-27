<script setup>
import { ref, onMounted } from 'vue';
// Import Element Plus icons
import { Sunny, Location, VideoCamera } from '@element-plus/icons-vue';
import { useRouter } from 'vue-router';
// import CommonHome from '../../components/CommonHome.vue'; // 不再需要，因为Home.vue已经包含了CommonHome

const router = useRouter();

// Mock data for today's recommended dishes
const recommendedDishes = ref([
  { name: '冰镇西瓜汁', kcal: '180kcal', rating: 5, type: 'drink' },
  { name: '泰式青木瓜沙拉', kcal: '120kcal', rating: 4, type: 'salad' },
  { name: '凉面套餐', kcal: '320kcal', rating: 5, type: 'meal' }
]);

// Mock weather data
const weather = ref({ temp: 32, condition: '晴天' });

// Mock user info removed - now handled by CommonHome component
// Search functionality removed - now handled by CommonHome component

// Handle menu navigation
const navigateTo = (path) => {
  router.push(path);
};

// WebSocket connection
let wsAttempts = 0;
const maxAttempts = 10;

// Initialize WebSocket connection with auto-reconnect using main process WebSocket
const initializeWebSocket = () => {
  // Replace with actual backend WebSocket URL
  const wsUrl = 'ws://localhost:9091'; // Backend Netty server URL

  console.log('Connecting to WebSocket server:', wsUrl);

  // Use WebSocket from main process via IPC
  if (window.api) {
    window.api.connectWebSocket(wsUrl);
  } else {
    console.warn('WebSocket API not available');
  }
};

// Send WebSocket message
const sendWebSocketMessage = (message) => {
  if (window.api) {
    window.api.sendWebSocketMessage(message);
  } else {
    console.error('API not available, cannot send WebSocket message');
  }
};

// WebSocket event handlers setup
const listenersRegistered = window.api?.webSocketListenersRegistered || window.webSocketListenersRegistered;
if (!listenersRegistered && window.api) {
  // Listen for WebSocket events from main process
  window.api?.onWebSocketOpen((event) => {
    console.log('WebSocket connection established:', event);

    // Send authentication if needed
    const authMsg = {
      msgType: 'auth',
      userId: 'test-user-123', // Replace with actual user ID
      token: 'test-token' // Replace with actual token
    };
    sendWebSocketMessage(authMsg);
  });

  window.api?.onWebSocketMessage((message) => {
    console.log('WebSocket message received:', message);
    const { msgType, content, fromId, toId } = message;

    switch (msgType) {
      case 'auth':
        console.log('Authentication response:', content);
        break;

      case 'orderUpdate':
        console.log('Order update received:', content);
        // Update UI with order status
        break;

      case 'chat':
        console.log('Chat message from', fromId, 'to', toId, ':', content);
        // Update chat UI
        break;

      case 'system':
        console.log('System message:', content);
        // Show system notification
        break;

      default:
        console.log('Unknown message type:', msgType);
    }
  });

  window.api?.onWebSocketClose((code, reason) => {
    console.log('WebSocket connection closed:', code, reason);

    // Auto-reconnect if not reached max attempts
    if (wsAttempts < maxAttempts) {
      wsAttempts++;
      const delay = Math.min(3000 * wsAttempts, 30000); // Exponential backoff
      setTimeout(() => {
        console.log(`Reconnecting WebSocket... Attempt ${wsAttempts}/${maxAttempts}`);
        initializeWebSocket();
      }, delay);
    }
  });

  window.api?.onWebSocketError((error) => {
    console.error('WebSocket error:', error);
  });

  // Check if api is extensible before adding property
  if (window.api && Object.isExtensible(window.api)) {
    window.api.webSocketListenersRegistered = true;
  } else {
    // Use a separate variable if api object is not extensible
    window.webSocketListenersRegistered = true;
  }
}

// Initialize WebSocket on mount
onMounted(() => {
  if (window.api) {
    initializeWebSocket();
  }
});
</script>

<template>
      <!-- Right Content Area -->
        <div class="weather-section">
          <el-card shadow="hover">
            <div class="weather-content">
              <el-icon class="weather-icon"><Sunny /></el-icon>
              <div class="weather-info">
                <div class="temp">{{ weather.temp }}℃</div>
                <div class="condition">今日推荐：冰饮/凉菜系列</div>
              </div>
            </div>
          </el-card>
        </div>

        <div class="recommendation-section">
          <h3>今日推荐</h3>
          <el-carousel :interval="3000" height="180px">
            <el-carousel-item v-for="(dish, index) in recommendedDishes" :key="index">
              <el-card shadow="hover" class="dish-card">
                <div class="dish-info">
                  <div class="dish-name">{{ dish.name }}</div>
                  <div class="dish-kcal">{{ dish.kcal }}</div>
                  <div class="dish-rating">
                    <el-rate
                      v-model="dish.rating"
                      disabled
                      show-score
                      text-color="#FF6B6B"
                      class="rating"
                    ></el-rate>
                  </div>
                </div>
              </el-card>
            </el-carousel-item>
          </el-carousel>
        </div>

        <div class="hot-section">
          <el-card shadow="hover">
            <div class="hot-content">
              <el-icon class="fire-icon">🔥</el-icon>
              <span>今日热点：立秋贴秋膘特惠套餐(仅剩10份)</span>
            </div>
          </el-card>
        </div>

        <div class="nearby-section">
          <el-button type="primary" size="large" class="nearby-btn" @click="navigateTo('/user/home/merchants')">
            <el-icon><Location /></el-icon>
            查找附近商家
          </el-button>
        </div>

        <div class="tutorial-section">
          <h3>制作教程与指南</h3>
          <div class="tutorial-grid">
            <el-card shadow="hover" class="tutorial-card">
              <el-icon class="video-icon"><VideoCamera /></el-icon>
              <span>青木瓜沙拉制作教程</span>
            </el-card>
            <el-card shadow="hover" class="tutorial-card">
              <el-icon class="light-icon">💡</el-icon>
              <span>夏日低卡饮食指南</span>
            </el-card>
          </div>
          <el-button type="primary" size="large" class="more-link" @click="navigateTo('/user/home/tutorials')">
            <el-icon><ArrowRight /></el-icon>
            <span>查看更多教程</span>
          </el-button>
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

  .weather-section {
    margin-bottom: 20px;

    .weather-content {
      display: flex;
      align-items: center;
      gap: 20px;

      .weather-icon {
        font-size: 48px;
        color: #f7b267;
      }

      .weather-info {
        font-size: 18px;

        .temp {
          font-size: 24px;
          font-weight: bold;
          color: #FF6B6B;
        }
      }
    }
  }

  .recommendation-section {
    margin-bottom: 20px;

    h3 {
      margin-bottom: 10px;
      font-size: 20px;
      font-weight: bold;
    }

    .dish-card {
      height: 160px;
      display: flex;
      align-items: center;
      justify-content: center;

      .dish-info {
        text-align: center;

        .dish-name {
          font-size: 24px;
          font-weight: bold;
        }

        .dish-kcal {
          font-size: 16px;
          color: #999;
        }

        .dish-rating {
          margin-top: 10px;
        }
      }
    }
  }

  .hot-section {
    margin-bottom: 20px;

    .hot-content {
      display: flex;
      align-items: center;
      gap: 10px;

      .fire-icon {
        font-size: 24px;
        color: #FF6B6B;
      }
    }
  }

  .nearby-section {
    margin-bottom: 20px;

    .nearby-btn {
      background-color: #FF6B6B;
      border: none;

      &:hover {
        background-color: #ff5252;
      }
    }
  }

  .tutorial-section {
    margin-bottom: 20px;

    h3 {
      margin-bottom: 20px;
      font-size: 20px;
      font-weight: bold;
    }

    .tutorial-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
      gap: 15px;
      margin-bottom: 20px;
    }

    .tutorial-card {
      height: 150px;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 10px;

      .video-icon {
        font-size: 36px;
        color: #FF6B6B;
      }

      .light-icon {
        font-size: 36px;
        color: #f7b267;
      }
    }

    .more-link {
      font-size: 14px;
      margin: 0;
    }
  }
}
</style>