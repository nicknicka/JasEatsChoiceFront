<script setup>
import { ref, onMounted } from 'vue';
import { useLocation } from '../../composables/useLocation.js';
// 导入 Element Plus 图标
import { Sunny, Cloudy, Location, VideoCamera, ArrowRight } from '@element-plus/icons-vue';
import { useRouter } from 'vue-router';
import api from '../../utils/api.js';
import { API_CONFIG } from '../../config/index.js';
// 导入 WebSocket 常量
import { WS_CONFIG } from '../../constants/wsConstants.js';

const router = useRouter();

// 教程数据 - 从后端获取
const featuredTutorials = ref([]);

// 今日推荐菜品 - 来自后端
const recommendedDishes = ref([]);
// 推荐菜品空状态消息
const recommendEmptyMessage = ref('暂无推荐菜品');
// 今日热点 - 从后端获取
const hotTopic = ref("");

// 天气和位置数据
const weather = ref({
  temp: 32,
  condition: '晴天',
  city: '',
  address: ''
});

// 使用位置选择组合式函数
const {
  cascaderLocationData,
  locationDialogVisible,
  manualLocation,
  handleManualLocationSelect
} = useLocation();


// 根据天气条件获取对应的图标
const getWeatherIcon = () => {
  const condition = weather.value.condition;
  if (!condition) return Sunny;
  if (condition.includes('晴')) return Sunny;
  if (condition.includes('云') || condition.includes('阴') || condition.includes('雨') || condition.includes('雷') || condition.includes('雪')) return Cloudy;
  return Sunny;
};

// 根据天气条件获取推荐的菜品系列
const getRecommendedDishesSeries = () => {
  const condition = weather.value.condition;
  const temp = weather.value.temp;

  // 默认值
  if (condition === undefined) {
    return '热门推荐';
  }

  // 高温天气推荐
  if (temp > 28 || condition.includes('晴')) {
    return '冰饮/凉菜系列';
  }
  // 低温天气推荐
  if (temp < 15 || condition.includes('雪')) {
    return '热食/火锅系列';
  }
  // 雨天推荐
  if (condition.includes('雨')) {
    return '汤品/暖食系列';
  }
  // 多云阴天推荐
  if (condition.includes('云') || condition.includes('阴')) {
    return '均衡饮食系列';
  }
  // 默认推荐
  return '特色菜品系列';
};

// 从后端获取推荐菜品
const fetchRecommendedDishes = () => {
  api.get(API_CONFIG.recipe.recommend)
    .then(response => {
      // Check if response has a message
      if (response.message) {
        recommendEmptyMessage.value = response.message;
      }

      // Handle both null/undefined and empty array cases
      if (response.data && Array.isArray(response.data) && response.data.length > 0) {
        recommendedDishes.value = response.data;
      } else {
        // Set to empty array to show empty state
        recommendedDishes.value = [];
      }
    })
    .catch(error => {
      console.error('加载推荐菜品失败:', error);
      // Reset to default message on error
      recommendEmptyMessage.value = '暂无推荐菜品';
    });
};

// 从后端获取今日热点
const fetchHotTopic = () => {
  // 假设后端提供了获取今日热点的API
  api.get(API_CONFIG.home.hotTopic)
    .then(response => {
      if (response.data) {
        hotTopic.value = response.data;
      } else {
        // 接口成功但返回空数据时，清空热点
        hotTopic.value = "";
      }
    })
    .catch(error => {
      console.error('加载今日热点失败:', error);
      // 请求失败时使用默认文本
      hotTopic.value = "";
    });
};

// 处理自动定位
const handleAutoLocation = async () => {
  // 调用现有 fetchWeather 函数（无参数）获取自动定位
  await fetchWeather();
  // 定位成功后关闭对话框
  locationDialogVisible.value = false;
};

