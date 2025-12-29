<script setup>
import { ref, computed, onMounted } from 'vue'
import { ArrowDown } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { API_CONFIG } from '../../config'
import axios from 'axios'
import { useAuthStore } from '../../store/authStore'

// 导入自定义组件
import RecipeDetail from '../../components/RecipeDetail.vue'
import AddDish from '../../components/AddDish.vue'
import ImportMerchantDish from '../../components/ImportMerchantDish.vue'
import AddRecipe from '../../components/recipe/AddRecipe.vue'

// 餐型图标映射
const getMealIcon = (type) => {
  const mealTypeIcons = {
    breakfast: '🥣',
    lunch: '🍚',
    dinner: '🍱',
    afternoon_tea: '🍵',
    tea: '🍵',
    night_snack: '🍪',
    snack: '🍪',
    morning_snack: '🥐',
    brunch: '🥐',
    supper: '🌙',
    midnight_snack: '🌙',
    health_snack: '💪',
    fitness_meal: '💪',
    dessert: '🍰',
    sweet: '🍰',
    soup: '🍲',
    porridge: '🍲',
    salad: '🥗',
    vegetable: '🥗',
    meat: '🥩',
    protein: '🥩'
  }
  return mealTypeIcons[type] || '🍴'
}

// 获取认证信息
const authStore = useAuthStore()

// 今日食谱数据
const todayRecipes = ref([])

// 批量操作相关变量
const selectedRecipes = ref([]) // 存储选中的食谱

// 添加菜品相关变量
const newDish = ref({
  name: '',
  ingredients: [],
  calories: 0,
  protein: 0,
  carbs: 0,
  fat: 0
})
const newIngredient = ref('') // 单个食材输入

// 营养摄入数据 - 从菜品数据计算得出
// TODO: 未来可以通过AI预测菜品营养数据，当前若菜品无数据则默认为0
const nutritionData = computed(() => {
  let totalCalories = 0
  let totalProtein = 0
  let totalCarbs = 0
  let totalFat = 0

  // 遍历筛选后的食谱和菜品，确保recipe和recipe.items存在
  filteredRecipes.value.forEach((recipe) => {
    if (recipe && recipe.items) {
      // 确保recipe.items是数组
      const items = Array.isArray(recipe.items)
        ? recipe.items
        : typeof recipe.items === 'string'
          ? JSON.parse(recipe.items)
          : []
      items.forEach((dish) => {
        // 如果菜品有营养数据则累加，否则默认为0
        totalCalories += dish?.calories || 0
        totalProtein += dish?.protein || 0
        totalCarbs += dish?.carbs || 0
        totalFat += dish?.fat || 0
      })
    }
  })

  return {
    calories: totalCalories,
    protein: totalProtein,
    carbs: totalCarbs,
    fat: totalFat
  }
})

// 筛选条件
const filters = ref({
  mealType: 'all'
})

// 加载今日食谱数据
const loadTodayRecipes = () => {
  // 确保有userId
  if (!authStore.userId) {
    console.error('加载今日食谱失败: 缺少userId')
    ElMessage.error('加载今日食谱失败: 用户未登录')
    todayRecipes.value = []
    return
  }

  axios
    .get(API_CONFIG.baseURL + API_CONFIG.recipe.today, {
      params: {
        userId: authStore.userId
      }
    })
    .then((response) => {
      // console.log(response) ;
      if (
        response.data.data &&
        response.data.data.recipes &&
        response.data.data.recipes.length > 0
      ) {
        // console.log('加载今日食谱成功:', response.data.data.recipes);
        // 确保所有食谱都有items数组，并且移除任何null或无效的食谱
        todayRecipes.value = response.data.data.recipes
          .filter((recipe) => recipe && recipe.id) // 确保食谱存在且有id
          .map((recipe) => ({
            ...recipe,
            items: typeof recipe.items === 'string' ? JSON.parse(recipe.items) : recipe.items || []
          }))
        // nutritionData now computed from recipe items, no need for direct assignment
      } else {
        // 后端没有返回数据
        todayRecipes.value = []
      }
    })
    .catch((error) => {
      console.error('加载今日食谱失败:', error)
      // 请求失败时，也显示没有数据
      todayRecipes.value = []
      ElMessage.error('加载今日食谱失败')
    })
}

// 组件挂载时加载数据
onMounted(() => {
  loadTodayRecipes()
})

// 默认使用一列布局
const layoutType = ref('one-column')

// 获取标签类型
const getTagType = (type) => {
  switch (type) {
    case 'breakfast':
      return 'warning'
    case 'lunch':
      return 'success'
    case 'dinner':
      return 'primary'
    case 'afternoon_tea':
    case 'tea':
      return 'purple'
    case 'night_snack':
    case 'snack':
      return 'blue'
    case 'morning_snack':
    case 'brunch':
      return 'orange'
    case 'supper':
    case 'midnight_snack':
      return 'cyan'
    case 'health_snack':
    case 'fitness_meal':
      return 'green'
    case 'dessert':
    case 'sweet':
      return 'pink'
    case 'soup':
    case 'porridge':
      return 'teal'
    case 'salad':
    case 'vegetable':
      return 'success'
    case 'meat':
    case 'protein':
      return 'brown'
    default:
      return 'info'
  }
}

// 模态框状态
const detailDialogVisible = ref(false)
const replaceDialogVisible = ref(false)
const addDishVisible = ref(false)

// 当前选中的食谱和菜品
const selectedRecipe = ref(null)
const selectedDish = ref(null)

// 自定义菜品
const showCustomDishInput = ref(false)
const customDishName = ref('')

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

// 导入商家菜品对话框
const importMerchantDishVisible = ref(false)

// 订单列表 使用mock数据
const orders = ref([
  {
    id: 1,
    orderNo: 'ORDER_001',
    totalPrice: 89.5,
    dishes: [
      { name: '宫保鸡丁', nutrition: '250kcal/份' },
      { name: '清炒西兰花', nutrition: '120kcal/份' },
      { name: '米饭', nutrition: '110kcal/碗' }
    ]
  },
  {
    id: 2,
    orderNo: 'ORDER_002',
    totalPrice: 68.0,
    dishes: [
      { name: '番茄鸡蛋面', nutrition: '320kcal/份' },
      { name: '凉拌黄瓜', nutrition: '80kcal/份' }
    ]
  }
])

