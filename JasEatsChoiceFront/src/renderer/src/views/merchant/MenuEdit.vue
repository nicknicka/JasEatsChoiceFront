<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElDatePicker, ElSelect, ElOption, ElInput } from 'element-plus';
import CommonBackButton from '../../components/common/CommonBackButton.vue';

const route = useRoute();
const router = useRouter();

// 菜单基本信息
const menuInfo = ref({
  name: '午餐菜单',
  description: '精选午餐菜品，营养美味',
  autoOnline: '2024-11-22 11:00',
  autoOffline: '2024-11-22 14:00',
  status: 'online' // online: 上架中, draft: 草稿, offline: 下架中
});

// 菜单状态映射
const menuStatusMap = {
  online: { text: '🟢 上架中', type: 'success' },
  draft: { text: '🟡 草稿', type: 'warning' },
  offline: { text: '🔴 下架中', type: 'danger' }
};

// 菜品列表
const dishesList = ref([
  { id: 1, name: '麻辣香锅饭', price: 18, status: 'online', statusText: '🟢 在售' },
  { id: 2, name: '鱼香肉丝面', price: 16, status: 'online', statusText: '🟢 在售' },
  { id: 3, name: '宫保鸡丁饭', price: 18, status: 'almost_sold', statusText: '🟡 即将售罄' }
]);

// 搜索关键词
const searchKeyword = ref('');

// 模拟所有菜单数据（与Menu.vue保持一致）
const allMenus = ref([
  {
    id: 1,
    name: '午餐菜单',
    dishes: 12,
    status: 'online',
    updateTime: '2024-11-21 10:00',
    autoOnline: '2024-11-22 11:00',
    autoOffline: '2024-11-22 14:00',
    description: '精选午餐菜品，营养美味'
  },
  {
    id: 2,
    name: '晚餐菜单',
    dishes: 8,
    status: 'offline',
    updateTime: '2024-11-21 14:00',
    autoOnline: '',
    autoOffline: '',
    description: '美味晚餐，让您回味无穷'
  },
  {
    id: 3,
    name: '夜宵菜单',
    dishes: 5,
    status: 'draft',
    updateTime: '2024-11-20 22:00',
    autoOnline: '',
    autoOffline: '',
    description: '深夜美食，满足您的味蕾'
  }
]);

// 页面加载
onMounted(() => {
  // 从路由参数获取菜单ID并加载菜单数据
  const menuId = parseInt(route.query.menuId);
  if (menuId) {
    // 根据菜单ID查找菜单
    const menu = allMenus.value.find(m => m.id === menuId);
    if (menu) {
      // 更新菜单信息
      menuInfo.value = {
        name: menu.name,
        description: menu.description || '',
        autoOnline: menu.autoOnline,
        autoOffline: menu.autoOffline,
        status: menu.status
      };
      console.log('加载菜单:', menu);
    }
  }
});

// 保存菜单
const saveMenu = (saveType) => {
  // 根据保存类型更新菜单状态
  switch (saveType) {
    case 'online':
      menuInfo.value.status = 'online';
      break;
    case 'offline':
      menuInfo.value.status = 'offline';
      break;
    case 'draft':
      menuInfo.value.status = 'draft';
      break;
  }

  // 模拟保存
  console.log('保存菜单:', menuInfo.value);
  ElMessage.success('菜单保存成功');

  // 跳回菜单管理页面
  router.push('/merchant/home/menu');
};

// 移除菜品
const removeDish = (dish) => {
  const index = dishesList.value.findIndex(item => item.id === dish.id);
  if (index !== -1) {
    dishesList.value.splice(index, 1);
    ElMessage.success('菜品已移除');
  }
};

