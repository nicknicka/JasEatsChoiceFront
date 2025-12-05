<template>
  <div class="diet-record-container">
    <!-- 顶部日历选择区域 -->
    <div class="calendar-section">
      <div class="section-header">
        <div class="section-title">
          <el-icon class="title-icon"><CalendarIcon /></el-icon>
          <span>饮食记录</span>
        </div>
        <div class="calendar-control">
          <el-date-picker
            v-model="selectedDate"
            type="date"
            placeholder="选择日期"
            format="YYYY年MM月DD日"
            value-format="YYYY-MM-DD"
            @change="handleDateChange"
            size="large"
            class="calendar-picker"
            popper-class="custom-date-picker"
          >
            <template #suffix-icon>
              <el-icon class="calendar-suffix-icon"><CalendarIcon /></el-icon>
            </template>
          </el-date-picker>
          <el-button type="primary" size="small" class="add-btn">
            <el-icon><Plus /></el-icon>
            添加记录
          </el-button>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-card">
      <div class="stat-item">
        <div class="stat-number">{{ totalCalories }}</div>
        <div class="stat-label">总卡路里</div>
      </div>
      <div class="stat-divider"></div>
      <div class="stat-item">
        <div class="stat-number">{{ dietRecords.length }}</div>
        <div class="stat-label">记录条数</div>
      </div>
    </div>

    <!-- 饮食记录显示区域 -->
    <div class="records-section">
      <div class="meal-sections">
        <!-- 早餐 -->
        <div v-if="getMealsByType('breakfast').length > 0" class="meal-section">
          <div class="meal-section-header breakfast">
            <el-icon class="meal-icon"><Sunrise /> </el-icon>
            <span class="meal-section-title">早餐</span>
            <el-tag size="small" type="success" class="meal-count">
              {{ getMealsByType('breakfast').length }}
            </el-tag>
          </div>
          <div class="meal-records">
            <div
              v-for="record in getMealsByType('breakfast')"
              :key="record.id"
              class="record-card"
              @mouseenter="hoverRecord = record.id"
              @mouseleave="hoverRecord = null"
            >
              <div class="record-info">
                <div class="record-time">{{ record.time }}</div>
                <div class="record-content">
                  <div class="food-name">{{ record.foodName }}</div>
                  <div class="food-description">{{ record.description }}</div>
                </div>
              </div>
              <div class="record-calories">
                <span class="calories-text">{{ record.calories }}</span>
                <span class="calories-unit">kcal</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 午餐 -->
        <div v-if="getMealsByType('lunch').length > 0" class="meal-section">
          <div class="meal-section-header lunch">
            <el-icon class="meal-icon"><Sunny /> </el-icon>
            <span class="meal-section-title">午餐</span>
            <el-tag size="small" type="warning" class="meal-count">
              {{ getMealsByType('lunch').length }}
            </el-tag>
          </div>
          <div class="meal-records">
            <div
              v-for="record in getMealsByType('lunch')"
              :key="record.id"
              class="record-card"
              @mouseenter="hoverRecord = record.id"
              @mouseleave="hoverRecord = null"
            >
              <div class="record-info">
                <div class="record-time">{{ record.time }}</div>
                <div class="record-content">
                  <div class="food-name">{{ record.foodName }}</div>
                  <div class="food-description">{{ record.description }}</div>
                </div>
              </div>
              <div class="record-calories">
                <span class="calories-text">{{ record.calories }}</span>
                <span class="calories-unit">kcal</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 晚餐 -->
        <div v-if="getMealsByType('dinner').length > 0" class="meal-section">
          <div class="meal-section-header dinner">
            <el-icon class="meal-icon"><Moon /> </el-icon>
            <span class="meal-section-title">晚餐</span>
            <el-tag size="small" type="danger" class="meal-count">
              {{ getMealsByType('dinner').length }}
            </el-tag>
          </div>
          <div class="meal-records">
            <div
              v-for="record in getMealsByType('dinner')"
              :key="record.id"
              class="record-card"
              @mouseenter="hoverRecord = record.id"
              @mouseleave="hoverRecord = null"
            >
              <div class="record-info">
                <div class="record-time">{{ record.time }}</div>
                <div class="record-content">
                  <div class="food-name">{{ record.foodName }}</div>
                  <div class="food-description">{{ record.description }}</div>
                </div>
              </div>
              <div class="record-calories">
                <span class="calories-text">{{ record.calories }}</span>
                <span class="calories-unit">kcal</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 加餐 -->
        <div v-if="getMealsByType('snack').length > 0" class="meal-section">
          <div class="meal-section-header snack">
            <el-icon class="meal-icon"><Coffee /> </el-icon>
            <span class="meal-section-title">加餐</span>
            <el-tag size="small" type="info" class="meal-count">
              {{ getMealsByType('snack').length }}
            </el-tag>
          </div>
          <div class="meal-records">
            <div
              v-for="record in getMealsByType('snack')"
              :key="record.id"
              class="record-card"
              @mouseenter="hoverRecord = record.id"
              @mouseleave="hoverRecord = null"
            >
              <div class="record-info">
                <div class="record-time">{{ record.time }}</div>
                <div class="record-content">
                  <div class="food-name">{{ record.foodName }}</div>
                  <div class="food-description">{{ record.description }}</div>
                </div>
              </div>
              <div class="record-calories">
                <span class="calories-text">{{ record.calories }}</span>
                <span class="calories-unit">kcal</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 空数据提示 -->
      <div v-if="dietRecords.length === 0" class="empty-records">
        <el-empty
          image="https://cdn-icons-png.flaticon.com/128/4385/4385277.png"
          description="暂无饮食记录，点击右上角添加按钮开始记录吧"
        >
          <template #bottom>
            <el-button type="primary" class="add-empty-btn">
              <el-icon><Plus /></el-icon>
              添加第一条记录
            </el-button>
          </template>
        </el-empty>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import {
  Calendar as CalendarIcon,
  Plus,
  Sunrise,
  Sunny,
  Moon,
  Coffee
} from '@element-plus/icons-vue'
import api from '../../utils/api.js'
import { API_CONFIG } from '../../config/index.js'

