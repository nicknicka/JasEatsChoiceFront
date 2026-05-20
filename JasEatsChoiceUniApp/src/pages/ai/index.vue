<template>
	<view class="ai-page">
		<!-- 统一顶部导航栏 -->
		<view class="unified-nav">
			<view class="nav-tabs">
				<view
					class="tab-item"
					v-for="tab in tabs"
					:key="tab.key"
					:class="{ active: activeTab === tab.key }"
					@click="switchTab(tab.key)"
				>
					<text class="tab-icon">{{ tab.icon }}</text>
					<text class="tab-label">{{ tab.label }}</text>
				</view>
			</view>
		</view>

		<!-- 标签页内容 -->
		<view class="tabs-content">
			<!-- AI聊天 -->
			<view v-if="activeTab === 'chat'" class="tab-pane chat-pane">
				<ChatWelcomeGuide
					v-if="showWelcomeGuide"
					@start="handleStartChat"
					@showQuestions="showQuestionsDrawer = true"
				/>

				<view v-else class="chat-container">
					<ChatMessageList
						:messages="displayMessages"
						:scrollIntoView="scrollIntoView"
					/>

					<view v-if="isLoading" class="loading-status">
						<ChatLoadingIndicator :state="loadingState" />
					</view>

					<ChatInputArea
						:inputText="inputText"
						:isStreaming="isStreaming"
						:uploadedImages="uploadedImages"
						:showEmojiPicker="showEmojiPicker"
						:loadingText="loadingText"
						@update:inputText="inputText = $event"
						@send="sendMessage"
						@stop="stopStreamingHandler"
						@toggleEmoji="toggleEmoji"
						@selectEmoji="selectEmoji"
						@chooseImage="handleChooseImage"
						@removeImage="removeUploadedImage"
						@toggleQuickQuestions="showQuestionsDrawer = true"
						@clearHistory="confirmClearHistory"
					/>
				</view>

				<QuickQuestionsDrawer
					:visible="showQuestionsDrawer"
					:questions="quickQuestions"
					@select="handleQuickQuestionSelect"
					@update:visible="showQuestionsDrawer = $event"
				/>
			</view>

			<!-- 菜品识别 -->
			<view v-if="activeTab === 'recognition'" class="tab-pane">
				<DishRecognition />
			</view>

			<!-- 食谱优化 -->
			<view v-if="activeTab === 'recipe'" class="tab-pane">
				<RecipeOptimization />
			</view>

			<!-- 内容提取 -->
			<view v-if="activeTab === 'extraction'" class="tab-pane">
				<ContentExtraction />
			</view>
		</view>
	</view>
</template>
<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/store'
import { useChatMessages } from '@/composables/ai'
import { useChatInput } from '@/composables/ai'
import { useChatStreaming } from '@/composables/ai'
import { useErrorHandler } from '@/composables'
import { createPageDebug } from '@/utils/page-debug'

// 组件导入
import ChatMessageList from './components/ChatMessageList.vue'
import ChatWelcomeGuide from './components/ChatWelcomeGuide.vue'
import ChatLoadingIndicator from '@/components/ChatLoadingIndicator.vue'
import QuickQuestionsDrawer from './components/QuickQuestionsDrawer.vue'
import ChatInputArea from './components/ChatInputArea.vue'
import DishRecognition from './components/DishRecognition.vue'
import RecipeOptimization from './components/RecipeOptimization.vue'
import ContentExtraction from './components/ContentExtraction.vue'

// 用户信息store
const userStore = useUserStore()
const pageDebug = createPageDebug('AI助手')

// 错误处理
const { handleError, confirm } = useErrorHandler()

// ==================== 标签页 ====================
const activeTab = ref('chat')
const tabs = ref([
	{ key: 'chat', label: 'AI聊天', icon: '💬' },
	{ key: 'recognition', label: '菜品识别', icon: '📷' },
	{ key: 'recipe', label: '食谱优化', icon: '🍳' },
	{ key: 'extraction', label: '内容提取', icon: '📝' }
])

