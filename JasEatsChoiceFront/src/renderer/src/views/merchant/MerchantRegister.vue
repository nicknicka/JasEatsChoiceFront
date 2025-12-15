<template>
  <div class="register-container">
    <div class="register-card">
      <h2 class="register-title">商户注册</h2>

      <el-form :model="registerForm" :rules="registerRules" ref="registerFormRef" label-width="120px">
        <el-form-item label="商户名称" prop="merchantName">
          <el-input v-model="registerForm.merchantName" placeholder="请输入商户名称" autocomplete="off" />
        </el-form-item>

        <el-form-item label="营业执照号" prop="businessLicense">
          <el-input v-model="registerForm.businessLicense" placeholder="请输入营业执照号" />
        </el-form-item>

        <el-form-item label="经营范围" prop="businessScope">
          <el-select v-model="registerForm.businessScope" placeholder="请选择经营范围" multiple style="width: 100%;" @change="handleBusinessScopeChange">
            <el-option label="中餐" value="中餐" />
            <el-option label="西餐" value="西餐" />
            <el-option label="快餐" value="快餐" />
            <el-option label="甜点" value="甜点" />
            <el-option label="饮品" value="饮品" />
            <el-option label="其他" value="其他" />
            <el-option label="自定义" value="自定义" />
          </el-select>

          <!-- 自定义经营范围输入框 -->
          <div v-if="showCustomBusinessScope" style="display: flex; gap: 10px; margin-top: 10px;">
            <el-input v-model="customBusinessScope" placeholder="请输入自定义经营范围" style="flex: 1;" />
            <el-button type="primary" @click="addCustomBusinessScope">确定</el-button>
          </div>
        </el-form-item>

        <el-form-item label="联系人姓名" prop="contactName">
          <el-input v-model="registerForm.contactName" placeholder="请输入联系人姓名" />
        </el-form-item>

        <el-form-item label="联系人电话" prop="contactPhone">
          <el-input v-model="registerForm.contactPhone" placeholder="请输入联系人电话" />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input v-model="registerForm.email" placeholder="请输入邮箱" type="email" />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input v-model="registerForm.password" type="password" placeholder="请输入密码" />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="registerForm.confirmPassword" type="password" placeholder="请确认密码" />
        </el-form-item>

        <el-form-item label="验证码" prop="captcha">
          <div style="display: flex; align-items: center;">
            <el-input v-model="registerForm.captcha" placeholder="请输入验证码" style="width: 40%; margin-right: 10px;" />
            <img
              v-if="captchaImage"
              :src="captchaImage"
              style="width: 120px; height: 36px; cursor: pointer; margin-right: 10px;"
              @click="getCaptcha"
              alt="验证码"
            />
            <el-button type="text" size="small" @click="getCaptcha" style="margin-left: 0;">刷新</el-button>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submitForm" block>注册</el-button>
        </el-form-item>

        <div class="login-link">
          <span>已有账号？</span>
          <el-button type="text" @click="toLogin">立即登录</el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElForm, ElFormItem, ElInput, ElButton, ElSelect, ElOption } from 'element-plus';
import { useRouter } from 'vue-router';
import api from '../../utils/api';
import { API_CONFIG } from '../../config/index.js';
import { useAuthStore } from '../../store/authStore';
import { useUserStore } from '../../store/userStore';

const router = useRouter();

// 注册表单数据
const registerForm = reactive({
  merchantName: '',
  businessLicense: '',
  businessScope: [],
  contactName: '',
  contactPhone: '',
  email: '',
  password: '',
  confirmPassword: '',
  captcha: ''
});

// 自定义经营范围相关
const showCustomBusinessScope = ref(false);
const customBusinessScope = ref('');

// 自定义经营范围变化处理
const handleBusinessScopeChange = (value) => {
  // 检查是否选择了"自定义"选项
  showCustomBusinessScope.value = value.includes('自定义');

  // 如果取消选择"自定义"，则清空输入框
  if (!showCustomBusinessScope.value) {
    customBusinessScope.value = '';
  }
};

// 添加自定义经营范围
const addCustomBusinessScope = () => {
  if (customBusinessScope.value.trim()) {
    // 如果"自定义"选项还在列表中，先移除它
    const index = registerForm.businessScope.indexOf('自定义');
    if (index !== -1) {
      registerForm.businessScope.splice(index, 1);
    }

    // 添加自定义经营范围
    registerForm.businessScope.push(customBusinessScope.value.trim());

    // 清空输入框并隐藏自定义选项区域
    customBusinessScope.value = '';
    showCustomBusinessScope.value = false;
  }
};

