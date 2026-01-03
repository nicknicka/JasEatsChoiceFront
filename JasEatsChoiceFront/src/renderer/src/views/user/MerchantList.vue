<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import axios from 'axios'

// 引入API配置
import { API_CONFIG } from '../../config/index.js'

const router = useRouter()

// 商家列表数据 - 初始化为空数组
const merchants = ref([])

// 加载状态
const isLoading = ref(false)

// 筛选条件
const filters = ref({
  type: 'all',
  sort: 'distance'
})

// 搜索关键词
const searchKeyword = ref('')

// 商家类型筛选选项
const typeOptions = ref([
  { label: '全部', value: 'all' },
  { label: '轻食', value: '轻食' },
  { label: '早餐', value: '早餐' },
  { label: '中餐', value: '中餐' },
  { label: '健身餐', value: '健身餐' }
])

// 商家排序选项
const sortOptions = ref([
  { label: '距离最近', value: 'distance' },
  { label: '评分最高', value: 'rating' }
])

// 获取当前路由
const route = useRoute()

// 页面加载时从URL获取搜索参数并加载商家数据
onMounted(() => {
  const searchQuery = route.query.search
  if (searchQuery) {
    searchKeyword.value = searchQuery
  }

  // 加载商家列表数据
  loadMerchants()
})

// 监听筛选条件变化，重新加载数据
watch([searchKeyword, () => filters.value.type, () => filters.value.sort], () => {
  loadMerchants()
})

// 从后端加载商家列表
const loadMerchants = () => {
  isLoading.value = true

  // 调用后端API获取商家列表，包含所有筛选参数
  axios
    .get(API_CONFIG.baseURL + API_CONFIG.merchant.list, {
      params: {
        search: searchKeyword.value,
        type: filters.value.type,
        sort: filters.value.sort
      }
    })
    .then((response) => {
      console.log('获取商家列表成功:', response.data)
      // 假设后端返回的数据结构与前端期望的一致
      // 如果结构不同，需要在这里进行转换
      if (response.data.data) {
        merchants.value = response.data.data
      } else {
        // 处理空数据情况
        merchants.value = []
      }
    })
    .catch((error) => {
      console.error('加载商家列表失败:', error)
    })
    .finally(() => {
      isLoading.value = false
    })
}

// 立即下单功能
const orderNow = (merchant) => {
  // 将商家信息存储到会话存储
  sessionStorage.setItem('selectedMerchant', JSON.stringify(merchant))
  // 跳转到商家详情页面的立即下单流程
  router.push({
    path: '/user/home/merchant-detail',
    query: { viewMode: 'order' }
  })
}

// 重置筛选条件
const resetFilters = () => {
  searchKeyword.value = ''
  filters.value = {
    type: 'all',
    sort: 'distance'
  }
}

// 计算属性：过滤和排序后的商家列表
const filteredMerchants = computed(() => {
  let result = [...merchants.value].map((merchant) => {
    // 统一状态处理
    let normalizedStatus = '未知状态'
    let isOpen = false
    if (merchant.status === true || merchant.status === 'true' || merchant.status === '营业中') {
      normalizedStatus = '营业中'
      isOpen = true
    } else if (
      merchant.status === false ||
      merchant.status === 'false' ||
      merchant.status === '已停业'
    ) {
      normalizedStatus = '已停业'
      isOpen = false
    }

    // 返回包含归一化状态的商家对象副本
    return {
      ...merchant,
      normalizedStatus,
      isOpen
    }
  })

  // 类型筛选
  if (filters.value.type !== 'all') {
    result = result.filter((merchant) => merchant.type === filters.value.type)
  }

  // 搜索筛选
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(
      (merchant) =>
        merchant.name.toLowerCase().includes(keyword) ||
        (merchant.tags && merchant.tags.some((tag) => tag.toLowerCase().includes(keyword)))
    )
  }

  // 排序
  if (filters.value.sort === 'distance') {
    // 按距离排序
    result.sort((a, b) => {
      const distanceA =
        a.distance && a.distance !== '未知距离'
          ? parseFloat(a.distance.replace('km', ''))
          : Infinity
      const distanceB =
        b.distance && b.distance !== '未知距离'
          ? parseFloat(b.distance.replace('km', ''))
          : Infinity
      return distanceA - distanceB
    })
  } else if (filters.value.sort === 'rating') {
    // 按评分排序
    result.sort((a, b) => b.rating - a.rating)
  }

  return result
})
</script>

