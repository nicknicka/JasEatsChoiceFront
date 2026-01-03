<script setup>
import { ref, onMounted } from 'vue'
import CommonLocationPicker from '../../components/CommonLocationPicker.vue'
import CommonWeatherWidget from '../../components/CommonWeatherWidget.vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'
import { API_CONFIG } from '../../config/index.js'

const router = useRouter()

// 定位相关
const locationPicker = ref(null)
const currentLocation = ref(null)
const locationError = ref(false)
const currentCity = ref('')

// 天气相关
const weatherWidget = ref(null)

// 定位变化处理
const handleLocationChanged = (locationData) => {
  currentLocation.value = locationData.location
  currentCity.value = locationData.city
  // 根据位置更新推荐
  updateRecommendationsByLocation(locationData)
}

// 天气变化处理
const handleWeatherUpdated = (weatherData) => {
  // 根据天气数据更新推荐
  // 这里可以使用weatherData.weatherTags直接生成推荐
  console.log('天气数据更新:', weatherData)
}

// 定位成功后的处理
const handleLocationSuccess = (position) => {
  const { latitude, longitude } = position.coords
  const accuracy = position.coords.accuracy

  currentLocation.value = { latitude, longitude }

  // 检查定位误差是否超过500米
  if (accuracy > 500) {
    locationError.value = true
    ElMessageBox.warning({
      title: '定位误差提示',
      message: `当前定位误差为${Math.round(accuracy)}米，可能影响推荐准确性。是否重新定位？`,
      confirmButtonText: '重新定位',
      cancelButtonText: '取消',
      callback: (action) => {
        if (action === 'confirm') {
          getCurrentLocation()
        }
      }
    })
  } else {
    locationError.value = false
    ElMessage.success(`定位成功，误差${Math.round(accuracy)}米`)
    // 可以在这里调用根据位置更新推荐的函数
    updateRecommendationsByLocation({ latitude, longitude })
  }
}

// 定位失败后的处理
const handleLocationError = (error) => {
  let errorMessage = '定位失败'
  switch (error.code) {
    case error.PERMISSION_DENIED:
      // 用户拒绝定位请求，不显示错误信息，避免重复打扰
      locationError.value = true
      return
    case error.POSITION_UNAVAILABLE:
      errorMessage = '定位信息不可用'
      break
    case error.TIMEOUT:
      errorMessage = '定位请求超时'
      break
    case error.UNKNOWN_ERROR:
      errorMessage = '未知定位错误'
      break
  }
  locationError.value = true
  ElMessage.error(errorMessage)
}

// 获取当前位置
const getCurrentLocation = () => {
  if ('geolocation' in navigator) {
    navigator.geolocation.getCurrentPosition(handleLocationSuccess, handleLocationError, {
      enableHighAccuracy: true,
      timeout: 10000,
      maximumAge: 300000
    })
  } else {
    locationError.value = true
    ElMessage.error('您的浏览器不支持GPS定位功能')
  }
}

// 天气与时间双维度推荐逻辑
const updateRecommendationsByWeatherAndTime = async () => {
  // 检查用户是否已关闭天气推荐
  const savedSettings = localStorage.getItem('userSettings')
  let weatherRecommendationEnabled = true

  if (savedSettings) {
    const parsedSettings = JSON.parse(savedSettings)
    weatherRecommendationEnabled = parsedSettings.privacy?.weatherRecommendation !== false
  }

  if (!weatherRecommendationEnabled) {
    console.log('用户已关闭天气推荐')
    return
  }

  try {
    // 调用真实天气API获取天气数据
    const response = await axios.get(API_CONFIG.baseURL + API_CONFIG.weather.current, {
      params: { city: '北京' } // 默认查询北京天气，实际应用中可以先获取定位再查询
    })
    const weatherData = response.data.data
    const { temperature, humidity } = weatherData
    const now = new Date()
    const hour = now.getHours()

    // 时间维度推荐：早餐、午餐、晚餐、夜宵
    let timeType = ''
    if (hour >= 6 && hour < 10) timeType = '早餐'
    else if (hour >= 10 && hour < 14) timeType = '午餐'
    else if (hour >= 14 && hour < 18) timeType = '下午茶'
    else if (hour >= 18 && hour < 22) timeType = '晚餐'
    else timeType = '夜宵'

    // 天气维度推荐
    let weatherTags = []
    if (temperature > 30) weatherTags.push('冰饮', '凉菜', '轻食')
    else if (temperature < 10) weatherTags.push('热饮', '热菜', '火锅')
    if (humidity > 80) weatherTags.push('祛湿粥品', '清淡饮食')

    // 根据天气和时间推荐菜品（模拟）
    const weatherTimeRecommendations = generateWeatherTimeRecommendations(
      timeType,
      weatherTags,
      weatherData
    )

    // 将天气时间推荐添加到推荐列表
    recommendations.value = [...recommendations.value, ...weatherTimeRecommendations]

    // 为标签分配随机类型
    assignRandomTagTypes(recommendations.value)
  } catch (error) {
    console.error('天气推荐失败:', error)
    ElMessage.error('天气推荐功能暂时不可用')
  }
}

