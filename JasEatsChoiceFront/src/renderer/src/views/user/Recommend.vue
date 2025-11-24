<script setup>
import { ref } from 'vue';

// 我的推荐数据
const recommendations = ref([
  {
    id: 1,
    name: '健康轻食套餐',
    type: '午餐',
    calories: 420,
    tags: ['低卡', '高纤维', '蛋白质丰富'],
    reason: '基于您的卡路里目标和饮食偏好推荐',
    rating: 4.8,
    image: '🥗'
  },
  {
    id: 2,
    name: '营养早餐组合',
    type: '早餐',
    calories: 380,
    tags: ['营养均衡', '能量持久'],
    reason: '适合您的早起时间和健康需求',
    rating: 4.6,
    image: '🥪'
  },
  {
    id: 3,
    name: '轻脂晚餐',
    type: '晚餐',
    calories: 320,
    tags: ['低脂肪', '易消化'],
    reason: '帮助您保持每日卡路里摄入平衡',
    rating: 4.7,
    image: '🍲'
  },
  {
    id: 4,
    name: '健身补充餐',
    type: '加餐',
    calories: 280,
    tags: ['高蛋白', '增肌'],
    reason: '适合您的健身计划',
    rating: 4.9,
    image: '🥤'
  }
]);
</script>

<template>
  <div class="recommend-container">
    <h2>我的推荐</h2>

    <!-- 推荐列表 -->
    <div class="recommend-grid">
      <el-card
        v-for="item in recommendations"
        :key="item.id"
        class="recommend-card"
      >
        <div class="card-header">
          <div class="dish-image">{{ item.image }}</div>
          <div class="dish-info">
            <div class="dish-name">{{ item.name }}</div>
            <div class="dish-type">
              <el-tag type="primary" size="small">{{ item.type }}</el-tag>
            </div>
          </div>
        </div>

        <div class="calories-info">
          <span>🔥</span>
          <span>{{ item.calories }} kcal</span>
        </div>

        <div class="tags-section">
          <el-tag
            v-for="tag in item.tags"
            :key="tag"
            size="small"
            :type="tag === '低卡' ? 'success' : tag === '高纤维' ? 'warning' : tag === '蛋白质丰富' ? 'info' : 'primary'"
          >
            {{ tag }}
          </el-tag>
        </div>

        <div class="reason-section">
          <div class="reason-title">推荐理由</div>
          <div class="reason-text">{{ item.reason }}</div>
        </div>

        <div class="card-actions">
          <div class="rating">
            <el-rate
              v-model="item.rating"
              :disabled="true"
              show-text
            />
          </div>
          <el-button type="primary" size="small">立即查看</el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<style scoped lang="less">
.recommend-container {
  padding: 0 20px 20px 20px;

  h2 {
    font-size: 24px;
    margin: 0 0 20px 0;
  }

  .recommend-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
    gap: 20px;
  }

  .recommend-card {
    .card-header {
      display: flex;
      gap: 20px;
      margin-bottom: 15px;
      align-items: center;

      .dish-image {
        font-size: 60px;
      }

      .dish-info {
        .dish-name {
          font-size: 18px;
          font-weight: bold;
          margin-bottom: 5px;
        }
      }
    }

    .calories-info {
      display: flex;
      gap: 5px;
      color: #ff6b6b;
      font-weight: bold;
      margin-bottom: 15px;
      font-size: 16px;
    }

    .tags-section {
      margin-bottom: 15px;
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
    }

    .reason-section {
      margin-bottom: 20px;

      .reason-title {
        font-weight: bold;
        margin-bottom: 5px;
      }

      .reason-text {
        color: #666;
        font-size: 14px;
        line-height: 1.5;
      }
    }

    .card-actions {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .rating {
        :deep(.el-rate__text) {
          font-size: 14px;
        }
      }
    }
  }
}
</style>
