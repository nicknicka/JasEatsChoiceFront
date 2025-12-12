<template>
  <div class="my-collection-container">
    <div class="header">
      <common-back-button />
      <h2>我的收藏</h2>
    </div>

    <div class="filter-bar">
      <el-select v-model="filterType" placeholder="筛选类型" style="width: 150px; margin-right: 10px;">
        <el-option label="全部" value="all" />
        <el-option label="商家" value="merchant" />
        <el-option label="菜品" value="dish" />
        <el-option label="文章" value="article" />
      </el-select>

      <el-button type="primary" size="small" @click="applyFilter">
        <el-icon><Search /></el-icon> 查询
      </el-button>

      <el-button type="default" size="small" @click="resetFilter">
        <el-icon><Refresh /></el-icon> 重置
      </el-button>

      <el-button type="danger" size="small" @click="clearAll" style="margin-left: auto;">
        <el-icon><Delete /></el-icon> 清空全部
      </el-button>
    </div>

    <div class="collection-grid">
      <el-card
        v-for="item in filteredCollections"
        :key="item.id"
        class="collection-card"
        shadow="hover"
      >
        <div class="card-header">
          <div class="item-type" :class="item.type === 'merchant' ? 'type-merchant' : (item.type === 'dish' ? 'type-dish' : 'type-article')">
            {{ item.type === 'merchant' ? '商家' : (item.type === 'dish' ? '菜品' : '文章') }}
          </div>
          <el-button
            type="text"
            size="small"
            danger
            @click.stop="removeCollection(item.id)"
          >
            <el-icon><Delete /></el-icon>
            删除
          </el-button>
        </div>

        <div class="card-body">
          <img :src="item.image || defaultImage" :alt="item.title" class="collection-image" />
          <h3 class="collection-title">{{ item.title }}</h3>
          <p class="collection-description">{{ item.description }}</p>
          <div class="collection-meta">
            <span class="meta-item">
              <el-icon><Calendar /></el-icon>
              {{ item.date }}
            </span>
          </div>
        </div>

        <div class="card-footer">
          <el-button type="primary" size="small" @click.stop="viewDetails(item)">
            <el-icon><View /></el-icon>
            查看详情
          </el-button>
        </div>
      </el-card>
    </div>

    <div v-if="filteredCollections.length === 0" class="empty-state">
      <el-icon class="empty-icon"><Star /></el-icon>
      <p>您还没有收藏任何内容</p>
      <el-button type="primary" @click="goToHome">
        <el-icon><House /></el-icon>
        去首页看看
      </el-button>
    </div>

    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[9, 18, 36]"
        :total="filteredCollections.length"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import {
  Search,
  Refresh,
  Delete,
  Calendar,
  View,
  Star,
  House
} from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import CommonBackButton from '../../components/common/CommonBackButton.vue';

const router = useRouter();

// 默认图片
const defaultImage = 'https://via.placeholder.com/200x150';

// 模拟收藏数据
const collections = ref([]);

// 筛选条件
const filterType = ref('all');

// 分页
const currentPage = ref(1);
const pageSize = ref(9);

// 计算过滤后的收藏
const filteredCollections = computed(() => {
  let filtered = [...collections.value];

  // 类型过滤
  if (filterType.value !== 'all') {
    filtered = filtered.filter(item => item.type === filterType.value);
  }

  return filtered;
});

// 分页数据
const paginatedCollections = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return filteredCollections.value.slice(start, end);
});

// 应用筛选
const applyFilter = () => {
  ElMessage.success('筛选条件已应用');
  currentPage.value = 1;
};

// 重置筛选
const resetFilter = () => {
  filterType.value = 'all';
  currentPage.value = 1;
  ElMessage.info('筛选条件已重置');
};

// 移除收藏
const removeCollection = (id) => {
  ElMessageBox.confirm('确定要删除该收藏吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  .then(() => {
    const index = collections.value.findIndex(item => item.id === id);
    if (index !== -1) {
      collections.value.splice(index, 1);

      // 更新本地存储
      localStorage.setItem('myCollections', JSON.stringify(collections.value));

      // 更新用户信息中的收藏数量
      const userInfo = JSON.parse(localStorage.getItem('userInfo'));
      userInfo.collections = collections.value.length;
      localStorage.setItem('userInfo', JSON.stringify(userInfo));

      ElMessage.success('收藏已删除');
    }
  })
  .catch(() => {
    ElMessage.info('已取消删除');
  });
};

// 查看详情
const viewDetails = (item) => {
  switch (item.type) {
    case 'merchant':
      router.push({ path: '/user/home/merchant-detail', query: { id: item.id } });
      break;
    case 'dish':
      // 这里可以跳转到菜品详情页
      ElMessage.info('菜品详情页面正在开发中');
      break;
    case 'article':
      // 这里可以跳转到文章详情页
      ElMessage.info('文章详情页面正在开发中');
      break;
    default:
      ElMessage.info('未知类型');
  }
};