// 根据天气和时间生成推荐菜品（模拟）
const generateWeatherTimeRecommendations = (timeType, weatherTags) => {
  const rejectionHistory = loadRejectionHistory()
  // 模拟菜品数据库
  const dishes = [
    {
      name: '冰爽柠檬水',
      type: '冰饮',
      calories: 50,
      tags: ['冰饮', '夏季', '解渴']
    },
    { name: '凉拌黄瓜', type: '凉菜', calories: 80, tags: ['凉菜', '夏季', '清爽'] },
    { name: '鸡肉沙拉', type: '轻食', calories: 350, tags: ['轻食', '健康', '低卡'] },
    { name: '南瓜粥', type: '热饮', calories: 120, tags: ['热饮', '早餐', '营养'] },
    { name: '红烧肉', type: '热菜', calories: 450, tags: ['热菜', '晚餐', '贴秋膘'] },
    { name: '羊肉火锅', type: '火锅', calories: 600, tags: ['火锅', '冬季', '暖胃'] },
    {
      name: '红豆薏米粥',
      type: '祛湿粥品',
      calories: 150,
      tags: ['祛湿粥品', '养生', '清淡']
    },
    { name: '菊花茶', type: '热饮', calories: 30, tags: ['热饮', '下午茶', '清火'] }
  ]

  // 根据时间、天气标签和用户拒绝历史过滤菜品
  const filteredDishes = dishes.filter((dish) => {
    // 时间和天气标签过滤
    const matchesCriteria =
      dish.type.includes(timeType) || weatherTags.some((tag) => dish.tags.includes(tag))

    // 检查该菜品是否被拒绝过多次
    const rejectionEntry = rejectionHistory.find(
      (entry) => entry.name.includes(dish.name) || dish.name.includes(entry.name)
    )

    // 如果匹配时间和天气标签，且拒绝次数小于等于2次，推荐；否则不推荐
    return matchesCriteria && (!rejectionEntry || rejectionEntry.count <= 2)
  })

  // 转换为推荐格式
  return filteredDishes.map((dish, index) => ({
    id: recommendations.value.length + index + 1,
    name: `${timeType}推荐: ${dish.name}`,
    type: timeType,
    calories: dish.calories,
    tags: [...dish.tags, timeType],
    reason: `${timeType}${weatherTags.length > 0 ? `，${weatherTags.join('、')}适合` : '适合'}`,
    rating: 4.8,
    image: '🍱'
  }))
}

// 根据位置更新推荐（模拟）
const updateRecommendationsByLocation = (location) => {
  // 这里可以添加根据经纬度获取附近商家和推荐菜品的逻辑
  console.log('根据位置更新推荐:', location)
}

