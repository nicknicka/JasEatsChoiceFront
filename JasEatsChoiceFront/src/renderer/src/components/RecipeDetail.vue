<script setup>
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'

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
const emit = defineEmits(['close', 'update:visible', 'update:cookTime', 'update:details'])

// 处理关闭事件
const handleClose = () => {
  emit('close')
  emit('update:visible', false)
}

// 烹饪时间编辑相关变量
const isEditingCookTime = ref(false)
const cookTimeValue = ref(null)

// 食谱详情编辑相关变量
const isEditingDetails = ref(false)
const detailsValue = ref('')

// 监听recipe变化，初始化烹饪时间
watch(
  () => props.recipe?.cookTime,
  (newVal) => {
    if (newVal) {
      cookTimeValue.value = newVal
    } else {
      cookTimeValue.value = '00:00'
    }
  },
  { immediate: true }
)

// 监听recipe变化，初始化食谱详情
watch(
  () => props.recipe?.details,
  (newVal) => {
    detailsValue.value = newVal || ''
  },
  { immediate: true }
)

// 开始编辑烹饪时间
const startEditCookTime = () => {
  isEditingCookTime.value = true
}

// 保存烹饪时间
const saveCookTime = () => {
  if (!cookTimeValue.value) {
    ElMessage.warning('请选择烹饪时间')
    return
  }

  // 通知父组件更新烹饪时间
  emit('update:cookTime', cookTimeValue.value)
  isEditingCookTime.value = false
}

// 取消编辑烹饪时间
const cancelEditCookTime = () => {
  // 恢复原始值
  cookTimeValue.value = props.recipe?.cookTime || '00:00'
  isEditingCookTime.value = false
}

// 开始编辑食谱详情
const startEditDetails = () => {
  isEditingDetails.value = true
}

// 保存食谱详情
const saveDetails = () => {
  if (!detailsValue.value) {
    ElMessage.warning('请输入食谱详情')
    return
  }

  // 通知父组件更新食谱详情
  emit('update:details', detailsValue.value)
  isEditingDetails.value = false
}

// 取消编辑食谱详情
const cancelEditDetails = () => {
  // 恢复原始值
  detailsValue.value = props.recipe?.details || ''
  isEditingDetails.value = false
}

// 根据食谱类型获取颜色主题
const getThemeColor = () => {
  if (!props.recipe?.type) return 'default'

  switch (props.recipe.type) {
    case '早餐':
    case 'breakfast':
    case '早午餐':
    case 'brunch':
      return 'breakfast' // 黄色/橙色系
    case '午餐':
    case 'lunch':
      return 'lunch' // 绿色系
    case '晚餐':
    case 'dinner':
    case 'supper':
      return 'dinner' // 蓝色系
    case '加餐':
    case '下午加餐':
    case 'afternoon_tea':
    case '宵夜':
    case '夜宵':
    case 'night_snack':
    case 'snack':
      return 'snack' // 深蓝色系
    default:
      return 'default'
  }
}