// 导入订单对话框
const importOrderVisible = ref(false)
const selectedOrder = ref(null)

// 替换菜品列表 mock数据
const replacementDishes = ref([
  { id: 1, name: '全麦面包', type: 'breakfast', nutrition: '247kcal/片' },
  { id: 2, name: '蒸南瓜', type: 'breakfast', nutrition: '26kcal/100g' },
  { id: 3, name: '烤鸡胸肉', type: 'lunch', nutrition: '165kcal/100g' },
  { id: 4, name: '西兰花', type: 'lunch', nutrition: '34kcal/100g' },
  { id: 5, name: '清蒸鱼', type: 'dinner', nutrition: '105kcal/100g' },
  { id: 6, name: '炒青菜', type: 'dinner', nutrition: '15kcal/100g' }
])

// 添加食谱对话框
const addRecipeVisible = ref(false)

// 查看详情
const viewRecipeDetails = (recipe) => {
  selectedRecipe.value = recipe
  detailDialogVisible.value = true
}

// 更新烹饪时间
const handleUpdateCookTime = (newCookTime) => {
  if (selectedRecipe.value) {
    // 保存原始值，用于失败时恢复
    const originalCookTime = selectedRecipe.value.cookTime

    // 更新本地数据
    selectedRecipe.value.cookTime = newCookTime

    // 在todayRecipes数组中找到对应的食谱并更新
    const index = todayRecipes.value.findIndex((recipe) => recipe.id === selectedRecipe.value.id)
    if (index !== -1) {
      todayRecipes.value[index].cookTime = newCookTime
    }

    // 调用后端API更新食谱
    axios
      .put(API_CONFIG.baseURL + API_CONFIG.recipe.update + selectedRecipe.value.id, {
        ...selectedRecipe.value,
        cookTime: newCookTime
      })
      .then((response) => {
        console.log('更新烹饪时间成功:', response)
        ElMessage.success('烹饪时间已更新')
      })
      .catch((error) => {
        console.error('更新烹饪时间失败:', error)
        // 显示错误信息给用户
        ElMessage.error('更新烹饪时间失败，请稍后重试')
        // 恢复到原始值
        selectedRecipe.value.cookTime = originalCookTime
        // 在todayRecipes数组中恢复原始值
        if (index !== -1) {
          todayRecipes.value[index].cookTime = originalCookTime
        }
      })
  }
}

// 更新食谱详情
const handleUpdateDetails = (newDetails) => {
  if (selectedRecipe.value) {
    // 保存原始值，用于失败时恢复
    const originalDetails = selectedRecipe.value.details

    // 更新本地数据
    selectedRecipe.value.details = newDetails

    // 在todayRecipes数组中找到对应的食谱并更新
    const index = todayRecipes.value.findIndex((recipe) => recipe.id === selectedRecipe.value.id)
    if (index !== -1) {
      todayRecipes.value[index].details = newDetails
    }

    // 调用后端API更新食谱
    axios
      .put(API_CONFIG.baseURL + API_CONFIG.recipe.update + selectedRecipe.value.id, {
        ...selectedRecipe.value,
        details: newDetails
      })
      .then((response) => {
        console.log('更新食谱详情成功:', response)
        ElMessage.success('食谱详情已更新')
      })
      .catch((error) => {
        console.error('更新食谱详情失败:', error)
        // 显示错误信息给用户
        ElMessage.error('更新食谱详情失败，请稍后重试')
        // 恢复到原始值
        if (selectedRecipe.value) {
          selectedRecipe.value.details = originalDetails
        }
        // 在todayRecipes数组中恢复原始值
        const index = todayRecipes.value.findIndex((recipe) => recipe.id === selectedRecipe.value.id)
        if (index !== -1) {
          todayRecipes.value[index].details = originalDetails
        }
      })
  }
}

