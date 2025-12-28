<script setup>
import { ref, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'
import { API_CONFIG } from '../config/index.js'

// 对外暴露的属性和事件
const props = defineProps({
  // 是否自动获取定位
  autoLocate: {
    type: Boolean,
    default: false
  },
  // 是否显示定位误差提示
  showAccuracyAlert: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['location-changed', 'location-error'])

// 定位相关状态
const isLocating = ref(false)
const currentLocation = ref(null)
const locationError = ref(false)
const locationCity = ref('')

// 定位成功后的处理
const handleLocationSuccess = (position) => {
  const { latitude, longitude } = position.coords
  const accuracy = position.coords.accuracy

  currentLocation.value = { latitude, longitude }

  // 检查定位误差是否超过500米
  if (showAccuracyAlert.value && accuracy > 500) {
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
    // 通过逆地理编码获取城市信息
    getCityByLocation(latitude, longitude)
  }
}

// 定位失败后的处理
const handleLocationError = (error) => {
  let errorMessage = '定位失败'
  switch (error.code) {
    case error.PERMISSION_DENIED:
      // 用户拒绝定位请求，不显示错误信息，避免重复打扰
      locationError.value = true
      emit('location-error', error)
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
  emit('location-error', error)
}

// 获取当前位置
const getCurrentLocation = () => {
  if ('geolocation' in navigator) {
    isLocating.value = true
    navigator.geolocation.getCurrentPosition(
      (position) => {
        isLocating.value = false
        handleLocationSuccess(position)
      },
      (error) => {
        isLocating.value = false
        handleLocationError(error)
      },
      { enableHighAccuracy: true, timeout: 10000, maximumAge: 300000 }
    )
  } else {
    locationError.value = true
    ElMessage.error('您的浏览器不支持GPS定位功能')
  }
}

// 根据经纬度获取城市信息
const getCityByLocation = async (latitude, longitude) => {
  try {
    const response = await axios.get(API_CONFIG.baseURL + API_CONFIG.location.location, {
      params: {
        latitude,
        longitude
      }
    })
    if (response.data) {
      const { city } = response.data
      locationCity.value = city
      // 对外发射定位结果
      emit('location-changed', {
        latitude,
        longitude,
        city,
        location: currentLocation.value
      })
    }
  } catch (error) {
    console.error('逆地理编码失败:', error)
    // 即使获取城市失败，也发射定位结果
    emit('location-changed', {
      latitude,
      longitude,
      city: '',
      location: currentLocation.value
    })
  }
}

// 手动选择城市
const selectCity = async (city) => {
  if (!city) return

  locationCity.value = city
  // 对外发射定位结果（手动选择城市时，经纬度为空）
  emit('location-changed', {
    latitude: null,
    longitude: null,
    city,
    location: null
  })
}

// 组件挂载时自动定位
onMounted(() => {
  if (props.autoLocate) {
    getCurrentLocation()
  }
})

// 暴露方法给父组件
defineExpose({
  getCurrentLocation,
  selectCity,
  currentLocation,
  locationCity,
  locationError
})
</script>

<template>
  <!-- 该组件主要提供定位功能，不包含UI展示 -->
</template>
