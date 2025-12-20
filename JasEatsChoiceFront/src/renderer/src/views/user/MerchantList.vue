<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import axios from "axios";

// 引入API配置
import { API_CONFIG } from "../../config/index.js";

const router = useRouter();

// 商家列表数据 - 初始化为空数组
const merchants = ref([]);

// 加载状态
const isLoading = ref(false);

// 筛选条件
const filters = ref({
	type: "all",
	sort: "distance",
});

// 搜索关键词
const searchKeyword = ref("");

// 商家类型筛选选项
const typeOptions = ref([
  { label: "全部", value: "all" },
  { label: "轻食", value: "轻食" },
  { label: "早餐", value: "早餐" },
  { label: "中餐", value: "中餐" },
  { label: "健身餐", value: "健身餐" }
]);

// 商家排序选项
const sortOptions = ref([
  { label: "距离最近", value: "distance" },
  { label: "评分最高", value: "rating" }
]);

// 获取当前路由
const route = useRoute();

// 页面加载时从URL获取搜索参数并加载商家数据
onMounted(() => {
	const searchQuery = route.query.search;
	if (searchQuery) {
		searchKeyword.value = searchQuery;
	}

	// 加载商家列表数据
	loadMerchants();
});

// 从后端加载商家列表
const loadMerchants = () => {
	isLoading.value = true;

	// 调用后端API获取商家列表
	axios
		.get(API_CONFIG.baseURL + API_CONFIG.merchant.list, {
			params: { search: searchKeyword.value },
		})
		.then((response) => {
			// 假设后端返回的数据结构与前端期望的一致
			// 如果结构不同，需要在这里进行转换
			if (response.data.data) {
				merchants.value = response.data.data;
			} else {
				// 处理空数据情况
				merchants.value = [];
			}
		})
		.catch((error) => {
			console.error("加载商家列表失败:", error);
		})
		.finally(() => {
			isLoading.value = false;
		});
};


// 立即下单功能
const orderNow = (merchant) => {
	// 将商家信息存储到会话存储
	sessionStorage.setItem("selectedMerchant", JSON.stringify(merchant));
	// 跳转到商家详情页面的立即下单流程
	router.push({
		path: "/user/home/merchant-detail",
		query: { viewMode: "order" },
	});
};

// 计算属性：过滤和排序后的商家列表
const filteredMerchants = computed(() => {
	let result = [...merchants.value];

	// 类型筛选
	if (filters.value.type !== "all") {
		result = result.filter((merchant) => merchant.type === filters.value.type);
	}

	// 搜索筛选
	if (searchKeyword.value) {
		const keyword = searchKeyword.value.toLowerCase();
		result = result.filter(
			(merchant) =>
				merchant.name.toLowerCase().includes(keyword) ||
				(merchant.tags &&
					merchant.tags.some((tag) => tag.toLowerCase().includes(keyword)))
		);
	}

	// 排序
	if (filters.value.sort === "distance") {
		// 按距离排序
		result.sort((a, b) => {
			const distanceA = parseFloat((a.distance || "0km").replace("km", ""));
			const distanceB = parseFloat((b.distance || "0km").replace("km", ""));
			return distanceA - distanceB;
		});
	} else if (filters.value.sort === "rating") {
		// 按评分排序
		result.sort((a, b) => b.rating - a.rating);
	}

	return result;
});
</script>

