<script setup>
import { useRouter } from 'vue-router'
import { ref, onMounted, computed, watch, provide } from 'vue'
import {
  Search,
  Menu,
  Shop,
  Calendar,
  DataAnalysis,
  Document,
  List,
  Message,
  ChatDotRound,
  Setting,
  HomeFilled,
  User
} from '@element-plus/icons-vue'
import { decodeJwt } from '../utils/api.js'
import { useAuthStore } from '../store/authStore'
import { useUserStore } from '../store/userStore'
// 导入CommonAvatar组件
import CommonAvatar from './CommonAvatar.vue'

const router = useRouter()

// 导航到指定路径
const navigateTo = (path) => {
  router.push(path)
}

// 获取 Pinia 存储
const authStore = useAuthStore()
const userStore = useUserStore()

// 用户信息 - 从 Pinia 中获取
// 注释：使用计算属性直接从 userStore 获取 userInfo

// 用户角色
const userRole = ref('user') // 'user' 或 'merchant'
// 是否已注册商家 - 直接通过userInfo.merchantId判断，不再需要单独的状态变量

// 提供更新用户信息的方法给子组件
const updateSidebarAvatar = (avatarUrl) => {
  if (userStore.userInfo) {
    userStore.userInfo.avatar = avatarUrl
  }
}
provide('updateSidebarAvatar', updateSidebarAvatar)

// 预定义菜单数据
const menuData = {
  // 用户端菜单 - 分组折叠版本
  user: [
    { index: '1', name: '首页', icon: HomeFilled, path: '/user/home' }, // 首页入口
    // 推荐与发现分组
    {
      index: 'group-1',
      name: '推荐与发现',
      icon: Menu,
      children: [
        { index: '2', name: '我的推荐', icon: Menu, path: '/user/home/recommend' },
        { index: '3', name: '商家查找', icon: Shop, path: '/user/home/merchants' }
      ]
    },
    // 饮食管理分组
    {
      index: 'group-2',
      name: '饮食管理',
      icon: Calendar,
      children: [
        { index: '4', name: '今日食谱', icon: Calendar, path: '/user/home/today-recipe' },
        { index: '61', name: '饮食记录', icon: Calendar, path: '/user/home/diet-record' },
        { index: '5', name: '卡路里统计', icon: DataAnalysis, path: '/user/home/calorie' },
        { index: '6', name: '我的食谱', icon: Document, path: '/user/home/my-recipe' }
      ]
    },
    // 个人中心分组
    {
      index: 'group-3',
      name: '个人中心',
      icon: User,
      children: [
        { index: '7', name: '用户中心', icon: User, path: '/user/home/profile' },
        { index: '8', name: '查看订单', icon: List, path: '/user/home/orders' }
      ]
    },
    // 智能助手分组
    {
      index: 'group-4',
      name: '智能助手',
      icon: ChatDotRound,
      children: [
        { index: '9', name: '消息中心', icon: Message, path: '/user/home/message-center' },
        { index: '11', name: '聊天消息', icon: ChatDotRound, path: '/user/home/chat' },
        { index: '10', name: 'AI饮食助手', icon: ChatDotRound, path: '/user/home/ai' }
      ]
    },
    // 设置菜单
    {
      index: '12',
      name: '设置',
      icon: Setting,
      path: '/user/home/settings',
      isSetting: true
    }
  ],
  // 商家端菜单
  // 商家端菜单 - 按功能模块重新排序：首页 → 核心业务 → 店铺管理 → 客户沟通 → 经营分析
  merchant: [
    { index: '1', name: '我的店铺', icon: HomeFilled, path: '/merchant/home' }, // 首页入口
    { index: '2', name: '今日订单', icon: List, path: '/merchant/home/today-orders' }, // 核心业务模块
    { index: '3', name: '菜单管理', icon: Shop, path: '/merchant/home/menu' }, // 店铺管理模块
    {
      index: '4',
      name: '菜品管理',
      icon: Document,
      path: '/merchant/home/dish-management'
    },
    { index: '6', name: '商家聊天', icon: ChatDotRound, path: '/merchant/home/chat' }, // 客户沟通模块
    { index: '9', name: '消息管理', icon: Message, path: '/merchant/home/messages' },
    {
      index: '7',
      name: '评价中心',
      icon: DataAnalysis,
      path: '/merchant/home/comments'
    },
    {
      index: '8',
      name: '经营统计',
      icon: DataAnalysis,
      path: '/merchant/home/statistics'
    } // 经营分析模块
  ]
}

