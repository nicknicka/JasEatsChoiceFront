<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '../../utils/api.js'
import { useAuthStore } from '../../store/authStore'
import { API_CONFIG } from '../../config/index.js'

const router = useRouter()
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

// 今日菜单数据
const todayMenus = ref([])

// 当前选中的菜单
const selectedMenu = ref(null)
// 当前菜单的菜品
const currentMenuDishes = ref([])

// 筛选后的菜单
const filteredMenus = ref([...todayMenus.value])

// 菜单类型筛选
const activeMenuTypeFilter = ref('全部')

// 菜单状态筛选
const activeMenuFilter = ref('全部')

// 菜单分类列表
const menuCategories = ref(['全部', '早餐', '午餐', '晚餐', '下午茶', '今日特色'])

// 菜单状态列表
const menuStatuses = ref([
  { value: '全部', label: '全部' },
  { value: 'online', label: '上架中' },
  { value: 'offline', label: '下架中' },
  { value: 'draft', label: '草稿' }
])

// 菜单状态映射
const menuStatusMap = {
  online: { text: '上架中', icon: '🟢', type: 'success' },
  draft: { text: '草稿', icon: '🟡', type: 'warning' },
  offline: { text: '下架中', icon: '🔴', type: 'danger' }
}

// 菜品状态映射
const dishStatusMap = {
  online: { text: '🟢 在售', type: 'success' },
  almost_sold: { text: '🟡 即将售罄', type: 'warning' },
  offline: { text: '🔴 下架', type: 'danger' }
}

// 模拟菜品数据，关联到各个菜单
const dishData = {
  早餐菜单: [
    {
      id: 1,
      name: '豆浆',
      price: 3,
      category: '饮品',
      status: 'online',
      stock: 100,
      updateTime: '2024-11-21 06:00'
    },
    {
      id: 2,
      name: '油条',
      price: 2,
      category: '主食',
      status: 'online',
      stock: 80,
      updateTime: '2024-11-21 06:30'
    }
  ],
  午餐菜单: [
    {
      id: 4,
      name: '鱼香肉丝',
      price: 18,
      category: '热菜',
      status: 'online',
      stock: 50,
      updateTime: '2024-11-21 10:30'
    }
  ],
  晚餐菜单: [
    {
      id: 7,
      name: '红烧肉',
      price: 22,
      category: '热菜',
      status: 'online',
      stock: 30,
      updateTime: '2024-11-21 16:30'
    }
  ],
  下午茶菜单: [
    {
      id: 9,
      name: '奶茶',
      price: 15,
      category: '饮品',
      status: 'online',
      stock: 70,
      updateTime: '2024-11-21 14:00'
    }
  ],
  今日特色菜单: [
    {
      id: 4,
      name: '鱼香肉丝',
      price: 18,
      category: '热菜',
      status: 'online',
      stock: 50,
      updateTime: '2024-11-21 10:30'
    }
  ]
}

// 从后端获取今日菜单数据
const fetchTodayMenus = () => {
  api
    .get(`/v1/menus/merchants/${merchantId}/menu`)
    .then((response) => {
      if (response.code === '200' && response.data) {
        todayMenus.value = response.data.map((menu) => ({
          ...menu,
          status: menu.status === 'active' ? 'online' : 'offline',
          dishes: 0,
          updateTime: menu.updateTime ? menu.updateTime.replace('T', ' ') : '',
          autoOnline: menu.autoStartTime ? menu.autoStartTime.replace('T', ' ') : '',
          autoOffline: menu.autoEndTime ? menu.autoEndTime.replace('T', ' ') : ''
        }))
        filteredMenus.value = [...todayMenus.value]
      }
    })
    .catch((error) => {
      console.error('获取今日菜单数据失败:', error)
    })
}

// 切换菜单
const switchMenu = (menu) => {
  selectedMenu.value = menu
  currentMenuDishes.value = dishData[menu.name] || []
}

