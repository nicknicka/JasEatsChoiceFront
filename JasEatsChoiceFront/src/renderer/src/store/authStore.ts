import { defineStore } from 'pinia'

export const useAuthStore = defineStore('auth', {
  // 严格的 Pinia 状态声明
  state: () => ({
    token: '',
    userId: '',
    merchantId: '',
    currentRole: ''
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
    },

    setUserId(newUserId: string) {
      this.userId = newUserId
    },

    setMerchantId(newMerchantId: string) {
      this.merchantId = newMerchantId
    },

    setCurrentRole(newRole: string) {
      this.currentRole = newRole
    },

    clearAuth() {
      this.token = ''
      this.userId = ''
      this.merchantId = ''
      this.currentRole = ''
    }
  }
})
