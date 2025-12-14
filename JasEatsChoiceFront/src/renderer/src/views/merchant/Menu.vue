<script setup>
import { ref, onMounted, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useRouter } from 'vue-router';
import axios from 'axios';
import { API_CONFIG } from '../../config/index.js';

const router = useRouter();
// 菜单状态映射
const menuStatusMap = {
  online: { text: '上架中', icon: '🟢', type: 'success' },
  draft: { text: '草稿', icon: '🟡', type: 'warning' },
  offline: { text: '下架中', icon: '🔴', type: 'danger' }
};

// 菜单数据
const menuList = ref([]);

const loading = ref(false);
const searchKeyword = ref('');
const activeStatusFilter = ref('all');

// 筛选菜单
const filteredMenus = ref([]);

// 页面加载时初始化
onMounted(() => {
  loading.value = true;
  // 从localStorage获取商家ID
  const merchantId = localStorage.getItem('merchantId');
  if (!merchantId) {
    ElMessage.error('未检测到商家ID，请重新登录');
    router.push('/merchant/login');
    return;
  }
  // 从API获取菜单数据
  axios.get(`${API_CONFIG.baseURL}${API_CONFIG.merchant.menu.replace('{merchantId}', merchantId)}`)
    .then(response => {
      if (response.data && response.data.success) {
        menuList.value = response.data.data;
        filteredMenus.value = [...menuList.value]; // 更新筛选后的菜单
      }
    })
    .catch(error => {
      console.error('加载菜单失败:', error);
      ElMessage.error('加载菜单失败');
    })
    .finally(() => {
      loading.value = false;
    });
});

// 更新筛选
const updateFilter = () => {
  filteredMenus.value = menuList.value.filter(menu => {
    // 状态筛选
    if (activeStatusFilter.value !== 'all' && menu.status !== activeStatusFilter.value) {
      return false;
    }

    // 搜索筛选
    if (searchKeyword.value && !menu.name.includes(searchKeyword.value)) {
      return false;
    }

    return true;
  });
};

// 监听filteredMenus变化，确保全选状态正确更新
watch(() => filteredMenus.value, () => {
  // 如果过滤后的菜单数量减少，且当前选中的菜单数量等于过滤前的数量，那么需要调整选中的菜单
  if (selectedMenus.value.length > filteredMenus.value.length) {
    selectedMenus.value = selectedMenus.value.filter(menu => filteredMenus.value.includes(menu));
  }
}, { deep: true });

// 切换状态
const toggleMenuStatus = (menu) => {
  let newStatus = '';

  if (menu.status === 'online') {
    newStatus = 'offline';
  } else if (menu.status === 'offline' || menu.status === 'draft') {
    newStatus = 'online';
  }

  menu.status = newStatus;
  updateFilter();
  ElMessage.success(`菜单已${menuStatusMap[newStatus].text}`);
};

// 编辑菜单
const editMenu = (menu) => {
  console.log('编辑菜单:', menu);
  // 导航到菜单编辑页面并传递菜单ID
  router.push({ path: '/merchant/home/menu-edit', query: { menuId: menu.id } });
};

// 删除菜单
const deleteMenu = (menu) => {
  ElMessageBox.confirm('确定要删除该菜单吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  .then(() => {
    const index = menuList.value.findIndex(item => item.id === menu.id);
    if (index !== -1) {
      menuList.value.splice(index, 1);
      updateFilter();
      ElMessage.success('菜单已删除');
    }
  })
  .catch(() => {
    ElMessage.info('已取消删除');
  });
};

// 批量操作
const selectedMenus = ref([]);

const batchOperation = (operation) => {
  if (selectedMenus.value.length === 0) {
    ElMessage.warning('请先选择菜单');
    return;
  }

  switch (operation) {
    case 'online':
      selectedMenus.value.forEach(menu => {
        menu.status = 'online';
      });
      ElMessage.success('批量上架成功');
      break;
    case 'offline':
      selectedMenus.value.forEach(menu => {
        menu.status = 'offline';
      });
      ElMessage.success('批量下架成功');
      break;
    case 'delete':
      ElMessageBox.confirm('确定要删除所选菜单吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      .then(() => {
        menuList.value = menuList.value.filter(menu => !selectedMenus.value.includes(menu));
        selectedMenus.value = [];
        updateFilter();
        ElMessage.success('批量删除成功');
      })
      .catch(() => {});
      return;
  }

  updateFilter();
  selectedMenus.value = [];
};

