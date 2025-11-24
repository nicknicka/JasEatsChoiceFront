import { createRouter, createWebHistory } from 'vue-router'

// Import auth views
const Login = () => import('../views/auth/Login.vue')

// Import user views
const UserHome = () => import('../views/user/Home.vue') // 用户首页
const UserRecommend = () => import('../views/user/Recommend.vue') // 我的推荐
const UserMerchantList = () => import('../views/user/MerchantList.vue') // 商家查找
const UserMerchantDetail = () => import('../views/user/MerchantDetail.vue') // 商家详情
const UserTodayRecipe = () => import('../views/user/TodayRecipe.vue') // 今日食谱
const UserCalorie = () => import('../views/user/Calorie.vue') // 卡路里统计
const UserMyRecipe = () => import('../views/user/MyRecipe.vue') // 我的食谱
const UserOrders = () => import('../views/user/Orders.vue') // 我的订单
const UserConsumeHistory = () => import('../views/user/ConsumeHistory.vue') // 消费记录
const UserMessageCenter = () => import('../views/user/MessageCenter.vue') // 消息中心
const UserSystemNotification = () => import('../views/user/SystemNotification.vue') // 系统通知
const UserChat = () => import('../views/user/Chat.vue') // 单聊
const UserGroupChat = () => import('../views/user/GroupChat.vue') // 群聊
const UserAI = () => import('../views/user/AI.vue') // AI饮食助手
const UserSettings = () => import('../views/user/Settings.vue') // 设置
const UserProfile = () => import('../views/user/Profile.vue') // 用户中心
const UserAddress = () => import('../views/user/Address.vue') // 地址管理
const UserContact = () => import('../views/user/Contact.vue') // 联系客服

// Import merchant views
const MerchantHome = () => import('../views/merchant/Home.vue') // 商家首页
const MerchantOrders = () => import('../views/merchant/Orders.vue') // 商家订单管理
const MerchantMenu = () => import('../views/merchant/Menu.vue') // 商家菜单管理
const MerchantMessages = () => import('../views/merchant/Messages.vue') // 商家消息管理

