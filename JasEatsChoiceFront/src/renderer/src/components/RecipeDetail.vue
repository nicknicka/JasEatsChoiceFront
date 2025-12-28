<script setup>
import { computed } from 'vue'

// 接收从父组件传递的 props
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  recipe: {
    type: Object,
    default: null
  }
})

// 定义事件
const emit = defineEmits(['close'])

// 处理关闭事件
const handleClose = () => {
  emit('close')
}

// 计算营养数据
const nutritionData = computed(() => {
  if (!props.recipe || !props.recipe.items) {
    return {
      calories: 0,
      protein: 0,
      carbs: 0,
      fat: 0
    }
  }

  // 确保items是数组
  const items = Array.isArray(props.recipe.items)
    ? props.recipe.items
    : typeof props.recipe.items === 'string'
      ? JSON.parse(props.recipe.items)
      : []

  return items.reduce(
    (acc, dish) => {
      return {
        calories: acc.calories + (dish?.calories || 0),
        protein: acc.protein + (dish?.protein || 0),
        carbs: acc.carbs + (dish?.carbs || 0),
        fat: acc.fat + (dish?.fat || 0)
      }
    },
    { calories: 0, protein: 0, carbs: 0, fat: 0 }
  )
})

// 格式化餐型显示
const getMealTypeName = (type) => {
  const mealTypeMap = {
    breakfast: '早餐',
    lunch: '午餐',
    dinner: '晚餐',
    afternoon_tea: '下午茶',
    tea: '茶点',
    night_snack: '夜宵',
    snack: '零食',
    morning_snack: '上午加餐',
    brunch: '早午餐',
    supper: '晚餐',
    midnight_snack: '宵夜',
    health_snack: '健康零食',
    fitness_meal: '健身餐',
    dessert: '甜点',
    sweet: '甜食',
    soup: '汤',
    porridge: '粥',
    salad: '沙拉',
    vegetable: '蔬菜',
    meat: '肉类',
    protein: '蛋白质'
  }
  return mealTypeMap[type] || type
}
</script>

<template>
  <el-dialog
    v-model="props.visible"
    :title="recipe ? `${recipe.name} 详情` : '食谱详情'"
    width="600px"
    top="10%"
    @close="handleClose"
  >
    <div v-if="recipe" class="recipe-details">
      <div class="detail-item">
        <span class="detail-label">餐型:</span>
        <span class="detail-value">
          {{ getMealTypeName(recipe.type) }}
        </span>
      </div>
      <div class="detail-item">
        <span class="detail-label">菜品:</span>
        <div class="detail-value dish-list">
          <div
            v-for="(item, index) in recipe.items && recipe.items.length > 0
              ? recipe.items
              : ['待添加菜品']"
            :key="index"
            class="dish-item"
            :class="{ 'empty-dish': typeof item === 'string' }"
          >
            <h5 class="dish-name">
              {{ typeof item === 'object' ? item.name : item }}
            </h5>
            <div
              v-if="typeof item === 'object' && item.ingredients && item.ingredients.length > 0"
              class="dish-ingredients"
            >
              <el-tag
                v-for="(ingredient, ingIndex) in item.ingredients"
                :key="ingIndex"
                size="small"
                type="info"
              >
                {{ ingredient }}
              </el-tag>
            </div>
            <div
              v-else-if="
                typeof item === 'object' && (!item.ingredients || item.ingredients.length === 0)
              "
              class="no-ingredients"
            >
              <el-tag size="small" type="warning">无食材信息</el-tag>
            </div>
          </div>
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

    <template #footer>
      <el-button @click="handleClose">关闭</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
/* 食谱详情对话框样式 */
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

    /* 餐型值样式 */
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

    /* 菜品列表样式 */
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

      /* 待添加菜品样式 */
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
</style>
