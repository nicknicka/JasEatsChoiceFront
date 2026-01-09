<script setup>
import { useAuthStore } from '../../store/authStore'
import { useUserStore } from '../../store/userStore'
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElTag, ElIcon, ElButton } from 'element-plus'
import {
  Location,
  Phone,
  Message,
  Clock,
  ShoppingBag,
  Star,
  Money,
  CircleCheck,
  CircleClose,
  Loading,
  Edit
} from '@element-plus/icons-vue'

const authStore = useAuthStore()
const userStore = useUserStore()
const loading = ref(false)

// 从 userStore 中获取商家信息
const merchantInfo = computed(() => userStore.merchantInfo || {
  id: authStore.merchantId,
  name: '',
  rating: 0,
  phone: '',
  email: '',
  address: '',
  avatar: '',
  status: false,
  businessHours: '',
  category: '',
  averagePrice: 0,
  businessScope: []
})

// 获取商家信息
const fetchMerchantInfo = async () => {
  if (!authStore.merchantId) {
    console.error('商家ID不存在')
    return
  }

  loading.value = true
  try {
    await userStore.fetchMerchantInfo()
    console.log('商家信息获取成功:', userStore.merchantInfo)
  } catch (error) {
    console.error('获取商家信息失败:', error)
    ElMessage.error('获取商家信息失败')
  } finally {
    loading.value = false
  }
}

// 组件挂载时获取商家信息
onMounted(() => {
  fetchMerchantInfo()
})

// 格式化营业时间
const formatBusinessHours = (hours) => {
  if (!hours) return '暂无'
  return hours
}

// 格式化经营品类
const formatBusinessScope = (scope) => {
  if (!scope || scope === null || scope === '') return '暂无'
  // 确保scope是数组类型
  const scopeArray = Array.isArray(scope) ? scope : [scope]
  if (scopeArray.length === 0) return '暂无'
  return scopeArray.join('、')
}

// 格式化平均价格
const formatAveragePrice = (price) => {
  if (!price) return '暂无'
  return `¥${price}/人`
}

// 点击编辑按钮
const handleEditClick = () => {
  ElMessage.info('编辑功能待实现')
  // 这里可以添加跳转到编辑页面的逻辑
}
</script>

