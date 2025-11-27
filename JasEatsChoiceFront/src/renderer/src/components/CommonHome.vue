<script setup>
import { useRouter } from "vue-router";
import { ref, onMounted, computed, watch } from "vue";
import {
	Search,
	Menu,
	Shop,
	Calendar,
	DataAnalysis,
	Document,
	List,
	Message,
	ChatDotRound,
	Setting,
	HomeFilled,
} from "@element-plus/icons-vue";

const router = useRouter();

// 导航到指定路径
const navigateTo = (path) => {
	router.push(path);
};

// 模拟用户信息
const userInfo = ref({ name: "佳食用户", avatar: "👤" });

// 用户角色
const userRole = ref("user"); // 'user' 或 'merchant'

// 预定义菜单数据
const menuData = {
	// 用户端菜单
	user: [
		{ index: "1", name: "用户首页", icon: HomeFilled, path: "/user/home" },
		{ index: "2", name: "我的推荐", icon: Menu, path: "/user/home/recommend" },
		{ index: "3", name: "商家查找", icon: Shop, path: "/user/home/merchants" },
		{ index: "4", name: "今日食谱", icon: Calendar, path: "/user/home/today-recipe" },
		{
			index: "5",
			name: "卡路里统计",
			icon: DataAnalysis,
			path: "/user/home/calorie",
		},
		{ index: "6", name: "我的食谱", icon: Document, path: "/user/home/my-recipe" },
		{ index: "7", name: "查看订单", icon: List, path: "/user/home/orders" },
		{
			index: "8",
			name: "消息中心",
			icon: Message,
			path: "/user/home/message-center",
		},
		{ index: "9", name: "AI饮食助手", icon: ChatDotRound, path: "/user/home/ai" },
		{ index: "10", name: "用户聊天", icon: ChatDotRound, path: "/user/home/chat" },
		{
			index: "11",
			name: "设置",
			icon: Setting,
			path: "/user/home/settings",
			isSetting: true,
		},
	],
	// 商家端菜单
	merchant: [
		{ index: "1", name: "商家首页", icon: HomeFilled, path: "/merchant/home" },
		{ index: "2", name: "今日订单", icon: List, path: "/merchant/home/today-orders" }, // 修改为正确的路由路径
		{ index: "3", name: "菜单管理", icon: Shop, path: "/merchant/home/menu" }, // 修改为正确的路由路径
		{
			index: "4",
			name: "菜品管理",
			icon: Document,
			path: "/merchant/home/dish-management",
		},
		{ index: "5", name: "我的店铺", icon: Shop, path: "/merchant/home/my-shop" },
		{ index: "6", name: "商家聊天", icon: ChatDotRound, path: "/merchant/home/chat" },
		{
			index: "7",
			name: "评价中心",
			icon: DataAnalysis,
			path: "/merchant/home/comments",
		}, // 添加评价中心菜单
		{
			index: "8",
			name: "经营统计",
			icon: DataAnalysis,
			path: "/merchant/home/statistics",
		},
		{ index: "9", name: "消息管理", icon: Message, path: "/merchant/home/messages" }, // 修改为正确的路由路径
	],
};

// 根据当前角色过滤菜单
const currentMenu = computed(() => {
	return menuData[userRole.value] ? menuData[userRole.value] : menuData.user || [];
});

// 菜单点击事件处理
const handleMenuSelect = (index) => {
	const menuItem = currentMenu.value.find((item) => item.index === index);
	if (menuItem) {
		navigateTo(menuItem.path);
	}
};

// 头像放大弹窗
const showLargeAvatar = ref(false);

// 头像点击事件处理
const handleAvatarClick = () => {
	// 无论用户端还是商家端，点击头像都放大显示
	showLargeAvatar.value = true;
};

// 角色切换功能
const toggleRole = () => {
	try {
		// 切换角色
		userRole.value = userRole.value === "user" ? "merchant" : "user";

		// 更新用户信息和跳转
		if (userRole.value === "user") {
			userInfo.value = { name: "用户端", avatar: "👤" };
			navigateTo("/user/home");
		} else {
			userInfo.value = { name: "商户端", avatar: "🏪" };
			navigateTo("/merchant/home");
		}

		// 保存当前角色到localStorage
		localStorage.setItem("currentRole", userRole.value);

		console.log("角色切换成功:", userRole.value);
	} catch (error) {
		console.error("角色切换失败:", error);
	}
};

// 页面加载时从localStorage或当前路由恢复角色
onMounted(() => {
	try {
		// 1. First check current route to determine role
		let detectedRole = "user"; // Default to user

		if (router.currentRoute.value?.path?.startsWith("/merchant/")) {
			detectedRole = "merchant";
		}

		// 2. Then check localStorage
		const savedRole = localStorage.getItem("currentRole");

		// 3. Use detected role from route if route is for merchant, otherwise use saved or default
		if (savedRole && (detectedRole === "user" || router.currentRoute.path === "/")) {
			userRole.value = savedRole;
		} else {
			userRole.value = detectedRole;
		}

		// Update user info
		if (userRole.value === "merchant") {
			userInfo.value = { name: "商户端", avatar: "🏪" };
		} else if (userRole.value === "user") {
			userInfo.value = { name: "用户端", avatar: "👤" };
		}

		// Save the final role to localStorage
		localStorage.setItem("currentRole", userRole.value);

		console.log("恢复角色成功:", userRole.value);
	} catch (error) {
		console.error("恢复角色失败:", error);
	}
});

