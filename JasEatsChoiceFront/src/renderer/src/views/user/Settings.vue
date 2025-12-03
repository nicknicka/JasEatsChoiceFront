<template>
  <div class="settings-container">
    <h2>设置</h2>

    <el-card class="settings-card">
      <div class="settings-section">
        <h3>🧑 用户设置</h3>
        <div class="form-row">
          <div class="form-label">头像</div>
          <div class="form-content">
            <el-avatar :size="60" class="user-avatar" :src="userInfo.avatarUrl">👤</el-avatar>
            <input
              type="file"
              accept="image/*"
              style="display: none;"
              id="avatar-upload"
              @change="handleAvatarUpload"
            />
            <el-button type="primary" size="small" style="margin-left: 10px;" @click="handleAvatarClick">更换头像</el-button>
          </div>
        </div>

        <div class="form-row">
          <div class="form-label">手机号</div>
          <div class="form-content">
            <el-input v-model="userInfo.phone" readonly style="width: 200px;" />
            <el-button type="text" size="small" style="margin-left: 10px;" @click="handleEditPhone">修改</el-button>
          </div>
        </div>

        <div class="form-row">
          <div class="form-label">邮箱</div>
          <div class="form-content">
            <el-input v-model="userInfo.email" readonly style="width: 200px;" />
            <el-button type="text" size="small" style="margin-left: 10px;" @click="handleEditEmail">修改</el-button>
          </div>
        </div>

        <div class="form-row">
          <div class="form-label">密码</div>
          <div class="form-content">
            <el-input type="password" placeholder="********" readonly style="width: 200px;" />
            <el-button type="text" size="small" style="margin-left: 10px;" @click="handleEditPassword">修改</el-button>
          </div>
        </div>
      </div>

      <el-divider />

      <div class="settings-section">
        <h3>🔍 显示设置</h3>

        <div class="form-row">
          <div class="form-label">字体大小</div>
          <div class="form-content">
            <el-radio-group v-model="fontSize" style="margin-right: 20px;">
              <el-radio label="small">小</el-radio>
              <el-radio label="medium">中</el-radio>
              <el-radio label="large">大</el-radio>
              <el-radio label="extra-large">超大</el-radio>
            </el-radio-group>
          </div>
        </div>

        <div class="form-row">
          <div class="form-label">主题切换</div>
          <div class="form-content">
            <el-switch v-model="theme" active-text="深色" inactive-text="浅色" />
          </div>
        </div>
      </div>

      <el-divider />

      <div class="settings-section">
        <h3>🔔 通知设置</h3>

        <div class="form-row">
          <div class="form-label">订单通知</div>
          <div class="form-content">
            <el-switch v-model="notifications.order" />
          </div>
        </div>

        <div class="form-row">
          <div class="form-label">活动通知</div>
          <div class="form-content">
            <el-switch v-model="notifications.activity" />
          </div>
        </div>

        <div class="form-row">
          <div class="form-label">商家回复</div>
          <div class="form-content">
            <el-switch v-model="notifications.merchantReply" />
          </div>
        </div>

        <div class="form-row">
          <div class="form-label">群聊消息</div>
          <div class="form-content">
            <el-switch v-model="notifications.groupChat" />
          </div>
        </div>
      </div>

      <el-divider />

      <div class="settings-section">
        <h3>🕶️ 隐私设置</h3>

        <div class="form-row">
          <div class="form-label">定位权限</div>
          <div class="form-content">
            <el-switch v-model="privacy.location" />
          </div>
        </div>

        <div class="form-row">
          <div class="form-label">推荐权限</div>
          <div class="form-content">
            <el-switch v-model="privacy.recommendation" />
          </div>
        </div>

        <div class="form-row">
          <div class="form-label">天气推荐</div>
          <div class="form-content">
            <el-switch v-model="privacy.weatherRecommendation" />
          </div>
        </div>

        <div class="form-row">
          <div class="form-content">
            <el-button type="warning" size="small" style="margin-right: 10px;" @click="clearCache">清除缓存</el-button>
            <el-button type="primary" size="small" @click="exportData">数据导出</el-button>
          </div>
        </div>
      </div>

      <el-divider />

      <div class="settings-section">
        <h3>📱 版本信息</h3>

        <div class="form-row">
          <div class="form-label">当前版本</div>
          <div class="form-content">1.0.0</div>
        </div>

        <div class="form-row">
          <div class="form-content">
            <el-button type="text" size="small" style="margin-right: 10px;" @click="checkUpdate">检查更新</el-button>
            <el-button type="text" size="small" @click="submitFeedback">反馈建议</el-button>
          </div>
        </div>
      </div>

      <div class="settings-actions">
        <el-button type="primary" @click="saveSettings">保存设置</el-button>
        <el-button type="warning" style="margin-left: 10px;" @click="resetSettings">重置默认</el-button>
      </div>
    </el-card>

    <!-- Edit Phone Dialog -->
    <el-dialog title="修改手机号" v-model="editPhoneDialogVisible" width="30%">
      <el-form ref="phoneFormRef" :model="phoneForm" label-width="80px">
        <el-form-item label="手机号">
          <el-input v-model="phoneForm.phone" placeholder="请输入新手机号" />
        </el-form-item>
        <el-form-item label="验证码">
          <div style="display: flex;">
            <el-input v-model="phoneForm.verificationCode" placeholder="请输入验证码" style="margin-right: 10px;" />
            <el-button type="primary" @click="sendSmsCode" :disabled="smsCodeCountdown > 0">
              {{ smsCodeCountdown > 0 ? `${smsCodeCountdown}秒后重新发送` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="editPhoneDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitPhoneEdit">确认</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- Edit Email Dialog -->
    <el-dialog title="修改邮箱" v-model="editEmailDialogVisible" width="30%">
      <el-form ref="emailFormRef" :model="emailForm" label-width="80px">
        <el-form-item label="邮箱">
          <el-input v-model="emailForm.email" placeholder="请输入新邮箱" />
        </el-form-item>
        <el-form-item label="验证码">
          <div style="display: flex;">
            <el-input v-model="emailForm.verificationCode" placeholder="请输入验证码" style="margin-right: 10px;" />
            <el-button type="primary" @click="sendEmailCode" :disabled="emailCodeCountdown > 0">
              {{ emailCodeCountdown > 0 ? `${emailCodeCountdown}秒后重新发送` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="editEmailDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitEmailEdit">确认</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- Edit Password Dialog -->
    <el-dialog title="修改密码" v-model="editPasswordDialogVisible" width="30%">
      <el-form ref="passwordFormRef" :model="passwordForm" label-width="80px">
        <el-form-item label="旧密码">
          <el-input type="password" v-model="passwordForm.oldPassword" placeholder="请输入旧密码" />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input type="password" v-model="passwordForm.newPassword" placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input type="password" v-model="passwordForm.confirmPassword" placeholder="请确认新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="editPasswordDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitPasswordEdit">确认</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, inject } from 'vue';
import { ElMessage, ElDialog, ElInput, ElForm, ElFormItem } from 'element-plus';
import api, { decodeJwt } from '../../utils/api.js';
import { API_CONFIG } from '../../config/index.js';

// User information
const userInfo = ref({
  phone: '',
  email: '',
  avatarUrl: ''
});

// 正式设置数据（用于保存到localStorage）
const officialSettings = ref({
  fontSize: 'medium',
  theme: false,
  notifications: {
    order: true,
    activity: true,
    merchantReply: true,
    groupChat: true
  },
  privacy: {
    location: true,
    recommendation: true,
    weatherRecommendation: true // 添加天气推荐设置
  }
});

// 临时设置数据（用于页面实时修改）
const tempSettings = ref({ ...officialSettings.value });

// 辅助变量，方便模板中直接使用
const fontSize = ref(tempSettings.value.fontSize);
const theme = ref(tempSettings.value.theme);
const notifications = ref({ ...tempSettings.value.notifications });
const privacy = ref({ ...tempSettings.value.privacy });

// User info edit dialogs
const editPhoneDialogVisible = ref(false);
const editEmailDialogVisible = ref(false);
const editPasswordDialogVisible = ref(false);

// Form data
const phoneForm = ref({
  phone: '',
  verificationCode: ''
});

const emailForm = ref({
  email: '',
  verificationCode: ''
});

// Verification code countdowns
const smsCodeCountdown = ref(0);
const emailCodeCountdown = ref(0);

const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});

// Avatar upload
const avatarUploadInput = ref(null);

// Load saved settings from localStorage on mount and fetch user info from backend
onMounted(() => {
  // Fetch user information from backend
  const fetchUserInfo = () => {
    const token = localStorage.getItem('token');
    let userId = parseInt(localStorage.getItem('userId') || '1', 10); // 默认值

    if (token) {
      const decodedToken = decodeJwt(token);
      if (decodedToken && decodedToken.userId) {
        userId = decodedToken.userId;
      }
    }

    // 调用后端API获取用户信息
    api.get(API_CONFIG.user.profile.replace('{userId}', userId))
      .then(response => {
        if (response.data && response.data.success) {
          userInfo.value = response.data.data;

          // Load avatar from backend or use default
          if (userInfo.value.avatar) {
            userInfo.value.avatarUrl = userInfo.value.avatar;
          }
        }
      })
      .catch(error => {
        console.error('加载用户信息失败:', error);
      });
  };

  // Load saved settings
  const savedSettings = localStorage.getItem('userSettings');
  if (savedSettings) {
    const parsed = JSON.parse(savedSettings);

    // 更新正式设置数据
    officialSettings.value = {
      fontSize: parsed.fontSize || 'medium',
      theme: parsed.theme || false,
      notifications: parsed.notifications || {
        order: true,
        activity: true,
        merchantReply: true,
        groupChat: true
      },
      privacy: parsed.privacy || {
        location: true,
        recommendation: true,
        weatherRecommendation: true
      }
    };

    // 更新临时设置变量
    fontSize.value = officialSettings.value.fontSize;
    theme.value = officialSettings.value.theme;
    notifications.value = { ...officialSettings.value.notifications };
    privacy.value = { ...officialSettings.value.privacy };

    // Update theme
    updateTheme();
  }

  // Fetch user info from backend
  fetchUserInfo();

  // Load avatar from localStorage as fallback
  const savedAvatar = localStorage.getItem('userAvatar');
  if (savedAvatar) {
    userInfo.value.avatarUrl = savedAvatar;
  }
});

// Handle save settings with localStorage persistence
const saveSettings = () => {
  // 将临时修改的设置同步到正式设置数据中
  const updatedSettings = {
    fontSize: fontSize.value,
    theme: theme.value,
    notifications: { ...notifications.value },
    privacy: { ...privacy.value }
  };

  // 更新正式设置数据
  officialSettings.value = { ...updatedSettings };

  // 保存到localStorage
  localStorage.setItem('userSettings', JSON.stringify(officialSettings.value));

  ElMessage.success('设置已保存');
  console.log('Saved settings:', officialSettings.value);

  // 更新主题
  updateTheme();
};

// Handle reset settings
const resetSettings = () => {
  // 默认设置值
  const defaultSettings = {
    fontSize: 'medium',
    theme: false,
    notifications: {
      order: true,
      activity: true,
      merchantReply: true,
      groupChat: true
    },
    privacy: {
      location: true,
      recommendation: true,
      weatherRecommendation: true
    }
  };

  // 更新临时设置变量
  fontSize.value = defaultSettings.fontSize;
  theme.value = defaultSettings.theme;
  notifications.value = { ...defaultSettings.notifications };
  privacy.value = { ...defaultSettings.privacy };

  // 更新正式设置数据并保存
  saveSettings();

  ElMessage.info('设置已重置为默认值');
};

// Update theme
const updateTheme = () => {
  if (theme.value) {
    document.body.classList.add('dark-theme');
    document.body.classList.remove('light-theme');
  } else {
    document.body.classList.add('light-theme');
    document.body.classList.remove('dark-theme');
  }
};

// Avatar upload functionality
// Get the update function from CommonHome.vue
const updateSidebarAvatar = inject('updateSidebarAvatar');

const handleAvatarClick = () => {
  document.getElementById('avatar-upload').click();
};

const handleAvatarUpload = (event) => {
  const file = event.target.files[0];
  if (file) {
    const reader = new FileReader();
    reader.onload = (e) => {
      const newAvatarUrl = e.target.result;

      // Update local user info
      userInfo.value.avatarUrl = newAvatarUrl;

      // Update sidebar avatar
      if (updateSidebarAvatar) {
        updateSidebarAvatar(newAvatarUrl);
      }

      // Save to localStorage
      localStorage.setItem('userAvatar', newAvatarUrl);

      ElMessage.success('头像已更换');
    };
    reader.readAsDataURL(file);
  }
};

// Handle send SMS verification code
const sendSmsCode = () => {
  const phone = phoneForm.value.phone;
  if (!phone) {
    ElMessage.warning('请输入手机号');
    return;
  }

  // Call backend API to send SMS code
  api.post(`${API_CONFIG.user.sendSmsCode}`, { phone })
    .then(response => {
      if (response.success) {
        ElMessage.success('验证码已发送');
        // Start countdown
        smsCodeCountdown.value = 60;
        const timer = setInterval(() => {
          smsCodeCountdown.value--;
          if (smsCodeCountdown.value <= 0) {
            clearInterval(timer);
          }
        }, 1000);
      } else {
        ElMessage.error(response.message || '发送失败');
      }
    })
    .catch(error => {
      ElMessage.error(error.message || '发送失败');
    });
};

// Handle edit phone
const handleEditPhone = () => {
  editPhoneDialogVisible.value = true;
  // Auto-fill current phone number
  phoneForm.value.phone = userInfo.value.phone;
};

const submitPhoneEdit = () => {
  if (phoneForm.value.phone && phoneForm.value.verificationCode) {
    // Call backend API to update phone number
    api.put(`${API_CONFIG.user.profile.replace('{userId}', userInfo.value.id)}`, {
      phone: phoneForm.value.phone,
      verificationCode: phoneForm.value.verificationCode
    })
    .then(response => {
      if (response.success) {
        ElMessage.success('手机号已修改');
        // Update local user info
        userInfo.value.phone = phoneForm.value.phone;
        editPhoneDialogVisible.value = false;
        phoneForm.value = { phone: '', verificationCode: '' };
      } else {
        ElMessage.error(response.message || '手机号修改失败');
      }
    })
    .catch(error => {
      ElMessage.error(error.message || '手机号修改失败');
    });
  } else {
    ElMessage.warning('请填写完整信息');
  }
};

// Handle send email verification code
const sendEmailCode = () => {
  const email = emailForm.value.email;
  if (!email) {
    ElMessage.warning('请输入邮箱地址');
    return;
  }

  // Call backend API to send email code
  api.post(`${API_CONFIG.user.sendEmailCode}`, { email })
    .then(response => {
      if (response.success) {
        ElMessage.success('验证码已发送');
        // Start countdown
        emailCodeCountdown.value = 60;
        const timer = setInterval(() => {
          emailCodeCountdown.value--;
          if (emailCodeCountdown.value <= 0) {
            clearInterval(timer);
          }
        }, 1000);
      } else {
        ElMessage.error(response.message || '发送失败');
      }
    })
    .catch(error => {
      ElMessage.error(error.message || '发送失败');
    });
};

// Handle edit email
const handleEditEmail = () => {
  editEmailDialogVisible.value = true;
  // Auto-fill current email
  emailForm.value.email = userInfo.value.email;
};

const submitEmailEdit = () => {
  if (emailForm.value.email && emailForm.value.verificationCode) {
    // Call backend API to update email
    api.put(`${API_CONFIG.user.profile.replace('{userId}', userInfo.value.id)}`, {
      email: emailForm.value.email,
      verificationCode: emailForm.value.verificationCode
    })
    .then(response => {
      if (response.success) {
        ElMessage.success('邮箱已修改');
        // Update local user info
        userInfo.value.email = emailForm.value.email;
        editEmailDialogVisible.value = false;
        emailForm.value = { email: '', verificationCode: '' };
      } else {
        ElMessage.error(response.message || '邮箱修改失败');
      }
    })
    .catch(error => {
      ElMessage.error(error.message || '邮箱修改失败');
    });
  } else {
    ElMessage.warning('请填写完整信息');
  }
};

// Handle edit password
const handleEditPassword = () => {
  editPasswordDialogVisible.value = true;
};

const submitPasswordEdit = () => {
  if (passwordForm.value.oldPassword && passwordForm.value.newPassword && passwordForm.value.confirmPassword) {
    if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
      ElMessage.warning('新密码和确认密码不一致');
      return;
    }
    // In real app: call API to update password
    ElMessage.success('密码已修改');
    editPasswordDialogVisible.value = false;
    passwordForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' };
  } else {
    ElMessage.warning('请填写完整信息');
  }
};

// Handle clear cache
const clearCache = () => {
  // Clear localStorage except for user settings and avatar
  const userSettings = localStorage.getItem('userSettings');
  const userAvatar = localStorage.getItem('userAvatar');

  localStorage.clear();

  if (userSettings) localStorage.setItem('userSettings', userSettings);
  if (userAvatar) localStorage.setItem('userAvatar', userAvatar);

  ElMessage.success('缓存已清除');
  console.log('Cache cleared');
};

// Handle data export
const exportData = () => {
  // Create data to export
  const userData = {
    profile: {
      phone: userInfo.value.phone || '未设置',
      email: userInfo.value.email || '未设置',
    },
    settings: JSON.parse(localStorage.getItem('userSettings') || '{}'),
    exportDate: new Date().toISOString()
  };

  // Convert to JSON and download
  const dataStr = JSON.stringify(userData, null, 2);
  const dataBlob = new Blob([dataStr], { type: 'application/json' });
  const dataUrl = URL.createObjectURL(dataBlob);

  const a = document.createElement('a');
  a.href = dataUrl;
  a.download = '用户数据导出.json';
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(dataUrl);

  ElMessage.success('数据导出成功');
  console.log('Data export completed:', userData);
};

// Handle check for updates
const checkUpdate = () => {
  // In real app: call API to check for updates
  ElMessage.info('当前已是最新版本');
  console.log('Checked for updates');
};

// Handle feedback
const submitFeedback = () => {
  // In real app: call API to submit feedback
  ElMessage.success('反馈已提交');
  console.log('Feedback submitted');
};
</script>

<style scoped>
.settings-container {
  padding: 0 20px 20px 20px;
}

.settings-container h2 {
  font-size: 24px;
  margin: 0 0 20px 0;
}

.settings-container .settings-card {
  padding: 20px;
}

.settings-container .settings-section {
  margin-bottom: 20px;
}

.settings-container .settings-section h3 {
  font-size: 18px;
  margin: 0 0 20px 0;
  font-weight: bold;
}

.settings-container .form-row {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.settings-container .form-row .form-label {
  width: 150px;
  font-weight: bold;
}

.settings-container .form-row .form-content {
  flex: 1;
}

.settings-container .settings-actions {
  margin-top: 30px;
  text-align: center;
}

.settings-container .user-avatar {
  background-color: transparent; /* 移除额外的背景颜色 */
}
</style>
