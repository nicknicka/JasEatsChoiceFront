<template>
  <div class="chat-container">
    <div class="chat-header">
      <h3 class="page-title">【群聊】</h3>
      <el-button type="primary" size="small" @click="createNewGroup">
        + 新建群聊
      </el-button>
    </div>

    <div class="chat-content">
      <!-- 左侧会话列表 -->
      <div class="conversation-list">
        <div
          class="conversation-item"
          v-for="chat in groupChats"
          :key="chat.id"
          :class="{ 'active': selectedChat?.id === chat.id }"
          @click="selectChat(chat)"
        >
          <div class="conversation-avatar">
            <div class="emoji-avatar">
              {{ chat.avatar || '👥' }}
            </div>
            <div v-if="chat.unreadCount > 0" class="unread-count">
              {{ chat.unreadCount }}
            </div>
          </div>
          <div class="conversation-info">
            <div class="name-time">
              <span class="name">
                {{ chat.name }}
              </span>
              <span class="time">{{ chat.lastTime }}</span>
            </div>
            <div class="last-message">{{ chat.lastMessage || '暂无消息' }}</div>
          </div>
        </div>
      </div>

    <!-- 右侧聊天内容 -->
    <div class="chat-area" v-if="selectedChat">
      <!-- 群聊头部 -->
      <div class="chat-area-header">
        <div class="conversation-info">
          <div class="name-info">
            <span class="name">{{ selectedChat.name }}</span>
          </div>
        </div>
        <div class="chat-actions">
          <el-button type="primary" size="small" @click="createGroupOrder">创建群订单</el-button>
          <el-button size="small" @click="joinGroupOrder">加入群订单</el-button>
        </div>
      </div>

      <!-- 群订单信息 -->
      <div class="group-order-info" v-if="currentGroupOrder">
        <el-card>
          <template #header>
            <div class="card-header">
              <span class="card-title">当前群订单</span>
              <el-button type="success" size="small" @click="goToOrderConfirmation" v-if="currentGroupOrder">
                去支付
              </el-button>
            </div>
          </template>
          <div class="order-overview">
            <div class="overview-item">
              <span class="info-label">群名称：</span>
              <span class="info-value">{{ currentGroupOrder.groupName }}</span>
            </div>
            <div class="overview-item">
              <span class="info-label">创建人：</span>
              <span class="info-value">{{ currentGroupOrder.creator }}</span>
            </div>
            <div class="overview-item">
              <span class="info-label">总金额：</span>
              <span class="info-value">¥{{ currentGroupOrder.totalAmount.toFixed(2) }}</span>
            </div>
            <div class="overview-item">
              <span class="info-label">参与人数：</span>
              <span class="info-value">{{ currentGroupOrder.members.length }}人</span>
            </div>
          </div>

          <div class="order-items">
            <h4 class="section-title">订单商品</h4>
            <div class="item-list">
              <el-tag v-for="item in currentGroupOrder.orderItems" :key="item.id" class="order-item-tag" type="info">
                {{ item.name }} ×{{ item.quantity }}
              </el-tag>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 右侧下方：具体聊天内容 -->
      <div class="messages-container">
        <div
          v-for="message in chatMessages"
          :key="message.id"
          class="message-item"
          :class="{
            'others-message': message.sender !== '我',
            'my-message': message.sender === '我'
          }"
        >
          <div class="message-header">
            <span class="sender-name">{{ message.sender }}</span>
          </div>
          <div class="message-content">
            {{ message.content }}
            <div class="message-time">{{ message.time }}</div>
          </div>
        </div>
      </div>

      <!-- 消息输入框 -->
      <div class="message-input-container">
        <el-input
          v-model="newMessage"
          placeholder="输入消息..."
          @keyup.enter="sendMessage"
        ></el-input>
        <el-button type="primary" @click="sendMessage">发送</el-button>
      </div>
    </div>

    <!-- 空选择提示 -->
    <div class="empty-select" v-else>
      <div class="empty-icon">👥</div>
      <p>请选择一个群聊开始交流</p>
    </div>
  </div>
