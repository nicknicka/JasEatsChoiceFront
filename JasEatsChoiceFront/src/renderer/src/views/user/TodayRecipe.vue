<script setup>
import { ref, computed } from 'vue';
import { ArrowDown } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';

// 今日食谱数据
const todayRecipes = ref([
  { id: 1, name: '早餐', type: 'breakfast', items: ['牛奶燕麦粥', '水煮蛋', '苹果'] },
  { id: 2, name: '午餐', type: 'lunch', items: ['番茄炒蛋', '清炒菠菜', '杂粮饭'] },
  { id: 3, name: '晚餐', type: 'dinner', items: ['清蒸鲈鱼', '凉拌黄瓜', '小米粥'] }
]);

// 营养摄入数据
const nutritionData = ref({
  calories: 1850,
  protein: 85,
  carbs: 220,
  fat: 55
});

// 切换食谱类型
const activeMealType = ref('all');

// 筛选条件
const filters = ref({
  mealType: 'all',
  calorieRange: 'all' // 'all', 'low' (<=1500), 'medium' (1501-2000), 'high' (>2000)
});

// 卡路里范围筛选
const calorieRanges = [
  { value: 'all', label: '全部' },
  { value: 'low', label: '低卡 (<=1500)' },
  { value: 'medium', label: '中卡 (1501-2000)' },
  { value: 'high', label: '高卡 (>2000)' }
];

// 获取标签类型
const getTagType = (type) => {
  switch (type) {
    case 'breakfast':
      return 'warning';
    case 'lunch':
      return 'success';
    case 'dinner':
      return 'primary';
    default:
      return 'info';
  }
};

// 模态框状态
const detailDialogVisible = ref(false);
const replaceDialogVisible = ref(false);

// 当前选中的食谱和菜品
const selectedRecipe = ref(null);
const selectedDish = ref(null);

// 替换菜品列表
const replacementDishes = ref([
  { id: 1, name: '全麦面包', type: 'breakfast', nutrition: '247kcal/片' },
  { id: 2, name: '蒸南瓜', type: 'breakfast', nutrition: '26kcal/100g' },
  { id: 3, name: '烤鸡胸肉', type: 'lunch', nutrition: '165kcal/100g' },
  { id: 4, name: '西兰花', type: 'lunch', nutrition: '34kcal/100g' },
  { id: 5, name: '清蒸鱼', type: 'dinner', nutrition: '105kcal/100g' },
  { id: 6, name: '炒青菜', type: 'dinner', nutrition: '15kcal/100g' }
]);

// 查看详情
const viewRecipeDetails = (recipe) => {
  selectedRecipe.value = recipe;
  detailDialogVisible.value = true;
};

// 替换菜品
const replaceDish = (recipe, dish) => {
  selectedRecipe.value = recipe;
  selectedDish.value = dish;
  replaceDialogVisible.value = true;
};

// 确认替换菜品
const confirmReplaceDish = (newDish) => {
  if (selectedRecipe.value && selectedDish.value) {
    // 找到并替换菜品
    const index = selectedRecipe.value.items.indexOf(selectedDish.value);
    if (index !== -1) {
      selectedRecipe.value.items[index] = newDish.name;
      ElMessage.success('菜品已替换');
    }
    replaceDialogVisible.value = false;
    // 重置选中状态
    selectedRecipe.value = null;
    selectedDish.value = null;
  }
};

