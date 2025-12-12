<template>
  <div class="consume-history-container">
    <el-button
      type="default"
      size="small"
      @click="goBack"
      style="margin-bottom: 20px;"
    >
      <el-icon><Back /></el-icon> 返回
    </el-button>
    <h2>消费记录</h2>

    <div class="filter-bar">
      <el-select v-model="filterType" placeholder="筛选类型" style="width: 150px; margin-right: 10px;">
        <el-option label="全部" value="all" />
        <el-option label="充值" value="recharge" />
        <el-option label="消费" value="consume" />
        <el-option label="提现" value="withdraw" />
      </el-select>

      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        style="width: 300px; margin-right: 10px;"
      />

      <el-button type="primary" size="small" @click="applyFilter">
        <el-icon><Search /></el-icon> 查询
      </el-button>

      <el-button type="default" size="small" @click="resetFilter">
        <el-icon><Refresh /></el-icon> 重置
      </el-button>
    </div>

    <div class="total-balance">
      <span class="label">当前平台币余额:</span>
      <span class="balance">{{ currentBalance }}个</span>
    </div>

    <el-table :data="paginatedHistory" style="width: 100%; margin-top: 20px;">
      <el-table-column prop="date" label="日期" width="150" />
      <el-table-column prop="type" label="类型" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.type === 'recharge' ? 'success' : (scope.row.type === 'withdraw' ? 'danger' : 'info')">
            {{ scope.row.type === 'recharge' ? '充值' : (scope.row.type === 'withdraw' ? '提现' : '消费') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="amount" label="金额" width="120">
        <template #default="scope">
          <span :class="scope.row.type === 'recharge' ? 'income' : 'expense'">
            {{ scope.row.type === 'recharge' ? '+' : '-' }}{{ scope.row.amount }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.status === 'success' ? 'success' : 'warning'">
            {{ scope.row.status === 'success' ? '成功' : '失败' }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        :total="filteredHistory.length"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { Search, Refresh, Back } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';

// 路由实例
const router = useRouter();

// 返回个人中心
const goBack = () => {
  router.push('/user/home/profile');
};

// 模拟交易历史数据
const history = ref([
  {
    id: 1,
    date: '2025-11-24 10:30:00',
    type: 'consume',
    amount: 20,
    description: '购买餐厅套餐',
    status: 'success'
  },
  {
    id: 2,
    date: '2025-11-23 18:45:00',
    type: 'recharge',
    amount: 100,
    description: '支付宝充值',
    status: 'success'
  },
  {
    id: 3,
    date: '2025-11-22 12:00:00',
    type: 'consume',
    amount: 15,
    description: '购买饮品',
    status: 'success'
  },
  {
    id: 4,
    date: '2025-11-21 20:00:00',
    type: 'consume',
    amount: 35,
    description: '购买晚餐',
    status: 'success'
  },
  {
    id: 5,
    date: '2025-11-20 14:30:00',
    type: 'recharge',
    amount: 100,
    description: '微信充值',
    status: 'success'
  }
]);

// 筛选条件
const filterType = ref('all');
const dateRange = ref([]);

// 分页
const currentPage = ref(1);
const pageSize = ref(10);

// 从localStorage获取当前余额
const currentBalance = ref('177');

// 计算过滤后的历史记录
const filteredHistory = computed(() => {
  let filtered = [...history.value];

  // 类型过滤
  if (filterType.value !== 'all') {
    filtered = filtered.filter(item => item.type === filterType.value);
  }

  // 日期过滤
  if (dateRange.value && dateRange.value.length === 2) {
    const startDate = dateRange.value[0].getTime();
    const endDate = dateRange.value[1].getTime();
    filtered = filtered.filter(item => {
      const itemDate = new Date(item.date).getTime();
      return itemDate >= startDate && itemDate <= endDate;
    });
  }

  // 按日期降序
  filtered.sort((a, b) => new Date(b.date) - new Date(a.date));

  return filtered;
});

// 分页数据
const paginatedHistory = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return filteredHistory.value.slice(start, end);
});

// 应用筛选
const applyFilter = () => {
  ElMessage.success('筛选条件已应用');
  currentPage.value = 1;
};

// 重置筛选
const resetFilter = () => {
  filterType.value = 'all';
  dateRange.value = [];
  currentPage.value = 1;
  ElMessage.info('筛选条件已重置');
};

// 页面大小变化
const handleSizeChange = (val) => {
  pageSize.value = val;
  currentPage.value = 1;
};

// 页面变化
const handleCurrentChange = (val) => {
  currentPage.value = val;
};

// 从localStorage加载用户信息和交易记录
onMounted(() => {
  const savedUserInfo = localStorage.getItem('userInfo');
  if (savedUserInfo) {
    const userInfo = JSON.parse(savedUserInfo);
    currentBalance.value = userInfo.wallet ? userInfo.wallet.balance : '177';
  }

  const savedHistory = localStorage.getItem('consumeHistory');
  if (savedHistory) {
    history.value = JSON.parse(savedHistory);
  }
});

// 监听历史记录变化，保存到localStorage
history.value.forEach((item, index) => {
  item.id = Date.now() + index; // 确保id唯一
});
localStorage.setItem('consumeHistory', JSON.stringify(history.value));
</script>

<style scoped>
.consume-history-container {
  padding: 0 20px 20px 20px;
}

.filter-bar {
  margin: 20px 0;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.total-balance {
  margin: 10px 0;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 5px;
  font-size: 16px;
}

.total-balance .label {
  font-weight: bold;
}

.total-balance .balance {
  color: #67c23a;
  font-weight: bold;
  font-size: 20px;
  margin-left: 10px;
}

.income {
  color: #67c23a;
  font-weight: bold;
}

.expense {
  color: #f56c6c;
  font-weight: bold;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}
</style>
