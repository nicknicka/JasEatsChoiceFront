<script setup>
import { ref, onMounted, watch, TransitionGroup } from 'vue'
import dayjs from 'dayjs'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowDown, ArrowUp, Edit, Delete, Download,
  Check, CirclePlus, CircleCheck, CircleClose, InfoFilled
} from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { API_CONFIG } from '../../config/index.js'
// 导入authStore
import { useAuthStore } from '../../store/authStore'

const router = useRouter()
// 菜单状态映射
const menuStatusMap = {
  online: { text: '上架中', type: 'success' },
  draft: { text: '草稿', type: 'warning' },
  offline: { text: '下架中', type: 'danger' }
}

// 菜单数据
const menuList = ref([])

const loading = ref(false)
const searchKeyword = ref('')
const activeStatusFilter = ref('all')

// 筛选菜单
const filteredMenus = ref([])

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)
const paginatedMenus = ref([])

// 排序相关
const sortField = ref('updateTime')
const sortOrder = ref('desc') // 'asc' 或 'desc'

// 更新排序
const updateSorting = () => {
  filteredMenus.value.sort((a, b) => {
    if (a[sortField.value] < b[sortField.value]) {
      return sortOrder.value === 'asc' ? -1 : 1
    }
    if (a[sortField.value] > b[sortField.value]) {
      return sortOrder.value === 'asc' ? 1 : -1
    }
    return 0
  })
}

// 更新分页
const updatePagination = () => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  paginatedMenus.value = filteredMenus.value.slice(start, end)
}

// 页面加载时初始化
onMounted(() => {
  loading.value = true
  // 从authStore获取商家ID
  const authStore = useAuthStore()
  const merchantId = authStore.merchantId
  if (!merchantId) {
    ElMessage.error('未检测到商家ID，请重新登录')
    return
  }
  // 从API获取菜单数据
  axios
    .get(`${API_CONFIG.baseURL}${API_CONFIG.merchant.menu.replace('{merchantId}', merchantId)}`)
    .then((response) => {
      if (response.data && response.data.success) {
        menuList.value = response.data.data
        filteredMenus.value = [...menuList.value] // 更新筛选后的菜单
        updatePagination() // 更新分页
      }
    })
    .catch((error) => {
      console.error('加载菜单失败:', error)
      ElMessage.error('加载菜单失败')
    })
    .finally(() => {
      loading.value = false
    })
})

// 更新筛选
const updateFilter = () => {
  filteredMenus.value = menuList.value.filter((menu) => {
    // 状态筛选
    if (activeStatusFilter.value !== 'all' && menu.status !== activeStatusFilter.value) {
      return false
    }

    // 搜索筛选
    if (searchKeyword.value) {
      const lowerCaseKeyword = searchKeyword.value.toLowerCase()
      const lowerCaseMenuName = menu.name.toLowerCase()
      if (!lowerCaseMenuName.includes(lowerCaseKeyword)) {
        return false
      }
    }

    return true
  })
  // 更新排序
  updateSorting()
  // 更新分页
  currentPage.value = 1 // 筛选后回到第一页
  updatePagination()
}

// 监听filteredMenus变化，确保全选状态正确更新
watch(
  () => filteredMenus.value,
  () => {
    // 如果过滤后的菜单数量减少，且当前选中的菜单数量等于过滤前的数量，那么需要调整选中的菜单
    if (selectedMenus.value.length > filteredMenus.value.length) {
      selectedMenus.value = selectedMenus.value.filter((menu) => filteredMenus.value.includes(menu))
    }
  },
  { deep: true }
)

// 切换状态
const toggleMenuStatus = (menu) => {
  let newStatus = ''

  if (menu.status === 'online') {
    newStatus = 'offline'
  } else if (menu.status === 'offline' || menu.status === 'draft') {
    newStatus = 'online'
  }

  menu.status = newStatus
  updateFilter()
  ElMessage.success(`菜单已${menuStatusMap[newStatus].text}`)
}

// 编辑菜单
const editMenu = (menu) => {
  console.log('编辑菜单:', menu)
  // 导航到菜单编辑页面并传递菜单ID
  router.push({ path: '/merchant/home/menu-edit', query: { menuId: menu.id } })
}

// 删除菜单
const deleteMenu = (menu) => {
  ElMessageBox.confirm('确定要删除该菜单？删除后不可恢复！', '警告', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'danger'
  })
    .then(() => {
      const index = menuList.value.findIndex((item) => item.id === menu.id)
      if (index !== -1) {
        menuList.value.splice(index, 1)
        updateFilter()
        ElMessage.success('菜单已删除')
      }
    })
    .catch(() => {
      ElMessage.info('已取消删除')
    })
}

