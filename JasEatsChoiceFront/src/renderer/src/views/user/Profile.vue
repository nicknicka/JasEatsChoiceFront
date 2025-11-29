<template>
  <div class="profile-container">
    <h2>个人中心</h2>

    <el-card class="profile-card">
      <!-- 顶部头像区域 -->
      <div class="profile-header">
        <div class="avatar-container">
          <el-avatar :size="120" class="user-avatar">
            {{ userInfo.name.charAt(0) }}
          </el-avatar>
        </div>
        <div class="user-basic-info">
          <h3 class="user-name">{{ userInfo.name }}</h3>
          <div class="user-stats">
            <div class="stat-item">
              <span class="stat-label">手机号</span>
              <span class="stat-value">{{ userInfo.phone }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">所在地</span>
              <span class="stat-value">{{ userInfo.location }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">今日摄入</span>
              <span class="stat-value calorie-highlight">{{ userInfo.todayCalorie }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">本周均衡度</span>
              <span class="stat-value balance-highlight">{{ userInfo.weekBalance }}</span>
            </div>
          </div>
          <div class="action-buttons">
            <el-button type="primary" size="small" @click="shareProfile" class="share-btn">📤 分享</el-button>
          </div>
        </div>
      </div>

      <el-divider />

      <el-divider />

      <div class="order-module">
        <h3 class="module-title">📜 订单模块</h3>
        <div class="order-stats">
          <div class="order-stat-card">
            <div class="stat-value order-in-progress">{{ userInfo.orders.inProgress }}笔</div>
            <div class="stat-label">进行中订单</div>
          </div>
          <div class="order-stat-card">
            <div class="stat-value order-pending">{{ userInfo.orders.pending }}笔</div>
            <div class="stat-label">待确认订单</div>
          </div>
          <div class="order-stat-card">
            <div class="stat-value order-pending-comment">{{ userInfo.orders.pendingComment }}笔</div>
            <div class="stat-label">待评价订单</div>
          </div>
        </div>
        <div style="display: flex; justify-content: flex-end; margin-top: 10px;">
          <el-button type="primary" size="small" @click="goToAllOrders">
            <span>🔍 查看所有订单</span>
          </el-button>
        </div>
      </div>

      <el-divider />

      <div class="wallet-module">
        <h3 class="module-title">💰 钱包模块</h3>
        <div class="wallet-card">
          <div class="wallet-header">
            <div class="wallet-label">平台币余额</div>
          </div>
          <div class="wallet-balance">
            <span class="balance-number">{{ userInfo.wallet.balance }}</span>
            <span class="balance-unit">个</span>
          </div>
          <div class="wallet-actions">
            <el-button type="primary" size="small" @click="recharge" class="wallet-action-btn">
              💸 充值
            </el-button>
            <el-button type="primary" size="small" @click="withdraw" class="wallet-action-btn withdraw-btn">
              📥 提现
            </el-button>
            <el-button type="text" size="small" @click="goToConsumeHistory">
              📊 消费记录
            </el-button>
          </div>
        </div>
      </div>

      <el-divider />

      <div class="other-modules">
        <div class="module-item-card">
          <div class="module-item-content">
            <div class="module-item-icon">🎁</div>
            <div class="module-item-info">
              <div class="module-item-title">我的收藏</div>
              <div class="module-item-desc">共{{ userInfo.collections }}个</div>
            </div>
          </div>
          <el-button type="text" size="small" @click="goToMyCollection" class="module-item-btn">
            查看收藏
          </el-button>
        </div>

        <div class="module-item-card">
          <div class="module-item-content">
            <div class="module-item-icon">📝</div>
            <div class="module-item-info">
              <div class="module-item-title">我的地址</div>
              <div class="module-item-desc">共{{ userInfo.addresses }}个 | 默认地址：{{ userInfo.defaultAddress }}</div>
            </div>
          </div>
          <el-button type="text" size="small" @click="goToAddress" class="module-item-btn">
            管理地址
          </el-button>
        </div>
      </div>

      <el-divider />

      <div class="bottom-actions">
        <el-button type="text" size="small" @click="goToContact">📞 联系客服</el-button>
        <el-button type="text" size="small" @click="submitFeedback">🙋‍♂️ 反馈建议</el-button>
        <el-button type="text" size="small" danger @click="logout">🔚 退出登录</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';

const router = useRouter();

// 真实数据
const userInfo = ref({
  name: '张三',
  phone: '138xxxx8888',
  location: '北京朝阳',
  todayCalorie: '620kcal',
  weekBalance: '85%',
  orders: {
    inProgress: '2',
    pending: '1',
    pendingComment: '3'
  },
  wallet: {
    balance: '177'
  },
  collections: '8',
  addresses: '5',
  defaultAddress: '公司'
});

// 从本地存储加载真实数据
onMounted(() => {
  const savedUserInfo = localStorage.getItem('userInfo');
  if (savedUserInfo) {
    userInfo.value = JSON.parse(savedUserInfo);
  }
});

// 跳转到所有订单页面
const goToAllOrders = () => {
  router.push('/user/home/orders');
};

// 跳转到消费记录页面
const goToConsumeHistory = () => {
  router.push('/user/home/consume-history');
};

// 跳转到我的收藏页面
const goToMyCollection = () => {
  router.push('/user/home/my-collection');
};

// 跳转到地址管理页面
const goToAddress = () => {
  router.push('/user/home/address');
};

// 充值功能
const recharge = () => {
  // 创建充值表单对话框
  ElMessageBox.prompt('请输入充值金额(单位:平台币)', '充值', {
    inputPattern: /^[1-9]\d*$/,
    inputValidator: (value) => {
      if (!value) {
        return '请输入充值金额';
      }
      if (Number(value) <= 0) {
        return '充值金额必须大于0';
      }
      return true;
    }
  })
  .then(({ value }) => {
    // 模拟充值成功
    const newBalance = (Number(userInfo.value.wallet.balance) + Number(value)).toString();
    userInfo.value.wallet.balance = newBalance;

    // 创建交易记录
    const rechargeRecord = {
      id: Date.now(),
      date: new Date().toISOString().replace('T', ' ').substring(0, 19),
      type: 'recharge',
      amount: Number(value),
      description: '平台币充值',
      status: 'success'
    };

    // 保存到交易历史
    let history = localStorage.getItem('consumeHistory');
    if (history) {
      history = JSON.parse(history);
      history.push(rechargeRecord);
    } else {
      history = [rechargeRecord];
    }
    localStorage.setItem('consumeHistory', JSON.stringify(history));

    // 更新本地存储
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value));

    // 跳转到消费记录页面查看交易
    router.push('/user/home/consume-history');

    ElMessage.success(`充值成功!已到账${value}平台币`);
  })
  .catch(() => {
    ElMessage.info('已取消充值');
  });
};

// 提现功能
const withdraw = () => {
  // 创建提现表单对话框
  ElMessageBox.prompt('请输入提现金额(单位:平台币)', '提现', {
    inputPattern: /^[1-9]\d*$/,
    inputValidator: (value) => {
      if (!value) {
        return '请输入提现金额';
      }
      const numValue = Number(value);
      const balance = Number(userInfo.value.wallet.balance);

      if (numValue <= 0) {
        return '提现金额必须大于0';
      }

      if (numValue > balance) {
        return `提现金额不能超过余额${balance}平台币`;
      }

      return true;
    }
  })
  .then(({ value }) => {
    // 模拟提现成功
    const newBalance = (Number(userInfo.value.wallet.balance) - Number(value)).toString();
    userInfo.value.wallet.balance = newBalance;

    // 创建交易记录
    const withdrawRecord = {
      id: Date.now(),
      date: new Date().toISOString().replace('T', ' ').substring(0, 19),
      type: 'withdraw',
      amount: Number(value),
      description: '平台币提现',
      status: 'success'
    };

    // 保存到交易历史
    let history = localStorage.getItem('consumeHistory');
    if (history) {
      history = JSON.parse(history);
      history.push(withdrawRecord);
    } else {
      history = [withdrawRecord];
    }
    localStorage.setItem('consumeHistory', JSON.stringify(history));

    // 更新本地存储
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value));

    // 跳转到消费记录页面查看交易
    router.push('/user/home/consume-history');

    ElMessage.success(`提现成功!已转出${value}平台币`);
  })
  .catch(() => {
    ElMessage.info('已取消提现');
  });
};

