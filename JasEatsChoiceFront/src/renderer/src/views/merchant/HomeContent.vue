<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import api from "../../utils/api.js";

const router = useRouter();

// 页面跳转
const navigateToOrders = () => {
	router.push("/merchant/home/orders");
};

// 查看订单详情
const viewOrderDetails = (order) => {
	// 跳转到订单详情页面
	router.push(`/merchant/home/orders/details?orderId=${order.orderId}`);
};

// 更新订单状态
const updateOrderStatus = (order) => {
	// 模拟状态更新逻辑
	const statusFlow = {
		待处理: "制作中",
		制作中: "待配送",
		待配送: "已完成",
		已完成: "已完成",
	};

	const nextStatus = statusFlow[order.status] || "已完成";
	order.status = nextStatus;

	// 发送WebSocket通知或API请求

	ElMessage.success(`订单 ${order.orderId} 状态已更新为 ${nextStatus}`);
};

// 通知用户
const notifyUser = (order) => {
	// 模拟通知用户逻辑
	// 这里可以通过WebSocket或推送服务发送通知

	ElMessage.success(`已通知用户订单 ${order.orderId} 的最新状态`);
};

// 概览项导航
const navigateToStatistics = () => {
	router.push("/merchant/home/statistics");
};

const navigateToComments = () => {
	router.push("/merchant/home/comments");
};

const navigateToMessages = () => {
	router.push("/merchant/home/messages");
};

// 筛选功能
const activeFilter = ref("today");

// 菜单筛选功能
const activeMenuFilter = ref("all");

// 所有订单数据
const allOrders = ref([]);

// 筛选后的订单
const filteredOrders = ref([]);

// 订单状态映射
const orderStatusMap = {
	1: "待处理",
	2: "备菜中",
	3: "烹饪中",
	4: "待配送",
	5: "已完成",
	6: "已取消",
};

// 筛选订单
const filterOrders = (filterType) => {
	activeFilter.value = filterType;

	// 简单的筛选逻辑，根据实际时间处理
	const now = new Date();
	const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
	const weekStart = new Date(
		now.getFullYear(),
		now.getMonth(),
		now.getDate() - now.getDay()
	);
	const monthStart = new Date(now.getFullYear(), now.getMonth(), 1);

	filteredOrders.value = allOrders.value.filter((order) => {
		const orderDate = new Date(order.createTime);

		// 时间范围过滤
		let timeMatch = true;
		switch (filterType) {
			case "today":
				timeMatch = orderDate >= today;
				break;
			case "week":
				timeMatch = orderDate >= weekStart;
				break;
			case "month":
				timeMatch = orderDate >= monthStart;
				break;
		}

		return timeMatch;
	});
};

const navigateToMenu = () => {
	router.push("/merchant/home/menu");
};

// 快捷操作函数 - 设置优惠
const setDiscount = () => {
	ElMessage.info("设置优惠功能已触发");
	// 可以在此处添加具体的实现逻辑
};

// 快捷操作函数 - 调整营业时间
const adjustBusinessHours = () => {
	ElMessage.info("调整营业时间功能已触发");
	// 可以在此处添加具体的实现逻辑
};

// 快捷操作函数 - 联系客服
const contactCustomerService = () => {
	ElMessage.info("联系客服功能已触发");
	// 可以在此处添加具体的实现逻辑
};

// 菜单状态映射
const menuStatusMap = {
	online: { text: "上架中", icon: "🟢", type: "success" },
	draft: { text: "草稿", icon: "🟡", type: "warning" },
	offline: { text: "下架中", icon: "🔴", type: "danger" },
};

// 菜品状态映射
const dishStatusMap = {
	online: { text: "🟢 在售", type: "success" },
	almost_sold: { text: "🟡 即将售罄", type: "warning" },
	offline: { text: "🔴 下架", type: "danger" },
};

