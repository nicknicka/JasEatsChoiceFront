<script setup>
import { useAuthStore } from '../../store/authStore'
import { useUserStore } from '../../store/userStore'
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElTag, ElIcon, ElButton, ElDialog, ElForm, ElFormItem, ElInput, ElInputNumber, ElSwitch, ElUpload, ElMessageBox, ElCascader } from 'element-plus'
import {
  Location,
  Phone,
  Message,
  Clock,
  ShoppingBag,
  Star,
  Money,
  CircleCheck,
  CircleClose,
  Loading,
  Edit,
  Plus,
  Document,
  Switch
} from '@element-plus/icons-vue'
import api from '../../utils/api.js'
import { API_CONFIG } from '../../config/index.js'

const authStore = useAuthStore()
const userStore = useUserStore()
const loading = ref(false)

// 编辑对话框相关
const editDialogVisible = ref(false)
const editForm = ref({})
const editFormRef = ref(null)

// 地址选项数据（示例数据，包含主要省份和城市）
const addressOptions = ref([
  {
    value: '北京',
    label: '北京',
    children: [
      {
        value: '北京市',
        label: '北京市',
        children: [
          { value: '东城区', label: '东城区' },
          { value: '西城区', label: '西城区' },
          { value: '朝阳区', label: '朝阳区' },
          { value: '海淀区', label: '海淀区' },
          { value: '丰台区', label: '丰台区' },
          { value: '石景山区', label: '石景山区' },
          { value: '昌平区', label: '昌平区' },
          { value: '大兴区', label: '大兴区' }
        ]
      }
    ]
  },
  {
    value: '上海',
    label: '上海',
    children: [
      {
        value: '上海市',
        label: '上海市',
        children: [
          { value: '黄浦区', label: '黄浦区' },
          { value: '徐汇区', label: '徐汇区' },
          { value: '长宁区', label: '长宁区' },
          { value: '静安区', label: '静安区' },
          { value: '普陀区', label: '普陀区' },
          { value: '虹口区', label: '虹口区' },
          { value: '杨浦区', label: '杨浦区' },
          { value: '浦东新区', label: '浦东新区' }
        ]
      }
    ]
  },
  {
    value: '广东',
    label: '广东',
    children: [
      {
        value: '广州市',
        label: '广州市',
        children: [
          { value: '天河区', label: '天河区' },
          { value: '越秀区', label: '越秀区' },
          { value: '海珠区', label: '海珠区' },
          { value: '荔湾区', label: '荔湾区' },
          { value: '白云区', label: '白云区' },
          { value: '黄埔区', label: '黄埔区' },
          { value: '番禺区', label: '番禺区' }
        ]
      },
      {
        value: '深圳市',
        label: '深圳市',
        children: [
          { value: '福田区', label: '福田区' },
          { value: '罗湖区', label: '罗湖区' },
          { value: '南山区', label: '南山区' },
          { value: '宝安区', label: '宝安区' },
          { value: '龙岗区', label: '龙岗区' },
          { value: '龙华区', label: '龙华区' },
          { value: '坪山区', label: '坪山区' }
        ]
      },
      {
        value: '东莞市',
        label: '东莞市',
        children: [
          { value: '莞城区', label: '莞城区' },
          { value: '南城区', label: '南城区' },
          { value: '东城区', label: '东城区' },
          { value: '万江区', label: '万江区' },
          { value: '虎门镇', label: '虎门镇' },
          { value: '长安镇', label: '长安镇' }
        ]
      },
      {
        value: '佛山市',
        label: '佛山市',
        children: [
          { value: '禅城区', label: '禅城区' },
          { value: '南海区', label: '南海区' },
          { value: '顺德区', label: '顺德区' },
          { value: '三水区', label: '三水区' },
          { value: '高明区', label: '高明区' }
        ]
      }
    ]
  },
  {
    value: '江苏',
    label: '江苏',
    children: [
      {
        value: '南京市',
        label: '南京市',
        children: [
          { value: '玄武区', label: '玄武区' },
          { value: '秦淮区', label: '秦淮区' },
          { value: '鼓楼区', label: '鼓楼区' },
          { value: '建邺区', label: '建邺区' },
          { value: '栖霞区', label: '栖霞区' },
          { value: '雨花台区', label: '雨花台区' },
          { value: '江宁区', label: '江宁区' }
        ]
      },
      {
        value: '苏州市',
        label: '苏州市',
        children: [
          { value: '姑苏区', label: '姑苏区' },
          { value: '虎丘区', label: '虎丘区' },
          { value: '吴中区', label: '吴中区' },
          { value: '相城区', label: '相城区' },
          { value: '吴江区', label: '吴江区' },
          { value: '工业园区', label: '工业园区' }
        ]
      },
      {
        value: '无锡市',
        label: '无锡市',
        children: [
          { value: '崇安区', label: '崇安区' },
          { value: '南长区', label: '南长区' },
          { value: '北塘区', label: '北塘区' },
          { value: '锡山区', label: '锡山区' },
          { value: '惠山区', label: '惠山区' },
          { value: '滨湖区', label: '滨湖区' }
        ]
      }
    ]
  },
  {
    value: '浙江',
    label: '浙江',
    children: [
      {
        value: '杭州市',
        label: '杭州市',
        children: [
          { value: '上城区', label: '上城区' },
          { value: '下城区', label: '下城区' },
          { value: '江干区', label: '江干区' },
          { value: '拱墅区', label: '拱墅区' },
          { value: '西湖区', label: '西湖区' },
          { value: '滨江区', label: '滨江区' },
          { value: '余杭区', label: '余杭区' }
        ]
      },
      {
        value: '宁波市',
        label: '宁波市',
        children: [
          { value: '海曙区', label: '海曙区' },
          { value: '江东区', label: '江东区' },
          { value: '江北区', label: '江北区' },
          { value: '北仑区', label: '北仑区' },
          { value: '镇海区', label: '镇海区' },
          { value: '鄞州区', label: '鄞州区' }
        ]
      },
      {
        value: '温州市',
        label: '温州市',
        children: [
          { value: '鹿城区', label: '鹿城区' },
          { value: '龙湾区', label: '龙湾区' },
          { value: '瓯海区', label: '瓯海区' },
          { value: '洞头区', label: '洞头区' },
          { value: '永嘉县', label: '永嘉县' },
          { value: '平阳县', label: '平阳县' }
        ]
      }
    ]
  }
])

