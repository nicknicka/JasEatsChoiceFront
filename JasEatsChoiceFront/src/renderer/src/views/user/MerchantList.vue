<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';

const router = useRouter();

// 商家列表数据
const merchants = ref([
  {
    id: 1,
    name: '健康轻食馆',
    type: '轻食',
    rating: 4.8,
    distance: '1.2km',
    status: '营业中',
    tags: ['低卡', '新鲜', '快捷'],
    image: '🥗'
  },
  {
    id: 2,
    name: '营养早餐店',
    type: '早餐',
    rating: 4.6,
    distance: '2.5km',
    status: '营业中',
    tags: ['营养', '多样', '准时'],
    image: '🥪'
  },
  {
    id: 3,
    name: '美食天地',
    type: '中餐',
    rating: 4.9,
    distance: '3.8km',
    status: '营业中',
    tags: ['传统', '美味', '实惠'],
    image: '🍚'
  },
  {
    id: 4,
    name: '健身餐厅',
    type: '健身餐',
    rating: 4.7,
    distance: '500m',
    status: '营业中',
    tags: ['高蛋白', '增肌', '减脂'],
    image: '🏋️‍♂️'
  }
]);

// 筛选条件
const filters = ref({
  type: 'all',
  sort: 'distance'
});

// 搜索关键词
const searchKeyword = ref('');

// 获取当前路由
const route = useRoute();

// 页面加载时从URL获取搜索参数
onMounted(() => {
  const searchQuery = route.query.search;
  if (searchQuery) {
    searchKeyword.value = searchQuery;
  }
});

// 跳转到商家详情页面
const viewMerchantDetails = (merchant) => {
  // 将商家信息存储到会话存储，以便在详情页面使用
  sessionStorage.setItem('selectedMerchant', JSON.stringify(merchant));
  // 跳转到商家详情页面，使用查看详情模式
  router.push({
    path: '/user/home/merchant-detail',
    query: { viewMode: 'details' }
  });
};

// 立即下单功能
const orderNow = (merchant) => {
  // 将商家信息存储到会话存储
  sessionStorage.setItem('selectedMerchant', JSON.stringify(merchant));
  // 跳转到商家详情页面的立即下单流程
  router.push({
    path: '/user/home/merchant-detail',
    query: { viewMode: 'order' }
  });
};

// 计算属性：过滤和排序后的商家列表
const filteredMerchants = computed(() => {
  let result = [...merchants.value];

  // 类型筛选
  if (filters.value.type !== 'all') {
    result = result.filter(merchant => merchant.type === filters.value.type);
  }

  // 搜索筛选
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase();
    result = result.filter(merchant =>
      merchant.name.toLowerCase().includes(keyword) ||
      merchant.tags.some(tag => tag.toLowerCase().includes(keyword))
    );
  }

  // 排序
  if (filters.value.sort === 'distance') {
    // 按距离排序
    result.sort((a, b) => {
      const distanceA = parseFloat(a.distance.replace('km', ''));
      const distanceB = parseFloat(b.distance.replace('km', ''));
      return distanceA - distanceB;
    });
  } else if (filters.value.sort === 'rating') {
    // 按评分排序
    result.sort((a, b) => b.rating - a.rating);
  }

  return result;
});
</script>

<template>
  <div class="merchant-list-container">
    <h2>商家查找</h2>

    <!-- 搜索和筛选区 -->
    <div class="search-filter-section">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索商家名称..."
        clearable
        class="search-input"
      >
        <template #prefix>
          <span>🔍</span>
        </template>
      </el-input>

      <div class="filter-row">
        <el-select
          v-model="filters.type"
          placeholder="筛选类型"
          size="small"
          style="width: 120px"
        >
          <el-option label="全部" value="all" />
          <el-option label="轻食" value="轻食" />
          <el-option label="早餐" value="早餐" />
          <el-option label="中餐" value="中餐" />
          <el-option label="健身餐" value="健身餐" />
        </el-select>

        <el-select
          v-model="filters.sort"
          placeholder="排序方式"
          size="small"
          style="width: 120px"
        >
          <el-option label="距离最近" value="distance" />
          <el-option label="评分最高" value="rating" />
        </el-select>
      </div>
    </div>

    <!-- 商家列表 -->
    <div class="merchant-grid">
      <el-card
        v-for="merchant in filteredMerchants"
        :key="merchant.id"
        class="merchant-card"
      >
        <div class="card-header">
          <div class="merchant-image">{{ merchant.image }}</div>
          <div class="merchant-info">
            <div class="merchant-name">{{ merchant.name }}</div>
            <div class="merchant-rating">
              <el-rate
                v-model="merchant.rating"
                :disabled="true"
                show-text
                size="small"
              />
              <span class="distance">{{ merchant.distance }}</span>
            </div>
            <div class="merchant-status">
              <el-tag
                :type="merchant.status === '营业中' ? 'success' : 'danger'"
                size="small"
              >
                {{ merchant.status }}
              </el-tag>
            </div>
          </div>
        </div>

        <div class="merchant-type">
          <el-tag type="primary" size="small">{{ merchant.type }}</el-tag>
        </div>

        <div class="merchant-tags">
          <el-tag
            v-for="tag in merchant.tags"
            :key="tag"
            size="small"
            type="info"
          >
            {{ tag }}
          </el-tag>
        </div>

        <div class="card-actions">
          <el-button type="primary" size="small" @click="viewMerchantDetails(merchant)">查看详情</el-button>
          <el-button type="success" size="small" @click="orderNow(merchant)">立即下单</el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<style scoped lang="less">
.merchant-list-container {
  padding: 0 20px 20px 20px;

  h2 {
    font-size: 24px;
    margin: 0 0 20px 0;
  }

  .search-filter-section {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    .search-input {
      width: 300px;
    }

    .filter-row {
      display: flex;
      gap: 10px;
    }
  }

  .merchant-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
    gap: 20px;
  }

  .merchant-card {
    .card-header {
      display: flex;
      gap: 20px;
      margin-bottom: 15px;
      align-items: center;

      .merchant-image {
        font-size: 50px;
      }

      .merchant-info {
        .merchant-name {
          font-size: 18px;
          font-weight: bold;
          margin-bottom: 5px;
        }

        .merchant-rating {
          display: flex;
          align-items: center;
          gap: 10px;
          margin-bottom: 5px;

          .distance {
            font-size: 14px;
            color: #666;
          }
        }
      }
    }

    .merchant-type {
      margin-bottom: 10px;
    }

    .merchant-tags {
      margin-bottom: 20px;
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
    }

    .card-actions {
      display: flex;
      gap: 10px;

      .el-button {
        flex: 1;
      }
    }
  }
}
</style>
