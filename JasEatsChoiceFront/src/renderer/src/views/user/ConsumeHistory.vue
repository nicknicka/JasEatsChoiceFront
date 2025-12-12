<template>
	<div class="consume-history-container">
		<common-back-button
			type="default"
			size="small"
			@click="goBack"
			:use-router-back="false"
			style="margin-bottom: 20px"
		/>
		<h2>消费记录</h2>

		<div class="filter-bar">
			<el-select
				v-model="filterType"
				placeholder="筛选类型"
				style="width: 150px; margin-right: 10px"
			>
				<el-option label="全部" value="all" />
				<el-option label="充值" value="recharge" />
				<el-option label="消费" value="consume" />
				<el-option label="提现" value="withdraw" />
			</el-select>

			<el-date-picker
				v-model="dateRange"
				type="daterange"
				range-separator="至"
				start-placeholder="开始日期"
				end-placeholder="结束日期"
				style="width: 300px; margin-right: 10px"
			/>

			<el-button type="primary" size="small" @click="applyFilter">
				<el-icon><Search /></el-icon> 查询
			</el-button>

			<el-button type="default" size="small" @click="resetFilter">
				<el-icon><Refresh /></el-icon> 重置
			</el-button>
		</div>

		<div class="total-balance">
			<span class="label">当前平台币余额:</span>
			<span class="balance">{{ currentBalance }}个</span>
		</div>

		<el-table :data="paginatedHistory" style="width: 100%; margin-top: 20px">
			<el-table-column prop="date" label="日期" width="150" />
			<el-table-column prop="type" label="类型" width="100">
				<template #default="scope">
					<el-tag
						:type="
							scope.row.type === 'recharge'
								? 'success'
								: scope.row.type === 'withdraw'
								? 'danger'
								: 'info'
						"
					>
						{{
							scope.row.type === "recharge"
								? "充值"
								: scope.row.type === "withdraw"
								? "提现"
								: "消费"
						}}
					</el-tag>
				</template>
			</el-table-column>
			<el-table-column prop="amount" label="金额" width="120">
				<template #default="scope">
					<span :class="scope.row.type === 'recharge' ? 'income' : 'expense'">
						{{ scope.row.type === "recharge" ? "+" : "-"
						}}{{ scope.row.amount }}
					</span>
				</template>
			</el-table-column>
			<el-table-column prop="description" label="描述" />
			<el-table-column prop="status" label="状态" width="100">
				<template #default="scope">
					<el-tag
						:type="scope.row.status === 'success' ? 'success' : 'warning'"
					>
						{{ scope.row.status === "success" ? "成功" : "失败" }}
					</el-tag>
				</template>
			</el-table-column>
		</el-table>

		<div class="pagination">
			<el-pagination
				v-model:current-page="currentPage"
				v-model:page-size="pageSize"
				:page-sizes="[10, 20, 50]"
				:total="total"
				layout="total, sizes, prev, pager, next, jumper"
				@size-change="handleSizeChange"
				@current-change="handleCurrentChange"
			/>
		</div>
	</div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from "vue";
import { useRouter } from "vue-router";
import { Search, Refresh } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import CommonBackButton from '../../components/common/CommonBackButton.vue';
import axios from "axios";
import { API_CONFIG } from "../../config/index.js";

// 路由实例
const router = useRouter();

// 返回个人中心
const goBack = () => {
	router.push("/user/home/profile");
};

// 消费记录数据
const history = ref([]);
const total = ref(0); // 总记录数
const loading = ref(false); // 加载状态

// 筛选条件
const filterType = ref("all");
const dateRange = ref([]);

// 分页参数
const currentPage = ref(1);
const pageSize = ref(10);

// 当前余额
const currentBalance = ref("177");

// 计算属性：过滤后的历史记录
const filteredHistory = computed(() => {
	let filtered = [...history.value];

	// 类型过滤
	if (filterType.value !== "all") {
		filtered = filtered.filter(item => item.type === filterType.value);
	}

	// 日期范围过滤
	if (dateRange.value && dateRange.value.length === 2) {
		const startDate = new Date(dateRange.value[0]);
		const endDate = new Date(dateRange.value[1]);
		filtered = filtered.filter(item => {
			const itemDate = new Date(item.date);
			return itemDate >= startDate && itemDate <= endDate;
		});
	}

	return filtered;
});

