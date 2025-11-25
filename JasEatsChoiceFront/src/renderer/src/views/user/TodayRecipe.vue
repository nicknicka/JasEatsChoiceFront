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

// 筛选条件
const filters = ref({
  mealType: 'all'
});

// 布局设置
const layoutType = ref('two-column'); // 'one-column' 或 'two-column'

// 获取标签类型
const getTagType = (type) => {
  switch (type) {
    case 'breakfast':
      return 'warning';
    case 'lunch':
      return 'success';
    case 'dinner':
      return 'primary';
    case 'afternoon_tea':
    case 'tea':
      return 'purple';
    case 'night_snack':
    case 'snack':
      return 'blue';
    case 'morning_snack':
    case 'brunch':
      return 'orange';
    case 'supper':
    case 'midnight_snack':
      return 'cyan';
    case 'health_snack':
    case 'fitness_meal':
      return 'green';
    case 'dessert':
    case 'sweet':
      return 'pink';
    case 'soup':
    case 'porridge':
      return 'teal';
    case 'salad':
    case 'vegetable':
      return 'success';
    case 'meat':
    case 'protein':
      return 'brown';
    default:
      return 'info';
  }
};

// 模态框状态
const detailDialogVisible = ref(false);
const replaceDialogVisible = ref(false);
const addDishVisible = ref(false);

// 当前选中的食谱和菜品
const selectedRecipe = ref(null);
const selectedDish = ref(null);

// 自定义菜品
const showCustomDishInput = ref(false);
const customDishName = ref('');

// 新菜品输入
const newDishName = ref('');

// 替换菜品列表
const replacementDishes = ref([
  { id: 1, name: '全麦面包', type: 'breakfast', nutrition: '247kcal/片' },
  { id: 2, name: '蒸南瓜', type: 'breakfast', nutrition: '26kcal/100g' },
  { id: 3, name: '烤鸡胸肉', type: 'lunch', nutrition: '165kcal/100g' },
  { id: 4, name: '西兰花', type: 'lunch', nutrition: '34kcal/100g' },
  { id: 5, name: '清蒸鱼', type: 'dinner', nutrition: '105kcal/100g' },
  { id: 6, name: '炒青菜', type: 'dinner', nutrition: '15kcal/100g' }
]);

// 添加菜单
const addMenuVisible = ref(false);
const newMenu = ref({
  name: '',
  type: '',
  items: []
});

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

// 添加菜品
const addDish = (recipe) => {
  selectedRecipe.value = recipe;
  addDishVisible.value = true;
};

// 确认添加菜品
const confirmAddDish = () => {
  if (selectedRecipe.value && newDishName.value.trim()) {
    selectedRecipe.value.items.push(newDishName.value.trim());
    ElMessage.success('菜品已添加');
    addDishVisible.value = false;
    newDishName.value = '';
    selectedRecipe.value = null;
  }
};

// 删除菜品
const deleteDish = (recipe, dish) => {
  if (recipe && dish) {
    const index = recipe.items.indexOf(dish);
    if (index !== -1) {
      recipe.items.splice(index, 1);
      ElMessage.success('菜品已删除');
    }
  }
};

// 添加新菜单
const addNewMenu = () => {
  if (newMenu.value.name.trim() && newMenu.value.type.trim()) {
    const menu = {
      id: Date.now(), // 使用时间戳作为唯一ID
      name: newMenu.value.name.trim(),
      type: newMenu.value.type.trim().toLowerCase(),
      items: ['待添加菜品'] // 初始默认菜品
    };

    todayRecipes.value.push(menu);
    ElMessage.success('菜单已添加');

    // 重置表单
    newMenu.value = {
      name: '',
      type: '',
      items: []
    };

    // 关闭模态框
    addMenuVisible.value = false;
  }
};