<template>
	<div class="merchant-list-container">
		<h2>商家查找</h2>

		<!-- 搜索和筛选区 -->
		<div class="search-filter-section">
			<el-input
				v-model="searchKeyword"
				placeholder="搜索商家名称..."
				clearable
				class="search-input"
			>
				<template #prefix>
					<span>🔍</span>
				</template>
			</el-input>

			<div class="filter-row">
				<el-select
					v-model="filters.type"
					placeholder="筛选类型"
					size="small"
					style="width: 140px"
					class="type-select"
				>
					<el-option
						v-for="option in typeOptions"
						:key="option.value"
						:label="option.label"
						:value="option.value"
					/>
				</el-select>

				<el-select
					v-model="filters.sort"
					placeholder="排序方式"
					size="small"
					style="width: 140px"
				>
					<el-option
						v-for="option in sortOptions"
						:key="option.value"
						:label="option.label"
						:value="option.value"
					/>
				</el-select>
			</div>
		</div>

		<!-- 商家列表 -->
		<div class="merchant-grid">
			<!-- 加载中状态 -->
			<el-skeleton :rows="6" v-if="isLoading" class="loading-skeleton" />

			<el-card
				v-for="merchant in filteredMerchants"
				:key="merchant.id"
				class="merchant-card"
				v-else-if="filteredMerchants.length > 0"
			>
				<div class="card-header">
					<div class="merchant-image">{{ merchant.image || '🏪' }}</div>
					<div class="merchant-info">
						<div class="merchant-name">{{ merchant.name }}</div>
						<div class="merchant-rating">
							<el-rate
								v-model="merchant.rating"
								:disabled="true"
								show-text
								size="small"
							/>
							<span class="distance">{{ merchant.distance || '未知距离' }}</span>
						</div>
						<div class="merchant-status">
							<el-tag
								:type="
									merchant.status === '营业中' ? 'success' : 'danger'
								"
								size="small"
							>
								{{ merchant.status || '未知状态' }}
							</el-tag>
						</div>
					</div>
				</div>

				<!-- 商家类型 - 只在有数据时显示 -->
				<div class="merchant-type" v-if="merchant.type">
					<el-tag type="primary" size="small">{{ merchant.type }}</el-tag>
				</div>

				<!-- 商家标签 - 只在有数据时显示 -->
				<div class="merchant-tags" v-if="merchant.tags && merchant.tags.length > 0">
					<el-tag
						v-for="tag in merchant.tags"
						:key="tag"
						size="small"
						type="info"
					>
						{{ tag }}
					</el-tag>
				</div>

				<div class="card-actions">
					<el-button
						type="primary"
						size="small"
						icon="el-icon-shopping-cart-2"
						icon-position="left"
						@click="orderNow(merchant)"
						>立即下单</el-button
					>
				</div>
			</el-card>

			<!-- 空数据提示 -->
			<div class="empty-data" v-else>
				<div class="empty-icon">😕</div>
				<div class="empty-text">
					<h3>暂无商家数据</h3>
					<p>当前条件下没有找到任何商家，请尝试调整搜索条件或筛选方式</p>
				</div>
			</div>
		</div>
	</div>
</template>

<style scoped lang="less">
.merchant-list-container {
	padding: 0 20px 20px 20px;

	h2 {
		font-size: 24px;
		margin: 0 0 20px 0;
	}

	.search-filter-section {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 20px;
		padding: 15px;
		background-color: #f8f9fa;
		border-radius: 8px;
		box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);

		.search-input {
			width: 300px;
		}

		.filter-row {
			display: flex;
			gap: 15px;
		}

		.type-select {
			.el-select__input {
				border-radius: 4px;
				border: 1px solid #dcdfe6;
			}
		}
	}

	.merchant-grid {
		display: grid;
		grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
		gap: 20px;
		padding: 0 20px;
	}

	.merchant-card {
		width: 100%;
		box-sizing: border-box;
		transition: all 0.3s ease;
		border-radius: 12px;
		box-shadow: 0 2px 15px rgba(0, 0, 0, 0.08);

		&:hover {
			box-shadow: 0 4px 25px rgba(0, 0, 0, 0.12);
			transform: translateY(-2px);
		}

		.card-header {
			display: flex;
			gap: 20px;
			margin-bottom: 15px;
			align-items: center;

			.merchant-image {
				font-size: 50px;
			}

			.merchant-info {
				.merchant-name {
					font-size: 18px;
					font-weight: bold;
					margin-bottom: 5px;
				}

				.merchant-rating {
					display: flex;
					align-items: center;
					gap: 10px;
					margin-bottom: 5px;

					.distance {
						font-size: 14px;
						color: #666;
					}
				}
			}
		}

		.merchant-type {
			margin-bottom: 10px;
		}

		.merchant-tags {
			margin-bottom: 20px;
			display: flex;
			flex-wrap: wrap;
			gap: 8px;
		}

		.card-actions {
			display: flex;
			gap: 10px;
			padding-top: 16px;
			border-top: 1px solid #f0f0f0;

			.el-button {
				flex: 1;
				border-radius: 8px;
				font-weight: 500;
			}
		}
	}

	// 加载中样式
	.loading-skeleton {
		grid-column: 1 / -1;
	}

	// 空数据样式
	.empty-data {
		grid-column: 1 / -1;
		text-align: center;
		padding: 80px 20px;
		background-color: #f8f9fa;
		border-radius: 8px;
		box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);

		.empty-icon {
			font-size: 80px;
			margin-bottom: 20px;
			opacity: 0.6;
		}

		.empty-text {
			color: #666;

			h3 {
				font-size: 20px;
				margin: 0 0 10px 0;
				color: #333;
			}

			p {
				font-size: 14px;
				margin: 0;
			}
		}
	}
}
</style>
