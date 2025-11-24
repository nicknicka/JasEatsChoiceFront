<template>
  <div class="contact-container">
    <h2>联系客服</h2>

    <el-card class="contact-card">
      <div class="contact-info">
        <div class="contact-item">
          <div class="contact-label">客服电话</div>
          <div class="contact-value">400-888-8888</div>
        </div>

        <div class="contact-item">
          <div class="contact-label">工作时间</div>
          <div class="contact-value">9:00-21:00（周一至周日）</div>
        </div>

        <div class="contact-item">
          <div class="contact-label">客服邮箱</div>
          <div class="contact-value">support@jaseats.com</div>
        </div>

        <div class="contact-item">
          <div class="contact-label">在线客服</div>
          <div class="contact-value">
            <el-button type="primary" size="small">
              <el-icon><ChatDotRound /></el-icon>
              开始在线咨询
            </el-button>
          </div>
        </div>
      </div>

      <el-divider />

      <div class="feedback-section">
        <h3>反馈建议</h3>
        <el-form ref="feedbackFormRef" :model="feedback" label-width="80px">
          <el-form-item label="标题" prop="title">
            <el-input v-model="feedback.title" placeholder="请输入反馈标题" />
          </el-form-item>
          <el-form-item label="内容" prop="content">
            <el-input
              v-model="feedback.content"
              placeholder="请输入反馈内容"
              type="textarea"
              rows="5"
            />
          </el-form-item>
          <el-form-item label="联系方式" prop="contact">
            <el-input v-model="feedback.contact" placeholder="请留下您的联系方式，以便我们及时回复" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="submitFeedback">提交反馈</el-button>
            <el-button @click="resetForm">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { ChatDotRound } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';

// 反馈表单数据
const feedback = ref({
  title: '',
  content: '',
  contact: ''
});

// 提交反馈
const submitFeedback = () => {
  // 表单验证
  if (!feedback.value.title || !feedback.value.content) {
    ElMessage.warning('请填写反馈标题和内容');
    return;
  }

  // 模拟提交反馈
  console.log('提交的反馈:', feedback.value);

  // 重置表单
  resetForm();

  // 提示成功
  ElMessage.success('反馈已提交，我们会尽快处理');
};

// 重置表单
const resetForm = () => {
  feedback.value = {
    title: '',
    content: '',
    contact: ''
  };
};
</script>

<style scoped lang="less">
.contact-container {
  padding: 0 20px 20px 20px;

  h2 {
    font-size: 24px;
    margin: 0 0 20px 0;
  }

  .contact-card {
    padding: 20px;

    .contact-info {
      margin-bottom: 20px;

      .contact-item {
        margin-bottom: 15px;
        display: flex;
        align-items: center;

        .contact-label {
          width: 120px;
          font-weight: bold;
          margin-right: 20px;
        }

        .contact-value {
          flex: 1;
        }
      }
    }

    .feedback-section {
      h3 {
        font-size: 18px;
        margin: 0 0 15px 0;
        font-weight: bold;
      }
    }
  }
}
</style>
