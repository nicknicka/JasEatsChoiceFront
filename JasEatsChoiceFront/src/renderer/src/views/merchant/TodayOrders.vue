<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  View,
  CircleCheck,
  CircleCheckFilled,
  Goods,
  Dish,
  Select,
  MoreFilled,
  User,
  CircleClose,
  Delete,
  Search,
  Refresh,
  Reading,
  Filter,
  List
} from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import CommonBackButton from '../../components/common/CommonBackButton.vue'

const router = useRouter()
const loading = ref(false)

// 菜品列表展开状态
const expandedItems = ref(new Set())

// 用户信息展开状态
const expandedUserInfo = ref(new Set())

// 切换用户信息展开状态
const toggleUserInfoExpand = (orderId) => {
  if (expandedUserInfo.value.has(orderId)) {
    expandedUserInfo.value.delete(orderId)
  } else {
    expandedUserInfo.value.add(orderId)
  }
}

// 数字动画
const animatedValues = ref({
  total: 0,
  totalAmount: 0,
  pendingCount: 0,
  preparingCount: 0,
  completedCount: 0
})

// 动画数字
const animateValue = (key, endValue, duration = 1000) => {
  const startValue = animatedValues.value[key]
  const startTime = performance.now()

  const animate = (currentTime) => {
    const elapsed = currentTime - startTime
    const progress = Math.min(elapsed / duration, 1)

    // 使用 easeOutQuart 缓动函数
    const easeOutQuart = 1 - Math.pow(1 - progress, 4)
    animatedValues.value[key] = Math.floor(startValue + (endValue - startValue) * easeOutQuart)

    if (progress < 1) {
      requestAnimationFrame(animate)
    } else {
      animatedValues.value[key] = endValue
    }
  }

  requestAnimationFrame(animate)
}

// 切换菜品列表展开状态
const toggleItemsExpand = (orderId) => {
  if (expandedItems.value.has(orderId)) {
    expandedItems.value.delete(orderId)
  } else {
    expandedItems.value.add(orderId)
  }
}

// 获取相对时间显示
const getRelativeTime = (timeStr) => {
  const orderTime = new Date(timeStr)
  const now = new Date()
  const diff = now - orderTime
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  return timeStr.split(' ')[1] // 显示具体时间
}

// 获取订单卡片样式类
const getOrderCardClass = (order) => {
  const classes = []
  if (order.unread) classes.push('unread-order')
  if (order.priority === 'high') classes.push('urgent-order')
  classes.push(`status-${order.status}`)
  return classes.join(' ')
}

// 订单状态映射（对应后端状态码）
// 0-待支付、1-待接单、2-备菜中、3-烹饪中、4-待上菜、5-已完成、6-已取消
const orderStatusMap = {
  0: { text: '待支付', type: 'info', color: '#909399' },
  1: { text: '待接单', type: 'danger', color: '#f56c6c' },
  2: { text: '备菜中', type: 'warning', color: '#e6a23c' },
  3: { text: '烹饪中', type: 'warning', color: '#ff9800' },
  4: { text: '待上菜', type: 'primary', color: '#409eff' },
  5: { text: '已完成', type: 'success', color: '#67c23a' },
  6: { text: '已取消', type: 'info', color: '#c0c4cc' }
}

// 状态筛选映射（用于筛选标签）
const statusFilterMap = {
  all: { text: '全部', value: 'all' },
  1: { text: '待接单', value: 1 },
  2: { text: '备菜中', value: 2 },
  3: { text: '烹饪中', value: 3 },
  4: { text: '待上菜', value: 4 },
  5: { text: '已完成', value: 5 }
}

// 生成今天的模拟订单数据（匹配后端实体结构）
const generateTodayOrders = () => {
  const today = new Date()
  const year = today.getFullYear()
  const month = String(today.getMonth() + 1).padStart(2, '0')
  const day = String(today.getDate()).padStart(2, '0')
  const dateStr = `${year}-${month}-${day}`
  const orderNumSuffix = `${year}${month}${day}`

  return [
    {
      id: '1',
      orderNo: `JD${orderNumSuffix}001`,
      status: 1, // 待接单
      userId: 'u001',
      user: '小明',
      phone: '138****8888',
      address: '科技园A栋12楼',
      totalAmount: 78.0,
      createTime: `${dateStr} 10:30`,
      updateTime: `${dateStr} 10:30`,
      // 订单菜品（对应OrderDish实体）
      orderDishes: [
        { dishId: 'd001', dishName: '宫保鸡丁', quantity: 1, price: 38.0, customization: '' },
        { dishId: 'd002', dishName: '米饭', quantity: 2, price: 2.0, customization: '' }
      ],
      remark: '少辣',
      unread: true,
      priority: 'high'
    },
    {
      id: '2',
      orderNo: `JD${orderNumSuffix}002`,
      status: 2, // 备菜中
      userId: 'u002',
      user: '小红',
      phone: '139****9999',
      address: '幸福小区3号楼',
      totalAmount: 45.0,
      createTime: `${dateStr} 10:35`,
      updateTime: `${dateStr} 10:40`,
      orderDishes: [
        { dishId: 'd003', dishName: '鱼香肉丝', quantity: 1, price: 28.0, customization: '' },
        { dishId: 'd004', dishName: '麻婆豆腐', quantity: 1, price: 17.0, customization: '' }
      ],
      remark: '',
      unread: false,
      priority: 'normal'
    },
    {
      id: '3',
      orderNo: `JD${orderNumSuffix}003`,
      status: 5, // 已完成
      userId: 'u003',
      user: '小刚',
      phone: '137****7777',
      address: '大学城南区5栋',
      totalAmount: 62.0,
      createTime: `${dateStr} 10:40`,
      updateTime: `${dateStr} 11:20`,
      orderDishes: [
        { dishId: 'd005', dishName: '回锅肉', quantity: 1, price: 32.0, customization: '' },
        { dishId: 'd006', dishName: '青菜', quantity: 2, price: 8.0, customization: '' },
        { dishId: 'd002', dishName: '米饭', quantity: 3, price: 2.0, customization: '' }
      ],
      remark: '多加饭',
      unread: false,
      priority: 'normal'
    },
    {
      id: '4',
      orderNo: `JD${orderNumSuffix}004`,
      status: 1, // 待接单
      userId: 'u004',
      user: '小李',
      phone: '136****6666',
      address: '万达广场写字楼B座',
      totalAmount: 128.0,
      createTime: `${dateStr} 11:00`,
      updateTime: `${dateStr} 11:00`,
      orderDishes: [
        { dishId: 'd007', dishName: '水煮鱼', quantity: 1, price: 68.0, customization: '微辣' },
        { dishId: 'd008', dishName: '夫妻肺片', quantity: 1, price: 38.0, customization: '' },
        { dishId: 'd002', dishName: '米饭', quantity: 4, price: 2.0, customization: '' }
      ],
      remark: '尽快送达',
      unread: true,
      priority: 'high'
    },
    {
      id: '5',
      orderNo: `JD${orderNumSuffix}005`,
      status: 3, // 烹饪中
      userId: 'u005',
      user: '小王',
      phone: '135****5555',
      address: '中心公园附近',
      totalAmount: 96.0,
      createTime: `${dateStr} 11:15`,
      updateTime: `${dateStr} 11:30`,
      orderDishes: [
        { dishId: 'd009', dishName: '糖醋排骨', quantity: 1, price: 48.0, customization: '' },
        { dishId: 'd010', dishName: '西红柿炒蛋', quantity: 1, price: 18.0, customization: '' }
      ],
      remark: '',
      unread: false,
      priority: 'normal'
    },
    {
      id: '6',
      orderNo: `JD${orderNumSuffix}006`,
      status: 1, // 待接单
      userId: 'u006',
      user: '小张',
      phone: '134****4444',
      address: '第一人民医院',
      totalAmount: 58.5,
      createTime: `${dateStr} 11:30`,
      updateTime: `${dateStr} 11:30`,
      orderDishes: [
        { dishId: 'd011', dishName: '酸辣土豆丝', quantity: 1, price: 18.0, customization: '' },
        { dishId: 'd012', dishName: '青椒肉丝', quantity: 1, price: 32.5, customization: '少油' }
      ],
      remark: '清淡口味',
      unread: true,
      priority: 'high'
    },
    {
      id: '7',
      orderNo: `JD${orderNumSuffix}007`,
      status: 5, // 已完成
      userId: 'u007',
      user: '小赵',
      phone: '133****3333',
      address: '火车站北广场',
      totalAmount: 156.0,
      createTime: `${dateStr} 10:25`,
      updateTime: `${dateStr} 11:10`,
      orderDishes: [
        { dishId: 'd013', dishName: '毛血旺', quantity: 1, price: 68.0, customization: '' },
        { dishId: 'd014', dishName: '蒜蓉西兰花', quantity: 2, price: 16.0, customization: '' },
        { dishId: 'd002', dishName: '米饭', quantity: 5, price: 2.0, customization: '' }
      ],
      remark: '不要香菜',
      unread: false,
      priority: 'normal'
    }
  ]
}

