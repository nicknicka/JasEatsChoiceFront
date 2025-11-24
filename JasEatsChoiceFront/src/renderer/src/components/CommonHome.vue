<script setup>
import { useRouter } from 'vue-router';
import { ref, onMounted, computed } from 'vue';
import {
  Search, Menu, Shop, Calendar, DataAnalysis,
  Document, List, Message, ChatDotRound, Setting,
  HomeFilled
} from '@element-plus/icons-vue';

const router = useRouter();

// 导航到指定路径
const navigateTo = (path) => {
  router.push(path);
};

// 模拟用户信息
const userInfo = ref({ name: '佳食用户', avatar: '👤' });

// 用户角色
const userRole = ref('user'); // 'user' 或 'merchant'

// 预定义菜单数据
const menuData = {
  // 用户端菜单
  user: [
    { index: '1', name: '我的推荐', icon: Menu, path: '/user/home/recommend' },
    { index: '2', name: '商家查找', icon: Shop, path: '/user/home/merchants' },
    { index: '3', name: '今日食谱', icon: Calendar, path: '/user/home/today-recipe' },
    { index: '4', name: '卡路里统计', icon: DataAnalysis, path: '/user/home/calorie' },
    { index: '5', name: '我的食谱', icon: Document, path: '/user/home/my-recipe' },
    { index: '6', name: '查看订单', icon: List, path: '/user/home/orders' },
    { index: '7', name: '消息中心', icon: Message, path: '/user/home/message-center' },
    { index: '8', name: 'AI饮食助手', icon: ChatDotRound, path: '/user/home/ai' },
    { index: '9', name: '设置', icon: Setting, path: '/user/home/settings', isSetting: true }
  ],
  // 商家端菜单
  merchant: [
    { index: '1', name: '商家首页', icon: HomeFilled, path: '/merchant/home' },
    { index: '2', name: '订单管理', icon: List, path: '/merchant/orders' },
    { index: '3', name: '菜单管理', icon: Shop, path: '/merchant/menu' },
    { index: '4', name: '消息管理', icon: Message, path: '/merchant/messages' }
  ]
};

// 根据当前角色过滤菜单
const currentMenu = computed(() => {
  return menuData[userRole.value] ? menuData[userRole.value] : menuData.user || [];
});

// 搜索功能

// 菜单点击事件处理
const handleMenuSelect = (index) => {
  const menuItem = currentMenu.value.find(item => item.index === index);
  if (menuItem) {
    navigateTo(menuItem.path);
  }
};

// 角色切换功能
const toggleRole = () => {
  try {
    // 切换角色
    userRole.value = userRole.value === 'user' ? 'merchant' : 'user';

    // 更新用户信息和跳转
    if (userRole.value === 'user') {
      userInfo.value = { name: '佳食用户', avatar: '👤' };
      navigateTo('/user/home');
    } else {
      userInfo.value = { name: '佳商', avatar: '🏪' };
      navigateTo('/merchant/home');
    }

    // 保存当前角色到localStorage
    localStorage.setItem('currentRole', userRole.value);

    console.log('角色切换成功:', userRole.value);
  } catch (error) {
    console.error('角色切换失败:', error);
  }
};

// 页面加载时从localStorage恢复角色
onMounted(() => {
  try {
    const savedRole = localStorage.getItem('currentRole');
    if (savedRole) {
      userRole.value = savedRole;
      if (userRole.value === 'merchant') {
        userInfo.value = { name: '佳商', avatar: '🏪' };
      }
    }
  } catch (error) {
    console.error('恢复角色失败:', error);
  }
});
const searchQuery = ref('');

const handleSearch = (value) => {
  // 实现搜索逻辑
  try {
    // 如果搜索内容为空，不执行搜索
    if (!value || value.trim() === '') {
      return;
    }

    console.log('开始搜索:', value);

    // 根据当前角色跳转到对应的搜索页面
    if (userRole.value === 'user') {
      // 用户角色，跳转到商家列表页面并携带搜索参数
      navigateTo({
        path: '/user/home/merchants',
        query: { search: value.trim() }
      });
    } else {
      // 商家角色，跳转到商家订单页面或其他适合商家的搜索页面
      navigateTo('/merchant/home/orders');
      console.log('商家角色搜索功能待实现:', value);
    }
  } catch (error) {
    console.error('搜索失败:', error);
  }
};
</script>

<template>
  <div class="app-container">
    <!-- 顶部导航栏 -->
    <el-header class="top-nav-bar">
      <div class="logo" @click="navigateTo('/user/home')">🎨 佳食宜选</div>
      <el-input
        v-model="searchQuery"
        placeholder="🔍 搜索框(支持菜品/商家搜索)"
        clearable
        class="search-input"
        @input="handleSearch"
        @keyup.enter="handleSearch(searchQuery)"
      >
        <template #append>
          <el-button type="primary" @click="handleSearch(searchQuery)">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
        </template>
      </el-input>
      <div class="user-info">
        <el-button type="text" class="identity-switch" @click="toggleRole">👤/🏪</el-button>
        <span>{{ userInfo.name }}</span>
      </div>
    </el-header>

    <div class="main-content">
      <!-- 左侧菜单栏 -->
      <el-aside width="168px" class="sidebar-menu">
        <div class="avatar-section" @click="navigateTo('/user/home/profile')">
          <el-avatar :size="80" class="user-avatar" style="cursor: pointer;">👤</el-avatar>
        </div>

        <el-menu
          default-active="1"
          class="menu-list"
          @select="handleMenuSelect"
        >
          <el-menu-item
            v-for="menuItem in currentMenu"
            :key="menuItem.index"
            :index="menuItem.index"
            :class="{ 'setting-menu': menuItem.isSetting }"
          >
            <el-icon>
              <component :is="menuItem.icon" />
            </el-icon>
            <template #title>{{ menuItem.name }}</template>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 右侧内容区域，使用router-view实现子组件内容访问 -->
      <el-main class="content-area">
        <router-view />
      </el-main>
    </div>
  </div>
</template>

<style scoped lang="less">
.app-container {
  height: 100vh;
  width: 100%;
}

.top-nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: 60px;
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.logo {
  font-size: 24px;
  font-weight: bold;
  color: #FF6B6B;
  cursor: pointer;
}

.search-input {
  width: 400px;
  margin: 0 20px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;

  .identity-switch {
    font-size: 18px;
    padding: 0;
  }
}

.main-content {
  display: flex;
  height: calc(100vh - 60px);
}

.sidebar-menu {
  background-color: #fff;
  border-right: 1px solid #eee;

  .avatar-section {
    text-align: center;
    padding: 20px 0;
    border-bottom: 1px solid #eee;

    .user-avatar {
      background-color: #FF6B6B;
    }
  }

  .menu-list {
    border: none;
    height: calc(100% - 120px);
  }

  .setting-menu {
    border-top: 1px solid #eee;
    margin-top: 20px;
  }
}

.content-area {
  padding: 20px;
  background-color: #f5f5f5;
  overflow-y: auto;
}
</style>