// 模拟可用菜品数据
const availableDishes = ref([
  { id: 1, name: '麻辣香锅饭', price: 18, status: 'online', statusText: '🟢 在售' },
  { id: 2, name: '鱼香肉丝面', price: 16, status: 'online', statusText: '🟢 在售' },
  { id: 3, name: '宫保鸡丁饭', price: 18, status: 'almost_sold', statusText: '🟡 即将售罄' },
  { id: 4, name: '酸辣汤', price: 8, status: 'online', statusText: '🟢 在售' },
  { id: 5, name: '可乐', price: 3, status: 'offline', statusText: '🔴 下架' },
  { id: 6, name: '红烧肉饭', price: 20, status: 'online', statusText: '🟢 在售' },
  { id: 7, name: '炒青菜', price: 10, status: 'online', statusText: '🟢 在售' },
  { id: 8, name: '番茄鸡蛋面', price: 15, status: 'online', statusText: '🟢 在售' }
]);

// 添加菜品对话框
const showAddDishDialog = ref(false);
const selectedDish = ref(null);

// 批量关联菜品对话框
const showBatchAssociateDialog = ref(false);
const selectedDishesBatch = ref([]);

// 添加菜品
const addDish = () => {
  if (selectedDish.value) {
    // 检查菜品是否已在菜单中
    const isExist = dishesList.value.some(dish => dish.id === selectedDish.value.id);
    if (!isExist) {
      dishesList.value.push({ ...selectedDish.value });
      ElMessage.success('菜品已添加');
    } else {
      ElMessage.warning('该菜品已在菜单中');
    }
    // 重置状态
    showAddDishDialog.value = false;
    selectedDish.value = null;
  }
};

// 批量关联菜品
const batchAssociateDishes = () => {
  if (selectedDishesBatch.value.length > 0) {
    let addedCount = 0;
    let existingCount = 0;

    selectedDishesBatch.value.forEach(dish => {
      const isExist = dishesList.value.some(existingDish => existingDish.id === dish.id);
      if (!isExist) {
        dishesList.value.push({ ...dish });
        addedCount++;
      } else {
        existingCount++;
      }
    });

    // 显示结果信息
    const messages = [];
    if (addedCount > 0) messages.push(`${addedCount} 个菜品已成功关联`);
    if (existingCount > 0) messages.push(`${existingCount} 个菜品已在菜单中`);

    if (messages.length > 0) {
      ElMessage.success(messages.join('；'));
    }

    // 重置状态
    showBatchAssociateDialog.value = false;
    selectedDishesBatch.value = [];
  }
};
</script>

