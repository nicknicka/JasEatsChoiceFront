<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Document,
  CircleCheck,
  Dish,
  Clock,
  Sunny,
  Moon,
  Coffee,
  FolderOpened,
  ArrowRight,
  Edit,
  CircleClose,
  CircleCheckFilled
} from '@element-plus/icons-vue'
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
const menuCategories = ref(['全部', '早餐', '午餐', '晚餐', '加餐'])

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
  online: { text: '在售', type: 'success' },
  almost_sold: { text: '即将售罄', type: 'warning' },
  offline: { text: '下架', type: 'danger' }
}

// 统计数据
const menuStatistics = computed(() => {
  const total = todayMenus.value.length
  const online = todayMenus.value.filter(m => m.status === 'online').length
  const totalDishes = todayMenus.value.reduce((sum, menu) => sum + (menu.dishes || 0), 0)
  const latestUpdate = todayMenus.value.length > 0
    ? todayMenus.value.reduce((latest, menu) => {
        return !latest || (menu.updateTime && menu.updateTime > latest) ? menu.updateTime : latest
      }, '')
    : '暂无'

  return {
    total,
    online,
    totalDishes,
    latestUpdate
  }
})

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

// 格式化时间显示
const formatTime = (timeStr) => {
  if (!timeStr) return '暂无'
  // 只返回时间部分 HH:mm
  const parts = timeStr.split(' ')
  if (parts.length >= 2) {
    return parts[1].substring(0, 5)
  }
  return timeStr.substring(0, 5)
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
  <div class="today-menu-container">
    <!-- 统计概览卡片 -->
    <div class="statistics-overview">
      <div class="stat-card stat-total">
        <div class="stat-icon"><el-icon :size="28"><Document /></el-icon></div>
        <div class="stat-content">
          <div class="stat-value">{{ menuStatistics.total }}</div>
          <div class="stat-label">总菜单</div>
        </div>
      </div>
      <div class="stat-card stat-online">
        <div class="stat-icon"><el-icon :size="28"><CircleCheck /></el-icon></div>
        <div class="stat-content">
          <div class="stat-value">{{ menuStatistics.online }}</div>
          <div class="stat-label">上架中</div>
        </div>
      </div>
      <div class="stat-card stat-dishes">
        <div class="stat-icon"><el-icon :size="28"><Dish /></el-icon></div>
        <div class="stat-content">
          <div class="stat-value">{{ menuStatistics.totalDishes }}</div>
          <div class="stat-label">总菜品</div>
        </div>
      </div>
      <div class="stat-card stat-time">
        <div class="stat-icon"><el-icon :size="28"><Clock /></el-icon></div>
        <div class="stat-content">
          <div class="stat-value-small">{{ menuStatistics.latestUpdate }}</div>
          <div class="stat-label">最近更新</div>
        </div>
      </div>
    </div>

    <!-- 菜单卡片 -->
    <div class="quick-actions-card today-menu-card">
      <div class="menu-header">
        <h3 class="card-title"><el-icon :size="22"><Document /></el-icon> 今日菜单</h3>
        <div class="filter-section">
          <span class="filter-label">分类：</span>
          <el-tag
            v-for="category in menuCategories"
            :key="category"
            effect="plain"
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
            effect="plain"
            class="menu-status-tag"
            :class="[
              { active: activeMenuFilter === status.value },
              status.value === 'online' ? 'tag-online' :
              status.value === 'offline' ? 'tag-offline' :
              status.value === 'draft' ? 'tag-draft' : ''
            ]"
            @click="filterMenus(status.value, 'status')"
            >{{ status.label }}</el-tag
          >
        </div>
      </div>

      <!-- 菜单卡片网格 -->
      <transition name="fade" mode="out-in">
        <div class="menu-grid" :key="activeMenuFilter + activeMenuTypeFilter">
          <div
            v-for="menu in filteredMenus"
            :key="menu.id"
            class="menu-card"
            :class="[
              { selected: selectedMenu?.id === menu.id },
              `status-${menu.status}`
            ]"
            @click="switchMenu(menu)"
          >
            <div class="menu-card-header">
              <div class="menu-icon">
                <el-icon :size="24">
                  <Sunny v-if="menu.name.includes('早餐') || menu.name.includes('午餐')" />
                  <Moon v-else-if="menu.name.includes('晚餐')" />
                  <Coffee v-else-if="menu.name.includes('加餐')" />
                  <Dish v-else />
                </el-icon>
              </div>
              <div class="menu-status-badge">
                <el-icon :size="14">
                  <CircleCheck v-if="menu.status === 'online'" />
                  <CircleClose v-else-if="menu.status === 'offline'" />
                  <Clock v-else />
                </el-icon>
                <span>{{ menuStatusMap[menu.status].text }}</span>
              </div>
            </div>
            <div class="menu-name">{{ menu.name }}</div>
            <div class="menu-info">
              <span class="dishes-count"><el-icon :size="14"><Dish /></el-icon> {{ menu.dishes }} 菜品</span>
              <span class="update-time"><el-icon :size="14"><Clock /></el-icon> {{ formatTime(menu.updateTime) }}</span>
            </div>
            <div class="menu-auto-time" v-if="menu.autoOnline || menu.autoOffline">
              <span v-if="menu.autoOnline"><el-icon :size="12"><ArrowRight /></el-icon> {{ formatTime(menu.autoOnline) }}</span>
              <span v-if="menu.autoOffline"><el-icon :size="12"><ArrowRight /></el-icon> {{ formatTime(menu.autoOffline) }}</span>
            </div>
          </div>
        </div>
      </transition>

      <!-- 空状态 -->
      <div v-if="filteredMenus.length === 0" class="empty-state">
        <div class="empty-icon"><el-icon :size="64"><Dish /></el-icon></div>
        <p class="empty-text">今日暂未设置菜单</p>
      </div>

      <div class="view-all">
        <el-button type="text" @click="navigateToMenu">
          <el-icon :size="18"><ArrowRight /></el-icon> 查看全部菜单
        </el-button>
      </div>
    </div>

    <!-- 菜品列表 -->
    <transition name="slide-up">
      <div v-if="selectedMenu" class="quick-actions-card dishes-card">
        <div class="menu-header">
          <h3 class="card-title">
            <el-icon :size="20"><Dish /></el-icon> {{ selectedMenu.name }} - 菜品列表
            <el-tag size="small" class="menu-tag" effect="plain">
              {{ selectedMenu.dishes }} 道菜品
            </el-tag>
          </h3>
        </div>

        <div class="dish-list">
          <transition-group name="list" tag="div">
            <div
              class="dish-item"
              v-for="dish in currentMenuDishes"
              :key="dish.id"
            >
              <div class="dish-cover">
                <div class="dish-image"><el-icon :size="40"><Dish /></el-icon></div>
              </div>
              <div class="dish-info">
                <div class="dish-name">
                  <span class="name">{{ dish.name }}</span>
                  <el-tag
                    :type="dishStatusMap[dish.status].type"
                    size="small"
                    effect="plain"
                  >
                    {{ dishStatusMap[dish.status].text }}
                  </el-tag>
                </div>

                <div class="dish-desc">
                  {{ dish.description || '美味可口，欢迎品尝' }}
                </div>

                <div class="dish-stats">
                  <span class="dish-category"><el-icon :size="14"><FolderOpened /></el-icon> {{ dish.category }}</span>
                  <span class="dish-price">¥{{ dish.price }}</span>
                  <span
                    class="dish-stock"
                    :class="{
                      'stock-almost': dish.status === 'almost_sold',
                      'stock-off': dish.status === 'offline'
                    }"
                  >
                    <el-icon :size="12">
                      <CircleClose v-if="dish.status === 'offline'" />
                      <Clock v-else-if="dish.status === 'almost_sold'" />
                      <CircleCheck v-else />
                    </el-icon>
                    {{
                      dish.status === 'almost_sold'
                        ? ' 即将售罄'
                        : dish.status === 'offline'
                          ? ' 已下架'
                          : ` ${dish.stock} 份`
                    }}
                  </span>
                </div>
              </div>
              <div class="dish-actions">
                <el-button type="primary" size="small" @click="editDish(dish)">
                  <el-icon :size="14"><Edit /></el-icon> 编辑
                </el-button>
                <el-button
                  :type="dish.status === 'online' ? 'warning' : 'success'"
                  size="small"
                  effect="plain"
                  @click="toggleDishStatus(dish)"
                >
                  <el-icon :size="14">
                    <CircleClose v-if="dish.status === 'online'" />
                    <CircleCheck v-else />
                  </el-icon>
                  {{ dish.status === 'online' ? ' 下架' : ' 上架' }}
                </el-button>
              </div>
            </div>
          </transition-group>
        </div>
      </div>
    </transition>
  </div>