// 筛选后的食谱列表
const filteredRecipes = computed(() => {
  let filtered = [...todayRecipes.value];

  // 餐型筛选
  if (filters.value.mealType !== 'all') {
    filtered = filtered.filter(recipe => recipe.type === filters.value.mealType);
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
      </div>
    </div>

    <!-- 添加菜单按钮和布局切换 -->
    <div class="add-menu-section">
      <el-button
        type="primary"
        size="small"
        @click="addMenuVisible = true"
        style="margin-right: 20px;"
      >
        ➕ 添加菜单
      </el-button>

      <!-- 布局切换按钮 -->
      <el-button-group>
        <el-button
          type="primary"
          :plain="layoutType !== 'one-column'"
          @click="layoutType = 'one-column'"
          size="small"
        >
          一列布局
        </el-button>
        <el-button
          type="primary"
          :plain="layoutType !== 'two-column'"
          @click="layoutType = 'two-column'"
          size="small"
        >
          两列布局
        </el-button>
      </el-button-group>
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
    <div :class="['recipe-list', layoutType]">
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
              {{
                recipe.type === 'breakfast' ? '🥣' :
                recipe.type === 'lunch' ? '🍚' :
                recipe.type === 'dinner' ? '🍱' :
                recipe.type === 'afternoon_tea' || recipe.type === 'tea' ? '🍵' :
                recipe.type === 'night_snack' || recipe.type === 'snack' ? '🍪' :
                recipe.type === 'morning_snack' || recipe.type === 'brunch' ? '🥐' :
                recipe.type === 'supper' || recipe.type === 'midnight_snack' ? '🌙' :
                recipe.type === 'health_snack' || recipe.type === 'fitness_meal' ? '💪' :
                recipe.type === 'dessert' || recipe.type === 'sweet' ? '🍰' :
                recipe.type === 'soup' || recipe.type === 'porridge' ? '🍲' :
                recipe.type === 'salad' || recipe.type === 'vegetable' ? '🥗' :
                recipe.type === 'meat' || recipe.type === 'protein' ? '🥩' : '🍴'
              }}
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
          <el-button type="text" size="small" @click="addDish(recipe)">添加菜品</el-button>
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
          <el-dropdown trigger="click">
            <el-button type="text" size="small">
              删除菜品 <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item
                  v-for="dish in recipe.items"
                  :key="dish"
                  @click="deleteDish(recipe, dish)"
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

      <el-divider />

      <div class="custom-dish-section">
        <el-button
          type="text"
          @click="showCustomDishInput = !showCustomDishInput"
        >
          {{ showCustomDishInput ? '使用预设菜品' : '自定义菜品' }}
        </el-button>

        <div v-if="showCustomDishInput" class="custom-dish-input">
          <el-input
            v-model="customDishName"
            placeholder="请输入自定义菜品名称"
            clearable
            style="margin-bottom: 10px;"
          />
          <el-button
            type="primary"
            size="small"
            @click="
              confirmReplaceDish({
                name: customDishName.trim(),
                type: selectedRecipe.type
              });
              customDishName = '';
            "
            :disabled="!customDishName.trim()"
          >
            确认替换为自定义菜品
          </el-button>
        </div>
      </div>
    </div>
  </el-dialog>

  <!-- 添加菜品对话框 -->
  <el-dialog
    v-model="addDishVisible"
    :title="selectedRecipe ? `为${selectedRecipe.name}添加菜品` : '添加菜品'"
    width="400px"
    top="20%"
  >
    <div v-if="selectedRecipe" class="add-dish-form">
      <el-form class="form-container">
        <el-form-item label="菜品名称" prop="name" required>
          <el-input
            v-model="newDishName"
            placeholder="请输入新菜品名称"
          />
        </el-form-item>
      </el-form>
    </div>

    <template #footer>
      <el-button @click="addDishVisible = false">取消</el-button>
      <el-button
        type="primary"
        @click="confirmAddDish"
        :disabled="!newDishName.trim()"
      >
        确定
      </el-button>
    </template>
  </el-dialog>

  <!-- 添加菜单对话框 -->
  <el-dialog
    v-model="addMenuVisible"
    title="添加新菜单"
    width="400px"
    top="20%"
  >
    <el-form :model="newMenu" class="add-menu-form">
      <el-form-item label="菜单名称" prop="name" required>
        <el-input
          v-model="newMenu.name"
          placeholder="请输入菜单名称（如：下午茶、夜宵）"
        />
      </el-form-item>

      <el-form-item label="类型标识" prop="type" required>
        <el-input
          v-model="newMenu.type"
          placeholder="请输入类型标识（如：afternoon_tea、night_snack）"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="addMenuVisible = false">取消</el-button>
      <el-button
        type="primary"
        @click="addNewMenu"
        :disabled="!newMenu.name.trim() || !newMenu.type.trim()"
      >
        确定
      </el-button>
    </template>
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
    display: flex;
    flex-wrap: wrap;
    gap: 20px;

    &.one-column {
      flex-direction: column;

      .recipe-card {
        flex: 1 1 100%; /* 单列时宽度100% */
        max-width: 100%; /* 确保宽度充满容器 */
        min-width: 280px; /* 最小宽度 */
        width: 100%; /* 明确设置宽度为100% */
        box-sizing: border-box; /* 确保padding和border不会增加总宽度 */
        margin: 0; /* 去除外边距 */
      }
    }

    &.two-column {
      flex-direction: row;

      .recipe-card {
        flex: 1 1 calc(50% - 10px); /* 精确计算两列宽度，减去间距 */
        max-width: calc(50% - 10px); /* 确保两列总和为100% */
        min-width: 280px; /* 最小宽度 */
      }
    }

    /* 响应式处理 - 增加断点 */
    @media (max-width: 768px) { /* 在平板设备上自动转为单列 */
      .recipe-card {
        flex: 1 1 100% !important;
        max-width: 100% !important;
      }
    }

    @media (min-width: 769px) and (max-width: 992px) { /* 在中大屏设备上两列 */
      .recipe-card {
        flex: 1 1 45% !important;
        max-width: 45% !important;
      }
    }

    @media (min-width: 993px) { /* 在大屏设备上可以考虑更宽 */
      .recipe-card {
        flex: 1 1 42% !important;
        max-width: 42% !important;
      }
    }
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

    // 自定义菜单类型样式
    &.afternoon_tea,
    &.tea {
      border-left: 4px solid #9C27B0;

      .meal-icon.afternoon_tea,
      .meal-icon.tea {
        color: #9C27B0;
        font-size: 24px;
      }
    }

    &.night_snack,
    &.snack {
      border-left: 4px solid #1E88E5;

      .meal-icon.night_snack,
      .meal-icon.snack {
        color: #1E88E5;
        font-size: 24px;
      }
    }

    &.morning_snack,
    &.brunch {
      border-left: 4px solid #FF9800;

      .meal-icon.morning_snack,
      .meal-icon.brunch {
        color: #FF9800;
        font-size: 24px;
      }
    }

    &.supper,
    &.midnight_snack {
      border-left: 4px solid #00BCD4;

      .meal-icon.supper,
      .meal-icon.midnight_snack {
        color: #00BCD4;
        font-size: 24px;
      }
    }

    &.health_snack,
    &.fitness_meal {
      border-left: 4px solid #4CAF50;

      .meal-icon.health_snack,
      .meal-icon.fitness_meal {
        color: #4CAF50;
        font-size: 24px;
      }
    }

    &.dessert,
    &.sweet {
      border-left: 4px solid #E91E63;

      .meal-icon.dessert,
      .meal-icon.sweet {
        color: #E91E63;
        font-size: 24px;
      }
    }

    &.soup,
    &.porridge {
      border-left: 4px solid #009688;

      .meal-icon.soup,
      .meal-icon.porridge {
        color: #009688;
        font-size: 24px;
      }
    }

    &.salad,
    &.vegetable {
      border-left: 4px solid #8BC34A;

      .meal-icon.salad,
      .meal-icon.vegetable {
        color: #8BC34A;
        font-size: 24px;
      }
    }

    &.meat,
    &.protein {
      border-left: 4px solid #795548;

      .meal-icon.meat,
      .meal-icon.protein {
        color: #795548;
        font-size: 24px;
      }
    }

    // 默认样式
    &.info {
      border-left: 4px solid #00BCD4;

      .meal-icon.info {
        color: #00BCD4;
        font-size: 24px;
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
