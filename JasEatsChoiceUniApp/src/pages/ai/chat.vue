<template>
  <view class="ai-chat-container">
    <view class="chat-header">
      <text class="header-title">AI聊天助手</text>
    </view>

    <view class="chat-messages" ref="chatScroll">
      <view class="message" v-for="msg in messages" :key="msg.id">
        <view class="message-avatar" :class="{ user: msg.isUser }">
          {{ msg.isUser ? '我' : 'AI' }}
        </view>
        <view class="message-content" :class="{ user: msg.isUser }">
          {{ msg.content }}
        </view>
      </view>
    </view>

    <view class="chat-input-area">
      <input
        type="text"
        v-model="inputText"
        placeholder="输入您的问题..."
        class="chat-input"
        @keyup.enter="sendMessage"
      />
      <button class="send-btn" @click="sendMessage">发送</button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'

const messages = ref([
  { id: 1, content: '你好！我是AI助手，有什么可以帮您的吗？', isUser: false }
])
const inputText = ref('')
const chatScroll = ref(null)

const sendMessage = () => {
  if (!inputText.value.trim()) return

  const userMsg = {
    id: Date.now(),
    content: inputText.value.trim(),
    isUser: true
  }
  messages.value.push(userMsg)
  inputText.value = ''

  // 模拟AI回复
  setTimeout(() => {
    const aiMsg = {
      id: Date.now() + 1,
      content: `您问的是：${userMsg.content}，我正在处理您的请求...`,
      isUser: false
    }
    messages.value.push(aiMsg)
    scrollToBottom()
  }, 1000)

  scrollToBottom()
}

const scrollToBottom = () => {
  uni.nextTick(() => {
    if (chatScroll.value) {
      uni.createSelectorQuery().select('.chat-messages').boundingClientRect((rect) => {
        uni.pageScrollTo({
          scrollTop: rect.height,
          duration: 300
        })
      }).exec()
    }
  })
}

onMounted(() => {
  scrollToBottom()
})

watch(messages, () => {
  scrollToBottom()
}, { deep: true })
</script>

<style scoped>
.ai-chat-container {
  background-color: #f5f5f5;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.chat-header {
  background-color: #FF6B35;
  color: #fff;
  padding: 20px 15px;
  font-size: 18px;
  font-weight: bold;
}

.chat-messages {
  flex: 1;
  padding: 15px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.message {
  display: flex;
  gap: 10px;
  align-items: flex-start;
}

.message.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 14px;
  font-weight: bold;
  flex-shrink: 0;
}

.message-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 14px;
  font-weight: bold;
  flex-shrink: 0;
}

.message-avatar:not(.user) {
  background-color: #FFE0B2;
  color: #FF6B35;
}

.message-avatar.user {
  background-color: #FF6B35;
  color: #fff;
  order: 1;
}

.message-content {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 18px;
  font-size: 14px;
  line-height: 1.4;
}

.message-content:not(.user) {
  background-color: #fff;
  color: #333;
  border-bottom-left-radius: 6px;
}

.message-content.user {
  background-color: #FF6B35;
  color: #fff;
  border-bottom-right-radius: 6px;
  order: 0;
}

.chat-input-area {
  background-color: #fff;
  padding: 15px;
  display: flex;
  gap: 10px;
  align-items: center;
  box-shadow: 0 -2px 8px rgba(0,0,0,0.05);
}

.chat-input {
  flex: 1;
  height: 44px;
  padding: 0 15px;
  border: 1px solid #eee;
  border-radius: 22px;
  font-size: 14px;
}

.send-btn {
  width: 80px;
  height: 44px;
  background-color: #FF6B35;
  border: none;
  border-radius: 22px;
  color: #fff;
  font-size: 14px;
  cursor: pointer;
}
</style>