</template>

<style scoped lang="less">
.today-menu-container {
  // 统计概览卡片
  .statistics-overview {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 16px;
    margin-bottom: 24px;

    .stat-card {
      background: #ffffff;
      border-radius: 12px;
      padding: 20px;
      display: flex;
      align-items: center;
      gap: 16px;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
      border: 1px solid #f0f0f0;
      transition: all 0.3s ease;
      cursor: default;

      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
      }

      .stat-icon {
        font-size: 36px;
        width: 60px;
        height: 60px;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 12px;
        background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
        flex-shrink: 0;

        .el-icon {
          color: #909399;
        }
      }

      .stat-content {
        flex: 1;

        .stat-value {
          font-size: 28px;
          font-weight: 700;
          color: #303133;
          line-height: 1;
          margin-bottom: 6px;
        }

        .stat-value-small {
          font-size: 13px;
          font-weight: 500;
          color: #606266;
          line-height: 1.4;
          margin-bottom: 6px;
        }

        .stat-label {
          font-size: 13px;
          color: #909399;
          font-weight: 500;
        }
      }

      &.stat-total .stat-icon {
        background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
      }

      &.stat-online .stat-icon {
        background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
      }

      &.stat-dishes .stat-icon {
        background: linear-gradient(135deg, #fff3e0 0%, #ffe0b2 100%);
      }

      &.stat-time .stat-icon {
        background: linear-gradient(135deg, #f3e5f5 0%, #e1bee7 100%);
      }
    }
  }

  // 今日菜单卡片
  .quick-actions-card {
    margin-bottom: 24px;

    &.today-menu-card {
      padding: 24px;
      border-radius: 16px;
      background: linear-gradient(135deg, #ffffff 0%, #fafbfc 100%);
      box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
      border: 1px solid #e8e8e8;

      .menu-header {
        display: flex;
        justify-content: flex-start;
        align-items: center;
        margin-bottom: 20px;
        flex-wrap: wrap;
        gap: 16px;

        &:first-child {
          padding-bottom: 16px;
          border-bottom: 2px solid #f0f0f0;
        }

        .card-title {
          font-size: 20px;
          font-weight: 700;
          margin: 0;
          color: #303133;
          margin-right: auto;
          display: flex;
          align-items: center;
          gap: 8px;

          .el-icon {
            color: #909399;
          }
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
          gap: 12px;
          flex-wrap: wrap;

          .menu-filter-tag,
          .menu-status-tag {
            cursor: pointer;
            transition: all 0.3s ease;
            border-radius: 20px;
            padding: 6px 16px;
            font-size: 13px;
            font-weight: 500;
            border: 1.5px solid #e0e0e0;
            background: #fafafa;
            color: #606266;

            &:hover {
              transform: translateY(-2px);
              box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
              background: #f0f0f0;
            }

            &.active {
              background: #409eff;
              color: #ffffff;
              border-color: #409eff;
              box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
            }

            &.tag-online {
              &:hover, &.active {
                background: #409eff;
                border-color: #409eff;
                color: #ffffff;
              }
            }

            &.tag-offline {
              &:hover, &.active {
                background: #909399;
                border-color: #909399;
                color: #ffffff;
              }
            }

            &.tag-draft {
              &:hover, &.active {
                background: #e6a23c;
                border-color: #e6a23c;
                color: #ffffff;
              }
            }
          }
        }
      }

      // 菜单卡片网格
      .menu-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
        gap: 16px;
        margin-bottom: 20px;

        .menu-card {
          background: #ffffff;
          border-radius: 12px;
          padding: 18px;
          border: 2px solid #f0f0f0;
          transition: all 0.3s ease;
          cursor: pointer;
          position: relative;
          overflow: hidden;

          &::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            height: 4px;
            background: linear-gradient(90deg, #409eff 0%, #66b1ff 100%);
            opacity: 0;
            transition: opacity 0.3s ease;
          }

          &:hover {
            transform: translateY(-6px);
            box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
            border-color: #409eff;

            &::before {
              opacity: 1;
            }

            .menu-icon {
              transform: scale(1.1) rotate(5deg);
            }
          }

          &.selected {
            border-color: #409eff;
            background: linear-gradient(135deg, #ecf5ff 0%, #ffffff 100%);
            box-shadow: 0 4px 16px rgba(64, 158, 255, 0.2);

            &::before {
              opacity: 1;
            }
          }

          &.status-online {
            border-left: 4px solid #409eff;
          }

          &.status-offline {
            border-left: 4px solid #909399;
          }

          &.status-draft {
            border-left: 4px solid #e6a23c;
          }

          .menu-card-header {
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
            margin-bottom: 12px;

            .menu-icon {
              font-size: 32px;
              width: 50px;
              height: 50px;
              display: flex;
              align-items: center;
              justify-content: center;
              background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
              border-radius: 10px;
              transition: transform 0.3s ease;

              .el-icon {
                color: #909399;
              }
            }

            .menu-status-badge {
              display: flex;
              align-items: center;
              gap: 4px;
              font-size: 12px;
              font-weight: 500;
              padding: 4px 10px;
              border-radius: 12px;
              background: #f5f7fa;

              .el-icon {
                &.el-icon-circle-check {
                  color: #409eff;
                }

                &.el-icon-circle-close {
                  color: #909399;
                }

                &.el-icon-clock {
                  color: #909399;
                }
              }
            }
          }

          .menu-name {
            font-size: 16px;
            font-weight: 600;
            color: #303133;
            margin-bottom: 12px;
            line-height: 1.4;
          }

          .menu-info {
            display: flex;
            flex-wrap: wrap;
            gap: 12px;
            font-size: 13px;
            color: #909399;
            margin-bottom: 8px;

            .dishes-count {
              color: #606266;
              font-weight: 500;
              display: flex;
              align-items: center;
              gap: 4px;

              .el-icon {
                color: #909399;
              }
            }

            .update-time {
              display: flex;
              align-items: center;
              gap: 4px;

              .el-icon {
                color: #909399;
              }
            }
          }

          .menu-auto-time {
            display: flex;
            gap: 12px;
            font-size: 12px;
            color: #909399;
            padding-top: 8px;
            border-top: 1px dashed #e8e8e8;

            span {
              display: flex;
              align-items: center;
              gap: 4px;

              .el-icon {
                color: #909399;
              }
            }
          }
        }
      }

      // 空状态
      .empty-state {
        text-align: center;
        padding: 60px 20px;
        color: #909399;

        .empty-icon {
          font-size: 64px;
          margin-bottom: 16px;
          opacity: 0.5;

          .el-icon {
            color: #c0c4cc;
          }
        }

        .empty-text {
          font-size: 16px;
          margin: 0;
          font-weight: 500;
        }
      }

      .view-all {
        text-align: right;
        margin-top: 20px;
        padding-top: 20px;
        border-top: 1px solid #f0f0f0;

        .el-button {
          color: #409eff;
          font-weight: 600;
          transition: all 0.3s ease;

          &:hover {
            color: #66b1ff;
            transform: translateX(4px);
          }
        }
      }
    }

    // 菜品列表卡片
    .dishes-card {
      margin-bottom: 24px;
      padding: 24px;
      border-radius: 16px;
      background: linear-gradient(135deg, #ffffff 0%, #fafbfc 100%);
      box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
      border: 1px solid #e8e8e8;

      .menu-header {
        margin-bottom: 20px;
        padding-bottom: 16px;
        border-bottom: 2px solid #f0f0f0;

        .card-title {
          font-size: 18px;
          font-weight: 700;
          color: #303133;
          margin: 0;
          display: flex;
          align-items: center;
          gap: 12px;

          .el-icon {
            color: #909399;
          }

          .menu-tag {
            background: linear-gradient(135deg, #e8f4fd 0%, #d0e8ff 100%);
            color: #409eff;
            border: none;
            font-weight: 600;
          }
        }
      }

      .dish-list {
        display: flex;
        flex-direction: column;
        gap: 16px;

        .dish-item {
          display: flex;
          align-items: flex-start;
          gap: 16px;
          padding: 18px;
          background: #ffffff;
          border: 2px solid #f5f5f5;
          border-radius: 12px;
          transition: all 0.3s ease;
          overflow: hidden;

          &::before {
            content: '';
            position: absolute;
            left: 0;
            top: 0;
            bottom: 0;
            width: 4px;
            background: linear-gradient(180deg, #409eff 0%, #66b1ff 100%);
            transform: scaleY(0);
            transition: transform 0.3s ease;
          }

          &:hover {
            transform: translateX(6px);
            box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
            border-color: #409eff;

            &::before {
              transform: scaleY(1);
            }

            .dish-cover .dish-image {
              transform: scale(1.05);
            }
          }

          .dish-cover {
            flex-shrink: 0;

            .dish-image {
              font-size: 48px;
              width: 90px;
              height: 90px;
              display: flex;
              align-items: center;
              justify-content: center;
              background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
              border-radius: 12px;
              transition: transform 0.3s ease;

              .el-icon {
                color: #909399;
              }
            }
          }

          .dish-info {
            flex: 1;
            min-width: 0;

            .dish-name {
              display: flex;
              align-items: center;
              gap: 10px;
              margin-bottom: 10px;

              .name {
                font-size: 17px;
                font-weight: 600;
                color: #303133;
                flex: 1;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
              }
            }

            .dish-desc {
              font-size: 13px;
              color: #909399;
              margin-bottom: 12px;
              line-height: 1.6;
              overflow: hidden;
              text-overflow: ellipsis;
              display: -webkit-box;
              -webkit-line-clamp: 2;
              -webkit-box-orient: vertical;
            }

            .dish-stats {
              display: flex;
              flex-wrap: wrap;
              gap: 16px;
              font-size: 13px;

              .dish-category {
                background: linear-gradient(135deg, #e8f4fd 0%, #d0e8ff 100%);
                color: #409eff;
                padding: 4px 12px;
                border-radius: 6px;
                font-weight: 500;
                display: flex;
                align-items: center;
                gap: 4px;

                .el-icon {
                  color: #409eff;
                }
              }

              .dish-price {
                color: #606266;
                font-weight: 600;
                font-size: 15px;
              }

              .dish-stock {
                font-size: 13px;
                font-weight: 500;
                color: #909399;
                display: flex;
                align-items: center;
                gap: 4px;

                .el-icon {
                  color: inherit;
                }

                &.stock-almost {
                  color: #e6a23c;

                  .el-icon {
                    color: #e6a23c;
                  }
                }

                &.stock-off {
                  color: #909399;

                  .el-icon {
                    color: #909399;
                  }
                }
              }
            }
          }

          .dish-actions {
            display: flex;
            flex-direction: column;
            gap: 8px;
            flex-shrink: 0;

            .el-button {
              min-width: 80px;
              border-radius: 8px;
              font-weight: 500;
              transition: all 0.3s ease;

              &.el-button--primary {
                background-color: #409eff;
                border-color: #409eff;

                &:hover {
                  background-color: #66b1ff;
                  border-color: #66b1ff;
                  transform: translateY(-2px);
                }
              }

              &.el-button--warning {
                color: #909399;
                border-color: #dcdfe6;
                background: #fff;

                &:hover {
                  color: #909399;
                  background-color: #f5f7fa;
                  border-color: #c0c4cc;
                  transform: translateY(-2px);
                }
              }

              &.el-button--success {
                background-color: #409eff;
                border-color: #409eff;
                color: #ffffff;

                &:hover {
                  background-color: #66b1ff;
                  border-color: #66b1ff;
                  transform: translateY(-2px);
                }
              }
            }
          }
        }
      }
    }
  }
}

// 动画效果
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-up-enter-active {
  transition: all 0.4s ease;
}

.slide-up-leave-active {
  transition: all 0.3s ease;
}

.slide-up-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.slide-up-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

.list-enter-active,
.list-leave-active {
  transition: all 0.3s ease;
}

.list-enter-from {
  opacity: 0;
  transform: translateX(-20px);
}

.list-leave-to {
  opacity: 0;
  transform: translateX(20px);
}

// 响应式设计
@media (max-width: 768px) {
  .today-menu-container {
    .statistics-overview {
      grid-template-columns: repeat(2, 1fr);
      gap: 12px;

      .stat-card {
        padding: 16px;

        .stat-icon {
          font-size: 28px;
          width: 48px;
          height: 48px;
        }

        .stat-content {
          .stat-value {
            font-size: 22px;
          }

          .stat-value-small {
            font-size: 11px;
          }

          .stat-label {
            font-size: 12px;
          }
        }
      }
    }

    .quick-actions-card.today-menu-card {
      .menu-grid {
        grid-template-columns: 1fr;
      }
    }

    .dishes-card .dish-list .dish-item {
      flex-direction: column;

      .dish-actions {
        flex-direction: row;
        width: 100%;

        .el-button {
          flex: 1;
        }
      }
    }
  }
}
</style>
