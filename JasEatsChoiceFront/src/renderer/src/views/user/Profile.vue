<template>
  <div class="profile-container">
    <h2>个人中心</h2>

    <el-card class="profile-card">
      <div class="profile-info">
        <div class="basic-info">
          <div class="info-item">📱 <span>基本信息：</span>{{ userInfo.name }} | {{ userInfo.phone }} | {{ userInfo.location }}</div>
          <div class="info-item">📊 <span>饮食数据：</span>今日已摄入{{ userInfo.todayCalorie }} | 本周均衡度{{ userInfo.weekBalance }}</div>
        </div>

        <div class="action-buttons">
          <el-button type="text" size="small" @click="shareProfile">📤 分享</el-button>
        </div>
      </div>

      <el-divider />

      <div class="order-module">
        <h3>📜 订单模块</h3>
        <div class="order-stats">
          <div class="stat-item">
            <div class="stat-label">进行中订单</div>
            <div class="stat-value">{{ userInfo.orders.inProgress }}笔</div>
          </div>
          <div class="stat-item">
            <div class="stat-label">待确认订单</div>
            <div class="stat-value">{{ userInfo.orders.pending }}笔</div>
          </div>
          <div class="stat-item">
            <div class="stat-label">待评价订单</div>
            <div class="stat-value">{{ userInfo.orders.pendingComment }}笔</div>
          </div>
        </div>
        <el-button type="primary" size="small" style="margin-top: 10px;" @click="goToAllOrders">查看所有订单</el-button>
      </div>

      <el-divider />

      <div class="wallet-module">
        <h3>💰 钱包模块</h3>
        <div class="wallet-info">
          平台币余额：{{ userInfo.wallet.balance }}个 |
          <el-button type="text" size="small" @click="recharge">充值</el-button> |
          <el-button type="text" size="small" @click="withdraw">提现</el-button> |
          <el-button type="text" size="small" @click="goToConsumeHistory">消费记录</el-button>
        </div>
      </div>

      <el-divider />

      <div class="other-modules">
        <div class="module-item">
          🎁 <span>我的收藏：</span>共{{ userInfo.collections }}个
          <el-button type="text" size="small" @click="goToMyCollection">查看收藏</el-button>
        </div>
        <div class="module-item">
          📝 <span>我的地址：</span>共{{ userInfo.addresses }}个 | 默认地址：{{ userInfo.defaultAddress }}
          <el-button type="text" size="small" @click="goToAddress">管理地址</el-button>
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
}

.profile-container h2 {
  font-size: 24px;
  margin: 0 0 20px 0;
}

.profile-card {
  padding: 20px;
}

.profile-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.basic-info .info-item {
  margin-bottom: 5px;
  font-size: 16px;
}

.basic-info .info-item span {
  font-weight: bold;
}

.order-module h3,
.wallet-module h3 {
  font-size: 18px;
  margin: 0 0 15px 0;
  font-weight: bold;
}

.order-stats {
  display: flex;
  gap: 30px;
  margin-bottom: 5px;
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

.wallet-info {
  font-size: 16px;
  margin-bottom: 10px;
}

.other-modules {
  margin-bottom: 10px;
}

.module-item {
  font-size: 16px;
  margin-bottom: 8px;
}

.module-item span {
  font-weight: bold;
}

.bottom-actions {
  margin-top: 20px;
}

.bottom-actions .el-button {
  margin-right: 20px;
}
</style>