// 替换菜品
const replaceDish = (recipe, dish) => {
  selectedRecipe.value = recipe
  selectedDish.value = dish
  replaceDialogVisible.value = true
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
          const recipeIndex = todayRecipes.value.findIndex((r) => r.id === selectedRecipe.value.id)
          if (recipeIndex !== -1) {
            // 确保返回的食谱有items数组并已解析
            const updatedRecipe = {
              ...response.data.data,
              items:
                typeof response.data.data.items === 'string'
                  ? JSON.parse(response.data.data.items)
                  : response.data.data.items || []
            }
            todayRecipes.value[recipeIndex] = updatedRecipe
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

// 添加菜品
const addDish = (recipe) => {
  // 确保recipe.items是数组
  recipe.items = recipe.items || []
  selectedRecipe.value = recipe
  addDishVisible.value = true
}

// 确认添加菜品（已迁移到组件）
// const confirmAddDish = () => {

// 确认从订单导入食谱
const confirmImportOrder = () => {
  if (selectedOrder.value) {
    console.log('Selected order:', selectedOrder.value)
    // 创建新食谱数据
    const newRecipeData = {
      name: `订单-${selectedOrder.value.orderNo}`,
      type: 'dinner', // 默认类型，可根据实际情况调整
      items: selectedOrder.value.dishes.map((dish) => ({
        name: dish.name,
        ingredients: [],
        calories: 0,
        protein: 0,
        carbs: 0,
        fat: 0
      })),
      userId: authStore.userId
    }

    // 调用后端API添加食谱 - 将items转换为JSON字符串
    const newRecipeDataWithStringItems = {
      ...newRecipeData,
      items: JSON.stringify(newRecipeData.items)
    }

    axios
      .post(API_CONFIG.baseURL + API_CONFIG.recipe.add, newRecipeDataWithStringItems)
      .then((response) => {
        // 检查返回的数据是否有效
        if (response.data.data) {
          // 确保返回的食谱有items数组并已解析
          const newRecipe = {
            ...response.data.data,
            items:
              typeof response.data.data.items === 'string'
                ? JSON.parse(response.data.data.items)
                : response.data.data.items || []
          }
          // 将返回的食谱添加到本地列表
          todayRecipes.value.push(newRecipe)
          ElMessage.success('订单已成功导入为新食谱')
          importOrderVisible.value = false
          selectedOrder.value = null
        } else {
          ElMessage.error('导入订单失败: 服务器返回无效数据')
        }
      })
      .catch((error) => {
        console.error('导入订单失败:', error)
        ElMessage.error('导入订单失败')
      })
  }
}

// 确认添加菜品
const confirmAddDish = () => {
  if (selectedRecipe.value && newDish.value.name.trim()) {
    // 验证菜品名称格式
    if (!isValidDishName(newDish.value.name)) {
      ElMessage.error('菜品名称只能包含中文、英文、数字和常见符号')
      return
    }

    // 如果有食材，将菜品和食材一起保存
    const dishWithIngredients = {
      name: newDish.value.name,
      ingredients: [...newDish.value.ingredients],
      calories: newDish.value.calories,
      protein: newDish.value.protein,
      carbs: newDish.value.carbs,
      fat: newDish.value.fat
    }

    // 先添加到本地
    selectedRecipe.value.items.push(dishWithIngredients)

    // 调用后端API更新食谱 - 将items转换为JSON字符串
    const updateData = {
      ...selectedRecipe.value,
      items: JSON.stringify(selectedRecipe.value.items)
    }

    axios
      .put(API_CONFIG.baseURL + API_CONFIG.recipe.update + selectedRecipe.value.id, updateData)
      .then((response) => {
        // 更新本地数据 - 确保items字段已解析
        const recipeIndex = todayRecipes.value.findIndex((r) => r.id === selectedRecipe.value.id)
        if (recipeIndex !== -1) {
          // 确保返回的食谱有items数组并已解析
          const updatedRecipe = {
            ...response.data.data,
            items:
              typeof response.data.data.items === 'string'
                ? JSON.parse(response.data.data.items)
                : response.data.data.items || []
          }
          todayRecipes.value[recipeIndex] = updatedRecipe
        }

        ElMessage.success('菜品已添加')
        addDishVisible.value = false

        // 重置表单
        newDish.value = {
          name: '',
          ingredients: [],
          calories: 0,
          protein: 0,
          carbs: 0,
          fat: 0
        }
        newIngredient.value = ''

        selectedRecipe.value = null
      })
      .catch((error) => {
        console.error('添加菜品失败:', error)
        // 失败时恢复本地数据
        selectedRecipe.value.items.pop()
        ElMessage.error('添加菜品失败')
      })
  } else {
    ElMessage.error('请输入菜品名称')
  }
}

// 验证菜品名称格式的函数
const isValidDishName = (name) => {
  // 允许中文、英文、数字、空格以及常见的标点符号
  const nameRegex = /^[\u4e00-\u9fa5a-zA-Z0-9\s\-_\(\)\[\]\{\}\/\.\,，。！？；：]*$/
  return nameRegex.test(name.trim())
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
        const recipeIndex = todayRecipes.value.findIndex((r) => r.id === recipe.id)
        if (recipeIndex !== -1) {
          // 确保返回的食谱有items数组并已解析
          const updatedRecipe = {
            ...response.data.data,
            items:
              typeof response.data.data.items === 'string'
                ? JSON.parse(response.data.data.items)
                : response.data.data.items || []
          }
          todayRecipes.value[recipeIndex] = updatedRecipe
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
          const recipeIndex = todayRecipes.value.findIndex((r) => r.id === recipe.id)
          if (recipeIndex !== -1) {
            todayRecipes.value[recipeIndex] = response.data.data
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
            const index = todayRecipes.value.findIndex((r) => r.id === id)
            if (index !== -1) {
              todayRecipes.value.splice(index, 1)
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
          const recipeIndex = todayRecipes.value.findIndex((r) => r.id === recipe.id)
          if (recipeIndex !== -1) {
            // 确保返回的食谱有items数组并已解析
            const updatedRecipe = {
              ...response.data.data,
              items:
                typeof response.data.data.items === 'string'
                  ? JSON.parse(response.data.data.items)
                  : response.data.data.items || []
            }
            todayRecipes.value[recipeIndex] = updatedRecipe
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
        const recipeIndex = todayRecipes.value.findIndex((r) => r.id === updatedRecipe.id)
        if (recipeIndex !== -1) {
          // 确保返回的食谱有items数组并已解析
          const originalRecipe = todayRecipes.value[recipeIndex]
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
          todayRecipes.value[recipeIndex] = updatedRecipeWithParsedItems
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

// 添加新菜单
const addNewMenu = () => {
  // 这个函数已经被AddRecipe组件替换
}

// 处理从AddRecipe组件添加的新食谱
const handleAddRecipe = (newRecipeData) => {
  const authStore = useAuthStore()

  // 准备食谱数据 - 将type转换为小写
  const recipeData = {
    ...newRecipeData,
    type: newRecipeData.type.toLowerCase(), // 保持与系统一致的小写格式
    items: newRecipeData.ingredients || [], // 映射字段
    userId: authStore.userId || 0 // 使用当前用户ID
  }

  // 调用后端API添加食谱 - 将items转换为JSON字符串
  const recipeDataWithStringItems = {
    ...recipeData,
    items: JSON.stringify(recipeData.items)
  }

  axios
    .post(API_CONFIG.baseURL + API_CONFIG.recipe.add, recipeDataWithStringItems)
    .then((response) => {
      // 检查返回的数据是否有效
      if (response.data.data) {
        // 确保返回的食谱有items数组并已解析
        const newRecipe = {
          ...response.data.data,
          items:
            typeof response.data.data.items === 'string'
              ? JSON.parse(response.data.data.items)
              : response.data.data.items || [],
          isFavorite: response.data.data.favorite || false // 映射收藏字段
        }

        // 将返回的食谱添加到本地列表
        todayRecipes.value.push(newRecipe)
        ElMessage.success('食谱已添加')

      } else {
        ElMessage.error('添加食谱失败: 服务器返回无效数据')
      }
    })
    .catch((error) => {
      console.error('添加食谱失败:', error)
      ElMessage.error('添加食谱失败')
    })
}

// 处理自定义菜品替换
const handleCustomDishReplacement = () => {
  if (customDishName.value.trim()) {
    // 验证菜品名称格式
    if (!isValidDishName(customDishName.value)) {
      ElMessage.error('菜品名称只能包含中文、英文、数字和常见符号')
      return
    }

    confirmReplaceDish({
      name: customDishName.value.trim(),
      type: selectedRecipe.value.type
    })
    customDishName.value = ''
  }
}

// 单个食谱收藏/取消收藏
const toggleRecipeFavorite = (recipe) => {
  // 发送API请求切换收藏状态
  axios
    .put(API_CONFIG.baseURL + API_CONFIG.recipe.toggleFavorite + recipe.id, {})
    .then((response) => {
      console.log('切换收藏状态成功:', response)
      const updatedRecipe = response.data.data
      if (updatedRecipe && updatedRecipe.id) {
        // 更新本地数据 - 确保items字段已解析
        const recipeIndex = todayRecipes.value.findIndex((r) => r.id === updatedRecipe.id)
        if (recipeIndex !== -1) {
          // 确保返回的食谱有items数组并已解析
          const originalRecipe = todayRecipes.value[recipeIndex]
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
          todayRecipes.value[recipeIndex] = updatedRecipeWithParsedItems
        }
        // 显示提示
        if (updatedRecipe.isFavorite) {
          ElMessage.success('食谱已收藏')
        } else {
          ElMessage.success('食谱已取消收藏')
        }
      } else {
        // API请求失败，本地切换收藏状态
        recipe.isFavorite = !recipe.isFavorite
        // 显示提示
        if (recipe.isFavorite) {
          ElMessage.success('食谱已收藏')
        } else {
          ElMessage.success('食谱已取消收藏')
        }
      }
    })
    .catch((error) => {
      console.error('切换收藏状态失败:', error)
      // 网络错误时，本地切换收藏状态作为降级方案
      recipe.isFavorite = !recipe.isFavorite
      // 显示提示
      if (recipe.isFavorite) {
        ElMessage.warning('网络异常，已在本地标记为收藏')
      } else {
        ElMessage.warning('网络异常，已在本地取消收藏')
      }
    })
}

// 打开导入商家菜品对话框
const openImportMerchantDish = (recipe) => {
  selectedRecipe.value = recipe
  importMerchantDishVisible.value = true
}

// 筛选后的食谱列表
const filteredRecipes = computed(() => {
  let filtered = [...todayRecipes.value]

  // 首先筛选掉null和没有id的食谱
  filtered = filtered.filter((recipe) => recipe && recipe.id)

  // 按收藏状态和修改时间排序：收藏的食谱置顶，然后按照修改时间从晚到早排序（更改越晚越靠前）
  filtered.sort((a, b) => {
    // 首先比较收藏状态，收藏的排前面
    if (a.isFavorite && !b.isFavorite) return -1
    if (!a.isFavorite && b.isFavorite) return 1

    // 如果收藏状态相同，比较修改时间（假设字段名为updateTime）
    // 这里需要根据实际字段名调整，如果没有则可以注释这部分
    const timeA = new Date(a.updateTime || 0)
    const timeB = new Date(b.updateTime || 0)
    return timeB - timeA // 从晚到早排序（最新修改的在最上面）
  })

  // 餐型筛选
  if (filters.value.mealType !== 'all') {
    if (filters.value.mealType === 'snack') {
      // 加餐包含所有零食类餐型
      filtered = filtered.filter(
        (recipe) =>
          recipe &&
          [
            // 再次确保recipe不为null
            'snack',
            'night_snack',
            'morning_snack',
            'afternoon_tea',
            'tea',
            'brunch',
            'midnight_snack'
          ].includes(recipe.type)
      )
    } else {
      filtered = filtered.filter((recipe) => recipe && recipe.type === filters.value.mealType)
    }
  }

  return filtered
})
</script>

<template>
  <div class="today-recipe-container">
    <div class="recipe-header">
      <h2>今日食谱</h2>
      <div class="filter-container">
        <!-- 餐型筛选 -->
        <div class="meal-type-tabs">
          <el-button
            type="primary"
            :plain="filters.mealType !== 'all'"
            size="small"
            @click="filters.mealType = 'all'"
          >
            全部
          </el-button>
          <el-button
            type="primary"
            :plain="filters.mealType !== 'breakfast'"
            size="small"
            @click="filters.mealType = 'breakfast'"
          >
            早餐
          </el-button>
          <el-button
            type="primary"
            :plain="filters.mealType !== 'lunch'"
            size="small"
            @click="filters.mealType = 'lunch'"
          >
            午餐
          </el-button>
          <el-button
            type="primary"
            :plain="filters.mealType !== 'dinner'"
            size="small"
            @click="filters.mealType = 'dinner'"
          >
            晚餐
          </el-button>
          <el-button
            type="primary"
            :plain="filters.mealType !== 'snack'"
            size="small"
            @click="filters.mealType = 'snack'"
          >
            加餐
          </el-button>
        </div>
      </div>
    </div>

    <!-- 营养摄入统计 -->
    <el-card class="nutrition-card">
      <template #header>
        <div class="card-header">营养摄入统计</div>
      </template>
      <div class="nutrition-stats">
        <div class="stat-item">
          <div class="stat-label">卡路里</div>
          <div class="stat-value">{{ nutritionData.calories }} kcal</div>
        </div>
        <div class="stat-item">
          <div class="stat-label">蛋白质</div>
          <div class="stat-value">{{ nutritionData.protein }} g</div>
        </div>
        <div class="stat-item">
          <div class="stat-label">碳水化合物</div>
          <div class="stat-value">{{ nutritionData.carbs }} g</div>
        </div>
        <div class="stat-item">
          <div class="stat-label">脂肪</div>
          <div class="stat-value">{{ nutritionData.fat }} g</div>
        </div>
      </div>
    </el-card>

    <!-- 添加菜单按钮和布局切换 -->
    <div class="add-recipe-section">
      <el-button type="primary" size="small" @click="addRecipeVisible = true">
        ➕ 添加食谱
      </el-button>

      <el-button type="success" size="small" @click="importOrderVisible = true">
        ➕ 从订单导入
      </el-button>

      <!-- 批量管理按钮 -->
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
    </div>
    <!-- 食谱列表 -->
    <div :class="['recipe-list', layoutType]">
      <div v-if="filteredRecipes.length === 0" class="no-recipes-message">
        <el-empty description="今日没有食谱数据"></el-empty>
      </div>
      <el-checkbox-group v-else v-model="selectedRecipes">
        <el-card
          v-for="recipe in filteredRecipes"
          :key="recipe.id"
          class="recipe-card"
          :class="[recipe.type, { 'recipe-card-favorited': recipe.isFavorite }]"
        >
          <template #header>
            <div class="card-header">
              <!-- 批量选择复选框 -->
              <div class="checkbox-wrapper">
                <el-checkbox :label="recipe.id"></el-checkbox>
              </div>
              <span class="meal-icon">
                {{ getMealIcon(recipe?.type) }}
              </span>
              {{ recipe.name }}
              <!-- 右上角收藏按钮 -->
              <div class="card-favorite">
                <el-button
                  type="text"
                  size="small"
                  :class="{ 'favorite-btn': recipe.isFavorite }"
                  style="padding: 0; margin: 0; font-size: 18px"
                  @click="toggleRecipeFavorite(recipe)"
                >
                  {{ recipe.isFavorite ? '⭐' : '☆' }}
                </el-button>
              </div>
            </div>
          </template>
          <div class="recipe-items">
            <el-tag
              v-for="(item, index) in recipe.items && recipe.items.length > 0
                ? recipe.items
                : ['待添加菜品']"
              :key="index"
              :type="getTagType(recipe.type)"
            >
              {{ typeof item === 'object' ? item.name : item }}
            </el-tag>
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
            <el-dropdown v-if="recipe.items && recipe.items.length > 0" trigger="click">
              <el-button type="text" size="small">
                替换菜品
                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item
                    v-for="dish in recipe.items || []"
                    :key="dish.id || dish"
                    @click="replaceDish(recipe, dish)"
                  >
                    {{ typeof dish === 'object' ? dish.name : dish }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-dropdown v-if="recipe.items && recipe.items.length > 0" trigger="click">
              <el-button type="text" size="small">
                删除菜品
                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item
                    v-for="dish in recipe.items || []"
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
  </div>

  <!-- 查看详情组件 -->
  <RecipeDetail
    v-model:visible="detailDialogVisible"
    :recipe="selectedRecipe"
    @close="selectedRecipe = null"
    @update:cook-time="handleUpdateCookTime"
    @update:details="handleUpdateDetails"
  ></RecipeDetail>

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

      <div class="custom-dish-section">
        <el-button type="text" @click="showCustomDishInput = !showCustomDishInput">
          {{ showCustomDishInput ? '使用预设菜品' : '自定义菜品' }}
        </el-button>

        <div v-if="showCustomDishInput" class="custom-dish-input">
          <el-input
            v-model="customDishName"
            placeholder="请输入自定义菜品名称"
            clearable
            style="margin-bottom: 10px"
          />
          <el-button
            type="primary"
            size="small"
            :disabled="!customDishName.trim()"
            @click="handleCustomDishReplacement"
          >
            确认替换为自定义菜品
          </el-button>
        </div>
      </div>
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

  <!-- 导入订单对话框 -->
  <el-dialog v-model="importOrderVisible" title="从订单导入食谱" width="600px" top="10%">
    <div class="import-merchant-dish-container">
      <!-- 订单列表 -->
      <el-select v-model="selectedOrder" placeholder="请选择要导入的订单" style="width: 100%">
        <el-option
          v-for="order in orders"
          :key="order.id"
          :label="`订单号: ${order.orderNo} - 总价: ${order.totalPrice}元`"
          :value="order"
        >
          <template #default>
            <div>
              <div class="order-option-header">
                <span>{{ `订单号: ${order.orderNo} - 总价: ${order.totalPrice}元` }}</span>
              </div>
              <div class="order-option-dishes" style="margin-top: 8px">
                <el-tag v-for="dish in order.dishes" :key="dish.name" size="small" type="info">
                  {{ dish.name }}
                </el-tag>
              </div>
            </div>
          </template>
        </el-option>
      </el-select>
    </div>

    <template #footer>
      <el-button @click="importOrderVisible = false">取消</el-button>
      <el-button type="primary" @click="confirmImportOrder"> 导入为新食谱 </el-button>
    </template>
  </el-dialog>

  <!-- 添加食谱对话框 -->
  <AddRecipe v-model:visible="addRecipeVisible" @add-recipe="handleAddRecipe" />
</template>

<style scoped lang="less">
.today-recipe-container {
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

    .meal-type-tabs {
      gap: 10px;
    }
  }

  .nutrition-card {
    margin-bottom: 24px;
    background: rgba(255, 255, 255, 0.95) !important;
    border-radius: 16px !important;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);

    .card-header {
      font-size: 18px;
      font-weight: 700;
    }

    .nutrition-stats {
      display: flex;
      justify-content: space-between;
      padding: 20px;

      .stat-item {
        text-align: center;
        min-width: 120px;
        flex: 1;

        .stat-label {
          font-size: 14px;
          color: #666;
          margin-bottom: 8px;
        }

        .stat-value {
          font-size: 28px;
          font-weight: 700;
          color: #ff6b6b;
          margin-bottom: 12px;
        }
      }
    }
  }

  .recipe-list {
    display: flex;
    flex-direction: column;
    width: 100%;
    gap: 25px;

    .recipe-card {
      flex: 1 1 100%;
      max-width: 100%;
      min-width: 317px;
      box-sizing: border-box;
      margin: 0;
    }
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

    // 为收藏状态的卡片添加与餐型匹配的hover阴影效果
    &.recipe-card-favorited:hover {
      transform: translateY(-8px); // 增强悬浮提升效果

      // 早餐
      &.breakfast {
        box-shadow: 0 15px 40px rgba(255, 193, 7, 0.25) !important;
        // border-color: #ffc107 !important;
      }

      // 午餐
      &.lunch {
        box-shadow: 0 15px 40px rgba(76, 175, 80, 0.25) !important;
        border-color: #4caf50 !important;
      }

      // 晚餐
      &.dinner {
        box-shadow: 0 15px 40px rgba(33, 150, 243, 0.25) !important;
        border-color: #2196f3 !important;
      }

      // 下午茶/茶点
      &.afternoon_tea,
      &.tea {
        box-shadow: 0 15px 40px rgba(156, 39, 176, 0.25) !important;
        border-color: #9c27b0 !important;
      }

      // 夜宵/零食
      &.night_snack,
      &.snack {
        box-shadow: 0 15px 40px rgba(30, 136, 229, 0.25) !important;
        border-color: #1e88e5 !important;
      }

      // 上午加餐/早午餐
      &.morning_snack,
      &.brunch {
        box-shadow: 0 15px 40px rgba(255, 152, 0, 0.25) !important;
        border-color: #ff9800 !important;
      }

      // 宵夜/深夜零食
      &.supper,
      &.midnight_snack {
        box-shadow: 0 15px 40px rgba(0, 188, 212, 0.25) !important;
        border-color: #00bcd4 !important;
      }

      // 健康零食/健身餐
      &.health_snack,
      &.fitness_meal {
        box-shadow: 0 15px 40px rgba(76, 175, 80, 0.25) !important;
        border-color: #4caf50 !important;
      }

      // 甜点/甜食
      &.dessert,
      &.sweet {
        box-shadow: 0 15px 40px rgba(233, 30, 99, 0.25) !important;
        border-color: #e91e63 !important;
      }

      // 汤/粥
      &.soup,
      &.porridge {
        box-shadow: 0 15px 40px rgba(0, 150, 136, 0.25) !important;
        border-color: #009688 !important;
      }

      // 沙拉/蔬菜
      &.salad,
      &.vegetable {
        box-shadow: 0 15px 40px rgba(139, 195, 74, 0.25) !important;
        border-color: #8bc34a !important;
      }

      // 肉类/蛋白质
      &.meat,
      &.protein {
        box-shadow: 0 15px 40px rgba(121, 85, 72, 0.25) !important;
        border-color: #795548 !important;
      }

      // 默认样式
      &.info {
        box-shadow: 0 15px 40px rgba(0, 188, 212, 0.25) !important;
        border-color: #00bcd4 !important;
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
      // border-bottom: 1px solid #eef2f7 !important;

      .meal-icon {
        font-size: 32px;
        padding: 10px;
        border-radius: 50%;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: white;
        box-shadow: 0 6px 20px rgba(102, 126, 234, 0.3);
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

    .recipe-actions {
      text-align: right;
      margin: 0 24px 20px;
      padding-top: 20px;
      border-top: 1px solid #eef2f7;

      /* 让所有按钮和下拉触发元素在一行显示 */
      display: flex;
      justify-content: flex-end;
      gap: 8px; /* 统一间距 */

      .el-button {
        font-size: 14px;
        padding: 6px 16px;
        border-radius: 8px;
        margin: 0;
      }
    }

    &.breakfast {
      // border-left: 4px solid #ffc107;
      border-left: 4px solid #ffc;
      // .meal-icon.breakfast {
      // 	color: #ffc107;
      // }
    }

    &.lunch {
      border-left: 4px solid #4caf50;

      .meal-icon.lunch {
        color: #4caf50;
      }
    }

    &.dinner {
      border-left: 4px solid #2196f3;

      .meal-icon.dinner {
        color: #2196f3;
      }
    }

    // 自定义菜单类型样式 - 早餐
    &.breakfast {
      border-left: 4px solid #ffc107;

      &::before {
        background: linear-gradient(90deg, #ffc107 0%, #ffeb3b 100%);
      }

      .meal-icon {
        background: linear-gradient(135deg, #ffc107 0%, #ffeb3b 100%) !important;
        color: #333 !important;
      }
    }

    // 午餐
    &.lunch {
      border-left: 4px solid #4caf50;

      &::before {
        background: linear-gradient(90deg, #4caf50 0%, #8bc34a 100%);
      }

      .meal-icon {
        background: linear-gradient(135deg, #4caf50 0%, #8bc34a 100%) !important;
        color: white !important;
      }
    }

    // 晚餐
    &.dinner {
      border-left: 4px solid #2196f3;

      &::before {
        background: linear-gradient(90deg, #2196f3 0%, #64b5f6 100%);
      }

      .meal-icon {
        background: linear-gradient(135deg, #2196f3 0%, #64b5f6 100%) !important;
        color: white !important;
      }
    }

    // 下午茶/茶点
    &.afternoon_tea,
    &.tea {
      border-left: 4px solid #9c27b0;

      &::before {
        background: linear-gradient(90deg, #9c27b0 0%, #ba68c8 100%);
      }

      .meal-icon {
        background: linear-gradient(135deg, #9c27b0 0%, #ba68c8 100%) !important;
        color: white !important;
      }
    }

    // 夜宵/零食
    &.night_snack,
    &.snack {
      border-left: 4px solid #1e88e5;

      &::before {
        background: linear-gradient(90deg, #1e88e5 0%, #42a5f5 100%);
      }

      .meal-icon {
        background: linear-gradient(135deg, #1e88e5 0%, #42a5f5 100%) !important;
        color: white !important;
      }
    }

    // 上午加餐/早午餐
    &.morning_snack,
    &.brunch {
      border-left: 4px solid #ff9800;

      &::before {
        background: linear-gradient(90deg, #ff9800 0%, #ffa726 100%);
      }

      .meal-icon {
        background: linear-gradient(135deg, #ff9800 0%, #ffa726 100%) !important;
        color: white !important;
      }
    }

    // 宵夜/深夜零食
    &.supper,
    &.midnight_snack {
      border-left: 4px solid #00bcd4;

      &::before {
        background: linear-gradient(90deg, #00bcd4 0%, #29b6f6 100%);
      }

      .meal-icon {
        background: linear-gradient(135deg, #00bcd4 0%, #29b6f6 100%) !important;
        color: white !important;
      }
    }

    // 健康零食/健身餐
    &.health_snack,
    &.fitness_meal {
      border-left: 4px solid #4caf50;

      &::before {
        background: linear-gradient(90deg, #4caf50 0%, #81c784 100%);
      }

      .meal-icon {
        background: linear-gradient(135deg, #4caf50 0%, #81c784 100%) !important;
        color: white !important;
      }
    }

    // 甜点/甜食
    &.dessert,
    &.sweet {
      border-left: 4px solid #e91e63;

      &::before {
        background: linear-gradient(90deg, #e91e63 0%, #f06292 100%);
      }

      .meal-icon {
        background: linear-gradient(135deg, #e91e63 0%, #f06292 100%) !important;
        color: white !important;
      }
    }

    // 汤/粥
    &.soup,
    &.porridge {
      border-left: 4px solid #009688;

      &::before {
        background: linear-gradient(90deg, #009688 0%, #26a69a 100%);
      }

      .meal-icon {
        background: linear-gradient(135deg, #009688 0%, #26a69a 100%) !important;
        color: white !important;
      }
    }

    // 沙拉/蔬菜
    &.salad,
    &.vegetable {
      border-left: 4px solid #8bc34a;

      &::before {
        background: linear-gradient(90deg, #8bc34a 0%, #aed581 100%);
      }

      .meal-icon {
        background: linear-gradient(135deg, #8bc34a 0%, #aed581 100%) !important;
        color: white !important;
      }
    }

    // 肉类/蛋白质
    &.meat,
    &.protein {
      border-left: 4px solid #795548;

      &::before {
        background: linear-gradient(90deg, #795548 0%, #a1887f 100%);
      }

      .meal-icon {
        background: linear-gradient(135deg, #795548 0%, #a1887f 100%) !important;
        color: white !important;
      }
    }

    // 默认样式
    &.info {
      border-left: 4px solid #00bcd4;

      .meal-icon.info {
        color: #00bcd4;
        font-size: 24px;
      }
    }
  }

  .add-recipe-section {
    display: flex;
    justify-content: center;
    align-items: center;
    margin-bottom: 24px;
    gap: 12px; /* 统一按钮间距 */

    .el-button {
      border-radius: 24px !important;
      padding: 10px 24px !important;
      font-weight: 600 !important;
    }
  }
}

// 自定义标签颜色和交互
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

// 食材输入区域样式
.ingredients-input {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
  align-items: center;

  .el-input {
    flex: 1;
  }
}

.ingredients-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  width: 100%;
}

// 所有对话框标题样式
.el-dialog__header {
  .el-dialog__title {
    font-size: 24px !important;
    font-weight: 700 !important;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
    background-clip: text !important;
    -webkit-background-clip: text !important;
    color: transparent !important;
    text-shadow: 2px 2px 6px rgba(102, 126, 234, 0.3) !important;
    letter-spacing: 1px !important;
    padding: 6px 0 !important;
    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  }
}

// 食谱详情对话框样式
.recipe-details {
  .detail-item {
    margin-bottom: 20px;

    .detail-label {
      font-weight: 700;
      font-size: 16px;
      color: #2c3e50;
      margin-right: 12px;
      padding: 8px 16px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      border-radius: 24px;
      box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
      letter-spacing: 0.5px;
      margin-bottom: 12px;
      display: inline-block;
    }

    .detail-value {
      font-size: 14px;
      color: #666;
    }

    // 餐型值样式
    .detail-item:first-child .detail-value {
      font-size: 20px;
      font-weight: 700;
      color: #2196f3;
      margin-left: 8px;
      text-shadow: 1px 1px 3px rgba(33, 150, 243, 0.2);
    }

    .nutrition-info {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
      gap: 16px;
      padding: 20px;
      background: linear-gradient(135deg, #ffffff 0%, #fff5f5 100%);
      border-radius: 12px;
      border: 1px solid #ffebee;
      margin-top: 12px;
    }

    .nutrition-item {
      margin-bottom: 0;
      padding: 12px 16px;
      background: white;
      border-radius: 8px;
      text-align: center;
      border: 1px solid #ffcdd2;
      transition: all 0.3s ease;

      &:hover {
        box-shadow: 0 4px 12px rgba(255, 107, 107, 0.1);
        transform: translateY(-1px);
      }

      .nutrition-label {
        font-weight: 600;
        font-size: 14px;
        color: #757575;
        display: block;
        margin-bottom: 4px;
      }

      .nutrition-value {
        color: #ff5252;
        font-weight: 700;
        font-size: 20px;
        margin-left: 0;
      }
    }

    // 菜品列表样式
    .dish-list {
      display: flex;
      flex-direction: column;
      gap: 24px;
      margin-top: 16px;
      max-height: 200px; /* 调整为你需要的最大高度 */
      overflow-y: auto; /* 超过最大高度时显示垂直滚动条 */
      padding-right: 10px; /* 为滚动条预留空间 */
    }

    .dish-item {
      padding: 20px;
      background: linear-gradient(135deg, #ffffff 0%, #f5f9ff 100%);
      border-radius: 12px;
      border-left: 5px solid #2196f3;
      border: 1px solid #e3f2fd;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

      &:hover {
        box-shadow: 0 8px 24px rgba(33, 150, 243, 0.15);
        transform: translateY(-2px);
        border-color: #1976d2;
      }

      // 待添加菜品样式
      &.empty-dish {
        background: linear-gradient(135deg, #fafafa 0%, #f0f0f0 100%) !important;
        border: 1px dashed #ccc !important;
        border-left: 5px solid #9e9e9e !important;
        opacity: 0.7;
        box-shadow: none !important;

        &:hover {
          transform: none !important;
          cursor: default;
        }

        .dish-name {
          font-style: italic;
          color: #999;
        }
      }
    }

    .dish-name {
      font-size: 18px;
      font-weight: 700;
      margin: 0 0 14px 0;
      color: #2c3e50;
      display: flex;
      align-items: center;
      gap: 10px;

      &::before {
        content: '🍽️';
        font-size: 22px;
      }
    }

    .dish-ingredients {
      display: flex;
      flex-wrap: wrap;
      gap: 10px;
      margin-top: 8px;
    }

    .dish-ingredients .el-tag {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border: none;
      color: white;
      font-weight: 500;
      opacity: 0.9;
      transition: all 0.2s ease;

      &:hover {
        opacity: 1;
        transform: translateY(-1px);
        box-shadow: 0 3px 8px rgba(102, 126, 234, 0.4);
      }
    }

    .no-ingredients {
      margin-top: 12px;
    }

    .no-ingredients .el-tag {
      background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
      border: none;
      color: white;
    }
  }
}

// 营养编辑区域样式
.nutrition-edit-section {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-top: 8px;
}

.nutrition-input-group {
  margin-bottom: 8px;
}

// 添加菜品对话框样式
.add-dish-form {
  .form-container {
    background: linear-gradient(135deg, #ffffff 0%, #f8f9ff 100%);
    padding: 24px;
    border-radius: 12px;
    border: 1px solid #e3f2fd;
  }

  // 表单标签样式
  .el-form-item__label {
    font-weight: 700 !important;
    font-size: 14px !important;
    color: #2c3e50 !important;
  }

  // 必填项红色星号
  .el-form-item.is-required > .el-form-item__label::before {
    color: #ff4d4f;
    font-weight: 700;
  }

  // 输入框样式
  .el-input__wrapper {
    border-radius: 8px !important;
    border: 1px solid #d9d9d9 !important;
    transition: all 0.3s ease !important;

    &:focus-within {
      border-color: #667eea !important;
      box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1) !important;
    }
  }

  // 食材输入区域样式
  .ingredients-input {
    display: flex;
    gap: 12px;
    align-items: center;
  }

  // 添加食材按钮样式
  .ingredients-input .el-button {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border: none;
    color: white;
    border-radius: 8px;
    transition: all 0.3s ease;

    &:hover {
      background: linear-gradient(135deg, #764ba2 0%, #667eea 100%);
      box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
      transform: translateY(-2px);
    }
  }

  // 食材列表样式
  .ingredients-list {
    .el-tag {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border: none;
      color: white;
      opacity: 0.9;
      transition: all 0.2s ease;

      &:hover {
        opacity: 1;
        transform: translateY(-1px);
        box-shadow: 0 3px 8px rgba(102, 126, 234, 0.4);
      }
    }
  }
}

// 所有对话框按钮样式
:deep(.el-dialog__footer) {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  padding: 16px 24px;

  .el-button {
    padding: 8px 20px;
    border-radius: 8px;
    font-weight: 600;
    transition: all 0.3s ease;
  }

  // 取消按钮
  .el-button--default {
    border-color: #d9d9d9;

    &:hover {
      border-color: #667eea;
      color: #667eea;
      box-shadow: 0 2px 8px rgba(102, 126, 234, 0.2);
    }
  }

  // 确定/主按钮
  .el-button--primary {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border: none;

    &:hover {
      background: linear-gradient(135deg, #764ba2 0%, #667eea 100%);
      box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
      transform: translateY(-2px);
    }
  }
}

// 隐藏批量选择复选框的自动label
.checkbox-wrapper {
  :deep(.el-checkbox__label) {
    display: none !important;
  }
  margin-right: 10px;
}

// 收藏按钮样式
.favorite-btn {
  color: #ffd700 !important; // 收藏状态用金色，确保覆盖默认样式
  font-weight: bold;
}

// 卡片头部样式
.card-header {
  position: relative; // 设置为相对定位，让收藏按钮可以绝对定位
  display: flex;
  align-items: center;
  gap: 10px;
}

// 右上角收藏按钮样式
.card-favorite {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
}

// 收藏按钮通用样式
.recipe-actions .el-button {
  // 确保所有按钮样式统一
  margin-right: 10px;
}

// 导入商家菜品对话框样式
.import-merchant-dish-container {
  background: linear-gradient(135deg, #ffffff 0%, #f8f9ff 100%);
  padding: 24px;
  border-radius: 12px;
  border: 1px solid #e3f2fd;

  // 表单标签
  .el-form-item__label {
    font-weight: 700 !important;
    font-size: 14px !important;
    color: #2c3e50 !important;
  }

  // 下拉选择框
  .el-select__wrapper {
    border-radius: 8px !important;
    border: 1px solid #d9d9d9 !important;
    transition: all 0.3s ease !important;

    &:focus-within {
      border-color: #667eea !important;
      box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1) !important;
    }
  }

  // 菜品列表
  .merchant-dishes-list {
    margin-top: 20px;
    padding: 16px;
    background: white;
    border-radius: 8px;
    border: 1px solid #e0e0e0;

    h4 {
      color: #2c3e50;
      margin-bottom: 16px;
      font-size: 16px;
      font-weight: 700;
    }
  }

  // 菜品项
  .dish-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 8px 0;
    border-bottom: 1px solid #f5f5f5;

    &:last-child {
      border-bottom: none;
    }

    // 复选框
    :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
      background-color: #667eea;
      border-color: #667eea;
    }

    // 营养信息
    .dish-nutrition {
      font-size: 14px;
      color: #999;
    }
  }
}

// 替换菜品对话框样式
.replace-dish-container {
  .current-dish {
    margin-bottom: 20px;

    .detail-label {
      font-weight: bold;
    }

    .detail-value {
      color: #ff6b6b;
      font-weight: bold;
      margin-left: 10px;
    }
  }

  .available-dishes {
    .detail-label {
      font-weight: bold;
      display: block;
      margin-bottom: 15px;
    }

    .dish-list {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
      gap: 15px;
    }

    .dish-card {
      cursor: pointer;
      border-left: 4px solid #ccc;
      transition: all 0.3s ease;

      &:hover {
        transform: translateY(-5px);
        box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
      }

      .dish-name {
        font-size: 16px;
        font-weight: bold;
        margin-bottom: 5px;
      }

      .dish-nutrition {
        font-size: 14px;
        color: #999;
      }

      &.breakfast {
        border-left-color: #ffc107;
      }

      &.lunch {
        border-left-color: #4caf50;
      }

      &.dinner {
        border-left-color: #2196f3;
      }
    }
  }
}
</style>