// 批量操作
const selectedMenus = ref([])

const batchOperation = (operation) => {
  if (selectedMenus.value.length === 0) {
    ElMessage.warning('请先选择菜单')
    return
  }

  switch (operation) {
    case 'online':
      selectedMenus.value.forEach((menu) => {
        menu.status = 'online'
      })
      ElMessage.success('批量上架成功')
      break
    case 'offline':
      selectedMenus.value.forEach((menu) => {
        menu.status = 'offline'
      })
      ElMessage.success('批量下架成功')
      break
    case 'delete':
      ElMessageBox.confirm('确定要删除选中的所有菜单？删除后不可恢复！', '警告', {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'danger'
      })
        .then(() => {
          menuList.value = menuList.value.filter((menu) => !selectedMenus.value.includes(menu))
          selectedMenus.value = []
          updateFilter()
          ElMessage.success('批量删除成功')
        })
        .catch(() => {})
      return
  }

  updateFilter()
  selectedMenus.value = []
}

// 导出菜单为CSV格式
const exportMenu = (menu) => {
  // 构建CSV内容
  const csvContent = [
    // 表头
    ['菜单名称', '状态', '菜品数量', '更新时间', '自动上架时间', '自动下架时间'].join(','),
    // 数据行
    [
      menu.name,
      menuStatusMap[menu.status].text,
      menu.dishes,
      dayjs(menu.updateTime).format('YYYY-MM-DD HH:mm:ss'),
      menu.autoOnline ? dayjs(menu.autoOnline, 'HH:mm:ss').format('HH:mm') : '',
      menu.autoOffline ? dayjs(menu.autoOffline, 'HH:mm:ss').format('HH:mm') : ''
    ].map(item => `"${item}"`).join(',') // 转义包含逗号或引号的内容
  ].join('\n')

  // 创建Blob对象
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })

  // 创建下载链接
  const link = document.createElement('a')
  const url = URL.createObjectURL(blob)
  link.setAttribute('href', url)
  link.setAttribute('download', `${menu.name}_${dayjs().format('YYYYMMDDHHmmss')}.csv`)
  link.style.visibility = 'hidden'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)

  ElMessage.success('菜单导出成功')
}

// 选择/取消选择单个菜单
const toggleMenuSelection = (menu) => {
  const index = selectedMenus.value.findIndex((item) => item.id === menu.id)

  if (index === -1) {
    selectedMenus.value.push(menu)
  } else {
    selectedMenus.value.splice(index, 1)
  }
}

// 新增菜单对话框
const addMenuDialogVisible = ref(false)

// 新菜单表单数据
const newMenu = ref({
  name: '',
  category: 'lunch',
  autoOnline: '',
  autoOffline: '',
  status: 'online'
})

// 打开添加菜单对话框
const openAddMenuDialog = () => {
  addMenuDialogVisible.value = true

  // 重置表单数据
  newMenu.value = {
    name: '',
    category: 'lunch',
    autoOnline: '',
    autoOffline: '',
    status: 'online'
  }
}

// 保存新菜单
const saveNewMenu = () => {
  // 简单的表单验证
  if (!newMenu.value.name.trim()) {
    ElMessage.warning('请填写菜单名称')
    return
  }

  // 从authStore获取商家ID
  const authStore = useAuthStore()
  const merchantId = authStore.merchantId
  if (!merchantId) {
    ElMessage.error('未检测到商家ID，请重新登录')
    router.push('/merchant/login')
    return
  }

  // 准备请求参数
  const menuData = {
    name: newMenu.value.name,
    category: newMenu.value.category,
    status: newMenu.value.status,
    autoOnline: newMenu.value.autoOnline,
    autoOffline: newMenu.value.autoOffline
  }

  // 发送POST请求到后端保存菜单
  axios
    .post(
      `${API_CONFIG.baseURL}${API_CONFIG.merchant.menu.replace('{merchantId}', merchantId)}`,
      menuData
    )
    .then((response) => {
      console.log('保存菜单响应:', response)
      if (response.data && response.data.success) {
        // 从响应中获取完整的菜单对象
        const savedMenu = response.data.data

        // 添加到菜单列表
        menuList.value.push(savedMenu)
        updateFilter()
        addMenuDialogVisible.value = false
        ElMessage.success('菜单已添加')
      }else
      {
        ElMessage.error(`保存菜单失败: ${response.data.message || '未知错误'}`)
      }
    })
    .catch((error) => {
      console.error('保存菜单失败:', error)
      ElMessage.error('保存菜单失败')
    })
}

