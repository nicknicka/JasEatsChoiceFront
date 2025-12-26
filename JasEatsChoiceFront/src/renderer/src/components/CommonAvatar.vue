<template>
	<div class="avatar-container">
		<el-avatar :size="size" class="user-avatar" :src="avatarUrl" :shape="shape">
			<template #error>
				<div class="avatar-error-class">
					{{ (fallbackText || "?").charAt(0) }}
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
			@change="handleFileSelect"
		/>

		<!-- Upload button if showUpload is true -->
		<el-button
			v-if="showUpload && showUploadButton"
			type="primary"
			size="small"
			class="upload-avatar-btn"
			@click="$refs.avatarInput.click()"
		>
			📸 更换头像
		</el-button>
	</div>
</template>

<script setup>
import { ref, computed } from "vue";

// Props
const props = defineProps({
	// Avatar URL
	avatarUrl: {
		type: String,
		default: "",
	},
	// Fallback text to show when image fails
	fallbackText: {
		type: String,
		default: "",
	},
	// Avatar size
	size: {
		type: [Number, String],
		default: 120,
	},
	// Avatar shape: circle | square
	shape: {
		type: String,
		default: "circle",
	},
	// Whether to show upload functionality
	showUpload: {
		type: Boolean,
		default: false,
	},
	// Whether to show the upload button
	showUploadButton: {
		type: Boolean,
		default: true,
	},
});

// Emits
const emit = defineEmits(["upload", "error"]);

// Handle file selection
const handleFileSelect = (event) => {
	const file = event.target.files[0];
	if (!file) return;

	// Emit the file to the parent component
	emit("upload", file);

	// Clear the input value to allow selecting the same file again
	event.target.value = "";
};
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
	content: "";
	position: absolute;
	/* 覆盖整个容器，包括padding区域 */
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	border-radius: 50%;
	/* 渐变背景 */
	background: linear-gradient(135deg, rgba(255, 221, 187, 0.7) 0%, rgba(187, 224, 255, 0.5) 50%, rgba(204, 204, 255, 0.3) 100%);
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
	transition: transform 0.27s linear;
	box-sizing: border-box; /* 确保边框不影响尺寸 */
	border-radius: 50%; /* 确保是圆形 */
	/* 添加一个白色的细边框，让头像和渐变背景有区分 */
	border: 1px solid rgba(255, 255, 255, 0.9);
}


.avatar-error-class {
	background: linear-gradient(135deg, #ff6b6b 0%, #ffa500 100%);
	box-shadow: 0 4px 12px rgba(255, 107, 107, 0.3);
	color: #fff;
	font-size: 48px;
	background-clip: text;
}

.user-avatar:hover {
	transform: scale(1.05);
}

.upload-avatar-btn {
	margin-top: 10px;
}
</style>