// 处理位置确认
const handleConfirmLocation = () => {
  if (manualLocation.value && manualLocation.value.length > 0) {
    // 对于级联选择器，将数组拼接成完整地址字符串
    const fullAddress = manualLocation.value.join('');
    // 从位置数组中提取城市用于天气API (简化逻辑)
    const city = manualLocation.value[1] || manualLocation.value[0] || '';

    // 立即在UI上更新地址 - 确保不是数组或空数组
    weather.value.address = Array.isArray(fullAddress) ? '未获取到详细地址' : fullAddress || '未获取到详细地址';
    weather.value.city = Array.isArray(city) ? city.join('') : city || '未知城市';

    // 获取详细天气信息
    fetchWeather(city)
      .then(() => {
        locationDialogVisible.value = false;
      });
  } else {
    // 如果未选择手动位置，则使用自动定位
    handleAutoLocation();
  }
};

// 从后端获取位置和天气数据
const fetchWeather = async (selectedCity = null) => {
  try {
    if (selectedCity) {
      // 为选择的城市获取天气信息
      weather.value.city = selectedCity;
      const weatherResponse = await api.get(`${API_CONFIG.weather.current}?city=${encodeURIComponent(selectedCity)}`);
      if (weatherResponse?.data) {
        const { temperature, condition } = weatherResponse.data;
        // 仅当值已定义时才更新
        if (temperature !== undefined) {
          weather.value.temp = temperature;
        }
        if (condition !== undefined) {
          weather.value.condition = condition;
        }
      }
    } else {
      // 步骤1: 从后端获取当前位置
      const locationResponse = await api.get(API_CONFIG.location.location);
      if (locationResponse.data) {
        let { city, address } = locationResponse.data;

        // 处理异常数据格式
        if (Array.isArray(city)) {
          city = city.join('');
        }
        if (Array.isArray(address) || address === '[][]') {
          address = '未获取到详细地址';
        }

        weather.value.city = city;
        weather.value.address = address;

        // 步骤2: 根据城市获取天气信息
        const weatherResponse = await api.get(`${API_CONFIG.weather.current}?city=${encodeURIComponent(city)}`);
        if (weatherResponse?.data) {
          const { temperature, condition } = weatherResponse.data;
          // 仅当值已定义时才更新
          if (temperature !== undefined) {
            weather.value.temp = temperature;
          }
          if (condition !== undefined) {
            weather.value.condition = condition;
          }
        }
      }
    }
  } catch (error) {
    console.error(selectedCity ? '加载天气失败:' : '加载天气或位置失败:', error);
  }
  console.log('获取天气数据:', weather.value);
};

// 处理菜单导航
const navigateTo = (path) => {
  router.push(path);
};

// WebSocket 连接
let wsAttempts = 0;
const maxAttempts = 10;

// 使用主进程 WebSocket 初始化带有自动重连功能的 WebSocket 连接
const initializeWebSocket = () => {
  // 使用 WebSocket 常量构建完整 URL
  const wsUrl = `${WS_CONFIG.URL}${WS_CONFIG.ENDPOINT}`; // 后端 Netty 服务器 URL

  console.log('Connecting to WebSocket server:', wsUrl);

  // 通过 IPC 使用主进程的 WebSocket
  if (window.api) {
    window.api.connectWebSocket(wsUrl);
  } else {
    console.warn('WebSocket API not available');
  }
};

// 发送 WebSocket 消息
const sendWebSocketMessage = (message) => {
  if (window.api) {
    window.api.sendWebSocketMessage(message);
  } else {
    console.error('API not available, cannot send WebSocket message');
  }
};

