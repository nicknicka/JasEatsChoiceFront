<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'
import { API_CONFIG } from '../../config'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from './../../store/authStore'
import { useUserStore } from './../../store/userStore'

// 引入新组件
import RecipeDetail from '../../components/recipe/RecipeDetail.vue'
import AddRecipe from '../../components/recipe/AddRecipe.vue'

// 我的食谱数据
const myRecipes = ref([])
const loadingFailed = ref(false)

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
  }

  axios
    .get(API_CONFIG.baseURL + API_CONFIG.recipe.favorite, {
      params: {
        userId: userId
      }
    })
    .then((response) => {
      if (response.data.data) {
        myRecipes.value = response.data.data
      } else {
        myRecipes.value = []
      }
      loadingFailed.value = false
    })
    .catch((error) => {
      console.error('加载我的食谱失败:', error)
      myRecipes.value = []
      loadingFailed.value = true
      ElMessage.error('加载我的食谱失败')
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

// 删除食谱
const deleteRecipe = (id) => {
  ElMessageBox.confirm('确定要删除该食谱吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => {
      const index = myRecipes.value.findIndex((recipe) => recipe.id === id)
      if (index !== -1) {
        myRecipes.value.splice(index, 1)
        ElMessage.success('食谱已删除')
      }
    })
    .catch(() => {
      ElMessage.info('已取消删除')
    })
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
        <el-button type="primary" size="small" @click="openAddDialog">
          <span>➕</span>
          添加食谱
        </el-button>
      </div>
    </div>

    <!-- 食谱列表 -->
    <div class="recipe-grid">
      <el-card
        v-for="recipe in filteredRecipes"
        :key="recipe.id"
        class="recipe-card"
        :class="[recipe.type, { 'recipe-card-favorited': recipe.favorite }]"
      >
        <template #header>
          <div class="card-header">
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
            v-for="(item, index) in recipe.ingredients || ['暂无食材信息']"
            :key="index"
            :type="getTagType(recipe.type)"
          >
            {{ typeof item === 'string' ? item : item }}
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
            >立即下单</el-button
          >
          <el-button type="danger" size="small" @click="deleteRecipe(recipe.id)"
            >删除食谱</el-button
          >
        </div>
      </el-card>
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
  />

  <!-- 添加食谱组件 -->
  <AddRecipe v-model:visible="addDialogVisible" @add-recipe="handleAddRecipe" />
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
    grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
    gap: 20px;
    width: 100%;
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
