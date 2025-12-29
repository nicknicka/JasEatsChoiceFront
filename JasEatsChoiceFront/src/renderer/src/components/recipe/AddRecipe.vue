<script setup>
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

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
  >
    <div class="add-recipe-form">
      <el-form :model="newRecipe" label-width="80px" status-icon>
        <el-form-item label="名称" prop="name" required>
          <el-input v-model="newRecipe.name" placeholder="请输入食谱名称" />
        </el-form-item>

        <el-form-item label="类型" prop="type" required>
          <el-select v-model="newRecipe.type" style="width: 100%">
            <el-option label="早餐" value="早餐" />
            <el-option label="午餐" value="午餐" />
            <el-option label="晚餐" value="晚餐" />
            <el-option label="加餐" value="加餐" />
          </el-select>
        </el-form-item>

        <el-form-item label="准备时间" prop="time" required>
          <el-time-picker
            v-model="newRecipe.time"
            placeholder="选择准备时间"
            type="time"
            format="HH:mm:ss"
            value-format="HH:mm:ss"
            :is-range="false"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="食谱详情" prop="details">
          <el-input
            v-model="newRecipe.details"
            type="textarea"
            :rows="3"
            placeholder="请输入食谱详情"
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
.add-recipe-form {
  padding: 20px 0;
  max-width: 400px;
  margin: 0 auto;
}
</style>