</div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';

const router = useRouter();

// 模拟群聊列表数据
const groupChats = ref([
  {
    id: 1,
    name: '美食爱好者群',
    avatar: '🍴',
    lastMessage: '李四: 我要麻婆豆腐',
    lastTime: '10:33',
    unreadCount: 0
  },
  {
    id: 2,
    name: '同事午餐群',
    avatar: '👨‍💼',
    lastMessage: '小明: 今天中午吃什么？',
    lastTime: '09:15',
    unreadCount: 2
  },
  {
    id: 3,
    name: '家庭聚餐群',
    avatar: '👨👩👧👦',
    lastMessage: '妈妈: 周末家庭聚餐',
    lastTime: '昨天',
    unreadCount: 0
  }
]);

// 当前选中的群聊
const selectedChat = ref(null);

// 模拟群订单数据
const currentGroupOrder = ref(null);

// 模拟群聊消息 (根据选中的群聊动态切换)
const chatMessages = ref([]);

// 新消息输入
const newMessage = ref('');

// 选择群聊
const selectChat = (chat) => {
  selectedChat.value = chat;
  // 模拟根据不同群聊加载不同的消息
  if (chat.id === 1) {
    chatMessages.value = [
      { id: 1, sender: '系统', content: '李四加入了群聊', time: '10:30' },
      { id: 2, sender: '张三', content: '大家一起点个外卖吧！', time: '10:31' },
      { id: 3, sender: '王五', content: '好啊，我要宫保鸡丁', time: '10:32' },
      { id: 4, sender: '李四', content: '我要麻婆豆腐', time: '10:33' }
    ];
  } else if (chat.id === 2) {
    chatMessages.value = [
      { id: 1, sender: '小明', content: '今天中午吃什么？', time: '09:15' },
      { id: 2, sender: '小红', content: '我要一份红烧肉盖饭', time: '09:16' }
    ];
  } else if (chat.id === 3) {
    chatMessages.value = [
      { id: 1, sender: '妈妈', content: '周末家庭聚餐', time: '昨天' }
    ];
  }
  // 清除未读消息
  chat.unreadCount = 0;
};

// 新建群聊
const createNewGroup = () => {
  ElMessage.success('新建群聊功能待实现');
};

// 发送消息
const sendMessage = () => {
  if (newMessage.value.trim() && selectedChat.value) {
    const newMsg = {
      id: chatMessages.value.length + 1,
      sender: '我',
      content: newMessage.value.trim(),
      time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
    };

    chatMessages.value.push(newMsg);
    // 更新群聊列表中的最后一条消息
    selectedChat.value.lastMessage = `我: ${newMessage.value.trim()}`;
    selectedChat.value.lastTime = newMsg.time;

    newMessage.value = '';
  }
};

// 创建群订单
const createGroupOrder = () => {
  // 创建一个新的群订单
  if (selectedChat.value) {
    const order = {
      orderId: `GO${Date.now()}`, // 生成唯一的群订单ID
      groupId: selectedChat.value.id,
      groupName: selectedChat.value.name,
      creator: '我', // 当前登录用户
      members: ['我'],
      orderItems: [],
      totalAmount: 0.0,
      status: 'active', // 订单状态：active（活动）、closed（已关闭）、paid（已支付）
      createTime: new Date().toISOString()
    };

    // 这里可以添加实际的API请求
    // await axios.post('/api/group-orders', order);

    currentGroupOrder.value = order;
    ElMessage.success('群订单已创建');

    // 更新群聊消息
    const orderMsg = {
      id: chatMessages.value.length + 1,
      sender: '系统',
      content: '我创建了一个群订单，大家可以加入并添加商品',
      time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
    };
    chatMessages.value.push(orderMsg);

    // 更新群聊列表的最后一条消息
    selectedChat.value.lastMessage = '系统: 我创建了一个群订单';
    selectedChat.value.lastTime = orderMsg.time;
  } else {
    ElMessage.error('请先选择一个群聊');
  }
};