// 全部订单数据
const orders = ref([])

// 当前选中的状态筛选（使用数字状态码）
const activeStatusFilter = ref('all')

// 搜索关键词
const searchKeyword = ref('')

// 筛选后的订单（改为computed自动计算）
const filteredOrders = computed(() => {
  return orders.value
    .filter((order) => {
      // 状态筛选（支持数字状态码）
      const statusMatch =
        activeStatusFilter.value === 'all' || order.status === activeStatusFilter.value

      // 搜索筛选
      const searchMatch =
        !searchKeyword.value ||
        order.orderNo.toLowerCase().includes(searchKeyword.value.toLowerCase()) ||
        order.user.includes(searchKeyword.value) ||
        order.phone.includes(searchKeyword.value)

      return statusMatch && searchMatch
    })
    // 按创建时间倒序排序
    .sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
})

// 获取今天的日期（格式：YYYY-MM-DD）
const today = computed(() => {
  const date = new Date()
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
})

// 订单概览统计
const orderOverview = computed(() => {
  const total = filteredOrders.value.length
  const totalAmount = filteredOrders.value.reduce((sum, order) => sum + order.totalAmount, 0)
  // 使用数字状态码统计：1-待接单、2-备菜中、3-烹饪中、4-待上菜、5-已完成
  const pendingCount = filteredOrders.value.filter((order) => order.status === 1).length
  const preparingCount = filteredOrders.value.filter((order) => order.status === 2 || order.status === 3).length
  const cookingCount = filteredOrders.value.filter((order) => order.status === 3).length
  const readyCount = filteredOrders.value.filter((order) => order.status === 4).length
  const completedCount = filteredOrders.value.filter((order) => order.status === 5).length
  const unreadCount = filteredOrders.value.filter((order) => order.unread).length

  return {
    total,
    totalAmount,
    pendingCount,
    preparingCount,
    cookingCount,
    readyCount,
    completedCount,
    unreadCount
  }
})

// 监听订单概览变化，触发动画
watch(orderOverview, (newVal) => {
  animateValue('total', newVal.total)
  animateValue('totalAmount', newVal.totalAmount)
  animateValue('pendingCount', newVal.pendingCount)
  animateValue('preparingCount', newVal.preparingCount)
  animateValue('completedCount', newVal.completedCount)
}, { deep: true })

// 是否有数据
const hasOrders = computed(() => orders.value.length > 0)

// 刷新订单数据
const refreshOrders = () => {
  loading.value = true
  setTimeout(() => {
    orders.value = generateTodayOrders()
    loading.value = false
  }, 300)
}

// 查看订单详情
const viewOrderDetails = (order) => {
  // 标记为已读
  if (order.unread) {
    order.unread = false
    ElMessage.success('订单已标记为已读')
  }

  // 导航到订单详情页
  router.push(`/merchant/home/order-detail/${order.id}`)
}

// 更新订单状态
const updateOrderStatus = (order, newStatus) => {
  order.status = newStatus
  // 同时更新 updateTime
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  const hours = String(now.getHours()).padStart(2, '0')
  const minutes = String(now.getMinutes()).padStart(2, '0')
  order.updateTime = `${year}-${month}-${day} ${hours}:${minutes}`
  ElMessage.success(`订单状态已更新为${orderStatusMap[newStatus].text}`)
}

// 取消订单
const cancelOrder = (order) => {
  ElMessageBox.confirm('确定要取消此订单吗？取消后将影响商家信誉。', '取消订单确认', {
    confirmButtonText: '确定取消',
    cancelButtonText: '再想想',
    type: 'warning',
    distinguishCancelAndClose: true
  })
    .then(() => {
      updateOrderStatus(order, 'cancelled')
      ElMessage.warning('订单已取消')
    })
    .catch(() => {})
}

// 删除订单
const deleteOrder = (order) => {
  ElMessageBox.confirm('删除后订单将无法恢复，确定要删除吗？', '删除订单确认', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'error',
    distinguishCancelAndClose: true
  })
    .then(() => {
      const index = orders.value.findIndex((item) => item.id === order.id)
      if (index !== -1) {
        orders.value.splice(index, 1)
        ElMessage.success('订单已删除')
      }
    })
    .catch(() => {})
}

// 批量操作：标记所有为已读
const markAllAsRead = () => {
  let count = 0
  filteredOrders.value.forEach((order) => {
    if (order.unread) {
      order.unread = false
      count++
    }
  })
  if (count > 0) {
    ElMessage.success(`已将 ${count} 个订单标记为已读`)
  } else {
    ElMessage.info('没有未读订单')
  }
}

// 获取标签类型
const getTagType = (status) => {
  if (status === 'all') return 'primary'
  return orderStatusMap[status]?.type || 'info'
}

// 获取状态标签文本（显示全部订单的数量，不受搜索影响）
const getStatusLabel = (status) => {
  if (status === 'all') return `全部 (${orders.value.length})`
  const count = orders.value.filter(o => o.status === status).length
  return `${orderStatusMap[status].text} (${count})`
}

// 获取状态的订单数量
const getStatusCount = (status) => {
  if (status === 'all') return orders.value.length
  return orders.value.filter(o => o.status === status).length
}

