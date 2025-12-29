<script setup>
import { ref, computed, onMounted } from 'vue'

import { ArrowDown } from '@element-plus/icons-vue'
import axios from 'axios'
import { API_CONFIG } from '../../config'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '../../store/authStore'

// 引入组件
import RecipeDetail from '../../components/RecipeDetail.vue'
import AddDish from '../../components/AddDish.vue'
import ImportMerchantDish from '../../components/ImportMerchantDish.vue'
import AddRecipe from '../../components/recipe/AddRecipe.vue'
import { useUserStore } from '../../store/userStore'

// 我的食谱数据
const myRecipes = ref([])
const loadingFailed = ref(false)

// 批量操作相关变量
const selectedRecipes = ref([]) // 存储选中的食谱

// 模态框状态
const replaceDialogVisible = ref(false)
const addDishVisible = ref(false)
const importMerchantDishVisible = ref(false)

// 当前选中的菜品
const selectedDish = ref(null)

// 商家列表和选中商家 - 传递给ImportMerchantDish组件
const merchants = ref([
  {
    id: 1,
    name: '健康餐厅',
    dishes: [
      { id: 1, name: '有机蔬菜沙拉', nutrition: '120kcal/份' },
      { id: 2, name: '烤三文鱼', nutrition: '280kcal/份' }
    ]
  },
  {
    id: 2,
    name: '健身餐吧',
    dishes: [
      { id: 3, name: '鸡胸肉盖饭', nutrition: '450kcal/份' },
      { id: 4, name: '糙米粥', nutrition: '180kcal/份' }
    ]
  }
])

// 替换菜品列表 mock数据
const replacementDishes = ref([
  { id: 1, name: '全麦面包', type: '早餐', nutrition: '247kcal/片' },
  { id: 2, name: '蒸南瓜', type: '早餐', nutrition: '26kcal/100g' },
  { id: 3, name: '烤鸡胸肉', type: '午餐', nutrition: '165kcal/100g' },
  { id: 4, name: '西兰花', type: '午餐', nutrition: '34kcal/100g' },
  { id: 5, name: '清蒸鱼', type: '晚餐', nutrition: '105kcal/100g' },
  { id: 6, name: '炒青菜', type: '晚餐', nutrition: '15kcal/100g' }
])

// 加载我的食谱数据
const loadMyRecipes = () => {
  // 获取用户信息 - 从Pinia store获取
  const authStore = useAuthStore()
  const userStore = useUserStore()

  let userId = null

  // 从authStore获取userId，如果authStore中没有则从userStore的userInfo中获取
  if (authStore.userId) {
    userId = authStore.userId
  } else if (userStore.userInfo?.userId) {
    userId = userStore.userInfo.userId
  } else {
    console.error('加载我的食谱失败: 无法获取用户ID')
    ElMessage.error('加载我的食谱失败: 无法获取用户ID')
    return
  }

  // 确保userId有效
  if (!userId) {
    console.error('加载我的食谱失败: 用户ID无效')
    ElMessage.error('加载我的食谱失败: 用户ID无效')
    return
  }

  // 通过API从后端获取我的食谱数据
  axios
    .get(`${API_CONFIG.baseURL}${API_CONFIG.recipe.favorite}`, {
      params: {
        userId: userId
      }
    })
    .then((response) => {
      console.log('加载我的食谱成功:', response)
      if (response.data?.code === '200' && response.data?.data) {
        myRecipes.value = response.data.data.map((recipe) => ({
          ...recipe,
          // 确保食谱数据格式一致
          items: recipe.items
            ? typeof recipe.items === 'string'
              ? JSON.parse(recipe.items)
              : recipe.items
            : [],
          ingredients: recipe.ingredients
            ? typeof recipe.ingredients === 'string'
              ? JSON.parse(recipe.ingredients)
              : recipe.ingredients
            : [],
          time: recipe.time || '30分钟' // 默认值
        }))
      } else {
        myRecipes.value = []
      }
      loadingFailed.value = false
      console.log('加载我的食谱成功:', myRecipes.value)
    })
    .catch((error) => {
      console.error('加载我的食谱失败:', error)
      myRecipes.value = []
      loadingFailed.value = true
      ElMessage.error('加载我的食谱失败，请稍后重试')
    })
}

// 组件挂载时加载数据
onMounted(() => {
  loadMyRecipes()
})

// 食谱筛选
const recipeFilter = ref('all')