<template>
  <div class="merchant-list-container">
    <h2>商家查找</h2>

    <!-- 搜索和筛选区 -->
    <div class="search-filter-section">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索商家名称、类型或特色..."
        clearable
        class="search-input"
        aria-label="搜索商家名称、类型或特色"
      >
        <template #prefix>
          <span class="el-input__icon">🔍</span>
        </template>
      </el-input>

      <div class="filter-row">
        <el-select
          v-model="filters.type"
          placeholder="筛选类型"
          size="small"
          style="width: 140px"
          class="type-select"
        >
          <el-option
            v-for="option in typeOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>

        <el-select v-model="filters.sort" placeholder="排序方式" size="small" style="width: 140px">
          <el-option
            v-for="option in sortOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>

        <el-button type="default" size="small" @click="resetFilters" class="reset-btn">
          重置
        </el-button>
      </div>
    </div>

    <!-- 商家列表 -->
    <div class="merchant-grid">
      <!-- 加载中状态 -->
      <el-skeleton :rows="6" v-if="isLoading" class="loading-skeleton" />

      <el-card
        v-for="merchant in filteredMerchants"
        :key="merchant.id"
        :class="['merchant-card', merchant.isOpen ? 'merchant-card-open' : 'merchant-card-closed']"
        v-else-if="filteredMerchants.length > 0"
      >
        <div class="card-header">
          <div class="merchant-image">
            <img
              v-if="merchant.image && merchant.image !== '未知'"
              :src="merchant.image"
              :alt="merchant.name"
              class="merchant-img"
            />
            <span v-else>🏪</span>
          </div>
          <div class="merchant-info">
            <div class="merchant-name">{{ merchant.name }}</div>
            <div class="merchant-meta">
              <div class="merchant-rating">
                <el-rate v-model="merchant.rating" :disabled="true" show-text size="small" />
                <span class="distance">{{ merchant.distance || '未知距离' }}</span>
              </div>
              <div class="merchant-status">
                <el-tag :type="merchant.isOpen ? 'success' : 'danger'" size="small">
                  {{ merchant.normalizedStatus }}
                </el-tag>
              </div>
            </div>
          </div>
        </div>

        <!-- 商家信息行 -->
        <div class="merchant-details">
          <!-- 商家类型 - 只在有数据时显示 -->
          <div class="merchant-type" v-if="merchant.type">
            <el-tag type="primary" size="small">{{ merchant.type }}</el-tag>
          </div>

          <!-- 商家特色/优惠信息 -->
          <div class="merchant-features">
            <el-tag v-if="merchant.isNew" type="warning" size="small">新店</el-tag>
            <el-tag v-if="merchant.discount" type="success" size="small">{{
              merchant.discount
            }}</el-tag>
          </div>

          <!-- 商家标签 - 只在有数据时显示 -->
          <div class="merchant-tags" v-if="merchant.tags && merchant.tags.length > 0">
            <el-tag v-for="tag in merchant.tags.slice(0, 2)" :key="tag" size="small" type="info">
              {{ tag }}
            </el-tag>
            <el-tag v-if="merchant.tags.length > 2" size="small" type="info">
              +{{ merchant.tags.length - 2 }}
            </el-tag>
          </div>
        </div>

        <div class="card-actions">
          <el-button
            type="primary"
            size="small"
            icon="el-icon-shopping-cart-2"
            icon-position="left"
            @click="orderNow(merchant)"
            >立即下单</el-button
          >
        </div>
      </el-card>

      <!-- 空数据提示 -->
      <div class="empty-data" v-else>
        <div class="empty-icon">🍴</div>
        <div class="empty-text">
          <h3>暂无商家数据</h3>
          <p>当前条件下没有找到任何商家</p>
        </div>
        <div class="empty-actions">
          <el-button type="primary" @click="resetFilters">重置筛选条件</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="less">
