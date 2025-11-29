<script setup>
import { ref, computed, onMounted } from 'vue';
import axios from 'axios';
import { API_CONFIG } from '../../config';
import { ElMessage, ElMessageBox } from 'element-plus';


// 我的食谱数据
const myRecipes = ref([]);

// 加载我的食谱数据
const loadMyRecipes = () => {
  axios.get(API_CONFIG.baseURL + API_CONFIG.recipe.favorite)
    .then(response => {
      if (response.data.data) {
        myRecipes.value = response.data.data;
      }
    })
    .catch(error => {
      console.error('加载我的食谱失败:', error);
      // 使用默认数据作为 fallback
      myRecipes.value = [
        {
          id: 1,
          name: '健康早餐组合',
          type: '早餐',
          calories: 380,
          time: '5分钟',
          favorite: true
        },
        {
          id: 2,
          name: '减脂午餐',
          type: '午餐',
          calories: 450,
          time: '15分钟',
          favorite: false
        },
        {
          id: 3,
          name: '轻食晚餐',
          type: '晚餐',
          calories: 320,
          time: '10分钟',
          favorite: true
        },
        {
          id: 4,
          name: '健身餐',
          type: '加餐',
          calories: 280,
          time: '8分钟',
          favorite: true
        }
      ];
      ElMessage.error('加载我的食谱失败，将显示默认数据');
    });
};

// 组件挂载时加载数据
onMounted(() => {
  loadMyRecipes();
});

// 食谱筛选
const recipeFilter = ref('all');

// 计算属性：过滤后的食谱列表，收藏的食谱排在前面
const filteredRecipes = computed(() => {
  let filtered = [];

  if (recipeFilter.value === 'all') {
    filtered = [...myRecipes.value];
  } else {
    filtered = myRecipes.value.filter(recipe => recipe.type === recipeFilter.value);
  }

  // 排序：收藏的食谱在前
  return filtered.sort((a, b) => {
    // 如果a收藏而b未收藏，a排在前面
    if (a.favorite && !b.favorite) return -1;
    // 如果b收藏而a未收藏，b排在前面
    if (!a.favorite && b.favorite) return 1;
    // 否则保持原顺序
    return 0;
  });
});

// 切换收藏状态
const toggleFavorite = (recipe) => {
  recipe.favorite = !recipe.favorite;
};

// 食谱详情模态框
const detailDialogVisible = ref(false);
const selectedRecipe = ref(null);

// 查看详情
const viewRecipeDetails = (recipe) => {
  selectedRecipe.value = recipe;
  detailDialogVisible.value = true;
};

// 添加食谱对话框
const addDialogVisible = ref(false);

// 新食谱表单数据
const newRecipe = ref({
  name: '',
  type: '早餐',
  calories: '',
  time: '',
  favorite: false
});

// 打开添加食谱对话框
const openAddDialog = () => {
  addDialogVisible.value = true;
  // 重置表单数据
  newRecipe.value = {
    name: '',
    type: '早餐',
    calories: '',
    time: '',
    favorite: false
  };
};

// 保存新食谱
const saveNewRecipe = () => {
  // 简单的表单验证
  if (!newRecipe.value.name.trim()) {
    ElMessage.warning('请填写食谱名称');
    return;
  }

  if (!newRecipe.value.calories || isNaN(newRecipe.value.calories) || newRecipe.value.calories <= 0) {
    ElMessage.warning('请输入有效的卡路里数值');
    return;
  }

  if (!newRecipe.value.time.trim()) {
    ElMessage.warning('请填写准备时间');
    return;
  }

  // 创建新食谱对象
  const recipe = {
    id: Date.now(), // 使用时间戳作为唯一ID
    name: newRecipe.value.name,
    type: newRecipe.value.type,
    calories: Number(newRecipe.value.calories),
    time: newRecipe.value.time,
    favorite: newRecipe.value.favorite
  };

  // 添加到食谱列表
  myRecipes.value.push(recipe);
  addDialogVisible.value = false;
  ElMessage.success('食谱已添加');
};

