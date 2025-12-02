import axios from 'axios'
import { API_CONFIG } from '../config'

// 解码JWT令牌以获取用户信息 - 简化的Electron版本
const decodeJwt = (token) => {
  try {
    // 检查token是否有效
    if (!token || typeof token !== 'string') return null

    // JWT令牌分为三部分，中间部分是负载（Payload）
    const tokenParts = token.split('.')
    if (tokenParts.length !== 3) return null

    const payloadPart = tokenParts[1]
    if (!payloadPart) return null

    // Base64Url解码：
    // 1. 将Base64Url转换为普通Base64
    // 2. 确保长度是4的倍数
    // 3. 使用Buffer解析（Electron环境保证Buffer可用）
    const base64 = payloadPart.replace(/-/g, '+').replace(/_/g, '/')
    const padded = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), '=')

    // 使用浏览器原生的atob方法进行Base64解码
    // 在Electron渲染进程中，atob是可用的，但需要先将Base64Url转换为标准Base64
    // 并确保处理Unicode字符
    const binaryStr = atob(padded)

    // 处理Unicode字符转换
    const bytes = new Uint8Array(binaryStr.length)
    for (let i = 0; i < binaryStr.length; i++) {
      bytes[i] = binaryStr.charCodeAt(i)
    }

    const decoder = new TextDecoder('utf-8')
    let decoded = decoder.decode(bytes)

    // 移除所有控制字符，包括可能导致问题的Unicode控制字符
    decoded = decoded.replace(/[\p{Cc}\p{Cs}]/gu, '')

    // 尝试解析为JSON
    return JSON.parse(decoded)
  } catch (error) {
    console.error('JWT解码失败:', error)
    // 输出更多调试信息，帮助定位问题
    console.error('Token:', token)
    if (error.name === 'SyntaxError') {
      try {
        const tokenParts = token.split('.')
        const base64 = tokenParts[1].replace(/-/g, '+').replace(/_/g, '/')
        const padded = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), '=')
        const binaryStr = atob(padded)
        const bytes = new Uint8Array(binaryStr.length)
        for (let i = 0; i < binaryStr.length; i++) {
          bytes[i] = binaryStr.charCodeAt(i)
        }

        const decoder = new TextDecoder('utf-8')
        let decoded = decoder.decode(bytes)
        // 移除所有控制字符，包括可能导致问题的Unicode控制字符
        decoded = decoded.replace(/[\p{Cc}\p{Cs}]/gu, '')
        console.error('Decoded payload (before JSON parse):', decoded)
      } catch (innerError) {
        console.error('Failed to decode payload for debugging:', innerError)
      }
    }
    return null
  }
}

// 创建axios实例
const api = axios.create({
  baseURL: API_CONFIG.baseURL,
  timeout: 10000 // 请求超时时间
})

// 默认重试次数
const DEFAULT_RETRIES = 2
// 重试延迟（毫秒）
const RETRY_DELAY = 1000

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

// 响应拦截器 - 重试逻辑
api.interceptors.response.use(
  (response) => {
    // 只返回数据部分
    return response.data
  },
  async (error) => {
    const config = error.config

    // 如果配置不存在或者没有设置重试，则直接拒绝
    if (!config) {
      return Promise.reject(error)
    }

    // 设置默认重试次数
    config.retryCount = config.retryCount || 0

    // 判断是否需要重试
    if (
      config.retryCount < DEFAULT_RETRIES &&
      // 可以重试的错误类型
      (error.code === 'ETIMEDOUT' ||
        error.code === 'ECONNABORTED' ||
        error.code === 'NETWORK_ERROR' ||
        (error.response && error.response.status >= 500))
    ) {
      // 增加重试计数
      config.retryCount += 1

      // 创建一个延迟函数
      const delay = (ms) => new Promise((resolve) => setTimeout(resolve, ms))

      // 延迟后重试请求
      return delay(RETRY_DELAY).then(() => {
        console.log(`API请求重试 (${config.retryCount}/${DEFAULT_RETRIES})`)
        return api(config)
      })
    }

    // 格式化错误信息
    const formattedError = new Error('网络请求失败，请稍后重试')
    if (error.response) {
      formattedError.status = error.response.status
      formattedError.data = error.response.data

      // 根据状态码设置不同的错误消息
      switch (error.response.status) {
        case 400:
          formattedError.message = '请求参数错误'
          break
        case 401:
          formattedError.message = '未授权，请重新登录'
          // 可以在这里添加自动登出逻辑
          break
        case 403:
          formattedError.message = '权限不足'
          break
        case 404:
          formattedError.message = '请求的资源不存在'
          break
        case 500:
          formattedError.message = '服务器内部错误'
          break
        case 503:
          formattedError.message = '服务器维护中'
          break
        default:
          formattedError.message = `请求失败 (${error.response.status})`
      }
    } else if (error.request) {
      // 请求已发送但没有收到响应
      formattedError.message = '网络连接超时，请检查网络设置'
      formattedError.code = error.code
    } else {
      // 请求配置错误
      formattedError.message = error.message
    }

    console.error('API请求失败:', formattedError)
    return Promise.reject(formattedError)
  }
)

export default api
export { decodeJwt }