// 日历选择的日期，默认是今天
const selectedDate = ref(new Date().toISOString().split('T')[0])

// 饮食记录数据
const dietRecords = ref([])

// 记录悬停状态
const hoverRecord = ref(null)

// 计算总卡路里
const totalCalories = ref(0)

// 根据餐食类型筛选记录
const getMealsByType = (mealType) => {
  return dietRecords.value.filter(record => record.mealType === mealType)
}

// 更新总卡路里
const updateTotalCalories = () => {
  totalCalories.value = dietRecords.value.reduce((total, record) => total + (record.calories || 0), 0)
}

// 使用模拟数据
const useMockData = () => {
  dietRecords.value = [
    {
      id: 1,
      mealType: 'breakfast',
      mealTypeName: '早餐',
      time: '08:30',
      foodName: '牛奶',
      calories: 120,
      description: '纯牛奶250ml'
    },
    {
      id: 2,
      mealType: 'breakfast',
      mealTypeName: '早餐',
      time: '08:45',
      foodName: '鸡蛋',
      calories: 140,
      description: '煮鸡蛋1个'
    },
    {
      id: 3,
      mealType: 'lunch',
      mealTypeName: '午餐',
      time: '12:30',
      foodName: '米饭',
      calories: 116,
      description: '白米饭100g'
    },
    {
      id: 4,
      mealType: 'lunch',
      mealTypeName: '午餐',
      time: '12:45',
      foodName: '青菜',
      calories: 50,
      description: '清炒青菜200g'
    },
    {
      id: 5,
      mealType: 'snack',
      mealTypeName: '加餐',
      time: '15:30',
      foodName: '苹果',
      calories: 95,
      description: '中等大小苹果1个'
    }
  ]
}

