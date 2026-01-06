<script setup>
import { ref, onMounted, watch, TransitionGroup } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'
import { API_CONFIG } from '../../config/index.js'
// 导入authStore
import { useAuthStore } from '../../store/authStore'

// 菜品状态映射
const dishStatusMap = {
  online: { text: '🟢 在售', type: 'success' },
  almost_sold: { text: '🟡 即将售罄', type: 'warning' },
  offline: { text: '🔴 下架', type: 'danger' }
}

// 菜品数据
const dishesList = ref([])

const loading = ref(false)
const searchKeyword = ref('')
const activeStatusFilter = ref('all')
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
      if (response.data && response.data.success) {
        // 预处理菜品数据，确保所有菜品都有有效的状态和时间格式
        const processedDishes = response.data.data.map(dish => {
          // 确保status存在且是有效的值
          const validStatuses = ['online', 'almost_sold', 'offline']
          if (!dish.status || !validStatuses.includes(dish.status)) {
            // 设置默认状态为online
            dish.status = 'online'
          }

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
    // 状态筛选
    if (activeStatusFilter.value !== 'all' && dish.status !== activeStatusFilter.value) {
      return false
    }

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

// 切换状态
const toggleDishStatus = (dish) => {
  let newStatus = ''

  if (dish.status === 'online') {
    newStatus = 'offline'
  } else if (dish.status === 'offline' || dish.status === 'almost_sold') {
    newStatus = 'online'
  } else {
    // 默认处理未知状态，设为online
    newStatus = 'online'
  }

  dish.status = newStatus
  updateFilter()
  ElMessage.success(`菜品已${dishStatusMap[newStatus]?.text || newStatus}`)
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

  // 找到要编辑的菜品并更新
  const index = dishesList.value.findIndex((item) => item.id === editDishForm.value.id)
  if (index !== -1) {
    // 更新菜品信息，确保包含食材和卡路里
    dishesList.value[index] = {
      ...dishesList.value[index],
      ...editDishForm.value,
      updateTime: new Date().toISOString().slice(0, 19).replace('T', ' ') // 更新时间
    }

    updateFilter()
    editDishDialogVisible.value = false
    ElMessage.success('菜品已更新')
  }
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
  status: 'online',
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
  editDishForm.value = JSON.parse(
    JSON.stringify({
      ...dish,
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

  // 创建新菜品对象
  const newDishObj = {
    id: Date.now(),
    name: newDish.value.name,
    price: newDish.value.price,
    category: newDish.value.category,
    status: newDish.value.status,
    stock: newDish.value.stock,
    ingredients: newDish.value.ingredients,
    totalCalories: newDish.value.totalCalories,
    updateTime: new Date().toISOString().slice(0, 19).replace('T', ' ')
  }

  // 添加到菜品列表
  dishesList.value.push(newDishObj)
  updateFilter()
  addDishDialogVisible.value = false
  ElMessage.success('菜品已添加')
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
          style="min-width: 250px; max-width: 400px; width: auto; flex: 1; max-width: 400px; margin-right: 10px"
          @input="updateFilter"
        />
        <el-button type="primary" @click="openAddDishDialog">
          <span>➕</span>
          新增菜品
        </el-button>
      </div>
    </div>

    <div class="dish-filters">
      <div class="filter-section">
        <span class="filter-label">📋 状态筛选：</span>
        <el-tag
          v-for="status in ['all', 'online', 'almost_sold', 'offline']"
          :key="status"
          :type="activeStatusFilter === status ? 'primary' : 'info'"
          effect="plain"
          @click="
            () => {
              activeStatusFilter = status
              updateFilter()
            }
          "
          class="status-filter"
        >
          {{ status === 'all' ? '全部菜品' : dishStatusMap[status].text }}
        </el-tag>
      </div>
    </div>

    <div class="dish-list">
      <TransitionGroup
        name="list"
        tag="div"
      >
        <div class="dish-item" v-for="dish in paginatedDishes" :key="dish.id">
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
              <el-tag :type="dishStatusMap[dish.status]?.type || 'info'">
                {{ dishStatusMap[dish.status]?.text || '未知状态' }}
              </el-tag>
            </div>

            <div class="dish-stats">
              <span class="dish-category">🍽️ 分类：{{ dish.category }}</span>
              <span class="dish-price">💰 价格：¥{{ dish.price }}</span>
              <span class="dish-stock">📦 库存：{{ dish.stock }}</span>
              <span class="update-time">⏰ 更新时间：{{ dish.updateTime }}</span>
            </div>
          </div>

          <div class="dish-actions">
            <el-button type="primary" size="small" @click="toggleDishStatus(dish)">
              {{ dish.status === 'online' ? '🔴 下架' : '🟢 上架' }}
            </el-button>

            <el-button type="warning" size="small" @click="editDish(dish)"> ✏️ 编辑 </el-button>

            <el-button type="danger" size="small" @click="deleteDish(dish)"> 🗑️ 删除 </el-button>
          </div>
        </div>
      </div>
    </TransitionGroup>
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
      >
        🟢 批量上架
      </el-button>

      <el-button
        type="warning"
        size="small"
        @click="batchOperation('offline')"
        :disabled="selectedDishes.length === 0"
      >
        🔴 批量下架
      </el-button>

      <el-button
        type="danger"
        size="small"
        @click="batchOperation('delete')"
        :disabled="selectedDishes.length === 0"
      >
        🗑️ 批量删除
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
    <el-dialog v-model="addDishDialogVisible" title="添加新菜品" width="600px" top="10%">
      <el-form :model="newDish" label-width="100px" status-icon>
        <el-form-item label="名称" prop="name" required>
          <el-input v-model="newDish.name" placeholder="请输入菜品名称" />
        </el-form-item>

        <el-form-item label="价格" prop="price" required>
          <el-input v-model.number="newDish.price" placeholder="请输入价格" type="number" />
        </el-form-item>

        <el-form-item label="分类" prop="category" required>
          <el-select v-model="newDish.category" style="width: 100%">
            <el-option label="主食" value="主食" />
            <el-option label="汤品" value="汤品" />
            <el-option label="饮料" value="饮料" />
            <el-option label="小吃" value="小吃" />
          </el-select>
        </el-form-item>

        <el-form-item label="库存" prop="stock" required>
          <el-input v-model.number="newDish.stock" placeholder="请输入库存" type="number" />
        </el-form-item>

        <el-form-item label="状态">
          <el-select v-model="newDish.status" style="width: 100%">
            <el-option label="上架" value="online" />
            <el-option label="下架" value="offline" />
          </el-select>
        </el-form-item>

        <!-- 必选食材 -->
        <el-form-item label="必选食材" required>
          <div class="optional-ingredients-container">
            <div class="input-button-row">
              <el-input
                v-model="newMandatoryIngredient"
                placeholder="请输入必选食材"
                @keyup.enter="addMandatoryIngredient"
                clearable
                style="width: calc(300px - 80px)"
              />
              <el-button
                type="primary"
                @click="addMandatoryIngredient"
                style="margin-left: 10px"
              >
                添加
              </el-button>
            </div>
            <div class="ingredients-tags">
              <el-tag
                v-for="(ingredient, index) in newDish.ingredients.mandatory"
                :key="index"
                type="danger"
                closable
                @close="removeMandatoryIngredient(index)"
              >
                {{ ingredient }}
              </el-tag>
            </div>
          </div>
        </el-form-item>

        <!-- 可选食材 -->
        <el-form-item label="可选食材">
          <div class="optional-ingredients-container">
            <div class="input-button-row">
              <el-input
                v-model="newOptionalIngredient"
                placeholder="请输入可选食材"
                @keyup.enter="addOptionalIngredient"
                clearable
                style="width: calc(300px - 80px)"
              />
              <el-button
                type="primary"
                @click="addOptionalIngredient"
                style="margin-left: 10px"
              >
                添加
              </el-button>
            </div>
            <div class="ingredients-tags">
              <el-tag
                v-for="(ingredient, index) in newDish.ingredients.optional"
                :key="index"
                type="info"
                closable
                @close="removeOptionalIngredient(index)"
              >
                {{ ingredient }}
              </el-tag>
            </div>
          </div>
        </el-form-item>

        <!-- 卡路里计算 -->
        <el-form-item label="总卡路里">
          <div class="calorie-display">{{ newDish.totalCalories }} kcal</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addDishDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveNewDish">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 编辑菜品对话框 -->
    <el-dialog v-model="editDishDialogVisible" title="编辑菜品" width="600px" top="10%">
      <el-form :model="editDishForm" label-width="100px" status-icon>
        <el-form-item label="名称" prop="name" required>
          <el-input v-model="editDishForm.name" placeholder="请输入菜品名称" />
        </el-form-item>

        <el-form-item label="价格" prop="price" required>
          <el-input v-model.number="editDishForm.price" placeholder="请输入价格" type="number" />
        </el-form-item>

        <el-form-item label="分类" prop="category" required>
          <el-select v-model="editDishForm.category" style="width: 100%">
            <el-option label="主食" value="主食" />
            <el-option label="汤品" value="汤品" />
            <el-option label="饮料" value="饮料" />
            <el-option label="小吃" value="小吃" />
          </el-select>
        </el-form-item>

        <el-form-item label="库存" prop="stock" required>
          <el-input v-model.number="editDishForm.stock" placeholder="请输入库存" type="number" />
        </el-form-item>

        <el-form-item label="状态">
          <el-select v-model="editDishForm.status" style="width: 100%">
            <el-option label="上架" value="online" />
            <el-option label="即将售罄" value="almost_sold" />
            <el-option label="下架" value="offline" />
          </el-select>
        </el-form-item>

        <!-- 必选食材 -->
        <el-form-item label="必选食材">
          <div class="optional-ingredients-container">
            <div class="input-button-row">
              <el-input
                v-model="editNewMandatoryIngredient"
                placeholder="请输入必选食材"
                @keyup.enter="editAddMandatoryIngredient"
                clearable
                style="width: calc(300px - 80px)"
              />
              <el-button
                type="primary"
                @click="editAddMandatoryIngredient"
                style="margin-left: 10px"
              >
                添加
              </el-button>
            </div>
            <div class="ingredients-tags">
              <el-tag
                v-for="(ingredient, index) in editDishForm.ingredients.mandatory"
                :key="index"
                type="danger"
                closable
                @close="editRemoveMandatoryIngredient(index)"
              >
                {{ ingredient }}
              </el-tag>
            </div>
          </div>
        </el-form-item>

        <!-- 可选食材 -->
        <el-form-item label="可选食材">
          <div class="optional-ingredients-container">
            <div class="input-button-row">
              <el-input
                v-model="editNewOptionalIngredient"
                placeholder="请输入可选食材"
                @keyup.enter="editAddOptionalIngredient"
                clearable
                style="width: calc(300px - 80px)"
              />
              <el-button
                type="primary"
                @click="editAddOptionalIngredient"
                style="margin-left: 10px"
              >
                添加
              </el-button>
            </div>
            <div class="ingredients-tags">
              <el-tag
                v-for="(ingredient, index) in editDishForm.ingredients.optional"
                :key="index"
                type="info"
                closable
                @close="editRemoveOptionalIngredient(index)"
              >
                {{ ingredient }}
              </el-tag>
            </div>
          </div>
        </el-form-item>

        <!-- 卡路里计算 -->
        <el-form-item label="总卡路里">
          <div class="calorie-display">{{ editDishForm.totalCalories }} kcal</div>
        </el-form-item>
      </el-form>
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
  color: #f56c6c;
}

.optional-ingredients-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 300px;

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

.dish-management-container {
  padding: 24px;

  .dish-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;

    .page-title {
      font-size: 18px;
      font-weight: 600;
      margin: 0;
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
      padding: 16px;
      border: 1px solid #e4e7ed;
      border-radius: 4px;
      margin-bottom: 12px;
      background-color: #fff;
      transition: box-shadow 0.3s;

      &:hover {
        box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
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
            gap: 10px;
            margin-bottom: 12px;

            .name {
              font-size: 16px;
              font-weight: 600;
            }
          }

          .dish-stats {
            display: flex;
            flex-wrap: wrap;
            gap: 24px;
            margin-bottom: 16px; /* 增加底部间距，为水平排列的按钮腾出空间 */
            font-size: 14px;

            .dish-category,
            .dish-price,
            .dish-stock {
              color: #606266;
            }
          }
        }

        .dish-actions {
          display: flex;
          flex-direction: row;
          gap: 8px;
          justify-content: flex-start;
          flex-wrap: wrap;

          button {
            width: 100px;
          }
        }
      }
    }
  }

  .batch-actions {
    display: flex;
    align-items: center;
    gap: 16px;

    .select-all {
      display: flex;
      align-items: center;
      gap: 8px;
      font-weight: 500;
    }
  }

  // 分页容器样式
  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }

  .dialog-footer {
    text-align: right;
  }
}
</style>
