<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElDatePicker, ElSelect, ElOption, ElInput } from 'element-plus';

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

// 页面加载
onMounted(() => {
  // 可以从路由参数获取菜单ID并加载菜单数据
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
  router.push('/merchant/menu');
};

// 移除菜品
const removeDish = (dish) => {
  const index = dishesList.value.findIndex(item => item.id === dish.id);
  if (index !== -1) {
    dishesList.value.splice(index, 1);
    ElMessage.success('菜品已移除');
  }
};
</script>

<template>
  <div class="menu-edit-container">
    <div class="menu-edit-header">
      <div class="header-left">
        <h3 class="page-title">【菜单编辑】</h3>
        <el-button type="text" class="back-btn" @click="$router.back()">↩ 返回</el-button>
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
          <el-button type="primary" size="small">➕ 添加菜品</el-button>
          <el-button type="info" size="small">🔗 批量关联菜品</el-button>
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
        <el-button type="text" @click="$router.back()">🔙 取消编辑</el-button>
      </div>
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