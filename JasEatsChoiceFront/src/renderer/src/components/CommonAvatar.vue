<template>
  <!-- 单一容器解决 class 传递问题 -->
  <!-- 将所有未识别的属性传递给容器 -->
  <div
    class="avatar-container"
    @click="handleAvatarClick"
    :style="{ cursor: clickToEnlarge ? 'pointer' : 'default' }"
    v-bind="$attrs"
  >
    <!-- 加载状态：仅当有头像URL且未加载完成时显示 -->
    <el-skeleton v-if="avatarUrl && !isLoaded" :size="size" shape="circle" active />

    <!-- 头像主体 -->
    <el-avatar
      v-else
      :size="size"
      class="user-avatar"
      :src="avatarUrl"
      :shape="shape"
      v-loading="isLoading"
      element-loading-text="加载中..."
    >
      <!-- 默认显示：当头像URL为空时显示 -->
      <div class="avatar-error-class">
        {{ (fallbackText || '?').charAt(0) }}
      </div>

      <template #error>
        <div class="avatar-error-class">
          {{ (fallbackText || '?').charAt(0) }}
        </div>
      </template>
    </el-avatar>

    <!-- Avatar upload input (hidden) -->
    <input
      v-if="showUpload"
      type="file"
      accept="image/*"
      ref="avatarInput"
      style="display: none"
      @click.stop
      @change="handleFileSelect"
    />

    <!-- Upload button if showUpload is true -->
    <el-button
      v-if="showUpload && showUploadButton"
      type="primary"
      size="small"
      class="upload-avatar-btn"
      @click.stop="$refs.avatarInput.click()"
    >
      📸 更换头像
    </el-button>
  </div>

  <!-- 头像放大对话框 -->
  <el-dialog
    v-model="showLargeAvatar"
    :title="enlargeTitle"
    width="300px"
    top="20%"
    @close="handleCloseDialog"
  >
    <div style="text-align: center; padding: 20px 0">
      <el-avatar :size="enlargeSize" class="user-avatar" :src="avatarUrl" :shape="shape">
        <div class="avatar-error-class">
          {{ (fallbackText || '?').charAt(0) }}
        </div>
      </el-avatar>
    </div>
    <template #footer>
      <span class="dialog-footer">
        <el-button type="primary" @click="handleCloseDialog">关闭</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'

// 加载状态
const isLoading = ref(false)
const isLoaded = ref(false)

// 组件属性定义
const props = defineProps({
  // Avatar URL
  avatarUrl: {
    type: String,
    default: ''
  },
  // Fallback text to show when image fails
  fallbackText: {
    type: String,
    default: ''
  },
  // Avatar size
  size: {
    type: [Number, String],
    default: 120
  },
  // Avatar shape: circle | square
  shape: {
    type: String,
    default: 'circle'
  },
  // Whether to show upload functionality
  showUpload: {
    type: Boolean,
    default: false
  },
  // Whether to show the upload button
  showUploadButton: {
    type: Boolean,
    default: true
  },
  // Whether to enable click to enlarge functionality
  clickToEnlarge: {
    type: Boolean,
    default: false
  },
  // Enlarged avatar size in dialog
  enlargeSize: {
    type: [Number, String],
    default: 200
  },
  // Dialog title when avatar is enlarged
  enlargeTitle: {
    type: String,
    default: '个人头像'
  }
})

// Emits
const emit = defineEmits(['upload', 'error', 'click', 'enlarge'])

// Handle file selection
const handleFileSelect = (event) => {
  const file = event.target.files[0]
  if (!file) return

  // Emit the file to the parent component
  emit('upload', file)

  // Clear the input value to allow selecting the same file again
  event.target.value = ''
}

// Ref for dialog visibility
const showLargeAvatar = ref(false)

// 监听头像URL变化，管理加载状态
watch(
  () => props.avatarUrl,
  (newUrl) => {
    if (newUrl) {
      isLoading.value = true
      // 创建临时图片对象来监听加载状态
      const img = new Image()

      img.onload = () => {
        isLoading.value = false
        isLoaded.value = true
      }

      img.onerror = () => {
        isLoading.value = false
        isLoaded.value = true
      }

      img.src = newUrl
    } else {
      isLoaded.value = false
    }
  },
  { immediate: true } // 立即执行一次
)

// Handle avatar click event
const handleAvatarClick = () => {
  emit('click')

  // Show dialog if clickToEnlarge is enabled
  if (props.clickToEnlarge) {
    showLargeAvatar.value = true
    emit('enlarge', true)
  }
}

// Handle dialog close
const handleCloseDialog = () => {
  showLargeAvatar.value = false
  emit('enlarge', false)
}
</script>

<style scoped>
.avatar-container {
  position: relative;
  display: inline-block; /* 确保容器只占内容宽度 */
  margin: 0; /* 清除默认外边距 */
  padding: 12px; /* 增加padding用于显示模糊效果 */
  /* 为容器添加圆角 */
  border-radius: 50%;
  /* 隐藏溢出内容 */
  overflow: visible;
}

/* 渐变背景层 - 实现羽化效果的核心 */
.avatar-container::after {
  content: '';
  position: absolute;
  /* 覆盖整个容器，包括padding区域 */
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: 50%;
  /* 渐变背景 */
  background: linear-gradient(
    135deg,
    rgba(255, 221, 187, 0.7) 0%,
    rgba(187, 224, 255, 0.5) 50%,
    rgba(204, 204, 255, 0.3) 100%
  );
  /* 模糊效果 */
  filter: blur(21px);
  /* 确保在头像后面 */
  z-index: 0;
}

/* 调整头像容器的z-index，确保头像在渐变层上面 */
.avatar-container > * {
  position: relative;
  z-index: 1;
}

.user-avatar {
  background-color: #fff;
  transition:
    transform 0.27s linear,
    box-shadow 0.27s linear;
  box-sizing: border-box; /* 确保边框不影响尺寸 */
  border-radius: 50%; /* 确保是圆形 */
  /* 添加一个白色的细边框，让头像和渐变背景有区分 */
  border: 3px solid rgba(255, 255, 255, 0.95);
  /* 移除多重发光阴影，避免与外层模糊背景冲突 */
  box-shadow: none;
}

.avatar-error-class {
  background: linear-gradient(135deg, #ff6b6b 0%, #ffa500 100%);
  box-shadow: none;
  /* 渐变文字效果 */
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  /* 动态字体大小 */
  font-size: 48px;
  /* 确保文字居中 */
  display: flex;
  align-items: center;
  justify-content: center;
}

.user-avatar:hover {
  transform: scale(1.05);
}

.upload-avatar-btn {
  margin-top: 10px;
}
</style>