// 节日/节气与特色菜品映射
const festivalDishes = {
  // 节气
  立春: ['春卷', '春饼', '菠菜汤'],
  雨水: ['南瓜粥', '豆苗炒鸡蛋', '清蒸鲈鱼'],
  惊蛰: ['春笋', '韭菜炒鸡蛋', '山药排骨汤'],
  春分: ['春茶', '青团', '马兰头拌香干'],
  清明: ['清明粿', '青团', '乌米饭'],
  谷雨: ['谷雨茶', '香椿炒蛋', '鲫鱼炖豆腐'],
  立夏: ['立夏饭', '乌米饭', '咸鸭蛋'],
  小满: ['苦菜', '苦瓜炒鸡蛋', '绿豆汤'],
  芒种: ['青梅酒', '芒果布丁', '凉拌黄瓜'],
  夏至: ['夏至面', '绿豆汤', '西瓜'],
  小暑: ['凉面', '冰淇淋', '薄荷茶'],
  大暑: ['大暑羊', '凉茶', '西瓜'],
  立秋: ['贴秋膘', '红烧肉', '炖鸡'],
  处暑: ['老鸭汤', '莲藕排骨汤', '炒菱角'],
  白露: ['白露茶', '桂圆莲子粥', '烤鸭'],
  秋分: ['秋分蟹', '大闸蟹', '葡萄'],
  寒露: ['菊花酒', '芝麻糕', '银耳羹'],
  霜降: ['柿子', '牛肉火锅', '萝卜汤'],
  立冬: ['立冬饺', '羊肉汤', '板栗'],
  小雪: ['腊味', '火锅', '热奶茶'],
  大雪: ['腌肉', '羊肉火锅', '红薯粥'],
  冬至: ['冬至饺', '汤圆', '羊肉汤'],
  小寒: ['腊八粥', '炖羊肉', '热可可'],
  大寒: ['大寒粥', '涮羊肉', '暖锅'],

  // 节日
  春节: ['饺子', '年糕', '年夜饭'],
  元宵: ['元宵', '汤圆', '花灯'],
  端午: ['粽子', '雄黄酒', '咸鸭蛋'],
  中秋: ['月饼', '柚子', '螃蟹'],
  重阳: ['重阳糕', '菊花酒', '登高'],
  腊八: ['腊八粥', '腊八蒜', '腊八豆腐']
}

// 判断当前日期对应的节气或节日（简化实现）
const getCurrentFestival = () => {
  const now = new Date()
  const month = now.getMonth() + 1
  const day = now.getDate()

  // 简化的节气判断（实际应该使用更精确的算法）
  const solarTerms = [
    { name: '小寒', month: 1, day: 5 },
    { name: '大寒', month: 1, day: 20 },
    { name: '立春', month: 2, day: 4 },
    { name: '雨水', month: 2, day: 19 },
    { name: '惊蛰', month: 3, day: 5 },
    { name: '春分', month: 3, day: 20 },
    { name: '清明', month: 4, day: 4 },
    { name: '谷雨', month: 4, day: 19 },
    { name: '立夏', month: 5, day: 5 },
    { name: '小满', month: 5, day: 20 },
    { name: '芒种', month: 6, day: 5 },
    { name: '夏至', month: 6, day: 21 },
    { name: '小暑', month: 7, day: 7 },
    { name: '大暑', month: 7, day: 22 },
    { name: '立秋', month: 8, day: 7 },
    { name: '处暑', month: 8, day: 23 },
    { name: '白露', month: 9, day: 7 },
    { name: '秋分', month: 9, day: 23 },
    { name: '寒露', month: 10, day: 8 },
    { name: '霜降', month: 10, day: 23 },
    { name: '立冬', month: 11, day: 7 },
    { name: '小雪', month: 11, day: 22 },
    { name: '大雪', month: 12, day: 7 },
    { name: '冬至', month: 12, day: 21 }
  ]

  // 检查节日
  if (month === 1 && day === 1) return '春节'
  if (month === 1 && day >= 15) return '元宵'
  if (month === 5 && day === 5) return '端午'
  if (month === 8 && day === 15) return '中秋'
  if (month === 9 && day === 9) return '重阳'
  if (month === 12 && day === 8) return '腊八'

  // 检查节气
  for (const term of solarTerms) {
    if (term.month === month && term.day === day) {
      return term.name
    }
  }

  return null
}