// ==================== 使用 Composables ====================
const {
	messages,
	scrollIntoView,
	hasLoadedHistory,
	isEmpty,
	loadHistory,
	addMessage,
	appendContent,
	updateMessage,
	saveMessageToBackend,
	clearHistory
} = useChatMessages()

const {
	inputText,
	uploadedImages,
	showEmojiPicker,
	quickQuestions,
	quickQuestionsExpanded,
	canSend,
	selectEmoji,
	toggleEmoji,
	toggleQuickQuestions,
	chooseImage,
	removeImage,
	getInputData,
	resetInput
} = useChatInput()

const {
	streamingState,
	isStreaming,
	isTyping,
	isLoading,
	loadingText,
	startStreaming,
	stopStreaming,
	parseCardData
} = useChatStreaming()

// ==================== 计算属性 ====================
const displayMessages = computed(() => messages.value)

const loadingState = computed(() => streamingState.value.toLowerCase())
const WELCOME_MESSAGE = '您好！我是AI饮食助手，有什么可以帮您的吗？'
const hasUserContext = computed(() => {
	return Boolean(uni.getStorageSync('userId') || userStore.userInfo?.userId)
})

const showWelcomeGuide = computed(() => {
	if (isLoading.value) {
		return false
	}

	if (hasUserContext.value && !hasLoadedHistory.value && messages.value.length === 0) {
		return false
	}

	if (messages.value.length === 0) {
		return true
	}

	return (
		messages.value.length === 1 &&
		messages.value[0].sender === 'ai' &&
		messages.value[0].content === WELCOME_MESSAGE
	)
})

const showQuestionsDrawer = ref(false)

// ==================== 方法 ====================
const switchTab = (tabKey) => {
	pageDebug.action('切换AI标签页', {
		from: activeTab.value,
		to: tabKey
	})
	activeTab.value = tabKey
	showQuestionsDrawer.value = false
	showEmojiPicker.value = false
	if (tabKey === 'chat') {
		setTimeout(() => {
			scrollToBottom()
		}, 300)
	}
}

const scrollToBottom = async () => {
	await new Promise((resolve) => setTimeout(resolve, 100))
}

const sendMessage = async () => {
	const { text } = getInputData()
	if (!text) return
	pageDebug.action('发送AI消息', {
		textLength: text.length,
		hasImage: uploadedImages.value.length > 0
	})

	addMessage({
		sender: 'user',
		content: text,
		avatar: '👤',
		isUser: true
	})

	saveMessageToBackend('user', text)

	if (quickQuestionsExpanded.value) {
		quickQuestionsExpanded.value = false
	}
	showQuestionsDrawer.value = false

	resetInput()
	await scrollToBottom()

	const aiMessageIndex = messages.value.length
	addMessage({
		sender: 'ai',
		content: '',
		avatar: '🤖',
		isUser: false
	})

	await scrollToBottom()

	await startStreaming(
		text,
		messages.value.slice(0, -1).map((msg) => ({
			role: msg.isUser ? 'user' : 'assistant',
			content: msg.content
		})),
		(content) => {
			appendContent(aiMessageIndex, content)
		},
		async () => {
			const aiContent = messages.value[aiMessageIndex].content
			const { content: cleanContent, cardData, messageType } = parseCardData(aiContent)
			updateMessage(aiMessageIndex, {
				content: cleanContent,
				messageType,
				cardData
			})
			await saveMessageToBackend('ai', cleanContent, messageType, cardData)
			pageDebug.requestSuccess('AI回复完成', {
				messageType,
				contentLength: cleanContent.length
			})
		},
		(error) => {
			pageDebug.requestFail('AI回复失败', error)
			handleError(error, 'sendMessage')
			if (!messages.value[aiMessageIndex].content) {
				updateMessage(aiMessageIndex, {
					content: '抱歉，我现在无法回答这个问题，请稍后再试。'
				})
			}
		}
	)
}

const stopStreamingHandler = () => {
	pageDebug.action('停止AI回复')
	stopStreaming()
	uni.showToast({
		title: '已停止AI回复',
		icon: 'none'
	})
}