// 计算属性：过滤后的食谱列表，收藏的食谱排在前面
const filteredRecipes = computed(() => {
  let filtered = []

  if (recipeFilter.value === 'all') {
    filtered = [...myRecipes.value]
  } else {
    filtered = myRecipes.value.filter((recipe) => recipe.type === recipeFilter.value)
  }

  // 排序：收藏的食谱在前
  return filtered.sort((a, b) => {
    // 如果a收藏而b未收藏，a排在前面
    if (a.favorite && !b.favorite) return -1
    // 如果b收藏而a未收藏，b排在前面
    if (!a.favorite && b.favorite) return 1
    // 否则保持原顺序
    return 0
  })
})

// 切换收藏状态
const toggleFavorite = (recipe) => {
  recipe.favorite = !recipe.favorite
}

// 食谱详情组件相关
const detailDialogVisible = ref(false)
const selectedRecipe = ref(null)

// 查看详情
const viewRecipeDetails = (recipe) => {
  selectedRecipe.value = recipe
  detailDialogVisible.value = true
}

// 更新收藏状态
const updateRecipe = (updatedRecipe) => {
  // 在myRecipes数组中找到对应的食谱并更新
  const index = myRecipes.value.findIndex((recipe) => recipe.id === updatedRecipe.id)
  if (index !== -1) {
    myRecipes.value[index] = updatedRecipe
  }

  // 更新详情对话框中的食谱
  selectedRecipe.value = updatedRecipe
}

// 更新烹饪时间
const handleUpdateCookTime = (newCookTime) => {
  if (selectedRecipe.value) {
    // 更新本地数据
    selectedRecipe.value.cookTime = newCookTime

    // 在myRecipes数组中找到对应的食谱并更新
    const index = myRecipes.value.findIndex((recipe) => recipe.id === selectedRecipe.value.id)
    if (index !== -1) {
      myRecipes.value[index].cookTime = newCookTime
    }

    // 调用后端API更新食谱
    axios
      .put(API_CONFIG.baseURL + API_CONFIG.recipe.update + selectedRecipe.value.id, {
        ...selectedRecipe.value,
        cookTime: newCookTime
      })
      .then((response) => {
        console.log('更新烹饪时间成功:', response)
      })
      .catch((error) => {
        console.error('更新烹饪时间失败:', error)
      })
  }
}

// 替换菜品
const replaceDish = (recipe, dish) => {
  selectedRecipe.value = recipe
  selectedDish.value = dish
  replaceDialogVisible.value = true
}

// 添加菜品
const addDish = (recipe) => {
  // 确保recipe.items是数组
  recipe.items = recipe.items || []
  selectedRecipe.value = recipe
  addDishVisible.value = true
}

// 确认替换菜品
const confirmReplaceDish = (newDish) => {
  if (selectedRecipe.value && selectedDish.value && selectedRecipe.value.items) {
    // 先保存原菜品，以便失败时恢复
    const oldDish = selectedDish.value

    // 找到并替换菜品
    const index = selectedRecipe.value.items.indexOf(selectedDish.value)
    if (index !== -1) {
      // 替换菜品
      selectedRecipe.value.items[index] = {
        name: newDish.name,
        ingredients: [],
        calories: 0,
        protein: 0,
        carbs: 0,
        fat: 0
      }

      // 调用后端API更新食谱 - 将items转换为JSON字符串
      const updateData = {
        ...selectedRecipe.value,
        items: JSON.stringify(selectedRecipe.value.items)
      }

      axios
        .put(API_CONFIG.baseURL + API_CONFIG.recipe.update + selectedRecipe.value.id, updateData)
        .then((response) => {
          // 更新本地数据 - 确保items字段已解析
          const recipeIndex = myRecipes.value.findIndex((r) => r.id === selectedRecipe.value.id)
          if (recipeIndex !== -1) {
            // 确保返回的食谱有items数组并已解析
            const updatedRecipe = {
              ...response.data.data,
              items:
                typeof response.data.data.items === 'string'
                  ? JSON.parse(response.data.data.items)
                  : response.data.data.items || []
            }
            myRecipes.value[recipeIndex] = updatedRecipe
          }

          ElMessage.success('菜品已替换')
          replaceDialogVisible.value = false

          // 重置选中状态
          selectedRecipe.value = null
          selectedDish.value = null
        })
        .catch((error) => {
          console.error('替换菜品失败:', error)
          // 失败时恢复本地数据
          selectedRecipe.value.items[index] = oldDish
          ElMessage.error('替换菜品失败')
        })
    }
  }
}

