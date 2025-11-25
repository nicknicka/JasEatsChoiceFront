<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';

// 菜品状态映射
const dishStatusMap = {
  online: { text: '🟢 在售', type: 'success' },
  almost_sold: { text: '🟡 即将售罄', type: 'warning' },
  offline: { text: '🔴 下架', type: 'danger' }
};

// 模拟菜品数据
const dishesList = ref([
  { id: 1, name: '麻辣香锅饭', price: 18, category: '主食', status: 'online', stock: 50, updateTime: '2024-11-21 10:00' },
  { id: 2, name: '鱼香肉丝面', price: 16, category: '主食', status: 'online', stock: 30, updateTime: '2024-11-21 14:00' },
  { id: 3, name: '宫保鸡丁饭', price: 18, category: '主食', status: 'almost_sold', stock: 5, updateTime: '2024-11-20 22:00' },
  { id: 4, name: '酸辣汤', price: 8, category: '汤品', status: 'online', stock: 100, updateTime: '2024-11-21 09:30' },
  { id: 5, name: '可乐', price: 3, category: '饮料', status: 'offline', stock: 0, updateTime: '2024-11-20 18:00' }
]);

const loading = ref(false);
const searchKeyword = ref('');
const activeStatusFilter = ref('all');
const selectedDishes = ref([]);

// 页面加载时初始化
onMounted(() => {
  loading.value = true;
  // 模拟异步加载
  setTimeout(() => {
    loading.value = false;
  }, 500);
});

// 筛选菜品
const filteredDishes = ref([]);
filteredDishes.value = [...dishesList.value];

// 更新筛选
const updateFilter = () => {
  filteredDishes.value = dishesList.value.filter(dish => {
    // 状态筛选
    if (activeStatusFilter.value !== 'all' && dish.status !== activeStatusFilter.value) {
      return false;
    }

    // 搜索筛选
    if (searchKeyword.value && !dish.name.includes(searchKeyword.value) && !dish.category.includes(searchKeyword.value)) {
      return false;
    }

    return true;
  });
};

// 切换状态
const toggleDishStatus = (dish) => {
  let newStatus = '';

  if (dish.status === 'online') {
    newStatus = 'offline';
  } else if (dish.status === 'offline' || dish.status === 'almost_sold') {
    newStatus = 'online';
  }

  dish.status = newStatus;
  updateFilter();
  ElMessage.success(`菜品已${dishStatusMap[newStatus].text}`);
};

// 编辑菜品
const editDish = (dish) => {
  console.log('编辑菜品:', dish);
  ElMessage.info('编辑菜品功能开发中');
};

// 删除菜品
const deleteDish = (dish) => {
  ElMessageBox.confirm('确定要删除该菜品吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  .then(() => {
    const index = dishesList.value.findIndex(item => item.id === dish.id);
    if (index !== -1) {
      dishesList.value.splice(index, 1);
      updateFilter();
      ElMessage.success('菜品已删除');
    }
  })
  .catch(() => {
    ElMessage.info('已取消删除');
  });
};

// 批量操作
const batchOperation = (operation) => {
  if (selectedDishes.value.length === 0) {
    ElMessage.warning('请先选择菜品');
    return;
  }

  switch (operation) {
    case 'online':
      selectedDishes.value.forEach(dish => {
        dish.status = 'online';
      });
      ElMessage.success('批量上架成功');
      break;
    case 'offline':
      selectedDishes.value.forEach(dish => {
        dish.status = 'offline';
      });
      ElMessage.success('批量下架成功');
      break;
    case 'delete':
      ElMessageBox.confirm('确定要删除所选菜品吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      .then(() => {
        dishesList.value = dishesList.value.filter(dish => !selectedDishes.value.includes(dish));
        selectedDishes.value = [];
        updateFilter();
        ElMessage.success('批量删除成功');
      })
      .catch(() => {});
      return;
  }

  updateFilter();
  selectedDishes.value = [];
};

// 新增菜品对话框
const addDishDialogVisible = ref(false);

