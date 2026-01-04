<script setup>
import { useAuthStore } from '../../store/authStore'
import api from '../../utils/api.js'
import { ref } from 'vue'

const authStore = useAuthStore()
let merchantId = authStore.merchantId

// 如果 Pinia 中没有商家ID，尝试从 localStorage 读取
if (!merchantId) {
  const localStorageMerchantId = localStorage.getItem('auth_merchantId')
  if (localStorageMerchantId) {
    merchantId = localStorageMerchantId
    authStore.setMerchantId(localStorageMerchantId) // 更新到 Pinia 中
  }
}

// 商家信息
const merchantInfo = ref({
  id: merchantId,
  name: '健康轻食馆',
  rating: 4.8,
  phone: '138-1234-5678',
  email: 'health-food@example.com',
  address: '北京市朝阳区建国路88号'
})

// 获取商家信息
const fetchMerchantInfo = () => {
  api
    .get(`/v1/merchant/${merchantId}`)
    .then((response) => {
      if (response.code === '200' && response.data) {
        merchantInfo.value = response.data
      }
    })
    .catch((error) => {
      console.error('获取商家信息失败:', error)
    })
}

fetchMerchantInfo()
</script>

<template>
  <div class="merchant-info-card">
    <div class="info-header">
      <div class="avatar-section">
        <span class="avatar">📸</span>
      </div>
      <div class="detail-section">
        <div class="merchant-name">🏪 {{ merchantInfo.nickname }}</div>
        <div class="merchant-rating">🌟 {{ merchantInfo.rating }}</div>
        <div class="contact-info">
          <span class="phone">📞 {{ merchantInfo.phone }}</span>
          <span class="email">📧 {{ merchantInfo.email }}</span>
          <span class="address">📍 {{ merchantInfo.address }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="less">
.merchant-info-card {
  margin-bottom: 24px;
  padding: 24px;
  border: 2px solid #409eff;
  border-radius: 12px;
  background-color: #ffffff;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);

  .info-header {
    display: flex;
    align-items: center;
    gap: 20px;

    .avatar-section {
      .avatar {
        font-size: 64px;
      }
    }

    .detail-section {
      flex: 1;

      .merchant-name {
        font-size: 20px;
        font-weight: 600;
        margin-bottom: 8px;
      }

      .merchant-rating {
        margin-bottom: 8px;
      }

      .contact-info {
        display: flex;
        flex-wrap: wrap;
        gap: 20px;
        font-size: 14px;
        color: #606266;
      }
    }
  }
}
</style>