// 导出菜单
const exportMenu = (menu) => {
  console.log('导出菜单:', menu);
  ElMessage.info('导出菜单功能开发中');
};

// 选择/取消选择单个菜单
const toggleMenuSelection = (menu) => {
  const index = selectedMenus.value.findIndex(item => item.id === menu.id);

  if (index === -1) {
    selectedMenus.value.push(menu);
  } else {
    selectedMenus.value.splice(index, 1);
  }
};

// 新增菜单对话框
const addMenuDialogVisible = ref(false);

// 新菜单表单数据
const newMenu = ref({
  name: '',
  category: 'lunch',
  autoOnline: '',
  autoOffline: '',
  status: 'online'
});

// 打开添加菜单对话框
const openAddMenuDialog = () => {
  addMenuDialogVisible.value = true;

  // 重置表单数据
  newMenu.value = {
    name: '',
    category: 'lunch',
    autoOnline: '',
    autoOffline: '',
    status: 'online'
  };
};

// 保存新菜单
const saveNewMenu = () => {
  // 简单的表单验证
  if (!newMenu.value.name.trim()) {
    ElMessage.warning('请填写菜单名称');
    return;
  }

  // 从localStorage获取商家ID
  const merchantId = localStorage.getItem('merchantId');
  if (!merchantId) {
    ElMessage.error('未检测到商家ID，请重新登录');
    router.push('/merchant/login');
    return;
  }

  // 准备请求参数
  const menuData = {
    name: newMenu.value.name,
    category: newMenu.value.category,
    status: newMenu.value.status,
    autoOnline: newMenu.value.autoOnline,
    autoOffline: newMenu.value.autoOffline
  };

  // 发送POST请求到后端保存菜单
  axios.post(`${API_CONFIG.baseURL}${API_CONFIG.merchant.menu.replace('{merchantId}', merchantId)}`, menuData)
    .then(response => {
      if (response.data && response.data.success) {
        // 从响应中获取完整的菜单对象
        const savedMenu = response.data.data;

        // 添加到菜单列表
        menuList.value.push(savedMenu);
        updateFilter();
        addMenuDialogVisible.value = false;
        ElMessage.success('菜单已添加');
      }
    })
    .catch(error => {
      console.error('保存菜单失败:', error);
      ElMessage.error('保存菜单失败');
    });
};

// 检查全选状态：0=未选择，1=部分选择，2=全选
const getSelectAllState = () => {
  if (selectedMenus.value.length === 0) {
    return 0;
  } else if (selectedMenus.value.length === filteredMenus.value.length && filteredMenus.value.length > 0) {
    return 2;
  } else {
    return 1;
  }
};

