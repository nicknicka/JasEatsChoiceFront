<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElTimePicker, ElSelect, ElOption, ElInput, ElMessageBox } from 'element-plus'
import { CircleCheck, CirclePlus, CircleClose, Document, Grid, Clock, Switch } from '@element-plus/icons-vue'
import CommonBackButton from '../../components/common/CommonBackButton.vue'
import axios from 'axios'
import { API_CONFIG } from '../../config/index.js'
import { useAuthStore } from '../../store/authStore'
import dayjs from 'dayjs'


const route = useRoute()
const router = useRouter()

// 菜单基本信息
const menuInfo = ref({})

// 菜单状态映射
const menuStatusMap = {
  online: { text: '上架中', type: 'success' },
  draft: { text: '草稿', type: 'warning' },
  offline: { text: '下架中', type: 'danger' }
}

// 加载状态
const loading = ref(false)

// 菜品列表
const dishesList = ref([])

// 搜索关键词
const searchKeyword = ref('')

// 菜品状态映射（与DishManagement.vue保持一致）
const dishStatusMap = {
  online: { text: '🟢 在售', type: 'success' },
  almost_sold: { text: '🟡 即将售罄', type: 'warning' },
  offline: { text: '🔴 下架', type: 'danger' }
}

// 页面加载
onMounted(async () => {
  // 从路由参数获取菜单ID
  const menuId = route.query.menuId
  loading.value = true
  if (!menuId) {
    ElMessage.error('无效的菜单ID')
    router.push('/merchant/home/menu')
    return
  }

  try {
    // 从authStore获取商家ID
    const authStore = useAuthStore()
    const merchantId = authStore.merchantId
    if (!merchantId) {
      ElMessage.error('未检测到商家ID，请重新登录')
      router.push('/merchant/login')
      return
    }

    // 获取菜单详情
    const menuResponse = await axios.get(`${API_CONFIG.baseURL}${API_CONFIG.merchant.menu.replace('{merchantId}', merchantId)}/${menuId}`)
    if (menuResponse.data && menuResponse.data.success) {
      menuInfo.value = menuResponse.data.data
      // 格式化时间
      if (menuInfo.value.autoOnline) {
        menuInfo.value.autoOnline = dayjs(menuInfo.value.autoOnline).format('HH:mm:ss')
      }
      if (menuInfo.value.autoOffline) {
        menuInfo.value.autoOffline = dayjs(menuInfo.value.autoOffline).format('HH:mm:ss')
      }
    }

    // 获取所有菜品数据
    const dishesResponse = await axios.get(`${API_CONFIG.baseURL}${API_CONFIG.dish.list}`)
    if (dishesResponse.data && dishesResponse.data.success) {
      availableDishes.value = dishesResponse.data.data.map(dish => ({
        ...dish,
        statusText: dishStatusMap[dish.status] ? dishStatusMap[dish.status].text : '🔴 下架'
      }))
    }

    // 获取菜单关联的菜品
    const menuDishesResponse = await axios.get(`${API_CONFIG.baseURL}${API_CONFIG.merchant.menu.replace('{merchantId}', merchantId)}/${menuId}/dishes`)
    if (menuDishesResponse.data && menuDishesResponse.data.success) {
      dishesList.value = menuDishesResponse.data.data.map(dish => ({
        ...dish,
        statusText: dishStatusMap[dish.status] ? dishStatusMap[dish.status].text : '🔴 下架'
      }))
    }
  } catch (error) {
    console.error('加载菜单数据失败:', error)
    ElMessage.error('加载菜单数据失败')
    router.push('/merchant/home/menu')
  } finally {
    loading.value = false
  }
})