// 根据当前角色过滤菜单
// 当前激活的菜单项索引
const activeMenuIndex = ref('1')

// 根据当前角色过滤菜单
const currentMenu = computed(() => {
  return menuData[userRole.value] ? menuData[userRole.value] : menuData.user || []
})

// 根据当前路由计算并设置激活的菜单项索引 - 支持分组菜单
const updateActiveMenuIndex = () => {
  const currentPath = router.currentRoute.value.path
  console.log('当前路由:', currentPath)

  // 特殊处理商家详情页 - 激活商家查找菜单
  if (currentPath.startsWith('/user/home/merchant-detail')) {
    activeMenuIndex.value = '3' // "商家查找"的索引是3
    console.log('匹配到商家详情页，激活商家查找菜单')
    return
  }

  // 特殊处理地址管理和联系客服页面 - 激活用户中心菜单
  if (
    currentPath.startsWith('/user/home/address') ||
    currentPath.startsWith('/user/home/contact')
  ) {
    activeMenuIndex.value = '7' // "用户中心"的索引是7
    console.log('匹配到用户中心相关页面，激活用户中心菜单')
    return
  }

  // 查找当前路由对应的菜单项 - 包括分组内的子菜单
  for (const menuItem of currentMenu.value) {
    // 如果是分组菜单，检查其子菜单
    if (menuItem.children) {
      for (const childItem of menuItem.children) {
        if (currentPath.startsWith(childItem.path)) {
          activeMenuIndex.value = childItem.index
          return
        }
      }
    }
    // 如果是普通菜单，直接检查
    else if (currentPath.startsWith(menuItem.path)) {
      activeMenuIndex.value = menuItem.index
      return
    }
  }

  // 如果没有匹配到，默认激活第一个菜单项
  activeMenuIndex.value = currentMenu.value[0]?.index || '1'
  console.log('未匹配到菜单项，默认激活第一个')
}

// 菜单点击事件处理 - 支持分组菜单
const handleMenuSelect = (index) => {
  // 查找菜单项，包括分组内的子菜单
  let targetMenuItem = null

  // 遍历当前菜单
  for (const menuItem of currentMenu.value) {
    // 如果是分组菜单，查找其子菜单
    if (menuItem.children) {
      targetMenuItem = menuItem.children.find(childItem => childItem.index === index)
      if (targetMenuItem) break
    }
    // 如果是普通菜单，直接比较
    else if (menuItem.index === index) {
      targetMenuItem = menuItem
      break
    }
  }

  // 如果找到目标菜单，进行跳转
  if (targetMenuItem) {
    navigateTo(targetMenuItem.path)
  }
}

// 头像放大弹窗
const showLargeAvatar = ref(false)

// 菜单栏宽度状态
const sidebarWidth = ref('150px')

// 监听菜单展开事件
const handleMenuOpen = () => {
  sidebarWidth.value = '220px' // 展开时增宽
}

// 监听菜单关闭事件
const handleMenuClose = () => {
  sidebarWidth.value = '150px' // 关闭时恢复原宽度
}


// 角色切换功能
const toggleRole = () => {
  try {
    // 切换角色
    userRole.value = userRole.value === 'user' ? 'merchant' : 'user'

    // 跳转对应页面
    if (userRole.value === 'user') {
      navigateTo('/user/home')
    } else {
      navigateTo('/merchant/home')
    }

    // Don't save role to localStorage - always default to user

    console.log('角色切换成功:', userRole.value)
  } catch (error) {
    console.error('角色切换失败:', error)
  }
}