// 清空全部
const clearAll = () => {
  ElMessageBox.confirm('确定要清空所有收藏吗？此操作不可恢复。', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'danger'
  })
  .then(() => {
    collections.value = [];

    // 更新本地存储
    localStorage.setItem('myCollections', JSON.stringify(collections.value));

    // 更新用户信息中的收藏数量
    const userInfo = JSON.parse(localStorage.getItem('userInfo'));
    userInfo.collections = 0;
    localStorage.setItem('userInfo', JSON.stringify(userInfo));

    ElMessage.success('所有收藏已清空');
  })
  .catch(() => {
    ElMessage.info('已取消清空');
  });
};

// 去首页
const goToHome = () => {
  router.push('/user/home');
};

// 页面大小变化
const handleSizeChange = (val) => {
  pageSize.value = val;
  currentPage.value = 1;
};

// 页面变化
const handleCurrentChange = (val) => {
  currentPage.value = val;
};

// 从localStorage加载收藏数据
onMounted(() => {
  const savedCollections = localStorage.getItem('myCollections');
  if (savedCollections) {
    collections.value = JSON.parse(savedCollections);
  } else {
    // 初始化模拟数据
    collections.value = [
      {
        id: 1,
        type: 'merchant',
        title: '健康餐厅',
        description: '提供各种健康、营养的餐食',
        image: 'https://via.placeholder.com/200x150/4CAF50/ffffff?text=Healthy+Restaurant',
        date: '2025-11-23'
      },
      {
        id: 2,
        type: 'dish',
        title: '蔬菜沙拉',
        description: '新鲜蔬菜搭配自制酱料',
        image: 'https://via.placeholder.com/200x150/8BC34A/ffffff?text=Salad',
        date: '2025-11-22'
      },
      {
        id: 3,
        type: 'article',
        title: '健康饮食指南',
        description: '如何保持健康的饮食习惯',
        image: 'https://via.placeholder.com/200x150/03A9F4/ffffff?text=Article',
        date: '2025-11-21'
      },
      {
        id: 4,
        type: 'merchant',
        title: '素食餐厅',
        description: '纯素食餐厅，提供各种美味素食',
        image: 'https://via.placeholder.com/200x150/FF9800/ffffff?text=Vegetarian+Restaurant',
        date: '2025-11-20'
      },
      {
        id: 5,
        type: 'dish',
        title: '烤鸡胸肉',
        description: '低脂肪、高蛋白的健康选择',
        image: 'https://via.placeholder.com/200x150/FF5722/ffffff?text=Grilled+Chicken',
        date: '2025-11-19'
      },
      {
        id: 6,
        type: 'article',
        title: '运动后饮食',
        description: '运动后应该吃什么',
        image: 'https://via.placeholder.com/200x150/9C27B0/ffffff?text=Post+Workout+Diet',
        date: '2025-11-18'
      }
    ];

    // 保存到localStorage
    localStorage.setItem('myCollections', JSON.stringify(collections.value));

    // 更新用户信息中的收藏数量
    const userInfo = JSON.parse(localStorage.getItem('userInfo'));
    userInfo.collections = collections.value.length;
    localStorage.setItem('userInfo', JSON.stringify(userInfo));
  }
});
</script>

<style scoped lang="less">
.my-collection-container {
  padding: 0 20px 20px 20px;

  .header {
    display: flex;
    align-items: center;
    margin-bottom: 20px;

    /* 移除自定义返回按钮样式，使用组件默认样式 */

    h2 {
      font-size: 24px;
      margin: 0;
    }
  }

  .filter-bar {
    margin: 20px 0;
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 10px;
  }

  .collection-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 20px;
    margin: 20px 0;

    .collection-card {
      height: 100%;
      display: flex;
      flex-direction: column;

      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;

        .item-type {
          padding: 2px 8px;
          border-radius: 4px;
          font-size: 12px;
          color: white;
          font-weight: bold;

          &.type-merchant {
            background-color: #4CAF50;
          }

          &.type-dish {
            background-color: #FF9800;
          }

          &.type-article {
            background-color: #2196F3;
          }
        }
      }

      .card-body {
        flex: 1;

        .collection-image {
          width: 100%;
          height: 150px;
          object-fit: cover;
          margin-bottom: 15px;
          border-radius: 4px;
        }

        .collection-title {
          font-size: 18px;
          font-weight: bold;
          margin: 0 0 10px 0;
        }

        .collection-description {
          font-size: 14px;
          color: #666;
          margin: 0 0 15px 0;
          height: 42px;
          overflow: hidden;
          text-overflow: ellipsis;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          line-clamp: 2;
          -webkit-box-orient: vertical;
        }

        .collection-meta {
          font-size: 12px;
          color: #999;

          .meta-item {
            margin-right: 20px;
          }
        }
      }

      .card-footer {
        text-align: right;
        margin-top: 15px;
      }
    }
  }

  .empty-state {
    text-align: center;
    padding: 80px 0;

    .empty-icon {
      font-size: 64px;
      color: #ccc;
      margin-bottom: 20px;
    }

    p {
      font-size: 18px;
      color: #666;
      margin-bottom: 30px;
    }
  }

  .pagination {
    margin-top: 20px;
    text-align: right;
  }
}
</style>