// 表单验证规则
const registerRules = reactive({
  merchantName: [
    { required: true, message: '请输入商户名称', trigger: 'blur' },
    { min: 2, max: 50, message: '商户名称长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  businessLicense: [
    { required: true, message: '请输入营业执照号', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9]+$/, message: '营业执照号只能包含字母和数字', trigger: ['blur', 'change'] }
  ],
  businessScope: [
    { required: true, message: '请选择经营范围', trigger: 'change' }
  ],
  contactName: [
    { required: true, message: '请输入联系人姓名', trigger: 'blur' }
  ],
  contactPhone: [
    { required: true, message: '请输入联系人电话', trigger: ['blur', 'change'] },
    { pattern: /^1[3456789]\d{9}$/, message: '请输入正确的手机号码', trigger: ['blur', 'change'] }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: ['blur', 'change'] },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: ['blur', 'change'] }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度在 6 到 32 个字符', trigger: 'blur' },
    { pattern: /^(?=.*[a-zA-Z])(?=.*\d)/, message: '密码必须包含字母和数字', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: (rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入密码不一致'));
        } else {
          callback();
        }
      }, trigger: 'blur' }
  ],
  captcha: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ]
});

// 验证码相关
const captchaImage = ref(''); // 验证码图片
const captchaKey = ref('');   // 验证码Key（用于后端验证）

// 从后端获取验证码
const getCaptcha = async () => {
  try {
    const response = await api.get('/v1/captcha/checkCode'); // 调用后端验证码接口
    // 添加调试信息，查看响应结构
    // console.log('验证码接口完整响应:', response);
    // console.log('验证码接口响应data:', response.data);

    // 智能判断响应结构
    const captchaData = response.data;
    if (captchaData) {
      // Base64图片需要添加前缀才能正确显示
      captchaImage.value = 'data:image/png;base64,' + captchaData.checkCode;
      captchaKey.value = captchaData.checkCodeKey;     // 验证码唯一标识
    }
  } catch (error) {
    console.error('获取验证码失败:', error);
    ElMessage.error('获取验证码失败，请稍后重试');
  }
};

// 表单引用
const registerFormRef = ref(null);

// 页面加载时获取验证码
onMounted(() => {
  getCaptcha();
});

// 提交表单
const submitForm = () => {
  if (registerFormRef.value) {
    registerFormRef.value.validate((valid) => {
      if (valid) {
        // 准备后端需要的数据结构
        const merchantData = {
          // 注意：前端字段与后端字段映射
          name: registerForm.merchantName, // 后端是name，前端是merchantName
          phone: registerForm.contactPhone, // 后端是phone，前端是contactPhone
          password: registerForm.password,
          // 其他字段暂时保留，等待后端实体更新后启用
          businessLicense: registerForm.businessLicense,
          businessScope: registerForm.businessScope, // 直接发送数组
          contactName: registerForm.contactName,
          email: registerForm.email,
          captcha: registerForm.captcha,
          captchaKey: captchaKey.value
        };

        // 调用商家注册API
        api.post(API_CONFIG.merchant.register, merchantData)
          .then(apiResponse => {
            // 严格检查响应是否有效（注意：响应拦截器已将 response.data 直接返回）
            if (!apiResponse || !apiResponse.id) { // 检查商家ID是否存在
              console.error('注册失败: 无效的API响应', apiResponse);
              ElMessage.error('注册失败: 服务器返回无效响应');
              return;
            }

            ElMessage.success('注册成功！');

            // 获取 Pinia 存储实例
            const authStore = useAuthStore();
            const userStore = useUserStore();

            console.log('商家注册响应:', apiResponse);
            // 保存用户信息和角色
            const merchantInfo = {
              merchantId: apiResponse.id, // 商家ID
              name: apiResponse.name,
              phone: apiResponse.phone,
              email: apiResponse.email,
              businessLicense: apiResponse.businessLicense,
              businessScope: apiResponse.businessScope,
              contactName: apiResponse.contactName,
              avatar: apiResponse.avatar,
              rating: apiResponse.rating,
              status: apiResponse.status,
              businessHours: apiResponse.businessHours
            };

            // 使用 Pinia 存储商家信息
            userStore.setMerchantInfo(merchantInfo);

            // 使用 Pinia 存储商家ID到认证信息
            authStore.setMerchantId(merchantInfo.merchantId);

            // 假设商家注册成功后，后端会返回一个真实的 token
            // const token = response.data.data;
            // authStore.setToken(token);

            // 注册成功后跳转到商家首页
            setTimeout(() => {
              router.push('/merchant/home');
            }, 1500);
          })
          .catch(error => {
            console.error('注册失败:', error);
            ElMessage.error('注册失败，请稍后重试');
          });
      } else {
        ElMessage.error('表单验证失败，请检查输入');
      }
    });
  }
};

// 跳转到登录页面
const toLogin = () => {
  router.push('/login');
};
</script>

<style scoped lang="less">
.register-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
}

.register-card {
  width: 100%;
  max-width: 500px;
  background-color: #fff;
  border-radius: 10px;
  padding: 30px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
}

.register-title {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
  font-size: 24px;
}

.login-link {
  text-align: center;
  margin-top: 15px;
  font-size: 14px;
  color: #666;

  span {
    margin-right: 8px;
  }
}
</style>