// 保存菜单
const saveMenu = async (saveType) => {
  loading.value = true
  // 根据保存类型更新菜单状态
  switch (saveType) {
    case 'online':
      menuInfo.value.status = 'online'
      break
    case 'offline':
      menuInfo.value.status = 'offline'
      break
    case 'draft':
      menuInfo.value.status = 'draft'
      break
  }

  // 表单验证
  if (!menuInfo.value.name.trim()) {
    ElMessage.warning('请填写菜单名称')
    return
  }

  // 验证自动时间
  if (menuInfo.value.autoOnline && menuInfo.value.autoOffline) {
    const onlineTime = dayjs(menuInfo.value.autoOnline)
    const offlineTime = dayjs(menuInfo.value.autoOffline)
    if (offlineTime.isBefore(onlineTime)) {
      ElMessage.warning('自动下架时间必须晚于自动上架时间')
      return
    }
  }

  try {
    // 从authStore获取商家ID
    const authStore = useAuthStore()
    const merchantId = authStore.merchantId
    if (!merchantId) {
      ElMessage.error('未检测到商家ID，请重新登录')
      router.push('/merchant/login')
      return
    }

    // 从路由参数获取菜单ID
    const menuId = route.query.menuId
    if (!menuId) {
      ElMessage.error('无效的菜单ID')
      router.push('/merchant/home/menu')
      return
    }

    // 准备保存的数据
    const saveData = {
      ...menuInfo.value,
      dishes: dishesList.value.map(dish => dish.id) // 只保存菜品ID
    }

    // 更新菜单
    const response = await axios.put(`${API_CONFIG.baseURL}${API_CONFIG.merchant.menu.replace('{merchantId}', merchantId)}/${menuId}`, saveData)
    console.log('resonse data', response) ;
    ElMessage.success('菜单保存成功')

    // 跳回菜单管理页面
    router.push('/merchant/home/menu')
  } catch (error) {
    console.error('保存菜单失败:', error)
    ElMessage.error('保存菜单失败')
  } finally {
    loading.value = false
  }
}

// 移除菜品
const removeDish = (dish) => {
  const index = dishesList.value.findIndex((item) => item.id === dish.id)
  if (index !== -1) {
    dishesList.value.splice(index, 1)
    ElMessage.success('菜品已移除')
  }
}

// 可用菜品数据
const availableDishes = ref([])

// 过滤后的可用菜品（用于搜索）
const filteredAvailableDishes = computed(() => {
  if (!searchKeyword.value) {
    return availableDishes.value
  }
  const keyword = searchKeyword.value.toLowerCase()
  return availableDishes.value.filter(dish => {
    return dish.name.toLowerCase().includes(keyword) || dish.description?.toLowerCase().includes(keyword) || dish.category?.toLowerCase().includes(keyword)
  })
})

// 添加菜品对话框
const showAddDishDialog = ref(false)
const selectedDish = ref(null)

// 批量关联菜品对话框
const showBatchAssociateDialog = ref(false)
const selectedDishesBatch = ref([])

// 添加菜品
const addDish = () => {
  if (selectedDish.value) {
    // 检查菜品是否已在菜单中
    const isExist = dishesList.value.some((dish) => dish.id === selectedDish.value.id)
    if (!isExist) {
      dishesList.value.push({ ...selectedDish.value })
      ElMessage.success('菜品已添加')
    } else {
      ElMessage.warning('该菜品已在菜单中')
    }
    // 重置状态
    showAddDishDialog.value = false
    selectedDish.value = null
  }
}

// 设置自动上架时间为当前时间 + 1小时
const setAutoOnlineTime = () => {
  menuInfo.value.autoOnline = dayjs().add(1, 'hour').format('HH:mm:ss')
}

// 设置自动下架时间为当前时间 + 3小时
const setAutoOfflineTime = () => {
  menuInfo.value.autoOffline = dayjs().add(3, 'hour').format('HH:mm:ss')
}

// 处理取消编辑
const handleCancelEdit = () => {
  ElMessageBox.confirm('确定要取消编辑吗？未保存的更改将丢失。', '提示', {
    confirmButtonText: '确定取消',
    cancelButtonText: '继续编辑',
    type: 'warning'
  })
    .then(() => {
      // 导航回菜单管理页面
      router.push('/merchant/home/menu')
    })
    .catch(() => {
      // 用户取消了取消操作，继续编辑
    })
}

// 批量关联菜品
const batchAssociateDishes = () => {
  if (selectedDishesBatch.value.length > 0) {
    let addedCount = 0
    let existingCount = 0

    selectedDishesBatch.value.forEach((dish) => {
      const isExist = dishesList.value.some((existingDish) => existingDish.id === dish.id)
      if (!isExist) {
        dishesList.value.push({ ...dish })
        addedCount++
      } else {
        existingCount++
      }
    })

    // 显示结果信息
    const messages = []
    if (addedCount > 0) messages.push(`${addedCount} 个菜品已成功关联`)
    if (existingCount > 0) messages.push(`${existingCount} 个菜品已在菜单中`)

    if (messages.length > 0) {
      ElMessage.success(messages.join('；'))
    }

    // 重置状态
    showBatchAssociateDialog.value = false
    selectedDishesBatch.value = []
  }
}
</script>