// 模拟菜品数据，关联到各个菜单
const dishData = {
	早餐菜单: [
		{
			id: 1,
			name: "豆浆",
			price: 3,
			category: "饮品",
			status: "online",
			stock: 100,
			updateTime: "2024-11-21 06:00",
		},
		{
			id: 2,
			name: "油条",
			price: 2,
			category: "主食",
			status: "online",
			stock: 80,
			updateTime: "2024-11-21 06:30",
		},
		{
			id: 3,
			name: "包子",
			price: 1.5,
			category: "主食",
			status: "online",
			stock: 120,
			updateTime: "2024-11-21 06:15",
		},
	],
	午餐菜单: [
		{
			id: 4,
			name: "鱼香肉丝",
			price: 18,
			category: "热菜",
			status: "online",
			stock: 50,
			updateTime: "2024-11-21 10:30",
		},
		{
			id: 5,
			name: "宫保鸡丁",
			price: 16,
			category: "热菜",
			status: "online",
			stock: 40,
			updateTime: "2024-11-21 10:45",
		},
		{
			id: 6,
			name: "西红柿鸡蛋",
			price: 12,
			category: "热菜",
			status: "online",
			stock: 60,
			updateTime: "2024-11-21 10:20",
		},
	],
	晚餐菜单: [
		{
			id: 7,
			name: "红烧肉",
			price: 22,
			category: "热菜",
			status: "online",
			stock: 30,
			updateTime: "2024-11-21 16:30",
		},
		{
			id: 8,
			name: "清蒸鱼",
			price: 28,
			category: "海鲜",
			status: "online",
			stock: 20,
			updateTime: "2024-11-21 16:45",
		},
	],
	下午茶菜单: [
		{
			id: 9,
			name: "奶茶",
			price: 15,
			category: "饮品",
			status: "online",
			stock: 70,
			updateTime: "2024-11-21 14:00",
		},
		{
			id: 10,
			name: "蛋糕",
			price: 25,
			category: "甜点",
			status: "online",
			stock: 40,
			updateTime: "2024-11-21 14:30",
		},
	],
	今日特色菜单: [
		{
			id: 4,
			name: "鱼香肉丝",
			price: 18,
			category: "热菜",
			status: "online",
			stock: 50,
			updateTime: "2024-11-21 10:30",
		},
		{
			id: 9,
			name: "奶茶",
			price: 15,
			category: "饮品",
			status: "online",
			stock: 70,
			updateTime: "2024-11-21 14:00",
		},
	],
};

// 假设商家ID为1，可以根据实际情况从登录信息或路由参数中获取
const merchantId = 1;

// 今日菜单数据
const todayMenus = ref([]);

// 从后端获取今日菜单数据
const fetchTodayMenus = () => {
	api.get(`/api/v1/merchants/${merchantId}/menu`)
		.then(response => {
			if (response.code === '200' && response.data) {
				// 假设后端返回的菜单数据结构与我们需要的基本一致
				// 如果需要转换数据格式，可以在这里处理
				todayMenus.value = response.data.map(menu => ({
					...menu,
					status: menu.status === 'active' ? 'online' : 'offline',
					// 暂时设置dishes为0，后面需要实现获取菜品数量的接口
					dishes: 0,
					// 格式转换：LocalDateTime to String
					updateTime: menu.updateTime ? menu.updateTime.replace('T', ' ') : '',
					autoOnline: menu.autoStartTime ? menu.autoStartTime.replace('T', ' ') : '',
					autoOffline: menu.autoEndTime ? menu.autoEndTime.replace('T', ' ') : ''
				}));
				// 初始化筛选后的菜单
				filteredMenus.value = [...todayMenus.value];
			}
		})
		.catch(error => {
			console.error('获取今日菜单数据失败:', error);
		});
};

// 当前选中的菜单
const selectedMenu = ref(null);
// 当前菜单的菜品
const currentMenuDishes = ref([]);

// 筛选后的菜单
const filteredMenus = ref([...todayMenus.value]);

// 菜单类型筛选
const activeMenuTypeFilter = ref("all");

// 切换菜单
const switchMenu = (menu) => {
	selectedMenu.value = menu;
	currentMenuDishes.value = dishData[menu.name] || [];
};