.merchant-list-container {
  padding: 0 20px 20px 20px;

  h2 {
    font-size: 32px;
    margin: 0 0 20px 20px;
    color: #1a202c;
    font-weight: 800;
    letter-spacing: -0.5px;

    // 添加装饰性下划线
    &::after {
      content: '';
      display: block;
      width: 60px;
      height: 4px;
      background: linear-gradient(135deg, #23d160 0%, #20c997 100%);
      border-radius: 2px;
      margin-top: 12px;
    }
  }

  .search-filter-section {
    display: flex;
    flex-direction: column; // 修改为垂直布局
    gap: 12px; // 搜索框和筛选区之间的间距
    margin-bottom: 24px;
    padding: 20px;
    background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%); // 渐变背景
    border-radius: 20px;
    box-shadow: 0 6px 24px rgba(0, 0, 0, 0.08); // 更明显的阴影

    .search-input {
      width: 100%;

      :deep(.el-input__inner) {
        border-radius: 14px;
        border: none; /* 去掉搜索框的方形边框 */
        height: 48px;
        font-size: 15px;
        padding-left: 17px;
        padding-right: 21px;
        transition: all 0.3s ease;
        background-color: #ffffff;
      }

      :deep(.el-input__prefix) {
        left: 20px;
        top: 50%;
      }

      /* 搜索图标样式 */
      :deep(.el-input__prefix-inner .el-input__icon) {
        color: #94a3b8;
        font-size: 20px;
      }

      :deep(.el-input__suffix-inner .el-icon-circle-close) {
        font-size: 20px;
        color: #cbd5e1;
      }
    }

    .filter-row {
      display: flex;
      gap: 15px;
      justify-content: center; /* 居中对齐 */
      align-items: center; /* 垂直居中 */
    }

    /* 重置按钮样式 */
    .reset-btn {
      flex-shrink: 0;
      border-radius: 14px;
      height: 48px;
      font-size: 15px;
      padding: 0 32px;
      transition: all 0.3s ease;
      /* 确保按钮文本居中 */
      display: flex !important;
      align-items: center !important;
      justify-content: center !important;
    }

    :deep(.el-select) {
      flex: 1;
      min-width: 180px;

      :deep(.el-select__wrapper) {
        border-radius: 14px;
        border: 2px solid #e2e8f0;
        height: 48px;
        transition: all 0.3s ease;
      }

      :deep(.el-select__input) {
        font-size: 15px;
      }
    }
  }

  .merchant-grid {
    display: flex;
    flex-direction: row;
    flex-wrap: wrap; /* 允许卡片换行 */
    gap: 20px;
    padding: 0 20px;
    justify-content: center; /* 卡片居中排列 */
  }

  .merchant-card {
    flex: 1 1 300px; /* 卡片自适应宽度，最小300px */
    max-width: 500px; /* 最大宽度限制 */
    box-sizing: border-box;
    transition: all 0.3s ease;
    border-radius: 12px;
    box-shadow: 0 2px 15px rgba(0, 0, 0, 0.08);

    &:hover {
      box-shadow: 0 4px 25px rgba(0, 0, 0, 0.12);
      transform: translateY(-2px);
    }
  }

  // 营业中商家卡片 - 非悬停状态
  .merchant-card-open {
    // 恢复卡片基础样式，移除绿色光晕
    box-shadow: 0 2px 15px rgba(0, 0, 0, 0.08);
    transition: all 0.3s ease;

    &:hover {
      // 卡片悬浮时保持基础阴影增强效果
      box-shadow: 0 4px 25px rgba(0, 0, 0, 0.12);
      transform: translateY(-2px);

      // 卡片悬浮时，为营业中标签添加绿色光晕和阴影
      .merchant-status .el-tag[type='success'] {
        box-shadow: 0 0 30px rgba(35, 209, 96, 0.8); /* 标签的绿色光晕效果 */
      }
    }
  }

  // 所有商家卡片通用样式
  .merchant-card {
    .card-header {
      display: flex;
      gap: 20px;
      margin-bottom: 15px;
      align-items: center;

      .merchant-image {
        font-size: 50px;
        display: flex;
        align-items: center;
        justify-content: center;

        .merchant-img {
          width: 80px;
          height: 80px;
          border-radius: 50%;
          object-fit: cover;
        }
      }

      .merchant-info {
        display: flex;
        flex-direction: column; /* 垂直布局 */
        align-items: flex-start; /* 左对齐 */
        gap: 8px; /* 调整间距 */
        flex: 1; /* 占据剩余空间 */

        .merchant-name {
          font-size: 18px;
          font-weight: bold;
          margin-bottom: 4px;
        }

        .merchant-meta {
          display: flex;
          align-items: center;
          justify-content: space-between;
          width: 100%;
        }

        .merchant-rating {
          display: flex;
          align-items: center;
          gap: 10px;

          .distance {
            font-size: 14px;
            color: #666;
          }
        }

        .merchant-status {
          // 营业中标签样式
          .el-tag[type='success'] {
            :deep(.el-tag__content) {
              color: white !important;
            }
            background-color: #23d160 !important; /* 使用稍微浅一点的绿色 */
            border-color: #23d160 !important; /* 边框颜色同步 */
            transition: box-shadow 0.3s ease; /* 光晕过渡效果 */
          }

          // 非营业中标签样式
          .el-tag[type='danger'] {
            :deep(.el-tag__content) {
              color: white !important;
            }
            background-color: #ff4d4f !important; /* 红色 */
            border-color: #ff4d4f !important; /* 边框颜色同步 */
            box-shadow: none; /* 非营业中标签没有光晕 */
          }
        }
      }
    }

    .merchant-details {
      margin-bottom: 20px;
      display: flex;
      flex-wrap: wrap;
      gap: 10px; /* 调整元素之间的间距 */
      align-items: center; /* 垂直居中对齐 */
    }

    .merchant-type {
      margin: 0; /* 重置margin */
    }

    .merchant-features {
      display: flex;
      gap: 8px;
    }

    .merchant-tags {
      margin: 0; /* 重置margin */
      display: flex;
      gap: 8px;
    }

    .card-actions {
      display: flex;
      justify-content: center; /* 将按钮居中 */
      padding-top: 16px;
      border-top: 1px solid #f0f0f0;

      .el-button {
        width: 100%; /* 按钮宽度占满 */
        border-radius: 8px;
        font-weight: 500;
      }
    }
  }

  // 加载中样式
  .loading-skeleton {
    width: 100%;
    flex-shrink: 0;
  }

  // 空数据样式
  .empty-data {
    width: 100%;
    flex-shrink: 0;
    text-align: center;
    padding: 80px 20px;
    background-color: #f8f9fa;
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);

    .empty-icon {
      font-size: 80px;
      margin-bottom: 20px;
      opacity: 0.6;
    }

    .empty-text {
      color: #666;

      h3 {
        font-size: 20px;
        margin: 0 0 10px 0;
        color: #333;
      }

      p {
        font-size: 14px;
        margin: 0;
      }
    }

    .empty-actions {
      margin-top: 30px;

      .el-button {
        padding: 8px 24px;
        font-size: 14px;
      }
    }
  }
}
</style>
