<script setup>
import { ref, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'
import { API_CONFIG } from '../../config/index.js'
// 导入authStore
import { useAuthStore } from '../../store/authStore'
// 导入图标
import { Search, Plus, CircleCheck, CircleClose, Delete } from '@element-plus/icons-vue'
import {
  Goods as GoodsIcon,
  Money as MoneyIcon,
  Folder as FolderIcon,
  GoodsFilled as StockIcon,
  SwitchButton as StatusIcon,
  List as IngredientsIcon,
  Document as DetailsIcon,
  Flame as FlameIcon
} from '@element-plus/icons-vue'


// 菜品数据
const dishesList = ref([])

const loading = ref(false)
const searchKeyword = ref('')
const selectedDishes = ref([])

// 分页参数
const currentPage = ref(1)
const pageSize = ref(10)
const filteredDishes = ref([])
const paginatedDishes = ref([]) // 分页后的菜品数据

// 更新分页数据
const updatePagination = () => {
  const startIndex = (currentPage.value - 1) * pageSize.value
  const endIndex = startIndex + pageSize.value
  paginatedDishes.value = filteredDishes.value.slice(startIndex, endIndex)
}
// 三态全选复选框的状态：0=未选择，1=部分选择，2=全选
// const selectAllState = ref(0); // 不再需要这个状态变量，直接通过计算获得
// 页面加载时初始化
onMounted(() => {
  loading.value = true
  // 从authStore获取商家ID
  const authStore = useAuthStore()
  const merchantId = authStore.merchantId
  if (!merchantId) {
    ElMessage.error('未检测到商家ID，请重新登录')
    loading.value = false
    return
  }

  // 从API获取菜品数据
  axios
    .get(`${API_CONFIG.baseURL}${API_CONFIG.dish.list}`, {
      params: {
        merchantId: merchantId
      }
    })
    .then((response) => {
      console.log('菜品响应数据:', response)
      if (response.data && response.data.code === "200") {
        // 预处理菜品数据，确保所有菜品都有有效的状态和时间格式
        const processedDishes = response.data.data.map(dish => {
          // 转换时间格式为 yyyy-MM-dd HH:mm:ss
          if (dish.createTime) {
            dish.createTime = new Date(dish.createTime).toLocaleString('zh-CN', {
              year: 'numeric',
              month: '2-digit',
              day: '2-digit',
              hour: '2-digit',
              minute: '2-digit',
              second: '2-digit'
            })
          }
          if (dish.updateTime) {
            dish.updateTime = new Date(dish.updateTime).toLocaleString('zh-CN', {
              year: 'numeric',
              month: '2-digit',
              day: '2-digit',
              hour: '2-digit',
              minute: '2-digit',
              second: '2-digit'
            })
          }

          // 确保price显示为正确的数字格式
          if (dish.price && typeof dish.price === 'object') {
            dish.price = dish.price.toString()
          }

          // 处理库存空值情况
          if (dish.stock == null || dish.stock === '') {
            dish.stock = 0
          }

          // 优化分类字段显示
          if (dish.category && dish.category.startsWith('category_')) {
            // 将 category_1 转换为 分类1
            dish.category = `分类${dish.category.replace('category_', '')}`
          }

          // 将食材 JSON 字符串解析为对象
          if (dish.ingredients && typeof dish.ingredients === 'string') {
            try {
              dish.ingredients = JSON.parse(dish.ingredients)
            } catch (error) {
              console.error('解析食材信息失败:', error)
              dish.ingredients = { mandatory: [], optional: [] }
            }
          }

          // 将后端的 calorie 字段映射到前端的 totalCalories 字段
          if (dish.calorie !== undefined) {
            dish.totalCalories = dish.calorie
          }

          return dish
        })
        dishesList.value = processedDishes
        filteredDishes.value = [...dishesList.value] // 更新筛选后的菜品
        updatePagination() // 初始化分页数据
      }
    })
    .catch((error) => {
      console.error('加载菜品失败:', error)
      ElMessage.error('加载菜品失败')
    })
    .finally(() => {
      loading.value = false
    })
})

// 筛选菜品 - 修复重复声明

// 更新筛选
const updateFilter = () => {
  filteredDishes.value = dishesList.value.filter((dish) => {
    // 搜索筛选
    if (
      searchKeyword.value &&
      !dish.name.includes(searchKeyword.value) &&
      !dish.category.includes(searchKeyword.value)
    ) {
      return false
    }

    return true
  })

  // 重置到第一页
  currentPage.value = 1

  // 更新分页数据
  updatePagination()
}


// 编辑菜品
const editDish = (dish) => {
  openEditDishDialog(dish)
}

// 保存编辑后的菜品
const saveEditedDish = () => {
  // 简单的表单验证
  if (!editDishForm.value.name.trim()) {
    ElMessage.warning('请填写菜品名称')
    return
  }

  // 准备请求数据，将 ingredients 对象序列化为 JSON 字符串，并将 totalCalories 映射为 calorie
  const requestData = {
    ...editDishForm.value,
    calorie: editDishForm.value.totalCalories,
    ingredients: JSON.stringify(editDishForm.value.ingredients)
  }
  // 删除不需要的 totalCalories 字段
  delete requestData.totalCalories

  // 发送后端请求
  axios.put(`${API_CONFIG.baseURL}${API_CONFIG.dish.detail}${requestData.id}`, requestData)
    .then((response) => {
      if (response.status === 200 && response.data && response.data.success) {
        // 从后端返回中获取更新后的菜品数据
        const updatedDish = response.data.data

        // 更新本地菜品列表
        const index = dishesList.value.findIndex((item) => item.id === updatedDish.id)
        if (index !== -1) {
          dishesList.value[index] = updatedDish
          updateFilter()
          editDishDialogVisible.value = false
          ElMessage.success('菜品已更新')
        }
      } else {
        ElMessage.error(response.data?.message || '菜品更新失败')
      }
    })
    .catch((error) => {
      console.error('更新菜品失败:', error)
      ElMessage.error('网络错误，菜品更新失败')
    })
}

// 切换菜品状态（上架/下架）
const toggleDishStatus = (dish) => {
  const newStatus = dish.status === 'online' ? 'offline' : 'online'
  const statusText = newStatus === 'online' ? '上架' : '下架'

  ElMessageBox.confirm(`确定要将该菜品${statusText}吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => {
      dish.status = newStatus
      ElMessage.success(`菜品已${statusText}`)
    })
    .catch(() => {
      ElMessage.info('已取消操作')
    })
}

// 删除菜品
const deleteDish = (dish) => {
  ElMessageBox.confirm('确定要删除该菜品吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => {
      const index = dishesList.value.findIndex((item) => item.id === dish.id)
      if (index !== -1) {
        dishesList.value.splice(index, 1)
        updateFilter()
        ElMessage.success('菜品已删除')
      }
    })
    .catch(() => {
      ElMessage.info('已取消删除')
    })
}

// 批量操作
const batchOperation = (operation) => {
  if (selectedDishes.value.length === 0) {
    ElMessage.warning('请先选择菜品')
    return
  }

  // 批量操作确认对话框
  const getConfirmMessage = () => {
    switch (operation) {
      case 'online':
        return '确定要将所选菜品批量上架吗？'
      case 'offline':
        return '确定要将所选菜品批量下架吗？'
      case 'delete':
        return '确定要删除所选菜品吗？'
      default:
        return '确定要执行批量操作吗？'
    }
  }

  const getSuccessMessage = () => {
    switch (operation) {
      case 'online':
        return '批量上架成功'
      case 'offline':
        return '批量下架成功'
      case 'delete':
        return '批量删除成功'
      default:
        return '批量操作成功'
    }
  }

  // 显示确认对话框
  ElMessageBox.confirm(getConfirmMessage(), '操作确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => {
      // 执行批量操作
      switch (operation) {
        case 'online':
          selectedDishes.value.forEach((dish) => {
            dish.status = 'online'
          })
          break
        case 'offline':
          selectedDishes.value.forEach((dish) => {
            dish.status = 'offline'
          })
          break
        case 'delete':
          dishesList.value = dishesList.value.filter((dish) => !selectedDishes.value.includes(dish))
          break
      }

      // 更新筛选和选择状态
      updateFilter()

      // 删除操作需要特殊处理选择状态
      if (operation !== 'delete') {
        selectedDishes.value = []
      } else {
        selectedDishes.value = []
      }

      // 显示操作成功消息
      ElMessage.success(getSuccessMessage())

      // 强制更新界面，确保全选状态正确更新
      setTimeout(() => {
        updateFilter()
      }, 0)
    })
    .catch(() => {
      // 用户取消操作
      ElMessage.info('已取消批量操作')
    })
}

// 新增菜品对话框
const addDishDialogVisible = ref(false)

// 食材数据已移除，改为直接输入

// 新必选食材输入
const newMandatoryIngredient = ref('')

// 新可选食材输入
const newOptionalIngredient = ref('')

// 新菜品表单数据
const newDish = ref({
  name: '',
  price: 0,
  category: '主食',
  stock: 100,
  ingredients: {
    mandatory: [], // 必选食材改为字符串数组
    optional: [] // 可选食材改为字符串数组
  },
  totalCalories: 0 // 总卡路里
})

// 添加必选食材
const addMandatoryIngredient = () => {
  if (newMandatoryIngredient.value.trim()) {
    const ingredient = newMandatoryIngredient.value.trim()
    // 检查重复
    if (!newDish.value.ingredients.mandatory.includes(ingredient)) {
      newDish.value.ingredients.mandatory.push(ingredient)
      newMandatoryIngredient.value = ''
      calculateTotalCalories()
    } else {
      ElMessage.warning('该必选食材已存在')
    }
  }
}

// 添加可选食材
const addOptionalIngredient = () => {
  if (newOptionalIngredient.value.trim()) {
    const ingredient = newOptionalIngredient.value.trim()
    // 检查重复
    if (!newDish.value.ingredients.optional.includes(ingredient)) {
      newDish.value.ingredients.optional.push(ingredient)
      newOptionalIngredient.value = ''
      calculateTotalCalories()
    } else {
      ElMessage.warning('该可选食材已存在')
    }
  }
}

// 删除必选食材
const removeMandatoryIngredient = (index) => {
  newDish.value.ingredients.mandatory.splice(index, 1)
  calculateTotalCalories()
}

// 删除可选食材
const removeOptionalIngredient = (index) => {
  newDish.value.ingredients.optional.splice(index, 1)
  calculateTotalCalories()
}

// 计算总卡路里
const calculateTotalCalories = () => {
  // 由于改为直接输入食材名称，暂时简化卡路里计算
  // 实际项目中可以根据食材名称匹配数据库中的卡路里数据
  // 或添加输入框让商家直接输入卡路里
  newDish.value.totalCalories = 0
}

// 编辑菜品对话框
const editDishDialogVisible = ref(false)

// 编辑菜品表单数据
const editDishForm = ref({
  ingredients: {
    mandatory: [], // 必选食材改为字符串数组
    optional: [] // 可选食材改为字符串数组
  },
  totalCalories: 0
})

// 新必选食材输入（编辑时使用）
const editNewMandatoryIngredient = ref('')

// 新可选食材输入（编辑时使用）
const editNewOptionalIngredient = ref('')

// 添加必选食材（编辑时使用）
const editAddMandatoryIngredient = () => {
  if (editNewMandatoryIngredient.value.trim()) {
    const ingredient = editNewMandatoryIngredient.value.trim()
    // 检查重复
    if (!editDishForm.value.ingredients.mandatory.includes(ingredient)) {
      editDishForm.value.ingredients.mandatory.push(ingredient)
      editNewMandatoryIngredient.value = ''
      calculateEditTotalCalories()
    } else {
      ElMessage.warning('该必选食材已存在')
    }
  }
}

// 添加可选食材（编辑时使用）
const editAddOptionalIngredient = () => {
  if (editNewOptionalIngredient.value.trim()) {
    const ingredient = editNewOptionalIngredient.value.trim()
    // 检查重复
    if (!editDishForm.value.ingredients.optional.includes(ingredient)) {
      editDishForm.value.ingredients.optional.push(ingredient)
      editNewOptionalIngredient.value = ''
      calculateEditTotalCalories()
    } else {
      ElMessage.warning('该可选食材已存在')
    }
  }
}

// 删除必选食材（编辑时使用）
const editRemoveMandatoryIngredient = (index) => {
  editDishForm.value.ingredients.mandatory.splice(index, 1)
  calculateEditTotalCalories()
}

// 删除可选食材（编辑时使用）
const editRemoveOptionalIngredient = (index) => {
  editDishForm.value.ingredients.optional.splice(index, 1)
  calculateEditTotalCalories()
}

// 打开编辑菜品对话框
const openEditDishDialog = (dish) => {
  // 复制菜品数据到编辑表单，确保包含食材信息且为数组
  // 排除status字段，状态由菜单管理
  const { status, ...dishWithoutStatus } = dish

  editDishForm.value = JSON.parse(
    JSON.stringify({
      ...dishWithoutStatus,
      ingredients: {
        mandatory: Array.isArray(dish.ingredients?.mandatory) ? dish.ingredients.mandatory : [],
        optional: Array.isArray(dish.ingredients?.optional) ? dish.ingredients.optional : []
      },
      totalCalories: dish.totalCalories || 0
    })
  )
  editDishDialogVisible.value = true
}

// 计算编辑菜品的总卡路里
const calculateEditTotalCalories = () => {
  // 由于改为直接输入食材名称，暂时简化卡路里计算
  // 实际项目中可以根据食材名称匹配数据库中的卡路里数据
  // 或添加输入框让商家直接输入卡路里
  editDishForm.value.totalCalories = 0
}

// 打开添加菜品对话框
const openAddDishDialog = () => {
  addDishDialogVisible.value = true
}

// 保存新菜品
const saveNewDish = () => {
  // 简单的表单验证
  if (!newDish.value.name.trim()) {
    ElMessage.warning('请填写菜品名称')
    return
  }

  // 从authStore获取商家ID
  const authStore = useAuthStore()
  const merchantId = authStore.merchantId
  if (!merchantId) {
    ElMessage.error('未检测到商家ID，请重新登录')
    return
  }

  // 准备请求数据，将 ingredients 对象序列化为 JSON 字符串，并将 totalCalories 映射为 calorie
  const requestData = {
    name: newDish.value.name,
    price: newDish.value.price,
    category: newDish.value.category,
    stock: newDish.value.stock,
    ingredients: JSON.stringify(newDish.value.ingredients),
    calorie: newDish.value.totalCalories,
    merchantId
  }

  // 发送后端请求
  axios.post(`${API_CONFIG.baseURL}${API_CONFIG.dish.list}`, requestData)
    .then((response) => {
      if (response.status === 200 && response.data && response.data.success) {
        const dishData = response.data.data // 获取后端返回的完整菜品数据
        dishesList.value.push(dishData)
        updateFilter()
        addDishDialogVisible.value = false
        ElMessage.success('菜品已添加')
      } else {
        ElMessage.error(response.data?.message || '菜品添加失败')
      }
    })
    .catch((error) => {
      console.error('添加菜品失败:', error)
      ElMessage.error('网络错误，菜品添加失败')
    })
}

// 选择/取消选择单个菜品
const toggleDishSelection = (dish) => {
  const index = selectedDishes.value.findIndex((item) => item.id === dish.id)

  if (index === -1) {
    selectedDishes.value.push(dish)
  } else {
    selectedDishes.value.splice(index, 1)
  }
  // console.log('dish',dish) ;
  // console.log('选择状态：', getSelectAllState());
  // console.log('已选择菜品：', selectedDishes.value);
}

// 全选/取消全选
const toggleSelectAll = () => {
  const currentState = getSelectAllState()

  if (currentState === 2) {
    // 当前是全选状态，点击后取消全选
    selectedDishes.value = []
  } else {
    // 当前是未选或部分选择状态，点击后全选
    selectedDishes.value = [...filteredDishes.value]
  }

  // 触发Vue的响应式更新
  selectedDishes.value = [...selectedDishes.value]

  // console.log('全选状态：', getSelectAllState());
  // console.log('已选择菜品：', selectedDishes.value);
}

// 检查全选状态
const getSelectAllState = () => {
  if (selectedDishes.value.length === 0) {
    return 0
  } else if (
    selectedDishes.value.length === filteredDishes.value.length &&
    filteredDishes.value.length > 0
  ) {
    // 已选择所有项目
    return 2
  } else {
    // 部分选择
    return 1
  }
}

// 监听filteredDishes变化，确保全选状态正确更新
watch(
  () => filteredDishes.value,
  () => {
    // 如果过滤后的菜品数量减少，且当前选中的菜品数量等于过滤前的数量，那么需要调整选中的菜品
    if (selectedDishes.value.length > filteredDishes.value.length) {
      // 只保留过滤后仍存在的菜品
      selectedDishes.value = selectedDishes.value.filter((selectedDish) =>
        filteredDishes.value.some((filteredDish) => filteredDish.id === selectedDish.id)
      )
    }
  }
)

// 获取单个菜品的选中状态
const getDishCheckedState = (dish) => {
  // 直接根据selectedDishes数组判断菜品是否被选中
  // console.log('getDishCheckedState selected',selectedDishes.value);
  // console.log('getDishCheckedState',dish);
  // console.log('getDishCheckedState checked', selectedDishes.value.some(item => item.id === dish.id));

  // 确保返回值是布尔类型
  const isChecked = selectedDishes.value.some((item) => item.id === dish.id)
  // console.log('getDishCheckedState final result:', isChecked);
  return isChecked
}
</script>

<template>
  <div class="dish-management-container">
    <div class="dish-header">
      <div class="header-left">
        <h3 class="page-title">【菜品管理】</h3>
      </div>
      <div class="header-right">
        <el-input
          v-model="searchKeyword"
          placeholder="输入菜品名称或分类..."
          style="min-width: 250px; max-width: 400px; width: auto; flex: 1; max-width: 400px; margin-right: 12px"
          @input="updateFilter"
          clearable
        >
          <template #prefix>
            <el-icon style="color: #909399"><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="openAddDishDialog" class="add-button">
          <el-icon><Plus /></el-icon>
          新增菜品
        </el-button>
      </div>
    </div>


    <div class="dish-list">
      <div class="dish-list-container">
        <div
          class="dish-item"
          v-for="dish in paginatedDishes"
          :key="dish.id"
        >
          <div class="dish-selection">
            <el-checkbox
              :model-value="getDishCheckedState(dish)"
              @change="toggleDishSelection(dish)"
            />
          </div>

          <div class="dish-content">
            <div class="dish-info">
              <div class="dish-name">
                <span class="name">{{ dish.name }}</span>
                <el-tag
                  :type="dish.status === 'online' ? 'success' : dish.status === 'almost_sold' ? 'warning' : 'danger'"
                  size="small"
                  style="margin-left: 8px; font-size: 12px;"
                >
                  {{ dish.status === 'online' ? '上架' : dish.status === 'almost_sold' ? '即将售罄' : '下架' }}
                </el-tag>
              </div>

              <div class="dish-stats">
                <div class="stat-item">
                  <span class="stat-label">🍽️ 分类：</span>
                  <span class="stat-value">{{ dish.category }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">💰 价格：</span>
                  <span class="stat-value">¥{{ dish.price }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">📦 库存：</span>
                  <span class="stat-value">{{ dish.stock }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">⏰ 更新时间：</span>
                  <span class="stat-value">{{ dish.updateTime }}</span>
                </div>
              </div>
            </div>

            <div class="dish-actions">
              <el-button
                :type="dish.status === 'online' ? 'danger' : 'success'"
                size="small"
                @click="toggleDishStatus(dish)"
                :class="{ 'btn-active': true }"
              >
                {{ dish.status === 'online' ? '下架' : '上架' }}
              </el-button>

              <el-button
                type="primary"
                size="small"
                @click="editDish(dish)"
                :class="{ 'btn-active': true }"
              >
                编辑
              </el-button>

              <el-button
                type="warning"
                size="small"
                @click="deleteDish(dish)"
                :class="{ 'btn-active': true }"
              >
                删除
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="batch-actions" v-if="filteredDishes.length > 0">
      <span class="select-all">
        <el-checkbox
          :indeterminate="getSelectAllState() === 1"
          :model-value="getSelectAllState() === 2"
          @change="toggleSelectAll"
        />
        全选
      </span>

      <el-button
        type="success"
        size="small"
        @click="batchOperation('online')"
        :disabled="selectedDishes.length === 0"
        class="batch-btn"
      >
        <el-icon><CircleCheck /></el-icon>
        批量上架
      </el-button>

      <el-button
        type="warning"
        size="small"
        @click="batchOperation('offline')"
        :disabled="selectedDishes.length === 0"
        class="batch-btn"
      >
        <el-icon><CircleClose /></el-icon>
        批量下架
      </el-button>

      <el-button
        type="danger"
        size="small"
        @click="batchOperation('delete')"
        :disabled="selectedDishes.length === 0"
        class="batch-btn"
      >
        <el-icon><Delete /></el-icon>
        批量删除
      </el-button>
    </div>

    <!-- 分页组件 -->
    <div class="pagination-container" v-if="filteredDishes.length > 0">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="filteredDishes.length"
        :page-sizes="[5, 10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="updatePagination"
        @current-change="updatePagination"
      />
    </div>

    <!-- 空数据提示 -->
    <el-empty v-if="filteredDishes.length === 0" description="暂无菜品">
      <template #bottom>
        <el-button type="primary" size="small" @click="addDishDialogVisible = true">新增菜品</el-button>
      </template>
    </el-empty>

    <!-- 添加菜品对话框 -->
    <el-dialog
      v-model="addDishDialogVisible"
      title="添加新菜品"
      width="700px"
      top="10%"
      transition="dialog-fade"
    >
      <div class="add-dish-form">
        <el-form
          :model="newDish"
          label-width="120px"
          status-icon
          class="custom-form"
        >
          <el-form-item label="名称" prop="name" required>
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><GoodsIcon /></el-icon>
                <span>名&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;称</span>
              </div>
            </template>
            <el-input v-model="newDish.name" placeholder="例：宫保鸡丁" />
          </el-form-item>

          <el-form-item label="价格" prop="price" required>
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><MoneyIcon /></el-icon>
                <span>价&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;格</span>
              </div>
            </template>
            <el-input
              v-model.number="newDish.price"
              placeholder="请输入价格"
              type="number"
            />
          </el-form-item>

          <el-form-item label="分类" prop="category" required>
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><FolderIcon /></el-icon>
                <span>分&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;类</span>
              </div>
            </template>
            <el-select
              v-model="newDish.category"
              style="width: 100%"
              filterable
              allow-create
              default-first-option
              placeholder="请选择或输入分类"
            >
              <el-option label="主食" value="主食" />
              <el-option label="汤品" value="汤品" />
              <el-option label="饮料" value="饮料" />
              <el-option label="小吃" value="小吃" />
            </el-select>
          </el-form-item>

          <el-form-item label="库存" prop="stock" required>
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><StockIcon /></el-icon>
                <span>库&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;存</span>
              </div>
            </template>
            <el-input
              v-model.number="newDish.stock"
              placeholder="请输入库存"
              type="number"
            />
          </el-form-item>

          <el-form-item label="状态">
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><StatusIcon /></el-icon>
                <span>状&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;态</span>
              </div>
            </template>
            <el-select
              v-model="newDish.status"
              style="width: 100%"
            >
              <el-option label="上架" value="online" />
              <el-option label="即将售罄" value="almost_sold" />
              <el-option label="下架" value="offline" />
            </el-select>
          </el-form-item>

          <!-- 必选食材 -->
          <el-form-item label="必选食材" required>
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><IngredientsIcon /></el-icon>
                <span>必选食材</span>
              </div>
            </template>
            <div class="optional-ingredients-container">
              <div class="input-button-row">
                <el-input
                  v-model="newMandatoryIngredient"
                  placeholder="请输入必选食材"
                  @keyup.enter="addMandatoryIngredient"
                  clearable
                  style="width: calc(350px - 80px)"
                />
                <el-button
                  type="primary"
                  @click="addMandatoryIngredient"
                  style="margin-left: 10px"
                  class="add-ingredient-btn"
                >
                  添加
                </el-button>
              </div>
              <div class="ingredients-tags">
                <el-tag
                  v-for="(ingredient, index) in newDish.ingredients.mandatory"
                  :key="index"
                  type="warning"
                  closable
                  @close="removeMandatoryIngredient(index)"
                  class="ingredient-tag"
                >
                  {{ ingredient }}
                </el-tag>
              </div>
            </div>
          </el-form-item>

          <!-- 可选食材 -->
          <el-form-item label="可选食材">
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><IngredientsIcon /></el-icon>
                <span>可选食材</span>
              </div>
            </template>
            <div class="optional-ingredients-container">
              <div class="input-button-row">
                <el-input
                  v-model="newOptionalIngredient"
                  placeholder="请输入可选食材"
                  @keyup.enter="addOptionalIngredient"
                  clearable
                  style="width: calc(350px - 80px)"
                />
                <el-button
                  type="primary"
                  @click="addOptionalIngredient"
                  style="margin-left: 10px"
                  class="add-ingredient-btn"
                >
                  添加
                </el-button>
              </div>
              <div class="ingredients-tags">
                <el-tag
                  v-for="(ingredient, index) in newDish.ingredients.optional"
                  :key="index"
                  type="success"
                  closable
                  @close="removeOptionalIngredient(index)"
                  class="ingredient-tag"
                >
                  {{ ingredient }}
                </el-tag>
              </div>
            </div>
          </el-form-item>

          <!-- 卡路里计算 -->
          <el-form-item label="总卡路里">
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><FlameIcon /></el-icon>
                <span>总卡路里</span>
              </div>
            </template>
            <div class="calorie-display">{{ newDish.totalCalories }} kcal</div>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addDishDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveNewDish">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 编辑菜品对话框 -->
    <el-dialog
      v-model="editDishDialogVisible"
      title="编辑菜品"
      width="700px"
      top="10%"
      transition="dialog-fade"
    >
      <div class="add-dish-form">
        <el-form
          :model="editDishForm"
          label-width="120px"
          status-icon
          class="custom-form"
        >
          <el-form-item label="名称" prop="name" required>
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><GoodsIcon /></el-icon>
                <span>名&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;称</span>
              </div>
            </template>
            <el-input v-model="editDishForm.name" placeholder="例：宫保鸡丁" />
          </el-form-item>

          <el-form-item label="价格" prop="price" required>
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><MoneyIcon /></el-icon>
                <span>价&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;格</span>
              </div>
            </template>
            <el-input
              v-model.number="editDishForm.price"
              placeholder="请输入价格"
              type="number"
            />
          </el-form-item>

          <el-form-item label="分类" prop="category" required>
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><FolderIcon /></el-icon>
                <span>分&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;类</span>
              </div>
            </template>
            <el-select
              v-model="editDishForm.category"
              style="width: 100%"
              filterable
              allow-create
              default-first-option
              placeholder="请选择或输入分类"
            >
              <el-option label="主食" value="主食" />
              <el-option label="汤品" value="汤品" />
              <el-option label="饮料" value="饮料" />
              <el-option label="小吃" value="小吃" />
            </el-select>
          </el-form-item>

          <el-form-item label="库存" prop="stock" required>
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><StockIcon /></el-icon>
                <span>库&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;存</span>
              </div>
            </template>
            <el-input
              v-model.number="editDishForm.stock"
              placeholder="请输入库存"
              type="number"
            />
          </el-form-item>

          <el-form-item label="状态">
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><StatusIcon /></el-icon>
                <span>状&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;态</span>
              </div>
            </template>
            <el-select
              v-model="editDishForm.status"
              style="width: 100%"
            >
              <el-option label="上架" value="online" />
              <el-option label="即将售罄" value="almost_sold" />
              <el-option label="下架" value="offline" />
            </el-select>
          </el-form-item>

          <!-- 必选食材 -->
          <el-form-item label="必选食材">
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><IngredientsIcon /></el-icon>
                <span>必选食材</span>
              </div>
            </template>
            <div class="optional-ingredients-container">
              <div class="input-button-row">
                <el-input
                  v-model="editNewMandatoryIngredient"
                  placeholder="请输入必选食材"
                  @keyup.enter="editAddMandatoryIngredient"
                  clearable
                  style="width: calc(350px - 80px)"
                />
                <el-button
                  type="primary"
                  @click="editAddMandatoryIngredient"
                  style="margin-left: 10px"
                  class="add-ingredient-btn"
                >
                  添加
                </el-button>
              </div>
              <div class="ingredients-tags">
                <el-tag
                  v-for="(ingredient, index) in editDishForm.ingredients.mandatory"
                  :key="index"
                  type="warning"
                  closable
                  @close="editRemoveMandatoryIngredient(index)"
                  class="ingredient-tag"
                >
                  {{ ingredient }}
                </el-tag>
              </div>
            </div>
          </el-form-item>

          <!-- 可选食材 -->
          <el-form-item label="可选食材">
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><IngredientsIcon /></el-icon>
                <span>可选食材</span>
              </div>
            </template>
            <div class="optional-ingredients-container">
              <div class="input-button-row">
                <el-input
                  v-model="editNewOptionalIngredient"
                  placeholder="请输入可选食材"
                  @keyup.enter="editAddOptionalIngredient"
                  clearable
                  style="width: calc(350px - 80px)"
                />
                <el-button
                  type="primary"
                  @click="editAddOptionalIngredient"
                  style="margin-left: 10px"
                  class="add-ingredient-btn"
                >
                  添加
                </el-button>
              </div>
              <div class="ingredients-tags">
                <el-tag
                  v-for="(ingredient, index) in editDishForm.ingredients.optional"
                  :key="index"
                  type="success"
                  closable
                  @close="editRemoveOptionalIngredient(index)"
                  class="ingredient-tag"
                >
                  {{ ingredient }}
                </el-tag>
              </div>
            </div>
          </el-form-item>

          <!-- 卡路里计算 -->
          <el-form-item label="总卡路里">
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><FlameIcon /></el-icon>
                <span>总卡路里</span>
              </div>
            </template>
            <div class="calorie-display">{{ editDishForm.totalCalories }} kcal</div>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDishDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveEditedDish">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="less">
.calorie-display {
  font-size: 16px;
  font-weight: 600;
  color: #e6a23c;
}

.optional-ingredients-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 350px;

  .input-button-row {
    display: flex;
    align-items: center;
  }

  .ingredients-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-top: 8px;
  }
}

/* 表单容器 */
.add-dish-form {
  padding: 30px 0;
  max-width: 540px;
  margin: 0 auto;
}

/* 自定义Dialog样式 */
:deep(.el-dialog__header) {
  border-bottom: 2px solid rgba(102, 126, 234, 0.3);
  background: linear-gradient(135deg, rgba(230, 247, 255, 0.8) 0%, rgba(186, 231, 255, 0.8) 100%);
  padding: 24px 28px;
}

:deep(.el-dialog__title) {
  font-size: 20px;
  font-weight: 600;
  color: #1890ff;
  background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

:deep(.el-dialog__body) {
  padding: 32px 28px;
}

/* 表单字段样式 */
:deep(.el-form-item) {
  margin-bottom: 32px; /* 增加字段间距 */
}

/* 带图标的标签样式 */
.form-item-label {
  display: flex;
  align-items: center;
  gap: 8px;
}

.label-icon {
  font-size: 18px;
  color: #667eea;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  vertical-align: middle;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #555;
  font-size: 14px;
}

:deep(.el-form-item__label::before) {
  content: '';
  display: none; /* 隐藏原来的指示线 */
}

/* 输入框样式 */
:deep(.el-input__wrapper),
:deep(.el-select__wrapper),
:deep(.el-textarea__inner) {
  border-radius: 8px;
  border: 2px solid #e5e7eb;
  transition: all 0.3s ease;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

:deep(.el-input__wrapper:hover),
:deep(.el-select__wrapper:hover),
:deep(.el-textarea__inner:hover) {
  border-color: #91d5ff;
  box-shadow: 0 0 0 3px rgba(145, 213, 255, 0.1);
}

:deep(.el-input__wrapper.is-focus),
:deep(.el-select__wrapper.is-focus),
:deep(.el-textarea__inner.is-focus) {
  border-color: #40a9ff;
  box-shadow: 0 0 0 3px rgba(64, 169, 255, 0.15);
}

/* 弹窗动画 */
.dialog-fade-enter-active,
.dialog-fade-leave-active {
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.dialog-fade-enter-from {
  opacity: 0;
  transform: translateY(-20px) scale(0.95);
}

.dialog-fade-leave-to {
  opacity: 0;
  transform: translateY(20px) scale(0.95);
}

/* 按钮样式 */
:deep(.dialog-footer) {
  text-align: center;
  padding: 0 28px 24px;
}

:deep(.dialog-footer .el-button) {
  padding: 10px 28px;
  border-radius: 8px;
  font-weight: 500;
  font-size: 14px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

:deep(.dialog-footer .el-button--primary) {
  background: linear-gradient(135deg, #e6f7ff 0%, #bae7ff 100%);
  border: 1px solid #91d5ff;
  color: #0050b3;
}

:deep(.dialog-footer .el-button--primary:hover) {
  background: linear-gradient(135deg, #bae7ff 0%, #91d5ff 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(64, 169, 255, 0.3);
}

:deep(.dialog-footer .el-button--default) {
  border-color: #e5e7eb;
  background-color: #fafafa;
  color: #666;
}

:deep(.dialog-footer .el-button--default:hover) {
  border-color: #d9d9d9;
  background-color: #f0f0f0;
  color: #333;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

/* 添加食材按钮样式 */
.add-ingredient-btn {
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

/* 食材标签样式 */
.ingredient-tag {
  border-radius: 8px;
  padding: 4px 12px;
  font-size: 12px;
  font-weight: 500;

  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }
}

.dish-management-container {
  padding: 24px;
  background-color: #fafbfc;
  min-height: 100vh;

  .dish-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    padding: 20px;
    background: #ffffff;
    border-radius: 16px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);

    .page-title {
      font-size: 20px;
      font-weight: 700;
      margin: 0;
      color: #4a5568;
    }
  }

  .dish-filters {
    margin-bottom: 24px;

    .filter-section {
      display: flex;
      align-items: center;
      gap: 12px;

      .filter-label {
        font-weight: 500;
      }

      .status-filter {
        cursor: pointer;

        &:hover {
          opacity: 0.8;
        }
      }
    }
  }

  .dish-list {
    margin-bottom: 30px;

    /* List transition animations */
    .list-enter-active,
    .list-leave-active {
      transition: all 0.3s ease;
    }

    .list-enter-from,
    .list-leave-to {
      opacity: 0;
      transform: translateY(10px);
    }

    .dish-item {
      display: flex;
      align-items: flex-start;
      padding: 20px;
      border: none;
      border-radius: 16px;
      margin-bottom: 16px;
      background-color: #ffffff;
      transition: all 0.3s ease;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);

      &:hover {
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
        transform: translateY(-2px);
      }

      .dish-selection {
        margin-top: 4px;
        margin-right: 16px;
      }

      .dish-content {
        flex: 1;
        display: flex;
        flex-direction: column;

        .dish-info {
          .dish-name {
            display: flex;
            align-items: center;
            gap: 12px;
            margin-bottom: 16px;

            .name {
              font-size: 18px;
              font-weight: 700;
              color: #2d3748;
            }
          }

          .dish-stats {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
            gap: 16px;
            margin-bottom: 20px;
            font-size: 14px;

            .stat-item {
              display: flex;
              flex-direction: column;
              gap: 4px;

              .stat-label {
                color: #718096;
                font-size: 12px;
                font-weight: 500;
              }

              .stat-value {
                color: #4a5568;
                font-size: 14px;
                font-weight: 600;
              }
            }
          }
        }

        .dish-actions {
          display: flex;
          flex-direction: row;
          gap: 10px;
          justify-content: flex-start;
          flex-wrap: wrap;

          button {
            width: 90px;
            border-radius: 10px;
            font-weight: 500;
            transition: all 0.3s ease;
            border: none;
            box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);

            &:hover {
              transform: translateY(-1px);
              box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
            }

            &:active {
              transform: translateY(0);
            }
          }

          .btn-active {
            background: linear-gradient(135deg, #e6f7ff 0%, #bae7ff 100%);
            color: #0050b3;

            &:hover {
              background: linear-gradient(135deg, #bae7ff 0%, #91d5ff 100%);
            }
          }
        }
      }
    }
  }

  .batch-actions {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 16px 20px;
    background: #ffffff;
    border-radius: 16px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);

    .select-all {
      display: flex;
      align-items: center;
      gap: 8px;
      font-weight: 600;
      color: #4a5568;
    }
  }

  // 分页容器样式
  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
    padding: 16px;
    background: #ffffff;
    border-radius: 16px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  }

  .dialog-footer {
    text-align: right;
  }

  .add-button {
    background: linear-gradient(135deg, #f6ffed 0%, #d9f7be 100%);
    border: none;
    border-radius: 10px;
    padding: 10px 20px;
    font-weight: 600;
    color: #389e0d;
    box-shadow: 0 2px 8px rgba(56, 158, 13, 0.2);
    transition: all 0.3s ease;

    &:hover {
      background: linear-gradient(135deg, #d9f7be 0%, #b7eb8f 100%);
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(56, 158, 13, 0.3);
    }

    &:active {
      transform: translateY(0);
    }
  }

  .batch-btn {
    border-radius: 10px;
    font-weight: 500;
    transition: all 0.3s ease;
    border: none;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);

    &:hover:not(:disabled) {
      transform: translateY(-1px);
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
    }

    &:active:not(:disabled) {
      transform: translateY(0);
    }

    &:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
  }
}
</style>