// 筛选菜单：先按类型，再按状态
const filterMenus = (filterType, filterCategory = "status") => {
	if (filterCategory === "status") {
		activeMenuFilter.value = filterType;
	} else if (filterCategory === "type") {
		activeMenuTypeFilter.value = filterType;
	}

	// 组合筛选
	filteredMenus.value = todayMenus.value.filter((menu) => {
		// 状态筛选
		const statusMatch =
			activeMenuFilter.value === "all"
				? true
				: menu.status === activeMenuFilter.value;

		// 类型筛选
		let typeMatch = true;
		if (activeMenuTypeFilter.value !== "all") {
			const menuType = menu.name.replace("菜单", ""); // 从名称中提取类型
			typeMatch = menuType.includes(activeMenuTypeFilter.value);
		}

		return statusMatch && typeMatch;
	});

	// 如果当前选中的菜单不在筛选结果中，重置选择
	if (
		selectedMenu.value &&
		!filteredMenus.value.some((menu) => menu.id === selectedMenu.value.id)
	) {
		selectedMenu.value = null;
		currentMenuDishes.value = [];
	}
};

// 商家信息
const merchantInfo = ref({
	name: "XX餐厅",
	rating: "4.8/5.0",
	phone: "138XXXX8888",
	email: "xx@jaseats.com",
	address: "北京市朝阳区XX路123号",
});

// 编辑菜品
const editDish = (dish) => {
	console.log("编辑菜品:", dish);
	// 可以导航到菜品编辑页面
	// router.push({ path: '/merchant/dish-edit', query: { dishId: dish.id } });
};

// 切换菜品状态
const toggleDishStatus = (dish) => {
	const oldStatus = dish.status;
	dish.status = dish.status === "online" ? "offline" : "online";

	// 更新库存显示
	if (dish.status === "online" && dish.stock <= 10) {
		dish.status = "almost_sold";
	}

	ElMessage.success(
		`菜品 ${dish.name} 已${dish.status === "online" ? "上架" : "下架"}`
	);
	console.log("切换菜品状态:", dish);
};

// 营业概览
const businessOverview = ref({
	sales: 1234.0,
	orders: 28,
	newComments: 5,
	unreadMessages: 3,
});

// 页面加载
onMounted(() => {
	ElMessage.success("欢迎进入商家中心");
	// 调用后端API获取今日营业概览数据

	// 获取营业概览
	api.get(`/api/v1/merchant/${merchantId}/business-overview`)
		.then((response) => {
			if (response.code === "200" && response.data) {
				businessOverview.value = response.data;
			}
		})
		.catch((error) => {
			console.error("获取营业概览数据失败:", error);
			// 如果获取失败，保留模拟数据
		});

	// 获取订单列表
	api.get(`/api/v1/orders/merchant/${merchantId}`)
		.then((response) => {
			if (response.code === "200" && response.data) {
				allOrders.value = response.data;
				// 默认显示今日订单
				filterOrders("today");
			}
		})
		.catch((error) => {
			console.error("获取订单列表失败:", error);
			allOrders.value = [];
			filteredOrders.value = [];
		});

	// 获取今日菜单数据
	fetchTodayMenus();
});

// onUnmounted(() => {
//   ElMessage.success('欢迎下次再来');
// });
</script>

