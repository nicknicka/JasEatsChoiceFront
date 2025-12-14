<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../../utils/api.js'

const router = useRouter()

// 商家信息
const merchantInfo = ref({
  name: '健康轻食馆',
  rating: 4.8,
  phone: '138-1234-5678',
  email: 'health-food@example.com',
  address: '北京市朝阳区建国路88号'
})

// 商家营业概览
const businessOverview = ref({
  sales: 0,
  orders: 0,
  newComments: 0,
  unreadMessages: 3
})

// 优惠活动列表 - 初始化为空，等待后端数据
const discounts = ref([])

// 优惠管理对话框
const discountDialogVisible = ref(false)
const currentDiscountForm = ref({})
const isEditingDiscount = ref(false)

// 批量操作选中的优惠
const selectedDiscounts = ref([])

// 批量删除优惠
const batchDeleteDiscounts = () => {
  if (selectedDiscounts.value.length === 0) {
    ElMessage.warning('请先选择要删除的优惠')
    return
  }

  const discountIds = selectedDiscounts.value.map((discount) => discount.id)

  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedDiscounts.value.length} 个优惠活动吗？`,
    '批量删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(() => {
      // 调用后端API批量删除优惠
      api
        .delete(API_CONFIG.merchant.discounts.replace('{merchantId}', merchantInfo.value.id), {
          data: discountIds // 发送删除的ID列表
        })
        .then((response) => {
          if (response.data && response.data.success) {
            // 更新本地数据
            discounts.value = discounts.value.filter(
              (discount) => !discountIds.includes(discount.id)
            )
            selectedDiscounts.value = []
            ElMessage.success('优惠活动批量删除成功')
          }
        })
        .catch((error) => {
          console.error('批量删除优惠活动失败:', error)
          ElMessage.error('批量删除优惠活动失败')
        })
    })
    .catch(() => {
      ElMessage.info('已取消删除')
    })
}

// 打开优惠管理对话框
const openDiscountDialog = (discount = null) => {
  discountDialogVisible.value = true
  if (discount) {
    // 编辑模式
    isEditingDiscount.value = true
    currentDiscountForm.value = { ...discount }
  } else {
    // 新增模式
    isEditingDiscount.value = false
    currentDiscountForm.value = {
      name: '',
      type: '满减',
      description: '',
      status: 'active'
    }
  }
}

// 保存优惠
const saveDiscount = () => {
  // 简单的表单验证
  if (!currentDiscountForm.value.name || !currentDiscountForm.value.description) {
    ElMessage.error('请填写完整的优惠信息')
    return
  }

  if (isEditingDiscount.value) {
    // 编辑模式 - 更新现有优惠
    api
      .put(
        API_CONFIG.merchant.discounts.replace('{merchantId}', merchantInfo.value.id),
        currentDiscountForm.value
      )
      .then((response) => {
        if (response.data && response.data.success) {
          // 更新本地数据
          const index = discounts.value.findIndex((d) => d.id === currentDiscountForm.value.id)
          if (index !== -1) {
            discounts.value[index] = { ...currentDiscountForm.value }
          }
          ElMessage.success('优惠活动已更新')
        }
      })
      .catch((error) => {
        console.error('更新优惠活动失败:', error)
        ElMessage.error('更新优惠活动失败')
      })
  } else {
    // 新增模式 - 添加新优惠
    api
      .post(
        API_CONFIG.merchant.discounts.replace('{merchantId}', merchantInfo.value.id),
        currentDiscountForm.value
      )
      .then((response) => {
        if (response.data && response.data.success) {
          const newDiscount = response.data.data
          discounts.value.push(newDiscount)
          ElMessage.success('优惠活动已添加')
        }
      })
      .catch((error) => {
        console.error('添加优惠活动失败:', error)
        ElMessage.error('添加优惠活动失败')
      })
  }

  discountDialogVisible.value = false
  currentDiscountForm.value = {}
}

// 店铺相册
const shopAlbum = ref({
  environment: [],
  dishes: []
})

// 上传相关变量
const uploadAlbumType = ref('environment')
const imageUploadList = ref([])
const fullAlbumPreviewVisible = ref(false)

// 获取所有照片用于全屏预览
const getAllImages = computed(() => {
  return [...shopAlbum.value.environment, ...shopAlbum.value.dishes]
})

// 上传照片变更处理
const handleUpload = (file, fileList) => {
  console.log('上传照片变更:', file, fileList)
  imageUploadList.value = fileList
}

// 移除上传的照片
const handleUploadRemove = (removedFile, fileList) => {
  console.log('移除上传的照片:', removedFile)
  imageUploadList.value = fileList
}

// 确认上传照片
const confirmUpload = () => {
  if (imageUploadList.value.length === 0) {
    ElMessage.warning('请先选择要上传的照片')
    return
  }

  const albumTypeText = uploadAlbumType.value === 'environment' ? '店铺环境' : '菜品展示'
  const formData = new FormData()

  // 添加照片文件到FormData
  imageUploadList.value.forEach((file) => {
    formData.append('files', file.raw)
  })

  // 添加相册类型
  formData.append('albumType', uploadAlbumType.value)

  // 调用后端API上传照片
  api
    .post(API_CONFIG.merchant.album.replace('{merchantId}', merchantInfo.value.id), formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    .then((response) => {
      if (response.data && response.data.success) {
        const uploadedImages = response.data.data

        // 将上传的照片添加到对应相册
        shopAlbum.value[uploadAlbumType.value].push(...uploadedImages)

        // 上传完成后清空上传列表
        imageUploadList.value = []

        // 显示上传成功提示
        ElMessage.success(`已成功上传${uploadedImages.length}张照片到${albumTypeText}相册`)
      }
    })
    .catch((error) => {
      console.error('上传照片失败:', error)
      ElMessage.error('上传照片失败')
    })
}

// 删除相册图片
const deleteAlbumImage = (type, index) => {
  const imageUrl = shopAlbum.value[type][index]

  // 确认删除
  ElMessageBox.confirm('确定要删除这张照片吗？', '删除照片', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => {
      // 调用后端API删除照片
      api
        .delete(API_CONFIG.merchant.album.replace('{merchantId}', merchantInfo.value.id), {
          params: {
            imageUrl,
            albumType: type
          }
        })
        .then((response) => {
          if (response.data && response.data.success) {
            // 从本地相册中删除图片
            shopAlbum.value[type].splice(index, 1)
            ElMessage.success('照片已删除')
          }
        })
        .catch((error) => {
          console.error('删除照片失败:', error)
          ElMessage.error('删除照片失败')
        })
    })
    .catch(() => {
      ElMessage.info('已取消删除')
    })
}

// 打开全屏相册预览
const openFullAlbumPreview = () => {
  fullAlbumPreviewVisible.value = true
}

// 公告栏配置
const announcements = ref([])
const announcementDialogVisible = ref(false)
const currentAnnouncement = ref({
  title: '',
  content: '',
  status: 'active',
  startTime: null,
  endTime: null
})
const isEditingAnnouncement = ref(false)

// 获取公告列表
const getAnnouncements = () => {
  // 调用后端API获取公告列表
  let url = API_CONFIG.merchant.announcements
  url = url.replace('{merchantId}', merchantInfo.value.id)
  api
    .get(url)
    .then(function (response) {
      if (response.data && response.data.success) {
        announcements.value = response.data.data
      }
    })
    .catch(function (error) {
      console.error('获取公告列表失败:', error)
    })
}

// 打开公告编辑对话框
const openAnnouncementDialog = function (announcement = null) {
  announcementDialogVisible.value = true
  if (announcement) {
    isEditingAnnouncement.value = true
    currentAnnouncement.value = JSON.parse(JSON.stringify(announcement))
  } else {
    isEditingAnnouncement.value = false
    currentAnnouncement.value = {
      title: '',
      content: '',
      status: 'active',
      startTime: null,
      endTime: null
    }
  }
}

// 保存公告
const saveAnnouncement = function () {
  // 简单验证
  if (!currentAnnouncement.value.title || !currentAnnouncement.value.content) {
    ElMessage.error('请填写完整的公告信息')
    return
  }

  let apiMethod = isEditingAnnouncement.value ? api.put : api.post
  let apiUrl = API_CONFIG.merchant.announcements.replace('{merchantId}', merchantInfo.value.id)
  if (isEditingAnnouncement.value) {
    apiUrl = apiUrl + '/' + currentAnnouncement.value.id
  }

  apiMethod(apiUrl, currentAnnouncement.value)
    .then(function (response) {
      if (response.data && response.data.success) {
        let message = isEditingAnnouncement.value ? '公告已更新' : '公告已添加'
        ElMessage.success(message)
        getAnnouncements() // 刷新公告列表
        announcementDialogVisible.value = false
      }
    })
    .catch(function (error) {
      console.error('保存公告失败:', error)
      ElMessage.error('保存公告失败')
    })
}

// 删除公告
const deleteAnnouncement = function (announcement) {
  ElMessageBox.confirm("确定要删除公告 '" + announcement.title + "' 吗？", '删除公告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(function () {
      let url = API_CONFIG.merchant.announcements.replace('{merchantId}', merchantInfo.value.id)
      url = url + '/' + announcement.id
      api
        .delete(url)
        .then(function (response) {
          if (response.data && response.data.success) {
            ElMessage.success('公告已删除')
            getAnnouncements() // 刷新公告列表
          }
        })
        .catch(function (error) {
          console.error('删除公告失败:', error)
          ElMessage.error('删除公告失败')
        })
    })
    .catch(function () {
      ElMessage.info('已取消删除')
    })
}

// 切换公告状态
const toggleAnnouncementStatus = function (announcement) {
  let newStatus = announcement.status === 'active' ? 'inactive' : 'active'
  let statusText = newStatus === 'active' ? '已启用' : '已禁用'

  let url = API_CONFIG.merchant.announcements.replace('{merchantId}', merchantInfo.value.id)
  url = url + '/' + announcement.id + '/status'

  api
    .put(url, { status: newStatus })
    .then(function (response) {
      if (response.data && response.data.success) {
        announcement.status = newStatus
        ElMessage.success('公告已' + statusText)
      }
    })
    .catch(function (error) {
      console.error('切换公告状态失败:', error)
      ElMessage.error('切换公告状态失败')
    })
}

// 删除单个优惠
const deleteDiscount = (discount) => {
  ElMessageBox.confirm(`确定要删除优惠活动 "${discount.name}" 吗？`, '删除优惠', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => {
      // 调用后端API删除优惠
      api
        .delete(
          `${API_CONFIG.merchant.discounts.replace(
            '{merchantId}',
            merchantInfo.value.id
          )}/${discount.id}`
        )
        .then((response) => {
          if (response.data && response.data.success) {
            const index = discounts.value.findIndex((d) => d.id === discount.id)
            if (index !== -1) {
              discounts.value.splice(index, 1)
            }
            ElMessage.success('优惠活动删除成功')
          }
        })
        .catch((error) => {
          console.error('删除优惠活动失败:', error)
          ElMessage.error('删除优惠活动失败')
        })
    })
    .catch(() => {
      ElMessage.info('已取消删除')
    })
}

// 页面跳转
const navigateToOrders = () => {
  router.push('/merchant/home/orders')
}

// 查看订单详情
const viewOrderDetails = (order) => {
  // 跳转到订单详情页面
  router.push(`/merchant/home/orders/details?orderId=${order.id}`)
}

// 更新订单状态
const updateOrderStatus = (order) => {
  // 定义订单状态流转逻辑
  const statusFlow = {
    1: 2, // 待处理 -> 备菜中
    2: 3, // 备菜中 -> 烹饪中
    3: 4, // 烹饪中 -> 待配送
    4: 5, // 待配送 -> 已完成
    5: 5, // 已完成 -> 已完成（不可再改）
    6: 6 // 已取消 -> 已取消（不可再改）
  }

  const nextStatus = statusFlow[order.status] || order.status

  // 如果状态没有变化
  if (nextStatus === order.status) {
    ElMessage.warning(`订单 ${order.id} 当前状态不可变更`)
    return
  }

  // 调用API更新订单状态
  const updateData = {
    orderId: order.id,
    status: nextStatus
  }

  api
    .put(API_CONFIG.merchant.updateOrderStatus.replace('{orderId}', order.id), updateData)
    .then((response) => {
      if (response.data && response.data.success) {
        // 更新本地订单状态
        order.status = nextStatus
        ElMessage.success(`订单 ${order.id} 状态已更新为 ${orderStatusMap[nextStatus]}`)
      }
    })
    .catch((error) => {
      console.error('更新订单状态失败:', error)
      ElMessage.error('更新订单状态失败')
    })
}

// 通知用户
const notifyUser = (order) => {
  // 调用API通知用户
  const notifyData = {
    orderId: order.id,
    message: `您的订单 ${order.id} 状态已更新为 ${orderStatusMap[order.status]}`
  }

  api
    .post(API_CONFIG.merchant.notifyUser.replace('{orderId}', order.id), notifyData)
    .then((response) => {
      if (response.data && response.data.success) {
        ElMessage.success(`已成功通知用户订单 ${order.id} 的最新状态`)
      }
    })
    .catch((error) => {
      console.error('通知用户失败:', error)
      ElMessage.error('通知用户失败')
    })
}

// 概览项导航
const navigateToStatistics = () => {
  router.push('/merchant/home/statistics')
}

const navigateToComments = () => {
  router.push('/merchant/home/comments')
}

const navigateToMessages = () => {
  router.push('/merchant/home/messages')
}

// 营业概览配置数组 - 使用循环减少冗余
const overviewConfig = ref([
  {
    key: 'sales',
    icon: '💰',
    label: '营业额',
    onClick: navigateToStatistics,
    trend: '↑ 12.5%',
    trendClass: 'trend-up',
    suffix: '¥'
  },
  {
    key: 'orders',
    icon: '🍽️',
    label: '订单数',
    onClick: navigateToOrders,
    trend: '↑ 8.3%',
    trendClass: 'trend-up'
  },
  {
    key: 'newComments',
    icon: '🌟',
    label: '新增评价',
    onClick: navigateToComments,
    trend: '↓ 2.1%',
    trendClass: 'trend-down'
  },
  {
    key: 'unreadMessages',
    icon: '📞',
    label: '未读消息',
    onClick: navigateToMessages,
    trend: '→ 0%',
    trendClass: 'trend-neutral'
  }
])

// 筛选功能
const activeFilter = ref('today')

// 菜单筛选功能
const activeMenuFilter = ref('all')

// 菜单分类列表 - 使用数组减少冗余
const menuCategories = ref(['all', '早餐', '午餐', '晚餐', '下午茶', '今日特色'])

// 菜单状态列表 - 使用数组减少冗余
const menuStatuses = ref([
  { value: 'all', label: '全部' },
  { value: 'online', label: '上架中' },
  { value: 'offline', label: '下架中' },
  { value: 'draft', label: '草稿' }
])

// 所有订单数据
const allOrders = ref([])

// 筛选后的订单
const filteredOrders = ref([])

// 订单状态映射
const orderStatusMap = {
  1: '待处理',
  2: '备菜中',
  3: '烹饪中',
  4: '待配送',
  5: '已完成',
  6: '已取消'
}

// 筛选订单
const filterOrders = (filterType) => {
  activeFilter.value = filterType

  // 简单的筛选逻辑，根据实际时间处理
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const weekStart = new Date(now.getFullYear(), now.getMonth(), now.getDate() - now.getDay())
  const monthStart = new Date(now.getFullYear(), now.getMonth(), 1)

  filteredOrders.value = allOrders.value.filter((order) => {
    const orderDate = new Date(order.createTime)

    // 时间范围过滤
    let timeMatch = true
    switch (filterType) {
      case 'today':
        timeMatch = orderDate >= today
        break
      case 'week':
        timeMatch = orderDate >= weekStart
        break
      case 'month':
        timeMatch = orderDate >= monthStart
        break
    }

    return timeMatch
  })
}

const navigateToMenu = () => {
  router.push('/merchant/home/menu')
}

// 快捷操作函数 - 设置优惠
const setDiscount = () => {
  // 优惠活动管理在当前页面，无需跳转
}

// 快捷操作函数 - 联系客服
const contactCustomerService = () => {
  ElMessage.info('联系客服功能已触发')
  // 可以在此处添加具体的实现逻辑
}

// 菜单状态映射
const menuStatusMap = {
  online: { text: '上架中', icon: '🟢', type: 'success' },
  draft: { text: '草稿', icon: '🟡', type: 'warning' },
  offline: { text: '下架中', icon: '🔴', type: 'danger' }
}

// 菜品状态映射
const dishStatusMap = {
  online: { text: '🟢 在售', type: 'success' },
  almost_sold: { text: '🟡 即将售罄', type: 'warning' },
  offline: { text: '🔴 下架', type: 'danger' }
}

// 模拟菜品数据，关联到各个菜单
const dishData = {
  早餐菜单: [
    {
      id: 1,
      name: '豆浆',
      price: 3,
      category: '饮品',
      status: 'online',
      stock: 100,
      updateTime: '2024-11-21 06:00'
    },
    {
      id: 2,
      name: '油条',
      price: 2,
      category: '主食',
      status: 'online',
      stock: 80,
      updateTime: '2024-11-21 06:30'
    },
    {
      id: 3,
      name: '包子',
      price: 1.5,
      category: '主食',
      status: 'online',
      stock: 120,
      updateTime: '2024-11-21 06:15'
    }
  ],
  午餐菜单: [
    {
      id: 4,
      name: '鱼香肉丝',
      price: 18,
      category: '热菜',
      status: 'online',
      stock: 50,
      updateTime: '2024-11-21 10:30'
    },
    {
      id: 5,
      name: '宫保鸡丁',
      price: 16,
      category: '热菜',
      status: 'online',
      stock: 40,
      updateTime: '2024-11-21 10:45'
    },
    {
      id: 6,
      name: '西红柿鸡蛋',
      price: 12,
      category: '热菜',
      status: 'online',
      stock: 60,
      updateTime: '2024-11-21 10:20'
    }
  ],
  晚餐菜单: [
    {
      id: 7,
      name: '红烧肉',
      price: 22,
      category: '热菜',
      status: 'online',
      stock: 30,
      updateTime: '2024-11-21 16:30'
    },
    {
      id: 8,
      name: '清蒸鱼',
      price: 28,
      category: '海鲜',
      status: 'online',
      stock: 20,
      updateTime: '2024-11-21 16:45'
    }
  ],
  下午茶菜单: [
    {
      id: 9,
      name: '奶茶',
      price: 15,
      category: '饮品',
      status: 'online',
      stock: 70,
      updateTime: '2024-11-21 14:00'
    },
    {
      id: 10,
      name: '蛋糕',
      price: 25,
      category: '甜点',
      status: 'online',
      stock: 40,
      updateTime: '2024-11-21 14:30'
    }
  ],
  今日特色菜单: [
    {
      id: 4,
      name: '鱼香肉丝',
      price: 18,
      category: '热菜',
      status: 'online',
      stock: 50,
      updateTime: '2024-11-21 10:30'
    },
    {
      id: 9,
      name: '奶茶',
      price: 15,
      category: '饮品',
      status: 'online',
      stock: 70,
      updateTime: '2024-11-21 14:00'
    }
  ]
}

// 假设商家ID为1，可以根据实际情况从登录信息或路由参数中获取
const merchantId = 1

// 今日菜单数据
const todayMenus = ref([])

// 从后端获取今日菜单数据
const fetchTodayMenus = () => {
  api
    .get(`/v1/menus/merchants/${merchantId}/menu`)
    .then((response) => {
      if (response.code === '200' && response.data) {
        // 假设后端返回的菜单数据结构与我们需要的基本一致
        // 如果需要转换数据格式，可以在这里处理
        todayMenus.value = response.data.map((menu) => ({
          ...menu,
          status: menu.status === 'active' ? 'online' : 'offline',
          // 暂时设置dishes为0，后面需要实现获取菜品数量的接口
          dishes: 0,
          // 格式转换：LocalDateTime to String
          updateTime: menu.updateTime ? menu.updateTime.replace('T', ' ') : '',
          autoOnline: menu.autoStartTime ? menu.autoStartTime.replace('T', ' ') : '',
          autoOffline: menu.autoEndTime ? menu.autoEndTime.replace('T', ' ') : ''
        }))
        // 初始化筛选后的菜单
        filteredMenus.value = [...todayMenus.value]
      }
    })
    .catch((error) => {
      console.error('获取今日菜单数据失败:', error)
    })
}

// 当前选中的菜单
const selectedMenu = ref(null)
// 当前菜单的菜品
const currentMenuDishes = ref([])

// 筛选后的菜单
const filteredMenus = ref([...todayMenus.value])

// 菜单类型筛选
const activeMenuTypeFilter = ref('all')

// 切换菜单
const switchMenu = (menu) => {
  selectedMenu.value = menu
  currentMenuDishes.value = dishData[menu.name] || []
}

// 筛选菜单：先按类型，再按状态
const filterMenus = (filterType, filterCategory = 'status') => {
  if (filterCategory === 'status') {
    activeMenuFilter.value = filterType
  } else if (filterCategory === 'type') {
    activeMenuTypeFilter.value = filterType
  }

  // 组合筛选
  filteredMenus.value = todayMenus.value.filter((menu) => {
    // 状态筛选
    const statusMatch =
      activeMenuFilter.value === 'all' ? true : menu.status === activeMenuFilter.value

    // 类型筛选
    let typeMatch = true
    if (activeMenuTypeFilter.value !== 'all') {
      const menuType = menu.name.replace('菜单', '') // 从名称中提取类型
      typeMatch = menuType.includes(activeMenuTypeFilter.value)
    }

    return statusMatch && typeMatch
  })

  // 如果当前选中的菜单不在筛选结果中，重置选择
  if (
    selectedMenu.value &&
    !filteredMenus.value.some((menu) => menu.id === selectedMenu.value.id)
  ) {
    selectedMenu.value = null
    currentMenuDishes.value = []
  }
}

// 编辑菜品
const editDish = (dish) => {
  // 导航到菜品编辑页面
  router.push({
    path: '/merchant/home/dish-edit',
    query: { dishId: dish.id, menuName: selectedMenu.value.name }
  })
}

// 切换菜品状态
const toggleDishStatus = (dish) => {
  // 计算新状态
  const currentStatus = dish.status
  let newStatus

  // 根据当前状态确定新状态
  if (currentStatus === 'online' || currentStatus === 'almost_sold') {
    newStatus = 'offline'
  } else if (currentStatus === 'offline') {
    // 如果下架状态切换回上架，检查库存
    newStatus = dish.stock <= 10 ? 'almost_sold' : 'online'
  }

  // 更新本地状态
  const oldStatusText =
    currentStatus === 'online' ? '上架' : currentStatus === 'almost_sold' ? '即将售罄' : '下架'
  const newStatusText =
    newStatus === 'online' ? '上架' : newStatus === 'almost_sold' ? '即将售罄' : '下架'

  // 调用API更新菜品状态
  const updateData = {
    dishId: dish.id,
    status: newStatus
  }

  api
    .put(API_CONFIG.merchant.updateDishStatus.replace('{dishId}', dish.id), updateData)
    .then((response) => {
      if (response.data && response.data.success) {
        // 更新本地状态
        dish.status = newStatus
        ElMessage.success(`菜品 ${dish.name} 已从${oldStatusText}状态切换为${newStatusText}状态`)
      }
    })
    .catch((error) => {
      console.error('切换菜品状态失败:', error)
      ElMessage.error('切换菜品状态失败')
    })
}

// 页面加载
onMounted(() => {
  ElMessage.success('欢迎进入商家中心')
  // 调用后端API获取今日营业概览数据

  // 获取营业概览
  api
    .get(`/v1/merchant/${merchantId}/business-overview`)
    .then((response) => {
      if (response.code === '200' && response.data) {
        businessOverview.value = response.data
      }
    })
    .catch((error) => {
      console.error('获取营业概览数据失败:', error)
      // 如果获取失败，保留模拟数据
    })

  // 获取订单列表
  api
    .get(`/v1/orders/merchant/${merchantId}`)
    .then((response) => {
      if (response.code === '200' && response.data) {
        allOrders.value = response.data
        // 默认显示今日订单
        filterOrders('today')
      }
    })
    .catch((error) => {
      console.error('获取订单列表失败:', error)
      allOrders.value = []
      filteredOrders.value = []
    })

  // 获取商家信息
  api
    .get(`/v1/merchant/${merchantId}`)
    .then((response) => {
      if (response.code === '200' && response.data) {
        merchantInfo.value = response.data
      }
    })
    .catch((error) => {
      console.error('获取商家信息失败:', error)
      // 如果获取失败，使用默认模拟数据
      merchantInfo.value = {
        name: 'XX餐厅',
        rating: '4.8/5.0',
        phone: '138XXXX8888',
        email: 'xx@jaseats.com',
        address: '北京市朝阳区XX路123号'
      }
    })

  // 获取今日菜单数据
  fetchTodayMenus()
})

// onUnmounted(() => {
//   ElMessage.success('欢迎下次再来');
// });
</script>

<template>
  <div class="merchant-home-container" v-if="$route.path === '/merchant/home'">
    <div class="merchant-content">
      <!-- 商家信息 -->
      <div class="merchant-info-card">
        <div class="info-header">
          <div class="avatar-section">
            <span class="avatar">📸</span>
            <!-- <el-button type="primary" size="small" class="edit-btn">🔧 编辑资料</el-button> -->
          </div>
          <div class="detail-section">
            <div class="merchant-name">🏪 {{ merchantInfo.name }}</div>
            <div class="merchant-rating">🌟 {{ merchantInfo.rating }}</div>
            <div class="contact-info">
              <span class="phone">📞 {{ merchantInfo.phone }}</span>
              <span class="email">📧 {{ merchantInfo.email }}</span>
              <span class="address">📍 {{ merchantInfo.address }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 今日营业概览 -->
      <div class="overview-card">
        <h3 class="card-title">📈 今日营业概览：</h3>
        <div class="overview-grid">
          <div
            v-for="item in overviewConfig"
            :key="item.key"
            class="overview-item"
            :class="item.key"
            @click="item.onClick"
          >
            <div class="item-icon">{{ item.icon }}</div>
            <div class="item-content">
              <div class="overview-label">{{ item.label }}</div>
              <div class="overview-value">
                {{ item.suffix || '' }}{{ item.key === 'sales' ? businessOverview.sales.toFixed(0) : businessOverview[item.key] }}
              </div>
              <div class="item-trend" :class="item.trendClass">{{ item.trend }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 订单中心 -->
      <div class="orders-card">
        <div class="orders-header">
          <h3 class="card-title">📋 订单中心</h3>
          <div class="filter-section">
            <el-tag
              type="primary"
              effect="plain"
              class="filter-tag"
              :class="{ active: activeFilter === 'today' }"
              @click="filterOrders('today')"
              >今日订单</el-tag
            >
            <el-tag
              type="primary"
              effect="plain"
              class="filter-tag"
              :class="{ active: activeFilter === 'week' }"
              @click="filterOrders('week')"
              >本周订单</el-tag
            >
            <el-tag
              type="primary"
              effect="plain"
              class="filter-tag"
              :class="{ active: activeFilter === 'month' }"
              @click="filterOrders('month')"
              >本月订单</el-tag
            >
            <el-tag
              type="primary"
              effect="plain"
              class="filter-tag"
              :class="{ active: activeFilter === 'all' }"
              @click="filterOrders('all')"
              >全部订单</el-tag
            >
          </div>
        </div>

        <div class="orders-list">
          <div class="order-item" v-for="order in filteredOrders" :key="order.id">
            <div class="order-info">
              <div class="order-no">🍽️ 订单号：{{ order.id }}</div>
              <div class="order-details">
                <span class="amount"
                  >¥{{ order.totalAmount ? order.totalAmount.toFixed(2) : '0.00' }}</span
                >
                <span class="time">⏱️ {{ order.createTime }}</span>
                <el-tag
                  :type="
                    order.status === 1 || order.status === 2 || order.status === 3
                      ? 'info'
                      : order.status === 4
                        ? 'warning'
                        : order.status === 5
                          ? 'success'
                          : 'danger'
                  "
                >
                  {{ orderStatusMap[order.status] || '未知状态' }}
                </el-tag>
              </div>
            </div>
            <div class="order-actions">
              <el-button type="primary" size="small" @click="viewOrderDetails(order)"
                >🔍 详情</el-button
              >
              <el-button type="success" size="small" @click="updateOrderStatus(order)"
                >⏱️ 更新状态</el-button
              >
              <el-button type="warning" size="small" @click="notifyUser(order)"
                >🔔 通知用户</el-button
              >
            </div>
          </div>
          <div v-if="filteredOrders.length === 0" class="no-orders">
            <p>后端数据为空，当前没有订单</p>
          </div>
        </div>

        <div class="view-all">
          <el-button type="text" @click="navigateToOrders">📤 查看全部订单</el-button>
        </div>
      </div>

      <!-- 今日菜单 -->
      <div class="quick-actions-card today-menu-card">
        <div class="menu-header">
          <h3 class="card-title">📋 今日菜单</h3>
          <div class="filter-section">
            <span class="filter-label">分类：</span>
            <el-tag
              v-for="category in menuCategories"
              :key="category"
              type="primary"
              effect="plain"
              class="filter-tag"
              :class="{ active: activeMenuTypeFilter === category }"
              @click="filterMenus(category, 'type')"
              >{{ category }}</el-tag
            >
          </div>
        </div>

        <div class="menu-header">
          <div class="filter-section">
            <span class="filter-label">状态：</span>
            <el-tag
              v-for="status in menuStatuses"
              :key="status.value"
              type="primary"
              effect="plain"
              class="filter-tag"
              :class="{ active: activeMenuFilter === status.value }"
              @click="filterMenus(status.value, 'status')"
              >{{ status.label }}</el-tag
            >
          </div>
        </div>

        <div class="menu-list">
          <div
            class="menu-item"
            v-for="menu in filteredMenus"
            :key="menu.id"
            :class="{ active: selectedMenu?.id === menu.id }"
            @click="switchMenu(menu)"
          >
            <div class="menu-info">
              <div class="menu-name">
                <span class="name">{{ menu.name }}</span>
                <el-tag :type="menuStatusMap[menu.status].type">
                  {{ menuStatusMap[menu.status].icon }}
                  {{ menuStatusMap[menu.status].text }}
                </el-tag>
              </div>

              <div class="menu-stats">
                <span class="dishes-count">🍴 {{ menu.dishes }} 菜品</span>
                <span class="update-time">⏰ 更新时间：{{ menu.updateTime }}</span>
              </div>

              <div class="auto-times">
                <span v-if="menu.autoOnline" class="auto-online">
                  ⏰ 自动上架：{{ menu.autoOnline }}
                </span>
                <span v-if="menu.autoOffline" class="auto-offline">
                  ⏰ 自动下架：{{ menu.autoOffline }}
                </span>
              </div>
            </div>
          </div>
          <!-- 空菜单提示 -->
          <div v-if="filteredMenus.length === 0" class="empty-menu">
            <span>🍽️ 今日咱未设置菜单</span>
          </div>
        </div>

        <div class="view-all">
          <el-button type="text" @click="navigateToMenu">📤 查看全部菜单</el-button>
        </div>
      </div>

      <!-- 菜品列表 -->
      <div v-if="selectedMenu" class="quick-actions-card dishes-card">
        <div class="menu-header">
          <h3 class="card-title">🍽️ {{ selectedMenu.name }} - 菜品列表</h3>
        </div>

        <div class="dish-list">
          <div class="dish-item" v-for="dish in currentMenuDishes" :key="dish.id">
            <div class="dish-cover">
              {{ dish.image || '🍱' }}
            </div>
            <div class="dish-info">
              <div class="dish-name">
                <span class="name">{{ dish.name }}</span>
                <el-tag :type="dishStatusMap[dish.status].type" size="small">
                  {{ dishStatusMap[dish.status].text }}
                </el-tag>
              </div>

              <div class="dish-desc">
                {{ dish.description || '美味可口，欢迎品尝' }}
              </div>

              <div class="dish-stats">
                <span class="dish-category">📁 {{ dish.category }}</span>
                <span class="dish-price">💰 ¥{{ dish.price }}</span>
                <span
                  class="dish-stock"
                  :class="{
                    'stock-almost': dish.status === 'almost_sold',
                    'stock-off': dish.status === 'offline'
                  }"
                >
                  {{
                    dish.status === 'almost_sold'
                      ? '⏳ 即将售罄'
                      : dish.status === 'offline'
                        ? '❌ 已下架'
                        : `📦 ${dish.stock} 份`
                  }}
                </span>
              </div>
            </div>
            <div class="dish-actions">
              <el-button type="primary" size="small" @click="editDish(dish)"> ✏️ 编辑 </el-button>
              <el-button
                :type="dish.status === 'online' ? 'warning' : 'success'"
                size="small"
                @click="toggleDishStatus(dish)"
              >
                {{ dish.status === 'online' ? '🔴 下架' : '🟢 上架' }}
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 优惠管理部分 -->
      <div class="discounts-section">
        <div class="discounts-header">
          <div class="discount-title">
            <h3 class="card-title">💰 优惠活动管理</h3>
            <div class="active-discounts">{{ discounts.length }}个活动</div>
          </div>
          <div class="discount-actions">
            <el-button type="primary" size="small" @click="openDiscountDialog()">
              <el-icon><Plus /></el-icon> 添加优惠
            </el-button>
            <el-button
              type="warning"
              size="small"
              @click="batchDeleteDiscounts()"
              :disabled="selectedDiscounts.length === 0"
            >
              批量删除
            </el-button>
          </div>
        </div>
        <div class="discounts-table-container">
          <el-table
            :data="discounts"
            :default-sort="{ prop: 'createdTime', order: 'descending' }"
            @selection-change="(selection) => (selectedDiscounts = selection)"
          >
            <el-table-column type="selection" width="50" />
            <el-table-column prop="name" label="优惠名称" min-width="120" />
            <el-table-column prop="type" label="优惠类型" width="100" />
            <el-table-column prop="description" label="优惠描述" min-width="200" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.status === 'active' ? 'success' : 'warning'">
                  {{ scope.row.status === 'active' ? '已启用' : '已禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdTime" label="创建时间" width="160" />
            <el-table-column prop="updatedTime" label="更新时间" width="160" />
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="scope">
                <el-button type="primary" size="small" @click="openDiscountDialog(scope.row)">
                  编辑
                </el-button>
                <el-button type="danger" size="small" @click="() => deleteDiscount(scope.row)">
                  删除
                </el-button>
              </template>
            </el-table-column>

            <!-- 自定义空数据提示 -->
            <template #empty>
              <div class="empty-state">
                <span class="el-icon-info"></span>
                <p>暂无优惠活动，请点击右上角"添加优惠"创建</p>
              </div>
            </template>
          </el-table>
        </div>
      </div>

      <!-- 店铺相册 -->
      <div class="shop-album-card">
        <div class="album-header">
          <h4 class="card-title">📸 店铺相册</h4>
          <el-button type="primary" size="small" @click="openFullAlbumPreview">
            <el-icon class="el-icon-full-screen"></el-icon> 放大预览
          </el-button>
        </div>
        <div class="album-stats">
          <span class="stat-item">🔍 店铺环境 ({{ shopAlbum.environment.length }}张)</span>
          <span class="stat-item">🍽️ 菜品展示 ({{ shopAlbum.dishes.length }}张)</span>
        </div>

        <!-- 店铺环境图片 -->
        <div class="album-section">
          <h5 class="section-title">店铺环境</h5>
          <div class="album-grid">
            <div
              v-for="(image, index) in shopAlbum.environment"
              :key="`env-${index}`"
              class="album-item"
            >
              <div class="album-item-overlay">
                <el-button
                  type="danger"
                  size="small"
                  class="delete-img-btn"
                  @click.stop="deleteAlbumImage('environment', index)"
                >
                  <el-icon class="el-icon-delete"></el-icon>
                </el-button>
              </div>
              <el-image :src="image" :preview-src-list="shopAlbum.environment" fit="cover" />
            </div>
          </div>
          <!-- 美化的空状态提示 -->
          <div v-if="shopAlbum.environment.length === 0" class="album-empty-beautify">
            <el-icon class="empty-icon el-icon-picture-outline"></el-icon>
            <p class="empty-text">暂无店铺环境图片</p>
            <p class="empty-subtext">点击下方上传按钮添加图片</p>
          </div>
        </div>

        <!-- 菜品展示图片 -->
        <div class="album-section">
          <h5 class="section-title">菜品展示</h5>
          <div class="album-grid">
            <div
              v-for="(image, index) in shopAlbum.dishes"
              :key="`dish-${index}`"
              class="album-item"
            >
              <div class="album-item-overlay">
                <el-button
                  type="danger"
                  size="small"
                  class="delete-img-btn"
                  @click.stop="deleteAlbumImage('dishes', index)"
                >
                  <el-icon class="el-icon-delete"></el-icon>
                </el-button>
              </div>
              <el-image :src="image" :preview-src-list="shopAlbum.dishes" fit="cover" />
            </div>
          </div>
          <!-- 美化的空状态提示 -->
          <div v-if="shopAlbum.dishes.length === 0" class="album-empty-beautify">
            <el-icon class="empty-icon el-icon-food"></el-icon>
            <p class="empty-text">暂无菜品展示图片</p>
            <p class="empty-subtext">点击下方上传按钮添加图片</p>
          </div>
        </div>

        <!-- 上传按钮及相册选择 -->
        <div class="upload-section">
          <div class="upload-select">
            <label class="upload-label">选择相册：</label>
            <el-select
              v-model="uploadAlbumType"
              placeholder="请选择相册类型"
              size="small"
              style="width: 180px"
            >
              <el-option label="店铺环境" value="environment" />
              <el-option label="菜品展示" value="dishes" />
            </el-select>
          </div>

          <!-- 照片上传组件 -->
          <div class="upload-button">
            <el-upload
              action="#"
              list-type="picture-card"
              :on-change="handleUpload"
              :on-remove="handleUploadRemove"
              :auto-upload="false"
              :file-list="imageUploadList"
            >
              <el-icon class="el-icon-plus"></el-icon>
              <div class="el-upload__text">上传照片</div>
            </el-upload>

            <!-- 上传确认按钮 -->
            <el-button
              type="success"
              size="small"
              class="upload-confirm-btn"
              @click="confirmUpload"
              :disabled="imageUploadList.length === 0"
            >
              <el-icon class="el-icon-check"></el-icon> 确认上传
            </el-button>
          </div>
          <div class="el-upload__tip">仅支持 JPG/PNG 格式，单张不超过 5MB</div>
        </div>
      </div>

      <!-- 全屏相册预览对话框 -->
      <el-dialog v-model="fullAlbumPreviewVisible" title="店铺相册全屏预览" width="90%" top="5%">
        <div class="full-album-preview">
          <el-image-viewer
            v-if="fullAlbumPreviewVisible"
            :url-list="getAllImages"
            @close="fullAlbumPreviewVisible = false"
          />
        </div>
      </el-dialog>

      <!-- 公告栏配置 -->
      <div class="announcement-section">
        <div class="announcement-header">
          <h3 class="card-title">📢 公告栏管理</h3>
          <el-button type="primary" size="small" @click="openAnnouncementDialog()">
            <el-icon><Plus /></el-icon> 添加公告
          </el-button>
        </div>
        <div class="announcement-table-container">
          <el-table
            :data="announcements"
            :default-sort="{ prop: 'createdTime', order: 'descending' }"
          >
            <el-table-column prop="title" label="公告标题" min-width="200" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.status === 'active' ? 'success' : 'warning'">
                  {{ scope.row.status === 'active' ? '已启用' : '已禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="startTime" label="开始时间" width="180" />
            <el-table-column prop="endTime" label="结束时间" width="180" />
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="scope">
                <el-button type="primary" size="small" @click="openAnnouncementDialog(scope.row)">
                  编辑
                </el-button>
                <el-button
                  :type="scope.row.status === 'active' ? 'warning' : 'success'"
                  size="small"
                  @click="toggleAnnouncementStatus(scope.row)"
                >
                  {{ scope.row.status === 'active' ? '禁用' : '启用' }}
                </el-button>
                <el-button type="danger" size="small" @click="() => deleteAnnouncement(scope.row)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 空数据提示 -->
          <div v-if="announcements.length === 0" class="empty-state">
            <span class="el-icon-info" />
            <p>暂无公告，请点击右上角"添加公告"创建</p>
          </div>
        </div>
      </div>

      <!-- 公告编辑对话框 -->
      <el-dialog
        v-model="announcementDialogVisible"
        :title="isEditingAnnouncement ? '编辑公告' : '添加公告'"
        width="600px"
        top="10%"
      >
        <el-form :model="currentAnnouncement" label-width="100px" status-icon>
          <el-form-item label="公告标题" prop="title" required>
            <el-input v-model="currentAnnouncement.title" placeholder="请输入公告标题" />
          </el-form-item>
          <el-form-item label="公告内容" prop="content" required>
            <el-input
              v-model="currentAnnouncement.content"
              placeholder="请输入公告内容"
              type="textarea"
              :rows="4"
            />
          </el-form-item>
          <el-form-item label="状态" prop="status" required>
            <el-select v-model="currentAnnouncement.status" placeholder="请选择公告状态">
              <el-option label="已启用" value="active" />
              <el-option label="已禁用" value="inactive" />
            </el-select>
          </el-form-item>
          <el-form-item label="开始时间" prop="startTime">
            <el-date-picker
              v-model="currentAnnouncement.startTime"
              type="datetime"
              placeholder="选择开始时间"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="结束时间" prop="endTime">
            <el-date-picker
              v-model="currentAnnouncement.endTime"
              type="datetime"
              placeholder="选择结束时间"
              style="width: 100%"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="announcementDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="saveAnnouncement">确定</el-button>
          </span>
        </template>
      </el-dialog>

      <!-- 快捷操作 -->
      <div class="quick-actions-card">
        <h3 class="card-title">🎯 快捷操作：</h3>
        <div class="actions-grid">
          <div class="action-item" @click="navigateToMenu">
            <div class="action-icon">➕</div>
            <div class="action-label">新增菜单</div>
          </div>
          <div class="action-item" @click="setDiscount">
            <div class="action-icon">💰</div>
            <div class="action-label">设置优惠</div>
          </div>
          <div class="action-item" @click="contactCustomerService">
            <div class="action-icon">📞</div>
            <div class="action-label">联系客服</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="less">
.merchant-home-container {
  padding: 0 20px 20px 20px;

  .merchant-info-card {
    margin-bottom: 24px;
    padding: 24px; /* 添加内边距 */
    border: 2px solid #409eff; /* 使用Element Plus主色 */
    border-radius: 12px; /* 增加圆角 */
    background-color: #ffffff; /* 白色背景 */
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08); /* 增强阴影效果 */

    .info-header {
      display: flex;
      align-items: center;
      gap: 20px;

      .avatar-section {
        .avatar {
          font-size: 64px;
        }
        .edit-btn {
          margin-top: 10px;
        }
      }

      .detail-section {
        flex: 1;

        .merchant-name {
          font-size: 20px;
          font-weight: 600;
          margin-bottom: 8px;
        }

        .merchant-rating {
          margin-bottom: 8px;
        }

        .contact-info {
          display: flex;
          flex-wrap: wrap;
          gap: 20px;
          font-size: 14px;
          color: #606266;
        }
      }
    }
  }

  .overview-card {
    margin-bottom: 24px;
    padding: 24px;
    border: 2px solid #67c23a; /* 使用成功绿 */
    border-radius: 12px;
    background-color: #ffffff;
    box-shadow: 0 4px 20px rgba(103, 194, 58, 0.12);

    .card-title {
      font-size: 20px;
      font-weight: 700;
      margin-bottom: 20px;
      color: #e6a23c;
      display: flex;
      align-items: center;

      &::after {
        content: '';
        flex: 1;
        height: 1px;
        background: linear-gradient(to right, #e6a23c, transparent);
        margin-left: 15px;
      }
    }

    .overview-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
      gap: 20px;

      .overview-item {
        display: flex;
        align-items: center;
        gap: 16px;
        padding: 20px;
        border-radius: 12px;
        background: white;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
        transition: all 0.3s ease;
        cursor: pointer;
        border: 1px solid #f0f0f0;

        &:hover {
          transform: translateY(-5px);
          box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
          border-color: #ffd7a3;
        }

        &.sales {
          border-left: 4px solid #67c23a;

          &:hover {
            border-left: 4px solid #67c23a;
          }
        }

        &.orders {
          border-left: 4px solid #409eff;

          &:hover {
            border-left: 4px solid #409eff;
          }
        }

        &.comments {
          border-left: 4px solid #e6a23c;

          &:hover {
            border-left: 4px solid #e6a23c;
          }
        }

        &.messages {
          border-left: 4px solid #f56c6c;

          &:hover {
            border-left: 4px solid #f56c6c;
          }
        }

        .item-icon {
          font-size: 32px;
          width: 60px;
          height: 60px;
          display: flex;
          align-items: center;
          justify-content: center;
          border-radius: 50%;
          background: rgba(230, 162, 60, 0.1);
        }

        .item-content {
          flex: 1;

          .overview-label {
            font-size: 14px;
            color: #909399;
            margin-bottom: 4px;
            font-weight: 500;
          }

          .overview-value {
            font-size: 24px;
            font-weight: 700;
            margin-bottom: 4px;
          }

          .item-trend {
            font-size: 12px;
            font-weight: 600;

            &.trend-up {
              color: #67c23a;
            }

            &.trend-down {
              color: #f56c6c;
            }

            &.trend-neutral {
              color: #909399;
            }
          }
        }
      }
    }
  }

  .orders-card {
    margin-bottom: 24px;
    padding: 24px; /* 添加内边距 */
    border: 2px solid #409eff; /* 加强边框 */
    border-radius: 12px; /* 统一圆角 */
    background-color: #ffffff; /* 白色背景 */
    box-shadow: 0 4px 20px rgba(64, 158, 255, 0.1); /* 增强阴影 */

    .orders-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;

      .card-title {
        font-size: 18px;
        font-weight: 600;
        margin: 0;
      }

      .filter-section {
        .filter-tag {
          margin-right: 10px;
          cursor: pointer; // 添加鼠标悬浮点击样式

          &.active {
            color: #409eff;
            background-color: rgba(64, 158, 255, 0.1);
          }

          &:first-child {
            // 今日订单样式优化
            border-left: 3px solid #67c23a;
            padding-left: 8px;

            &.active {
              background-color: rgba(103, 194, 58, 0.1);
              color: #67c23a;
            }
          }
        }
      }
    }

    .orders-list {
      max-height: 400px;
      overflow-y: auto;
      padding-right: 8px;

      .no-orders {
        text-align: center;
        padding: 80px 0;
        color: #909399;
        font-size: 16px;
      }

      .order-item {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        padding: 16px;
        border: 1px solid #e4e7ed;
        border-radius: 4px;
        margin-bottom: 12px;

        .order-info {
          .order-no {
            font-weight: 600;
            margin-bottom: 8px;
          }

          .order-details {
            display: flex;
            flex-wrap: wrap;
            gap: 16px;
            font-size: 14px;

            .amount {
              font-weight: 600;
            }
          }
        }

        .order-actions {
          display: flex;
          gap: 8px;
          flex-wrap: wrap;
        }
      }
    }

    .view-all {
      text-align: right;
      margin-top: 12px;
    }
  }

  .quick-actions-card {
    margin-bottom: 24px;
    padding: 24px; /* 添加内边距 */
    border: 2px solid #e6a23c; /* 使用警告橙 */
    border-radius: 12px; /* 统一圆角 */
    background-color: #ffffff; /* 白色背景 */
    box-shadow: 0 4px 20px rgba(230, 162, 60, 0.1); /* 增强阴影 */

    .card-title {
      font-size: 18px;
      font-weight: 600;
      margin-bottom: 16px;
    }

    .actions-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
      gap: 20px;

      .action-item {
        display: flex;
        flex-direction: column;
        align-items: center;
        padding: 24px;
        border: 1px solid #e4e7ed;
        border-radius: 4px;
        cursor: pointer;
        transition: all 0.3s;

        &:hover {
          box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
        }

        .action-icon {
          font-size: 48px;
          margin-bottom: 8px;
        }

        .action-label {
          font-size: 14px;
          font-weight: 500;
        }
      }
    }

    // 今日菜单
    .today-menu-card {
      margin-bottom: 24px;
      padding: 24px; /* 添加内边距 */
      border: 2px solid #67c23a; /* 绿色主题边框 */
      border-radius: 12px; /* 统一圆角 */
      background-color: #ffffff; /* 白色背景 */
      box-shadow: 0 4px 20px rgba(103, 194, 58, 0.08); /* 增强阴影 */

      .menu-header {
        display: flex;
        justify-content: flex-start;
        align-items: center;
        margin-bottom: 28px; /* 增加底部间距 */
        flex-wrap: wrap;
        gap: 24px; /* 增加整体间距 */

        // 处理只有标题的情况 (第一行)
        &:has(.card-title) {
          padding-bottom: 16px; /* 添加底部内边距 */
          border-bottom: 1px solid #f0f9eb; /* 添加分隔线 */
          margin-bottom: 24px; /* 调整标题行与筛选行的间距 */
        }

        .card-title {
          font-size: 20px;
          font-weight: 700;
          margin: 0;
          color: #67c23a; /* 绿色主题标题 */
        }

        .filter-label {
          font-weight: 600; /* 加粗标签 */
          margin-right: 12px; /* 增加标签右侧间距 */
          color: #606266;
          font-size: 14px;
        }

        .filter-section {
          display: flex;
          align-items: center;
          gap: 14px; /* 增加标签之间的间距 */
          flex-wrap: wrap;

          .filter-tag {
            cursor: pointer;
            transition: all 0.3s ease;
            padding: 6px 14px; /* 增加标签内边距，让标签更舒展 */
            border-radius: 20px; /* 圆角优化，让标签更柔和 */

            &:hover {
              transform: translateY(-2px);
              box-shadow: 0 2px 8px rgba(103, 194, 58, 0.2); /* 绿色主题阴影 */
            }

            &.active {
              transform: translateY(-1px);
            }
          }
        }
      }

      .menu-list {
        margin-bottom: 20px;

        .menu-item {
          padding: 20px;
          border: 2px solid #eaf5ec; /* 淡绿色边框 */
          border-radius: 10px;
          margin-bottom: 16px;
          background-color: #fff;
          transition: all 0.3s ease;
          cursor: pointer;

          &:hover {
            box-shadow: 0 4px 16px rgba(103, 194, 58, 0.12); /* 绿色主题阴影 */
            border-color: #67c23a;
            transform: translateY(-4px);
          }

          &.active {
            border-color: #67c23a;
            box-shadow: 0 4px 16px rgba(103, 194, 58, 0.15);
            background-color: #f0f9eb; /* 淡绿色背景 */
          }

          .menu-info {
            .menu-name {
              display: flex;
              align-items: center;
              gap: 12px;
              margin-bottom: 16px;

              .name {
                font-size: 18px;
                font-weight: 600;
                color: #303133;
              }
            }

            .menu-stats,
            .auto-times {
              display: flex;
              flex-wrap: wrap;
              gap: 24px;
              margin-bottom: 8px;
              font-size: 14px;

              .dishes-count {
                color: #67c23a;
                font-weight: 500;
              }
            }

            .auto-times {
              font-size: 13px;
              color: #909399;
            }
          }
        }

        .empty-menu {
          text-align: center;
          padding: 80px 20px; /* 增加上下内边距 */
          color: #909399;
          font-size: 18px;
          background-color: #f7fff9; /* 淡绿色背景 */
          border: 2px dashed #67c23a; /* 绿色虚线边框 */
          border-radius: 12px;
          margin-bottom: 28px; /* 与其他元素保持一致的间距 */
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05); /* 轻微阴影 */
          transition: all 0.3s ease; /* 平滑过渡效果 */

          &:hover {
            box-shadow: 0 4px 16px rgba(103, 194, 58, 0.1); /* 悬停时增强阴影 */
            background-color: #eaf5ec; /* 悬停时加深背景色 */
          }

          span {
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 8px; /* 文字和图标间距 */
          }
        }
      }

      .view-all {
        text-align: right;
        margin-top: 24px;

        .el-button {
          color: #67c23a;
          border-color: #67c23a;
          transition: all 0.3s ease;

          &:hover {
            background-color: #67c23a;
            color: #fff;
            transform: translateX(4px);
          }
        }
      }
    }

    // 菜品列表样式
    .dishes-card {
      margin-bottom: 24px;
      padding: 24px; /* 添加内边距 */
      border: 2px solid #67c23a; /* 绿色边框 */
      border-radius: 12px; /* 统一圆角 */
      background-color: #ffffff; /* 白色背景 */
      box-shadow: 0 4px 20px rgba(103, 194, 58, 0.08); /* 增强阴影 */
      border-top: none;
      border-top-left-radius: 0;
      border-top-right-radius: 0;

      .dish-list {
        margin-bottom: 20px;

        .dish-item {
          padding: 20px;
          border: 2px solid #f0f9eb; /* 淡绿色边框 */
          border-radius: 10px;
          margin-bottom: 16px;
          background-color: #fff;
          transition: all 0.3s ease;
          display: flex;
          align-items: flex-start;
          gap: 16px;
          overflow: hidden;

          &:hover {
            box-shadow: 0 4px 16px rgba(103, 194, 58, 0.12); /* 绿色主题阴影 */
            border-color: #67c23a;
            transform: translateY(-4px);
          }

          .dish-cover {
            font-size: 48px;
            width: 90px;
            height: 90px;
            display: flex;
            align-items: center;
            justify-content: center;
            background: linear-gradient(135deg, #67c23a, #eaf5ec); /* 绿色渐变背景 */
            border-radius: 10px;
            flex-shrink: 0;
            color: #fff;
            box-shadow: 0 2px 8px rgba(103, 194, 58, 0.2);
            transition: all 0.3s ease;
          }

          &:hover .dish-cover {
            transform: scale(1.1);
          }

          .dish-info {
            flex: 1;

            .dish-name {
              display: flex;
              align-items: center;
              gap: 12px;
              margin-bottom: 10px;

              .name {
                font-size: 18px;
                font-weight: 600;
                color: #303133;
              }
            }

            .dish-desc {
              font-size: 14px;
              color: #606266;
              margin-bottom: 14px;
              line-height: 1.6;
            }

            .dish-stats {
              display: flex;
              flex-wrap: wrap;
              gap: 20px;
              font-size: 14px;
              color: #606266;

              .dish-category {
                background-color: #eaf5ec;
                color: #67c23a;
                padding: 4px 12px;
                border-radius: 6px;
                font-size: 12px;
                font-weight: 500;
              }

              .dish-price {
                color: #e6a23c;
                font-weight: 600;
                font-size: 16px;
              }

              .dish-stock {
                font-size: 13px;
                font-weight: 500;

                &.stock-almost {
                  color: #f59f00;
                }

                &.stock-off {
                  color: #f56c6c;
                }
              }
            }
          }

          .dish-actions {
            display: flex;
            flex-direction: column;
            gap: 10px;
            flex-shrink: 0;

            .el-button {
              width: 90px;
              transition: all 0.3s ease;

              &:hover {
                transform: translateY(-2px);
              }
            }
          }
        }
      }
    }
  }

  // 优惠活动管理
  .discounts-section {
    margin-bottom: 24px;
    padding: 24px;
    border: 2px solid #409eff; /* 主蓝色 */
    border-radius: 12px;
    background-color: #ffffff;
    box-shadow: 0 4px 20px rgba(64, 158, 255, 0.1);

    .discounts-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;

      .discount-title {
        .card-title {
          margin: 0;
          font-size: 20px;
          font-weight: 700;
        }

        .active-discounts {
          font-size: 14px;
          color: #909399;
          margin-top: 4px;
        }
      }

      .discount-actions {
        display: flex;
        gap: 12px;
      }
    }
  }

  // 店铺相册
  .shop-album-card {
    margin-bottom: 24px;
    padding: 24px;
    border: 2px solid #f56c6c; /* 错误红 */
    border-radius: 12px;
    background-color: #ffffff;
    box-shadow: 0 4px 20px rgba(245, 108, 108, 0.1);

    .album-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;

      .card-title {
        margin: 0;
        font-size: 20px;
        font-weight: 700;
      }
    }

    .album-stats {
      display: flex;
      gap: 32px;
      margin-bottom: 24px;
      font-size: 16px;
      color: #606266;

      .stat-item {
        display: flex;
        align-items: center;
        gap: 8px;
      }
    }

    .album-section {
      margin-bottom: 24px;

      .section-title {
        font-size: 16px;
        font-weight: 600;
        margin-bottom: 16px;
        color: #303133;
        padding-bottom: 8px;
        border-bottom: 2px solid #f56c6c;
        width: fit-content;
      }

      .album-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
        gap: 16px;
        margin-bottom: 12px;
      }

      .album-item {
        position: relative;
        border-radius: 8px;
        overflow: hidden;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        transition: all 0.3s ease;
        cursor: pointer;

        &:hover {
          transform: translateY(-4px);
          box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
        }

        .album-item-overlay {
          position: absolute;
          top: 8px;
          right: 8px;
          opacity: 0;
          transition: opacity 0.3s ease;
        }

        &:hover .album-item-overlay {
          opacity: 1;
        }

        .delete-img-btn {
          border-radius: 50%;
          width: 32px;
          height: 32px;
          padding: 0;
          display: flex;
          align-items: center;
          justify-content: center;
          box-shadow: 0 2px 8px rgba(245, 108, 108, 0.3);
        }

        :deep(.el-image) {
          width: 100%;
          height: 140px;
          object-fit: cover;
        }
      }
    }

    // 美化的空状态样式
    .album-empty-beautify {
      text-align: center;
      padding: 60px 20px;
      background-color: #fafafa;
      border: 2px dashed #dcdfe6;
      border-radius: 8px;
      margin-top: 8px;
      transition: all 0.3s ease;

      &:hover {
        background-color: #f5f7fa;
        border-color: #c6e2ff;
      }

      .empty-icon {
        font-size: 48px;
        color: #c0c4cc;
        margin-bottom: 16px;
      }

      .empty-text {
        font-size: 16px;
        color: #303133;
        margin-bottom: 8px;
        font-weight: 500;
      }

      .empty-subtext {
        font-size: 14px;
        color: #909399;
      }
    }

    // 上传区域样式优化
    .upload-section {
      background-color: #fafafa;
      padding: 24px;
      border-radius: 8px;
      border: 1px solid #e4e7ed;
      margin-top: 24px;

      .upload-select {
        margin-bottom: 20px;

        .upload-label {
          font-weight: 500;
          color: #303133;
          margin-right: 12px;
        }
      }

      .upload-button {
        display: flex;
        align-items: center;
        gap: 16px;
        flex-wrap: wrap;
        margin-bottom: 16px;
      }
    }
  }

  // 公告栏配置
  .announcement-section {
    margin-bottom: 24px;
    padding: 24px;
    border: 2px solid #909399; /* 中性灰 */
    border-radius: 12px;
    background-color: #ffffff;
    box-shadow: 0 4px 20px rgba(144, 147, 153, 0.1);

    .announcement-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;

      .card-title {
        margin: 0;
        font-size: 20px;
        font-weight: 700;
      }
    }
  }
}
</style>
