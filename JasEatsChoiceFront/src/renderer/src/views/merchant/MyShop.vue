<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage, ElDialog, ElForm, ElFormItem, ElInput, ElSelect, ElOption, ElUpload, ElMessageBox } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';

// 店铺基本信息
const shopInfo = ref({
  name: 'XX餐厅',
  avatar: '🏪', // 默认店铺头像
  rating: '4.8/5.0',
  address: '北京市朝阳区XX路123号',
  phone: '138XXXX8888',
  email: 'xx@jaseats.com',
  businessHours: '10:00-22:00',
  status: 'open' // open: 营业中, closed: 休息中
});

// 编辑用的临时店铺信息
const editShopInfo = ref({
  name: '',
  businessHours: '',
  address: '',
  phone: '',
  email: ''
});

// 当前优惠
const currentDiscount = ref('满30减5');

// 通知设置
const notificationSettings = ref({
  newOrder: true,
  newComment: true,
  systemNotice: true
});

// 店铺相册
const shopAlbum = ref({
  environment: 3,
  dishes: 12
});

// 头像放大弹窗
const showLargeAvatar = ref(false);

// 编辑对话框
const editDialogVisible = ref(false);

// 页面加载
onMounted(() => {
  // 模拟数据加载
});

// 切换营业状态
const toggleBusinessStatus = () => {
  shopInfo.value.status = shopInfo.value.status === 'open' ? 'closed' : 'open';
  ElMessage.success(`店铺已${shopInfo.value.status === 'open' ? '切换为营业中' : '切换为休息中'}`);
};

// 打开编辑对话框
const openEditDialog = () => {
  // 将当前店铺信息复制到编辑用的临时对象
  editShopInfo.value = { ...shopInfo.value };
  editDialogVisible.value = true;
};

// 保存编辑
const saveShopInfo = () => {
  // 数据合法性验证
  let isValid = true;

  // 验证店铺名称
  if (!editShopInfo.value.name || editShopInfo.value.name.trim() === '') {
    ElMessage.error('请填写店铺名称');
    isValid = false;
  }
  // 验证营业时间
  else if (!editShopInfo.value.businessHours || editShopInfo.value.businessHours.trim() === '') {
    ElMessage.error('请填写营业时间');
    isValid = false;
  }
  // 验证地址
  else if (!editShopInfo.value.address || editShopInfo.value.address.trim() === '') {
    ElMessage.error('请填写店铺地址');
    isValid = false;
  }
  // 验证联系方式（简单的手机号格式验证）
  else if (!/^1[3-9]\d{9}$/.test(editShopInfo.value.phone)) {
    ElMessage.error('请填写正确的手机号码');
    isValid = false;
  }
  // 验证邮箱
  else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(editShopInfo.value.email)) {
    ElMessage.error('请填写正确的邮箱地址');
    isValid = false;
  }

  // 验证通过，询问用户是否确认保存
  if (isValid) {
    ElMessageBox.confirm('确定要保存修改的店铺信息吗？', '确认保存', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    .then(() => {
      // 用户点击确定，更新店铺信息
      shopInfo.value.name = editShopInfo.value.name;
      shopInfo.value.businessHours = editShopInfo.value.businessHours;
      shopInfo.value.address = editShopInfo.value.address;
      shopInfo.value.phone = editShopInfo.value.phone;
      shopInfo.value.email = editShopInfo.value.email;

      // 关闭对话框并提示成功
      editDialogVisible.value = false;
      ElMessage.success('店铺信息已更新');
    })
    .catch(() => {
      // 用户点击取消，不保存
      ElMessage.info('已取消保存店铺信息');
    });
  }
};

// 上传照片
const handleUpload = (file) => {
  console.log('上传照片:', file);
  ElMessage.success('照片上传成功');
  return false; // 阻止自动上传
};
</script>