// 打开导入商家菜品对话框
const openImportMerchantDish = (recipe) => {
  selectedRecipe.value = recipe
  importMerchantDishVisible.value = true
}

// 处理导入商家菜品
const handleImportMerchantDishes = (recipe, dishesToImport) => {
  if (dishesToImport.length > 0) {
    // 这里需要知道要导入到哪个食谱，需要先设置 selectedRecipe
    if (recipe) {
      // 先保存当前的items，以便失败时恢复
      const originalItems = [...recipe.items]

      // 将商家菜品转换为食谱需要的格式并添加到本地
      dishesToImport.forEach((dish) => {
        const recipeDish = {
          name: dish.name,
          ingredients: [], // 商家菜品默认没有食材，用户可以后续添加
          calories: 0,
          protein: 0,
          carbs: 0,
          fat: 0
        }
        recipe.items.push(recipeDish)
      })

      // 调用后端API更新食谱 - 将items转换为JSON字符串
      const updateData = {
        ...recipe,
        items: JSON.stringify(recipe.items)
      }

      axios
        .put(API_CONFIG.baseURL + API_CONFIG.recipe.update + recipe.id, updateData)
        .then((response) => {
          // 更新本地数据 - 确保items字段已解析
          const recipeIndex = myRecipes.value.findIndex((r) => r.id === recipe.id)
          if (recipeIndex !== -1) {
            // 确保返回的食谱有items数组并已解析
            const updatedRecipe = {
              ...response.data.data,
              items:
                typeof response.data.data.items === 'string'
                  ? JSON.parse(response.data.data.items)
                  : response.data.data.items || []
            }
            myRecipes.value[recipeIndex] = updatedRecipe
          }

          ElMessage.success(`成功导入 ${dishesToImport.length} 道菜品`)
        })
        .catch((error) => {
          console.error('导入商家菜品失败:', error)
          // 失败时恢复本地数据
          recipe.items = originalItems
          ElMessage.error('导入商家菜品失败')
        })
    } else {
      ElMessage.error('请先选择要导入到的食谱')
    }
  } else {
    ElMessage.warning('请先选择要导入的菜品')
  }
}

// 处理添加菜品
const handleAddDish = (recipe, newDish) => {
  if (recipe && newDish.name.trim()) {
    // 先添加到本地
    recipe.items.push(newDish)

    // 调用后端API更新食谱 - 将items转换为JSON字符串
    const updateData = {
      ...recipe,
      items: JSON.stringify(recipe.items)
    }

    axios
      .put(API_CONFIG.baseURL + API_CONFIG.recipe.update + recipe.id, updateData)
      .then((response) => {
        // 更新本地数据 - 确保items字段已解析
        const recipeIndex = myRecipes.value.findIndex((r) => r.id === recipe.id)
        if (recipeIndex !== -1) {
          // 确保返回的食谱有items数组并已解析
          const updatedRecipe = {
            ...response.data.data,
            items:
              typeof response.data.data.items === 'string'
                ? JSON.parse(response.data.data.items)
                : response.data.data.items || []
          }
          myRecipes.value[recipeIndex] = updatedRecipe
        }

        ElMessage.success('菜品已添加')
      })
      .catch((error) => {
        console.error('添加菜品失败:', error)
        // 失败时恢复本地数据
        recipe.items.pop()
        ElMessage.error('添加菜品失败')
      })
  }
}

// 删除菜品
const deleteDish = (recipe, dish) => {
  if (recipe && dish && recipe.items) {
    // 先更新本地数据
    const index = recipe.items.indexOf(dish)
    if (index !== -1) {
      recipe.items.splice(index, 1)

      // 调用后端API更新食谱
      const updateData = {
        ...recipe
      }

      axios
        .put(API_CONFIG.baseURL + API_CONFIG.recipe.update + recipe.id, updateData)
        .then((response) => {
          // 更新本地数据
          const recipeIndex = myRecipes.value.findIndex((r) => r.id === recipe.id)
          if (recipeIndex !== -1) {
            myRecipes.value[recipeIndex] = response.data.data
          }

          ElMessage.success('菜品已删除')
        })
        .catch((error) => {
          console.error('删除菜品失败:', error)
          // 失败时恢复本地数据
          recipe.items.splice(index, 0, dish)
          ElMessage.error('删除菜品失败')
        })
    }
  }
}

