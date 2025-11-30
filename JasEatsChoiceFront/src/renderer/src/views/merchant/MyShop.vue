<script setup>
import { ref, onMounted, computed } from 'vue';
import { ElMessage, ElDialog, ElForm, ElFormItem, ElInput, ElUpload, ElMessageBox } from 'element-plus';
import { Plus, Clock, Document, Coin, Phone } from '@element-plus/icons-vue';
import axios from 'axios';
import { API_CONFIG } from '../../config/index.js';
import api, { decodeJwt } from '../../utils/api.js';

// 店铺基本信息 - 初始化为空，等待后端数据
const shopInfo = ref({
  name: '',
  avatar: '',
  rating: '',
  address: '',
  phone: '',
  email: '',
  businessHours: '',
  status: ''
});

// 编辑用的临时店铺信息
const editShopInfo = ref({
  name: '',
  avatar: '', // Add avatar field
  businessHours: '',
  address: '',
  phone: '',
  email: ''
});

// 优惠活动列表 - 初始化为空，等待后端数据
const discounts = ref([]);

// 优惠管理对话框
const discountDialogVisible = ref(false);
const currentDiscountForm = ref({});
const isEditingDiscount = ref(false);

// 批量操作选中的优惠
const selectedDiscounts = ref([]);

// 批量删除优惠
const batchDeleteDiscounts = () => {
  if (selectedDiscounts.value.length === 0) {
    ElMessage.warning('请先选择要删除的优惠');
    return;
  }

  const discountIds = selectedDiscounts.value.map(discount => discount.id);

  ElMessageBox.confirm(`确定要删除选中的 ${selectedDiscounts.value.length} 个优惠活动吗？`, '批量删除', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  .then(() => {
    // 调用后端API批量删除优惠
    api.delete(API_CONFIG.merchant.discounts.replace('{merchantId}', shopInfo.value.id), {
      data: discountIds // 发送删除的ID列表
    })
    .then(response => {
      if (response.data && response.data.success) {
        // 更新本地数据
        discounts.value = discounts.value.filter(discount => !discountIds.includes(discount.id));
        selectedDiscounts.value = [];
        ElMessage.success('优惠活动批量删除成功');
      }
    })
    .catch(error => {
      console.error('批量删除优惠活动失败:', error);
      ElMessage.error('批量删除优惠活动失败');
    });
  })
  .catch(() => {
    ElMessage.info('已取消删除');
  });
};

// 打开优惠管理对话框
const openDiscountDialog = (discount = null) => {
  discountDialogVisible.value = true;
  if (discount) {
    // 编辑模式
    isEditingDiscount.value = true;
    currentDiscountForm.value = { ...discount };
  } else {
    // 新增模式
    isEditingDiscount.value = false;
    currentDiscountForm.value = {
      name: '',
      type: '满减',
      description: '',
      status: 'active'
    };
  }
};

// 保存优惠
const saveDiscount = () => {
  // 简单的表单验证
  if (!currentDiscountForm.value.name || !currentDiscountForm.value.description) {
    ElMessage.error('请填写完整的优惠信息');
    return;
  }

  if (isEditingDiscount.value) {
    // 编辑模式 - 更新现有优惠
    api.put(API_CONFIG.merchant.discounts.replace('{merchantId}', shopInfo.value.id), currentDiscountForm.value)
      .then(response => {
        if (response.data && response.data.success) {
          // 更新本地数据
          const index = discounts.value.findIndex(d => d.id === currentDiscountForm.value.id);
          if (index !== -1) {
            discounts.value[index] = { ...currentDiscountForm.value };
          }
          ElMessage.success('优惠活动已更新');
        }
      })
      .catch(error => {
        console.error('更新优惠活动失败:', error);
        ElMessage.error('更新优惠活动失败');
      });
  } else {
    // 新增模式 - 添加新优惠
    api.post(API_CONFIG.merchant.discounts.replace('{merchantId}', shopInfo.value.id), currentDiscountForm.value)
      .then(response => {
        if (response.data && response.data.success) {
          const newDiscount = response.data.data;
          discounts.value.push(newDiscount);
          ElMessage.success('优惠活动已添加');
        }
      })
      .catch(error => {
        console.error('添加优惠活动失败:', error);
        ElMessage.error('添加优惠活动失败');
      });
  }

  discountDialogVisible.value = false;
  currentDiscountForm.value = {};
};

// 删除单个优惠
const deleteDiscount = (discount) => {
  ElMessageBox.confirm(`确定要删除优惠活动 "${discount.name}" 吗？`, '删除优惠', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  .then(() => {
    // 调用后端API删除优惠
    api.delete(`${API_CONFIG.merchant.discounts.replace('{merchantId}', shopInfo.value.id)}/${discount.id}`)
      .then(response => {
        if (response.data && response.data.success) {
          const index = discounts.value.findIndex(d => d.id === discount.id);
          if (index !== -1) {
            discounts.value.splice(index, 1);
          }
          ElMessage.success('优惠活动删除成功');
        }
      })
      .catch(error => {
        console.error('删除优惠活动失败:', error);
        ElMessage.error('删除优惠活动失败');
      });
  })
  .catch(() => {
    ElMessage.info('已取消删除');
  });
};

// 通知设置
const notificationSettings = ref({
  newOrder: true,
  newComment: true,
  systemNotice: true
});

// 店铺相册（包含模拟图片）
const shopAlbum = ref({
  environment: [
  ],
  dishes: [
  ]
});

// 头像放大弹窗
const showLargeAvatar = ref(false);

// 编辑对话框
const editDialogVisible = ref(false);

// 页面加载
onMounted(() => {
  // 从JWT令牌中获取商家ID
  const token = localStorage.getItem('token');
  let merchantId = 1; // 默认值

  if (token) {
    const decodedToken = decodeJwt(token);
    if (decodedToken && decodedToken.merchantId) {
      merchantId = decodedToken.merchantId;
    }
  }

  // 从API获取店铺信息
  api.get(`${API_CONFIG.merchant.detail}${merchantId}`)
    .then(response => {
      if (response.data && response.data.success) {
        const merchantData = response.data.data;
        // 将后端返回的数据映射到店铺信息
        shopInfo.value = {
          name: merchantData.name,
          avatar: merchantData.avatar || '🏪',
          rating: merchantData.rating || '4.8/5.0',
          address: merchantData.address,
          phone: merchantData.phone,
          email: merchantData.email,
          businessHours: merchantData.businessHours,
          status: merchantData.status ? 'open' : 'closed'
        };

        // 保存商家ID到shopInfo中，方便后续使用
        shopInfo.value.id = merchantData.id;
      }
    })
    .catch(error => {
      console.error('加载店铺信息失败:', error);
      ElMessage.error('加载店铺信息失败');
    });

  // 从API获取店铺相册
  api.get(`${API_CONFIG.merchant.album.replace('{merchantId}', merchantId)}`)
    .then(response => {
      if (response.data && response.data.success) {
        const albumData = response.data.data;
        // 将后端返回的数据映射到店铺相册
        shopAlbum.value = {
          environment: albumData.environment || [],
          dishes: albumData.dishes || []
        };
      }
    })
    .catch(error => {
      console.error('加载店铺相册失败:', error);
      // 如果获取失败，保留模拟数据
    });

  // 从API获取优惠活动
  api.get(`${API_CONFIG.merchant.discounts.replace('{merchantId}', merchantId)}`)
    .then(response => {
      if (response.data && response.data.success) {
        const discountsData = response.data.data;
        // 将后端返回的数据映射到优惠活动
        discounts.value = discountsData || [];
      }
    })
    .catch(error => {
      console.error('加载优惠活动失败:', error);
      // 如果获取失败，保留模拟数据
      const mockDiscounts = [
        { id: 1, name: '满30减5', type: '满减', description: '消费满30元减5元', status: 'active' },
        { id: 2, name: '满50减10', type: '满减', description: '消费满50元减10元', status: 'active' },
        { id: 3, name: '新用户立减2元', type: '新人优惠', description: '新用户首单立减2元', status: 'inactive' }
      ];
      discounts.value = mockDiscounts;
    });
});

// 切换营业状态
const toggleBusinessStatus = () => {
  // 调用后端API切换状态
  const newStatus = shopInfo.value.status === 'open' ? 'closed' : 'open';

  api.put(`${API_CONFIG.merchant.detail}${shopInfo.value.id}/status`, null, {
    params: { status: newStatus }
  })
  .then(response => {
    if (response.data && response.data.success) {
      shopInfo.value.status = newStatus;
      ElMessage.success(`店铺已${newStatus === 'open' ? '切换为营业中' : '切换为休息中'}`);
    } else {
      ElMessage.error('切换店铺状态失败');
    }
  })
  .catch(error => {
    console.error('切换店铺状态失败:', error);
    ElMessage.error('切换店铺状态失败');
  });
};

// 打开编辑对话框
const openEditDialog = () => {
  // 将当前店铺信息复制到编辑用的临时对象
  editShopInfo.value = { ...shopInfo.value };
  editDialogVisible.value = true;
};

// 快捷设置函数 - 调整营业时间
const adjustBusinessHours = () => {
  ElMessage.info('调整营业时间功能已触发');
  // 可以在此处添加具体的实现逻辑
};

// 快捷设置函数 - 设置公告
const setAnnouncement = () => {
  ElMessage.info('设置公告功能已触发');
  // 可以在此处添加具体的实现逻辑
};

// 快捷设置函数 - 联系平台
const contactPlatform = () => {
  ElMessage.info('联系平台功能已触发');
  // 可以在此处添加具体的实现逻辑
};

// 保存编辑
const saveShopInfo = () => {
  // 数据合法性验证
  let isValid = true;
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
      // 用户点击确定，调用API更新店铺信息
      api.put(`${API_CONFIG.merchant.detail}${shopInfo.value.id}`, editShopInfo.value)
        .then(response => {
          if (response.data && response.data.success) {
            // 更新本地店铺信息
            shopInfo.value.name = editShopInfo.value.name;
            shopInfo.value.avatar = editShopInfo.value.avatar; // Update avatar
            shopInfo.value.businessHours = editShopInfo.value.businessHours;
            shopInfo.value.address = editShopInfo.value.address;
            shopInfo.value.phone = editShopInfo.value.phone;
            shopInfo.value.email = editShopInfo.value.email;

            // 关闭对话框并提示成功
            editDialogVisible.value = false;
            ElMessage.success('店铺信息已更新');
          } else {
            ElMessage.error('更新店铺信息失败');
          }
        })
        .catch(error => {
          console.error('更新店铺信息失败:', error);
          ElMessage.error('更新店铺信息失败');
        });
    })
    .catch(() => {
      // 用户点击取消，不保存
      ElMessage.info('已取消保存店铺信息');
    });
  }
};