// 删除食谱
const deleteRecipe = (id) => {
  ElMessageBox.confirm('确定要删除该食谱吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  .then(() => {
    const index = myRecipes.value.findIndex(recipe => recipe.id === id);
    if (index !== -1) {
      myRecipes.value.splice(index, 1);
      ElMessage.success('食谱已删除');
    }
  })
  .catch(() => {
    ElMessage.info('已取消删除');
  });
};
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
          style="width: 150px; margin-right: 10px;"
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
      >
        <template #header>
          <div class="card-header">
            <div class="recipe-name">{{ recipe.name }}</div>
            <el-button
              type="text"
              size="small"
              @click="toggleFavorite(recipe)"
            >
              <span :style="{ color: recipe.favorite ? '#FFD700' : '#C0C4CC', fontSize: '20px' }">
                {{ recipe.favorite ? '⭐' : '☆' }}
              </span>
            </el-button>
          </div>
        </template>
        <div class="recipe-info">
          <div class="recipe-type">
            <el-tag
              :type="recipe.type === '早餐' ? 'warning' : recipe.type === '午餐' ? 'success' : recipe.type === '晚餐' ? 'primary' : 'info'"
            >
              {{ recipe.type }}
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
        </div>
        <div class="recipe-actions">
          <el-button type="primary" size="small" plain @click="viewRecipeDetails(recipe)">
            查看详情
          </el-button>
          <el-button type="danger" size="small" plain @click="deleteRecipe(recipe.id)">
            删除
          </el-button>
        </div>
      </el-card>
    </div>

    <!-- 空数据提示 -->
    <el-empty
      v-if="filteredRecipes.length === 0"
      description="暂无食谱"
    ></el-empty>
  </div>

  <!-- 食谱详情对话框 -->
  <el-dialog
    v-model="detailDialogVisible"
    :title="selectedRecipe ? selectedRecipe.name : '食谱详情'"
    width="600px"
    top="10%"
    class="recipe-detail-dialog"
  >
    <div v-if="selectedRecipe" class="recipe-detail-container">
      <!-- 头部信息 -->
      <div class="detail-header">
        <div class="header-left">
          <h3 class="recipe-title">{{ selectedRecipe.name }}</h3>
          <el-tag
            :type="selectedRecipe.type === '早餐' ? 'warning' : selectedRecipe.type === '午餐' ? 'success' : selectedRecipe.type === '晚餐' ? 'primary' : 'info'"
            size="large"
          >
            {{ selectedRecipe.type }}
          </el-tag>
        </div>
        <div class="header-right">
          <span
            :style="{ color: selectedRecipe.favorite ? '#FFD700' : '#C0C4CC', fontSize: '32px', cursor: 'pointer' }"
            @click="toggleFavorite(selectedRecipe)"
            title="点击切换收藏状态"
          >
            {{ selectedRecipe.favorite ? '⭐' : '☆' }}
          </span>
        </div>
      </div>

      <!-- 核心信息卡片 -->
      <div class="detail-cards">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon">🔥</div>
            <div class="stat-value">{{ selectedRecipe.calories }} kcal</div>
            <div class="stat-label">卡路里</div>
          </div>
        </el-card>

        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon">⏰</div>
            <div class="stat-value">{{ selectedRecipe.time }}</div>
            <div class="stat-label">准备时间</div>
          </div>
        </el-card>
      </div>

      <!-- 食谱详情 -->
      <div class="detail-section">
        <h4 class="section-title">食谱详情</h4>
        <div class="detail-content">
          <p>这是一个健康美味的{{ selectedRecipe.type }}食谱</p>
          <p>热量适中，营养均衡，适合日常食用</p>
        </div>
      </div>

      <!-- 食材列表 -->
      <div class="detail-section">
        <h4 class="section-title">主要食材</h4>
        <el-space direction="vertical" size="medium" class="ingredient-list">
          <el-tag v-for="ingredient in ['鸡蛋', '牛奶', '燕麦', '水果']" :key="ingredient" type="info">
            {{ ingredient }}
          </el-tag>
        </el-space>
      </div>

      <!-- 烹饪步骤 -->
      <div class="detail-section">
        <h4 class="section-title">烹饪步骤</h4>
        <ol class="cooking-steps">
          <li>准备好所需食材</li>
          <li>按照说明进行烹饪</li>
          <li>享受美味的{{ selectedRecipe.name }}</li>
        </ol>
      </div>
    </div>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </span>
    </template>
  </el-dialog>

  <!-- 添加食谱对话框 -->
  <el-dialog
    v-model="addDialogVisible"
    title="添加新食谱"
    width="500px"
    top="10%"
  >
    <div class="add-recipe-form">
      <el-form :model="newRecipe" label-width="80px" status-icon>
        <el-form-item label="名称" prop="name" required>
          <el-input v-model="newRecipe.name" placeholder="请输入食谱名称" />
        </el-form-item>

        <el-form-item label="类型" prop="type" required>
          <el-select v-model="newRecipe.type" style="width: 100%;">
            <el-option label="早餐" value="早餐" />
            <el-option label="午餐" value="午餐" />
            <el-option label="晚餐" value="晚餐" />
            <el-option label="加餐" value="加餐" />
          </el-select>
        </el-form-item>

        <el-form-item label="卡路里" prop="calories" required>
          <el-input v-model="newRecipe.calories" type="number" placeholder="请输入卡路里" />
        </el-form-item>

        <el-form-item label="时间" prop="time" required>
          <el-input v-model="newRecipe.time" placeholder="请输入准备时间" />
        </el-form-item>

        <el-form-item label="收藏">
          <el-switch v-model="newRecipe.favorite" />
        </el-form-item>
      </el-form>
    </div>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveNewRecipe">确定</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<style scoped lang="less">