// Watch for route changes to update role automatically
watch(
	() => router.currentRoute.value?.path,
	(newPath) => {
		let newRole = "user"; // Default to user

		if (newPath?.startsWith("/merchant/")) {
			newRole = "merchant";
		}

		// Only update if role changed
		if (userRole.value !== newRole) {
			userRole.value = newRole;

			// Update user info
			if (userRole.value === "merchant") {
				userInfo.value = { name: "商户端", avatar: "🏪" };
			} else if (userRole.value === "user") {
				userInfo.value = { name: "用户端", avatar: "👤" };
			}

			// Save the new role to localStorage
			localStorage.setItem("currentRole", userRole.value);
			console.log("路由变化自动更新角色:", userRole.value);
		}
	}
);

const searchQuery = ref("");

const handleSearch = (value) => {
	// 实现搜索逻辑
	try {
		// 如果搜索内容为空，不执行搜索
		if (!value || value.trim() === "") {
			return;
		}

		console.log("开始搜索:", value);

		// 根据当前角色跳转到对应的搜索页面
		if (userRole.value === "user") {
			// 用户角色，跳转到商家列表页面并携带搜索参数
			navigateTo({
				path: "/user/home/merchants",
				query: { search: value.trim() },
			});
		} else {
			// 商家角色，跳转到商家订单页面或其他适合商家的搜索页面
			navigateTo("/merchant/home/orders");
			console.log("商家角色搜索功能待实现:", value);
		}
	} catch (error) {
		console.error("搜索失败:", error);
	}
};
</script>

<template>
	<div class="app-container">
		<!-- 顶部导航栏 -->
		<el-header class="top-nav-bar">
			<div class="logo" @click="navigateTo('/user/home')">🎨 佳食宜选</div>
			<el-input
				v-model="searchQuery"
				placeholder="🔍 搜索框(支持菜品/商家搜索)"
				clearable
				class="search-input"
				@input="handleSearch"
				@keyup.enter="handleSearch(searchQuery)"
			>
				<template #append>
					<el-button type="primary" @click="handleSearch(searchQuery)">
						<el-icon><Search /></el-icon>
						搜索
					</el-button>
				</template>
			</el-input>
			<div class="user-info">
				<el-button type="text" class="identity-switch" @click="toggleRole">
					<span
						:class="['user-icon', userRole === 'user' ? 'icon-enlarged' : '']"
						>👤</span
					>
					<span
						:class="[
							'merchant-icon',
							userRole === 'merchant' ? 'icon-enlarged' : '',
						]"
						>🏪</span
					>
				</el-button>
			</div>
		</el-header>

		<div class="main-content">
			<!-- 左侧菜单栏 -->
			<el-aside width="168px" class="sidebar-menu">
				<div class="avatar-section" @click="handleAvatarClick">
					<el-avatar :size="80" class="user-avatar" style="cursor: pointer">{{
						userRole === "merchant" ? "🏪" : "👤"
					}}</el-avatar>
					<div class="username">{{ userInfo.name }}</div>
				</div>

				<el-menu default-active="1" class="menu-list" @select="handleMenuSelect">
					<el-menu-item
						v-for="menuItem in currentMenu"
						:key="menuItem.index"
						:index="menuItem.index"
						:class="{ 'setting-menu': menuItem.isSetting }"
					>
						<el-icon>
							<component :is="menuItem.icon" />
						</el-icon>
						<template #title>{{ menuItem.name }}</template>
					</el-menu-item>
				</el-menu>
			</el-aside>

			<!-- 右侧内容区域，使用router-view实现子组件内容访问 -->
			<el-main class="content-area">
				<router-view />
			</el-main>
		</div>

		<!-- 头像放大对话框 -->
		<el-dialog v-model="showLargeAvatar" title="个人头像" width="300px" top="20%">
			<div style="text-align: center; padding: 20px 0">
				<el-avatar :size="200" class="user-avatar">
					{{ userRole === "merchant" ? "🏪" : "👤" }}
				</el-avatar>
			</div>
			<template #footer>
				<span class="dialog-footer">
					<el-button type="primary" @click="showLargeAvatar = false"
						>关闭</el-button
					>
				</span>
			</template>
		</el-dialog>
	</div>
</template>

<style scoped lang="less">
.app-container {
	height: 100vh;
	width: 100%;
}

.top-nav-bar {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 0 20px;
	height: 60px;
	background-color: #fff;
	box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.logo {
	font-size: 24px;
	font-weight: bold;
	color: #ff6b6b;
	cursor: pointer;
}

.search-input {
	width: 400px;
	margin: 0 20px;
}

.user-info {
	display: flex;
	align-items: center;
	gap: 10px;

	.identity-switch {
		font-size: 18px;
		padding: 0;

		.user-icon,
		.merchant-icon {
			transition: font-size 0.3s ease;
		}

		.icon-enlarged {
			font-size: 24px;
			font-weight: bold;
		}
	}
}

.main-content {
	display: flex;
	height: calc(100vh - 60px);
}

.sidebar-menu {
	background-color: #fff;
	border-right: 1px solid #eee;

	.avatar-section {
		text-align: center;
		padding: 20px 0;
		border-bottom: 1px solid #eee;

		.user-avatar {
			background-color: #ff6b6b;
		}

		.username {
			margin-top: 8px;
			font-size: 14px;
			font-weight: 500;
			color: #333;
		}
	}

	.menu-list {
		border: none;
		height: calc(100% - 120px);
	}

	.setting-menu {
		border-top: 1px solid #eee;
		margin-top: 20px;
	}
}

.content-area {
	padding: 20px;
	background-color: #f5f5f5;
	overflow-y: auto;
}
</style>
