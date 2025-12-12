<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElUpload } from 'element-plus';
import CommonBackButton from '../../components/common/CommonBackButton.vue';

const route = useRoute();
const router = useRouter();

// 菜品基本信息
const dishInfo = ref({
  name: '麻辣香锅饭',
  category: '主食',
  price: 18,
  status: 'online', // online: 在售, almost_sold: 即将售罄, offline: 下架
  stock: 50,
  description: '精选食材，麻辣鲜香，回味无穷'
});

// 菜品分类选项
const categories = ['主食', '汤品', '饮料', '小吃'];

// 菜品状态映射
const dishStatusMap = {
  online: { text: '🟢 在售', type: 'success' },
  almost_sold: { text: '🟡 即将售罄', type: 'warning' },
  offline: { text: '🔴 下架', type: 'danger' }
};

// 页面加载
onMounted(() => {
  // 可以从路由参数获取菜品ID并加载菜品数据
});

// 保存菜品
const saveDish = (saveType) => {
  // 根据保存类型更新菜品状态
  if (saveType) {
    dishInfo.value.status = saveType;
  }

  // 模拟保存
  console.log('保存菜品:', dishInfo.value);
  ElMessage.success('菜品保存成功');

  // 跳回菜品管理页面
  router.push('/merchant/dish-management');
};

// 上传菜品图片
const handleUpload = (file) => {
  console.log('上传菜品图片:', file);
  ElMessage.success('图片上传成功');
  return false; // 阻止自动上传
};
</script>

<template>
  <div class="dish-edit-container">
    <div class="dish-edit-header">
      <div class="header-left">
        <h3 class="page-title">【菜品编辑】</h3>
        <common-back-button type="text" class="back-btn" />
      </div>
    </div>

    <div class="dish-edit-content">
      <!-- 菜品图片管理 -->
      <div class="dish-images-section">
        <h4 class="section-title">📷 菜品图片</h4>
        <el-upload
          action="#"
          list-type="picture-card"
          :auto-upload="false"
          @change="handleUpload"
        >
          <el-icon class="avatar-uploader-icon">
            <Plus />
          </el-icon>
        </el-upload>
      </div>

      <!-- 菜品基本信息 -->
      <div class="dish-info-section">
        <h4 class="section-title">📝 菜品基本信息</h4>
        <div class="info-item">
          <span class="info-label">🍽️ 菜品名称：</span>
          <el-input v-model="dishInfo.name" placeholder="请输入菜品名称" style="width: 300px;" />
        </div>
        <div class="info-item">
          <span class="info-label">📋 菜品分类：</span>
          <el-select v-model="dishInfo.category" placeholder="选择菜品分类" style="width: 200px;">
            <el-option
              v-for="category in categories"
              :key="category"
              :label="category"
              :value="category"
            />
          </el-select>
        </div>
        <div class="info-item">
          <span class="info-label">💰 价格：</span>
          <el-input-number
            v-model="dishInfo.price"
            :min="0.01"
            :step="0.01"
            style="width: 200px;"
          />
          <span class="unit">元</span>
        </div>
        <div class="info-item">
          <span class="info-label">📦 库存：</span>
          <el-input-number
            v-model="dishInfo.stock"
            :min="0"
            :step="1"
            style="width: 200px;"
          />
          <span class="unit">份</span>
        </div>
        <div class="info-item">
          <span class="info-label">📋 菜品状态：</span>
          <el-select v-model="dishInfo.status" placeholder="选择菜品状态" style="width: 200px;">
            <el-option
              v-for="(status, key) in dishStatusMap"
              :key="key"
              :label="status.text"
              :value="key"
            />
          </el-select>
        </div>
        <div class="info-item">
          <span class="info-label">📝 菜品描述：</span>
          <el-input
            v-model="dishInfo.description"
            placeholder="请输入菜品描述"
            style="width: 500px;"
            type="textarea"
            :rows="4"
          />
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="action-buttons">
        <el-button type="success" @click="saveDish('online')">💾 保存菜品并上架</el-button>
        <el-button type="warning" @click="saveDish('offline')">💾 保存菜品并下架</el-button>
        <el-button type="info" @click="saveDish()">💾 保存菜品</el-button>
        <el-button type="text" @click="$router.back()">🔙 取消编辑</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped lang="less">
.dish-edit-container {
  padding: 0 20px 20px 20px;

  .dish-edit-header {
    margin-bottom: 20px;

    .page-title {
      font-size: 18px;
      font-weight: 600;
      margin: 0;
    }
  }

  .dish-edit-content {
    .dish-images-section, .dish-info-section {
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

      .unit {
        margin-left: 8px;
        color: #606266;
      }
    }

    .action-buttons {
      display: flex;
      gap: 12px;
    }
  }
}
</style>