// 页面加载时从当前路由恢复角色，默认进入用户角色
onMounted(() => {
  try {
    if (!userStore.userInfo || userStore.userInfo.avatar === '') {
      userStore.fetchUserInfo()
    }

    // 1. First check current route to determine role
    let detectedRole = 'user' // Always default to user

    if (router.currentRoute.value?.path?.startsWith('/merchant/')) {
      detectedRole = 'merchant'
    }

    // 3. Always use detected role from route or default to user, ignore saved role
    userRole.value = detectedRole

    // User info is now managed through Pinia - no need to initialize it here
    // 从JWT令牌获取实际用户名（仅作参考，实际应用应将用户信息存储在userStore中）
    if (userRole.value === 'user' && authStore.token) {
      const decodedToken = decodeJwt(authStore.token)
      if (decodedToken && decodedToken.username && userStore.userInfo) {
        userStore.userInfo.name = decodedToken.username
      }
    }

    // Don't save role to localStorage - always default to user

    console.log('恢复角色成功:', userRole.value)

    // 页面加载后更新菜单项高亮
    updateActiveMenuIndex()
  } catch (error) {
    console.error('恢复角色失败:', error)
  }
})

// 监听路由变化，更新菜单项高亮
watch(
  () => router.currentRoute.value.path,
  () => {
    updateActiveMenuIndex()
  }
)

// 监听当前菜单变化，更新菜单项高亮
watch(
  currentMenu,
  () => {
    updateActiveMenuIndex()
  },
  { deep: true }
)

// 监听商家注册状态变化 - 不再需要，直接使用userStore.userInfo.merchantId判断

// Watch for route changes to update role automatically
watch(
  () => router.currentRoute.value?.path,
  (newPath) => {
    let newRole = 'user' // Default to user

    if (newPath?.startsWith('/merchant/')) {
      newRole = 'merchant'
    }

    // Only update if role changed
    if (userRole.value !== newRole) {
      userRole.value = newRole

      // Update user info based on role (using Pinia store)
      if (userRole.value === 'merchant') {
        // 商户端信息从userStore.merchantInfo获取
        userStore.userInfo = {
          name: '商户端',
          avatar: userStore.merchantInfo?.avatar || 'https://picsum.photos/id/200/150/150'
        }
      } else if (userRole.value === 'user') {
        // 从authStore获取token并解码用户名
        let username = '用户端'
        if (authStore.token) {
          const decodedToken = decodeJwt(authStore.token)
          if (decodedToken && decodedToken.username) {
            username = decodedToken.username
          }
        }
        // 使用userStore管理用户信息
        userStore.userInfo = {
          ...userStore.userInfo,
          name: username,
          avatar: '👤'
        }
      }

      // Role is now managed through Pinia - no need to save to localStorage
      console.log('路由变化自动更新角色:', userRole.value)
      // 更新角色后，重新计算激活的菜单项索引
      updateActiveMenuIndex()
    }
  }
)

const searchQuery = ref('')

const handleSearch = (value) => {
  // 实现搜索逻辑
  try {
    // 如果搜索内容为空，不执行搜索
    if (!value || value.trim() === '') {
      return
    }

    console.log('开始搜索:', value)

    // 根据当前角色跳转到对应的搜索页面
    if (userRole.value === 'user') {
      // 用户角色，跳转到商家列表页面并携带搜索参数
      navigateTo({
        path: '/user/home/merchants',
        query: { search: value.trim() }
      })
    } else {
      // 商家角色，跳转到订单页面并携带搜索参数
      navigateTo({
        path: '/merchant/home/orders',
        query: { search: value.trim() }
      })
      console.log('商家角色搜索功能:', value)
    }
  } catch (error) {
    console.error('搜索失败:', error)
  }
}
</script>

