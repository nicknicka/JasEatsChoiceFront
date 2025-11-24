<template>
  <div class="merchant-detail-container">
    <h2>{{ merchant.name }}</h2>

    <el-card class="merchant-detail-card">
      <!-- 商家基本信息 -->
      <div class="merchant-basic-info">
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
            <el-tag type="primary" size="small">{{ merchant.type }}</el-tag>
          </div>
        </div>
      </div>

      <!-- 商家标签 -->
      <div class="merchant-tags" style="margin-top: 20px;">
        <el-tag
          v-for="tag in merchant.tags"
          :key="tag"
          size="small"
          type="info"
        >
          {{ tag }}
        </el-tag>
      </div>

      <el-divider />

      <!-- 商家菜单 -->
      <div class="merchant-menu">
        <h3>推荐菜单</h3>
        <div class="menu-list">
          <el-card
            v-for="menuItem in menuItems"
            :key="menuItem.id"
            class="menu-card"
          >
            <div class="menu-info">
              <div class="menu-name">{{ menuItem.name }}</div>
              <div class="menu-price">¥{{ menuItem.price.toFixed(2) }}</div>
            </div>
            <div class="menu-desc">{{ menuItem.description }}</div>
            <el-button
              type="primary"
              size="small"
              style="margin-top: 10px;"
              @click="addMenuItem(menuItem)"
            >
              加入购物车
            </el-button>
          </el-card>
        </div>
      </div>

      <el-divider />

      <!-- 操作按钮 -->
      <div class="detail-actions" style="display: flex; gap: 10px;">
        <el-button type="primary" size="small">立即下单</el-button>
        <el-button type="success" size="small">收藏商家</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';

// 商家信息
const merchant = ref({
  id: 0,
  name: '',
  type: '',
  rating: 0,
  distance: '',
  status: '',
  tags: [],
  image: ''
});

// 菜单数据
const menuItems = ref([
  {
    id: 1,
    name: '经典健康套餐',
    price: 28.8,
    description: '包含新鲜蔬菜沙拉、烤鸡胸肉和糙米饭'
  },
  {
    id: 2,
    name: '高蛋白健身餐',
    price: 35.0,
    description: '适合增肌人群的高蛋白套餐'
  },
  {
    id: 3,
    name: '素食套餐',
    price: 22.5,
    description: '全素食，健康无负担'
  }
]);

// 组件挂载时加载商家信息
onMounted(() => {
  const savedMerchant = sessionStorage.getItem('selectedMerchant');
  if (savedMerchant) {
    merchant.value = JSON.parse(savedMerchant);
  } else {
    // 如果没有商家信息，返回商家列表
    // router.push('/user/home/merchants');
  }
});

// 添加菜单项到购物车
const addMenuItem = (item) => {
  ElMessage.success(`${item.name} 已加入购物车`);
  // 这里可以添加真实的购物车逻辑
  console.log('加入购物车:', item);
};
</script>

<style scoped lang="less">
.merchant-detail-container {
  padding: 0 20px 20px 20px;

  h2 {
    font-size: 24px;
    margin: 0 0 20px 0;
  }

  .merchant-detail-card {
    padding: 20px;

    .merchant-basic-info {
      display: flex;
      gap: 20px;
      align-items: center;

      .merchant-image {
        font-size: 80px;
      }

      .merchant-info {
        .merchant-name {
          font-size: 24px;
          font-weight: bold;
          margin-bottom: 10px;
        }

        .merchant-rating {
          display: flex;
          align-items: center;
          gap: 10px;
          margin-bottom: 10px;

          .distance {
            font-size: 16px;
            color: #666;
          }
        }
      }
    }

    .menu-list {
      display: flex;
      flex-direction: column;
      gap: 15px;
    }

    .menu-card {
      .menu-info {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 10px;

        .menu-name {
          font-size: 16px;
          font-weight: bold;
        }

        .menu-price {
          font-size: 18px;
          color: #ff6b6b;
          font-weight: bold;
        }
      }

      .menu-desc {
        color: #666;
        margin-bottom: 15px;
      }
    }
  }
}
</style>
