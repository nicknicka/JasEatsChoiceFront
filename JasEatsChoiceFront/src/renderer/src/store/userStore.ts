import { defineStore } from 'pinia'
import axios from 'axios'
import { API_CONFIG } from '../config'
import { useAuthStore } from './authStore'

export interface UserInfo {
  userId: string
  phone: string
  nickname: string
  email: string
  height: number | null
  weight: number | null
  dietGoal: string | null
  allergies: any // 过敏原信息，JSON格式
  preferTags: any // 饮食偏好标签，JSON格式
  disableWeatherRecommend: boolean | null
  createTime: string // 创建时间
  updateTime: string // 更新时间
  merchantId?: string // 商家ID，如果不为空表示用户已注册为商家
  avatar?: string // 用户头像URL
}

export interface MerchantInfo {
  merchantId: string
  name: string
  address?: string
  phone: string
  status: boolean
  createTime?: string
  updateTime?: string
  businessLicense: string
  businessScope: string[]
  contactName: string
  avatar?: string
  rating?: number
  businessHours?: string
  email?: string
  // 其他商家信息字段
}

export const useUserStore = defineStore('user', {
  // 严格的 Pinia 状态声明
  state: () => ({
    userInfo: null as UserInfo | null,
    merchantInfo: null as MerchantInfo | null
  }),

  // Getters (计算属性)
  getters: {
    isMerchantRegistered: (state) => !!state.userInfo?.merchantId || !!state.merchantInfo
  },

  // Actions (方法)
  actions: {
    async fetchUserInfo() {
      try {
        const authStore = useAuthStore()
        if (!authStore.userId) {
          throw new Error('用户ID不存在')
        }

        // 使用正确的API端点路径，后端是GET /v1/users/{userId}
        const response = await axios.get(`${API_CONFIG.baseURL}/v1/users/${authStore.userId}`)

        if (response.data.code === '200') {
          this.userInfo = response.data.data

          // 如果用户有注册商家ID，保存到authStore并获取商家信息
          if (this.userInfo && this.userInfo.merchantId) {
            authStore.setMerchantId(this.userInfo.merchantId)
            await this.fetchMerchantInfo() // 自动获取商家信息
          }

          return response.data.data
        } else {
          throw new Error(response.data.message || '获取用户信息失败')
        }
      } catch (error: any) {
        console.error('获取用户信息失败:', error)
        throw error
      }
    },

    setUserInfo(info: UserInfo) {
      this.userInfo = info

      // 如果用户有注册商家ID，保存到authStore
      if (info.merchantId) {
        const authStore = useAuthStore()
        authStore.setMerchantId(info.merchantId)
      }
    },

    async fetchMerchantInfo() {
      try {
        const authStore = useAuthStore()
        if (!authStore.merchantId) {
          throw new Error('商家ID不存在')
        }

        const response = await axios.get(`${API_CONFIG.baseURL}/v1/merchant/info`, {
          params: { merchantId: authStore.merchantId }
        })

        if (response.data.code === '200') {
          this.merchantInfo = response.data.data
          return response.data.data
        } else {
          throw new Error(response.data.message || '获取商家信息失败')
        }
      } catch (error: any) {
        console.error('获取商家信息失败:', error)
        throw error
      }
    },

    setMerchantInfo(info: MerchantInfo) {
      this.merchantInfo = info
    },

    clearUserInfo() {
      this.userInfo = null
      this.merchantInfo = null
    }
  }
})