// 根据节日/节气添加特色菜品推荐
const addFestivalRecommendations = () => {
  const currentFestival = getCurrentFestival()
  if (currentFestival && festivalDishes[currentFestival]) {
    const festivalDishList = festivalDishes[currentFestival]
    const rejectionHistory = loadRejectionHistory()

    // 为每个特色菜品创建推荐项
    const festivalRecommendations = festivalDishList
      .filter((dishName) => {
        // 检查该菜品是否被拒绝过多次
        const rejectionEntry = rejectionHistory.find(
          (entry) => entry.name.includes(dishName) || dishName.includes(entry.name)
        )
        // 如果拒绝次数小于等于2次，仍然推荐；否则不推荐
        return !rejectionEntry || rejectionEntry.count <= 2
      })
      .map((dishName, index) => {
        // 根据菜品名称选择合适的图标
        let icon = '🍲'
        if (dishName.includes('饺子') || dishName.includes('饺')) icon = '🥟'
        if (dishName.includes('粽子') || dishName.includes('粽')) icon = '🍙'
        if (dishName.includes('月饼') || dishName.includes('饼')) icon = '🥮'
        if (dishName.includes('汤') || dishName.includes('羹')) icon = '🍵'
        if (dishName.includes('茶')) icon = '🍵'

        return {
          id: recommendations.value.length + index + 1,
          name: `${currentFestival}特色: ${dishName}`,
          type: '节日特供',
          calories: 0,
          tags: ['节日特供', currentFestival],
          reason: `${currentFestival}传统特色美食`,
          rating: 4.9,
          image: icon
        }
      })

    // 将节日推荐添加到推荐列表
    recommendations.value = [...festivalRecommendations, ...recommendations.value]

    // 为标签分配随机类型
    assignRandomTagTypes(recommendations.value)
  }
}

// 加载用户拒绝的推荐历史
const loadRejectionHistory = () => {
  const saved = localStorage.getItem('rejectionHistory')
  return saved ? JSON.parse(saved) : []
}

// 保存用户拒绝的推荐历史
const saveRejectionHistory = (history) => {
  localStorage.setItem('rejectionHistory', JSON.stringify(history))
}

// 拒绝推荐
const rejectRecommendation = (item) => {
  // 获取当前拒绝历史
  let rejectionHistory = loadRejectionHistory()

  // 查找是否已经拒绝过该菜品
  const existingIndex = rejectionHistory.findIndex(
    (entry) => entry.name === item.name && entry.type === item.type
  )

  if (existingIndex > -1) {
    // 如果已经拒绝过，增加拒绝次数
    rejectionHistory[existingIndex].count += 1
  } else {
    // 如果没有拒绝过，添加到历史
    rejectionHistory.push({
      name: item.name,
      type: item.type,
      tags: item.tags,
      count: 1,
      rejectedAt: new Date().toISOString()
    })
  }

  // 保存更新后的拒绝历史
  saveRejectionHistory(rejectionHistory)

  // 从当前推荐列表中移除该菜品
  const itemIndex = recommendations.value.findIndex((rec) => rec.id === item.id)
  if (itemIndex > -1) {
    recommendations.value.splice(itemIndex, 1)
  }

  ElMessage.success('已标记为不感兴趣')
}

// 定义随机标签类型数组
const tagTypes = ['primary', 'success', 'warning', 'info', 'danger']

// 获取随机标签类型
const getRandomTagType = () => {
  return tagTypes[Math.floor(Math.random() * tagTypes.length)]
}

// 为每个推荐项的标签分配随机类型（只在数据加载时执行一次）
const assignRandomTagTypes = (recommendations) => {
  recommendations.forEach((item) => {
    if (item.tags && Array.isArray(item.tags)) {
      // 为每个标签创建带有随机类型的对象
      item.tagsWithType = item.tags
        .map((tag) => ({
          name: tag,
          type: getRandomTagType()
        }))
        .filter((tag) => tag.name && tag.name.trim() !== '') // 过滤空标签
    } else {
      item.tagsWithType = []
    }
  })
}