// 检查全选状态：0=未选择，1=部分选择，2=全选
const getSelectAllState = () => {
  if (selectedMenus.value.length === 0) {
    return 0
  } else if (
    selectedMenus.value.length === filteredMenus.value.length &&
    filteredMenus.value.length > 0
  ) {
    return 2
  } else {
    return 1
  }
}

// 全选/取消全选
const toggleSelectAll = () => {
  const currentState = getSelectAllState()

  if (currentState === 2) {
    // 当前是全选状态，点击后取消全选
    selectedMenus.value = []
  } else {
    // 当前是未选或部分选择状态，点击后全选
    selectedMenus.value = [...filteredMenus.value]
  }

  // 触发Vue的响应式更新
  selectedMenus.value = [...selectedMenus.value]
}
</script>

<template>
  <div class="menu-management-container">
    <div class="menu-header">
      <div class="header-left">
        <h3 class="page-title">【菜单管理】</h3>
      </div>
      <div class="header-right">
        <el-input
          v-model="searchKeyword"
          placeholder="输入菜单名称/关键词搜索..."
          style="min-width: 250px; max-width: 400px; width: auto; flex: 1; max-width: 400px; margin-right: 10px"
          @input="updateFilter"
        />
        <el-button type="primary" @click="openAddMenuDialog">
          <el-icon><CirclePlus /></el-icon>
          新增菜单
        </el-button>
      </div>
    </div>

    <!-- 筛选面板：整合状态筛选和排序 -->
    <div class="filter-panel">
      <div class="menu-filters">
        <div class="filter-section">
          <span class="filter-label">状态筛选：</span>
          <el-tag
            v-for="status in ['all', 'online', 'draft', 'offline']"
            :key="status"
            :type="activeStatusFilter === status ? 'primary' : 'info'"
            effect="plain"
            @click="
              () => {
                activeStatusFilter = status
                updateFilter()
              }
            "
            class="status-filter"
          >
            <el-icon v-if="status !== 'all' && status === 'online'"><CircleCheck /></el-icon> 
            <el-icon v-if="status !== 'all' && status === 'draft'"><CirclePlus /></el-icon>
            <el-icon v-if="status !== 'all' && status === 'offline'"><CircleClose /></el-icon>
            {{ status === 'all' ? '全部菜单' : menuStatusMap[status].text }}
          </el-tag>
        </div>

        <div class="filter-section">
          <span class="filter-label">排序：</span>
          <el-select v-model="sortField" style="width: 120px; margin-right: 8px" @change="updateFilter">
            <el-option label="更新时间" value="updateTime" />
            <el-option label="菜单名称" value="name" />
          </el-select>

          <el-select v-model="sortOrder" style="width: 100px" @change="updateFilter">
            <el-option label="升序" value="asc" />
            <el-option label="降序" value="desc" />
          </el-select>
          <el-tooltip content="按所选字段升序或降序排列菜单" placement="top">
            <el-icon style="color: #909399; margin-left: 4px; cursor: help"><InfoFilled /></el-icon>
          </el-tooltip>
        </div>
      </div>
    </div>

    <!-- 将批量操作和分页放在同一个容器中 -->
    <div class="batch-pagination-container" v-if="filteredMenus.length > 0">
      <div class="batch-actions">
        <span class="select-all">
          <el-checkbox
            :indeterminate="getSelectAllState() === 1"
            :model-value="getSelectAllState() === 2"
            @change="toggleSelectAll"
          />
          全选
          <span class="selected-count" :style="{ visibility: selectedMenus.length > 0 ? 'visible' : 'hidden' }">
            ({{ selectedMenus.length }}/{{ filteredMenus.length }})
          </span>
        </span>

        <el-button
          type="success"
          size="small"
          @click="batchOperation('online')"
          :disabled="selectedMenus.length === 0"
        >
          <el-icon><CircleCheck /></el-icon>
          批量上架
        </el-button>

        <el-button
          type="warning"
          size="small"
          @click="batchOperation('offline')"
          :disabled="selectedMenus.length === 0"
        >
          <el-icon><CircleClose /></el-icon>
          批量下架
        </el-button>

        <el-button
          type="danger"
          size="small"
          @click="batchOperation('delete')"
          :disabled="selectedMenus.length === 0"
        >
          <el-icon><Delete /></el-icon>
          批量删除
        </el-button>
      </div>

      <!-- 分页 -->
      <div class="menu-pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="filteredMenus.length"
          @size-change="updatePagination"
          @current-change="updatePagination"
        />
      </div>
    </div>

    <div class="menu-list">
      <TransitionGroup
        name="list"
        tag="div"
      >
        <div class="menu-item" v-for="menu in paginatedMenus" :key="menu.id">
        <div class="menu-selection">
          <el-checkbox
            :model-value="selectedMenus.includes(menu)"
            @change="toggleMenuSelection(menu)"
          />
        </div>

        <div class="menu-content">
          <div class="menu-info">
            <div class="menu-name">
              <span class="name">{{ menu.name }}</span>
              <el-tag :type="menuStatusMap[menu.status].type">
                <el-icon v-if="menu.status === 'online'"><CircleCheck /></el-icon>
                <el-icon v-if="menu.status === 'draft'"><CirclePlus /></el-icon>
                <el-icon v-if="menu.status === 'offline'"><CircleClose /></el-icon>
                {{ menuStatusMap[menu.status].text }}
              </el-tag>
            </div>

            <div class="menu-stats">
              <span class="dishes-count">🍴 {{ menu.dishes }} 菜品</span>
              <span class="update-time">⏰ 更新时间：{{ dayjs(menu.updateTime).format('YYYY-MM-DD HH:mm:ss') }}</span>
            </div>

            <div class="auto-times">
              <span v-if="menu.autoOnline" class="auto-online">
                ⏰ 自动上架：{{ dayjs(menu.autoOnline, 'HH:mm:ss').format('HH:mm') }}
              </span>
              <span v-if="menu.autoOffline" class="auto-offline">
                ⏰ 自动下架：{{ dayjs(menu.autoOffline, 'HH:mm:ss').format('HH:mm') }}
              </span>
            </div>
          </div>

          <div class="menu-actions">
            <el-button
              :type="menu.status === 'online' ? 'warning' : 'success'"
              size="small"
              @click="toggleMenuStatus(menu)"
            >
              <el-icon v-if="menu.status === 'online'"><CircleClose /></el-icon>
              <el-icon v-else><CircleCheck /></el-icon>
              {{ menu.status === 'online' ? '下架菜单' : '上架菜单' }}
            </el-button>

            <el-button type="primary" size="small" @click="editMenu(menu)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>

            <el-button type="danger" size="small" @click="deleteMenu(menu)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>

            <el-button type="info" size="small" @click="exportMenu(menu)">
              <el-icon><Download /></el-icon>
              导出菜单
            </el-button>
          </div>
        </div>
      </div>
    </TransitionGroup>
    </div>

    <!-- 空数据提示 -->
    <el-empty v-if="filteredMenus.length === 0" description="您还没有创建任何菜单~">
      <template #bottom>
        <el-button type="primary" size="small" @click="addMenuDialogVisible = true">新增菜单</el-button>
      </template>
    </el-empty>

    <!-- 添加菜单对话框 -->
    <el-dialog v-model="addMenuDialogVisible" title="添加新菜单" width="600px" top="10%">
      <el-form :model="newMenu" label-width="100px" status-icon>
        <el-form-item label="名称" prop="name" required>
          <el-input v-model="newMenu.name" placeholder="请输入菜单名称" />
        </el-form-item>

        <el-form-item label="分类" prop="category" required>
          <el-select v-model="newMenu.category" style="width: 100%">
            <el-option label="早餐" value="breakfast" />
            <el-option label="午餐" value="lunch" />
            <el-option label="晚餐" value="dinner" />
            <el-option label="夜宵" value="late-night" />
          </el-select>
        </el-form-item>

        <el-form-item label="自动上架时间">
          <el-time-picker
            v-model="newMenu.autoOnline"
            type="fixed-time"
            format="HH:mm:ss"
            value-format="HH:mm:ss"
            placeholder="选择自动上架时间"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="自动下架时间">
          <el-time-picker
            v-model="newMenu.autoOffline"
            type="fixed-time"
            format="HH:mm:ss"
            value-format="HH:mm:ss"
            placeholder="选择自动下架时间"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="状态">
          <el-select v-model="newMenu.status" style="width: 100%">
            <el-option label="上架中" value="online" />
            <el-option label="草稿" value="draft" />
            <el-option label="下架中" value="offline" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addMenuDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveNewMenu">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="less">
