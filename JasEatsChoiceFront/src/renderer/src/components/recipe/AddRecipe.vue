<script setup>
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ForkSpoon as ForkSpoonIcon,
  List as ListIcon,
  Clock as ClockIcon,
  Document as DocumentIcon
} from '@element-plus/icons-vue'

// 接收props
const props = defineProps({
  visible: Boolean
})

// 定义事件
const emit = defineEmits(['update:visible', 'add-recipe'])

// 关闭对话框
const closeDialog = () => {
  emit('update:visible', false)
  resetForm()
}

// 新食谱表单数据
const newRecipe = ref({
  name: '',
  type: '早餐',
  time: '',
  details: '', // 食谱描述字段
})

// 表单验证规则
const formRules = ref({
  name: [
    { required: true, message: '请填写食谱名称', trigger: 'blur' },
    { min: 2, max: 50, message: '食谱名称长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  time: [
    { required: true, message: '请选择准备时间', trigger: 'change' }
  ]
})

// 重置表单
const resetForm = () => {
  newRecipe.value = {
    name: '',
    type: '早餐',
    time: '',
    details: '',
  }
}

// 保存新食谱
const saveNewRecipe = () => {
  // 简单的表单验证
  if (!newRecipe.value.name.trim()) {
    ElMessage.warning('请填写食谱名称')
    return
  }

  // 准备时间验证
  if (!newRecipe.value.time) {
    ElMessage.warning('请选择有效的准备时间')
    return
  }

  // 创建新食谱对象
  const recipe = {
    id: Date.now(), // 使用时间戳作为唯一ID
    name: newRecipe.value.name,
    type: newRecipe.value.type,
    time: newRecipe.value.time,
    details: newRecipe.value.details,
  }

  // 发送添加事件
  emit('add-recipe', recipe)
  closeDialog()
  ElMessage.success('食谱已添加')
}
</script>

<template>
  <el-dialog
    :model-value="visible"
    @update:model-value="(val) => emit('update:visible', val)"
    title="添加新食谱"
    width="500px"
    top="10%"
    transition="dialog-fade"
  >
    <div class="add-recipe-form">
      <el-form
        :model="newRecipe"
        label-width="120px"
        status-icon
        :rules="formRules"
        @keyup.enter.native="saveNewRecipe"
      >
        <el-form-item label="名称" prop="name" required>
          <template #label>
            <div class="form-item-label">
              <el-icon class="label-icon"><ForkSpoonIcon /></el-icon>
              <span>名&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;称</span>
            </div>
          </template>
          <el-input v-model="newRecipe.name" placeholder="例：牛奶燕麦粥" />
        </el-form-item>

        <el-form-item label="类型" prop="type" required>
          <template #label>
            <div class="form-item-label">
              <el-icon class="label-icon"><ListIcon /></el-icon>
              <span>类&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;型</span>
            </div>
          </template>
          <el-select v-model="newRecipe.type" style="width: 100%">
            <el-option label="早餐" value="早餐" />
            <el-option label="午餐" value="午餐" />
            <el-option label="晚餐" value="晚餐" />
            <el-option label="加餐" value="加餐" />
          </el-select>
        </el-form-item>

        <el-form-item label="准备时间" prop="time" required>
          <template #label>
            <div class="form-item-label">
              <el-icon class="label-icon"><ClockIcon /></el-icon>
              <span>准备时间</span>
            </div>
          </template>
          <el-time-picker
            v-model="newRecipe.time"
            placeholder="例：00:15"
            type="time"
            format="HH:mm"
            value-format="HH:mm"
            :is-range="false"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="食谱详情" prop="details">
          <template #label>
            <div class="form-item-label">
              <el-icon class="label-icon"><DocumentIcon /></el-icon>
              <span>食谱详情</span>
            </div>
          </template>
          <el-input
            v-model="newRecipe.details"
            type="textarea"
            :rows="5"
            placeholder="请输入详细的食材和烹饪步骤"
          />
        </el-form-item>
      </el-form>
    </div>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="closeDialog">取消</el-button>
        <el-button type="primary" @click="saveNewRecipe">确定</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<style scoped>
/* 表单容器 */
.add-recipe-form {
  padding: 30px 0;
  max-width: 440px;
  margin: 0 auto;
}

/* 自定义Dialog样式 */
:deep(.el-dialog__header) {
  border-bottom: 2px solid rgba(102, 126, 234, 0.3);
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  padding: 24px 28px;
}

:deep(.el-dialog__title) {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

:deep(.el-dialog__body) {
  padding: 32px 28px;
}

/* 表单字段样式 */
:deep(.el-form-item) {
  margin-bottom: 32px; /* 增加字段间距 */
}

/* 带图标的标签样式 */
.form-item-label {
  display: flex;
  align-items: center;
  gap: 8px;
}

.label-icon {
  font-size: 18px;
  color: #667eea;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  vertical-align: middle;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #555;
  font-size: 14px;
}

:deep(.el-form-item__label::before) {
  content: '';
  display: none; /* 隐藏原来的指示线 */
}

/* 输入框样式 */
:deep(.el-input__wrapper),
:deep(.el-select__wrapper),
:deep(.el-textarea__inner) {
  border-radius: 8px;
  border: 2px solid #e5e7eb;
  transition: all 0.3s ease;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

:deep(.el-input__wrapper:hover),
:deep(.el-select__wrapper:hover),
:deep(.el-textarea__inner:hover) {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

:deep(.el-input__wrapper.is-focus),
:deep(.el-select__wrapper.is-focus),
:deep(.el-textarea__inner.is-focus) {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.15);
}

/* 时间选择器样式 */
:deep(.el-time-picker__input) {
  font-size: 14px;
}

/* 弹窗动画 */
.dialog-fade-enter-active,
.dialog-fade-leave-active {
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.dialog-fade-enter-from {
  opacity: 0;
  transform: translateY(-20px) scale(0.95);
}

.dialog-fade-leave-to {
  opacity: 0;
  transform: translateY(20px) scale(0.95);
}

/* 按钮样式 */
:deep(.dialog-footer) {
  text-align: center;
  padding: 0 28px 24px;
}

:deep(.dialog-footer .el-button) {
  padding: 10px 28px;
  border-radius: 8px;
  font-weight: 500;
  font-size: 14px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

:deep(.dialog-footer .el-button--primary) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

:deep(.dialog-footer .el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.3);
}

:deep(.dialog-footer .el-button--default) {
  border-color: #e5e7eb;
}

:deep(.dialog-footer .el-button--default:hover) {
  border-color: #667eea;
  color: #667eea;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
}
</style>