// 批量删除食谱
const batchDeleteRecipes = () => {
  if (selectedRecipes.value.length === 0) return

  // 确认删除
  ElMessageBox.confirm('确定要批量删除选中的食谱吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => {
      // 遍历删除选中的食谱
      const deletePromises = selectedRecipes.value.map((id) =>
        axios.delete(API_CONFIG.baseURL + API_CONFIG.recipe.delete + id)
      )

      Promise.all(deletePromises)
        .then((responses) => {
          // 删除成功，更新本地数据
          selectedRecipes.value.forEach((id) => {
            const index = myRecipes.value.findIndex((r) => r.id === id)
            if (index !== -1) {
              myRecipes.value.splice(index, 1)
            }
          })
          // 清空选中列表
          selectedRecipes.value = []
          ElMessage.success(`成功删除${responses.length}个食谱`)
        })
        .catch((error) => {
          console.error('批量删除失败:', error)
          ElMessage.error('批量删除失败')
        })
    })
    .catch(() => {
      // 取消删除
    })
}

// 批量收藏食谱
const batchFavoriteRecipes = () => {
  if (selectedRecipes.value.length === 0) return

  // 批量设置所有选中的食谱为收藏状态
  const recipeIds = selectedRecipes.value
  const favorite = true // 批量收藏

  axios
    .put(API_CONFIG.baseURL + API_CONFIG.recipe.batchToggleFavorite, {
      recipeIds,
      favorite
    })
    .then((response) => {
      // 更新本地数据
      console.log('批量收藏成功:', response)
      const updatedRecipes = response.data.data || []
      updatedRecipes.forEach((updatedRecipe) => {
        const recipeIndex = myRecipes.value.findIndex((r) => r.id === updatedRecipe.id)
        if (recipeIndex !== -1) {
          // 确保返回的食谱有items数组并已解析
          const originalRecipe = myRecipes.value[recipeIndex]
          // 使用 Object.assign 创建新对象，避免覆盖原对象
          const updatedRecipeWithParsedItems = Object.assign({}, originalRecipe, updatedRecipe, {
            items:
              typeof updatedRecipe.items === 'string'
                ? JSON.parse(updatedRecipe.items)
                : updatedRecipe.items || originalRecipe.items || [], // 确保items字段不会丢失
            // 统一字段名称，将后端返回的favorite映射为前端使用的isFavorite
            isFavorite:
              updatedRecipe.favorite !== undefined
                ? updatedRecipe.favorite
                : updatedRecipe.isFavorite
          })
          myRecipes.value[recipeIndex] = updatedRecipeWithParsedItems
        }
      })

      // 清空选中列表
      selectedRecipes.value = []
      ElMessage.success(`成功收藏${updatedRecipes.length}个食谱`)
    })
    .catch((error) => {
      console.error('批量收藏失败:', error)
      ElMessage.error('批量收藏失败')
    })
}

// 添加食谱组件相关
const addDialogVisible = ref(false)

// 添加新食谱
const handleAddRecipe = (newRecipe) => {
  myRecipes.value.push(newRecipe)
}

// 打开添加食谱对话框
const openAddDialog = () => {
  addDialogVisible.value = true
}

// 订单导入相关
const orders = ref([])
const importDialogVisible = ref(false)
const selectedOrder = ref(null)

// 从订单导入食谱
const importFromOrders = () => {
  const authStore = useAuthStore()
  const userStore = useUserStore()

  let userId = null

  // 获取用户ID
  if (authStore.userId) {
    userId = authStore.userId
  } else if (userStore.userInfo?.userId) {
    userId = userStore.userInfo.userId
  } else {
    ElMessage.error('无法获取用户ID')
    return
  }

  // 获取用户订单
  axios
    .get(`${API_CONFIG.baseURL}${API_CONFIG.order.list}${userId}`)
    .then((response) => {
      if (response.data?.code === '200' && response.data?.data) {
        orders.value = response.data.data
        importDialogVisible.value = true
      } else {
        ElMessage.warning('暂无订单数据')
      }
    })
    .catch((error) => {
      console.error('获取订单失败:', error)
      ElMessage.error('获取订单失败，请稍后重试')
    })
}