// 跳转到联系客服页面
const goToContact = () => {
  router.push('/user/home/contact');
};

// 提交反馈建议
const submitFeedback = () => {
  ElMessage.success('反馈已提交，我们会尽快处理');
};

// 退出登录
const logout = () => {
  // 清除本地存储
  localStorage.removeItem('isLoggedIn');
  localStorage.removeItem('userInfo');
  localStorage.removeItem('userAvatar');

  // 跳转到登录页面
  router.push('/login');
  ElMessage.success('已退出登录');
};

// 分享功能
const shareProfile = () => {
  // 模拟分享功能
  ElMessage.info('分享功能正在开发中');
};
</script>

<style scoped>
.profile-container {
  padding: 0 20px 20px 20px;
  min-height: 100vh;
}

.profile-container h2 {
  font-size: 28px;
  margin: 0 0 25px 0;
  color: #333;
  font-weight: 700;
}

.profile-card {
  padding: 25px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  background-color: #fff;
}

/* 顶部头像区域 */
.profile-header {
  display: flex;
  align-items: center;
  gap: 25px;
  padding: 20px 0;
}

.avatar-container {
  position: relative;
}

.user-avatar {
  background: linear-gradient(135deg, #ff6b6b 0%, #ffa500 100%);
  font-size: 48px;
  color: #fff;
  box-shadow: 0 4px 12px rgba(255, 107, 107, 0.3);
  transition: transform 0.3s ease;
}

.user-avatar:hover {
  transform: scale(1.05);
}

.user-basic-info {
  flex: 1;
  min-width: 0; /* 防止 flex 容器内内容溢出 */
}

.user-name {
  font-size: 24px;
  font-weight: 700;
  color: #2d3748;
  margin: 0 0 15px 0;
}

.user-stats {
  display: flex;
  flex-wrap: wrap;
  gap: 25px;
  margin-bottom: 15px;
  font-size: 14px;
}

.user-stats .stat-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-label {
  color: #718096;
  font-weight: 500;
}

.stat-value {
  color: #2d3748;
  font-weight: 600;
}

.calorie-highlight {
  color: #ff6b6b;
}

.balance-highlight {
  color: #48bb78;
}

.action-buttons {
  display: flex;
  gap: 12px;
}

.basic-info .info-item span {
  font-weight: bold;
}

.module-title {
  font-size: 18px;
  margin: 0 0 20px 0;
  font-weight: 700;
  color: #2d3748;
}

.order-stats {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-bottom: 20px;
}

.order-stat-card {
  flex: 1;
  min-width: 140px;
  padding: 20px;
  background: linear-gradient(135deg, #f0f4f8 0%, #e2e8f0 100%);
  border-radius: 12px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.order-stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

.order-stat-card .stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #2d3748;
  margin-bottom: 6px;
}

.order-in-progress {
  color: #2b6cb0; /* 蓝色 */
}

.order-pending {
  color: #dd6b20; /* 橙色 */
}

.order-pending-comment {
  color: #805ad5; /* 紫色 */
}

.order-stat-card .stat-label {
  font-size: 14px;
  color: #718096;
  margin-bottom: 2px;
}

.order-stat-card small {
  font-size: 12px;
  color: #a0aec0;
}

.stat-item {
  text-align: center;
}

.stat-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 5px;
}

.stat-value {
  font-size: 18px;
  font-weight: bold;
  color: #FF6B6B;
}

.wallet-card {
  background: linear-gradient(135deg, #fef5e7 0%, #fdebd0 100%);
  padding: 25px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.wallet-header {
  margin-bottom: 10px;
}

.wallet-label {
  font-size: 16px;
  color: #718096;
  font-weight: 500;
}

.wallet-balance {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 20px;
}

.balance-number {
  font-size: 48px;
  font-weight: 700;
  color: #d69e2e;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.balance-unit {
  font-size: 18px;
  color: #d69e2e;
  font-weight: 500;
}

.wallet-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.wallet-action-btn {
  background: linear-gradient(135deg, #f6e05e 0%, #ecc94b 100%);
  border: none;
  color: #2d3748;
  font-weight: 600;
  transition: transform 0.2s ease;
}

.wallet-action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(246, 224, 94, 0.4);
}

.withdraw-btn {
  background: linear-gradient(135deg, #4299e1 0%, #3182ce 100%);
  color: #fff;
}

.withdraw-btn:hover {
  box-shadow: 0 4px 8px rgba(66, 153, 225, 0.4);
}

.other-modules {
  margin-bottom: 20px;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.module-item-card {
  background: linear-gradient(135deg, #ebf8ff 0%, #bee3f8 100%);
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all 0.3s ease;
}

.module-item-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(190, 227, 248, 0.3);
}

.module-item-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.module-item-icon {
  font-size: 24px;
  line-height: 1;
}

.module-item-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.module-item-title {
  font-size: 16px;
  font-weight: 600;
  color: #2b6cb0;
}

.module-item-desc {
  font-size: 14px;
  color: #718096;
}

.module-item-btn {
  color: #2b6cb0;
  font-weight: 600;
}

/* 底部操作按钮 */
.bottom-actions {
  margin-top: 30px;
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
}

.bottom-actions .el-button {
  flex: 1;
  min-width: 120px;
  height: 40px;
  border-radius: 8px;
  font-weight: 600;
  transition: transform 0.2s ease;
}

.bottom-actions .el-button:hover {
  transform: translateY(-2px);
}

.bottom-actions .el-button:nth-child(1) {
  background: linear-gradient(135deg, #48bb78 0%, #38a169 100%);
  border: none;
  color: #fff;
}

.bottom-actions .el-button:nth-child(2) {
  background: linear-gradient(135deg, #9f7aea 0%, #805ad5 100%);
  border: none;
  color: #fff;
}

.bottom-actions .el-button:nth-child(3) {
  background: linear-gradient(135deg, #f56565 0%, #e53e3e 100%);
  border: none;
  color: #fff;
}
</style>
