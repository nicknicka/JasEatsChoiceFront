import axios from 'axios'
import { API_CONFIG } from '../config'

// 解码JWT令牌以获取用户信息
const decodeJwt = (token) => {
  try {
    // JWT令牌分为三部分，中间部分是负载（Payload）
    const payload = token.split('.')[1];
    // 解码Base64并解析为JSON
    return JSON.parse(atob(payload));
  } catch (error) {
    console.error('解码JWT令牌失败:', error);
    return null;
  }
};

// 创建axios实例
const api = axios.create({
  baseURL: API_CONFIG.baseURL,
  timeout: 10000 // 请求超时时间
})

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    // 添加请求头，如token等
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  (response) => {
    // 只返回数据部分
    return response.data
  },
  (error) => {
    console.error('响应错误:', error)
    return Promise.reject(error)
  }
)

export default api;
export { decodeJwt };