// 筛选菜单：先按类型，再按状态
const filterMenus = (filterType, filterCategory = 'status') => {
  if (filterCategory === 'status') {
    activeMenuFilter.value = filterType
  } else if (filterCategory === 'type') {
    activeMenuTypeFilter.value = filterType
  }

  // 确保总是基于todayMenus最新的数据进行筛选
  let newFilteredMenus = [...todayMenus.value]

  // 状态筛选
  if (activeMenuFilter.value !== '全部') {
    newFilteredMenus = newFilteredMenus.filter((menu) => menu.status === activeMenuFilter.value)
  }

  // 类型筛选
  if (activeMenuTypeFilter.value !== '全部') {
    const targetType = activeMenuTypeFilter.value
    newFilteredMenus = newFilteredMenus.filter((menu) => {
      const menuName = String(menu.name || '')
      return menuName.includes(targetType) || menuName.replace('菜单', '').includes(targetType)
    })
  }

  filteredMenus.value = newFilteredMenus

  // 如果当前选中的菜单不在筛选结果中，重置选择
  if (
    selectedMenu.value &&
    !filteredMenus.value.some((menu) => menu.id === selectedMenu.value.id)
  ) {
    selectedMenu.value = null
    currentMenuDishes.value = []
  }
}

// 页面跳转
const navigateToMenu = () => {
  router.push('/merchant/home/menu')
}

// 编辑菜品
const editDish = (dish) => {
  // 导航到菜品编辑页面
  router.push({
    path: '/merchant/home/dish-edit',
    query: { dishId: dish.id, menuName: selectedMenu.value.name }
  })
}

// 切换菜品状态
const toggleDishStatus = (dish) => {
  // 计算新状态
  const currentStatus = dish.status
  let newStatus

  if (currentStatus === 'online' || currentStatus === 'almost_sold') {
    newStatus = 'offline'
  } else if (currentStatus === 'offline') {
    newStatus = dish.stock <= 10 ? 'almost_sold' : 'online'
  }

  // 更新本地状态
  const oldStatusText =
    currentStatus === 'online' ? '上架' : currentStatus === 'almost_sold' ? '即将售罄' : '下架'
  const newStatusText =
    newStatus === 'online' ? '上架' : newStatus === 'almost_sold' ? '即将售罄' : '下架'

  // 调用API更新菜品状态
  const updateData = {
    dishId: dish.id,
    status: newStatus
  }

  api
    .put(API_CONFIG.merchant.updateDishStatus.replace('{dishId}', dish.id), updateData)
    .then((response) => {
      if (response.data && response.data.success) {
        dish.status = newStatus
        ElMessage.success(`菜品 ${dish.name} 已从${oldStatusText}状态切换为${newStatusText}状态`)
      }
    })
    .catch((error) => {
      console.error('切换菜品状态失败:', error)
      ElMessage.error('切换菜品状态失败')
    })
}

onMounted(() => {
  fetchTodayMenus()
})
</script>