// 获取空状态描述
const getEmptyDescription = () => {
  if (searchKeyword.value) {
    return '未找到匹配的订单'
  }
  if (activeStatusFilter.value === 1) {
    return '暂无待接单订单'
  }
  if (activeStatusFilter.value === 2) {
    return '暂无备菜中订单'
  }
  if (activeStatusFilter.value === 3) {
    return '暂无烹饪中订单'
  }
  if (activeStatusFilter.value === 4) {
    return '暂无待上菜订单'
  }
  if (activeStatusFilter.value === 5) {
    return '暂无已完成订单'
  }
  return '今日暂无订单'
}

// 页面加载时初始化
onMounted(() => {
  refreshOrders()
})</script>

<template>
  <div class="merchant-orders-container">
    <!-- 头部 -->
    <div class="orders-header">
      <div class="header-left">
        <h3 class="page-title">今日订单</h3>
        <span class="current-date">{{ today }}</span>
      </div>
      <div class="header-right">
        <el-button
          v-if="orderOverview.unreadCount > 0"
          type="primary"
          size="small"
          @click="markAllAsRead"
        >
          全部已读 ({{ orderOverview.unreadCount }})
        </el-button>
        <el-button size="small" :loading="loading" @click="refreshOrders">
          刷新
        </el-button>
        <common-back-button type="default" />
      </div>
    </div>

    <!-- 订单统计卡片 -->
    <div class="overview-section">
      <div class="stat-card total">
        <div class="stat-icon">
          <svg viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg"><path d="M912 160H128c-35.3 0-64 28.7-64 64v576c0 35.3 28.7 64 64 64h784c35.3 0 64-28.7 64-64V224c0-35.3-28.7-64-64-64z m-56 464H472c-4.4 0-8 3.6-8 8v56c0 4.4 3.6 8 8 8h384c4.4 0 8-3.6 8-8v-56c0-4.4-3.6-8-8-8z m0-192H472c-4.4 0-8 3.6-8 8v56c0 4.4 3.6 8 8 8h384c4.4 0 8-3.6 8-8v-56c0-4.4-3.6-8-8-8z m0-192H472c-4.4 0-8 3.6-8 8v56c0 4.4 3.6 8 8 8h384c4.4 0 8-3.6 8-8v-56c0-4.4-3.6-8-8-8z M168 624h200c4.4 0 8-3.6 8-8v-56c0-4.4-3.6-8-8-8H168c-4.4 0-8 3.6-8 8v56c0 4.4 3.6 8 8 8z m0-192h200c4.4 0 8-3.6 8-8v-56c0-4.4-3.6-8-8-8H168c-4.4 0-8 3.6-8 8v56c0 4.4 3.6 8 8 8z m0-192h200c4.4 0 8-3.6 8-8v-56c0-4.4-3.6-8-8-8H168c-4.4 0-8 3.6-8 8v56c0 4.4 3.6 8 8 8z" fill="currentColor"/></svg>
        </div>
        <div class="stat-content">
          <div class="stat-value animated-number">{{ animatedValues.total }}</div>
          <div class="stat-label">总订单</div>
        </div>
      </div>

      <div class="stat-card amount highlight">
        <div class="stat-icon">
          <svg viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg"><path d="M512 64C264.6 64 64 264.6 64 512s200.6 448 448 448 448-200.6 448-448S759.4 64 512 64z m0 820c-205.4 0-372-166.6-372-372s166.6-372 372-372 372 166.6 372 372-166.6 372-372 372z m151.2-500.2L534.6 536.2c-3.1 3.1-8.2 3.1-11.3 0l-109-109c-3.1-3.1-3.1-8.2 0-11.3l36.4-36.4c3.1-3.1 8.2-3.1 11.3 0l82 82 214.6-214.6c3.1-3.1 8.2-3.1 11.3 0l36.4 36.4c3.1 3.1 3.1 8.2 0 11.3z" fill="currentColor"/></svg>
        </div>
        <div class="stat-content">
          <div class="stat-value animated-number">¥{{ animatedValues.totalAmount.toFixed(0) }}</div>
          <div class="stat-label">总金额</div>
        </div>
      </div>

      <div class="stat-card pending">
        <div class="stat-icon">
          <svg viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg"><path d="M512 64C264.6 64 64 264.6 64 512s200.6 448 448 448 448-200.6 448-448S759.4 64 512 64z m0 820c-205.4 0-372-166.6-372-372s166.6-372 372-372 372 166.6 372 372-166.6 372-372 372z" fill="currentColor"/></svg>
        </div>
        <div class="stat-content">
          <div class="stat-value animated-number">{{ animatedValues.pendingCount }}</div>
          <div class="stat-label">待处理</div>
        </div>
      </div>

      <div class="stat-card preparing">
        <div class="stat-icon">
          <svg viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg"><path d="M512 64C264.6 64 64 264.6 64 512s200.6 448 448 448 448-200.6 448-448S759.4 64 512 64z m193.5 301.7l-210.6 292c-12.7 17.7-39 17.7-51.7 0L318.5 484.9c-3.8-5.3 0-12.7 6.5-12.7h46.9c10.2 0 19.9 4.9 25.9 13.3l71.2 98.8 157.2-218c6-8.3 15.6-13.3 25.9-13.3H699c6.5 0 10.3 7.4 6.5 12.7z" fill="currentColor"/></svg>
        </div>
        <div class="stat-content">
          <div class="stat-value animated-number">{{ animatedValues.preparingCount }}</div>
          <div class="stat-label">准备中</div>
        </div>
      </div>

      <div class="stat-card completed">
        <div class="stat-icon">
          <svg viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg"><path d="M512 64C264.6 64 64 264.6 64 512s200.6 448 448 448 448-200.6 448-448S759.4 64 512 64z m193.5 301.7l-210.6 292c-12.7 17.7-39 17.7-51.7 0L318.5 484.9c-3.8-5.3 0-12.7 6.5-12.7h46.9c10.2 0 19.9 4.9 25.9 13.3l71.2 98.8 157.2-218c6-8.3 15.6-13.3 25.9-13.3H699c6.5 0 10.3 7.4 6.5 12.7z" fill="currentColor"/></svg>
        </div>
        <div class="stat-content">
          <div class="stat-value animated-number">{{ animatedValues.completedCount }}</div>
          <div class="stat-label">已完成</div>
        </div>
      </div>
    </div>

    <!-- 筛选和搜索 -->
    <div class="filter-section">
      <div class="filter-left">
        <div class="filter-header">
          <el-icon class="filter-icon"><Filter /></el-icon>
          <span class="filter-label">状态筛选</span>
        </div>
        <div class="status-filter-group">
          <div
            v-for="status in ['all', 1, 2, 3, 4, 5]"
            :key="status"
            :class="['custom-status-tag', `status-tag-${status}`, { 'active': activeStatusFilter === status, 'zero-count': getStatusCount(status) === 0 }]"
            @click="activeStatusFilter = status"
          >
            <template v-if="status === 'all'">
              <el-icon class="tag-icon"><List /></el-icon>
            </template>
            <template v-else-if="status === 1">
              <el-icon class="tag-icon"><CircleClose /></el-icon>
            </template>
            <template v-else-if="status === 2 || status === 3">
              <el-icon class="tag-icon"><Goods /></el-icon>
            </template>
            <template v-else-if="status === 4">
              <el-icon class="tag-icon"><Dish /></el-icon>
            </template>
            <template v-else-if="status === 5">
              <el-icon class="tag-icon"><CircleCheckFilled /></el-icon>
            </template>
            <span class="tag-text">{{ getStatusLabel(status) }}</span>
            <el-icon v-if="activeStatusFilter === status && status !== 'all'" class="close-icon" @click.stop="activeStatusFilter = 'all'">
              <CircleClose />
            </el-icon>
          </div>
        </div>
      </div>

      <div class="filter-right">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索订单号/用户/电话"
          class="search-input"
          clearable
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
    </div>

    <!-- 快捷操作栏 -->
    <div class="quick-actions">
      <div class="quick-actions-left">
        <el-button
          v-if="orderOverview.unreadCount > 0"
          type="warning"
          size="small"
          @click="markAllAsRead"
          class="quick-action-btn"
        >
          <el-icon><Reading /></el-icon>
          <span>全部已读</span>
        </el-button>
        <el-button
          size="small"
          @click="refreshOrders"
          :loading="loading"
          class="quick-action-btn"
        >
          <el-icon><Refresh /></el-icon>
          <span>刷新</span>
        </el-button>
      </div>
      <div class="quick-actions-right">
        <span class="order-count-info">
          共 <strong>{{ filteredOrders.length }}</strong> 个订单
        </span>
      </div>
    </div>

    <!-- 订单列表 -->
    <div v-loading="loading" class="orders-list-section">
      <transition-group name="list" tag="div">
        <div v-for="order in filteredOrders" :key="order.id" :class="['order-item', getOrderCardClass(order)]">
          <div class="order-main">
            <div class="order-content">
              <div class="order-left">
                <div class="order-basic-info">
                  <div class="order-no">
                    <span class="no-label">订单号</span>
                    <span class="no-value">{{ order.orderNo }}</span>
                    <el-tag
                      v-if="order.priority === 'high'"
                      size="small"
                      type="danger"
                      effect="dark"
                      class="priority-tag"
                    >
                      加急
                    </el-tag>
                  </div>
                  <div class="order-amount">
                    <span class="amount-label">金额</span>
                    <span class="amount-value">¥{{ order.totalAmount.toFixed(2) }}</span>
                  </div>
                  <div class="order-time" :title="order.createTime">
                    <span class="time-label">时间</span>
                    <span class="time-value">{{ getRelativeTime(order.createTime) }}</span>
                  </div>
                </div>

                <!-- 可折叠的用户信息 -->
                <transition name="slide-fade">
                  <div v-show="expandedUserInfo.has(order.id)" class="order-user-info">
                    <div class="user-name">
                      <span class="info-label">用户</span>
                      <span class="info-value">{{ order.user }}</span>
                    </div>
                    <div class="user-phone">
                      <span class="info-label">电话</span>
                      <span class="info-value">{{ order.phone }}</span>
                    </div>
                    <div class="user-address">
                      <span class="info-label">地址</span>
                      <span class="info-value">{{ order.address }}</span>
                    </div>
                  </div>
                </transition>

                <!-- 订单备注（始终显示） -->
                <div v-if="order.remark" class="order-remark">
                  <div class="remark-icon">📝</div>
                  <div class="remark-content">
                    <span class="remark-label">备注：</span>
                    <span class="remark-text">{{ order.remark }}</span>
                  </div>
                </div>

                <div class="order-items">
                  <div class="items-label">菜品</div>
                  <div class="items-list">
                    <template v-if="order.orderDishes.length > 3 && !expandedItems.has(order.id)">
                      <el-tag
                        v-for="(dish, index) in order.orderDishes.slice(0, 3)"
                        :key="index"
                        size="small"
                        type="info"
                        effect="plain"
                        class="item-tag"
                      >
                        {{ dish.dishName }}x{{ dish.quantity }}
                        <span v-if="dish.customization" class="customization">({{ dish.customization }})</span>
                      </el-tag>
                      <el-tag
                        size="small"
                        class="more-tag"
                        @click="toggleItemsExpand(order.id)"
                      >
                        +{{ order.orderDishes.length - 3 }}
                      </el-tag>
                    </template>
                    <template v-else>
                      <el-tag
                        v-for="(dish, index) in order.orderDishes"
                        :key="index"
                        size="small"
                        type="info"
                        effect="plain"
                        class="item-tag"
                      >
                        {{ dish.dishName }}x{{ dish.quantity }}
                        <span v-if="dish.customization" class="customization">({{ dish.customization }})</span>
                      </el-tag>
                      <el-tag
                        v-if="order.orderDishes.length > 3"
                        size="small"
                        type="warning"
                        effect="plain"
                        class="collapse-tag"
                        @click="toggleItemsExpand(order.id)"
                      >
                        收起
                      </el-tag>
                    </template>
                  </div>
                </div>
              </div>

              <div class="order-right">
                <div class="order-status">
                  <el-tag :type="orderStatusMap[order.status].type" size="large">
                    {{ orderStatusMap[order.status].text }}
                  </el-tag>
                  <el-badge v-if="order.unread" :value="''" type="danger" class="unread-badge" />
                </div>
              </div>
            </div>

            <div class="order-divider"></div>

            <div class="order-actions">
              <!-- 主要操作按钮组 -->
              <div class="primary-actions">
                <el-button
                  type="primary"
                  size="small"
                  @click="viewOrderDetails(order)"
                  class="detail-btn"
                >
                  <el-icon><View /></el-icon>
                  <span>详情</span>
                </el-button>

                <!-- 状态转换按钮 -->
                <el-button
                  v-if="order.status === 1"
                  type="success"
                  size="small"
                  @click="updateOrderStatus(order, 2)"
                  class="action-btn"
                >
                  <el-icon><CircleCheck /></el-icon>
                  <span>接单</span>
                </el-button>

                <el-button
                  v-if="order.status === 2"
                  type="warning"
                  size="small"
                  @click="updateOrderStatus(order, 3)"
                  class="action-btn"
                >
                  <el-icon><Goods /></el-icon>
                  <span>烹饪</span>
                </el-button>

                <el-button
                  v-if="order.status === 3"
                  type="primary"
                  size="small"
                  @click="updateOrderStatus(order, 4)"
                  class="action-btn"
                >
                  <el-icon><Dish /></el-icon>
                  <span>上菜</span>
                </el-button>

                <el-button
                  v-if="order.status === 4"
                  type="success"
                  size="small"
                  @click="updateOrderStatus(order, 5)"
                  class="action-btn complete-btn"
                >
                  <el-icon><CircleCheckFilled /></el-icon>
                  <span>完成</span>
                </el-button>

                <el-button
                  v-if="order.status === 5"
                  type="info"
                  size="small"
                  disabled
                  class="action-btn"
                >
                  <el-icon><Select /></el-icon>
                  <span>已完成</span>
                </el-button>
              </div>

              <!-- 更多操作下拉菜单 -->
              <el-dropdown trigger="click" class="more-dropdown">
                <el-button type="info" size="small" plain class="more-btn">
                  <el-icon><MoreFilled /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="toggleUserInfoExpand(order.id)">
                      <el-icon><User /></el-icon>
                      <span>{{ expandedUserInfo.has(order.id) ? '隐藏' : '显示' }}用户信息</span>
                    </el-dropdown-item>
                    <el-dropdown-item
                      v-if="order.status < 5"
                      divided
                      @click="cancelOrder(order)"
                    >
                      <el-icon><CircleClose /></el-icon>
                      <span style="color: #e6a23c">取消订单</span>
                    </el-dropdown-item>
                    <el-dropdown-item divided @click="deleteOrder(order)">
                      <el-icon><Delete /></el-icon>
                      <span style="color: #f56c6c">删除订单</span>
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </div>

      <!-- 空数据提示 -->
      <div v-if="filteredOrders.length === 0" key="empty" class="empty-orders">
        <el-empty :description="getEmptyDescription()"></el-empty>
      </div>
      </transition-group>
    </div>
  </div>