<template>
  <div class="merchant-info-card">
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <ElIcon class="is-loading"><Loading /></ElIcon>
      <span>加载中...</span>
    </div>

    <!-- 商家信息卡片 -->
    <div v-else class="info-content">
      <div class="info-header">
        <div class="avatar-section">
          <img v-if="merchantInfo.avatar" :src="merchantInfo.avatar" class="avatar" alt="商家头像" />
          <div v-else class="avatar-placeholder">
            <ShoppingBag style="font-size: 32px; color: #409eff;" />
          </div>
        </div>

        <div class="detail-section">
          <div class="merchant-header">
            <div class="merchant-name">
              {{ merchantInfo.name }}
              <ElTag
                :type="merchantInfo.status ? 'success' : 'danger'"
                size="small"
                class="status-tag"
              >
                <ElIcon v-if="merchantInfo.status"><CircleCheck /></ElIcon>
                <ElIcon v-else><CircleClose /></ElIcon>
                {{ merchantInfo.status ? '营业中' : '已打烊' }}
              </ElTag>
            </div>
            <ElButton
              type="primary"
              size="small"
              @click="handleEditClick"
              class="edit-button"
            >
              <ElIcon><Edit /></ElIcon>
              <span>编辑信息</span>
            </ElButton>
          </div>

          <div class="merchant-rating">
            <ElTag type="warning" size="small" class="rating-tag">
              <ElIcon><Star /></ElIcon>
              <span>{{ merchantInfo.rating || 0.0 }}分</span>
            </ElTag>
          </div>

          <div class="contact-info">
            <div class="contact-item" v-if="merchantInfo.phone">
              <ElIcon class="icon"><Phone /></ElIcon>
              <span>{{ merchantInfo.phone }}</span>
            </div>
            <div class="contact-item" v-if="merchantInfo.email">
              <ElIcon class="icon"><Message /></ElIcon>
              <span>{{ merchantInfo.email }}</span>
            </div>
            <div class="contact-item" v-if="merchantInfo.address">
              <ElIcon class="icon"><Location /></ElIcon>
              <span>{{ merchantInfo.address }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 详细信息 -->
      <div class="info-details">
        <div class="detail-item">
          <div class="detail-label">
            <ElIcon><Clock /></ElIcon>
            <span>营业时间</span>
          </div>
          <div class="detail-value">{{ formatBusinessHours(merchantInfo.businessHours) }}</div>
        </div>

        <div class="detail-item">
          <div class="detail-label">
            <ElIcon><ShoppingBag /></ElIcon>
            <span>经营品类</span>
          </div>
          <div class="detail-value">{{ formatBusinessScope(merchantInfo.businessScope) }}</div>
        </div>

        <div class="detail-item">
          <div class="detail-label">
            <ElIcon><Money /></ElIcon>
            <span>平均价格</span>
          </div>
          <div class="detail-value">{{ formatAveragePrice(merchantInfo.averagePrice) }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="less">
.merchant-info-card {
  margin-bottom: 24px;
  padding: 24px;
  border: 1px solid #ebeef5;
  border-radius: 16px;
  background-color: #ffffff;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;

  &:hover {
    box-shadow: 0 4px 20px 0 rgba(0, 0, 0, 0.15);
    transform: translateY(-2px);
  }

  .loading-container {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 40px;
    color: #606266;
    font-size: 14px;

    .el-icon {
      margin-right: 8px;
      font-size: 20px;
    }
  }

  .info-content {
    .info-header {
      display: flex;
      align-items: flex-start;
      gap: 24px;
      margin-bottom: 20px;
      padding-bottom: 20px;
      border-bottom: 1px solid #f0f0f0;

      .avatar-section {
        flex-shrink: 0;

        .avatar {
          width: 80px;
          height: 80px;
          border-radius: 50%;
          object-fit: cover;
          border: 3px solid #f0f9ff;
        }

        .avatar-placeholder {
          width: 80px;
          height: 80px;
          border-radius: 50%;
          background-color: #f0f9ff;
          display: flex;
          align-items: center;
          justify-content: center;
        }
      }

      .detail-section {
        flex: 1;

        .merchant-header {
          display: flex;
          align-items: center;
          justify-content: space-between;
          margin-bottom: 12px;

          .merchant-name {
            font-size: 24px;
            font-weight: 700;
            color: #303133;
            display: flex;
            align-items: center;
            gap: 12px;
          }

          .merchant-actions {
            display: flex;
            align-items: center;
            gap: 12px;

            .merchant-status {
              .status-text {
                margin-left: 4px;
              }
            }

            .edit-button {
              display: flex;
              align-items: center;
              gap: 4px;
              background: linear-gradient(135deg, #e6f7ff 0%, #bae7ff 100%);
              border: 1px solid #91d5ff;
              color: #0050b3;
              border-radius: 8px;
              padding: 8px 16px;
              font-weight: 500;
              box-shadow: 0 2px 8px rgba(64, 169, 255, 0.2);
              transition: all 0.3s ease;

              &:hover {
                background: linear-gradient(135deg, #bae7ff 0%, #91d5ff 100%);
                box-shadow: 0 4px 12px rgba(64, 169, 255, 0.3);
                transform: translateY(-1px);
              }
            }
          }
        }

        .merchant-rating {
          margin-bottom: 12px;
        }

        .contact-info {
          display: flex;
          flex-wrap: wrap;
          gap: 20px;

          .contact-item {
            display: flex;
            align-items: center;
            font-size: 14px;
            color: #606266;

            .icon {
              margin-right: 6px;
              color: #409eff;
            }
          }
        }
      }
    }

    .info-details {
      display: flex;
      flex-wrap: wrap;
      gap: 32px;

      .detail-item {
        display: flex;
        align-items: center;
        gap: 8px;

        .detail-label {
          display: flex;
          align-items: center;
          min-width: 100px;
          color: #606266;
          font-size: 14px;
          font-weight: 500;

          .el-icon {
            margin-right: 6px;
            color: #409eff;
          }
        }

        .detail-value {
          color: #303133;
          font-size: 14px;
          font-weight: 400;
        }
      }
    }
  }
}

// 状态标签和评分标签样式优化
.status-tag, .rating-tag {
  cursor: pointer;
  line-height: 28px;
  white-space: nowrap;
  display: inline-flex;
  align-items: center;
  border-radius: 8px;
  padding: 4px 12px;

  :deep(.el-tag__content) {
    white-space: nowrap;
    display: inline-flex;
    align-items: center;
    gap: 4px;
  }

  &:hover {
    opacity: 0.8;
  }
}

// 编辑按钮样式优化
.edit-button {
  background: linear-gradient(135deg, #e6f7ff 0%, #bae7ff 100%);
  border: 1px solid #91d5ff;
  color: #0050b3;
  border-radius: 8px;
  padding: 8px 16px;
  font-weight: 500;
  box-shadow: 0 2px 8px rgba(64, 169, 255, 0.2);
  transition: all 0.3s ease;

  &:hover {
    background: linear-gradient(135deg, #bae7ff 0%, #91d5ff 100%);
    box-shadow: 0 4px 12px rgba(64, 169, 255, 0.3);
    transform: translateY(-1px);
  }
}

// 响应式设计
@media (max-width: 768px) {
  .merchant-info-card {
    padding: 16px;

    .info-header {
      flex-direction: column;
      align-items: center;
      text-align: center;

      .detail-section {
        .merchant-header {
          flex-direction: column;
          gap: 8px;

          .merchant-name {
            font-size: 20px;
          }

          .merchant-actions {
            flex-direction: column;
            gap: 8px;
          }
        }

        .contact-info {
          justify-content: center;
        }
      }
    }

    .info-details {
      flex-direction: column;
      gap: 16px;
    }
  }
}
</style>