<template>
	<div class="merchant-home-container" v-if="$route.path === '/merchant/home'">
		<div class="merchant-content">
			<!-- 商家信息 -->
			<div class="merchant-info-card">
				<div class="info-header">
					<div class="avatar-section">
						<span class="avatar">📸</span>
						<!-- <el-button type="primary" size="small" class="edit-btn">🔧 编辑资料</el-button> -->
					</div>
					<div class="detail-section">
						<div class="merchant-name">🏪 {{ merchantInfo.name }}</div>
						<div class="merchant-rating">🌟 {{ merchantInfo.rating }}</div>
						<div class="contact-info">
							<span class="phone">📞 {{ merchantInfo.phone }}</span>
							<span class="email">📧 {{ merchantInfo.email }}</span>
							<span class="address">📍 {{ merchantInfo.address }}</span>
						</div>
					</div>
				</div>
			</div>

			<!-- 今日营业概览 -->
			<div class="overview-card">
				<h3 class="card-title">📈 今日营业概览：</h3>
				<div class="overview-grid">
					<div class="overview-item sales" @click="navigateToStatistics">
						<div class="item-icon">💰</div>
						<div class="item-content">
							<div class="overview-label">营业额</div>
							<div class="overview-value">
								¥{{ businessOverview.sales.toFixed(0) }}
							</div>
							<div class="item-trend trend-up">↑ 12.5%</div>
						</div>
					</div>
					<div class="overview-item orders" @click="navigateToOrders">
						<div class="item-icon">🍽️</div>
						<div class="item-content">
							<div class="overview-label">订单数</div>
							<div class="overview-value">
								{{ businessOverview.orders }}
							</div>
							<div class="item-trend trend-up">↑ 8.3%</div>
						</div>
					</div>
					<div class="overview-item comments" @click="navigateToComments">
						<div class="item-icon">🌟</div>
						<div class="item-content">
							<div class="overview-label">新增评价</div>
							<div class="overview-value">
								{{ businessOverview.newComments }}
							</div>
							<div class="item-trend trend-down">↓ 2.1%</div>
						</div>
					</div>
					<div class="overview-item messages" @click="navigateToMessages">
						<div class="item-icon">📞</div>
						<div class="item-content">
							<div class="overview-label">未读消息</div>
							<div class="overview-value">
								{{ businessOverview.unreadMessages }}
							</div>
							<div class="item-trend trend-neutral">→ 0%</div>
						</div>
					</div>
				</div>
			</div>

			<!-- 订单中心 -->
			<div class="orders-card">
				<div class="orders-header">
					<h3 class="card-title">📋 订单中心</h3>
					<div class="filter-section">
						<el-tag
							type="primary"
							effect="plain"
							class="filter-tag"
							:class="{ active: activeFilter === 'today' }"
							@click="filterOrders('today')"
							>今日订单</el-tag
						>
						<el-tag
							type="primary"
							effect="plain"
							class="filter-tag"
							:class="{ active: activeFilter === 'week' }"
							@click="filterOrders('week')"
							>本周订单</el-tag
						>
						<el-tag
							type="primary"
							effect="plain"
							class="filter-tag"
							:class="{ active: activeFilter === 'month' }"
							@click="filterOrders('month')"
							>本月订单</el-tag
						>
						<el-tag
							type="primary"
							effect="plain"
							class="filter-tag"
							:class="{ active: activeFilter === 'all' }"
							@click="filterOrders('all')"
							>全部订单</el-tag
						>
					</div>
				</div>

				<div class="orders-list">
					<div
						class="order-item"
						v-for="order in filteredOrders"
						:key="order.id"
					>
						<div class="order-info">
							<div class="order-no">🍽️ 订单号：{{ order.id }}</div>
							<div class="order-details">
								<span class="amount"
									>¥{{
										order.totalAmount
											? order.totalAmount.toFixed(2)
											: "0.00"
									}}</span
								>
								<span class="time">⏱️ {{ order.createTime }}</span>
								<el-tag
									:type="
										order.status === 1 ||
										order.status === 2 ||
										order.status === 3
											? 'info'
											: order.status === 4
											? 'warning'
											: order.status === 5
											? 'success'
											: 'danger'
									"
								>
									{{ orderStatusMap[order.status] || "未知状态" }}
								</el-tag>
							</div>
						</div>
						<div class="order-actions">
							<el-button
								type="primary"
								size="small"
								@click="viewOrderDetails(order)"
								>🔍 详情</el-button
							>
							<el-button
								type="success"
								size="small"
								@click="updateOrderStatus(order)"
								>⏱️ 更新状态</el-button
							>
							<el-button
								type="warning"
								size="small"
								@click="notifyUser(order)"
								>🔔 通知用户</el-button
							>
						</div>
					</div>
					<div v-if="filteredOrders.length === 0" class="no-orders">
						<p>后端数据为空，当前没有订单</p>
					</div>
				</div>

				<div class="view-all">
					<el-button type="text" @click="navigateToOrders"
						>📤 查看全部订单</el-button
					>
				</div>
			</div>

			<!-- 快捷操作 -->
			<div class="quick-actions-card">
				<h3 class="card-title">🎯 快捷操作：</h3>
				<div class="actions-grid">
					<div class="action-item" @click="navigateToMenu">
						<div class="action-icon">➕</div>
						<div class="action-label">新增菜品</div>
					</div>
					<div class="action-item" @click="setDiscount">
						<div class="action-icon">💰</div>
						<div class="action-label">设置优惠</div>
					</div>
					<div class="action-item" @click="adjustBusinessHours">
						<div class="action-icon">⏱️</div>
						<div class="action-label">调整营业时间</div>
					</div>
					<div class="action-item" @click="contactCustomerService">
						<div class="action-icon">📞</div>
						<div class="action-label">联系客服</div>
					</div>
				</div>
			</div>

			<!-- 今日菜单 -->
			<div class="quick-actions-card today-menu-card">
				<div class="menu-header">
					<h3 class="card-title">📋 今日菜单</h3>
					<div class="filter-section">
						<span class="filter-label">分类：</span>
						<el-tag
							type="primary"
							effect="plain"
							class="filter-tag"
							:class="{ active: activeMenuTypeFilter === 'all' }"
							@click="filterMenus('all', 'type')"
							>全部</el-tag
						>
						<el-tag
							type="primary"
							effect="plain"
							class="filter-tag"
							:class="{ active: activeMenuTypeFilter === '早餐' }"
							@click="filterMenus('早餐', 'type')"
							>早餐</el-tag
						>
						<el-tag
							type="primary"
							effect="plain"
							class="filter-tag"
							:class="{ active: activeMenuTypeFilter === '午餐' }"
							@click="filterMenus('午餐', 'type')"
							>午餐</el-tag
						>
						<el-tag
							type="primary"
							effect="plain"
							class="filter-tag"
							:class="{ active: activeMenuTypeFilter === '晚餐' }"
							@click="filterMenus('晚餐', 'type')"
							>晚餐</el-tag
						>
						<el-tag
							type="primary"
							effect="plain"
							class="filter-tag"
							:class="{ active: activeMenuTypeFilter === '下午茶' }"
							@click="filterMenus('下午茶', 'type')"
							>下午茶</el-tag
						>
						<el-tag
							type="primary"
							effect="plain"
							class="filter-tag"
							:class="{ active: activeMenuTypeFilter === '今日特色' }"
							@click="filterMenus('今日特色', 'type')"
							>今日特色</el-tag
						>
					</div>
				</div>

				<div class="menu-header">
					<div class="filter-section">
						<span class="filter-label">状态：</span>
						<el-tag
							type="primary"
							effect="plain"
							class="filter-tag"
							:class="{ active: activeMenuFilter === 'all' }"
							@click="filterMenus('all', 'status')"
							>全部</el-tag
						>
						<el-tag
							type="primary"
							effect="plain"
							class="filter-tag"
							:class="{ active: activeMenuFilter === 'online' }"
							@click="filterMenus('online', 'status')"
							>上架中</el-tag
						>
						<el-tag
							type="primary"
							effect="plain"
							class="filter-tag"
							:class="{ active: activeMenuFilter === 'offline' }"
							@click="filterMenus('offline', 'status')"
							>下架中</el-tag
						>
						<el-tag
							type="primary"
							effect="plain"
							class="filter-tag"
							:class="{ active: activeMenuFilter === 'draft' }"
							@click="filterMenus('draft', 'status')"
							>草稿</el-tag
						>
					</div>
				</div>

				<div class="menu-list">
					<div
						class="menu-item"
						v-for="menu in filteredMenus"
						:key="menu.id"
						:class="{ active: selectedMenu?.id === menu.id }"
						@click="switchMenu(menu)"
					>
						<div class="menu-info">
							<div class="menu-name">
								<span class="name">{{ menu.name }}</span>
								<el-tag :type="menuStatusMap[menu.status].type">
									{{ menuStatusMap[menu.status].icon }}
									{{ menuStatusMap[menu.status].text }}
								</el-tag>
							</div>

							<div class="menu-stats">
								<span class="dishes-count"
									>🍴 {{ menu.dishes }} 菜品</span
								>
								<span class="update-time"
									>⏰ 更新时间：{{ menu.updateTime }}</span
								>
							</div>

							<div class="auto-times">
								<span v-if="menu.autoOnline" class="auto-online">
									⏰ 自动上架：{{ menu.autoOnline }}
								</span>
								<span v-if="menu.autoOffline" class="auto-offline">
									⏰ 自动下架：{{ menu.autoOffline }}
								</span>
							</div>
						</div>
					</div>
					<!-- 空菜单提示 -->
					<div v-if="filteredMenus.length === 0" class="empty-menu">
						<span>🍽️ 今日咱未设置菜单</span>
					</div>
				</div>

				<div class="view-all">
					<el-button type="text" @click="navigateToMenu"
						>📤 查看全部菜单</el-button
					>
				</div>
			</div>

			<!-- 菜品列表 -->
			<div v-if="selectedMenu" class="quick-actions-card dishes-card">
				<div class="menu-header">
					<h3 class="card-title">🍽️ {{ selectedMenu.name }} - 菜品列表</h3>
				</div>

				<div class="dish-list">
					<div
						class="dish-item"
						v-for="dish in currentMenuDishes"
						:key="dish.id"
					>
						<div class="dish-cover">
							{{ dish.image || "🍱" }}
						</div>
						<div class="dish-info">
							<div class="dish-name">
								<span class="name">{{ dish.name }}</span>
								<el-tag
									:type="dishStatusMap[dish.status].type"
									size="small"
								>
									{{ dishStatusMap[dish.status].text }}
								</el-tag>
							</div>

							<div class="dish-desc">
								{{ dish.description || "美味可口，欢迎品尝" }}
							</div>

							<div class="dish-stats">
								<span class="dish-category">📁 {{ dish.category }}</span>
								<span class="dish-price">💰 ¥{{ dish.price }}</span>
								<span
									class="dish-stock"
									:class="{
										'stock-almost': dish.status === 'almost_sold',
										'stock-off': dish.status === 'offline',
									}"
								>
									{{
										dish.status === "almost_sold"
											? "⏳ 即将售罄"
											: dish.status === "offline"
											? "❌ 已下架"
											: `📦 ${dish.stock} 份`
									}}
								</span>
							</div>
						</div>
						<div class="dish-actions">
							<el-button
								type="primary"
								size="small"
								@click="editDish(dish)"
							>
								✏️ 编辑
							</el-button>
							<el-button
								:type="dish.status === 'online' ? 'warning' : 'success'"
								size="small"
								@click="toggleDishStatus(dish)"
							>
								{{ dish.status === "online" ? "🔴 下架" : "🟢 上架" }}
							</el-button>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</template>