// 新菜品表单数据
const newDish = ref({
  name: '',
  price: 0,
  category: '主食',
  status: 'online',
  stock: 100
});

// 打开添加菜品对话框
const openAddDishDialog = () => {
  addDishDialogVisible.value = true;
};

// 保存新菜品
const saveNewDish = () => {
  // 简单的表单验证
  if (!newDish.value.name.trim()) {
    ElMessage.warning('请填写菜品名称');
    return;
  }

  // 创建新菜品对象
  const newDishObj = {
    id: Date.now(),
    name: newDish.value.name,
    price: newDish.value.price,
    category: newDish.value.category,
    status: newDish.value.status,
    stock: newDish.value.stock,
    updateTime: new Date().toISOString().slice(0, 19).replace('T', ' ')
  };

  // 添加到菜品列表
  dishesList.value.push(newDishObj);
  updateFilter();
  addDishDialogVisible.value = false;
  ElMessage.success('菜品已添加');
};

// 选择/取消选择单个菜品
const toggleDishSelection = (dish) => {
  const index = selectedDishes.value.findIndex(item => item.id === dish.id);

  if (index === -1) {
    selectedDishes.value.push(dish);
  } else {
    selectedDishes.value.splice(index, 1);
  }
};

// 全选/取消全选
const toggleSelectAll = () => {
  if (selectedDishes.value.length === filteredDishes.value.length) {
    selectedDishes.value = [];
  } else {
    selectedDishes.value = [...filteredDishes.value];
  }
};
</script>

<template>
  <div class="dish-management-container">
    <div class="dish-header">
      <div class="header-left">
        <h3 class="page-title">【菜品管理】</h3>
      </div>
      <div class="header-right">
        <el-input
          v-model="searchKeyword"
          placeholder="输入菜品名称或分类..."
          style="width: 300px; margin-right: 10px;"
          @input="updateFilter"
        />
        <el-button type="primary" @click="openAddDishDialog">
          <span>➕</span>
          新增菜品
        </el-button>
      </div>
    </div>

    <div class="dish-filters">
      <div class="filter-section">
        <span class="filter-label">📋 状态筛选：</span>
        <el-tag
          v-for="status in ['all', 'online', 'almost_sold', 'offline']"
          :key="status"
          :type="activeStatusFilter === status ? 'primary' : 'info'"
          effect="plain"
          @click="activeStatusFilter = status; updateFilter()"
          class="status-filter"
        >
          {{ status === 'all' ? '全部菜品' : dishStatusMap[status].text }}
        </el-tag>
      </div>
    </div>

    <div class="dish-list">
      <div class="dish-item" v-for="dish in filteredDishes" :key="dish.id">
        <div class="dish-selection">
          <el-checkbox
            :checked="selectedDishes.includes(dish)"
            @change="toggleDishSelection(dish)"
          />
        </div>

        <div class="dish-content">
          <div class="dish-info">
            <div class="dish-name">
              <span class="name">{{ dish.name }}</span>
              <el-tag :type="dishStatusMap[dish.status].type">
                {{ dishStatusMap[dish.status].text }}
              </el-tag>
            </div>

            <div class="dish-stats">
              <span class="dish-category">🍽️ 分类：{{ dish.category }}</span>
              <span class="dish-price">💰 价格：¥{{ dish.price }}</span>
              <span class="dish-stock">📦 库存：{{ dish.stock }}</span>
              <span class="update-time">⏰ 更新时间：{{ dish.updateTime }}</span>
            </div>
          </div>

          <div class="dish-actions">
            <el-button
              type="primary"
              size="small"
              @click="toggleDishStatus(dish)"
            >
              {{ dish.status === 'online' ? '🔴 下架' : '🟢 上架' }}
            </el-button>

            <el-button
              type="warning"
              size="small"
              @click="editDish(dish)"
            >
              ✏️ 编辑
            </el-button>

            <el-button
              type="danger"
              size="small"
              @click="deleteDish(dish)"
            >
              🗑️ 删除
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <div class="batch-actions" v-if="filteredDishes.length > 0">
      <span class="select-all">
        <el-checkbox
          :checked="selectedDishes.length === filteredDishes.length"
          @change="toggleSelectAll"
        />
        全选
      </span>

      <el-button
        type="success"
        size="small"
        @click="batchOperation('online')"
        :disabled="selectedDishes.length === 0"
      >
        🟢 批量上架
      </el-button>

      <el-button
        type="warning"
        size="small"
        @click="batchOperation('offline')"
        :disabled="selectedDishes.length === 0"
      >
        🔴 批量下架
      </el-button>

      <el-button
        type="danger"
        size="small"
        @click="batchOperation('delete')"
        :disabled="selectedDishes.length === 0"
      >
        🗑️ 批量删除
      </el-button>
    </div>

    <!-- 空数据提示 -->
    <el-empty v-if="filteredDishes.length === 0" description="暂无菜品"></el-empty>

    <!-- 添加菜品对话框 -->
    <el-dialog
      v-model="addDishDialogVisible"
      title="添加新菜品"
      width="600px"
      top="10%"
    >
      <el-form :model="newDish" label-width="100px" status-icon>
        <el-form-item label="名称" prop="name" required>
          <el-input v-model="newDish.name" placeholder="请输入菜品名称" />
        </el-form-item>

        <el-form-item label="价格" prop="price" required>
          <el-input v-model.number="newDish.price" placeholder="请输入价格" type="number" />
        </el-form-item>

        <el-form-item label="分类" prop="category" required>
          <el-select v-model="newDish.category" style="width: 100%;">
            <el-option label="主食" value="主食" />
            <el-option label="汤品" value="汤品" />
            <el-option label="饮料" value="饮料" />
            <el-option label="小吃" value="小吃" />
          </el-select>
        </el-form-item>

        <el-form-item label="库存" prop="stock" required>
          <el-input v-model.number="newDish.stock" placeholder="请输入库存" type="number" />
        </el-form-item>

        <el-form-item label="状态">
          <el-select v-model="newDish.status" style="width: 100%;">
            <el-option label="上架" value="online" />
            <el-option label="下架" value="offline" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addDishDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveNewDish">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="less">
