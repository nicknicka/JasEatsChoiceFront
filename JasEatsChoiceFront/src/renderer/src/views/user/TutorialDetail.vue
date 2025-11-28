<script setup>
import { ref, onMounted } from 'vue';
import { VideoCamera, Document, ArrowLeft } from '@element-plus/icons-vue';
import { useRouter, useRoute } from 'vue-router';

const router = useRouter();
const route = useRoute();

// 返回教程列表页面
const goBackToList = () => {
  router.push('/user/home/tutorials');
};

// 详细教程数据
const detailedTutorials = [
  {
    id: 1,
    title: '青木瓜沙拉制作教程',
    type: 'video',
    duration: '5:30',
    views: '12.5k',
    coverImage: 'https://picsum.photos/id/109/800/600',
    content: [
      '1. 将青木瓜去皮切片，越薄越好',
      '2. 加入花生、红辣椒、蒜末、鱼露、柠檬汁和糖',
      '3. 用木槌轻轻捣碎，使味道融合',
      '4. 加入西红柿片和生菜，搅拌均匀',
      '5. 装盘即可享用'
    ],
    tips: [
      '选择未成熟的青木瓜，口感更脆',
      '根据个人口味调整辣椒和鱼露的用量',
      '不要捣碎太久，保持木瓜片的脆度'
    ]
  },
  {
    id: 2,
    title: '夏日低卡饮食指南',
    type: 'article',
    duration: '8分钟',
    views: '8.2k',
    coverImage: 'https://picsum.photos/id/215/800/600',
    content: `夏日来临，如何在享受美食的同时保持健康体重？这篇低卡饮食指南将为您提供实用的建议。

**1. 多吃高纤维食物**：蔬菜、水果和全谷物富含纤维，能让您长时间保持饱腹感，减少零食的摄入。

**2. 选择健康的蛋白质来源**：鸡胸肉、鱼、豆类和低脂乳制品是优质的蛋白质来源，有助于维持肌肉质量。

**3. 控制碳水化合物的摄入量**：选择复杂碳水化合物，如燕麦、糙米和全麦面包，避免精制糖和白面粉制品。

**4. 多喝水**：保持充足的水分摄入，不仅能维持身体正常功能，还能减少饥饿感。

**5. 合理安排饮食时间**：规律的饮食时间有助于维持正常的新陈代谢，避免暴饮暴食。`,
    tips: [
      '尽量避免喝含糖饮料，选择白开水或茶水',
      '多吃季节性水果和蔬菜，味道更好营养更丰富',
      '适量运动结合健康饮食，效果更佳'
    ]
  },
  {
    id: 3,
    title: '健康早餐搭配技巧',
    type: 'video',
    duration: '3:45',
    views: '9.7k',
    coverImage: 'https://picsum.photos/id/1045/800/600',
    content: [
      '1. 选择一份主食，如燕麦粥或全麦面包',
      '2. 加入一份蛋白质，如鸡蛋、牛奶或豆浆',
      '3. 搭配一份水果或蔬菜，如香蕉、苹果或菠菜',
      '4. 可以添加一些坚果或种子，增加营养和口感'
    ],
    tips: [
      '早餐要吃好，保证足够的能量和营养',
      '避免吃过多油腻和甜的食物',
      '提前准备早餐，节省时间'
    ]
  }
];

// 当前教程
const currentTutorial = ref(null);

// 获取当前教程
onMounted(() => {
  const tutorialId = parseInt(route.params.id);
  currentTutorial.value = detailedTutorials.find(tutorial => tutorial.id === tutorialId);
});
</script>

<template>
  <div class="tutorial-detail-container">
    <div class="page-header">
      <el-button type="primary" :icon="ArrowLeft" size="small" @click="goBackToList">
        返回列表
      </el-button>
    </div>

    <el-card v-if="currentTutorial" class="tutorial-detail-card" shadow="hover">
      <div class="tutorial-cover">
        <img :src="currentTutorial.coverImage" :alt="currentTutorial.title" />
        <div class="tutorial-type-overlay">
          <el-icon :class="currentTutorial.type === 'video' ? 'video-icon' : 'article-icon'">
            <component :is="currentTutorial.type === 'video' ? VideoCamera : Document" />
          </el-icon>
          <span>{{ currentTutorial.type === 'video' ? '视频教程' : '图文指南' }}</span>
        </div>
      </div>

      <div class="tutorial-info">
        <h1 class="tutorial-title">{{ currentTutorial.title }}</h1>
        <div class="tutorial-meta">
          <span class="duration">{{ currentTutorial.duration }}</span>
          <span class="views">{{ currentTutorial.views }} 阅读/观看</span>
        </div>

        <div class="tutorial-content">
          <h3>内容</h3>
          <div v-if="currentTutorial.type === 'video'">
            <ol>
              <li v-for="(step, index) in currentTutorial.content" :key="index">
                {{ step }}
              </li>
            </ol>
          </div>
          <div v-else>
            <div v-html="currentTutorial.content"></div>
          </div>
        </div>

        <div class="tutorial-tips">
          <h3>小贴士</h3>
          <ul>
            <li v-for="(tip, index) in currentTutorial.tips" :key="index">
              {{ tip }}
            </li>
          </ul>
        </div>
      </div>
    </el-card>

    <div v-else class="not-found">
      <h2>教程不存在</h2>
      <el-button type="primary" @click="goBackToList">返回列表</el-button>
    </div>
  </div>
</template>

<style scoped lang="less">
.tutorial-detail-container {
  padding: 20px 0;

  .page-header {
    margin-bottom: 25px;
  }

  .tutorial-detail-card {
    padding: 0;
    max-width: 800px;
    margin: 0 auto;
  }

  .tutorial-cover {
    position: relative;
    width: 100%;
    height: 400px;
    overflow: hidden;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .tutorial-type-overlay {
      position: absolute;
      top: 20px;
      right: 20px;
      background: rgba(255, 255, 255, 0.9);
      padding: 8px 12px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      gap: 5px;

      .video-icon {
        color: #FF6B6B;
      }

      .article-icon {
        color: #f7b267;
      }

      span {
        font-weight: 600;
        color: #303133;
      }
    }
  }

  .tutorial-info {
    padding: 25px;

    .tutorial-title {
      font-size: 28px;
      font-weight: bold;
      color: #303133;
      margin-bottom: 15px;
    }

    .tutorial-meta {
      color: #909399;
      font-size: 16px;
      margin-bottom: 30px;
      display: flex;
      gap: 25px;
    }

    .tutorial-content {
      margin-bottom: 35px;

      h3 {
        font-size: 22px;
        font-weight: bold;
        color: #303133;
        margin-bottom: 15px;
      }

      ol {
        font-size: 16px;
        line-height: 2.0;
        color: #606266;
        padding-left: 25px;

        li {
          margin-bottom: 8px;
        }
      }

      p {
        font-size: 16px;
        line-height: 2.0;
        color: #606266;
        margin-bottom: 15px;
      }
    }

    .tutorial-tips {
      background-color: #f5f5f5;
      padding: 20px;
      border-radius: 8px;

      h3 {
        font-size: 20px;
        font-weight: bold;
        color: #303133;
        margin-bottom: 15px;
      }

      ul {
        font-size: 16px;
        line-height: 2.0;
        color: #606266;
        padding-left: 25px;

        li {
          margin-bottom: 8px;
        }
      }
    }
  }

  .not-found {
    text-align: center;
    padding: 60px 0;

    h2 {
      color: #606266;
      margin-bottom: 25px;
    }
  }
}
</style>