// 从后端获取饮食记录
const fetchDietRecords = async (date) => {
  try {
    // 这里需要获取当前登录用户的ID，假设已经存储在localStorage中
    const userInfo = JSON.parse(localStorage.getItem('userInfo'))
    if (!userInfo || !userInfo.userId) {
      console.error('未找到用户信息')
      // 如果没有用户信息，使用模拟数据
      useMockData()
      updateTotalCalories()
      return
    }

    // 格式化日期为YYYY-MM-DD格式
    const formattedDate = date

    // 调用后端API
    const apiUrl = API_CONFIG.diet.date.replace('{userId}', userInfo.userId) + formattedDate
    const response = await api.get(apiUrl)

    // 将后端返回的数据转换为前端需要的格式
    if (response && response.data) {
      const backendRecords = response.data

      // 转换数据格式
      dietRecords.value = backendRecords.map(record => ({
        id: record.id,
        mealType: record.mealTime === '早餐' ? 'breakfast' :
                  record.mealTime === '午餐' ? 'lunch' :
                  record.mealTime === '晚餐' ? 'dinner' : 'snack',
        mealTypeName: record.mealTime,
        time: record.recordTime ? record.recordTime.split('T')[1].substring(0, 5) : '',
        foodName: '', // 这里需要从菜品ID获取菜品名称，暂时留空
        calories: record.calorie,
        description: '' // 可以根据菜品信息生成描述
      }))
    }

    // 更新总卡路里
    updateTotalCalories()
  } catch (error) {
    console.error('获取饮食记录失败:', error)
    // 失败时可以使用模拟数据
    useMockData()
    updateTotalCalories()
  }
}

// 处理日期变化
const handleDateChange = (date) => {
  selectedDate.value = date
  // 根据选择的日期加载对应的饮食记录数据
  fetchDietRecords(date)
}

// 页面加载时初始化数据
onMounted(() => {
  // 加载默认日期的饮食记录数据
  fetchDietRecords(selectedDate.value)
})
</script>

<style scoped>
.diet-record-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 0;
}

/* 顶部日历区域 */
.calendar-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 32px;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 28px;
  font-weight: 700;
  color: white;
}

.title-icon {
  font-size: 32px;
}

.calendar-control {
  display: flex;
  align-items: center;
  gap: 12px;
}

.calendar-picker {
  background-color: white;
  border-radius: 8px;
  padding: 4px;
}

.calendar-suffix-icon {
  color: #667eea;
}

.add-btn {
  border-radius: 8px;
  font-weight: 600;
  background: white;
  color: #667eea;
  border: 2px solid white;
  transition: all 0.3s ease;
}

.add-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  color: white;
}

/* 统计卡片 */
.stats-card {
  display: flex;
  align-items: center;
  gap: 32px;
  background-color: white;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.stat-item {
  text-align: center;
}

.stat-number {
  font-size: 32px;
  font-weight: 700;
  color: #333;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #999;
  font-weight: 500;
}

.stat-divider {
  width: 2px;
  height: 48px;
  background-color: #e8e8e8;
}

/* 记录区域 */
.records-section {
  flex: 1;
  background-color: white;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  overflow-y: auto;
}

.meal-sections {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 餐食分类标题 */
.meal-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.meal-section-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 8px;
  font-size: 18px;
  font-weight: 600;
  color: white;
}

.meal-section-header.breakfast {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.meal-section-header.lunch {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.meal-section-header.dinner {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.meal-section-header.snack {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.meal-icon {
  font-size: 24px;
}

.meal-count {
  margin-left: auto;
}

/* 记录卡片 */
.meal-records {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 8px;
}

.record-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background-color: #f9fafb;
  border-radius: 12px;
  border-left: 4px solid #e5e7eb;
  transition: all 0.3s ease;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.record-card:hover {
  background-color: white;
  border-left-color: #667eea;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.record-info {
  flex: 1;
  min-width: 0;
}

.record-time {
  font-size: 14px;
  color: #6b7280;
  font-weight: 500;
  margin-bottom: 4px;
}

.food-name {
  font-size: 18px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 2px;
}

.food-description {
  font-size: 14px;
  color: #6b7280;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.record-calories {
  text-align: right;
  margin-left: 24px;
}

.calories-text {
  display: block;
  font-size: 24px;
  font-weight: 700;
  color: #ef4444;
}

.calories-unit {
  font-size: 12px;
  color: #9ca3af;
  font-weight: 500;
}

/* 空数据 */
.empty-records {
  margin: 60px 0;
  text-align: center;
}

.add-empty-btn {
  margin-top: 24px;
  border-radius: 8px;
  font-weight: 600;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .calendar-section {
    padding: 20px;
  }

  .section-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .section-title {
    font-size: 24px;
  }

  .stats-card {
    gap: 16px;
    padding: 16px;
  }

  .stat-number {
    font-size: 24px;
  }

  .record-card {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .record-calories {
    margin-left: 0;
  }
}
</style>