<template>
  <div class="merchant-shop-container">
    <div class="shop-header">
      <div class="header-left">
        <h3 class="page-title">【我的店铺】</h3>
        <el-button type="text" class="back-btn">↩ 返回</el-button>
      </div>
    </div>

    <div class="shop-content">
      <!-- 店铺基本信息 -->
      <div class="shop-info-card">
        <h4 class="card-title">🏪 店铺基本信息</h4>
        <div class="shop-avatar-section">
          <el-avatar :size="100" :src="shopInfo.avatar" class="shop-avatar" @click="showLargeAvatar = true"></el-avatar>
        </div>
        <div class="info-row">
          <span class="info-label">店铺名称：</span>
          <span class="info-value">{{ shopInfo.name }}</span>
          <span class="info-label">评分：</span>
          <span class="info-value">{{ shopInfo.rating }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">营业时间：</span>
          <span class="info-value">{{ shopInfo.businessHours }}</span>
          <span class="info-label">地址：</span>
          <span class="info-value">{{ shopInfo.address }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">联系方式：</span>
          <span class="info-value">{{ shopInfo.phone }}</span>
          <span class="info-label">邮箱：</span>
          <span class="info-value">{{ shopInfo.email }}</span>
        </div>
        <el-button type="primary" size="small" class="edit-btn" @click="openEditDialog">🔧 编辑基本信息</el-button>
      </div>

      <!-- 店铺状态管理 -->
      <div class="shop-status-card">
        <h4 class="card-title">📋 店铺状态管理</h4>
        <div class="status-row">
          <el-tag :type="shopInfo.status === 'open' ? 'success' : 'danger'">
            {{ shopInfo.status === 'open' ? '🔴 营业中' : '🔴 休息中' }}
          </el-tag>
          <el-button type="warning" size="small" @click="toggleBusinessStatus">⏱️ 切换状态</el-button>
        </div>
        <div class="discount-row">
          <span class="info-label">当前优惠：</span>
          <span class="info-value">{{ currentDiscount }}</span>
          <el-button type="text" size="small">📝 管理优惠</el-button>
        </div>
        <div class="notification-row">
          <span class="info-label">通知设置：</span>
          <span class="info-value">新订单通知{{ notificationSettings.newOrder ? '已开启' : '已关闭' }}</span>
        </div>
      </div>

      <!-- 店铺相册 -->
      <div class="shop-album-card">
        <h4 class="card-title">📸 店铺相册</h4>
        <div class="album-stats">
          <span class="stat-item">🔍 店铺环境 ({{ shopAlbum.environment }}张)</span>
          <span class="stat-item">🍽️ 菜品展示 ({{ shopAlbum.dishes }}张)</span>
        </div>
        <el-upload
          action="#"
          list-type="picture-card"
          :auto-upload="false"
          @change="handleUpload"
        >
          <el-icon class="avatar-uploader-icon">
            <Plus />
          </el-icon>
          <template #tip>
            <div class="el-upload__tip">仅支持 JPG/PNG 格式，且不超过 5MB</div>
          </template>
        </el-upload>
      </div>

      <!-- 快捷设置 -->
      <div class="quick-settings-card">
        <h4 class="card-title">🎯 快捷设置</h4>
        <div class="settings-grid">
          <el-button type="primary" size="small">⏱️ 调整营业时间</el-button>
          <el-button type="primary" size="small">📝 设置公告</el-button>
          <el-button type="primary" size="small">💰 设置配送费</el-button>
          <el-button type="primary" size="small">📞 联系平台</el-button>
        </div>
      </div>
    </div>

    <!-- 编辑基本信息对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑店铺基本信息"
      width="600px"
      top="10%"
    >
      <el-form :model="editShopInfo" label-width="100px" status-icon>
        <el-form-item label="店铺名称" prop="name" required>
          <el-input v-model="editShopInfo.name" placeholder="请输入店铺名称" />
        </el-form-item>
        <el-form-item label="营业时间" prop="businessHours" required>
          <el-input v-model="editShopInfo.businessHours" placeholder="请输入营业时间" />
        </el-form-item>
        <el-form-item label="地址" prop="address" required>
          <el-input v-model="editShopInfo.address" placeholder="请输入店铺地址" />
        </el-form-item>
        <el-form-item label="联系方式" prop="phone" required>
          <el-input v-model="editShopInfo.phone" placeholder="请输入联系方式" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email" required>
          <el-input v-model="editShopInfo.email" placeholder="请输入邮箱" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveShopInfo">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 头像放大对话框 -->
    <el-dialog v-model="showLargeAvatar" title="店铺头像" width="350px" top="20%">
      <div style="text-align: center; padding: 20px 0;">
        <el-avatar :size="250" :src="shopInfo.avatar"></el-avatar>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button type="primary" @click="showLargeAvatar = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="less">
.merchant-shop-container {
  padding: 0 20px 20px 20px;

  .shop-header {
    margin-bottom: 20px;

    .page-title {
      font-size: 18px;
      font-weight: 600;
      margin: 0;
    }
  }

  .shop-content {
    .shop-info-card, .shop-status-card, .shop-album-card, .quick-settings-card {
      background-color: #fff;
      border-radius: 8px;
      padding: 16px;
      margin-bottom: 20px;
      box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);

      .card-title {
        font-size: 16px;
        font-weight: 600;
        margin-bottom: 16px;
      }

      .shop-avatar-section {
        margin-bottom: 20px;
        text-align: center;
      }

      .shop-avatar {
        cursor: pointer;
        border: 2px solid #eee;
      }
    }

    .info-row {
      display: flex;
      gap: 24px;
      margin-bottom: 8px;

      .info-label {
        color: #606266;
      }
    }

    .status-row {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 16px;
    }

    .discount-row {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 8px;

      .info-label {
        color: #606266;
      }
    }

    .notification-row {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 8px;

      .info-label {
        color: #606266;
      }
    }

    .album-stats {
      display: flex;
      gap: 24px;
      margin-bottom: 16px;

      .stat-item {
        color: #606266;
      }
    }

    .settings-grid {
      display: flex;
      gap: 12px;
      flex-wrap: wrap;
    }
  }
}
</style>