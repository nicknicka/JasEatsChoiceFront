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
        <span class="detail-label">烹饪时间:</span>
        <span class="detail-value">
          {{ recipe.cookTime || '未设置' }}
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
      font-weight: 600;
      font-size: 15px;
      color: #555;
      margin-right: 12px;
      padding: 6px 14px;
      background: #f5f5f5;
      border-radius: 18px;
      border: 1px solid #ddd;
      margin-bottom: 10px;
      display: inline-block;
    }

    .detail-value {
      font-size: 14px;
      color: #666;
    }

    /* 餐型值样式 */
    .detail-item:first-child .detail-value {
      font-size: 18px;
      font-weight: 600;
      color: #4a90e2;
      margin-left: 6px;
    }

    /* 烹饪时间值样式 */
    .detail-item:nth-child(2) .detail-value {
      font-size: 14px;
      color: #888;
    }

    .nutrition-info {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
      gap: 12px;
      padding: 16px;
      background: #fafafa;
      border-radius: 10px;
      border: 1px solid #eee;
      margin-top: 10px;
    }

    .nutrition-item {
      margin-bottom: 0;
      padding: 10px 14px;
      background: white;
      border-radius: 6px;
      border: 1px solid #e0e0e0;
      transition: all 0.3s ease;

      &:hover {
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
        border-color: #d0d0d0;
      }

      .nutrition-label {
        font-weight: 500;
        font-size: 13px;
        color: #666;
        display: block;
        margin-bottom: 3px;
      }

      .nutrition-value {
        color: #4a90e2;
        font-weight: 600;
        font-size: 18px;
        margin-left: 0;
      }
    }

    /* 菜品列表样式 */
    .dish-list {
      display: flex;
      flex-direction: column;
      gap: 20px;
      margin-top: 14px;
      max-height: 200px;
      overflow-y: auto;
      padding-right: 10px;
    }

    .dish-item {
      padding: 18px;
      background: white;
      border-radius: 10px;
      border-left: 4px solid #4a90e2;
      border: 1px solid #e8e8e8;
      box-shadow: 0 1px 4px rgba(0, 0, 0, 0.02);
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

      &:hover {
        box-shadow: 0 3px 12px rgba(0, 0, 0, 0.04);
        border-color: #c4e1ff;
      }

      /* 待添加菜品样式 */
      &.empty-dish {
        background: #fafafa !important;
        border: 1px dashed #ccc !important;
        border-left: 4px solid #aaa !important;
        opacity: 0.8;

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
      font-size: 16px;
      font-weight: 600;
      margin: 0 0 12px 0;
      color: #333;
      display: flex;
      align-items: center;
      gap: 8px;

      &::before {
        content: '🍽️';
        font-size: 20px;
      }
    }

    .dish-ingredients {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      margin-top: 8px;
    }

    .dish-ingredients .el-tag {
      background: white;
      border: 1px solid #ddd;
      color: #666;
      font-weight: 400;
      opacity: 1;
      font-size: 13px;

      &:hover {
        background: #f5f5f5;
        border-color: #ccc;
        transform: none;
      }
    }

    .no-ingredients {
      margin-top: 10px;
    }

    .no-ingredients .el-tag {
      background: white;
      border: 1px solid #ffd4d4;
      color: #ff6b6b;
    }
  }
}
</style>