// 上传头像
const handleAvatarUpload = (file) => {
  console.log('上传头像:', file);
  // 模拟上传成功，将头像URL保存到临时编辑信息
  editShopInfo.value.avatar = 'https://picsum.photos/id/200/200/200'; // 替换为实际上传后的URL
  ElMessage.success('头像上传成功');
  return false; // 阻止自动上传
};

// 相册功能相关
const uploadAlbumType = ref('environment'); // 默认为店铺环境相册
const fullAlbumPreviewVisible = ref(false);
const imageUploadList = ref([]); // 保存上传的图片列表

// 获取所有图片用于全屏预览
const getAllImages = computed(() => {
  return [...shopAlbum.value.environment, ...shopAlbum.value.dishes];
});

// 上传照片变更处理
const handleUpload = (file, fileList) => {
  console.log('上传照片变更:', file, fileList);

  // 更新上传列表 - Element Plus不会自动处理受控组件的fileList更新
  imageUploadList.value = fileList;
};

// 移除上传的照片
const handleUploadRemove = (removedFile, fileList) => {
  console.log('移除上传的照片:', removedFile);

  // 更新上传列表 - Element Plus不会自动处理受控组件的fileList更新
  imageUploadList.value = fileList;
};

// 确认上传照片
const confirmUpload = () => {
  if (imageUploadList.value.length === 0) {
    return;
  }

  // 获取上传数量
  const uploadCount = imageUploadList.value.length;
  const albumTypeText = uploadAlbumType.value === 'environment' ? '店铺环境' : '菜品展示';

  // 创建FormData对象
  const formData = new FormData();

  // 添加图片文件
  imageUploadList.value.forEach(file => {
    formData.append('images', file.raw);
  });

  // 添加相册类型
  formData.append('albumType', uploadAlbumType.value);

  // 调用后端API上传照片
  api.post(API_CONFIG.merchant.album.replace('{merchantId}', shopInfo.value.id), formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
  .then(response => {
    if (response.data && response.data.success) {
      // 将后端返回的图片URL保存到对应相册
      const uploadedImages = response.data.data || [];
      shopAlbum.value[uploadAlbumType.value].push(...uploadedImages);

      // 上传完成后清空上传列表
      imageUploadList.value = [];

      // 显示上传成功提示
      ElMessage.success(`已成功上传${uploadCount}张照片到${albumTypeText}相册`);
    }
  })
  .catch(error => {
    console.error('上传照片失败:', error);
    ElMessage.error('上传照片失败');
  });
};

// 删除相册图片
const deleteAlbumImage = (type, index) => {
  ElMessageBox.confirm('确定要删除这张照片吗？', '删除照片', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  .then(() => {
    const imageUrl = shopAlbum.value[type][index];

    // 调用后端API删除照片
    api.delete(API_CONFIG.merchant.album.replace('{merchantId}', shopInfo.value.id), {
      params: {
        imageUrl,
        albumType: type
      }
    })
    .then(response => {
      if (response.data && response.data.success) {
        // 从本地相册中删除图片
        shopAlbum.value[type].splice(index, 1);
        ElMessage.success('照片已删除');
      }
    })
    .catch(error => {
      console.error('删除照片失败:', error);
      ElMessage.error('删除照片失败');
    });
  })
  .catch(() => {
    ElMessage.info('已取消删除');
  });
};

// 打开全屏相册预览
const openFullAlbumPreview = () => {
  fullAlbumPreviewVisible.value = true;
};
</script>

<template>
  <div class="merchant-shop-container">
    <div class="shop-header">
      <div class="header-left">
        <h3 class="page-title">【我的店铺】</h3>
      </div>
    </div>

    <div class="shop-content">
      <!-- 店铺基本信息 -->
      <div class="shop-info-card">
        <h4 class="card-title">🏪 店铺基本信息</h4>
        <div class="shop-avatar-section">
          <el-avatar :size="100" :src="shopInfo.avatar" class="shop-avatar" @click="showLargeAvatar = true"></el-avatar>
        </div>
        <div class="info-grid">
          <div class="info-item">
            <span class="info-label">店铺名称：</span>
            <span class="info-value">{{ shopInfo.name }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">评分：</span>
            <span class="info-value">{{ shopInfo.rating }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">营业时间：</span>
            <span class="info-value">{{ shopInfo.businessHours }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">地址：</span>
            <span class="info-value">{{ shopInfo.address }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">联系方式：</span>
            <span class="info-value">{{ shopInfo.phone }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">邮箱：</span>
            <span class="info-value">{{ shopInfo.email }}</span>
          </div>
        </div>
        <el-button type="primary" size="small" class="edit-btn" @click="openEditDialog" style="margin-left : 17px;">🔧 编辑基本信息</el-button>
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
        <!-- 优惠活动展示 -->
        <div class="discounts-section">
          <div class="discounts-header">
            <div class="discount-title">
              <span class="info-label">当前优惠：</span>
              <div class="active-discounts">
                <el-tag
                  v-for="discount in discounts"
                  :key="discount.id"
                  :type="discount.status === 'active' ? 'success' : 'warning'"
                  size="small"
                  class="discount-tag"
                >
                  {{ discount.name }}
                </el-tag>
              </div>
            </div>
            <div class="discount-actions">
              <el-button type="primary" size="small" class="manage-discount-btn" @click="openDiscountDialog()">
                <el-icon class="el-icon-plus"></el-icon> 添加优惠
              </el-button>
              <el-button type="danger" size="small" @click="batchDeleteDiscounts()" :disabled="selectedDiscounts.length === 0">
                <el-icon class="el-icon-delete"></el-icon> 批量删除
              </el-button>
            </div>
          </div>

          <!-- 优惠活动管理表格 -->
          <div class="discounts-table-container">
            <el-table
              :data="discounts"
              stripe
              border
              style="width: 100%; margin-top: 12px;"
              @selection-change="val => selectedDiscounts = val"
            >
              <el-table-column type="selection" width="55"></el-table-column>
              <el-table-column prop="name" label="优惠名称" min-width="120" show-overflow-tooltip>
                <template #default="scope">
                  {{ scope.row.name }}
                </template>
              </el-table-column>
              <el-table-column prop="type" label="优惠类型" min-width="100" show-overflow-tooltip>
                <template #default="scope">
                  {{ scope.row.type }}
                </template>
              </el-table-column>
              <el-table-column prop="description" label="优惠描述" min-width="180" show-overflow-tooltip>
                <template #default="scope">
                  {{ scope.row.description }}
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" min-width="100" show-overflow-tooltip>
                <template #default="scope">
                  <el-tag :type="scope.row.status === 'active' ? 'success' : 'warning'">
                    {{ scope.row.status === 'active' ? '已启用' : '已禁用' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="180" fixed="right">
                <template #default="scope">
                  <el-button
                    type="primary"
                    size="small"
                    @click="openDiscountDialog(scope.row)"
                    style="margin-right: 8px;"
                  >
                    <el-icon class="el-icon-edit"></el-icon> 编辑
                  </el-button>
                  <el-button
                    type="danger"
                    size="small"
                    @click="deleteDiscount(scope.row)"
                  >
                    <el-icon class="el-icon-delete"></el-icon> 删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
        <!-- <div class="notification-row">
          <span class="info-label">通知设置：</span>
          <span class="info-value">新订单通知{{ notificationSettings.newOrder ? '已开启' : '已关闭' }}</span>
        </div> -->
      </div>

      <!-- 店铺相册 -->
      <div class="shop-album-card">
        <div class="album-header">
          <h4 class="card-title">📸 店铺相册</h4>
          <el-button type="primary" size="small" @click="openFullAlbumPreview">
            <el-icon class="el-icon-full-screen"></el-icon> 放大预览
          </el-button>
        </div>
        <div class="album-stats">
          <span class="stat-item">🔍 店铺环境 ({{ shopAlbum.environment.length }}张)</span>
          <span class="stat-item">🍽️ 菜品展示 ({{ shopAlbum.dishes.length }}张)</span>
        </div>

        <!-- 店铺环境图片 -->
        <div class="album-section">
          <h5 class="section-title">店铺环境</h5>
          <div class="album-grid">
            <div v-for="(image, index) in shopAlbum.environment" :key="`env-${index}`" class="album-item">
              <div class="album-item-overlay">
                <el-button
                  type="danger"
                  size="small"
                  class="delete-img-btn"
                  @click.stop="deleteAlbumImage('environment', index)"
                >
                  <el-icon class="el-icon-delete"></el-icon>
                </el-button>
              </div>
              <el-image
                :src="image"
                :preview-src-list="shopAlbum.environment"
                fit="cover"
              />
            </div>
          </div>
          <!-- 空状态提示 -->
          <div v-if="shopAlbum.environment.length === 0" class="album-empty">
            【...图片暂时为空】
          </div>
        </div>

        <!-- 菜品展示图片 -->
        <div class="album-section">
          <h5 class="section-title">菜品展示</h5>
          <div class="album-grid">
            <div v-for="(image, index) in shopAlbum.dishes" :key="`dish-${index}`" class="album-item">
              <div class="album-item-overlay">
                <el-button
                  type="danger"
                  size="small"
                  class="delete-img-btn"
                  @click.stop="deleteAlbumImage('dishes', index)"
                >
                  <el-icon class="el-icon-delete"></el-icon>
                </el-button>
              </div>
              <el-image
                :src="image"
                :preview-src-list="shopAlbum.dishes"
                fit="cover"
              />
            </div>
          </div>
          <!-- 空状态提示 -->
          <div v-if="shopAlbum.dishes.length === 0" class="album-empty">
            【...图片暂时为空】
          </div>
        </div>

        <!-- 上传按钮及相册选择 -->
        <div class="upload-section">
          <div class="upload-select">
            <label class="upload-label">选择相册：</label>
            <el-select v-model="uploadAlbumType" placeholder="请选择相册类型" size="small" style="width: 180px;">
              <el-option label="店铺环境" value="environment" />
              <el-option label="菜品展示" value="dishes" />
            </el-select>
          </div>

          <el-upload
            action="#"
            list-type="picture-card"
            :file-list="imageUploadList"
            :auto-upload="false"
            @change="handleUpload"
            @remove="handleUploadRemove"
            class="upload-btn"
          >
            <el-icon class="avatar-uploader-icon">
              <Plus />
            </el-icon>
            <template #tip>
              <div class="el-upload__tip">仅支持 JPG/PNG 格式，且不超过 5MB</div>
            </template>
          </el-upload>

          <!-- 上传确认按钮 -->
          <el-button
            type="success"
            size="small"
            class="upload-confirm-btn"
            @click="confirmUpload"
            :disabled="imageUploadList.length === 0"
          >
            <el-icon class="el-icon-check"></el-icon> 确认上传
          </el-button>
        </div>
      </div>

      <!-- 快捷设置 -->
      <div class="quick-settings-card">
        <h4 class="card-title">🎯 快捷设置</h4>
        <div class="settings-grid">
          <el-button type="primary" size="small" class="quick-btn" @click="adjustBusinessHours">
            <Clock style="margin-right: 5px;" /> 调整营业时间
          </el-button>
          <el-button type="primary" size="small" class="quick-btn" @click="setAnnouncement">
            <Document style="margin-right: 5px;" /> 设置公告
          </el-button>
          <el-button type="primary" size="small" class="quick-btn" @click="contactPlatform">
            <Phone style="margin-right: 5px;" /> 联系平台
          </el-button>
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
        <el-form-item label="店铺头像" prop="avatar">
          <div class="avatar-uploader">
            <el-upload
              action="#"
              list-type="picture-card"
              :show-file-list="false"
              :auto-upload="false"
              @change="handleAvatarUpload"
            >
              <el-avatar v-if="editShopInfo.avatar" :size="80" :src="editShopInfo.avatar">
                {{ editShopInfo.name.substring(0, 1) }}
              </el-avatar>
              <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
            </el-upload>
            <div class="el-upload__tip">仅支持 JPG/PNG 格式，且不超过 5MB</div>
          </div>
        </el-form-item>
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

    <!-- 优惠活动管理对话框 -->
    <el-dialog
      v-model="discountDialogVisible"
      :title="isEditingDiscount ? '编辑优惠活动' : '添加优惠活动'"
      width="600px"
      top="10%"
    >
      <el-form :model="currentDiscountForm" label-width="100px" status-icon>
        <el-form-item label="优惠名称" prop="name" required>
          <el-input v-model="currentDiscountForm.name" placeholder="请输入优惠名称" />
        </el-form-item>
        <el-form-item label="优惠类型" prop="type" required>
          <el-select v-model="currentDiscountForm.type" placeholder="请选择优惠类型">
            <el-option label="满减" value="满减" />
            <el-option label="折扣" value="折扣" />
            <el-option label="新人优惠" value="新人优惠" />
            <el-option label="限时特惠" value="限时特惠" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="优惠描述" prop="description" required>
          <el-input
            v-model="currentDiscountForm.description"
            placeholder="请输入优惠描述"
            type="textarea"
            :rows="3"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status" required>
          <el-select v-model="currentDiscountForm.status" placeholder="请选择优惠状态">
            <el-option label="已启用" value="active" />
            <el-option label="已禁用" value="inactive" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="discountDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveDiscount">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 全屏相册预览对话框 -->
    <el-dialog v-model="fullAlbumPreviewVisible" title="店铺相册全屏预览" width="90%" top="5%">
      <div class="full-album-preview">
        <el-image-viewer
          v-if="fullAlbumPreviewVisible"
          :url-list="getAllImages"
          @close="fullAlbumPreviewVisible = false"
        />
      </div>
    </el-dialog>
  </div>
</template>

<style scoped lang="less">
@import "./MyShop.less";
</style>
