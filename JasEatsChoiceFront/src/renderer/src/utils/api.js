
import axios from 'axios'
import { API_CONFIG } from '../config'

// 创建axios实例
const api = axios.create({
  baseURL: API_CONFIG.baseURL,
  timeout: 10000 // 请求超时时间
})

// 请求拦截器
api.interceptors.request.use(
  config => {
    // 可以在这里添加请求头，如token等
    // const token = localStorage.getItem('token')
    // if (token) {
    //   config.headers.Authorization = `Bearer ${token}`
    // }
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  response => {
    // 只返回数据部分
    return response.data
  },
  error => {
    console.error('响应错误:', error)
    return Promise.reject(error)
  }
)

export default api