// 加入群订单
const joinGroupOrder = () => {
  // 加入一个已存在的群订单
  if (selectedChat.value) {
    // 检查是否当前有群订单
    if (currentGroupOrder.value) {
      if (currentGroupOrder.value.status === 'active') { // 只有活动状态的订单才能加入
        // 检查是否已经在群订单中
        if (!currentGroupOrder.value.members.includes('我')) {
          currentGroupOrder.value.members.push('我');
          ElMessage.success('已加入群订单');

          // 更新群聊消息
          const joinMsg = {
            id: chatMessages.value.length + 1,
            sender: '系统',
            content: '我加入了群订单',
            time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
          };
          chatMessages.value.push(joinMsg);

          // 更新群聊列表的最后一条消息
          selectedChat.value.lastMessage = '系统: 我加入了群订单';
          selectedChat.value.lastTime = joinMsg.time;
        } else {
          ElMessage.warning('你已经在群订单中了');
        }
      } else {
        ElMessage.error('该群订单已关闭或已支付，无法加入');
      }
    } else {
      ElMessage.error('当前群没有订单，请先创建群订单');
    }
  } else {
    ElMessage.error('请先选择一个群聊');
  }
};

// 跳转到订单确认页
const goToOrderConfirmation = () => {
  // 存储群订单信息到会话存储
  if (currentGroupOrder.value) {
    const pendingOrder = {
      cartItems: currentGroupOrder.value.orderItems.map(item => ({
        ...item,
        price: item.price || 22.2 // 使用商品自身价格或默认价格
      })),
      totalAmount: currentGroupOrder.value.totalAmount,
      fromChat: true,
      groupName: currentGroupOrder.value.groupName,
      orderId: currentGroupOrder.value.orderId, // 添加群订单ID
      creator: currentGroupOrder.value.creator, // 添加订单创建人
      members: currentGroupOrder.value.members // 添加订单成员列表
    };

    sessionStorage.setItem('pendingOrder', JSON.stringify(pendingOrder));

    // 更新订单状态为已关闭
    currentGroupOrder.value.status = 'closed';

    // 跳转到订单确认页
    router.push('/user/home/order-confirmation');
  } else {
    ElMessage.error('当前没有群订单');
  }
};
</script>

