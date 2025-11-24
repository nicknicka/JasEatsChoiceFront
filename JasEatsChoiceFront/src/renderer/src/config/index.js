// 后端API配置
export const API_CONFIG = {
  // 基础URL
  baseURL: 'http://localhost:9091',
  
  // AI助手API端点
  ai: {
    chat: '/api/ai/chat', // AI聊天接口
    recipe: '/api/ai/recipe', // 食谱推荐接口
    nutrient: '/api/ai/nutrient' // 营养分析接口
  },
  
  // 用户API端点
  user: {
    login: '/api/user/login', // 登录接口
    profile: '/api/user/profile', // 用户信息接口
    preferences: '/api/user/preferences' // 用户偏好接口
  },
  
  // 食谱API端点
  recipe: {
    today: '/api/recipe/today', // 今日食谱接口
    favorite: '/api/recipe/favorite', // 我的食谱接口
    recommend: '/api/recipe/recommend' // 推荐食谱接口
  },
  
  // 商家API端点
  merchant: {
    list: '/api/merchant/list', // 商家列表接口
    detail: '/api/merchant/detail' // 商家详情接口
  },
  
  // 消息API端点
  message: {
    list: '/api/message/list', // 消息列表接口
    send: '/api/message/send' // 发送消息接口
  }
}

// WebSocket配置
export const WS_CONFIG = {
  url: 'ws://localhost:9091' // WebSocket服务器地址
}
