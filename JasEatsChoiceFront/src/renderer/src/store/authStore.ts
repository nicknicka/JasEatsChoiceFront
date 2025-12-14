import { defineStore } from 'pinia'

export const useAuthStore = defineStore('auth', {
  // 严格的 Pinia 状态声明
  state: () => ({
    token: '',
    userId: '',
    merchantId: ''
  }),

  // Getters (计算属性)
  getters: {
    isAuthenticated: (state) => !!state.token,
    hasMerchantId: (state) => !!state.merchantId
  },

  // Actions (方法)
  actions: {
    setToken(newToken: string) {
      this.token = newToken
      // 移除 direct localStorage usage，将持久化逻辑移到外部或使用 Pinia 插件
    },

    setUserId(newUserId: string) {
      this.userId = newUserId
    },

    setMerchantId(newMerchantId: string) {
      this.merchantId = newMerchantId
    },

    clearAuth() {
      this.token = ''
      this.userId = ''
      this.merchantId = ''
    }
  }
})
