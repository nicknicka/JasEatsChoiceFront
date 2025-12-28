<script setup>
import { ref } from 'vue'
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
const emit = defineEmits(['close', 'add'])

// 新菜品输入
const newDish = ref({
  name: '',
  ingredients: [], // 食材列表
  calories: 0,
  protein: 0,
  carbs: 0,
  fat: 0
})

// 食材输入
const newIngredient = ref('')

// 添加食材
const addIngredient = () => {
  if (newIngredient.value.trim()) {
    // 防止重复添加
    if (!newDish.value.ingredients.includes(newIngredient.value.trim())) {
      newDish.value.ingredients.push(newIngredient.value.trim())
    }
    newIngredient.value = '' // 清空输入
  }
}

// 移除食材
const removeIngredient = (index) => {
  newDish.value.ingredients.splice(index, 1)
}

// 验证菜品名称格式的函数
const isValidDishName = (name) => {
  // 允许中文、英文、数字、空格以及常见的标点符号
  const nameRegex = /^[\u4e00-\u9fa5a-zA-Z0-9\s\-\_\(\)\[\]\{\}\/\.\,，。！？；：]*$/
  return nameRegex.test(name.trim())
}

// 确认添加菜品
const confirmAddDish = () => {
  if (props.recipe && newDish.value.name.trim()) {
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

    // 发送添加事件
    emit('add', props.recipe, dishWithIngredients)

    // 重置表单
    resetForm()

    // 关闭对话框
    emit('close')
  } else {
    ElMessage.error('请输入菜品名称')
  }
}

// 处理关闭事件
const handleClose = () => {
  resetForm()
  emit('close')
}

// 重置表单
const resetForm = () => {
  newDish.value = {
    name: '',
    ingredients: [],
    calories: 0,
    protein: 0,
    carbs: 0,
    fat: 0
  }
  newIngredient.value = ''
}
</script>

<template>
  <el-dialog
    v-model="props.visible"
    :title="recipe ? `为${recipe.name}添加菜品` : '添加菜品'"
    width="500px"
    top="20%"
    @close="handleClose"
  >
    <div v-if="recipe" class="add-dish-form">
      <el-form class="form-container">
        <el-form-item label="菜品名称" class="is-required">
          <el-input
            v-model="newDish.name"
            placeholder="请输入新菜品名称"
            @blur="
              () => {
                if (newDish.name.trim() && !isValidDishName(newDish.name)) {
                  ElMessage.error('菜品名称只能包含中文、英文、数字和常见符号')
                }
              }
            "
          />
        </el-form-item>

        <!-- 食材输入区域 -->
        <el-form-item label="食材（非必选）">
          <div class="ingredients-input">
            <el-input
              v-model="newIngredient"
              placeholder="请输入食材名称"
              @keyup.enter="addIngredient"
            />
            <el-button type="primary" size="small" @click="addIngredient"> 添加食材 </el-button>
          </div>

          <!-- 食材列表 -->
          <div v-if="newDish.ingredients.length > 0" class="ingredients-list">
            <el-tag
              v-for="(ingredient, index) in newDish.ingredients"
              :key="index"
              closable
              @close="removeIngredient(index)"
            >
              {{ ingredient }}
            </el-tag>
          </div>
        </el-form-item>

        <!-- 营养数据编辑 -->
        <el-form-item label="营养数据">
          <div class="nutrition-edit-section">
            <div class="nutrition-input-group">
              <el-input-number
                v-model="newDish.calories"
                :min="0"
                :precision="0"
                style="width: 150px"
                placeholder="卡路里 (kcal)"
              />
            </div>
            <div class="nutrition-input-group">
              <el-input-number
                v-model="newDish.protein"
                :min="0"
                :precision="1"
                style="width: 150px"
                placeholder="蛋白质 (g)"
              />
            </div>
            <div class="nutrition-input-group">
              <el-input-number
                v-model="newDish.carbs"
                :min="0"
                :precision="1"
                style="width: 150px"
                placeholder="碳水化合物 (g)"
              />
            </div>
            <div class="nutrition-input-group">
              <el-input-number
                v-model="newDish.fat"
                :min="0"
                :precision="1"
                style="width: 150px"
                placeholder="脂肪 (g)"
              />
            </div>
          </div>
        </el-form-item>
      </el-form>
    </div>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="confirmAddDish"> 确定 </el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
/* 添加菜品对话框样式 */
.add-dish-form {
  .form-container {
    background: linear-gradient(135deg, #ffffff 0%, #f8f9ff 100%);
    padding: 24px;
    border-radius: 12px;
    border: 1px solid #e3f2fd;
  }

  /* 表单标签样式 */
  .el-form-item__label {
    font-weight: 700 !important;
    font-size: 14px !important;
    color: #2c3e50 !important;
  }

  /* 必填项红色星号 */
  .el-form-item.is-required > .el-form-item__label::before {
    color: #ff4d4f;
    font-weight: 700;
  }

  /* 输入框样式 */
  .el-input__wrapper {
    border-radius: 8px !important;
    border: 1px solid #d9d9d9 !important;
    transition: all 0.3s ease !important;

    &:focus-within {
      border-color: #667eea !important;
      box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1) !important;
    }
  }

  /* 食材输入区域样式 */
  .ingredients-input {
    display: flex;
    gap: 12px;
    align-items: center;
  }

  /* 添加食材按钮样式 */
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

  /* 食材列表样式 */
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

/* 营养编辑区域样式 */
.nutrition-edit-section {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-top: 8px;
}

.nutrition-input-group {
  margin-bottom: 8px;
}
</style>