// 全选/取消全选
const toggleSelectAll = () => {
  const currentState = getSelectAllState();

  if (currentState === 2) {
    // 当前是全选状态，点击后取消全选
    selectedMenus.value = [];
  } else {
    // 当前是未选或部分选择状态，点击后全选
    selectedMenus.value = [...filteredMenus.value];
  }

  // 触发Vue的响应式更新
  selectedMenus.value = [...selectedMenus.value];
};
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
          placeholder="输入菜单名称..."
          style="width: 300px; margin-right: 10px;"
          @input="updateFilter"
        />
        <el-button type="primary" @click="openAddMenuDialog">
          <span>➕</span>
          新增菜单
        </el-button>
      </div>
    </div>

    <div class="menu-filters">
      <div class="filter-section">
        <span class="filter-label">📋 状态筛选：</span>
        <el-tag
          v-for="status in ['all', 'online', 'draft', 'offline']"
          :key="status"
          :type="activeStatusFilter === status ? 'primary' : 'info'"
          effect="plain"
          @click="activeStatusFilter = status; updateFilter()"
          class="status-filter"
        >
          {{ status === 'all' ? '全部菜单' :
             status === 'online' ? `${menuStatusMap[status].icon} ${menuStatusMap[status].text}` :
             status === 'draft' ? `${menuStatusMap[status].icon} ${menuStatusMap[status].text}` :
             `${menuStatusMap[status].icon} ${menuStatusMap[status].text}` }}
        </el-tag>
      </div>
    </div>

    <div class="menu-list">
      <div class="menu-item" v-for="menu in filteredMenus" :key="menu.id">
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
                {{ menuStatusMap[menu.status].icon }} {{ menuStatusMap[menu.status].text }}
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

          <div class="menu-actions">
            <el-button
              type="primary"
              size="small"
              @click="toggleMenuStatus(menu)"
            >
              {{ menu.status === 'online' ? '🔴 下架菜单' : '🟢 上架菜单' }}
            </el-button>

            <el-button
              type="warning"
              size="small"
              @click="editMenu(menu)"
            >
              ✏️ 编辑
            </el-button>

            <el-button
              type="danger"
              size="small"
              @click="deleteMenu(menu)"
            >
              🗑️ 删除
            </el-button>

            <el-button
              type="info"
              size="small"
              @click="exportMenu(menu)"
            >
              📤 导出菜单
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <div class="batch-actions" v-if="filteredMenus.length > 0">
      <span class="select-all">
        <el-checkbox
          :indeterminate="getSelectAllState() === 1"
          :model-value="getSelectAllState() === 2"
          @change="toggleSelectAll"
        />
        全选
      </span>

      <el-button
        type="success"
        size="small"
        @click="batchOperation('online')"
        :disabled="selectedMenus.length === 0"
      >
        🟢 批量上架
      </el-button>

      <el-button
        type="warning"
        size="small"
        @click="batchOperation('offline')"
        :disabled="selectedMenus.length === 0"
      >
        🔴 批量下架
      </el-button>

      <el-button
        type="danger"
        size="small"
        @click="batchOperation('delete')"
        :disabled="selectedMenus.length === 0"
      >
        🗑️ 批量删除
      </el-button>
    </div>

    <!-- 空数据提示 -->
    <el-empty v-if="filteredMenus.length === 0" description="暂无菜单"></el-empty>

    <!-- 添加菜单对话框 -->
    <el-dialog
      v-model="addMenuDialogVisible"
      title="添加新菜单"
      width="600px"
      top="10%"
    >
      <el-form :model="newMenu" label-width="100px" status-icon>
        <el-form-item label="名称" prop="name" required>
          <el-input v-model="newMenu.name" placeholder="请输入菜单名称" />
        </el-form-item>

        <el-form-item label="分类" prop="category" required>
          <el-select v-model="newMenu.category" style="width: 100%;">
            <el-option label="早餐" value="breakfast" />
            <el-option label="午餐" value="lunch" />
            <el-option label="晚餐" value="dinner" />
            <el-option label="夜宵" value="late-night" />
          </el-select>
        </el-form-item>

        <el-form-item label="自动上架时间">
          <el-time-picker
            v-model="newMenu.autoOnline"
            type="datetime"
            placeholder="选择自动上架时间"
            style="width: 100%;"
          />
        </el-form-item>

        <el-form-item label="自动下架时间">
          <el-time-picker
            v-model="newMenu.autoOffline"
            type="datetime"
            placeholder="选择自动下架时间"
            style="width: 100%;"
          />
        </el-form-item>

        <el-form-item label="状态">
          <el-select v-model="newMenu.status" style="width: 100%;">
            <el-option label="上架中" value="online" />
            <el-option label="草稿" value="draft" />
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
  padding: 0 20px 20px 20px;

  .menu-header {
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

  .menu-filters {
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

  .menu-list {
    margin-bottom: 20px;

    .menu-item {
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

      .menu-selection {
        margin-top: 4px;
        margin-right: 16px;
      }

      .menu-content {
        flex: 1;
        display: flex;
        justify-content: space-between;

        .menu-info {
          .menu-name {
            display: flex;
            align-items: center;
            gap: 10px;
            margin-bottom: 12px;

            .name {
              font-size: 16px;
              font-weight: 600;
            }
          }

          .menu-stats, .auto-times {
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
          }
        }

        .menu-actions {
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