.dish-management-container {
  padding: 0 20px 20px 20px;

  .dish-header {
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

  .dish-filters {
    margin-bottom: 24px;

    .filter-section {
      display: flex;
      align-items: center;
      gap: 12px;

      .filter-label {
        font-weight: 500;
      }

      .status-filter {
        cursor: pointer;

        &:hover {
          opacity: 0.8;
        }
      }
    }
  }

  .dish-list {
    margin-bottom: 20px;

    .dish-item {
      display: flex;
      align-items: flex-start;
      padding: 16px;
      border: 1px solid #e4e7ed;
      border-radius: 4px;
      margin-bottom: 12px;
      background-color: #fff;
      transition: box-shadow 0.3s;

      &:hover {
        box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
      }

      .dish-selection {
        margin-top: 4px;
        margin-right: 16px;
      }

      .dish-content {
        flex: 1;
        display: flex;
        justify-content: space-between;

        .dish-info {
          .dish-name {
            display: flex;
            align-items: center;
            gap: 10px;
            margin-bottom: 12px;

            .name {
              font-size: 16px;
              font-weight: 600;
            }
          }

          .dish-stats {
            display: flex;
            flex-wrap: wrap;
            gap: 24px;
            margin-bottom: 8px;
            font-size: 14px;

            .dish-category, .dish-price, .dish-stock {
              color: #606266;
            }
          }
        }

        .dish-actions {
          display: flex;
          flex-direction: column;
          gap: 8px;
          justify-content: flex-start;

          button {
            width: 100px;
          }
        }
      }
    }
  }

  .batch-actions {
    display: flex;
    align-items: center;
    gap: 16px;

    .select-all {
      display: flex;
      align-items: center;
      gap: 8px;
      font-weight: 500;
    }
  }

  .dialog-footer {
    text-align: right;
  }
}
</style>