</template>

<style scoped lang="less">
.merchant-orders-container {
  padding: 0 20px 20px 20px;

  .orders-header {
    margin-bottom: 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px 24px;
    background: linear-gradient(135deg, #2196f3 0%, #1976d2 100%);
    border-radius: 16px;
    box-shadow: 0 4px 16px rgba(33, 150, 243, 0.3);

    .header-left {
      display: flex;
      align-items: center;
      gap: 16px;

      .page-title {
        font-size: 24px;
        font-weight: 700;
        margin: 0;
        color: #ffffff;
        letter-spacing: 0.5px;
        text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
      }

      .current-date {
        font-size: 14px;
        color: #ffffff;
        background: rgba(255, 255, 255, 0.25);
        padding: 6px 16px;
        border-radius: 20px;
        font-weight: 500;
        backdrop-filter: blur(10px);
        border: 1px solid rgba(255, 255, 255, 0.3);
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      }
    }

    .header-right {
      display: flex;
      gap: 10px;
      align-items: center;

      :deep(.el-button) {
        backdrop-filter: blur(10px);
        border: 1px solid rgba(255, 255, 255, 0.3);
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        }
      }
    }
  }

  .overview-section {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 24px;
    background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
    border: 1px solid #90caf9;
    border-radius: 16px;
    margin-bottom: 24px;
    flex-wrap: wrap;
    gap: 16px;
    box-shadow: 0 4px 12px rgba(33, 150, 243, 0.15);

    .stat-card {
      display: flex;
      align-items: center;
      gap: 16px;
      padding: 20px 24px;
      background: #ffffff;
      border-radius: 16px;
      box-shadow: 0 2px 12px rgba(33, 150, 243, 0.08);
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      cursor: pointer;
      flex: 1;
      min-width: 160px;
      border: 1px solid rgba(144, 202, 249, 0.3);
      position: relative;
      overflow: hidden;

      &::before {
        content: '';
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: linear-gradient(135deg, rgba(255, 255, 255, 0) 0%, rgba(255, 255, 255, 0.5) 100%);
        opacity: 0;
        transition: opacity 0.3s ease;
      }

      &:hover {
        transform: translateY(-8px) scale(1.02);
        box-shadow: 0 12px 28px rgba(33, 150, 243, 0.25);
        border-color: rgba(144, 202, 249, 0.8);

        &::before {
          opacity: 1;
        }

        .stat-icon {
          transform: rotate(10deg) scale(1.1);
        }
      }

      &:active {
        transform: translateY(-6px) scale(1.01);
      }

      .stat-icon {
        width: 52px;
        height: 52px;
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;
        border-radius: 14px;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        position: relative;
        z-index: 1;

        svg {
          width: 28px;
          height: 28px;
        }
      }

      .stat-content {
        flex: 1;
        position: relative;
        z-index: 1;

        .stat-value {
          font-size: 26px;
          font-weight: 700;
          color: #1976d2;
          line-height: 1.2;
          letter-spacing: -0.5px;
          transition: all 0.3s ease;

          &.animated-number {
            display: inline-block;
            animation: countUp 0.6s ease-out;
          }
        }

        .stat-label {
          font-size: 13px;
          color: #64b5f6;
          margin-top: 6px;
          font-weight: 500;
          letter-spacing: 0.3px;
        }
      }

      &.total {
        border-left: 4px solid #2196f3;

        .stat-icon {
          background: linear-gradient(135deg, rgba(33, 150, 243, 0.15) 0%, rgba(33, 150, 243, 0.08) 100%);
          color: #2196f3;
        }

        .stat-value {
          color: #1976d2;
        }
      }

      &.amount {
        border-left: 4px solid #f56c6c;

        .stat-icon {
          background: linear-gradient(135deg, rgba(245, 108, 108, 0.15) 0%, rgba(245, 108, 108, 0.08) 100%);
          color: #f56c6c;
        }

        .stat-value {
          color: #d32f2f;
        }

        &.highlight {
          background: linear-gradient(135deg, #fff5f5 0%, #ffffff 100%);
          border-width: 2px;

          .stat-value {
            font-size: 28px;
          }
        }
      }

      &.pending {
        border-left: 4px solid #ff9800;

        .stat-icon {
          background: linear-gradient(135deg, rgba(255, 152, 0, 0.15) 0%, rgba(255, 152, 0, 0.08) 100%);
          color: #ff9800;
        }

        .stat-value {
          color: #f57c00;
        }
      }

      &.preparing {
        border-left: 4px solid #ff9800;

        .stat-icon {
          background: linear-gradient(135deg, rgba(255, 152, 0, 0.15) 0%, rgba(255, 152, 0, 0.08) 100%);
          color: #ff9800;
        }

        .stat-value {
          color: #f57c00;
        }
      }

      &.completed {
        border-left: 4px solid #4caf50;

        .stat-icon {
          background: linear-gradient(135deg, rgba(76, 175, 80, 0.15) 0%, rgba(76, 175, 80, 0.08) 100%);
          color: #4caf50;
        }

        .stat-value {
          color: #388e3c;
        }
      }
    }
  }

  .filter-section {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 20px;
    background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
    border-radius: 12px;
    margin-bottom: 16px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
    border: 1px solid #e8eef5;
    transition: all 0.3s ease;

    &:hover {
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
    }

    .filter-left {
      display: flex;
      flex-direction: column;
      gap: 8px;
      flex: 1;

      .filter-header {
        display: flex;
        align-items: center;
        gap: 6px;

        .filter-icon {
          font-size: 16px;
          color: #409eff;
        }

        .filter-label {
          font-size: 13px;
          font-weight: 600;
          color: #303133;
        }
      }

      .status-filter-group {
        display: flex;
        flex-wrap: wrap;
        gap: 6px;
        align-items: center;

        .custom-status-tag {
          cursor: pointer;
          transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
          padding: 4px 10px;
          font-size: 12px;
          font-weight: 500;
          border-radius: 6px;
          display: inline-flex;
          align-items: center;
          gap: 4px;
          position: relative;
          user-select: none;

          .tag-icon {
            font-size: 12px;
          }

          .tag-text {
            font-size: 12px;
          }

          .close-icon {
            font-size: 10px;
            margin-left: 2px;
            opacity: 0.8;
            transition: all 0.2s ease;

            &:hover {
              opacity: 1;
              transform: scale(1.1);
            }
          }

          &:hover {
            transform: translateY(-1px);
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
          }

          &:active {
            transform: translateY(0);
          }

          // 全部标签样式
          &.status-tag-all {
            background: #f0f2f5;
            color: #606266;
            border: 1px solid #dcdfe6;

            &:hover {
              background: #e9ecef;
              border-color: #c0c4cc;
            }

            &.active {
              background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
              color: #ffffff;
              border-color: #1890ff;
              box-shadow: 0 2px 6px rgba(24, 144, 255, 0.3);
            }
          }

          // 待接单样式 - 红色
          &.status-tag-1 {
            background: #fff1f0;
            color: #f56c6c;
            border: 1px solid #ffccc7;

            &:hover {
              background: #ffccc7;
              border-color: #ffa39e;
            }

            &.active {
              background: linear-gradient(135deg, #f56c6c 0%, #ff7875 100%);
              color: #ffffff;
              border-color: #f56c6c;
              box-shadow: 0 2px 6px rgba(245, 108, 108, 0.4);
            }

            &.zero-count {
              background: #f5f5f5;
              color: #bfbfbf;
              border-color: #d9d9d9;

              &:hover {
                background: #f0f0f0;
                border-color: #d0d0d0;
              }
            }
          }

          // 备菜中样式 - 橙色
          &.status-tag-2 {
            background: #fff7e6;
            color: #e6a23c;
            border: 1px solid #ffd591;

            &:hover {
              background: #ffe7ba;
              border-color: #ffc069;
            }

            &.active {
              background: linear-gradient(135deg, #e6a23c 0%, #f0a858 100%);
              color: #ffffff;
              border-color: #e6a23c;
              box-shadow: 0 2px 6px rgba(230, 162, 60, 0.4);
            }

            &.zero-count {
              background: #f5f5f5;
              color: #bfbfbf;
              border-color: #d9d9d9;

              &:hover {
                background: #f0f0f0;
                border-color: #d0d0d0;
              }
            }
          }

          // 烹饪中样式 - 深橙色
          &.status-tag-3 {
            background: #fff2e8;
            color: #ff9800;
            border: 1px solid #ffd8bf;

            &:hover {
              background: #ffdcc8;
              border-color: #ffb380;
            }

            &.active {
              background: linear-gradient(135deg, #ff9800 0%, #ffa726 100%);
              color: #ffffff;
              border-color: #ff9800;
              box-shadow: 0 2px 6px rgba(255, 152, 0, 0.4);
            }

            &.zero-count {
              background: #f5f5f5;
              color: #bfbfbf;
              border-color: #d9d9d9;

              &:hover {
                background: #f0f0f0;
                border-color: #d0d0d0;
              }
            }
          }

          // 待上菜样式 - 蓝色
          &.status-tag-4 {
            background: #e6f7ff;
            color: #409eff;
            border: 1px solid #91d5ff;

            &:hover {
              background: #bae7ff;
              border-color: #69c0ff;
            }

            &.active {
              background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
              color: #ffffff;
              border-color: #409eff;
              box-shadow: 0 2px 6px rgba(64, 158, 255, 0.4);
            }

            &.zero-count {
              background: #f5f5f5;
              color: #bfbfbf;
              border-color: #d9d9d9;

              &:hover {
                background: #f0f0f0;
                border-color: #d0d0d0;
              }
            }
          }

          // 已完成样式 - 绿色
          &.status-tag-5 {
            background: #f6ffed;
            color: #67c23a;
            border: 1px solid #b7eb8f;

            &:hover {
              background: #d9f7be;
              border-color: #95de64;
            }

            &.active {
              background: linear-gradient(135deg, #67c23a 0%, #7bcf58 100%);
              color: #ffffff;
              border-color: #67c23a;
              box-shadow: 0 2px 6px rgba(103, 194, 58, 0.4);
            }

            &.zero-count {
              background: #f5f5f5;
              color: #bfbfbf;
              border-color: #d9d9d9;

              &:hover {
                background: #f0f0f0;
                border-color: #d0d0d0;
              }
            }
          }
        }
      }
    }

    .filter-right {
      flex-shrink: 0;

      :deep(.search-input) {
        width: 320px;

        .el-input__wrapper {
          border-radius: 10px;
          padding: 8px 12px;
          box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
          transition: all 0.3s ease;

          &:hover {
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
          }

          &.is-focus {
            box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2), 0 2px 8px rgba(64, 158, 255, 0.3);
          }
        }

        .el-input__prefix {
          color: #909399;
        }
      }
    }
  }

  .quick-actions {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 20px;
    background: #ffffff;
    border-radius: 12px;
    margin-bottom: 20px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
    border: 1px solid #f0f0f0;

    .quick-actions-left {
      display: flex;
      gap: 10px;

      .quick-action-btn {
        display: inline-flex;
        align-items: center;
        gap: 6px;
        padding: 8px 16px;
        border-radius: 8px;
        font-weight: 500;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);

        .el-icon {
          font-size: 14px;
        }

        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        }

        &:active {
          transform: translateY(0);
        }
      }
    }

    .quick-actions-right {
      .order-count-info {
        font-size: 14px;
        color: #606266;
        padding: 8px 16px;
        background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
        border-radius: 8px;
        border: 1px solid #bae6fd;

        strong {
          color: #0284c7;
          font-size: 16px;
          font-weight: 700;
        }
      }
    }
  }

  .orders-list-section {
    min-height: 400px;

    .order-item {
      margin-bottom: 16px;
      margin-top: 16px;
      background-color: #fff;
      position: relative;
      overflow: visible;
      border-radius: 12px;

      .order-main {
        display: flex;
        flex-direction: column;
        padding: 18px 20px;
        border: 2px solid #e2e8f0;
        border-radius: 12px;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
        position: relative;
        overflow: visible;
        gap: 16px;

        .order-content {
          display: flex;
          justify-content: space-between;
          align-items: flex-start;
          gap: 20px;
        }

        .order-divider {
          height: 1px;
          background: linear-gradient(90deg, transparent 0%, #e2e8f0 10%, #e2e8f0 90%, transparent 100%);
          margin: 0;
        }

        &::before {
          content: '';
          position: absolute;
          left: 0;
          top: 0;
          bottom: 0;
          width: 4px;
          background: #e2e8f0;
          transition: all 0.3s ease;
          border-radius: 12px 0 0 12px;
        }

        &:hover {
          box-shadow: 0 6px 24px rgba(0, 0, 0, 0.12);
          border-color: #cbd5e1;
        }

        &:active {
          transform: scale(0.998);
        }
      }

      // 状态边框颜色（使用数字状态码）
      &.status-1 .order-main::before {
        background: linear-gradient(180deg, #f56c6c 0%, #ff7875 100%);
        box-shadow: 0 0 8px rgba(245, 108, 108, 0.3);
      }

      &.status-2 .order-main::before {
        background: linear-gradient(180deg, #e6a23c 0%, #f0a858 100%);
        box-shadow: 0 0 8px rgba(230, 162, 60, 0.3);
      }

      &.status-3 .order-main::before {
        background: linear-gradient(180deg, #ff9800 0%, #ffa726 100%);
        box-shadow: 0 0 8px rgba(255, 152, 0, 0.3);
      }

      &.status-4 .order-main::before {
        background: linear-gradient(180deg, #409eff 0%, #66b1ff 100%);
        box-shadow: 0 0 8px rgba(64, 158, 255, 0.3);
      }

      &.status-5 .order-main::before {
        background: linear-gradient(180deg, #67c23a 0%, #7bcf58 100%);
        box-shadow: 0 0 8px rgba(103, 194, 58, 0.3);
      }

      &.status-6 .order-main::before {
        background: linear-gradient(180deg, #c0c4cc 0%, #d3d4d6 100%);
      }

      // 未读订单样式
      &.unread-order .order-main {
        background: linear-gradient(to right, #fef2f2 0%, #ffffff 40%);
        border-color: #fee2e2;

        &::after {
          content: '新订单';
          position: absolute;
          top: -10px;
          left: 20px;
          background: linear-gradient(135deg, #f56c6c 0%, #ff7875 100%);
          color: white;
          font-size: 11px;
          padding: 4px 12px;
          border-radius: 12px;
          font-weight: 600;
          animation: newOrderPulse 2s ease-in-out infinite;
          box-shadow: 0 2px 8px rgba(245, 108, 108, 0.4);
          z-index: 10;
        }
      }

      // 加急订单样式
      &.urgent-order .order-main {
        border-width: 2px;
        border-style: solid;

        &:not(.status-cancelled) {
          border-color: #ff4d4f;
          background: linear-gradient(to right, #fff1f0 0%, #ffffff 20%);

          &::before {
            width: 5px;
            background: linear-gradient(180deg, #ff4d4f 0%, #ff7875 100%);
            box-shadow: 0 0 12px rgba(255, 77, 79, 0.5);
          }
        }
      }

      .order-left {
        flex: 1;
        min-width: 0;

        .order-basic-info {
          display: grid;
          grid-template-columns: auto auto 1fr;
          gap: 12px 20px;
          margin-bottom: 12px;
          font-size: 14px;
          align-items: center;

          .order-no {
            display: flex;
            align-items: center;
            gap: 8px;
            font-weight: 600;
            font-size: 15px;
            white-space: nowrap;

            .no-label {
              color: #909399;
              font-size: 12px;
              font-weight: 500;
              margin-right: 4px;
            }

            .no-value {
              color: #303133;
              font-family: 'Consolas', 'Monaco', monospace;
              font-weight: 600;
            }

            .priority-tag {
              animation: urgentPulse 1.5s ease-in-out infinite;
              font-weight: 600;
              flex-shrink: 0;
              margin-left: 8px;
            }
          }

          .order-amount {
            display: flex;
            align-items: center;
            gap: 6px;
            font-weight: 700;
            font-size: 17px;
            color: #f56c6c;
            white-space: nowrap;

            .amount-label {
              font-size: 12px;
              color: #909399;
              font-weight: 500;
            }

            .amount-value {
              font-family: 'Consolas', 'Monaco', monospace;
            }
          }

          .order-time {
            display: flex;
            align-items: center;
            gap: 6px;
            color: #606266;
            font-size: 13px;
            justify-self: end;
            white-space: nowrap;

            .time-label {
              color: #909399;
              font-size: 12px;
              font-weight: 500;
            }

            .time-value {
              font-weight: 500;
            }
          }
        }

        .order-user-info {
          display: grid;
          grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
          gap: 10px 20px;
          font-size: 14px;
          color: #606266;
          margin-bottom: 8px;

          > div {
            display: flex;
            align-items: center;
            gap: 8px;
            min-width: 0;

            .info-label {
              font-size: 12px;
              color: #909399;
              font-weight: 500;
              flex-shrink: 0;
              min-width: 36px;
            }

            .info-value {
              font-weight: 500;
              overflow: hidden;
              text-overflow: ellipsis;
              white-space: nowrap;

              &.remark-text {
                color: #e6a23c;
                font-style: italic;
              }
            }
          }

          .user-remark {
            grid-column: 1 / -1;
            background: linear-gradient(135deg, #fffbeb 0%, #fef3c7 100%);
            padding: 8px 12px;
            border-radius: 8px;
            border: 1px solid #fcd34d;

            .info-value {
              color: #d97706;
            }
          }
        }

        .order-remark {
          display: flex;
          align-items: center;
          gap: 10px;
          padding: 10px 14px;
          background: linear-gradient(135deg, #fffbeb 0%, #fef3c7 100%);
          border-radius: 10px;
          border: 1px solid #fcd34d;
          margin-bottom: 10px;
          box-shadow: 0 1px 4px rgba(252, 211, 77, 0.2);

          .remark-icon {
            font-size: 16px;
            flex-shrink: 0;
            animation: noteBounce 2s ease-in-out infinite;
          }

          .remark-content {
            flex: 1;
            display: flex;
            align-items: center;
            gap: 6px;
            font-size: 13px;

            .remark-label {
              font-weight: 600;
              color: #d97706;
              flex-shrink: 0;
            }

            .remark-text {
              color: #b45309;
              font-weight: 500;
              word-break: break-word;
            }
          }
        }

        .order-items {
          display: flex;
          align-items: flex-start;
          gap: 10px;
          margin-top: 10px;
          padding: 10px 14px;
          background: linear-gradient(135deg, #fafafa 0%, #f5f5f5 100%);
          border-radius: 10px;
          border: 1px solid #e8e8e8;

          .items-label {
            font-size: 12px;
            font-weight: 600;
            color: #606266;
            flex-shrink: 0;
            min-width: 36px;
          }

          .items-list {
            display: flex;
            flex-wrap: wrap;
            gap: 6px;
            flex: 1;

            .item-tag {
              font-size: 12px;
              font-weight: 500;
              padding: 3px 10px;
              transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
              background: #fff;
              border-color: #d9d9d9;
              color: #595959;

              .customization {
                color: #e6a23c;
                font-weight: 600;
                font-size: 11px;
              }

              &:hover {
                transform: translateY(-2px);
                box-shadow: 0 2px 6px rgba(0, 0, 0, 0.12);
              }
            }

            .more-tag {
              cursor: pointer;
              background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
              border-color: #1890ff;
              color: white;
              font-weight: 500;
              transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);

              &:hover {
                transform: translateY(-2px);
                box-shadow: 0 4px 8px rgba(24, 144, 255, 0.4);
              }
            }

            .collapse-tag {
              cursor: pointer;
              font-weight: 500;
              background: #fff;
              border-color: #faad14;
              color: #faad14;
              transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);

              &:hover {
                transform: translateY(-2px);
                background: #faad14;
                color: white;
                box-shadow: 0 2px 6px rgba(250, 173, 20, 0.3);
              }
            }
          }
        }
      }

      .order-right {
        display: flex;
        flex-direction: column;
        align-items: flex-end;
        gap: 12px;
        flex-shrink: 0;
        align-self: flex-start;

        .order-status {
          position: relative;

          .unread-badge {
            position: absolute;
            top: -5px;
            right: -5px;
          }
        }
      }

      .order-actions {
        display: flex;
        gap: 8px;
        justify-content: flex-end;
        align-items: center;
        width: 100%;
        padding-top: 4px;

        .primary-actions {
          display: flex;
          gap: 6px;
          flex-wrap: wrap;
          justify-content: flex-end;
        }

        :deep(.el-button) {
          display: inline-flex;
          align-items: center;
          gap: 4px;
          padding: 6px 12px;
          font-weight: 500;
          transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
          border-radius: 8px;
          box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);

          .el-icon {
            font-size: 14px;
          }

          &:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
          }

          &:active {
            transform: translateY(0);
          }

          &.detail-btn {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            border: none;
            color: white;

            &:hover {
              background: linear-gradient(135deg, #5568d3 0%, #653a8b 100%);
            }
          }

          &.action-btn {
            min-width: 70px;
            font-weight: 600;
          }

          &.complete-btn {
            background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
            border: none;
            color: white;

            &:hover {
              background: linear-gradient(135deg, #0e837a 0%, #2ed16b 100%);
              box-shadow: 0 4px 12px rgba(56, 239, 125, 0.4);
            }
          }

          &:disabled {
            opacity: 0.6;
            cursor: not-allowed;
            background: #f5f5f5;
            border-color: #d9d9d9;
            color: #999;

            &:hover {
              transform: none;
              box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
            }
          }
        }

        .more-dropdown {
          :deep(.el-button) {
            &.more-btn {
              width: 32px;
              height: 32px;
              padding: 0;
              display: inline-flex;
              align-items: center;
              justify-content: center;
              border-radius: 50%;
            }
          }
        }

        :deep(.el-dropdown-menu) {
          .el-dropdown-menu__item {
            display: flex;
            align-items: center;
            gap: 8px;
            padding: 8px 16px;

            .el-icon {
              font-size: 16px;
            }

            &:hover {
              background: linear-gradient(135deg, #f5f7fa 0%, #e8eef5 100%);
            }
          }
        }
      }
    }

    .empty-orders {
      text-align: center;
      margin-top: 50px;
    }
  }
}

// 动画关键帧
@keyframes newOrderPulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.85;
    transform: scale(1.08);
  }
}

@keyframes noteBounce {
  0%, 100% {
    transform: rotate(0deg);
  }
  25% {
    transform: rotate(-10deg);
  }
  75% {
    transform: rotate(10deg);
  }
}

@keyframes urgentPulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
    box-shadow: 0 0 0 0 rgba(245, 108, 108, 0.4);
  }
  50% {
    opacity: 0.9;
    transform: scale(1.05);
    box-shadow: 0 0 0 6px rgba(245, 108, 108, 0);
  }
}

@keyframes countUp {
  0% {
    opacity: 0;
    transform: translateY(10px);
  }
  50% {
    opacity: 1;
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

// 列表进入动画
.list-enter-active,
.list-leave-active {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.list-enter-from {
  opacity: 0;
  transform: translateY(20px) scale(0.95);
}

.list-leave-to {
  opacity: 0;
  transform: translateX(-30px);
}

.list-move {
  transition: transform 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

// 折叠动画
.slide-fade-enter-active {
  transition: all 0.3s ease-out;
}

.slide-fade-leave-active {
  transition: all 0.2s cubic-bezier(1, 0.5, 0.8, 1);
}

.slide-fade-enter-from,
.slide-fade-leave-to {
  transform: translateY(-10px);
  opacity: 0;
}

@media (max-width: 1200px) {
  .merchant-orders-container {
    .orders-list-section {
      .order-item {
        .order-main {
          .order-content {
            .order-left {
              .order-basic-info {
                grid-template-columns: auto auto 1fr;
                gap: 10px 14px;
              }

              .order-user-info {
                grid-template-columns: repeat(2, 1fr);
                gap: 8px 14px;
              }
            }
          }
        }
      }
    }

    .overview-section {
      .stat-card {
        min-width: 140px;
        padding: 16px 18px;

        .stat-value {
          font-size: 22px !important;
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .merchant-orders-container {
    padding: 12px;

    .orders-header {
      flex-direction: column;
      gap: 12px;
      align-items: stretch;
    }

    .overview-section {
      gap: 10px;

      .stat-card {
        min-width: calc(50% - 5px);
        padding: 16px;

        .stat-value {
          font-size: 20px !important;
        }

        .stat-icon {
          width: 44px;
          height: 44px;

          svg {
            width: 24px;
            height: 24px;
          }
        }
      }
    }

    .filter-section {
      flex-direction: column;
      gap: 16px;
      padding: 16px;

      .filter-left {
        width: 100%;

        .filter-header {
          .filter-label {
            font-size: 14px;
          }
        }

        .status-filter-group {
          gap: 4px;

          .custom-status-tag {
            padding: 3px 8px;
            font-size: 11px;

            .tag-icon {
              font-size: 11px;
            }

            .tag-text {
              font-size: 11px;
            }

            .close-icon {
              font-size: 9px;
            }
          }
        }
      }

      .filter-right {
        width: 100%;

        :deep(.search-input) {
          width: 100% !important;
        }
      }
    }

    .quick-actions {
      flex-direction: column;
      gap: 12px;
      padding: 14px;

      .quick-actions-left {
        width: 100%;
        justify-content: center;
      }

      .quick-actions-right {
        width: 100%;
        text-align: center;

        .order-count-info {
          display: inline-block;
        }
      }
    }

    .orders-list-section {
      .order-item {
        .order-main {
          flex-direction: column;
          gap: 12px;
          padding: 14px;

          .order-content {
            flex-direction: column;
            gap: 12px;
          }

          .order-left {
            width: 100%;

            .order-basic-info {
              grid-template-columns: 1fr;
              gap: 8px;

              .order-no {
                font-size: 14px;
              }

              .order-amount {
                font-size: 15px;
              }

              .order-time {
                justify-self: start;
              }
            }

            .order-user-info {
              grid-template-columns: 1fr;
              gap: 8px;

              > div {
                font-size: 13px;
              }
            }

            .order-items {
              flex-direction: column;
              gap: 8px;

              .items-label {
                font-size: 12px;
              }
            }

            .order-remark {
              flex-direction: row;
              padding: 8px 12px;

              .remark-icon {
                font-size: 14px;
              }

              .remark-content {
                font-size: 12px;

                .remark-label {
                  font-size: 12px;
                }
              }
            }
          }

          .order-right {
            align-items: flex-start;
            width: 100%;
          }

          .order-actions {
            flex-direction: column;
            width: 100%;
            gap: 10px;

            .primary-actions {
              width: 100%;
              justify-content: center;
            }

            :deep(.el-button) {
              width: 100%;
              justify-content: center;
              padding: 8px 12px;
              font-size: 13px;

              .el-icon {
                font-size: 16px;
              }
            }

            .more-dropdown {
              width: 100%;

              :deep(.el-button) {
                width: 100%;
                border-radius: 8px;
                padding: 8px;

                &.more-btn {
                  width: 100%;
                  border-radius: 8px;
                }
              }
            }
          }
        }
      }
    }
  }
}
</style>