.my-recipe-container {
  padding: 0 20px 20px 20px;

  .recipe-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h2 {
      font-size: 24px;
      margin: 0;
    }

    .filter-section {
      display: flex;
      align-items: center;
    }
  }

  .recipe-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 20px;
  }

  .recipe-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-size: 16px;
      font-weight: bold;

      .recipe-name {
        flex: 1;
        overflow: hidden;
        text-overflow: ellipsis;
      }
    }

    .recipe-info {
      margin: 20px 0;

      .recipe-type {
        margin-bottom: 15px;
      }

      .recipe-stats {
        display: flex;
        gap: 20px;

        .stat-item {
          display: flex;
          align-items: center;
          gap: 5px;
          color: #666;
        }
      }
    }

    .recipe-actions {
      display: flex;
      justify-content: flex-end;
      gap: 10px;
      margin-top: 20px;
    }
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

  /* 食谱详情对话框样式 */
  .recipe-detail-dialog {
    .recipe-detail-container {
      padding: 20px 0;
    }

    .detail-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 30px;
      padding-bottom: 15px;
      border-bottom: 2px solid #eee;

      .recipe-title {
        font-size: 24px;
        margin: 0 0 10px 0;
        color: #303133;
      }

      .header-right {
        padding-top: 10px;
      }
    }

    .detail-cards {
      display: flex;
      gap: 20px;
      margin-bottom: 30px;

      .stat-card {
        flex: 1;
        text-align: center;
        cursor: pointer;
        transition: transform 0.3s;

        &:hover {
          transform: translateY(-5px);
        }

        .stat-content {
          .stat-icon {
            font-size: 32px;
            margin-bottom: 10px;
          }

          .stat-value {
            font-size: 24px;
            font-weight: bold;
            color: #303133;
            margin-bottom: 5px;
          }

          .stat-label {
            font-size: 14px;
            color: #909399;
          }
        }
      }
    }

    .detail-section {
      margin-bottom: 30px;

      .section-title {
        font-size: 18px;
        font-weight: bold;
        color: #303133;
        margin-bottom: 15px;
      }

      .detail-content {
        color: #606266;
        line-height: 1.8;
      }

      .ingredient-list {
        margin-left: 0;

        .el-tag {
          padding: 8px 16px;
          font-size: 14px;
        }
      }

      .cooking-steps {
        padding-left: 20px;
        color: #606266;
        line-height: 2;

        li {
          margin-bottom: 10px;
        }
      }
    }
  }
}
</style>