// 创建路由实例
const router = createRouter({
  history: createWebHistory(),
  routes: [
    // 认证相关路由
    {
      path: '/login',
      name: 'login',
      component: Login,
      meta: { title: '佳食宜选-登录' }
    },
    // 根路径默认跳转到登录页面
    {
      path: '/',
      redirect: '/login'
    },
    // 用户模块根路径跳转到用户首页
    {
      path: '/user',
      redirect: '/user/home'
    },
    // 商家模块根路径跳转到商家首页
    {
      path: '/merchant',
      redirect: '/merchant/home'
    },
    // 用户模块路由
    {
      path: '/user/home',
      name: 'user-home',
      component: UserHome,
      meta: { title: '佳食宜选-用户首页' },
      // 所有Home的子功能都作为嵌套路由
      children: [
        // 默认显示HomeContent作为首页内容
        {
          path: '',
          name: 'user-home-content',
          component: () => import('../views/user/HomeContent.vue'),
          meta: { title: '佳食宜选-用户首页' }
        },
        {
          path: 'recommend',
          name: 'user-recommend',
          component: UserRecommend,
          meta: { title: '佳食宜选-我的推荐' }
        },
        {
          path: 'merchants',
          name: 'user-merchants',
          component: UserMerchantList,
          meta: { title: '佳食宜选-商家查找' }
        },
        {
          path: 'merchant-detail',
          name: 'user-merchant-detail',
          component: UserMerchantDetail,
          meta: { title: '佳食宜选-商家详情' }
        },
        {
          path: 'today-recipe',
          name: 'user-today-recipe',
          component: UserTodayRecipe,
          meta: { title: '佳食宜选-今日食谱' }
        },
        {
          path: 'calorie',
          name: 'user-calorie',
          component: UserCalorie,
          meta: { title: '佳食宜选-卡路里统计' }
        },
        {
          path: 'my-recipe',
          name: 'user-my-recipe',
          component: UserMyRecipe,
          meta: { title: '佳食宜选-我的食谱' }
        },
        {
          path: 'orders',
          name: 'user-orders',
          component: UserOrders,
          meta: { title: '佳食宜选-我的订单' }
        },
        {
          path: 'consume-history',
          name: 'user-consume-history',
          component: UserConsumeHistory,
          meta: { title: '佳食宜选-消费记录' }
        },
        {
          path: 'message-center',
          name: 'user-message-center',
          component: UserMessageCenter,
          meta: { title: '佳食宜选-消息中心' }
        },
        {
          path: 'system-notification',
          name: 'user-system-notification',
          component: UserSystemNotification,
          meta: { title: '佳食宜选-系统通知' }
        },
        {
          path: 'chat',
          name: 'user-chat',
          component: UserChat,
          meta: { title: '佳食宜选-单聊' }
        },
        {
          path: 'group-chat',
          name: 'user-group-chat',
          component: UserGroupChat,
          meta: { title: '佳食宜选-群聊' }
        },
        {
          path: 'ai',
          name: 'user-ai',
          component: UserAI,
          meta: { title: '佳食宜选-AI饮食助手' }
        },
        {
          path: 'settings',
          name: 'user-settings',
          component: UserSettings,
          meta: { title: '佳食宜选-设置' }
        },
        {
          path: 'profile',
          name: 'user-profile',
          component: UserProfile,
          meta: { title: '佳食宜选-用户中心' }
        },
        {
          path: 'address',
          name: 'user-address',
          component: UserAddress,
          meta: { title: '佳食宜选-地址管理' }
        },
        {
          path: 'contact',
          name: 'user-contact',
          component: UserContact,
          meta: { title: '佳食宜选-联系客服' }
        },
        {
          path: 'my-collection',
          name: 'user-my-collection',
          component: () => import('../views/user/MyCollection.vue'),
          meta: { title: '佳食宜选-我的收藏' }
        }
      ]
    },
    // 以下路由已移至/user/home的嵌套路由中
    // {
    //   path: '/user/message-center',
    //   name: 'user-message-center',
    //   component: UserMessageCenter,
    //   meta: { title: '佳食宜选-消息中心' }
    // },
    // {
    //   path: '/user/system-notification',
    //   name: 'user-system-notification',
    //   component: UserSystemNotification,
    //   meta: { title: '佳食宜选-系统通知' }
    // },
    // {
    //   path: '/user/chat',
    //   name: 'user-chat',
    //   component: UserChat,
    //   meta: { title: '佳食宜选-单聊' }
    // },
    // {
    //   path: '/user/group-chat',
    //   name: 'user-group-chat',
    //   component: UserGroupChat,
    //   meta: { title: '佳食宜选-群聊' }
    // },
    // {
    //   path: '/user/ai',
    //   name: 'user-ai',
    //   component: UserAI,
    //   meta: { title: '佳食宜选-AI饮食助手' }
    // },
    // {
    //   path: '/user/settings',
    //   name: 'user-settings',
    //   component: UserSettings,
    //   meta: { title: '佳食宜选-设置' }
    // },

    // 商家模块路由
    {
      path: '/merchant/home',
      name: 'merchant-home',
      component: MerchantHome,
      meta: { title: '佳食宜选-商家首页' }
    },
    {
      path: '/merchant/orders',
      name: 'merchant-orders',
      component: MerchantOrders,
      meta: { title: '佳食宜选-商家订单管理' }
    },
    {
      path: '/merchant/menu',
      name: 'merchant-menu',
      component: MerchantMenu,
      meta: { title: '佳食宜选-商家菜单管理' }
    },
    {
      path: '/merchant/messages',
      name: 'merchant-messages',
      component: MerchantMessages,
      meta: { title: '佳食宜选-商家消息管理' }
    }
  ]
})

// 路由导航守卫 - 用于设置页面标题和登录状态检查
router.beforeEach((to, from, next) => {
  // 设置当前页面标题
  if (to.meta.title) {
    document.title = to.meta.title
  }

  // 检查用户是否已登录
  const isLoggedIn = localStorage.getItem('isLoggedIn') === 'true'
  // 定义需要登录才能访问的受保护路由前缀
  const protectedPaths = ['/user/', '/merchant/']

  // 检查当前路由是否为受保护路由
  const isProtectedRoute = protectedPaths.some((path) => to.path.startsWith(path))
  if (isProtectedRoute && !isLoggedIn) {
    // 如果未登录且访问受保护路由，跳转到登录页面
    next('/login')
  } else {
    // 如果已登录且访问登录页面，跳转到用户首页
    if (to.path === '/login' && isLoggedIn) {
      next('/user/home')
    } else {
      // 其他情况正常跳转
      next()
    }
  }
})

export default router