// 从后端获取推荐数据
const fetchRecommendationsFromBackend = async () => {
  try {
    isLoading.value = true
    // 获取用户ID（从localStorage中获取，与其他页面保持一致）
    const userId = parseInt(localStorage.getItem('userId') || '1', 10)

    // 调用后端个性化推荐接口
    const response = await axios.get(`${API_CONFIG.baseURL}/v1/recommend/recommend/${userId}`)

    // 检查响应结构并提取推荐菜品
    const data = response.data.data
    if (data && data.dishes) {
      recommendations.value = data.dishes

      // 添加推荐理由等信息
      recommendations.value.forEach((dish) => {
        // 根据菜品信息生成推荐理由
        if (!dish.reason) {
          dish.reason = '基于您的饮食偏好推荐'
        }
        // 确保每个菜品有必要的字段
        dish.rating = dish.rating || 4.5
        dish.image = dish.image || '🍱'
      })

      // 为标签分配随机类型
      assignRandomTagTypes(recommendations.value)

      return data.dishes
    } else {
      recommendations.value = []
      return []
    }
  } catch (error) {
    console.error('获取推荐数据失败:', error)

    // 后端接口失败时，继续使用mock数据作为后备
    ElMessage.warning('个性化推荐服务暂时不可用，将为您提供基于节日和天气的推荐')

    // 先清除现有推荐数据，避免重复
    recommendations.value = []
    // 保持现有的mock数据生成逻辑
    addFestivalRecommendations()
    updateRecommendationsByWeatherAndTime()

    return null
  } finally {
    isLoading.value = false
  }
}

// 页面加载时获取定位和后端推荐数据
onMounted(async () => {
  getCurrentLocation()
  await fetchRecommendationsFromBackend()
})

// 加载状态
const isLoading = ref(false)

// 我的推荐数据
const recommendations = ref([])
</script>

<template>
  <div class="recommend-container">
    <!-- 引入定位和天气组件 -->
    <CommonLocationPicker
      ref="locationPicker"
      auto-locate
      @location-changed="handleLocationChanged"
    />
    <CommonWeatherWidget
      ref="weatherWidget"
      :city="currentCity"
      @weather-updated="handleWeatherUpdated"
    />

    <h2>我的推荐</h2>

    <!-- 加载中状态 -->
    <div class="loading-skeleton" v-if="isLoading">
      <el-skeleton :rows="6" type="card" :border="false" />
    </div>

    <!-- 推荐列表 -->
    <transition-group
      name="recommend-card"
      tag="div"
      class="recommend-grid"
      v-else-if="recommendations.length > 0"
    >
      <el-card v-for="item in recommendations" :key="item.id" class="recommend-card">
        <div class="card-header">
          <div class="dish-image">{{ item.image }}</div>
          <div class="dish-info">
            <div class="dish-name">{{ item.name }}</div>
            <div class="dish-type">
              <el-tag type="primary" size="small" v-if="item.type">{{ item.type }}</el-tag>
              <el-tag type="info" size="small" effect="plain" v-else>未分类</el-tag>
            </div>
          </div>
        </div>

        <div class="calories-info" v-if="item.calories">
          <span>🔥</span>
          <span>{{ item.calories }} kcal</span>
        </div>
        <div class="calories-info-unavailable" v-else>
          <span>🔥</span>
          <span>卡路里信息暂不可用</span>
        </div>

        <div class="tags-section">
          <el-tag v-for="tag in item.tagsWithType" :key="tag.name" size="small" :type="tag.type">
            {{ tag.name }}
          </el-tag>
        </div>

        <div class="reason-section">
          <div class="reason-title">推荐理由</div>
          <div class="reason-text" :class="{ 'empty-reason': !item.reason }">
            {{ item.reason || '暂无推荐理由' }}
          </div>
        </div>

        <div class="rating">
          <el-rate v-model="item.rating" :disabled="true" show-text />
        </div>

        <div class="card-actions">
          <el-button
            type="primary"
            size="small"
            @click="
              router.push({
                path: '/user/home/merchants',
                query: {
                  search: item.name.replace(/(.*推荐:|.*特色:)/, '').trim()
                }
              })
            "
            >立即下单</el-button
          >
          <el-button type="text" size="small" @click="rejectRecommendation(item)"
            >不感兴趣</el-button
          >
        </div>
      </el-card>
    </transition-group>

    <!-- 空状态提示 -->
    <div class="empty-state" v-else>
      <div class="empty-icon">🥺</div>
      <div class="empty-text">暂无推荐数据</div>
      <div class="empty-subtext">系统正在努力为您生成个性化推荐</div>
      <el-button type="primary" size="small" @click="fetchRecommendationsFromBackend"
        >重试获取推荐</el-button
      >
    </div>
  </div>
</template>