// 筛选后的食谱列表
const filteredRecipes = computed(() => {
  let filtered = [...todayRecipes.value];

  // 餐型筛选
  if (filters.value.mealType !== 'all') {
    filtered = filtered.filter(recipe => recipe.type === filters.value.mealType);
  }

  // 卡路里范围筛选
  switch (filters.value.calorieRange) {
    case 'low':
      filtered = filtered.filter(recipe => nutritionData.value.calories <= 1500);
      break;
    case 'medium':
      filtered = filtered.filter(recipe => nutritionData.value.calories > 1500 && nutritionData.value.calories <= 2000);
      break;
    case 'high':
      filtered = filtered.filter(recipe => nutritionData.value.calories > 2000);
      break;
    // 'all' 不筛选
  }

  return filtered;
});
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
            @click="filters.mealType = 'all'"
            size="small"
          >
            全部
          </el-button>
          <el-button
            type="primary"
            :plain="filters.mealType !== 'breakfast'"
            @click="filters.mealType = 'breakfast'"
            size="small"
          >
            早餐
          </el-button>
          <el-button
            type="primary"
            :plain="filters.mealType !== 'lunch'"
            @click="filters.mealType = 'lunch'"
            size="small"
          >
            午餐
          </el-button>
          <el-button
            type="primary"
            :plain="filters.mealType !== 'dinner'"
            @click="filters.mealType = 'dinner'"
            size="small"
          >
            晚餐
          </el-button>
        </div>

        <!-- 卡路里范围筛选 -->
        <el-select
          v-model="filters.calorieRange"
          placeholder="卡路里筛选"
          size="small"
          style="width: 160px; margin-left: 20px;"
        >
          <el-option
            v-for="range in calorieRanges"
            :key="range.value"
            :label="range.label"
            :value="range.value"
          />
        </el-select>
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

    <!-- 食谱列表 -->
    <div class="recipe-list">
      <el-card
        v-for="recipe in filteredRecipes"
        :key="recipe.id"
        class="recipe-card"
        :class="recipe.type"
      >
        <template #header>
          <div class="card-header">
            <span
              :class="`meal-icon ${recipe.type}`"
            >
              {{ recipe.type === 'breakfast' ? '🥣' : recipe.type === 'lunch' ? '🍚' : '🍱' }}
            </span>
            {{ recipe.name }}
          </div>
        </template>
        <div class="recipe-items">
          <el-tag
            v-for="item in recipe.items"
            :key="item"
            :type="getTagType(recipe.type)"
          >
            {{ item }}
          </el-tag>
        </div>
        <div class="recipe-actions">
          <el-button type="text" size="small" @click="viewRecipeDetails(recipe)">查看详情</el-button>
          <el-dropdown trigger="click">
            <el-button type="text" size="small">
              替换菜品 <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item
                  v-for="dish in recipe.items"
                  :key="dish"
                  @click="replaceDish(recipe, dish)"
                >
                  {{ dish }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-card>
    </div>
  </div>

  <!-- 查看详情对话框 -->
  <el-dialog
    v-model="detailDialogVisible"
    :title="selectedRecipe ? `${selectedRecipe.name} 详情` : '食谱详情'"
    width="600px"
    top="10%"
  >
    <div v-if="selectedRecipe" class="recipe-details">
      <div class="detail-item">
        <span class="detail-label">餐型:</span>
        <span class="detail-value">{{ selectedRecipe.name }}</span>
      </div>
      <div class="detail-item">
        <span class="detail-label">菜品:</span>
        <div class="detail-value">
          <el-tag
            v-for="item in selectedRecipe.items"
            :key="item"
            :type="getTagType(selectedRecipe.type)"
            style="margin-right: 10px; margin-bottom: 10px;"
          >
            {{ item }}
          </el-tag>
        </div>
      </div>
      <div class="detail-item">
        <span class="detail-label">营养信息:</span>
        <div class="detail-value nutrition-info">
          <div class="nutrition-item">
            <span class="nutrition-label">卡路里:</span>
            <span class="nutrition-value">{{ nutritionData.calories }} kcal</span>
          </div>
          <div class="nutrition-item">
            <span class="nutrition-label">蛋白质:</span>
            <span class="nutrition-value">{{ nutritionData.protein }} g</span>
          </div>
          <div class="nutrition-item">
            <span class="nutrition-label">碳水化合物:</span>
            <span class="nutrition-value">{{ nutritionData.carbs }} g</span>
          </div>
          <div class="nutrition-item">
            <span class="nutrition-label">脂肪:</span>
            <span class="nutrition-value">{{ nutritionData.fat }} g</span>
          </div>
        </div>
      </div>
    </div>
  </el-dialog>

  <!-- 替换菜品对话框 -->
  <el-dialog
    v-model="replaceDialogVisible"
    :title="selectedDish ? `替换 ${selectedDish}` : '替换菜品'"
    width="600px"
    top="10%"
  >
    <div v-if="selectedDish" class="replace-dish-container">
      <div class="current-dish">
        <span class="detail-label">当前菜品:</span>
        <span class="detail-value">{{ selectedDish }}</span>
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
    </div>
  </el-dialog>
</template>

<style scoped lang="less">
.today-recipe-container {
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

    .meal-type-tabs {
      gap: 10px;
    }
  }

  .nutrition-card {
    margin-bottom: 20px;

    .card-header {
      font-size: 18px;
      font-weight: bold;
    }

    .nutrition-stats {
      display: flex;
      justify-content: space-around;
      padding: 20px 0;

      .stat-item {
        text-align: center;

        .stat-label {
          font-size: 14px;
          color: #666;
          margin-bottom: 5px;
        }

        .stat-value {
          font-size: 24px;
          font-weight: bold;
          color: #FF6B6B;
        }
      }
    }
  }

  .recipe-list {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 20px;
  }

  .recipe-card {
    .card-header {
      display: flex;
      align-items: center;
      gap: 10px;
      font-size: 18px;
      font-weight: bold;

      .meal-icon {
        font-size: 24px;
      }
    }

    .recipe-items {
      margin: 20px 0;
      display: flex;
      flex-wrap: wrap;
      gap: 10px;
    }

    .recipe-actions {
      text-align: right;
      margin-top: 20px;
    }

    &.breakfast {
      border-left: 4px solid #FFC107;

      .meal-icon.breakfast {
        color: #FFC107;
      }
    }

    &.lunch {
      border-left: 4px solid #4CAF50;

      .meal-icon.lunch {
        color: #4CAF50;
      }
    }

    &.dinner {
      border-left: 4px solid #2196F3;

      .meal-icon.dinner {
        color: #2196F3;
      }
    }
  }
}

// 自定义标签颜色
:deep(.el-tag--warning) {
  background-color: #FFF3E0;
  color: #F57C00;
}

:deep(.el-tag--success) {
  background-color: #E8F5E9;
  color: #388E3C;
}

:deep(.el-tag--primary) {
  background-color: #E3F2FD;
  color: #1976D2;
}

// 食谱详情对话框样式
.recipe-details {
  .detail-item {
    margin-bottom: 20px;

    .detail-label {
      font-weight: bold;
      font-size: 14px;
      color: #333;
      margin-right: 10px;
    }

    .detail-value {
      font-size: 14px;
      color: #666;
    }

    .nutrition-info {
      display: flex;
      flex-wrap: wrap;
      gap: 20px;

      .nutrition-item {
        margin-bottom: 10px;

        .nutrition-label {
          font-weight: bold;
        }

        .nutrition-value {
          color: #FF6B6B;
          font-weight: bold;
          margin-left: 5px;
        }
      }
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
      color: #FF6B6B;
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
        border-left-color: #FFC107;
      }

      &.lunch {
        border-left-color: #4CAF50;
      }

      &.dinner {
        border-left-color: #2196F3;
      }
    }
  }
}
</style>