const handleChooseImage = async () => {
	try {
		pageDebug.action('选择聊天图片')
		await chooseImage(3)
		pageDebug.requestSuccess('选择聊天图片', {
			count: uploadedImages.value.length
		})
	} catch (error) {
		pageDebug.requestFail('选择聊天图片', error)
		handleError(error, 'chooseImage')
	}
}

const removeUploadedImage = (index) => {
	pageDebug.action('移除聊天图片', {
		index
	})
	removeImage(index)
}

const handleQuickQuestionSelect = (question) => {
	pageDebug.action('选择快捷提问', {
		question
	})
	inputText.value = question
	sendMessage()
}

const handleStartChat = (question = '') => {
	pageDebug.action('开始AI聊天', {
		hasPresetQuestion: Boolean(question)
	})
	if (question) {
		inputText.value = question
		sendMessage()
		return
	}
	showQuestionsDrawer.value = true
}

const confirmClearHistory = async () => {
	pageDebug.action('尝试清空聊天记录')
	const confirmed = await confirm('确定要清空所有聊天记录吗？')
	if (confirmed) {
		try {
			const userId = uni.getStorageSync('userId') || userStore.userInfo?.userId
			const success = await clearHistory(userId)
			if (success) {
				pageDebug.requestSuccess('清空聊天记录')
				uni.showToast({
					title: '已清空聊天记录',
					icon: 'success'
				})
			}
		} catch (error) {
			pageDebug.requestFail('清空聊天记录', error)
			handleError(error, 'clearHistory')
		}
	}
}

// ==================== 生命周期 ====================
onMounted(async () => {
	pageDebug.lifecycle('页面挂载', {
		hasUserContext: hasUserContext.value,
		activeTab: activeTab.value
	})
	const userId = uni.getStorageSync('userId') || userStore.userInfo?.userId
	if (userId) {
		pageDebug.requestStart('加载聊天历史', {
			userId
		})
		await loadHistory(userId)
		pageDebug.requestSuccess('加载聊天历史', {
			messageCount: messages.value.length
		})
	} else {
		pageDebug.anomaly('缺少用户信息，跳过加载聊天历史')
	}
})
</script>
<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.ai-page {
	height: 100%;
	width: 100%;
	background: $bg-color-white;
	display: flex;
	flex-direction: column;
	overflow: hidden;
}

/* 统一顶部导航栏 */
.unified-nav {
	height: $nav-height;
	background: $bg-color-white;
	border-bottom: 1rpx solid $border-color-light;
	box-shadow: $box-shadow-sm;
	@include flex-between;
	padding: 0 $spacing-md;
	flex-shrink: 0;
	position: relative;
	z-index: $z-index-sticky;
}

.nav-tabs {
	flex: 1;
	@include flex-center;
	gap: $spacing-md;
}

.tab-item {
	@include flex-center-column;
	gap: $spacing-xs;
	padding: $spacing-sm;
	transition: $transition-base;
	position: relative;

	&.active {
		.tab-label {
			color: $primary-500;
			font-weight: $font-weight-bold;
		}

		&::after {
			content: '';
			position: absolute;
			bottom: 0;
			left: 50%;
			transform: translateX(-50%);
			width: 32rpx;
			height: 4rpx;
			background: linear-gradient(135deg, $primary-500, $primary-800);
			border-radius: 2rpx;
		}
	}
}

.tab-icon {
	font-size: 40rpx;
	display: block;
}

.tab-label {
	font-size: 26rpx;
	color: $text-color-regular;
	transition: $transition-base;
}

/* 标签页内容 */
.tabs-content {
	flex: 1;
	height: 0;
	display: flex;
	flex-direction: column;
	overflow: hidden;
}

.tab-pane {
	flex: 1;
	height: 100%;
	overflow: hidden;
	display: flex;
	flex-direction: column;
}

/* 聊天容器 */
.chat-pane {
	display: flex;
	flex-direction: column;
	height: 100%;
	overflow: hidden;
	position: relative;
}

.chat-container {
	display: flex;
	flex: 1;
	flex-direction: column;
	min-height: 0;
	overflow: hidden;
}

/* 加载状态 */
.loading-status {
	@include flex-center;
	padding: $spacing-lg;
	background: $bg-color-light;
}
</style>
