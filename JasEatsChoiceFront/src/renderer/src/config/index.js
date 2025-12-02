// 后端API配置
export const API_CONFIG = {
  // 基础URL
  baseURL: 'http://localhost:8080',

  // AI助手API端点
  ai: {
    chat: '/api/v1/ai/chat', // AI聊天接口
    recipe: '/api/v1/ai/recipe', // 食谱推荐接口
    nutrient: '/api/v1/ai/nutrient', // 营养分析接口
    recognizeDish: '/api/v1/ai/dish-recognize' // 菜品识别接口
  },

  // 用户API端点
  user: {
    login: '/api/v1/users/login', // 登录接口
    profile: '/api/v1/users/{userId}', // 用户信息接口
    preferences: '/api/v1/users/{userId}/preferences', // 用户偏好接口
    sendSmsCode: '/api/v1/users/send-sms-code', // 发送手机验证码接口
    sendEmailCode: '/api/v1/users/send-email-code' // 发送邮箱验证码接口
  },

  // 食谱API端点
  recipe: {
    today: '/api/v1/recipe/today', // 今日食谱接口
    favorite: '/api/v1/recipe/favorite', // 我的食谱接口
    recommend: '/api/v1/recipe/recommend' // 推荐食谱接口
  },

  // 商家API端点
  merchant: {
    list: '/api/v1/merchant', // 商家列表接口
    detail: '/api/v1/merchant/', // 商家详情接口
    menu: '/api/v1/merchants/{merchantId}/menu', // 商家菜单接口
    comments: '/api/v1/merchant/{merchantId}/comments', // 商家评价接口
    album: '/api/v1/merchant/{merchantId}/album', // 商家相册接口
    discounts: '/api/v1/merchant/{merchantId}/discounts', // 商家优惠接口
    businessOverview: '/api/v1/merchant/{merchantId}/business-overview', // 营业概览接口
    avatar: '/api/v1/merchant/{merchantId}/avatar' // 商家头像接口
  },

  // 菜品API端点
  dish: {
    list: '/dishes', // 菜品列表接口
    detail: '/dishes/' // 菜品详情接口
  },

  // 消息API端点
  message: {
    list: '/api/v1/message/list', // 消息列表接口
    send: '/api/v1/message/send' // 发送消息接口
  },

  // 教程API端点
  tutorial: {
    featured: '/api/v1/tutorial/featured', // 精选教程接口（用于首页展示）
    list: '/api/v1/tutorial/list' // 全部教程接口
  },

  // 天气API端点
  weather: {
    current: '/api/v1/weather', // 获取天气信息接口
    location: '/api/v1/location' // 获取当前定位接口
  },

  // 订单API端点
  order: {
    list: '/api/v1/orders/user/', // 获取用户订单列表接口
    detail: '/api/v1/orders/' // 获取订单详情接口
  }
}

// WebSocket配置
export const WS_CONFIG = {
  url: 'ws://localhost:9091' // WebSocket服务器地址
}