// WebSocket 事件处理器设置
const listenersRegistered = window.api?.webSocketListenersRegistered || window.webSocketListenersRegistered;
if (!listenersRegistered && window.api) {
  // 监听来自主进程的 WebSocket 事件
  window.api?.onWebSocketOpen(() => {
    console.log('WebSocket connection established');

    // 必要时发送身份验证
    const authMsg = {
      msgType: 'auth',
      userId: localStorage.getItem('userId') || 'test-user-123', // 替换为实际用户 ID
      token: 'test-token' // 替换为实际令牌
    };
    sendWebSocketMessage(authMsg);
  });

  window.api?.onWebSocketMessage((message) => {
    console.log('WebSocket message received:', message);

    // 处理字符串和 Uint8Array 类型的消息
    let messageString;
    if (message instanceof Uint8Array) {
      // 使用 UTF-8 将 Uint8Array 解码为字符串
      messageString = new TextDecoder().decode(message);
    } else if (typeof message === 'string') {
      messageString = message;
    } else {
      console.error('Unknown WebSocket message type:', typeof message);
      return;
    }

    try {
      // 解析 JSON 消息
      const parsedMessage = JSON.parse(messageString);
      console.log('Parsed WebSocket message:', parsedMessage);

      const { msgType, content, fromId, toId } = parsedMessage;

      switch (msgType) {
        case 'auth':
          console.log('Authentication response:', content);
          break;

        case 'orderUpdate':
          console.log('Order update received:', content);
          // 更新订单状态的UI
          break;

        case 'chat':
          console.log('Chat message from', fromId, 'to', toId, ':', content);
          // 更新聊天UI
          break;

        case 'system':
          console.log('System message:', content);
          // 显示系统通知
          break;

        default:
          console.log('Unknown message type:', msgType);
      }
    } catch (error) {
      console.error('Failed to parse WebSocket message:', error);
      console.error('Message content:', messageString);
    }
  });

  window.api?.onWebSocketClose((code, reason) => {
    console.log('WebSocket connection closed:', code, reason);

    // 如果未达到最大尝试次数则自动重连
    if (wsAttempts < maxAttempts) {
      wsAttempts++;
      const delay = Math.min(3000 * wsAttempts, 30000); // 指数退避
      setTimeout(() => {
        console.log(`Reconnecting WebSocket... Attempt ${wsAttempts}/${maxAttempts}`);
        initializeWebSocket();
      }, delay);
    }
  });

  window.api?.onWebSocketError((error) => {
    console.error('WebSocket error:', error);
  });

  // 在添加属性之前检查 api 是否可扩展
  if (window.api && Object.isExtensible(window.api)) {
    window.api.webSocketListenersRegistered = true;
  } else {
    // 如果api对象不可扩展，则使用一个单独的变量
    window.webSocketListenersRegistered = true;
  }
}

// 从后端获取精选教程数据
const fetchFeaturedTutorials = () => {
  api.get(API_CONFIG.tutorial.featured)
    .then(response => {
      // Handle both null/undefined and empty array cases for consistency
      if (response.data && Array.isArray(response.data) && response.data.length > 0) {
        featuredTutorials.value = response.data;
      } else {
        featuredTutorials.value = [];
      }
    })
    .catch(error => {
      console.error('加载精选教程失败:', error);
      // 失败时使用模拟数据作为备份
      featuredTutorials.value = [
        { name: '青木瓜沙拉制作教程', type: 'video' },
        { name: '夏日低卡饮食指南', type: 'article' }
      ];
    });
};

