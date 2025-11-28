<template>
  <div class="login-container">
    <div class="login-card">
      <h2 class="login-title">用户登录</h2>

      <el-form :model="loginForm" :rules="loginRules" ref="loginFormRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入用户名" autocomplete="off" />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submitForm" block>登录</el-button>
          <el-button @click="resetForm" block>重置</el-button>
        </el-form-item>

        <div class="register-link">
          <span>没有账号？</span>
          <el-button type="text" @click="toRegister">立即注册</el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { ElMessage, ElForm, ElFormItem, ElInput, ElButton } from 'element-plus';
import { useRouter } from 'vue-router';

const router = useRouter();

// 登录表单数据
const loginForm = reactive({
  username: '',
  password: ''
});

// 表单验证规则
const loginRules = reactive({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
});

// 表单引用
const loginFormRef = ref(null);

// 提交表单
const submitForm = () => {
  if (loginFormRef.value) {
    loginFormRef.value.validate((valid) => {
      if (valid) {
        // 这里可以添加实际的登录逻辑
        console.log('登录成功:', loginForm);
        ElMessage.success('登录成功！');
        // 登录成功后跳转到用户首页
        setTimeout(() => {
          router.push('/user/home');
        }, 1500);
      } else {
        ElMessage.error('表单验证失败，请检查输入');
      }
    });
  }
};

// 重置表单
const resetForm = () => {
  if (loginFormRef.value) {
    loginFormRef.value.resetFields();
  }
};

// 跳转到注册页面
const toRegister = () => {
  router.push('/user/register');
};
</script>

<style scoped lang="less">
.login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
}

.login-card {
  width: 100%;
  max-width: 400px;
  background-color: #fff;
  border-radius: 10px;
  padding: 30px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
}

.login-title {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
  font-size: 24px;
}

.register-link {
  text-align: center;
  margin-top: 15px;
  font-size: 14px;
  color: #666;

  span {
    margin-right: 8px;
  }
}
</style>