<template>
  <div class="menu-edit-container">
    <div class="menu-edit-header">
      <div class="header-left">
        <h3 class="page-title">编辑菜单</h3>
        <CommonBackButton
          type="text"
          text="取消编辑"
          :useRouterBack="false"
          @click="handleCancelEdit"
          class="back-btn"
        />
      </div>
    </div>

    <div class="menu-edit-content">
      <!-- 菜单基本信息 -->
      <div class="menu-info-section">
        <h4 class="section-title">📝 菜单基本信息</h4>
        <div class="info-item">
          <span class="info-label"><el-icon><Document /></el-icon> 菜单名称</span>
          <el-input
            v-model="menuInfo.name"
            placeholder="请输入菜单名称"
            style="width: 300px"
            clearable
          ></el-input>
        </div>

        <div class="info-item">
          <span class="info-label"><el-icon><Clock /></el-icon> 自动上架时间</span>
          <el-time-picker
            v-model="menuInfo.autoOnline"
            type="fixed-time"
            format="HH:mm:ss"
            value-format="HH:mm:ss"
            placeholder="选择自动上架时间"
            style="width: 200px"
          ></el-time-picker>
        </div>
        <div class="info-item">
          <span class="info-label"><el-icon><Clock /></el-icon> 自动下架时间</span>
          <el-time-picker
            v-model="menuInfo.autoOffline"
            type="fixed-time"
            format="HH:mm:ss"
            value-format="HH:mm:ss"
            placeholder="选择自动下架时间"
            style="width: 200px"
          ></el-time-picker>
        </div>
        <div class="info-item">
          <span class="info-label"><el-icon><Switch /></el-icon> 菜单状态</span>
          <el-select
            v-model="menuInfo.status"
            placeholder="选择菜单状态"
            style="width: 200px"
            clearable
          >
            <el-option
              v-for="(status, key) in menuStatusMap"
              :key="key"
              :value="key"
              :label="status.text"
            >
              <template #label>
                <el-icon>
                  <CircleCheck v-if="key === 'online'" />
                  <CirclePlus v-else-if="key === 'draft'" />
                  <CircleClose v-else />
                </el-icon>
                {{ status.text }}
              </template>
            </el-option>
          </el-select>
        </div>
        <div class="info-item">
          <span class="info-label"><el-icon><Edit /></el-icon> 菜单描述</span>
          <el-input
            v-model="menuInfo.description"
            placeholder="请输入菜单描述"
            style="width: 500px"
            type="textarea"
            :rows="4"
            clearable
          ></el-input>
        </div>
      </div>

      <!-- 菜品管理 -->
      <div class="dishes-section">
        <h4 class="section-title">🍴 菜品管理</h4>
        <div class="dishes-header">
          <el-input
            v-model="searchKeyword"
            placeholder="输入菜品名称..."
            style="width: 250px"
            class="dishes-search"
            clearable
          />
          <el-button type="primary" size="small" @click="showAddDishDialog = true">
            添加菜品
          </el-button>
          <el-button type="info" size="small" @click="showBatchAssociateDialog = true">
            批量关联菜品
          </el-button>
        </div>
        <div class="dishes-list">
          <div v-for="dish in dishesList" :key="dish.id" class="dish-item">
            <span class="dish-info">
              <span class="dish-name">{{ dish.name }}</span>
              <span class="dish-price">¥{{ dish.price }}</span>
              <el-tag
                :type="dishStatusMap[dish.status]?.type || 'danger'"
                size="small"
                class="status-tag"
              >
                {{ dish.statusText }}
              </el-tag>
            </span>
            <el-button type="danger" size="small" @click="removeDish(dish)">
              移除
            </el-button>
          </div>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="action-buttons">
        <el-button type="success" @click="saveMenu('online')">保存菜单并上架</el-button>
        <el-button type="warning" @click="saveMenu('offline')">保存菜单并下架</el-button>
        <el-button type="info" @click="saveMenu('draft')">保存为草稿</el-button>
      </div>

      <!-- 添加菜品对话框 -->
      <el-dialog
        v-model="showAddDishDialog"
        title="添加菜品"
        width="600px"
        top="10%"
        transition="dialog-fade"
      >
        <div class="dialog-content">
          <el-select
            v-model="selectedDish"
            placeholder="请选择要添加的菜品"
            style="width: 100%"
            filterable
            clearable
          >
            <el-option
              v-for="dish in filteredAvailableDishes"
              :key="dish.id"
              :label="`${dish.name} - ¥${dish.price} ${dish.statusText}`"
              :value="dish"
            />
          </el-select>
        </div>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="showAddDishDialog = false">取消</el-button>
            <el-button type="primary" @click="addDish">确定添加</el-button>
          </span>
        </template>
      </el-dialog>

      <!-- 批量关联菜品对话框 -->
      <el-dialog
        v-model="showBatchAssociateDialog"
        title="批量关联菜品"
        width="600px"
        top="10%"
        transition="dialog-fade"
      >
        <div class="dialog-content">
          <el-select
            v-model="selectedDishesBatch"
            multiple
            placeholder="请选择要关联的菜品"
            style="width: 100%"
            filterable
            clearable
            collapse-tags
            collapse-tags-tooltip
          >
            <el-option
              v-for="dish in filteredAvailableDishes"
              :key="dish.id"
              :label="`${dish.name} - ¥${dish.price} ${dish.statusText}`"
              :value="dish"
            />
          </el-select>
        </div>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="showBatchAssociateDialog = false">取消</el-button>
            <el-button type="primary" @click="batchAssociateDishes">确定关联</el-button>
          </span>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<style scoped lang="less">