// 确认从订单导入食谱
const confirmImportFromOrder = () => {
  if (!selectedOrder.value) return

  // 构造新食谱数据
  const newRecipe = {
    name: `从订单导入 - ${selectedOrder.value.orderNo}`,
    type: '晚餐', // 默认餐型
    items: selectedOrder.value.items.map((item) => ({
      name: item.name,
      quantity: item.quantity,
      ingredients: [],
      calories: 0,
      protein: 0,
      carbs: 0,
      fat: 0
    })),
    calories: 0,
    time: '30分钟', // 默认时间
    favorite: false
  }

  // 计算总热量
  newRecipe.calories = selectedOrder.value.items.reduce(
    (sum, item) => sum + (item.calories || 0),
    0
  )

  // 调用添加食谱API
  axios
    .post(`${API_CONFIG.baseURL}${API_CONFIG.recipe.add}`, newRecipe)
    .then((response) => {
      if (response.data?.code === '200' && response.data?.data) {
        ElMessage.success('从订单导入食谱成功')
        importDialogVisible.value = false
        selectedOrder.value = null
        loadMyRecipes() // 重新加载食谱列表
      } else {
        ElMessage.error('导入食谱失败')
      }
    })
    .catch((error) => {
      console.error('导入食谱失败:', error)
      ElMessage.error('导入食谱失败，请稍后重试')
    })
}