// 从 userStore 中获取商家信息
const merchantInfo = computed(() => userStore.merchantInfo || {
  id: authStore.merchantId,
  name: '',
  rating: 0,
  phone: '',
  email: '',
  address: '',
  avatar: '',
  status: false,
  businessHours: '',
  category: '',
  averagePrice: 0,
  businessScope: []
})

// 获取商家信息
const fetchMerchantInfo = async () => {
  if (!authStore.merchantId) {
    console.error('商家ID不存在')
    return
  }

  loading.value = true
  try {
    await userStore.fetchMerchantInfo()
    console.log('商家信息获取成功:', userStore.merchantInfo)
  } catch (error) {
    console.error('获取商家信息失败:', error)
    ElMessage.error('获取商家信息失败')
  } finally {
    loading.value = false
  }
}

// 组件挂载时获取商家信息
onMounted(() => {
  fetchMerchantInfo()
})

// 格式化营业时间
const formatBusinessHours = (hours) => {
  if (!hours) return '暂无'
  return hours
}

// 格式化经营品类
const formatBusinessScope = (scope) => {
  if (!scope || scope === null || scope === '') return '暂无'

  // 处理字符串格式的数组，如 "[\"快餐\"]"
  let processedScope = scope
  if (typeof processedScope === 'string') {
    // 去除可能存在的引号和括号
    processedScope = processedScope.replace(/^\[|\]$/g, '').replace(/\"/g, '').replace(/\'/g, '')
    // 如果包含逗号，则分割成数组
    if (processedScope.includes(',')) {
      processedScope = processedScope.split(',').map(item => item.trim())
    }
  }

  // 确保scope是数组类型
  const scopeArray = Array.isArray(processedScope) ? processedScope : [processedScope]

  // 过滤空字符串
  const filteredScope = scopeArray.filter(item => item && item.trim())

  if (filteredScope.length === 0) return '暂无'

  return filteredScope.join('、')
}

// 格式化地址
const formatAddress = (address) => {
  if (!address) return '暂无'

  // 如果是数组（来自级联选择器），转换为字符串
  if (Array.isArray(address)) {
    return address.join('/')
  }

  // 如果是字符串，直接返回
  return address
}

// 格式化平均价格
const formatAveragePrice = (price) => {
  if (!price) return '暂无'
  return `¥${price}/人`
}

// 点击编辑按钮
const handleEditClick = () => {
  // 初始化编辑表单数据
  const info = {
    ...merchantInfo.value
  }

  // 将地址字符串转换为级联选择器需要的数组格式
  if (info.address) {
    // 尝试将地址字符串分割为省/市/区
    // 这里假设地址格式是 "省/市/区" 或类似格式
    if (typeof info.address === 'string') {
      // 尝试按常见的分隔符分割
      let addressArray = []
      if (info.address.includes('/')) {
        addressArray = info.address.split('/')
      } else if (info.address.includes(' ')) {
        addressArray = info.address.split(' ')
      } else if (info.address.includes('、')) {
        addressArray = info.address.split('、')
      } else {
        // 如果没有分隔符，可能需要根据实际数据格式处理
        addressArray = [info.address]
      }
      info.address = addressArray
    }
  }

  editForm.value = info
  editDialogVisible.value = true
}

// 表单验证
const validateForm = () => {
  // 简单的表单验证
  if (!editForm.value.name?.trim()) {
    ElMessage.warning('请输入商家名称')
    return false
  }
  if (editForm.value.name.length < 2 || editForm.value.name.length > 50) {
    ElMessage.warning('商家名称长度应在 2 到 50 个字符之间')
    return false
  }

  if (!editForm.value.phone?.trim()) {
    ElMessage.warning('请输入联系电话')
    return false
  }
  const phoneRegex = /^1[3-9]\d{9}$/
  if (!phoneRegex.test(editForm.value.phone)) {
    ElMessage.warning('请输入有效的手机号码')
    return false
  }

  if (!editForm.value.email?.trim()) {
    ElMessage.warning('请输入邮箱地址')
    return false
  }
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(editForm.value.email)) {
    ElMessage.warning('请输入有效的邮箱地址')
    return false
  }

  // 地址验证：确保选择了完整的地址（省/市/区）
  if (!editForm.value.address || !Array.isArray(editForm.value.address) || editForm.value.address.length < 3) {
    ElMessage.warning('请选择完整的店铺地址（省/市/区）')
    return false
  }

  if (!editForm.value.businessHours?.trim()) {
    ElMessage.warning('请输入营业时间')
    return false
  }

  if (!editForm.value.category?.trim()) {
    ElMessage.warning('请输入经营品类')
    return false
  }

  if (editForm.value.averagePrice === null || editForm.value.averagePrice === undefined || editForm.value.averagePrice < 0) {
    ElMessage.warning('请输入有效的平均价格')
    return false
  }

  return true
}