<template>
  <div class="quick-actions-card today-menu-card">
    <div class="menu-header">
      <h3 class="card-title">📋 今日菜单</h3>
      <div class="filter-section">
        <span class="filter-label">分类：</span>
        <el-tag
          v-for="category in menuCategories"
          :key="category"
          type="info"
          effect="light"
          class="menu-filter-tag"
          :class="{ active: activeMenuTypeFilter === category }"
          @click="filterMenus(category, 'type')"
          >{{ category }}</el-tag
        >
      </div>
    </div>

    <div class="menu-header">
      <div class="filter-section">
        <span class="filter-label">状态：</span>
        <el-tag
          v-for="status in menuStatuses"
          :key="status.value"
          :type="
            status.value === 'online'
              ? 'success'
              : status.value === 'offline'
                ? 'danger'
                : status.value === 'draft'
                  ? 'warning'
                  : 'primary'
          "
          effect="light"
          class="menu-status-tag"
          :class="{ active: activeMenuFilter === status.value }"
          @click="filterMenus(status.value, 'status')"
          >{{ status.label }}</el-tag
        >
      </div>
    </div>

    <div class="menu-table-container">
      <el-table
        :data="filteredMenus"
        :row-class-name="(row) => (selectedMenu?.id === row.id ? 'active' : '')"
        @row-click="switchMenu"
      >
        <el-table-column prop="name" label="菜单名称" min-width="200" />
        <el-table-column prop="status" label="状态" width="140">
          <template #default="scope">
            <el-tag :type="menuStatusMap[scope.row.status].type">
              {{ menuStatusMap[scope.row.status].icon }}
              {{ menuStatusMap[scope.row.status].text }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="dishes" label="菜品数量" width="120">
          <template #default="scope"> 🍴 {{ scope.row.dishes }} 菜品 </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="200" />
        <el-table-column prop="autoOnline" label="自动上架" width="180" />
        <el-table-column prop="autoOffline" label="自动下架" width="180" />

        <!-- 自定义空数据提示 -->
        <template #empty>
          <div class="empty-state">
            <span class="el-icon-info"></span>
            <p>🍽️ 今日咱未设置菜单</p>
          </div>
        </template>
      </el-table>
    </div>

    <div class="view-all">
      <el-button type="text" @click="navigateToMenu">📤 查看全部菜单</el-button>
    </div>

    <!-- 菜品列表 -->
    <div v-if="selectedMenu" class="quick-actions-card dishes-card">
      <div class="menu-header">
        <h3 class="card-title">🍽️ {{ selectedMenu.name }} - 菜品列表</h3>
      </div>

      <div class="dish-list">
        <div class="dish-item" v-for="dish in currentMenuDishes" :key="dish.id">
          <div class="dish-cover">
            {{ dish.image || '🍱' }}
          </div>
          <div class="dish-info">
            <div class="dish-name">
              <span class="name">{{ dish.name }}</span>
              <el-tag :type="dishStatusMap[dish.status].type" size="small">
                {{ dishStatusMap[dish.status].text }}
              </el-tag>
            </div>

            <div class="dish-desc">
              {{ dish.description || '美味可口，欢迎品尝' }}
            </div>

            <div class="dish-stats">
              <span class="dish-category">📁 {{ dish.category }}</span>
              <span class="dish-price">💰 ¥{{ dish.price }}</span>
              <span
                class="dish-stock"
                :class="{
                  'stock-almost': dish.status === 'almost_sold',
                  'stock-off': dish.status === 'offline'
                }"
              >
                {{
                  dish.status === 'almost_sold'
                    ? '⏳ 即将售罄'
                    : dish.status === 'offline'
                      ? '❌ 已下架'
                      : `📦 ${dish.stock} 份`
                }}
              </span>
            </div>
          </div>
          <div class="dish-actions">
            <el-button type="primary" size="small" @click="editDish(dish)"> ✏️ 编辑 </el-button>
            <el-button
              :type="dish.status === 'online' ? 'warning' : 'success'"
              size="small"
              @click="toggleDishStatus(dish)"
            >
              {{ dish.status === 'online' ? '🔴 下架' : '🟢 上架' }}
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="less">
.quick-actions-card {
  margin-bottom: 24px;

  // 今日菜单
  .today-menu-card {
    padding: 24px;
    border: 2px solid #67c23a;
    border-radius: 12px;
    background-color: #ffffff;
    box-shadow: 0 4px 20px rgba(103, 194, 58, 0.08);

    .menu-header {
      display: flex;
      justify-content: flex-start;
      align-items: center;
      margin-bottom: 28px;
      flex-wrap: wrap;
      gap: 24px;

      // 处理只有标题的情况 (第一行)
      &:has(.card-title) {
        padding-bottom: 16px;
        border-bottom: 1px solid #f0f9eb;
        margin-bottom: 24px;
      }

      .card-title {
        font-size: 20px;
        font-weight: 700;
        margin: 0;
        color: #67c23a;
      }

      .filter-label {
        font-weight: 600;
        margin-right: 12px;
        color: #606266;
        font-size: 14px;
      }

      .filter-section {
        display: flex;
        align-items: center;
        gap: 20px;
        flex-wrap: wrap;

        .menu-filter-tag,
        .menu-status-tag {
          cursor: pointer;
          transition: all 0.3s ease;
          border-radius: 20px;
          margin-right: 12px;
          margin-bottom: 8px;

          &:hover {
            transform: translateY(-2px);
            box-shadow: 0 3px 12px rgba(0, 0, 0, 0.15);
          }

          &.active {
            transform: translateY(-1px);
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.18);
          }
        }
      }
    }

    .menu-table-container {
      margin-bottom: 20px;
    }

    .view-all {
      text-align: right;
      margin-top: 24px;

      .el-button {
        color: #67c23a;
        border-color: #67c23a;
        transition: all 0.3s ease;

        &:hover {
          background-color: #67c23a;
          color: #fff;
          transform: translateX(4px);
        }
      }
    }
  }

  // 菜品列表样式
  .dishes-card {
    margin-bottom: 24px;
    padding: 24px;
    border: 2px solid #67c23a;
    border-radius: 12px;
    background-color: #ffffff;
    box-shadow: 0 4px 20px rgba(103, 194, 58, 0.08);
    border-top: none;
    border-top-left-radius: 0;
    border-top-right-radius: 0;

    .dish-list {
      margin-bottom: 20px;

      .dish-item {
        padding: 20px;
        border: 2px solid #f0f9eb;
        border-radius: 10px;
        margin-bottom: 16px;
        background-color: #fff;
        transition: all 0.3s ease;
        display: flex;
        align-items: flex-start;
        gap: 16px;
        overflow: hidden;

        &:hover {
          box-shadow: 0 4px 16px rgba(103, 194, 58, 0.12);
          border-color: #67c23a;
          transform: translateY(-4px);
        }

        .dish-cover {
          font-size: 48px;
          width: 90px;
          height: 90px;
          display: flex;
          align-items: center;
          justify-content: center;
          background: linear-gradient(135deg, #67c23a, #eaf5ec);
          border-radius: 10px;
          flex-shrink: 0;
          color: #fff;
          box-shadow: 0 2px 8px rgba(103, 194, 58, 0.2);
          transition: all 0.3s ease;
        }

        &:hover .dish-cover {
          transform: scale(1.1);
        }

        .dish-info {
          flex: 1;

          .dish-name {
            display: flex;
            align-items: center;
            gap: 12px;
            margin-bottom: 10px;

            .name {
              font-size: 18px;
              font-weight: 600;
              color: #303133;
            }
          }

          .dish-desc {
            font-size: 14px;
            color: #606266;
            margin-bottom: 14px;
            line-height: 1.6;
          }

          .dish-stats {
            display: flex;
            flex-wrap: wrap;
            gap: 20px;
            font-size: 14px;
            color: #606266;

            .dish-category {
              background-color: #eaf5ec;
              color: #67c23a;
              padding: 4px 12px;
              border-radius: 6px;
              font-size: 12px;
              font-weight: 500;
            }

            .dish-price {
              color: #e6a23c;
              font-weight: 600;
              font-size: 16px;
            }

            .dish-stock {
              font-size: 13px;
              font-weight: 500;

              &.stock-almost {
                color: #f59f00;
              }

              &.stock-off {
                color: #f56c6c;
              }
            }
          }
        }

        .dish-actions {
          display: flex;
          flex-direction: column;
          gap: 10px;
          flex-shrink: 0;

          .el-button {
            width: 90px;
            transition: all 0.3s ease;

            &:hover {
              transform: translateY(-2px);
            }
          }
        }
      }
    }
  }
}
</style>