// 导出食谱到饮食记录
const exportToDietRecord = () => {
  const authStore = useAuthStore()
  const userStore = useUserStore()

  let userId = null

  // 获取用户ID
  if (authStore.userId) {
    userId = authStore.userId
  } else if (userStore.userInfo?.userId) {
    userId = userStore.userInfo.userId
  } else {
    ElMessage.error('无法获取用户ID')
    return
  }

  // 确认导出
  ElMessageBox.confirm(
    `确定要将选中的 ${selectedRecipes.value.length} 个食谱导出到饮食记录吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    }
  )
    .then(() => {
      // 批量导出
      const exportPromises = selectedRecipes.value.map((recipeId) => {
        // 找到对应的食谱
        const recipe = myRecipes.value.find((r) => r.id === recipeId)
        if (!recipe) return Promise.resolve()

        // 构造饮食记录数据
        const dietRecord = {
          userId,
          recipeId,
          recordDate: new Date().toISOString().split('T')[0], // 今天的日期
          calories: recipe.calories,
          name: recipe.name
        }

        // 调用添加饮食记录API
        return axios.post(`${API_CONFIG.baseURL}${API_CONFIG.diet.add}`, dietRecord)
      })

      // 处理所有请求
      Promise.all(exportPromises)
        .then((responses) => {
          const successCount = responses.filter((res) => res?.data?.code === '200').length
          ElMessage.success(`成功导出 ${successCount} 个食谱到饮食记录`)
          selectedRecipes.value = [] // 清空选择
        })
        .catch((error) => {
          console.error('导出失败:', error)
          ElMessage.error('导出失败，请稍后重试')
        })
    })
    .catch(() => {
      // 取消导出
      ElMessage.info('已取消导出')
    })
}

// 获取标签类型
const getTagType = (type) => {
  switch (type) {
    case '早餐':
      return 'warning'
    case '午餐':
      return 'success'
    case '晚餐':
      return 'primary'
    case '加餐':
    case 'afternoon_tea':
    case 'tea':
      return 'info'
    case 'night_snack':
    case 'snack':
      return 'primary'
    default:
      return 'info'
  }
}
</script>

<template>
  <div class="my-recipe-container">
    <div class="recipe-header">
      <h2>我的食谱</h2>
      <div class="filter-section">
        <el-select
          v-model="recipeFilter"
          placeholder="筛选食谱"
          size="small"
          style="width: 150px; margin-right: 10px"
        >
          <el-option label="全部" value="all" />
          <el-option label="早餐" value="早餐" />
          <el-option label="午餐" value="午餐" />
          <el-option label="晚餐" value="晚餐" />
          <el-option label="加餐" value="加餐" />
        </el-select>
      </div>
    </div>

    <!-- 添加食谱和批量管理按钮 -->
    <div class="add-recipe-section">
      <!-- 第一行按钮 -->
      <div class="button-row">
        <el-button type="primary" size="small" @click="openAddDialog"> ➕ 添加食谱 </el-button>
        <el-button type="success" size="small" @click="importFromOrders"> 📥 从订单导入 </el-button>
      </div>

      <!-- 第二行按钮 -->
      <div class="button-row">
        <el-button
          type="danger"
          size="small"
          :disabled="selectedRecipes.length === 0"
          @click="batchDeleteRecipes"
        >
          🗑️ 批量删除
        </el-button>

        <el-button
          type="warning"
          size="small"
          :disabled="selectedRecipes.length === 0"
          @click="batchFavoriteRecipes"
        >
          ⭐ 批量收藏
        </el-button>

        <el-button
          type="info"
          size="small"
          :disabled="selectedRecipes.length === 0"
          @click="exportToDietRecord"
        >
          📤 导出到饮食记录
        </el-button>
      </div>
    </div>

    <!-- 食谱列表 -->
    <div class="recipe-grid">
      <el-checkbox-group v-model="selectedRecipes">
        <!-- 食谱卡片 -->
        <el-card
          v-for="recipe in filteredRecipes"
          :key="recipe.id"
          class="recipe-card"
          :class="[recipe.type, { 'recipe-card-favorited': recipe.favorite }]"
        >
          <template #header>
            <div class="card-header">
              <!-- 批量选择复选框 -->
              <div class="checkbox-wrapper">
                <el-checkbox :label="recipe.id"></el-checkbox>
              </div>
              <span :class="`meal-icon ${recipe.type}`">
                {{
                  recipe.type === '早餐'
                    ? '🥣'
                    : recipe.type === '午餐'
                      ? '🍚'
                      : recipe.type === '晚餐'
                        ? '🍱'
                        : recipe.type === '加餐'
                          ? '🍪'
                          : '🍴'
                }}
              </span>
              {{ recipe.name }}
              <!-- 右上角收藏按钮 -->
              <div class="card-favorite">
                <el-button
                  type="text"
                  size="small"
                  :class="{ 'favorite-btn': recipe.favorite }"
                  style="padding: 0; margin: 0; font-size: 18px"
                  @click="toggleFavorite(recipe)"
                >
                  {{ recipe.favorite ? '⭐' : '☆' }}
                </el-button>
              </div>
            </div>
          </template>
          <div class="recipe-items">
            <el-tag
              v-for="(item, index) in recipe.items || recipe.ingredients || ['暂无食材信息']"
              :key="index"
              :type="getTagType(recipe.type)"
            >
              {{ typeof item === 'string' ? item : item.name }}
            </el-tag>
          </div>
          <div class="recipe-stats">
            <div class="stat-item">
              <span>🔥</span>
              <span>{{ recipe.calories }} kcal</span>
            </div>
            <div class="stat-item">
              <span>⏰</span>
              <span>{{ recipe.time }}</span>
            </div>
          </div>
          <div class="recipe-actions">
            <el-button type="text" size="small" @click="viewRecipeDetails(recipe)"
              >查看详情</el-button
            >
            <el-button type="text" size="small" @click="addDish(recipe)">添加菜品</el-button>
            <el-button type="text" size="small" @click="openImportMerchantDish(recipe)"
              >导入商家菜品</el-button
            >
            <!-- 替换菜品按钮：仅在有菜品时显示 -->
            <el-dropdown
              v-if="
                (recipe.items && recipe.items.length > 0) ||
                (recipe.ingredients && recipe.ingredients.length > 0)
              "
              trigger="click"
            >
              <el-button type="text" size="small">
                替换菜品
                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item
                    v-for="dish in recipe.items || recipe.ingredients || []"
                    :key="dish.id || dish"
                    @click="replaceDish(recipe, dish)"
                  >
                    {{ typeof dish === 'object' ? dish.name : dish }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-dropdown
              v-if="
                (recipe.items && recipe.items.length > 0) ||
                (recipe.ingredients && recipe.ingredients.length > 0)
              "
              trigger="click"
            >
              <el-button type="text" size="small">
                删除菜品
                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item
                    v-for="dish in recipe.items || recipe.ingredients || []"
                    :key="dish.id || dish"
                    @click="deleteDish(recipe, dish)"
                  >
                    {{ typeof dish === 'object' ? dish.name : dish }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-card>
      </el-checkbox-group>
    </div>

    <!-- 空数据提示 -->
    <el-empty
      v-if="filteredRecipes.length === 0"
      :description="loadingFailed ? '暂未找到我的食谱' : '暂无食谱'"
    ></el-empty>
  </div>

  <!-- 食谱详情组件 -->
  <RecipeDetail
    v-model:visible="detailDialogVisible"
    v-model:recipe="selectedRecipe"
    @update:recipe="updateRecipe"
    @update:cook-time="handleUpdateCookTime"
  />

  <!-- 替换菜品对话框 -->
  <el-dialog
    v-model="replaceDialogVisible"
    :title="selectedDish ? `替换 ${selectedDish.name}` : '替换菜品'"
    width="600px"
    top="10%"
  >
    <div v-if="selectedDish" class="replace-dish-container">
      <div class="current-dish">
        <span class="detail-label">当前菜品:</span>
        <span class="detail-value">{{ selectedDish.name }}</span>
      </div>

      <div class="available-dishes">
        <span class="detail-label">可选菜品:</span>
        <div class="dish-list">
          <el-card
            v-for="dish in replacementDishes"
            :key="dish.id"
            :class="dish.type"
            class="dish-card"
            @click="confirmReplaceDish(dish)"
          >
            <div class="dish-name">{{ dish.name }}</div>
            <div class="dish-nutrition">{{ dish.nutrition }}</div>
          </el-card>
        </div>
      </div>

      <el-divider />
    </div>
  </el-dialog>

  <!-- 添加菜品组件 -->
  <AddDish
    v-model:visible="addDishVisible"
    :recipe="selectedRecipe"
    @add="handleAddDish"
    @close="selectedRecipe = null"
  ></AddDish>

  <!-- 导入商家菜品组件 -->
  <ImportMerchantDish
    v-model:visible="importMerchantDishVisible"
    :recipe="selectedRecipe"
    :merchants="merchants"
    @import="handleImportMerchantDishes"
    @close="selectedRecipe = null"
  ></ImportMerchantDish>

  <!-- 添加食谱组件 -->
  <AddRecipe v-model:visible="addDialogVisible" @add-recipe="handleAddRecipe" />

  <!-- 从订单导入对话框 -->
  <el-dialog
    v-model="importDialogVisible"
    title="从订单导入食谱"
    width="600px"
    top="10%"
    @close="selectedOrder = null"
  >
    <div v-if="orders.length > 0">
      <el-select
        v-model="selectedOrder"
        placeholder="请选择要导入的订单"
        style="width: 100%"
        size="large"
      >
        <el-option
          v-for="order in orders"
          :key="order.id"
          :label="`订单号: ${order.orderNo} (${new Date(order.createTime).toLocaleString()})`"
          :value="order"
        />
      </el-select>

      <div v-if="selectedOrder" style="margin-top: 20px">
        <h4>订单详情:</h4>
        <p>订单号: {{ selectedOrder.orderNo }}</p>
        <p>创建时间: {{ new Date(selectedOrder.createTime).toLocaleString() }}</p>
        <h5>菜品:</h5>
        <el-tag
          v-for="(item, index) in selectedOrder.items"
          :key="index"
          type="info"
          style="margin: 2px"
        >
          {{ item.name }} ({{ item.quantity }})
        </el-tag>
      </div>
    </div>
    <div v-else>暂无订单数据</div>

    <template #footer>
      <el-button @click="importDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="confirmImportFromOrder">导入</el-button>
    </template>
  </el-dialog>
</template>

<style lang="less">
.my-recipe-container {
  padding: 24px;
  min-height: 100vh;
  background: #f5f7fa;

  .recipe-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;

    h2 {
      font-size: 32px;
      margin: 0;
      color: #333;
    }

    .filter-section {
      display: flex;
      align-items: center;
    }
  }

  .recipe-grid {
    display: grid;
    grid-template-columns: 1fr; /* 单列显示 */
    gap: 20px;
    max-width: calc(100% - 80px); /* 调整宽度，预留左右边距 */
    margin: 0 auto; /* 居中显示 */
  }

  .recipe-card {
    margin-bottom: 16px !important;
    background: linear-gradient(135deg, #ffffff 0%, #f8f9ff 100%) !important;
    border-radius: 20px !important;
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
    border: 1px solid rgba(255, 255, 255, 0.8) !important;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    overflow: hidden;
    position: relative;

    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 4px;
      background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
    }

    &.recipe-card-favorited {
      border: 2px solid #ffd700 !important;
      box-shadow:
        0 8px 30px rgba(255, 215, 0, 0.15),
        0 0 0 3px rgba(255, 215, 0, 0.05);
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

      &::before {
        background: linear-gradient(90deg, #ffd700 0%, #ffed4e 100%);
      }
    }

    &:hover {
      transform: translateY(-6px);
      box-shadow: 0 12px 35px rgba(0, 0, 0, 0.15);
      border-color: rgba(255, 255, 255, 1) !important;
    }

    .card-header {
      position: relative;
      display: flex;
      align-items: center;
      gap: 16px;
      font-size: 20px;
      font-weight: 700;
      color: #2c3e50;
      padding: 20px 24px !important;

      .meal-icon {
        font-size: 32px;
        padding: 10px;
        border-radius: 50%;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: white;
        box-shadow: none;
        display: flex;
        align-items: center;
        justify-content: center;
        width: 56px;
        height: 56px;
      }
    }

    .recipe-items {
      margin: 24px;
      display: flex;
      flex-wrap: wrap;
      gap: 12px;

      .el-tag {
        padding: 8px 16px;
        border-radius: 20px;
        font-size: 14px;
        font-weight: 500;
      }
    }

    .recipe-stats {
      margin: 0 24px;
      display: flex;
      gap: 20px;

      .stat-item {
        display: flex;
        align-items: center;
        gap: 5px;
        color: #666;
      }
    }

    .recipe-actions {
      display: flex;
      justify-content: flex-end;
      margin: 20px 24px 24px;
      gap: 8px; /* 统一间距 */

      .el-button {
        font-size: 14px;
        padding: 6px 16px;
        border-radius: 8px;
        margin: 0;
      }
    }
  }

  // 右上角收藏按钮样式
  .card-favorite {
    position: absolute;
    right: 10px;
    top: 50%;
    transform: translateY(-50%);
  }

  // 批量选择复选框样式
  .checkbox-wrapper {
    :deep(.el-checkbox__label) {
      display: none !important;
    }
    margin-right: 10px;
  }

  // 添加食谱按钮样式
  .add-recipe-section {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    margin-bottom: 24px;
    gap: 12px; /* 行与行之间的间距 */

    .button-row {
      display: flex;
      gap: 12px; /* 按钮之间的间距 */

      .el-button {
        border-radius: 24px !important;
        padding: 10px 24px !important;
        font-weight: 600 !important;
      }
    }
  }

  // 收藏按钮样式
  .favorite-btn {
    color: #ffd700 !important; // 收藏状态用金色，确保覆盖默认样式
    font-weight: bold;
  }

  /* 添加食谱表单样式 */
  .add-recipe-form {
    padding: 20px 0;

    .el-form {
      max-width: 400px;
      margin: 0 auto;
    }

    .el-form-item {
      margin-bottom: 20px;
    }
  }

  /* 不同类型食谱卡片的样式 */
  .recipe-card {
    &.早餐 {
      border-left: 4px solid #ffc107;

      &::before {
        background: linear-gradient(90deg, #ffc107 0%, #ffeb3b 100%);
      }

      .meal-icon {
        background: linear-gradient(135deg, #ffc107 0%, #ffeb3b 100%) !important;
        color: #333 !important;
      }
    }

    &.午餐 {
      border-left: 4px solid #4caf50;

      &::before {
        background: linear-gradient(90deg, #4caf50 0%, #8bc34a 100%);
      }

      .meal-icon {
        background: linear-gradient(135deg, #4caf50 0%, #8bc34a 100%) !important;
        color: white !important;
      }
    }

    &.晚餐 {
      border-left: 4px solid #2196f3;

      &::before {
        background: linear-gradient(90deg, #2196f3 0%, #64b5f6 100%);
      }

      .meal-icon {
        background: linear-gradient(135deg, #2196f3 0%, #64b5f6 100%) !important;
        color: white !important;
      }
    }

    &.加餐 {
      border-left: 4px solid #1e88e5;

      &::before {
        background: linear-gradient(90deg, #1e88e5 0%, #42a5f5 100%);
      }

      .meal-icon {
        background: linear-gradient(135deg, #1e88e5 0%, #42a5f5 100%) !important;
        color: white !important;
      }
    }
  }

  /* 自定义标签颜色和交互 */
  :deep(.el-tag) {
    transition: all 0.3s ease;
    cursor: pointer;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    }
  }

  :deep(.el-tag--warning) {
    background-color: #fff3e0;
    color: #f57c00;
  }

  :deep(.el-tag--success) {
    background-color: #e8f5e9;
    color: #388e3c;
  }

  :deep(.el-tag--primary) {
    background-color: #e3f2fd;
    color: #1976d2;
  }

  :deep(.el-tag--info) {
    background-color: #e1f5fe;
    color: #0288d1;
  }

  :deep(.el-tag--purple) {
    background-color: #f3e5f5;
    color: #7b1fa2;
  }
  :deep(.el-tag--blue) {
    background-color: #e3f2fd;
    color: #1565c0;
  }

  .recipe-detail-dialog {
    padding: 24px;
    background-color: #f5f7fa;
    font-family:
      -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  }

  .detail-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 700px;
    border: 1px solid red;
  }
}
</style>