<style scoped lang="less">
.merchant-home-container {
	padding: 0 20px 20px 20px;

	.merchant-info-card {
		margin-bottom: 24px;
		padding: 24px; /* 添加内边距 */
		border: 2px solid #67c23a; /* 添加绿色边框 */
		border-radius: 8px; /* 圆角边框 */
		background-color: #ffffff; /* 白色背景 */
		box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05); /* 添加阴影效果 */

		.info-header {
			display: flex;
			align-items: center;
			gap: 20px;

			.avatar-section {
				.avatar {
					font-size: 64px;
				}
				.edit-btn {
					margin-top: 10px;
				}
			}

			.detail-section {
				flex: 1;

				.merchant-name {
					font-size: 20px;
					font-weight: 600;
					margin-bottom: 8px;
				}

				.merchant-rating {
					margin-bottom: 8px;
				}

				.contact-info {
					display: flex;
					flex-wrap: wrap;
					gap: 20px;
					font-size: 14px;
					color: #606266;
				}
			}
		}
	}

	.overview-card {
		margin-bottom: 24px;
		padding: 24px;
		border: 2px solid #e6a23c;
		border-radius: 12px;
		background-color: #ffffff; /* 改为白色背景 */
		box-shadow: 0 4px 20px rgba(230, 162, 60, 0.15);

		.card-title {
			font-size: 20px;
			font-weight: 700;
			margin-bottom: 20px;
			color: #e6a23c;
			display: flex;
			align-items: center;

			&::after {
				content: "";
				flex: 1;
				height: 1px;
				background: linear-gradient(to right, #e6a23c, transparent);
				margin-left: 15px;
			}
		}

		.overview-grid {
			display: grid;
			grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
			gap: 20px;

			.overview-item {
				display: flex;
				align-items: center;
				gap: 16px;
				padding: 20px;
				border-radius: 12px;
				background: white;
				box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
				transition: all 0.3s ease;
				cursor: pointer;
				border: 1px solid #f0f0f0;

				&:hover {
					transform: translateY(-5px);
					box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
					border-color: #ffd7a3;
				}

				&.sales {
					border-left: 4px solid #67c23a;

					&:hover {
						border-left: 4px solid #67c23a;
					}
				}

				&.orders {
					border-left: 4px solid #409eff;

					&:hover {
						border-left: 4px solid #409eff;
					}
				}

				&.comments {
					border-left: 4px solid #e6a23c;

					&:hover {
						border-left: 4px solid #e6a23c;
					}
				}

				&.messages {
					border-left: 4px solid #f56c6c;

					&:hover {
						border-left: 4px solid #f56c6c;
					}
				}

				.item-icon {
					font-size: 32px;
					width: 60px;
					height: 60px;
					display: flex;
					align-items: center;
					justify-content: center;
					border-radius: 50%;
					background: rgba(230, 162, 60, 0.1);
				}

				.item-content {
					flex: 1;

					.overview-label {
						font-size: 14px;
						color: #909399;
						margin-bottom: 4px;
						font-weight: 500;
					}

					.overview-value {
						font-size: 24px;
						font-weight: 700;
						margin-bottom: 4px;
					}

					.item-trend {
						font-size: 12px;
						font-weight: 600;

						&.trend-up {
							color: #67c23a;
						}

						&.trend-down {
							color: #f56c6c;
						}

						&.trend-neutral {
							color: #909399;
						}
					}
				}
			}
		}
	}

	.orders-card {
		margin-bottom: 24px;
		padding: 24px; /* 添加内边距 */
		border: 2px solid #409eff; /* 加强边框 */
		border-radius: 8px; /* 圆角边框 */
		background-color: #ffffff; /* 白色背景 */
		box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05); /* 添加阴影效果 */

		.orders-header {
			display: flex;
			justify-content: space-between;
			align-items: center;
			margin-bottom: 20px;

			.card-title {
				font-size: 18px;
				font-weight: 600;
				margin: 0;
			}

			.filter-section {
				.filter-tag {
					margin-right: 10px;
					cursor: pointer; // 添加鼠标悬浮点击样式

					&.active {
						color: #409eff;
						background-color: rgba(64, 158, 255, 0.1);
					}

					&:first-child {
						// 今日订单样式优化
						border-left: 3px solid #67c23a;
						padding-left: 8px;

						&.active {
							background-color: rgba(103, 194, 58, 0.1);
							color: #67c23a;
						}
					}
				}
			}
		}

		.orders-list {
			max-height: 400px;
			overflow-y: auto;
			padding-right: 8px;

			.no-orders {
				text-align: center;
				padding: 80px 0;
				color: #909399;
				font-size: 16px;
			}


			.order-item {
				display: flex;
				justify-content: space-between;
				align-items: flex-start;
				padding: 16px;
				border: 1px solid #e4e7ed;
				border-radius: 4px;
				margin-bottom: 12px;

				.order-info {
					.order-no {
						font-weight: 600;
						margin-bottom: 8px;
					}

					.order-details {
						display: flex;
						flex-wrap: wrap;
						gap: 16px;
						font-size: 14px;

						.amount {
							font-weight: 600;
						}
					}
				}

				.order-actions {
					display: flex;
					gap: 8px;
					flex-wrap: wrap;
				}
			}
		}

		.view-all {
			text-align: right;
			margin-top: 12px;
		}
	}

	.quick-actions-card {
		margin-bottom: 24px;
		padding: 24px; /* 添加内边距 */
		border: 2px solid #f56c6c; /* 添加红色边框 */
		border-radius: 8px; /* 圆角边框 */
		background-color: #ffffff; /* 白色背景 */
		box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05); /* 添加阴影效果 */

		.card-title {
			font-size: 18px;
			font-weight: 600;
			margin-bottom: 16px;
		}

		.actions-grid {
			display: grid;
			grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
			gap: 20px;

			.action-item {
				display: flex;
				flex-direction: column;
				align-items: center;
				padding: 24px;
				border: 1px solid #e4e7ed;
				border-radius: 4px;
				cursor: pointer;
				transition: all 0.3s;

				&:hover {
					box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
				}

				.action-icon {
					font-size: 48px;
					margin-bottom: 8px;
				}

				.action-label {
					font-size: 14px;
					font-weight: 500;
				}
			}
		}

		// 今日菜单
		.today-menu-card {
			margin-bottom: 24px;
			padding: 24px; /* 添加内边距 */
			border: 2px solid #909399; /* 灰色边框 */
			border-radius: 8px; /* 圆角边框 */
			background-color: #ffffff; /* 白色背景 */
			box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05); /* 添加阴影效果 */

			.menu-header {
				display: flex;
				justify-content: flex-start;
				align-items: center;
				margin-bottom: 20px;

				.card-title {
					font-size: 18px;
					font-weight: 600;
					margin: 0;
				}

				.filter-label {
					font-weight: 500;
					margin-right: 8px;
				}
			}

			.menu-list {
				margin-bottom: 20px;

				.menu-item {
					padding: 16px;
					border: 1px solid #e4e7ed;
					border-radius: 4px;
					margin-bottom: 12px;
					background-color: #fff;
					transition: box-shadow 0.3s;
					cursor: pointer;

					&:hover {
						box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
					}
				}

				.empty-menu {
					text-align: center;
					padding: 50px 20px;
					color: #666;
					font-size: 18px;
					background-color: #fff8e1; /* 浅黄色背景 */
					border: 2px dashed #ffb74d; /* 橙色虚线边框 */
					border-radius: 8px;
					margin-bottom: 20px;
					box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05); /* 轻微阴影 */
					transition: all 0.3s ease; /* 平滑过渡效果 */

					&:hover {
						box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08); /* 悬停时增强阴影 */
						background-color: #fff3cd; /* 悬停时加深背景色 */
					}

					span {
						display: flex;
						align-items: center;
						justify-content: center;
						gap: 8px; /* 文字和图标间距 */
					}
				}

				.menu-item {
					.menu-info {
						.menu-name {
							display: flex;
							align-items: center;
							gap: 10px;
							margin-bottom: 12px;

							.name {
								font-size: 16px;
								font-weight: 600;
							}
						}

						.menu-stats,
						.auto-times {
							display: flex;
							flex-wrap: wrap;
							gap: 24px;
							margin-bottom: 8px;
							font-size: 14px;

							.dishes-count {
								color: #606266;
							}
						}

						.auto-times {
							font-size: 13px;
							color: #909399;
						}
					}
				}
			}

			.view-all {
				text-align: right;
				margin-top: 12px;
			}
		}

		// 菜品列表样式
		.dishes-card {
			margin-bottom: 24px;
			padding: 24px; /* 添加内边距 */
			border: 2px solid #67c23a; /* 绿色边框 */
			border-radius: 8px; /* 圆角边框 */
			background-color: #ffffff; /* 白色背景 */
			box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05); /* 添加阴影效果 */

			.dish-list {
				margin-bottom: 20px;

				.dish-item {
					padding: 16px;
					border: 1px solid #e4e7ed;
					border-radius: 8px;
					margin-bottom: 12px;
					background-color: #fff;
					transition: all 0.3s;
					display: flex;
					align-items: flex-start;
					gap: 16px;

					&:hover {
						box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.1);
						transform: translateY(-2px);
					}

					.dish-cover {
						font-size: 48px;
						width: 80px;
						height: 80px;
						display: flex;
						align-items: center;
						justify-content: center;
						background-color: #f8f9fa;
						border-radius: 8px;
						flex-shrink: 0;
					}

					.dish-info {
						flex: 1;

						.dish-name {
							display: flex;
							align-items: center;
							gap: 10px;
							margin-bottom: 8px;

							.name {
								font-size: 16px;
								font-weight: 600;
								color: #303133;
							}
						}

						.dish-desc {
							font-size: 13px;
							color: #909399;
							margin-bottom: 12px;
							line-height: 1.5;
						}

						.dish-stats {
							display: flex;
							flex-wrap: wrap;
							gap: 20px;
							font-size: 14px;
							color: #606266;

							.dish-category {
								background-color: #ecf5ff;
								color: #66b1ff;
								padding: 2px 8px;
								border-radius: 4px;
								font-size: 12px;
							}

							.dish-price {
								color: #e6a23c;
								font-weight: 500;
							}

							.dish-stock {
								font-size: 13px;

								&.stock-almost {
									color: #f59f00;
								}

								&.stock-off {
									color: #f56c6c;
								}
							}
						}
					}

					.dish-actions {
						display: flex;
						flex-direction: column;
						gap: 8px;
						flex-shrink: 0;

						button {
							width: 80px;
						}
					}
				}
			}
		}
	}
}
</style>