.menu-management-container {
  padding: 24px;

  .menu-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;

    .page-title {
      font-size: 18px;
      font-weight: 600;
      margin: 0;
    }

    // 固定搜索和新增按钮区域的宽度范围，确保布局稳定
    .header-right {
      width: 55%; /* 占父容器55%宽度 */
      max-width: 650px; /* 最大宽度限制 */
      min-width: 450px; /* 最小宽度限制 */
      display: flex;
      align-items: center;
      gap: 10px; /* 统一内部元素间距 */
    }

    /* 小屏幕响应式调整 */
    @media (max-width: 767px) {
      flex-direction: column;
      gap: 16px;
      align-items: stretch;

      .header-right {
        width: 100% !important; /* 小屏幕下占满宽度 */
        min-width: auto !important; /* 取消最小宽度限制 */
        max-width: none !important; /* 取消最大宽度限制 */
        flex-direction: column;
        gap: 12px;
      }

      .el-input {
        min-width: auto;
        max-width: none;
      }
    }
  }

  .filter-panel {
    background-color: #f5f7fa;
    padding: 16px 24px;
    border-radius: 8px;
    margin-bottom: 24px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  }

  .menu-filters {
    display: flex;
    flex-direction: column; /* 改为纵向排列，将状态筛选和排序分成两行 */
    gap: 16px; /* 两行之间的间距 */
    align-items: flex-start; /* 左对齐 */

    .filter-section {
      display: flex;
      align-items: center;
      gap: 12px;
      white-space: nowrap; /* 防止筛选标签换行 */

      .filter-label {
        font-weight: 500;
        white-space: nowrap; /* 确保标签文本不换行 */
        margin-right: 8px;
      }

      .status-filter {
        cursor: pointer;
        line-height: 28px; /* 增加行高确保图标和文字居中对齐 */
        white-space: nowrap !important; /* 强制标签内所有内容在同一行显示 */
        &:hover {
          opacity: 0.8;
        }
        // 确保标签内部元素也不换行
        :deep(.el-tag__content) {
          white-space: nowrap;
          display: inline-flex;
          align-items: center;
        }
      }
    }
  }

  .menu-list {
    margin-bottom: 30px;

    /* List transition animations */
    .list-enter-active,
    .list-leave-active {
      transition: all 0.3s ease;
    }

    .list-enter-from,
    .list-leave-to {
      opacity: 0;
      transform: translateY(10px);
    }

    .menu-item {
      display: flex;
      align-items: flex-start;
      padding: 20px;
      border: 1px solid #e4e7ed;
      border-radius: 8px;
      margin-bottom: 16px;
      background-color: #fff;
      transition: all 0.3s;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);

      &:hover {
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        border-color: #c9e6ff;
      }

      .menu-selection {
        margin-top: 4px;
        margin-right: 16px;
      }

      .menu-content {
        flex: 1;
        display: flex;
        flex-direction: column;

        .menu-info {
          .menu-name {
            display: flex;
            align-items: center;
            gap: 10px;
            margin-bottom: 12px;

            .name {
              font-size: 18px;
              font-weight: 600;
              color: #303133;
            }
          }

          .menu-stats {
            display: flex;
            flex-wrap: wrap;
            gap: 24px;
            margin-bottom: 8px;
            font-size: 14px;

            .dishes-count {
              color: #606266;
            }
          }

          .auto-times {
            font-size: 13px;
            color: #909399;
            margin-bottom: 12px;
          }
        }

        .menu-actions {
          display: flex;
          gap: 12px;
          justify-content: flex-end;
          flex-wrap: wrap;
          margin-top: 12px;

          button {
            width: auto;
            padding: 4px 12px;
          }
        }
      }
    }
  }

  .batch-actions {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 20px;

    .select-all {
      display: flex;
      align-items: center;
      gap: 8px;
      font-weight: 500;
      white-space: nowrap; /* 防止全选文本换行 */
      min-width: 100px; /* 设置固定最小宽度，让批量操作按钮位置稳定 */

      .selected-count {
        font-size: 13px;
        font-weight: 400;
        color: #909399;
      }
    }
  }

  .menu-pagination {
    text-align: center;
  }

  /* 筛选面板小屏幕响应式调整 */
  @media (max-width: 767px) {
    .filter-panel {
      padding: 12px 16px;
      overflow-x: auto; /* 小屏幕下允许横向滚动 */
    }

    .menu-filters {
      gap: 12px;
    }

    .filter-section {
      gap: 8px;
    }

    .filter-label {
      white-space: nowrap; /* 确保标签在小屏幕也不换行 */
    }
  }

  /* 将批量操作和分页分成两行显示 */
  .batch-pagination-container {
    margin-bottom: 20px;

    .batch-actions {
      justify-content: flex-start; /* 批量操作左对齐 */
    }

    .menu-pagination {
      display: flex;
      justify-content: center;
      align-items: center;
      margin-top: 12px;
    }
  }

  .dialog-footer {
    text-align: right;
  }
}
</style>