<style scoped lang="less">
.recommend-container {
  padding: 0 20px 20px 20px;

  h2 {
    font-size: 32px;
    margin: 0 0 32px 20px;
    color: #1a202c;
    font-weight: 800;
    letter-spacing: -0.5px;

    // 添加装饰性下划线
    &::after {
      content: '';
      display: block;
      width: 60px;
      height: 4px;
      background: linear-gradient(135deg, #23d160 0%, #20c997 100%);
      border-radius: 2px;
      margin-top: 12px;
    }
  }

  .recommend-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
    gap: 20px;
    padding: 0 20px;
  }

  .recommend-card {
    width: 100%;
    box-sizing: border-box;
    transition: all 0.3s ease;
    border-radius: 12px;
    box-shadow: 0 2px 15px rgba(0, 0, 0, 0.08);

    &:hover {
      box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
      transform: translateY(-8px) scale(1.02);
    }

    .card-header {
      display: flex;
      gap: 20px;
      margin-bottom: 20px;
      align-items: center;
      padding: 0;

      .dish-image {
        font-size: 70px;
        line-height: 1;
      }

      .dish-info {
        flex: 1;

        .dish-name {
          font-size: 20px;
          font-weight: bold;
          margin-bottom: 8px;
          color: #333;
        }
      }
    }

    .calories-info {
      display: flex;
      gap: 10px;
      color: #ff6b6b;
      font-weight: 800;
      margin-bottom: 18px;
      font-size: 20px;
      align-items: center;
      padding: 8px 16px;
      background: linear-gradient(135deg, #fff5f5 0%, #ffe4e4 100%);
      border-radius: 20px;
    }

    .calories-info-unavailable {
      display: flex;
      gap: 8px;
      color: #c0c4cc;
      margin-bottom: 18px;
      font-size: 14px;
      align-items: center;
    }

    .tags-section {
      margin-bottom: 20px;
      display: flex;
      flex-wrap: wrap;
      gap: 10px;

      :deep(.el-tag) {
        border-radius: 20px;
      }
    }

    .reason-section {
      margin-bottom: 24px;

      .reason-title {
        font-weight: bold;
        margin-bottom: 8px;
        color: #333;
        font-size: 15px;
      }

      .reason-text {
        color: #666;
        font-size: 14px;
        line-height: 1.6;
      }

      .reason-text.empty-reason {
        color: #c0c4cc;
        font-style: italic;
      }
    }

    .rating {
      margin-bottom: 24px;

      :deep(.el-rate__text) {
        font-size: 14px;
        color: #e6a23c;
      }

      :deep(.el-rate__icon) {
        font-size: 16px;
      }
    }

    .card-actions {
      display: flex;
      justify-content: center; // 居中显示按钮
      align-items: center;
      gap: 15px; // 按钮间距
      padding-top: 16px;
      border-top: 1px solid #f0f0f0;

      .el-button {
        border-radius: 8px;
        font-weight: 500;
      }
    }
  }

  /* 推荐卡片过渡动画 */
  .recommend-card-move {
    transition: all 0.5s ease;
  }

  .recommend-card-enter-active,
  .recommend-card-leave-active {
    transition:
      opacity 0.3s ease,
      transform 0.5s ease;
  }

  .recommend-card-enter-from {
    opacity: 0;
    transform: translateY(20px);
  }

  .recommend-card-leave-to {
    opacity: 0;
    transform: translateY(-20px);
  }

  // 加载中样式
  .loading-skeleton {
    padding: 20px 0;
  }
}

.empty-state {
  text-align: center;
  padding: 100px 20px;
  background-color: #ffffff;
  border-radius: 12px;
  margin-top: 20px;
  box-shadow: 0 2px 15px rgba(0, 0, 0, 0.08);
  border: 1px dashed #e4e7ed;

  .empty-icon {
    font-size: 90px;
    margin-bottom: 20px;
    opacity: 0.7;
  }

  .empty-text {
    font-size: 22px;
    font-weight: bold;
    color: #333;
    margin-bottom: 12px;
  }

  .empty-subtext {
    font-size: 14px;
    color: #909399;
    margin-bottom: 36px;
    line-height: 1.6;
  }

  .el-button {
    border-radius: 8px;
    font-weight: 500;
    padding: 10px 24px;
  }
}
</style>