// 计算属性：分页后的历史记录
const paginatedHistory = computed(() => {
	const start = (currentPage.value - 1) * pageSize.value;
	const end = start + pageSize.value;
	return filteredHistory.value.slice(start, end);
});

// 从后端获取消费记录
const fetchConsumeHistory = async () => {
	try {
		loading.value = true;

		// 获取当前用户ID（从localStorage中获取）
		const userId = localStorage.getItem("userId") || 1;

		// 构造请求参数
		const params = {
			userId,
			page: currentPage.value,
			pageSize: pageSize.value,
			type: filterType.value,
			// 日期范围处理：转换为字符串格式
			...(dateRange.value && dateRange.value.length === 2
				? {
						startDate: dateRange.value[0].toISOString().split("T")[0],
						endDate: dateRange.value[1].toISOString().split("T")[0],
				  }
				: {}),
		};

		// 发送请求到后端
		const response = await axios.get(`${API_CONFIG.baseURL}/v1/consume-history`, {
			params,
		});

		// 处理响应数据
		if (response.data && response.data.success) {
			// 确保每条记录都有唯一id
			history.value = response.data.data.records.map((item, index) => ({
				...item,
				id: item.id || Date.now() + index
			}));
			total.value = response.data.data.total;
			ElMessage.success("消费记录加载成功");
		} else {
			ElMessage.error("消费记录加载失败：" + response.data.message);
		}
	} catch (error) {
		console.error("获取消费记录失败:", error);
		ElMessage.error("获取消费记录失败，请稍后重试");
	} finally {
		loading.value = false;
	}
};

// 应用筛选
const applyFilter = () => {
	currentPage.value = 1; // 筛选后回到第一页
	fetchConsumeHistory();
};

// 重置筛选
const resetFilter = () => {
	filterType.value = "all";
	dateRange.value = [];
	currentPage.value = 1;
	fetchConsumeHistory();
	ElMessage.info("筛选条件已重置");
};

// 页面大小变化
const handleSizeChange = (val) => {
	pageSize.value = val;
	currentPage.value = 1;
	fetchConsumeHistory();
};

// 页面变化
const handleCurrentChange = (val) => {
	currentPage.value = val;
	fetchConsumeHistory();
};

// 从localStorage加载用户信息和交易记录
onMounted(() => {
	const savedUserInfo = localStorage.getItem("userInfo");
	if (savedUserInfo) {
		const userInfo = JSON.parse(savedUserInfo);
		currentBalance.value = userInfo.wallet ? userInfo.wallet.balance : "177";
	}

	const savedHistory = localStorage.getItem("consumeHistory");
	if (savedHistory) {
		// 加载并确保每条记录都有唯一id
		const loadedHistory = JSON.parse(savedHistory);
		history.value = loadedHistory.map((item, index) => ({
			...item,
			id: item.id || Date.now() + index
		}));
		total.value = history.value.length; // 设置总记录数
	}
});

// 监听历史记录变化，保存到localStorage
watch(() => history.value, () => {
	// 直接保存到localStorage，不再修改原始数组
	localStorage.setItem("consumeHistory", JSON.stringify(history.value));
	total.value = history.value.length; // 更新总记录数
}, { deep: true });
</script>

<style scoped>
.consume-history-container {
	padding: 0 20px 20px 20px;
}

.filter-bar {
	margin: 20px 0;
	display: flex;
	align-items: center;
	flex-wrap: wrap;
	gap: 10px;
}

.total-balance {
	margin: 10px 0;
	padding: 15px;
	background-color: #f5f7fa;
	border-radius: 5px;
	font-size: 16px;
}

.total-balance .label {
	font-weight: bold;
}

.total-balance .balance {
	color: #67c23a;
	font-weight: bold;
	font-size: 20px;
	margin-left: 10px;
}

.income {
	color: #67c23a;
	font-weight: bold;
}

.expense {
	color: #f56c6c;
	font-weight: bold;
}

.pagination {
	margin-top: 20px;
	text-align: right;
}
</style>