// 保存编辑信息
const handleSaveEdit = async () => {
  if (!validateForm()) {
    return
  }

  try {
    // 处理地址数据，将级联选择器的数组转换为字符串
    const formData = {
      ...editForm.value
    }

    // 将地址数组转换为字符串（用 "/" 分隔）
    if (Array.isArray(formData.address)) {
      formData.address = formData.address.join('/')
    }

    // 调用API更新商家信息
    const response = await api.put(API_CONFIG.merchant.update.replace('{merchantId}', authStore.merchantId), formData)

    if (response.data && response.data.success) {
      // 更新用户存储中的信息
      await userStore.fetchMerchantInfo()
      ElMessage.success('商家信息更新成功')
      editDialogVisible.value = false
    } else {
      ElMessage.error('更新失败：' + (response.data?.message || '未知错误'))
    }
  } catch (error) {
    console.error('更新商家信息失败:', error)
    ElMessage.error('更新商家信息失败')
  }
}

// 取消编辑
const handleCancelEdit = () => {
  editDialogVisible.value = false
  editFormRef.value?.resetFields()
}

// 表单验证规则
const editFormRules = {
  name: [
    { required: true, message: '请输入商家名称', trigger: 'blur' },
    { min: 2, max: 50, message: '商家名称长度应在 2 到 50 个字符之间', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号码', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
  ],
  address: [
    { required: true, message: '请输入店铺地址', trigger: 'blur' },
    { min: 5, message: '地址长度至少5个字符', trigger: 'blur' }
  ],
  businessHours: [
    { required: true, message: '请输入营业时间', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请输入经营品类', trigger: 'blur' }
  ],
  averagePrice: [
    { required: true, message: '请输入平均价格', trigger: 'blur' },
    { type: 'number', min: 0, message: '平均价格必须大于等于0', trigger: 'blur' }
  ]
}
</script>

<template>
  <div class="merchant-info-card">
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <ElIcon class="is-loading"><Loading /></ElIcon>
      <span>加载中...</span>
    </div>

    <!-- 商家信息卡片 -->
    <div v-else class="info-content">
      <div class="info-header">
        <div class="avatar-section">
          <img v-if="merchantInfo.avatar" :src="merchantInfo.avatar" class="avatar" alt="商家头像" />
          <div v-else class="avatar-placeholder">
            <ShoppingBag style="font-size: 32px; color: #409eff;" />
          </div>
        </div>

        <div class="detail-section">
          <div class="merchant-header">
            <div class="merchant-name">
              {{ merchantInfo.name }}
              <ElTag
                :type="merchantInfo.status ? 'success' : 'danger'"
                size="small"
                class="status-tag"
              >
                <ElIcon v-if="merchantInfo.status"><CircleCheck /></ElIcon>
                <ElIcon v-else><CircleClose /></ElIcon>
                {{ merchantInfo.status ? '营业中' : '已打烊' }}
              </ElTag>
            </div>
            <ElButton
              type="primary"
              size="small"
              @click="handleEditClick"
              class="edit-button"
            >
              <ElIcon><Edit /></ElIcon>
              <span>编辑信息</span>
            </ElButton>
          </div>

          <div class="merchant-rating">
            <ElTag type="warning" size="small" class="rating-tag">
              <ElIcon><Star /></ElIcon>
              <span>{{ merchantInfo.rating || 0.0 }}分</span>
            </ElTag>
          </div>

          <div class="contact-info">
            <div class="contact-item" v-if="merchantInfo.phone">
              <ElIcon class="icon"><Phone /></ElIcon>
              <span>{{ merchantInfo.phone }}</span>
            </div>
            <div class="contact-item" v-if="merchantInfo.email">
              <ElIcon class="icon"><Message /></ElIcon>
              <span>{{ merchantInfo.email }}</span>
            </div>
            <div class="contact-item" v-if="merchantInfo.address">
              <ElIcon class="icon"><Location /></ElIcon>
              <span>{{ formatAddress(merchantInfo.address) }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 详细信息 -->
      <div class="info-details">
        <div class="detail-item">
          <div class="detail-label">
            <ElIcon><Clock /></ElIcon>
            <span>营业时间</span>
          </div>
          <div class="detail-value">{{ formatBusinessHours(merchantInfo.businessHours) }}</div>
        </div>

        <div class="detail-item">
          <div class="detail-label">
            <ElIcon><ShoppingBag /></ElIcon>
            <span>经营品类</span>
          </div>
          <div class="detail-value">{{ formatBusinessScope(merchantInfo.businessScope) }}</div>
        </div>

        <div class="detail-item">
          <div class="detail-label">
            <ElIcon><Money /></ElIcon>
            <span>平均价格</span>
          </div>
          <div class="detail-value">{{ formatAveragePrice(merchantInfo.averagePrice) }}</div>
        </div>
      </div>
    </div>
  </div>
  <!-- 编辑信息对话框 -->
  <ElDialog
    v-model="editDialogVisible"
    title="编辑商家信息"
    width="600px"
    top="10%"
    transition="dialog-fade"
  >
    <div class="edit-form">
      <div class="info-item">
        <span class="info-label"><ElIcon><Document /></ElIcon> 商家名称</span>
        <ElInput
          v-model="editForm.name"
          placeholder="请输入商家名称"
          style="width: 300px"
          clearable
        />
      </div>

      <div class="info-item">
        <span class="info-label"><ElIcon><Phone /></ElIcon> 联系电话</span>
        <ElInput
          v-model="editForm.phone"
          placeholder="请输入联系电话"
          style="width: 300px"
          clearable
        />
      </div>

      <div class="info-item">
        <span class="info-label"><ElIcon><Message /></ElIcon> 邮箱地址</span>
        <ElInput
          v-model="editForm.email"
          placeholder="请输入邮箱地址"
          style="width: 300px"
          clearable
        />
      </div>

      <div class="info-item">
        <span class="info-label"><ElIcon><Location /></ElIcon> 店铺地址</span>
        <ElCascader
          v-model="editForm.address"
          :options="addressOptions"
          placeholder="请选择店铺地址"
          style="width: 300px"
          clearable
          :props="{ checkStrictly: false, expandTrigger: 'hover' }"
        />
      </div>

      <div class="info-item">
        <span class="info-label"><ElIcon><Clock /></ElIcon> 营业时间</span>
        <ElInput
          v-model="editForm.businessHours"
          placeholder="请输入营业时间"
          style="width: 300px"
          clearable
        />
      </div>

      <div class="info-item">
        <span class="info-label"><ElIcon><ShoppingBag /></ElIcon> 经营品类</span>
        <ElInput
          v-model="editForm.category"
          placeholder="请输入经营品类"
          style="width: 300px"
          clearable
        />
      </div>

      <div class="info-item">
        <span class="info-label"><ElIcon><Money /></ElIcon> 平均价格</span>
        <ElInputNumber
          v-model="editForm.averagePrice"
          :min="0"
          :step="10"
          :precision="0"
          style="width: 300px"
          placeholder="请输入平均价格"
        />
      </div>

      <div class="info-item">
        <span class="info-label"><ElIcon><Switch /></ElIcon> 营业状态</span>
        <ElSwitch
          v-model="editForm.status"
          active-text="营业中"
          inactive-text="已打烊"
          active-color="#409eff"
          inactive-color="#909399"
        />
      </div>
    </div>

    <template #footer>
      <span class="dialog-footer">
        <ElButton @click="handleCancelEdit">取消</ElButton>
        <ElButton type="primary" @click="handleSaveEdit">保存</ElButton>
      </span>
    </template>
  </ElDialog>
</template>

<style scoped lang="less">
/* 对话框淡入动画 */
:deep(.dialog-fade-enter-active),
:deep(.dialog-fade-leave-active) {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

:deep(.dialog-fade-enter-from) {
  opacity: 0;
  transform: translateY(-20px) scale(0.95);
}

:deep(.dialog-fade-leave-to) {
  opacity: 0;
  transform: translateY(-20px) scale(0.95);
}

/* 编辑表单样式 - 参考菜单管理添加菜单样式 */
.edit-form {
  padding-left: 40px;

  .info-item {
    display: flex;
    align-items: center;
    gap: 4px;
    margin-bottom: 20px;

    .info-label {
      color: #555;
      width: 100px;
      font-weight: 500;
      font-size: 14px;
    }

    /* 输入框、选择框、数字输入器悬浮效果优化 - 与菜单管理保持一致 */
    :deep(.el-input__wrapper),
    :deep(.el-select__wrapper),
    :deep(.el-input-number .el-input__wrapper) {
      border-radius: 8px;
      border: 2px solid #e5e7eb;
      transition: all 0.3s ease;
      box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);

      &:hover {
        border-color: #91d5ff;
        box-shadow: 0 0 0 3px rgba(145, 213, 255, 0.1);
      }

      &.is-focus,
      &.is-focused {
        border-color: #40a9ff;
        box-shadow: 0 0 0 3px rgba(64, 169, 255, 0.15);
      }
    }
  }
}

/* 对话框样式优化 - 与菜单管理添加菜单保持一致 */
:deep(.el-dialog) {
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

:deep(.el-dialog__header) {
  border-bottom: 2px solid rgba(102, 126, 234, 0.3);
  background: linear-gradient(135deg, rgba(230, 247, 255, 0.8) 0%, rgba(186, 231, 255, 0.8) 100%);
  padding: 24px 28px;
  border-radius: 12px 12px 0 0;
}

:deep(.el-dialog__title) {
  font-size: 20px;
  font-weight: 600;
  color: #1890ff;
  background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

:deep(.el-dialog__body) {
  padding: 32px 28px;
  background-color: #fafbfc;
}

:deep(.el-dialog__footer) {
  border-top: 1px solid #f1f3f5;
  padding: 0 28px 24px;
  border-radius: 0 0 12px 12px;
  background-color: #ffffff;
}

/* 对话框按钮样式 - 与菜单管理添加菜单保持一致 */
.dialog-footer {
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
  background: linear-gradient(135deg, #e6f7ff 0%, #bae7ff 100%);
  border: 1px solid #91d5ff;
  color: #0050b3;
}

:deep(.dialog-footer .el-button--primary:hover) {
  background: linear-gradient(135deg, #bae7ff 0%, #91d5ff 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(64, 169, 255, 0.3);
}

:deep(.dialog-footer .el-button--default) {
  border-color: #e5e7eb;
  background-color: #fafafa;
  color: #666;
}

:deep(.dialog-footer .el-button--default:hover) {
  border-color: #d9d9d9;
  background-color: #f0f0f0;
  color: #333;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.merchant-info-card {
  margin-bottom: 24px;
  padding: 24px;
  border: 1px solid #ebeef5;
  border-radius: 16px;
  background-color: #ffffff;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;

  &:hover {
    box-shadow: 0 4px 20px 0 rgba(0, 0, 0, 0.15);
    transform: translateY(-2px);
  }

  .loading-container {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 40px;
    color: #606266;
    font-size: 14px;

    .el-icon {
      margin-right: 8px;
      font-size: 20px;
    }
  }

  .info-content {
    .info-header {
      display: flex;
      align-items: flex-start;
      gap: 24px;
      margin-bottom: 20px;
      padding-bottom: 20px;
      border-bottom: 1px solid #f0f0f0;

      .avatar-section {
        flex-shrink: 0;

        .avatar {
          width: 80px;
          height: 80px;
          border-radius: 50%;
          object-fit: cover;
          border: 3px solid #f0f9ff;
        }

        .avatar-placeholder {
          width: 80px;
          height: 80px;
          border-radius: 50%;
          background-color: #f0f9ff;
          display: flex;
          align-items: center;
          justify-content: center;
        }
      }

      .detail-section {
        flex: 1;

        .merchant-header {
          display: flex;
          align-items: center;
          justify-content: space-between;
          margin-bottom: 12px;

          .merchant-name {
            font-size: 24px;
            font-weight: 700;
            color: #303133;
            display: flex;
            align-items: center;
            gap: 12px;
          }

          .merchant-actions {
            display: flex;
            align-items: center;
            gap: 12px;

            .merchant-status {
              .status-text {
                margin-left: 4px;
              }
            }

            .edit-button {
              display: flex;
              align-items: center;
              gap: 4px;
              background: linear-gradient(135deg, #e6f7ff 0%, #bae7ff 100%);
              border: 1px solid #91d5ff;
              color: #0050b3;
              border-radius: 8px;
              padding: 8px 16px;
              font-weight: 500;
              box-shadow: 0 2px 8px rgba(64, 169, 255, 0.2);
              transition: all 0.3s ease;

              &:hover {
                background: linear-gradient(135deg, #bae7ff 0%, #91d5ff 100%);
                box-shadow: 0 4px 12px rgba(64, 169, 255, 0.3);
                transform: translateY(-1px);
              }
            }
          }
        }

        .merchant-rating {
          margin-bottom: 12px;
        }

        .contact-info {
          display: flex;
          flex-wrap: wrap;
          gap: 20px;

          .contact-item {
            display: flex;
            align-items: center;
            font-size: 14px;
            color: #606266;

            .icon {
              margin-right: 6px;
              color: #409eff;
            }
          }
        }
      }
    }

    .info-details {
      display: flex;
      flex-wrap: wrap;
      gap: 32px;

      .detail-item {
        display: flex;
        align-items: center;
        gap: 8px;

        .detail-label {
          display: flex;
          align-items: center;
          min-width: 100px;
          color: #606266;
          font-size: 14px;
          font-weight: 500;

          .el-icon {
            margin-right: 6px;
            color: #409eff;
          }
        }

        .detail-value {
          color: #303133;
          font-size: 14px;
          font-weight: 400;
        }
      }
    }
  }
}

// 状态标签和评分标签样式优化
.status-tag, .rating-tag {
  cursor: pointer;
  line-height: 28px;
  white-space: nowrap;
  display: inline-flex;
  align-items: center;
  border-radius: 8px;
  padding: 4px 12px;

  :deep(.el-tag__content) {
    white-space: nowrap;
    display: inline-flex;
    align-items: center;
    gap: 4px;
  }

  &:hover {
    opacity: 0.8;
  }
}

// 编辑按钮样式优化
.edit-button {
  background: linear-gradient(135deg, #e6f7ff 0%, #bae7ff 100%);
  border: 1px solid #91d5ff;
  color: #0050b3;
  border-radius: 8px;
  padding: 8px 16px;
  font-weight: 500;
  box-shadow: 0 2px 8px rgba(64, 169, 255, 0.2);
  transition: all 0.3s ease;

  &:hover {
    background: linear-gradient(135deg, #bae7ff 0%, #91d5ff 100%);
    box-shadow: 0 4px 12px rgba(64, 169, 255, 0.3);
    transform: translateY(-1px);
  }
}

// 响应式设计
@media (max-width: 768px) {
  .merchant-info-card {
    padding: 16px;

    .info-header {
      flex-direction: column;
      align-items: center;
      text-align: center;

      .detail-section {
        .merchant-header {
          flex-direction: column;
          gap: 8px;

          .merchant-name {
            font-size: 20px;
          }

          .merchant-actions {
            flex-direction: column;
            gap: 8px;
          }
        }

        .contact-info {
          justify-content: center;
        }
      }
    }

    .info-details {
      flex-direction: column;
      gap: 16px;
    }
  }
}
</style>