// 时间设置按钮样式
.time-set-btn {
  color: #1890ff;
  font-weight: 500;

  &:hover {
    color: #40a9ff;
    text-decoration: underline;
  }
}

// 菜品状态标签样式
.status-tag {
  margin-left: 8px;
  font-size: 12px;
  border-radius: 6px;
  padding: 2px 8px;
}

// 菜品信息样式
.dish-info {
  display: flex;
  align-items: center;
  gap: 12px;

  .dish-name {
    font-weight: 600;
    color: #2d3748;
    font-size: 14px;
  }

  .dish-price {
    color: #e6a23c;
    font-weight: 600;
    font-size: 14px;
  }
}

.menu-edit-container {
  padding: 0 20px 20px 20px;

  .menu-edit-header {
    background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
    padding: 20px;
    margin: -20px -20px 20px -20px;
    border-radius: 0 0 12px 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

    .page-title {
      font-size: 24px;
      font-weight: 700;
      margin: 0;
      color: #1976d2;
    }
  }

  .menu-edit-content {
    .menu-info-section,
    .dishes-section {
      background-color: #fff;
      border-radius: 12px;
      padding: 24px;
      margin-bottom: 24px;
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
    }

    .section-title {
      font-size: 18px;
      font-weight: 600;
      margin-bottom: 20px;
      color: #333;
      padding-bottom: 10px;
      border-bottom: 2px solid #e0e0e0;
    }

    .info-item {
      display: flex;
      align-items: center;
      gap: 16px;
      margin-bottom: 20px;

      .info-label {
        color: #555;
        width: 130px;
        font-weight: 500;
        font-size: 14px;
      }

      /* 输入框悬浮效果优化 */
      :deep(.el-input) {
        .el-input__wrapper {
          transition: all 0.3s ease;
          border-radius: 6px;

          &:hover {
            border-color: #409eff;
            box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
          }

          &.is-focus {
            border-color: #409eff;
            box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
          }
        }
      }

      /* 选择框悬浮效果优化 */
      :deep(.el-select) {
        .el-input {
          .el-input__wrapper {
            transition: all 0.3s ease;
            border-radius: 6px;

            &:hover {
              border-color: #409eff;
              box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
            }

            &.is-focus {
              border-color: #409eff;
              box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
            }
          }
        }
      }

      /* 时间选择器悬浮效果优化 */
      :deep(.el-time-picker) {
        .el-input {
          .el-input__wrapper {
            transition: all 0.3s ease;
            border-radius: 6px;

            &:hover {
              border-color: #409eff;
              box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
            }

            &.is-focus {
              border-color: #409eff;
              box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
            }
          }
        }
      }
    }

    .dishes-header {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 16px;

      .dishes-search {
        margin-right: auto;

        /* 搜索输入框悬浮效果优化 */
        :deep(.el-input) {
          .el-input__wrapper {
            transition: all 0.3s ease;
            border-radius: 6px;

            &:hover {
              border-color: #409eff;
              box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
            }

            &.is-focus {
              border-color: #409eff;
              box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
            }
          }
        }
      }
    }

    .dishes-list {
      .dish-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 16px;
        border: none;
        border-radius: 8px;
        margin-bottom: 12px;
        background-color: #ffffff;
        transition: all 0.3s ease;
        box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);

        &:hover {
          box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
        }
      }
    }

    .action-buttons {
      display: flex;
      gap: 16px;
    }
  }
}

// 对话框样式
:deep(.el-dialog) {
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

:deep(.el-dialog__header) {
  border-bottom: 1px solid #f1f3f5;
  padding: 20px 24px;
  border-radius: 12px 12px 0 0;
}

:deep(.el-dialog__title) {
  color: #495057;
  font-weight: 600;
}

:deep(.el-dialog__body) {
  padding: 24px;
  background-color: #fafbfc;
}

:deep(.el-dialog__footer) {
  border-top: 1px solid #f1f3f5;
  padding: 16px 24px;
  border-radius: 0 0 12px 12px;
  background-color: #ffffff;
}

.dialog-footer {
  text-align: right;
}

</style>