<template>
  <div class="app-container">
    <!-- 顶部导航栏 -->
    <el-header class="top-nav-bar">
      <div
        class="logo"
        @click="() => navigateTo(userRole === 'merchant' ? '/merchant/home' : '/user/home')"
      >
        🎨 佳食宜选
      </div>
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
        <!-- 商家端已注册：显示角色切换按钮 -->
        <el-button
          v-if="!!authStore.hasMerchantId"
          type="text"
          class="identity-switch"
          @click="toggleRole"
        >
          <span :class="['user-icon', userRole === 'user' ? 'icon-enlarged' : '']">👤</span>
          <span :class="['merchant-icon', userRole === 'merchant' ? 'icon-enlarged' : '']">🏪</span>
        </el-button>
        <!-- 商家端未注册：显示注册跳转图标 -->
        <el-button v-else type="text" @click="navigateTo('/merchant/register')">
          <el-icon><Shop /></el-icon>
          <span>商家注册</span>
        </el-button>
      </div>
    </el-header>

    <div class="main-content">
      <!-- 左侧菜单栏 -->
      <el-aside :width="sidebarWidth" class="sidebar-menu">
        <div class="avatar-section">
          <CommonAvatar
            :size="80"
            class="user-avatar"
            :avatar-url="userStore.userInfo?.avatar"
            :fallback-text="userStore.userInfo?.name || '用户'"
            :show-upload="false"
            :click-to-enlarge="true"
          >
          </CommonAvatar>
          <div class="username">
            {{ userStore.userInfo?.name || userRole === 'merchant' ? '商户端' : '用户端' }}
          </div>
        </div>

        <el-menu
          v-model:default-active="activeMenuIndex"
          class="menu-list"
          @select="handleMenuSelect"
          @open="handleMenuOpen"
          @close="handleMenuClose"
        >
          <!-- 遍历菜单，区分分组菜单和普通菜单项 -->
          <template v-for="menuItem in currentMenu" :key="menuItem.index">
            <!-- 分组菜单 -->
            <el-sub-menu
              v-if="menuItem.children"
              :index="menuItem.index"
            >
              <template #title>
                <el-icon>
                  <component :is="menuItem.icon" />
                </el-icon>
                <span>{{ menuItem.name }}</span>
              </template>
              <!-- 分组下的子菜单 -->
              <el-menu-item
                v-for="childItem in menuItem.children"
                :key="childItem.index"
                :index="childItem.index"
              >
                <el-icon>
                  <component :is="childItem.icon" />
                </el-icon>
                <template #title>{{ childItem.name }}</template>
              </el-menu-item>
            </el-sub-menu>

            <!-- 普通菜单项 -->
            <el-menu-item
              v-else
              :index="menuItem.index"
              :class="{ 'setting-menu': menuItem.isSetting }"
            >
              <el-icon>
                <component :is="menuItem.icon" />
              </el-icon>
              <template #title>{{ menuItem.name }}</template>
            </el-menu-item>
          </template>
        </el-menu>
      </el-aside>

      <!-- 右侧内容区域，使用router-view实现子组件内容访问 -->
      <el-main class="content-area">
        <router-view />
      </el-main>
    </div>

    <!-- 头像放大对话框 -->
    <el-dialog v-model="showLargeAvatar" title="个人头像" width="300px" top="20%">
      <div style="text-align: center; padding: 20px 0">
        <CommonAvatar
          :size="200"
          class="user-avatar"
          :avatar-url="userStore.userInfo?.avatar"
          :fallback-text="userRole === 'merchant' ? '商户' : '用户'"
          :show-upload="false"
        >
        </CommonAvatar>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button type="primary" @click="showLargeAvatar = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
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
  color: #ff6b6b;
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

    .user-icon,
    .merchant-icon {
      transition: font-size 0.3s ease;
    }

    .icon-enlarged {
      font-size: 24px;
      font-weight: bold;
    }
  }
}

.main-content {
  display: flex;
  height: calc(100vh - 60px);
}

.sidebar-menu {
  background-color: #fff;
  border-right: 1px solid #eee;
  transition: width 0.3s ease-in-out; /* 添加平滑过渡动画 */

  .avatar-section {
    text-align: center;
    padding: 20px 0;
    border-bottom: 1px solid #eee;


    .username {
      margin-top: 8px;
      font-size: 14px;
      font-weight: 500;
      color: #333;
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