<style scoped lang="less">
.chat-container {
  padding: 0 20px 20px 20px;
  height: calc(100vh - 60px);

  .chat-header {
    margin-bottom: 20px;
    .page-title {
      font-size: 18px;
      font-weight: 600;
      margin: 0;
    }
  }

  .chat-content {
    display: flex;
    gap: 20px;
    height: calc(100vh - 120px);

    .conversation-list {
      width: 37%; /* 固定宽度 */
      border: 1px solid #e4e7ed;
      border-radius: 4px;
      overflow : hidden;
      white-space: nowrap;
      text-overflow: ellipsis;

      .conversation-item {
        display: flex;
        align-items: center;
        padding: 16px; /* 调整内边距 */
        cursor: pointer;
        border-bottom: 1px solid #e4e7ed;
        transition: background-color 0.3s;
        position: relative; /* 为未读消息红点定位提供参考 */

        &:hover {
          background-color: #f5f7fa;
        }

        &.active {
          background-color: #ecf5ff;
        }

        .conversation-avatar {
          margin-right: 11px; /* 调整头像右侧间距 */
          position: relative; /* 为未读消息红点定位提供参考 */

          .emoji-avatar {
            width: 35px;
            height: 35px;
            border-radius: 7px;
            background-color: #f0f0f0;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 24px; /* 调整emoji大小 */
            text-align: center;
          }
        }

        .conversation-info {
          flex: 1;
          min-width: 0; /* 确保flex元素能正确收缩，让省略号生效 */

          .name-time {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 4px;
            font-size: 14px;

            .name {
              font-weight: 500;
              white-space: nowrap;
              overflow: hidden;
              text-overflow: ellipsis;
              flex: 1; /* 让名称占据剩余空间 */
              margin-right: 8px; /* 与时间保持一定距离 */

              .member-count {
                font-size: 8px;
                color: #909399;
              }
            }

            .time {
              font-size: 8px;
              white-space: nowrap; /* 时间不换行 */
              color: #909399;
            }
          }

          .last-message {
            font-size: 10px;
            color: #606266;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
          }
        }

        .unread-count {
          background-color: #f56c6c;
          // height: auto;
          width: 10px ;
          height: 10px;
          color: #fff;
          border-radius: 50%;
          padding: 2px; /* 减小内边距，使红点更紧凑 */
          font-size: 7px; /* 减小字体大小 */
          position: absolute; /* 绝对定位 */
          top: 0; /* 根据头像大小精确计算位置 */
          right: 0; /* 根据头像大小精确计算位置 */
          transform: translate(50%, -50%); /* 使红点中心对齐到头像右上角 */
          z-index: 1; /* 确保红点在最上层 */
          min-height: 7px; /* 设置最小高度，确保单个数字也能显示为圆形 */
          min-width: 7px; /* 设置最小宽度，确保单个数字也能显示为圆形 */
          text-align: center; /* 文字居中 */
        }
      }
    }

    .chat-area {
      flex: 1;
      border: 1px solid #e4e7ed;
      border-radius: 4px;
      display: flex;
      flex-direction: column;

      .chat-area-header {
        padding: 12px;
        border-bottom: 1px solid #e4e7ed;
        display: flex;
        justify-content: space-between;
        align-items: center;

        .conversation-info {
          display: flex;
          align-items: center;

          .name-info {
            .name {
              font-weight: 500;
              white-space: nowrap;
              overflow: hidden;
              text-overflow: ellipsis;

              .member-count {
                font-size: 12px;
                color: #909399;
              }
            }
          }
        }

        .chat-actions {
          display: flex;
          gap: 10px;
        }
      }

      .group-order-info {
        padding: 20px;
      }

      .order-overview {
        margin-bottom: 20px;

        .overview-item {
          margin-bottom: 10px;

          .info-label {
            font-weight: 600;
            color: #555;
            margin-right: 8px;
          }
        }
      }

      .order-items {
        margin-top: 20px;

        .section-title {
          font-size: 16px;
          font-weight: 600;
          margin-bottom: 15px;
          color: #34495e;
        }

        .item-list {
          display: flex;
          flex-wrap: wrap;
          gap: 8px;
        }

        .order-item-tag {
          margin-bottom: 8px;
        }
      }

      .messages-container {
        flex: 1;
        padding: 11px;
        overflow-y: auto;
        display: flex;
        flex-direction: column;


        .message-item {
          margin-bottom: 16px;
          max-width: 70%;

          .message-header {
            margin-bottom: 4px;
            .sender-name {
              font-size: 12px;
              color: #666;
            }
          }

          .message-content {
            border-radius: 10px;
            padding: 7px;
            font-size: 12px;

            .message-time {
              text-align: right;
              font-size: 10px;
              margin-top: 4px;
            }
          }

          &.others-message {
            align-self: flex-start;

            .message-content {
              background-color: #fff;
              border: 1px solid #ddd;

              .message-time {
                color: #909399;
              }
            }
          }

          &.my-message {
            align-self: flex-end;

            .message-content {
              background-color: #67c23a;
              color: #fff;

              .message-time {
                opacity: 0.8;
              }
            }
          }
        }
      }

      .message-input-container {
        padding: 12px;
        border-top: 1px solid #e4e7ed;
        display: flex;
        gap: 12px;

        .el-input {
          flex: 1;
        }

        button {
          align-self: flex-end;
        }
      }
    }
  }

  /* 空选择提示 */
  .empty-select {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    background-color: #fafafa;
    color: #999;

    .empty-icon {
      font-size: 48px;
      margin-bottom: 16px;
    }
  }
}
</style>