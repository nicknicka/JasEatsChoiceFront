<script setup>
import { ref, onMounted } from 'vue';
import { VideoCamera, Document, ArrowLeft } from '@element-plus/icons-vue';
import { useRouter } from 'vue-router';
import api from '../../utils/api.js';
import { API_CONFIG } from '../../config/index.js';

const router = useRouter();

// 返回用户首页
const goBackToHome = () => {
  router.push('/user/home');
};

// 教程数据 - 从后端获取
const tutorials = ref([]);

// 从后端获取所有教程数据
const fetchTutorials = () => {
  api.get(API_CONFIG.tutorial.list)
    .then(response => {
      if (response.data) {
        tutorials.value = response.data;
      }
    })
    .catch(error => {
      console.error('加载教程列表失败:', error);
      // 失败时使用模拟数据作为备份
      tutorials.value = [
        { id: 1, title: '青木瓜沙拉制作教程', type: 'video', duration: '5:30', views: '12.5k' },
        { id: 2, title: '夏日低卡饮食指南', type: 'article', duration: '8分钟', views: '8.2k' },
        { id: 3, title: '健康早餐搭配技巧', type: 'video', duration: '3:45', views: '9.7k' },
        { id: 4, title: '减脂餐制作基础', type: 'article', duration: '12分钟', views: '15.8k' },
        { id: 5, title: '果汁制作小技巧', type: 'video', duration: '4:15', views: '7.3k' },
        { id: 6, title: '均衡饮食营养知识', type: 'article', duration: '15分钟', views: '21.2k' }
      ];
    });
};

// 页面加载时获取教程数据
onMounted(() => {
  fetchTutorials();
});
</script>

<template>
  <div class="tutorials-container">
    <div class="page-header">
      <el-button type="primary" :icon="ArrowLeft" size="small" @click="goBackToHome">
        返回首页
      </el-button>
      <h2>制作教程与指南</h2>
    </div>

    <div class="tutorial-grid">
      <el-card
        v-for="tutorial in tutorials"
        :key="tutorial.id"
        class="tutorial-card"
        shadow="hover"
        @click="router.push(`/user/home/tutorials/${tutorial.id}`)"
      >
        <div class="tutorial-header">
          <el-icon :class="tutorial.type === 'video' ? 'video-icon' : 'article-icon'">
            <component :is="tutorial.type === 'video' ? VideoCamera : Document" />
          </el-icon>
          <el-tag :type="tutorial.type === 'video' ? 'danger' : 'warning'">
            {{ tutorial.type === 'video' ? '视频教程' : '图文指南' }}
          </el-tag>
        </div>
        <div class="tutorial-title">
          {{ tutorial.title }}
        </div>
        <div class="tutorial-meta">
          <span class="duration">{{ tutorial.duration }}</span>
          <span class="views">{{ tutorial.views }} 阅读/观看</span>
        </div>
      </el-card>
    </div>
  </div>
</template>

<style scoped lang="less">
.tutorials-container {
  padding: 20px 0;

  .page-header {
    display: flex;
    align-items: center;
    gap: 15px;
    margin-bottom: 25px;
  }

  h2 {
    font-size: 24px;
    font-weight: bold;
    color: #303133;
    margin: 0;
  }

  .tutorial-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
    gap: 20px;
  }

  .tutorial-card {
    height: 200px;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    padding: 20px;
    cursor: pointer;
    transition: transform 0.3s, box-shadow 0.3s;

    &:hover {
      transform: translateY(-5px);
      box-shadow: 0 12px 20px rgba(0, 0, 0, 0.1);
    }

    .tutorial-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 15px;

      .video-icon {
        font-size: 40px;
        color: #FF6B6B;
      }

      .article-icon {
        font-size: 40px;
        color: #f7b267;
      }
    }

    .tutorial-title {
      font-size: 18px;
      font-weight: bold;
      color: #303133;
      margin-bottom: 10px;
      line-height: 1.4;
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      line-clamp: 2;
      -webkit-box-orient: vertical;
    }

    .tutorial-meta {
      display: flex;
      justify-content: space-between;
      color: #909399;
      font-size: 14px;
    }
  }
}
</style>
