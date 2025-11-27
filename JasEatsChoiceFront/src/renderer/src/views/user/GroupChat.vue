<template>
  <div class="group-chat-container">
    <!-- 群聊头部 -->
    <div class="chat-header">
      <div class="chat-title">美食爱好者群</div>
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

    <!-- 群聊消息 -->
    <div class="chat-messages">
      <h4 class="section-title">群聊消息</h4>
      <div class="message-list">
        <div class="message-item" v-for="message in chatMessages" :key="message.id">
          <div class="message-sender">{{ message.sender }}:</div>
          <div class="message-content">{{ message.content }}</div>
          <div class="message-time">{{ message.time }}</div>
        </div>
      </div>

      <!-- 消息输入框 -->
      <div class="message-input">
        <el-input
          v-model="newMessage"
          placeholder="输入消息..."
          @keyup.enter="sendMessage"
        ></el-input>
        <el-button type="primary" @click="sendMessage">发送</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

// 模拟群订单数据
const currentGroupOrder = ref({
  groupName: '美食爱好者群',
  creator: '张三',
  totalAmount: 88.8,
  members: ['张三', '李四', '王五'],
  orderItems: [
    { id: 1, name: '宫保鸡丁', quantity: 2 },
    { id: 2, name: '麻婆豆腐', quantity: 1 },
    { id: 3, name: '米饭', quantity: 4 }
  ]
});

// 模拟群聊消息
const chatMessages = ref([
  { id: 1, sender: '系统', content: '李四加入了群聊', time: '10:30' },
  { id: 2, sender: '张三', content: '大家一起点个外卖吧！', time: '10:31' },
  { id: 3, sender: '王五', content: '好啊，我要宫保鸡丁', time: '10:32' },
  { id: 4, sender: '李四', content: '我要麻婆豆腐', time: '10:33' }
]);

// 新消息输入
const newMessage = ref('');

// 发送消息
const sendMessage = () => {
  if (newMessage.value.trim()) {
    chatMessages.value.push({
      id: chatMessages.value.length + 1,
      sender: '我',
      content: newMessage.value.trim(),
      time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
    });
    newMessage.value = '';
  }
};

// 创建群订单
const createGroupOrder = () => {
  // 实际应用中这里应该创建一个新的群订单
  console.log('创建群订单');
};

// 加入群订单
const joinGroupOrder = () => {
  // 实际应用中这里应该加入一个已存在的群订单
  console.log('加入群订单');
};

// 跳转到订单确认页
const goToOrderConfirmation = () => {
  // 存储群订单信息到会话存储
  const pendingOrder = {
    cartItems: currentGroupOrder.value.orderItems.map(item => ({
      ...item,
      price: 22.2 // 模拟价格
    })),
    totalAmount: currentGroupOrder.value.totalAmount,
    fromChat: true,
    groupName: currentGroupOrder.value.groupName
  };

  sessionStorage.setItem('pendingOrder', JSON.stringify(pendingOrder));

  // 跳转到订单确认页
  router.push('/user/home/order-confirmation');
};
</script>

<style scoped lang="less">
.group-chat-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f5f5;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.chat-title {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
}

.chat-actions {
  display: flex;
  gap: 10px;
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

.chat-messages {
  flex: 1;
  padding: 20px;
  overflow-y: auto;

  .section-title {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 20px;
    color: #34495e;
  }

  .message-list {
    margin-bottom: 20px;

    .message-item {
      margin-bottom: 15px;
      display: flex;
      flex-direction: column;

      .message-sender {
        font-weight: 600;
        color: #2c3e50;
      }

      .message-content {
        margin: 5px 0;
        color: #555;
      }

      .message-time {
        font-size: 12px;
        color: #909399;
      }
    }
  }

  .message-input {
    display: flex;
    gap: 10px;

    .el-input {
      flex: 1;
    }
  }
}
</style>