// 在挂载时初始化WebSocket
onMounted(async () => {
  fetchFeaturedTutorials();
  fetchRecommendedDishes();
  await fetchWeather();
  fetchHotTopic(); // 新增：获取今日热点

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
              <el-icon class="weather-icon"><component :is="getWeatherIcon()" /></el-icon>
              <div class="weather-info">
                <div class="location">
                  <el-button
                    type="text"
                    size="small"
                    @click="locationDialogVisible = true"
                    title="选择位置"
                    class="location-icon-button"
                  >
                    <el-icon><Location /></el-icon>
                  <span v-if="weather.address"> {{ weather.address }}</span>
                  <span v-else-if="weather.city">{{ weather.city }}</span>
                  </el-button>
                </div>
                <div class="temp">{{ weather.temp }}℃</div>
                <div class="condition">今日推荐：{{ getRecommendedDishesSeries() }}</div>
              </div>
            </div>
          </el-card>
        </div>

        <div class="recommendation-section">
          <h3>今日推荐</h3>
          <!-- When there are no recommended dishes -->
          <div v-if="recommendedDishes.length === 0" class="empty-recommendations">
            <el-empty
              :description="recommendEmptyMessage"
            >
            <el-button type="primary" @click="fetchRecommendedDishes">重新加载</el-button>
            </el-empty>
          </div>

          <!-- When there are recommended dishes -->
          <div v-else>
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
        </div>

        <!-- 今日热点 - 只有当有数据时显示 -->
        <div class="hot-section" v-if="hotTopic">
          <el-card shadow="hover">
            <div class="hot-content">
              <el-icon class="fire-icon">🔥</el-icon>
              <span>今日热点：{{ hotTopic }}</span>
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

          <!-- 当教程数据为空时显示 -->
          <div v-if="featuredTutorials.length === 0" class="empty-tutorials">
            <el-empty
              description="暂无教程数据"
            >              
            <el-button type="primary" @click="fetchFeaturedTutorials">重新加载</el-button>
            </el-empty>
          </div>

          <!-- 当教程数据不为空时显示 -->
          <div v-else>
            <div class="tutorial-grid">
              <el-card shadow="hover" class="tutorial-card" v-for="(tutorial, index) in featuredTutorials" :key="index">
                <el-icon :class="tutorial.type === 'video' ? 'video-icon' : 'light-icon'">
                  <VideoCamera v-if="tutorial.type === 'video'" />
                  <span v-else>💡</span>
                </el-icon>
                <span>{{ tutorial.name }}</span>
              </el-card>
            </div>
            <el-button type="primary" size="large" class="more-link" @click="navigateTo('/user/home/tutorials')">
              <el-icon><ArrowRight /></el-icon>
              <span>查看更多教程</span>
            </el-button>
          </div>
        </div>

    <!-- Location Selection Dialog -->
    <el-dialog
      v-model="locationDialogVisible"
      title="选择位置"
      width="400px"
    >
      <div class="location-dialog-content">
        <!-- Auto-location button -->
        <el-button
          type="primary"
          class="auto-location-btn"
          @click="handleAutoLocation"
        >
          <el-icon><Location /></el-icon>
          自动定位
        </el-button>

        <!-- Manual location selection -->
        <div class="manual-location-section">
          <h4>手动选择</h4>
          <el-cascader
            v-model="manualLocation"
            :options="cascaderLocationData"
            placeholder="请选择省/市/区"
            style="width: 100%"
            @change="handleManualLocationSelect"
            clearable
          />
        </div>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="locationDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleConfirmLocation">
            确认
          </el-button>
        </span>
      </template>
    </el-dialog>
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

        .location {
          font-size: 14px;
          color: #999;
          margin-bottom: 5px;

          .city-select {
            width: 120px;
            margin-right: 10px;
            vertical-align: middle;
          }

          .location-icon-button {
            margin-right: 10px;
            vertical-align: middle;
            color: #999;
            padding: 0;
          }

        }

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

    /* Empty recommendations styling */
    .empty-recommendations {
      margin-bottom: 20px;
      text-align: center;
      padding: 60px 0;
      background-color: #fafafa;
      border-radius: 10px;
      box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.06);
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

    .empty-tutorials {
      margin-bottom: 20px;
      text-align: center;
      padding: 60px 0;
      background-color: #fafafa;
      border-radius: 10px;
      box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.06);
    }

    /* 美化空状态的文本 */
    :deep(.el-empty__description) {
      color: #909399;
      font-size: 16px;
      margin-top: 20px;
    }

    /* 美化重新加载按钮 */
    .empty-tutorials .el-button {
      margin-top: 30px;
      border-radius: 25px;
      padding: 8px 32px;
      font-size: 14px;
    }
  }

  /* Location dialog styles */
  .location-dialog-content {
    padding: 20px 0;

    .auto-location-btn {
      margin-bottom: 20px;
      width: 100%;
    }

    .manual-location-section {
      h4 {
        margin: 0 0 10px 0;
        font-size: 14px;
        font-weight: bold;
      }

      .location-note {
        font-size: 12px;
        color: #909399;
        margin-top: 5px;
      }
    }
  }
}
</style>