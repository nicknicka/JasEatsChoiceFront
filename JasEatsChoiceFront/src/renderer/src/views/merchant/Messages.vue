<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../store/authStore'
import api, { decodeJwt } from '../../utils/api.js'
import { API_CONFIG } from '../../config/index.js'
import CommonBackButton from '../../components/common/CommonBackButton.vue'

const router = useRouter()

// 消息分类映射
const messageCategories = {
  all: '所有消息',
  system: '系统通知',
  order: '订单消息',
  comment: '评价消息'
}

// 消息数据，将从后端API获取
const messages = ref([])
const selectedMessage = ref(null)
const activeCategory = ref('all')

// 筛选后的消息
const filteredMessages = ref([])

// 未读消息统计
const unreadCounts = ref({
  system: 0,
  order: 0,
  comment: 0
})

// 计算未读消息数量
const calculateUnreadCounts = () => {
  unreadCounts.value = {
    system: messages.value.filter((msg) => msg.type === 'system' && !msg.isRead).length,
    order: messages.value.filter((msg) => msg.type === 'order' && !msg.isRead).length,
    comment: messages.value.filter((msg) => msg.type === 'comment' && !msg.isRead).length
  }
}

// 更新筛选
const updateFilter = () => {
  filteredMessages.value = messages.value.filter((message) => {
    return activeCategory.value === 'all' || message.type === activeCategory.value
  })
  calculateUnreadCounts() // 更新未读消息统计
}

// 页面加载时初始化
onMounted(() => {
  // 从后端API加载实际消息数据
  // 从JWT令牌中获取用户ID
  const authStore = useAuthStore()
  const token = authStore.token
  let userId = 1 // 默认值

  if (token) {
    const decodedToken = decodeJwt(token)
    if (decodedToken && decodedToken.userId) {
      userId = decodedToken.userId
    }
  } else {
    // 无法获取用户ID，弹出提示框要求重新登录
    ElMessageBox.alert('无法获取用户ID，请重新登录', '身份验证失败', {
      confirmButtonText: '重新登录',
      type: 'error',
      closeOnClickModal: false,
      closeOnPressEscape: false
    })
      .then(() => {
        // 用户点击重新登录按钮，清除本地存储并跳转到登录页面
        const authStore = useAuthStore()
        authStore.clearAuth()
        router.push('/login')
      })
      .catch(() => {
        // 点击取消按钮的处理，也可以跳转到登录页面
        const authStore = useAuthStore()
        authStore.clearAuth()
        router.push('/login')
      })
  }

  api
    .get(API_CONFIG.message.list, {
      params: { userId }
    })
    .then((response) => {
      if (response.data && response.data.success) {
        // 转换后端返回的数据格式以匹配前端期望的字段
        const formattedMessages = response.data.data.map((message) => ({
          id: message.id,
          // 后端返回的content作为前端的title
          title: message.content,
          content: message.content,
          // 后端返回的senderName作为前端的sender
          sender: message.senderName,
          // 后端返回的createTime作为前端的time
          time: message.createTime,
          // 后端返回的readStatus作为前端的isRead
          isRead: message.readStatus,
          // 暂时默认所有消息类型为system
          type: 'system'
        }))

        messages.value = formattedMessages
        filteredMessages.value = [...messages.value]
        calculateUnreadCounts() // 初始化未读消息统计
      }
    })
    .catch((error) => {
      console.error('加载消息失败:', error)
      ElMessage.error('加载消息失败，请稍后重试')
    })
})

// 查看消息详情
const viewMessageDetail = (message) => {
  // 检查是否是订单消息，如果是则导航到订单详情页
  if (message.type === 'order') {
    // 从消息标题或内容中提取订单号
    const orderIdMatch = message.title.match(/订单号(?:JD)?(\\d+)/)
    if (orderIdMatch) {
      const orderId = orderIdMatch[1]
      router.push(`/merchant/home/order-detail/${orderId}`)
      return
    }
  }
  // 普通消息则显示详情
  selectedMessage.value = message
  // 自动标记为已读
  if (!message.isRead) {
    message.isRead = true
    ElMessage.success('消息已标记为已读')
    updateFilter() // 刷新筛选后的列表以更新状态
  }
}

// 返回消息列表
const backToList = () => {
  selectedMessage.value = null
}

// 标记为已读
const markAsRead = (message) => {
  message.isRead = true
  ElMessage.success('消息已标记为已读')
  updateFilter()
}

// 全部标记为已读
const markAllAsRead = () => {
  filteredMessages.value.forEach((message) => {
    message.isRead = true
  })
  ElMessage.success('所有消息已标记为已读')
  updateFilter()
}
</script>

