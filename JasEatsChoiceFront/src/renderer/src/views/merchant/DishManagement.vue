<script setup>
import { ref, onMounted, watch, TransitionGroup } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'
import { API_CONFIG } from '../../config/index.js'

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
// 三态全选复选框的状态：0=未选择，1=部分选择，2=全选
// const selectAllState = ref(0); // 不再需要这个状态变量，直接通过计算获得
// 页面加载时初始化
onMounted(() => {
  loading.value = true
  // 从API获取菜品数据
  axios
    .get(`${API_CONFIG.baseURL}${API_CONFIG.dish.list}`)
    .then((response) => {
      if (response.data && response.data.success) {
        // 预处理菜品数据，确保所有菜品都有有效的状态
        const processedDishes = response.data.data.map(dish => {
          // 确保status存在且是有效的值
          const validStatuses = ['online', 'almost_sold', 'offline']
          if (!dish.status || !validStatuses.includes(dish.status)) {
            // 设置默认状态为online
            dish.status = 'online'
          }
          return dish
        })
        dishesList.value = processedDishes
        filteredDishes.value = [...dishesList.value] // 更新筛选后的菜品
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

// 筛选菜品
const filteredDishes = ref([])

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

  switch (operation) {
    case 'online':
      selectedDishes.value.forEach((dish) => {
        dish.status = 'online'
      })
      ElMessage.success('批量上架成功')
      break
    case 'offline':
      selectedDishes.value.forEach((dish) => {
        dish.status = 'offline'
      })
      ElMessage.success('批量下架成功')
      break
    case 'delete':
      ElMessageBox.confirm('确定要删除所选菜品吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => {
          dishesList.value = dishesList.value.filter((dish) => !selectedDishes.value.includes(dish))
          selectedDishes.value = []
          updateFilter()
          ElMessage.success('批量删除成功')
        })
        .catch(() => {})
      return
  }

  updateFilter()
  selectedDishes.value = []
  // 强制更新界面，确保全选状态正确更新
  setTimeout(() => {
    updateFilter()
  }, 0)
}

// 新增菜品对话框
const addDishDialogVisible = ref(false)

// 食材数据（模拟）
const ingredients = ref([
  // 必选食材
  { id: 1, name: '米饭', type: 'mandatory', calories: 116 },
  { id: 2, name: '面条', type: 'mandatory', calories: 137 },
  { id: 3, name: '面包', type: 'mandatory', calories: 264 },
  // 可选食材
  { id: 4, name: '鸡肉', type: 'optional', calories: 165 },
  { id: 5, name: '猪肉', type: 'optional', calories: 242 },
  { id: 6, name: '牛肉', type: 'optional', calories: 250 },
  { id: 7, name: '蔬菜', type: 'optional', calories: 50 },
  { id: 8, name: '鸡蛋', type: 'optional', calories: 78 }
])

// 新菜品表单数据
const newDish = ref({
  name: '',
  price: 0,
  category: '主食',
  status: 'online',
  stock: 100,
  ingredients: {
    mandatory: null, // 必选食材是单个值
    optional: [] // 可选食材是数组
  },
  totalCalories: 0 // 总卡路里
})

// 计算总卡路里
const calculateTotalCalories = () => {
  let total = 0

  // 计算必选食材卡路里 - 必选是单个值
  if (newDish.value.ingredients.mandatory) {
    const ingredient = ingredients.value.find(
      (ing) => ing.id === newDish.value.ingredients.mandatory
    )
    if (ingredient) {
      total += ingredient.calories
    }
  }

  // 计算可选食材卡路里 - 可选是数组
  newDish.value.ingredients.optional.forEach((ingredientId) => {
    const ingredient = ingredients.value.find((ing) => ing.id === ingredientId)
    if (ingredient) {
      total += ingredient.calories
    }
  })

  newDish.value.totalCalories = total
}

// 编辑菜品对话框
const editDishDialogVisible = ref(false)

// 编辑菜品表单数据
const editDishForm = ref({
  ingredients: {
    mandatory: null, // 必选食材是单个值，不是数组
    optional: [] // 可选食材是数组
  },
  totalCalories: 0
})

// 打开编辑菜品对话框
const openEditDishDialog = (dish) => {
  // 复制菜品数据到编辑表单，确保包含食材信息
  editDishForm.value = JSON.parse(
    JSON.stringify({
      ...dish,
      ingredients: dish.ingredients || { mandatory: null, optional: [] },
      totalCalories: dish.totalCalories || 0
    })
  )
  editDishDialogVisible.value = true
}

// 计算编辑菜品的总卡路里
const calculateEditTotalCalories = () => {
  let total = 0

  // 确保 ingredients 存在
  if (!editDishForm.value.ingredients) {
    editDishForm.value.ingredients = { mandatory: null, optional: [] }
  }

  // 计算必选食材卡路里 - 必选是单个值
  if (editDishForm.value.ingredients.mandatory) {
    const ingredient = ingredients.value.find(
      (ing) => ing.id === editDishForm.value.ingredients.mandatory
    )
    if (ingredient) {
      total += ingredient.calories
    }
  }

  // 计算可选食材卡路里 - 可选是数组
  editDishForm.value.ingredients.optional?.forEach((ingredientId) => {
    const ingredient = ingredients.value.find((ing) => ing.id === ingredientId)
    if (ingredient) {
      total += ingredient.calories
    }
  })

  editDishForm.value.totalCalories = total
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
        <div class="dish-item" v-for="dish in filteredDishes" :key="dish.id">
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
          <el-select
            v-model="newDish.ingredients.mandatory"
            style="width: 100%"
            placeholder="请选择必选食材"
            @change="calculateTotalCalories"
          >
            <el-option
              v-for="ingredient in ingredients"
              :key="ingredient.id"
              :value="ingredient.id"
              v-if="ingredient && ingredient.type === 'mandatory'"
            >
              {{ ingredient.name }} ({{ ingredient.calories }} kcal)
            </el-option>
          </el-select>
        </el-form-item>

        <!-- 可选食材 -->
        <el-form-item label="可选食材">
          <el-select
            v-model="newDish.ingredients.optional"
            style="width: 100%"
            placeholder="请选择可选食材"
            multiple
            @change="calculateTotalCalories"
          >
            <el-option
              v-for="ingredient in ingredients"
              :key="ingredient.id"
              :value="ingredient.id"
              v-if="ingredient && ingredient.type === 'optional'"
            >
              {{ ingredient.name }} ({{ ingredient.calories }} kcal)
            </el-option>
          </el-select>
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
          <el-select
            v-model="editDishForm.ingredients.mandatory"
            style="width: 100%"
            placeholder="请选择必选食材"
            @change="calculateEditTotalCalories"
          >
            <el-option
              v-for="ingredient in ingredients"
              :key="ingredient.id"
              :value="ingredient.id"
              v-if="ingredient && ingredient.type === 'mandatory'"
            >
              {{ ingredient.name }} ({{ ingredient.calories }} kcal)
            </el-option>
          </el-select>
        </el-form-item>

        <!-- 可选食材 -->
        <el-form-item label="可选食材">
          <el-select
            v-model="editDishForm.ingredients.optional"
            style="width: 100%"
            placeholder="请选择可选食材"
            multiple
            @change="calculateEditTotalCalories"
          >
            <el-option
              v-for="ingredient in ingredients"
              :key="ingredient.id"
              :value="ingredient.id"
              v-if="ingredient && ingredient.type === 'optional'"
            >
              {{ ingredient.name }} ({{ ingredient.calories }} kcal)
            </el-option>
          </el-select>
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

  .dialog-footer {
    text-align: right;
  }
}
</style>