<template>
  <div class="menu-edit-container">
    <div class="menu-edit-header">
      <div class="header-left">
        <h3 class="page-title">【菜单编辑】</h3>
        <common-back-button type="text" class="back-btn" />
      </div>
    </div>

    <div class="menu-edit-content">
      <!-- 菜单基本信息 -->
      <div class="menu-info-section">
        <h4 class="section-title">📝 菜单基本信息</h4>
        <div class="info-item">
          <span class="info-label">🍽️ 菜单名称：</span>
          <el-input v-model="menuInfo.name" placeholder="请输入菜单名称" style="width: 300px;" />
        </div>
        <div class="info-item">
          <span class="info-label">📝 菜单描述：</span>
          <el-input v-model="menuInfo.description" placeholder="请输入菜单描述" style="width: 500px;" />
        </div>
        <div class="info-item">
          <span class="info-label">📅 自动上架时间：</span>
          <el-date-picker
            v-model="menuInfo.autoOnline"
            type="datetime"
            placeholder="选择自动上架时间"
            style="width: 200px;"
          />
          <el-button type="text" size="small">⏰ 设置</el-button>
        </div>
        <div class="info-item">
          <span class="info-label">📅 自动下架时间：</span>
          <el-date-picker
            v-model="menuInfo.autoOffline"
            type="datetime"
            placeholder="选择自动下架时间"
            style="width: 200px;"
          />
          <el-button type="text" size="small">⏰ 设置</el-button>
        </div>
        <div class="info-item">
          <span class="info-label">📋 菜单状态：</span>
          <el-select v-model="menuInfo.status" placeholder="选择菜单状态" style="width: 200px;">
            <el-option
              v-for="(status, key) in menuStatusMap"
              :key="key"
              :label="status.text"
              :value="key"
            />
          </el-select>
        </div>
      </div>

      <!-- 菜品管理 -->
      <div class="dishes-section">
        <h4 class="section-title">🍴 菜品管理</h4>
        <div class="dishes-header">
          <el-input
            v-model="searchKeyword"
            placeholder="输入菜品名称..."
            style="width: 250px;"
            class="dishes-search"
          />
          <el-button type="primary" size="small" @click="showAddDishDialog = true">➕ 添加菜品</el-button>
          <el-button type="info" size="small" @click="showBatchAssociateDialog = true">🔗 批量关联菜品</el-button>
        </div>
        <div class="dishes-list">
          <div
            v-for="dish in dishesList"
            :key="dish.id"
            class="dish-item"
          >
            <span class="dish-info">{{ dish.name }} | ¥{{ dish.price }} | {{ dish.statusText }}</span>
            <el-button
              type="danger"
              size="small"
              @click="removeDish(dish)"
            >
              🗑️ 移除
            </el-button>
          </div>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="action-buttons">
        <el-button type="success" @click="saveMenu('online')">💾 保存菜单并上架</el-button>
        <el-button type="warning" @click="saveMenu('offline')">💾 保存菜单并下架</el-button>
        <el-button type="info" @click="saveMenu('draft')">💾 保存为草稿</el-button>
        <CommonBackButton type="text" text="🔙 取消编辑" />
      </div>

      <!-- 添加菜品对话框 -->
      <el-dialog
        v-model="showAddDishDialog"
        title="添加菜品"
        width="600px"
        top="10%"
      >
        <div class="dialog-content">
          <el-select
            v-model="selectedDish"
            placeholder="请选择要添加的菜品"
            style="width: 100%;"
            filterable
            clearable
          >
            <el-option
              v-for="dish in availableDishes"
              :key="dish.id"
              :label="`${dish.name} - ¥${dish.price} ${dish.statusText}`"
              :value="dish"
            />
          </el-select>
        </div>
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="showAddDishDialog = false">取消</el-button>
            <el-button type="primary" @click="addDish">确定添加</el-button>
          </div>
        </template>
      </el-dialog>

      <!-- 批量关联菜品对话框 -->
      <el-dialog
        v-model="showBatchAssociateDialog"
        title="批量关联菜品"
        width="600px"
        top="10%"
      >
        <div class="dialog-content">
          <el-select
            v-model="selectedDishesBatch"
            multiple
            placeholder="请选择要关联的菜品"
            style="width: 100%;"
            filterable
            clearable
            collapse-tags
            collapse-tags-tooltip
          >
            <el-option
              v-for="dish in availableDishes"
              :key="dish.id"
              :label="`${dish.name} - ¥${dish.price} ${dish.statusText}`"
              :value="dish"
            /> 
          </el-select>
        </div>
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="showBatchAssociateDialog = false">取消</el-button>
            <el-button type="primary" @click="batchAssociateDishes">确定关联</el-button>
          </div>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<style scoped lang="less">
.menu-edit-container {
  padding: 0 20px 20px 20px;

  .menu-edit-header {
    margin-bottom: 20px;

    .page-title {
      font-size: 18px;
      font-weight: 600;
      margin: 0;
    }
  }

  .menu-edit-content {
    .menu-info-section, .dishes-section {
      background-color: #fff;
      border-radius: 8px;
      padding: 16px;
      margin-bottom: 20px;
      box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
    }

    .section-title {
      font-size: 16px;
      font-weight: 600;
      margin-bottom: 16px;
    }

    .info-item {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 16px;

      .info-label {
        color: #606266;
        width: 120px;
      }
    }

    .dishes-header {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 16px;

      .dishes-search {
        margin-right: auto;
      }
    }

    .dishes-list {
      .dish-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 8px;
        border: 1px solid #e4e7ed;
        border-radius: 4px;
        margin-bottom: 8px;

        .dish-info {
          color: #303133;
        }
      }
    }

    .action-buttons {
      display: flex;
      gap: 12px;
    }
  }
}
</style>