// 计算营养数据
const nutritionData = computed(() => {
  if (!props.recipe) {
    return {
      calories: 0,
      protein: 0,
      carbs: 0,
      fat: 0
    }
  }

  // 确保items是数组，优先使用recipe.items，如果为空则使用recipe.ingredients
  let items = []
  if (props.recipe.items) {
    items = Array.isArray(props.recipe.items)
      ? props.recipe.items
      : typeof props.recipe.items === 'string'
        ? JSON.parse(props.recipe.items)
        : []
  } else if (props.recipe.ingredients) {
    items = Array.isArray(props.recipe.ingredients)
      ? props.recipe.ingredients
      : typeof props.recipe.ingredients === 'string'
        ? JSON.parse(props.recipe.ingredients)
        : []
  }

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

// 格式化烹饪时间显示
const formatCookTime = (time) => {
  if (!time) return '未设置'

  const [hours, minutes] = time.split(':').map(Number)

  if (hours === 0 && minutes === 0) {
    return '0'
  }

  let formatted = ''
  if (hours > 0) {
    formatted += `${hours} 小时`
  }

  if (minutes > 0) {
    formatted += (formatted ? ' ' : '') + `${minutes} 分钟`
  }

  return formatted
}
</script>

<template>
  <el-dialog
    :model-value="props.visible"
    :title="recipe ? `${recipe.name} 详情` : '食谱详情'"
    width="600px"
    top="5%"
    @update:model-value="emit('update:visible', $event)"
    @close="handleClose"
  >
    <div v-if="recipe" class="recipe-details" :class="getThemeColor()">
      <!-- 食谱封面与标题区域 -->
      <div class="recipe-header-section">
        <div class="recipe-title-block">
          <h2 class="recipe-main-title">{{ recipe.name }}</h2>
          <div class="recipe-basic-info">
            <div class="info-item">
              <span class="info-label">餐型:</span>
              <span class="info-value type-tag">{{ getMealTypeName(recipe.type) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">烹饪时间:</span>
              <div class="cook-time-control">
                <div v-if="!isEditingCookTime" class="cook-time-display">
                  <span class="time-text">{{ formatCookTime(recipe.cookTime) }}</span>
                  <el-button
                    type="primary"
                    size="small"
                    class="edit-cook-time-btn"
                    @click="startEditCookTime"
                  >
                    编辑
                  </el-button>
                </div>

                <div v-else class="cook-time-editor">
                  <el-time-picker
                    v-model="cookTimeValue"
                    type="time"
                    :picker-options="{
                      selectableRange: '00:00 - 23:59',
                      step: '00:01'
                    }"
                    format="HH:mm"
                    value-format="HH:mm"
                    placeholder="选择烹饪时间"
                    size="default"
                  />

                  <div class="editor-action-buttons">
                    <el-button type="primary" size="small" class="save-btn" @click="saveCookTime">
                      保存
                    </el-button>
                    <el-button
                      type="default"
                      size="small"
                      class="cancel-btn"
                      @click="cancelEditCookTime"
                    >
                      取消
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 内容区域 -->
      <div class="recipe-content-section">
        <!-- 营养信息卡片 -->
        <div class="nutrition-section">
          <div class="section-title">营养信息</div>
          <div class="nutrition-card-container">
            <div
              v-for="(nutrient, index) in [
                { label: '卡路里', value: nutritionData.calories + ' kcal', icon: '🔥' },
                { label: '蛋白质', value: nutritionData.protein + ' g', icon: '💪' },
                { label: '碳水化合物', value: nutritionData.carbs + ' g', icon: '🍚' },
                { label: '脂肪', value: nutritionData.fat + ' g', icon: '🥑' }
              ]"
              :key="index"
              class="nutrition-card"
            >
              <div class="nutrition-icon">{{ nutrient.icon }}</div>
              <div class="nutrition-label">{{ nutrient.label }}</div>
              <div class="nutrition-value">{{ nutrient.value }}</div>
            </div>
          </div>
        </div>

        <!-- 菜品列表区域 -->
        <div class="dishes-section">
          <div class="section-title">菜品组成</div>
          <div class="dish-list-container">
            <div
              v-for="(item, index) in recipe.items && recipe.items.length > 0
                ? recipe.items
                : recipe.ingredients && recipe.ingredients.length > 0
                  ? recipe.ingredients
                  : ['待添加菜品']"
              :key="index"
              class="dish-card-item"
              :class="{ 'empty-dish-card': typeof item === 'string' }"
            >
              <div class="dish-card-header">
                <span class="dish-emoji" :style="{ fontSize: '24px' }">
                  {{
                    typeof item === 'object' && item.type
                      ? {
                          主食: '🍚',
                          蔬菜: '🥬',
                          肉类: '🥩',
                          鱼类: '🐟',
                          汤品: '🍲',
                          水果: '🍎',
                          甜点: '🍰'
                        }[item.type] || '🍽️'
                      : '🍽️'
                  }}
                </span>
                <h3 class="dish-name-text">
                  {{ typeof item === 'object' ? item.name : item }}
                </h3>
              </div>

              <div class="dish-card-body">
                <div
                  v-if="typeof item === 'object' && item.ingredients && item.ingredients.length > 0"
                  class="ingredients-list"
                >
                  <el-tag
                    v-for="(ingredient, ingIndex) in item.ingredients"
                    :key="ingIndex"
                    size="small"
                  >
                    {{ ingredient }}
                  </el-tag>
                </div>
                <div
                  v-else-if="
                    typeof item === 'object' && (!item.ingredients || item.ingredients.length === 0)
                  "
                  class="no-ingredients-info"
                >
                  <el-tag size="small" type="warning">无食材信息</el-tag>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 食谱详情信息 -->
        <div class="details-section">
          <div class="section-title">食谱详情</div>
          <div class="details-content">
            <div v-if="!isEditingDetails" class="details-display">
              <span class="details-text">{{ detailsValue || '暂无详情' }}</span>
              <span class="edit-details-text" @click="startEditDetails">编辑</span>
            </div>

            <div v-else class="details-editor">
              <el-input
                v-model="detailsValue"
                type="textarea"
                :rows="3"
                placeholder="请输入食谱详情"
                size="default"
                style="width: 100%"
              />

              <div class="editor-action-buttons">
                <el-button type="primary" size="small" class="save-btn" @click="saveDetails">
                  保存
                </el-button>
                <el-button
                  type="default"
                  size="small"
                  class="cancel-btn"
                  @click="cancelEditDetails"
                >
                  取消
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button type="primary" size="default" @click="handleClose">关闭</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
/* 食谱详情对话框样式 */
.recipe-details {
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  padding: 16px;
  border-radius: 20px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
}

/* 食谱头部区域 */
.recipe-header-section {
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 2px solid #f0f2f5;
}

.recipe-title-block {
  text-align: center;
}

.recipe-main-title {
  font-size: 24px;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 16px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.recipe-basic-info {
  display: flex;
  justify-content: center;
  gap: 40px;
  flex-wrap: wrap;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.info-label {
  font-weight: 600;
  color: #667eea;
  font-size: 14px;
}

.info-value {
  font-size: 16px;
  font-weight: 500;
  color: #4a5568;
}

.type-tag {
  background: linear-gradient(135deg, #e6f2ff 0%, #e8d5ff 100%); /* 浅色系渐变 */
  color: #333; /* 文字颜色改为深色以保证可读性 */
  padding: 8px 18px;
  border-radius: 20px;
  font-weight: 600;
}

/* 烹饪时间控制 */
.cook-time-control {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 食谱详情控制 */
.details-control {
  width: 100%;
  max-width: 600px;
}

.details-display {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 12px;
  background-color: #f9fafb;
  border-radius: 8px;
  margin-bottom: 10px;
}

.details-text {
  font-size: 14px;
  line-height: 1.6;
  color: #4a5568;
  white-space: pre-wrap;
}

.edit-details-btn {
  align-self: flex-start;
  border-radius: 20px;
  padding: 6px 18px;
}

.details-editor {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.cook-time-display {
  display: flex;
  align-items: center;
  gap: 10px;
}

.time-text {
  font-size: 18px;
  font-weight: 600;
  color: #f57c00;
}

.edit-cook-time-btn {
  border-radius: 20px;
  padding: 8px 20px;
}

.cook-time-editor {
  display: flex;
  align-items: center;
  gap: 12px;
}

.time-picker {
  width: 180px;
}

.editor-action-buttons {
  display: flex;
  gap: 8px;
}

/* 内容区域 */
.recipe-content-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.section-title {
  font-size: 18px;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 12px;
  padding-left: 8px;
  border-left: 4px solid #667eea;
}

/* 营养信息 */
.nutrition-section {
  background: linear-gradient(135deg, #ffffff 0%, #f8f9ff 100%);
  padding: 24px;
  border-radius: 16px;
  border: 2px solid #e6ecf5;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.08);
}

.nutrition-card-container {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.nutrition-card {
  background: linear-gradient(135deg, #ffffff 0%, #f9fafb 100%);
  padding: 16px;
  border-radius: 14px;
  text-align: center;
  border: 2px solid #e2e8f0;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.nutrition-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.15);
  border-color: #667eea;
}

.nutrition-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.nutrition-label {
  font-size: 14px;
  font-weight: 600;
  color: #718096;
  margin-bottom: 8px;
}

.nutrition-value {
  font-size: 20px;
  font-weight: 700;
  color: #667eea;
}

/* 菜品列表 */
.dishes-section {
  background: linear-gradient(135deg, #ffffff 0%, #f8f9ff 100%);
  padding: 16px;
  border-radius: 16px;
  border: 2px solid #e6ecf5;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.08);
}

/* 食谱详情 */
.details-section {
  background: linear-gradient(135deg, #ffffff 0%, #f8f9ff 100%);
  padding: 16px;
  border-radius: 16px;
  border: 2px solid #e6ecf5;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.08);
  margin-top: 16px;
}

.details-content {
  margin-top: 12px;
}

.details-display {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 12px;
  background-color: #f9fafb;
  border-radius: 8px;
}

.details-text {
  font-size: 14px;
  line-height: 1.6;
  color: #4a5568;
  white-space: pre-wrap;
}

.edit-details-btn {
  align-self: flex-start;
  border-radius: 20px;
  padding: 6px 18px;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.edit-details-text {
  position: absolute;
  bottom: 12px;
  right: 12px;
  cursor: pointer;
  color: #667eea;
  font-size: 14px;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.details-display:hover .edit-details-text {
  opacity: 1;
}

.details-section:hover .edit-details-btn {
  opacity: 1;
}

.details-editor {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.dish-list-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
  /* 默认不设置最大高度，由内容决定 */
  max-height: none;
  /* 当内容超出视口高度的一半时显示滚动条 */
  max-height: calc(100vh - 400px);
  overflow-y: auto;
  padding-right: 8px;
}

.dish-card-item {
  background: linear-gradient(135deg, #ffffff 0%, #f9fafb 100%);
  padding: 20px;
  border-radius: 14px;
  border-left: 5px solid #667eea;
  border: 2px solid #e2e8f0;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.dish-card-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 32px rgba(102, 126, 234, 0.12);
  border-color: #667eea;
}

.dish-card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.dish-emoji {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  color: white;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.2);
}

.dish-name-text {
  font-size: 18px;
  font-weight: 700;
  color: #2d3748;
  margin: 0;
}

.ingredients-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.ingredients-list .el-tag {
  background: linear-gradient(135deg, #667eea15 0%, #764ba215 100%);
  border: 1px solid #667eea40;
  color: #667eea;
  font-weight: 500;
  opacity: 1;
  font-size: 13px;
  border-radius: 20px;
  padding: 6px 14px;
  transition: all 0.3s ease;
}

.ingredients-list .el-tag:hover {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-color: #667eea;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
}

.no-ingredients-info {
  margin-top: 12px;
}

/* 空菜品卡片 */
.empty-dish-card {
  background: linear-gradient(135deg, #ffffff 0%, #f7fafc 100%) !important;
  border: 2px dashed #cbd5e0 !important;
  border-left: 5px solid #a0aec0 !important;
  opacity: 0.8;
}

.empty-dish-card:hover {
  transform: translateY(-3px) !important;
  cursor: default;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.05) !important;
}

.empty-dish-card .dish-name-text {
  font-style: italic;
  color: #a0aec0;
}

/* 对话框底部 */
.dialog-footer {
  text-align: center;
  padding: 20px;
}

.dialog-footer .el-button {
  border-radius: 24px;
  padding: 12px 36px;
  font-weight: 600;
  font-size: 16px;
  background: linear-gradient(135deg, #e3f2fd 0%, #f8f9ff 100%);
  border: none;
  color: #333333;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.1);
  transition: all 0.3s ease;
}

.dialog-footer .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.2);
  background: linear-gradient(135deg, #d1ecf1 0%, #e6f7ff 100%);
}

/* 不同主题颜色适配 */
.recipe-details.breakfast .section-title {
  border-left-color: #f57c00;
}

.recipe-details.lunch .section-title {
  border-left-color: #388e3c;
}

.recipe-details.dinner .section-title {
  border-left-color: #1976d2;
}

.recipe-details.snack .section-title {
  border-left-color: #1565c0;
}

/* 早餐主题样式 */
.recipe-details.breakfast {
  .detail-item:first-child .detail-value {
    color: #f57c00 !important;
    text-shadow: 0 2px 4px rgba(245, 124, 0, 0.15) !important;
  }

  .nutrition-value {
    color: #f57c00 !important;
  }

  .dish-item {
    border-left: 5px solid #f57c00 !important;

    &:hover {
      border-color: #f57c00 !important;
      box-shadow: 0 12px 32px rgba(245, 124, 0, 0.15) !important;
    }
  }

  .detail-label {
    background: linear-gradient(135deg, #f57c00 0%, #ffb74d 100%) !important;
    color: #fff !important;
    border: none !important;
    box-shadow: 0 4px 15px rgba(245, 124, 0, 0.25) !important;
  }

  .nutrition-info {
    border-color: #ffe0b2 !important;
  }

  .nutrition-item {
    &:hover {
      border-color: #f57c00 !important;
      box-shadow: 0 8px 24px rgba(245, 124, 0, 0.15) !important;
    }
  }

  .dish-ingredients .el-tag {
    background: linear-gradient(135deg, #f57c0015 0%, #ffb74d15 100%) !important;
    border: 1px solid #f57c0040 !important;
    color: #f57c00 !important;

    &:hover {
      background: linear-gradient(135deg, #f57c00 0%, #ffb74d 100%) !important;
      border-color: #f57c00 !important;
      color: white !important;
      box-shadow: 0 4px 12px rgba(245, 124, 0, 0.2) !important;
    }
  }
}

/* 午餐主题样式 */
.recipe-details.lunch {
  .detail-item:first-child .detail-value {
    color: #388e3c !important;
    text-shadow: 0 2px 4px rgba(56, 142, 60, 0.15) !important;
  }

  .nutrition-value {
    color: #388e3c !important;
  }

  .dish-item {
    border-left: 5px solid #388e3c !important;

    &:hover {
      border-color: #388e3c !important;
      box-shadow: 0 12px 32px rgba(56, 142, 60, 0.15) !important;
    }
  }

  .detail-label {
    background: linear-gradient(135deg, #388e3c 0%, #81c784 100%) !important;
    color: #fff !important;
    border: none !important;
    box-shadow: 0 4px 15px rgba(56, 142, 60, 0.25) !important;
  }

  .nutrition-info {
    border-color: #c8e6c9 !important;
  }

  .nutrition-item {
    &:hover {
      border-color: #388e3c !important;
      box-shadow: 0 8px 24px rgba(56, 142, 60, 0.15) !important;
    }
  }

  .dish-ingredients .el-tag {
    background: linear-gradient(135deg, #388e3c15 0%, #81c78415 100%) !important;
    border: 1px solid #388e3c40 !important;
    color: #388e3c !important;

    &:hover {
      background: linear-gradient(135deg, #388e3c 0%, #81c784 100%) !important;
      border-color: #388e3c !important;
      color: white !important;
      box-shadow: 0 4px 12px rgba(56, 142, 60, 0.2) !important;
    }
  }
}

/* 晚餐主题样式 */
.recipe-details.dinner {
  .detail-item:first-child .detail-value {
    color: #1976d2 !important;
    text-shadow: 0 2px 4px rgba(25, 118, 210, 0.15) !important;
  }

  .nutrition-value {
    color: #1976d2 !important;
  }

  .dish-item {
    border-left: 5px solid #1976d2 !important;

    &:hover {
      border-color: #1976d2 !important;
      box-shadow: 0 12px 32px rgba(25, 118, 210, 0.15) !important;
    }
  }

  .detail-label {
    background: linear-gradient(135deg, #1976d2 0%, #64b5f6 100%) !important;
    color: #fff !important;
    border: none !important;
    box-shadow: 0 4px 15px rgba(25, 118, 210, 0.25) !important;
  }

  .nutrition-info {
    border-color: #bbdefb !important;
  }

  .nutrition-item {
    &:hover {
      border-color: #1976d2 !important;
      box-shadow: 0 8px 24px rgba(25, 118, 210, 0.15) !important;
    }
  }

  .dish-ingredients .el-tag {
    background: linear-gradient(135deg, #1976d215 0%, #64b5f615 100%) !important;
    border: 1px solid #1976d240 !important;
    color: #1976d2 !important;

    &:hover {
      background: linear-gradient(135deg, #1976d2 0%, #64b5f6 100%) !important;
      border-color: #1976d2 !important;
      color: white !important;
      box-shadow: 0 4px 12px rgba(25, 118, 210, 0.2) !important;
    }
  }
}

/* 加餐主题样式 */
.recipe-details.snack {
  .detail-item:first-child .detail-value {
    color: #1565c0 !important;
    text-shadow: 0 2px 4px rgba(21, 101, 192, 0.15) !important;
  }

  .nutrition-value {
    color: #1565c0 !important;
  }

  .dish-item {
    border-left: 5px solid #1565c0 !important;

    &:hover {
      border-color: #1565c0 !important;
      box-shadow: 0 12px 32px rgba(21, 101, 192, 0.15) !important;
    }
  }

  .detail-label {
    background: linear-gradient(135deg, #1565c0 0%, #42a5f5 100%) !important;
    color: #fff !important;
    border: none !important;
    box-shadow: 0 4px 15px rgba(21, 101, 192, 0.25) !important;
  }

  .nutrition-info {
    border-color: #80deea !important;
  }

  .nutrition-item {
    &:hover {
      border-color: #1565c0 !important;
      box-shadow: 0 8px 24px rgba(21, 101, 192, 0.15) !important;
    }
  }

  .dish-ingredients .el-tag {
    background: linear-gradient(135deg, #1565c015 0%, #42a5f515 100%) !important;
    border: 1px solid #1565c040 !important;
    color: #1565c0 !important;

    &:hover {
      background: linear-gradient(135deg, #1565c0 0%, #42a5f5 100%) !important;
      border-color: #1565c0 !important;
      color: white !important;
      box-shadow: 0 4px 12px rgba(21, 101, 192, 0.2) !important;
    }
  }
}
</style>