<template>
  <div class="messages-management-container">
    <div class="messages-header">
      <div class="header-left">
        <h3 class="page-title">【消息中心】</h3>
      </div>
      <div class="header-right" v-if="!selectedMessage">
        <el-button type="success" @click="markAllAsRead"> 全部标记为已读 </el-button>
      </div>
    </div>

    <div class="messages-content">
      <!-- 消息分类与列表 -->
      <div class="messages-list-container" v-if="!selectedMessage">
        <div class="category-section">
          <span class="category-label">📋 消息分类：</span>
          <el-tag
            v-for="category in ['all', 'system', 'order', 'comment']"
            :key="category"
            :type="activeCategory === category ? 'primary' : 'info'"
            effect="plain"
            @click="() => {
              activeCategory = category;
              updateFilter();
            }"
            class="category-tag"
          >
            {{ messageCategories[category] }}
            <el-badge
              v-if="category !== 'all' && unreadCounts[category]"
              :value="unreadCounts[category]"
              type="danger"
            />
          </el-tag>
        </div>

        <div class="messages-list">
          <div
            v-for="message in filteredMessages"
            :key="message.id"
            class="message-item"
            :class="{ 'unread-message': !message.isRead }"
            @click="viewMessageDetail(message)"
          >
            <div class="message-icon">🔔</div>
            <div class="message-content">
              <div class="message-title">{{ message.title }}</div>
              <div class="message-meta">
                <span class="message-time">{{ message.time }}</span>
                <el-tag :type="message.isRead ? 'success' : 'warning'">
                  {{ message.isRead ? '🔘 已读' : '✅ 未读' }}
                </el-tag>
              </div>
            </div>
          </div>
        </div>

        <!-- 空数据提示 -->
        <div v-if="filteredMessages.length === 0" class="empty-messages">
          <el-empty description="暂无消息"></el-empty>
        </div>
      </div>

      <!-- 消息详情 -->
      <div class="message-detail-container" v-if="selectedMessage">
        <div class="detail-header">
          <div class="detail-title">
            <h3>{{ selectedMessage.title }}</h3>
            <el-tag :type="selectedMessage.isRead ? 'success' : 'warning'">
              {{ selectedMessage.isRead ? '已读' : '未读' }}
            </el-tag>
          </div>
          <div class="detail-meta">
            <div class="sender-info">
              <span class="sender-label">发送者:</span>
              <span>{{ selectedMessage.sender }}</span>
            </div>
            <div class="time-info">
              <span class="time-label">时间:</span>
              <span>{{ selectedMessage.time }}</span>
            </div>
          </div>
        </div>
        <div class="detail-content">
          {{ selectedMessage.content }}
        </div>
        <div class="detail-actions">
          <CommonBackButton @click="backToList" :useRouterBack="false" text="返回列表" />
          <el-button
            v-if="!selectedMessage.isRead"
            type="success"
            @click="markAsRead(selectedMessage)"
          >
            标记为已读
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="less">
.messages-management-container {
  padding: 0 20px 20px 20px;

  .messages-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    .page-title {
      font-size: 18px;
      font-weight: 600;
      margin: 0;
    }
  }

  .messages-content {
    .messages-list-container {
      .category-section {
        margin-bottom: 24px;
        display: flex;
        align-items: center;
        gap: 12px;

        .category-label {
          font-weight: 500;
        }

        .category-tag {
          cursor: pointer;

          &:hover {
            opacity: 0.8;
          }
        }
      }

      .messages-list {
        .message-item {
          display: flex;
          align-items: flex-start;
          padding: 16px;
          border: 1px solid #e4e7ed;
          border-radius: 4px;
          margin-bottom: 12px;
          background-color: #fff;
          cursor: pointer;
          transition: box-shadow 0.3s;

          &:hover {
            box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
          }

          &.unread-message {
            border-left: 4px solid #409eff;
            background-color: rgba(64, 158, 255, 0.05);
          }

          .message-icon {
            font-size: 24px;
            margin: 4px 16px 0 0;
          }

          .message-content {
            flex: 1;

            .message-title {
              font-size: 14px;
              font-weight: 500;
              margin-bottom: 8px;
            }

            .message-meta {
              display: flex;
              justify-content: space-between;
              align-items: center;
              font-size: 12px;
              color: #909399;

              .message-time {
                flex: 1;
              }
            }
          }
        }
      }

      .empty-messages {
        text-align: center;
        margin-top: 50px;
      }
    }

    .message-detail-container {
      padding: 0;

      .detail-header {
        margin-bottom: 20px;

        .detail-title {
          display: flex;
          align-items: center;
          gap: 12px;
          margin-bottom: 15px;

          h3 {
            font-size: 20px;
            margin: 0;
          }
        }

        .detail-meta {
          display: flex;
          flex-direction: column;
          gap: 10px;
          font-size: 14px;

          .sender-info,
          .time-info {
            display: flex;
            align-items: center;
            gap: 8px;
          }

          .sender-label,
          .time-label {
            color: #909399;
            font-weight: 500;
          }
        }
      }

      .detail-content {
        font-size: 16px;
        line-height: 1.8;
        margin-bottom: 30px;
        padding: 20px;
        background-color: #f5f7fa;
        border-radius: 4px;
      }

      .detail-actions {
        display: flex;
        justify-content: flex-end;
        gap: 10px;
      }
    }
  }
}
</style>
