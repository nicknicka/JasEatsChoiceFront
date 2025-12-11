<script setup>
import { ref, computed, onMounted } from "vue";
import axios from "axios";
import { API_CONFIG } from "../../config";
import { ElMessage, ElMessageBox } from "element-plus";
import { Star } from "@element-plus/icons-vue";

// 我的食谱数据
const myRecipes = ref([]);
const loadingFailed = ref(false);

// 加载我的食谱数据
const loadMyRecipes = () => {
	axios
		.get(API_CONFIG.baseURL + API_CONFIG.recipe.favorite)
		.then((response) => {
			if (response.data.data) {
				myRecipes.value = response.data.data;
			} else {
				myRecipes.value = [];
			}
			loadingFailed.value = false;
		})
		.catch((error) => {
			console.error("加载我的食谱失败:", error);
			myRecipes.value = [];
			loadingFailed.value = true;
			ElMessage.error("加载我的食谱失败");
		});
};

// 组件挂载时加载数据
onMounted(() => {
	loadMyRecipes();
});

// 食谱筛选
const recipeFilter = ref("all");

// 计算属性：过滤后的食谱列表，收藏的食谱排在前面
const filteredRecipes = computed(() => {
	let filtered = [];

	if (recipeFilter.value === "all") {
		filtered = [...myRecipes.value];
	} else {
		filtered = myRecipes.value.filter((recipe) => recipe.type === recipeFilter.value);
	}

	// 排序：收藏的食谱在前
	return filtered.sort((a, b) => {
		// 如果a收藏而b未收藏，a排在前面
		if (a.favorite && !b.favorite) return -1;
		// 如果b收藏而a未收藏，b排在前面
		if (!a.favorite && b.favorite) return 1;
		// 否则保持原顺序
		return 0;
	});
});

// 切换收藏状态
const toggleFavorite = (recipe) => {
	recipe.favorite = !recipe.favorite;
};

// 食谱详情模态框
const detailDialogVisible = ref(false);
const selectedRecipe = ref(null);
const activeDishName = ref(""); // 用于折叠面板的激活项

// 查看详情
const viewRecipeDetails = (recipe) => {
	selectedRecipe.value = recipe;
	detailDialogVisible.value = true;
};

// 添加食谱对话框
const addDialogVisible = ref(false);

// 新食谱表单数据
const newRecipe = ref({
	name: "",
	type: "早餐",
	calories: "",
	time: "",
	favorite: false,
	details: "",
	ingredients: "",
	steps: "",
	dishComposition: [],
});

// 获取标签类型
const getTagType = (type) => {
	switch (type) {
		case "早餐":
			return "warning";
		case "午餐":
			return "success";
		case "晚餐":
			return "primary";
		case "加餐":
		case "afternoon_tea":
		case "tea":
			return "info";
		case "night_snack":
		case "snack":
			return "primary";
		default:
			return "info";
	}
};

// 打开添加食谱对话框
const openAddDialog = () => {
	addDialogVisible.value = true;
	// 重置表单数据
	newRecipe.value = {
		name: "",
		type: "早餐",
		calories: "",
		time: "",
		favorite: false,
		details: "",
		ingredients: "",
		steps: "",
		dishComposition: [],
	};
};

// 保存新食谱
const saveNewRecipe = () => {
	// 简单的表单验证
	if (!newRecipe.value.name.trim()) {
		ElMessage.warning("请填写食谱名称");
		return;
	}

	if (
		!newRecipe.value.calories ||
		isNaN(newRecipe.value.calories) ||
		newRecipe.value.calories <= 0
	) {
		ElMessage.warning("请输入有效的卡路里数值");
		return;
	}

	// 准备时间验证
	if (!newRecipe.value.time) {
		ElMessage.warning("请选择有效的准备时间");
		return;
	}

	// 创建新食谱对象
	const recipe = {
		id: Date.now(), // 使用时间戳作为唯一ID
		name: newRecipe.value.name,
		type: newRecipe.value.type,
		calories: Number(newRecipe.value.calories),
		time: newRecipe.value.time,
		favorite: newRecipe.value.favorite,
		details: newRecipe.value.details,
		ingredients: newRecipe.value.ingredients
			.split("\n")
			.filter((item) => item.trim()),
		steps: newRecipe.value.steps.split("\n").filter((item) => item.trim()),
		dishComposition: newRecipe.value.dishComposition,
	};

	// 添加到食谱列表
	myRecipes.value.push(recipe);
	addDialogVisible.value = false;
	ElMessage.success("食谱已添加");
};

// 删除食谱
const deleteRecipe = (id) => {
	ElMessageBox.confirm("确定要删除该食谱吗？", "提示", {
		confirmButtonText: "确定",
		cancelButtonText: "取消",
		type: "warning",
	})
		.then(() => {
			const index = myRecipes.value.findIndex((recipe) => recipe.id === id);
			if (index !== -1) {
				myRecipes.value.splice(index, 1);
				ElMessage.success("食谱已删除");
			}
		})
		.catch(() => {
			ElMessage.info("已取消删除");
		});
};
</script>

<template>
	<div class="my-recipe-container">
		<div class="recipe-header">
			<h2>我的食谱</h2>
			<div class="filter-section">
				<el-select
					v-model="recipeFilter"
					placeholder="筛选食谱"
					size="small"
					style="width: 150px; margin-right: 10px"
				>
					<el-option label="全部" value="all" />
					<el-option label="早餐" value="早餐" />
					<el-option label="午餐" value="午餐" />
					<el-option label="晚餐" value="晚餐" />
					<el-option label="加餐" value="加餐" />
				</el-select>
				<el-button type="primary" size="small" @click="openAddDialog">
					<span>➕</span>
					添加食谱
				</el-button>
			</div>
		</div>

		<!-- 食谱列表 -->
		<div class="recipe-grid">
			<el-card
				v-for="recipe in filteredRecipes"
				:key="recipe.id"
				class="recipe-card"
				:class="recipe.type"
			>
				<template #header>
					<div class="card-header">
						<span :class="`meal-icon ${recipe.type}`">
							{{
								recipe.type === "早餐"
									? "🥣"
									: recipe.type === "午餐"
									? "🍚"
									: recipe.type === "晚餐"
									? "🍱"
									: recipe.type === "加餐"
									? "🍪"
									: "🍴"
							}}
						</span>
						{{ recipe.name }}
						<el-button
							type="text"
							size="small"
							@click="toggleFavorite(recipe)"
						>
							<span
								:style="{
									color: recipe.favorite ? '#FFD700' : '#C0C4CC',
									fontSize: '20px',
								}"
							>
								{{ recipe.favorite ? "⭐" : "☆" }}
							</span>
						</el-button>
					</div>
				</template>
				<div class="recipe-items">
					<el-tag
						v-for="(item, index) in recipe.ingredients || ['暂无食材信息']"
						:key="index"
						:type="getTagType(recipe.type)"
					>
						{{ typeof item === "string" ? item : item }}
					</el-tag>
				</div>
				<div class="recipe-stats">
					<div class="stat-item">
						<span>🔥</span>
						<span>{{ recipe.calories }} kcal</span>
					</div>
					<div class="stat-item">
						<span>⏰</span>
						<span>{{ recipe.time }}</span>
					</div>
				</div>
				<div class="recipe-actions">
					<el-button type="text" size="small" @click="viewRecipeDetails(recipe)"
						>查看详情</el-button
					>
					<el-button type="danger" size="small" @click="deleteRecipe(recipe.id)"
						>删除食谱</el-button
					>
				</div>
			</el-card>
		</div>

		<!-- 空数据提示 -->
		<el-empty
			v-if="filteredRecipes.length === 0"
			:description="loadingFailed ? '暂未找到我的食谱' : '暂无食谱'"
		></el-empty>
	</div>

	<!-- 食谱详情对话框 -->
	<el-dialog
		v-model="detailDialogVisible"
		:title="selectedRecipe ? selectedRecipe.name : '食谱详情'"
		width="70%"
		:style="{ minWidth: '600px' }"
		top="8%"
		body-class="recipe-detail-dialog"
		draggable
	>
		<div v-if="selectedRecipe" class="recipe-detail-container">
			<!-- 头部信息 -->
			<div class="detail-header-section">
				<el-tag
					:type="
						selectedRecipe.type === '早餐'
							? 'warning'
							: selectedRecipe.type === '午餐'
							? 'success'
							: selectedRecipe.type === '晚餐'
							? 'primary'
							: 'info'
					"
					size="large"
					class="type-tag"
				>
					{{ selectedRecipe.type }}
				</el-tag>
        <el-icon
						:class="
							selectedRecipe.favorite
								? 'favorite-icon active'
								: 'favorite-icon'
						"
						@click="toggleFavorite(selectedRecipe)"
						title="点击切换收藏状态"
					>
						<Star />
        </el-icon>
			</div>

      <el-card shadow="hover" class="stat-card" :body-style="{display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', gap: '16px', padding: '24px 32px', background: '#f8f9fa', borderRadius: '8px'}">
					<div class="stat-label">🔥🔥 总卡路里</div>
					<div class="stat-text">
						<div class="stat-value">{{ selectedRecipe.calories }} kcal</div>
					</div>
				</el-card>
        <el-card shadow="hover" class="stat-card" :body-style="{display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', gap: '16px', padding: '24px 32px', background: '#f8f9fa', borderRadius: '8px'}">
					<div class="stat-label">⏰ 准备时间</div>
					<div class="stat-text">
						<div class="stat-value">{{ selectedRecipe.time || '00:00:00' }}</div>
					</div>
				</el-card>
			<!-- 核心信息卡片 -->
			<!-- <div class="detail-cards-section">
				<el-card shadow="hover" class="stat-card" :body-style="{display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', gap: '16px', padding: '24px 32px', background: '#f8f9fa', borderRadius: '8px'}">
					<div class="stat-label">🔥🔥 总卡路里</div>
					<div class="stat-text">
						<div class="stat-value">{{ selectedRecipe.calories }} kcal</div>
					</div>
				</el-card>
        <el-card shadow="hover" class="stat-card" :body-style="{display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', gap: '16px', padding: '24px 32px', background: '#f8f9fa', borderRadius: '8px'}">
					<div class="stat-label">⏰ 准备时间</div>
					<div class="stat-text">
						<div class="stat-value">{{ selectedRecipe.time || '00:00:00' }}</div>
					</div>
				</el-card>
			</div> -->

			<!-- 食谱详情 -->
			<el-card shadow="hover" :body-style="{ padding: '28px 32px', background: '#f8f9fa', borderRadius: '8px', marginTop: '24px' }">
				<h4 class="section-title">
					<el-icon class="section-icon">📝</el-icon>
					食谱详情
				</h4>
				<div class="detail-content">
					{{
						selectedRecipe.details ||
						"这是一个健康美味的" +
							selectedRecipe.type +
							"食谱，营养均衡，味道鲜美。"
					}}
				</div>
			</el-card>

			<!-- 菜品组成 -->
			<el-card shadow="hover" :body-style="{ padding: '28px 32px', background: '#f8f9fa', borderRadius: '8px', marginTop: '24px' }">
				<h4 class="section-title">
					<el-icon class="section-icon">🍽️</el-icon>
					菜品组成
				</h4>
				<div class="dish-composition">
					<el-collapse v-model="activeDishName" accordion class="dish-collapse">
						<el-collapse-item
							v-for="(dish, index) in selectedRecipe.dishComposition || [
								{ name: '空', ingredients: ['空'] },
							]"
							:key="index"
							:title="dish.name"
							:name="dish.name"
						>
							<div class="dish-ingredients">
								<el-tag
									v-for="(ingredient, ingIdx) in dish.ingredients"
									:key="ingIdx"
									type="primary"
									effect="plain"
									size="small"
								>
									{{ ingredient }}
								</el-tag>
							</div>
						</el-collapse-item>
					</el-collapse>
				</div>
			</el-card>

			<!-- 主要食材 -->
			<el-card shadow="hover" :body-style="{ padding: '28px 32px', background: '#f8f9fa', borderRadius: '8px', marginTop: '24px' }">
				<h4 class="section-title">
					<el-icon class="section-icon">🥬</el-icon>
					主要食材
				</h4>
				<div class="ingredient-grid">
					<el-tag
						v-for="(ingredient, index) in selectedRecipe.ingredients || [
							'鸡蛋',
							'牛奶',
							'燕麦',
							'水果',
							'蜂蜜',
							'苹果',
							'香蕉',
						]"
						:key="index"
						type="info"
						effect="light"
						size="large"
						class="ingredient-tag"
					>
						{{ ingredient }}
					</el-tag>
				</div>
			</el-card>

			<!-- 烹饪步骤 -->
			<el-card shadow="hover" :body-style="{ padding: '28px 32px', background: '#f8f9fa', borderRadius: '8px', marginTop: '24px' }">
				<h4 class="section-title">
					<el-icon class="section-icon">📋</el-icon>
					烹饪步骤
				</h4>
				<el-timeline class="cooking-steps">
					<el-timeline-item
						v-for="(step, index) in selectedRecipe.steps || [
							'这是一个健康美味的' + selectedRecipe.type + '食谱',
							'可以根据个人口味调整食材用量',
						]"
						:key="index"
					>
						<el-card shadow="never" :border="false">
							{{ step }}
						</el-card>
					</el-timeline-item>
				</el-timeline>
			</el-card>
		</div>

		<template #footer>
			<div class="dialog-footer">
				<el-button @click="detailDialogVisible = false">取消</el-button>
				<el-button type="primary" @click="detailDialogVisible = false"
					>关闭</el-button
				>
			</div>
		</template>
	</el-dialog>

	<!-- 添加食谱对话框 -->
	<el-dialog v-model="addDialogVisible" title="添加新食谱" width="500px" top="10%">
		<div class="add-recipe-form">
			<el-form :model="newRecipe" label-width="80px" status-icon>
				<el-form-item label="名称" prop="name" required>
					<el-input v-model="newRecipe.name" placeholder="请输入食谱名称" />
				</el-form-item>

				<el-form-item label="类型" prop="type" required>
					<el-select v-model="newRecipe.type" style="width: 100%">
						<el-option label="早餐" value="早餐" />
						<el-option label="午餐" value="午餐" />
						<el-option label="晚餐" value="晚餐" />
						<el-option label="加餐" value="加餐" />
					</el-select>
				</el-form-item>

				<el-form-item label="卡路里" prop="calories" required>
					<el-input
						v-model="newRecipe.calories"
						type="number"
						placeholder="请输入卡路里"
					/>
				</el-form-item>

				<el-form-item label="准备时间" prop="time" required>
					<el-time-picker
						v-model="newRecipe.time"
						placeholder="选择准备时间"
						type="time"
						format="HH:mm:ss"
						value-format="HH:mm:ss"
						:is-range="false"
						style="width: 100%"
					/>
				</el-form-item>

				<el-form-item label="食谱详情" prop="details">
					<el-input
						v-model="newRecipe.details"
						type="textarea"
						:rows="3"
						placeholder="请输入食谱详情"
					/>
				</el-form-item>

				<el-form-item label="食谱组成" prop="ingredients">
					<el-input
						v-model="newRecipe.ingredients"
						type="textarea"
						:rows="3"
						placeholder="请输入主要食材，每行一个"
					/>
				</el-form-item>

				<el-form-item label="烹饪步骤" prop="steps">
					<el-input
						v-model="newRecipe.steps"
						type="textarea"
						:rows="4"
						placeholder="请输入烹饪步骤，每行一个"
					/>
				</el-form-item>

				<el-form-item label="收藏">
					<el-switch v-model="newRecipe.favorite" />
				</el-form-item>
			</el-form>
		</div>
		<template #footer>
			<span class="dialog-footer">
				<el-button @click="addDialogVisible = false">取消</el-button>
				<el-button type="primary" @click="saveNewRecipe">确定</el-button>
			</span>
		</template>
	</el-dialog>
</template>

<style lang="less">
.my-recipe-container {
	padding: 24px;
	min-height: 100vh;
	background: #f5f7fa;

	.recipe-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 24px;

		h2 {
			font-size: 32px;
			margin: 0;
			color: #333;
		}

		.filter-section {
			display: flex;
			align-items: center;
		}
	}

	.recipe-grid {
		display: grid;
		grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
		gap: 20px;
		width: 100%;
	}

	.recipe-card {
		background: rgba(255, 255, 255, 0.95) !important;
		border-radius: 16px !important;
		box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
		transition: all 0.3s ease;

		&:hover {
			transform: translateY(-4px);
			box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
		}

		.card-header {
			display: flex;
			align-items: center;
			gap: 14px;
			font-size: 20px;
			font-weight: 700;

			.meal-icon {
				font-size: 28px;
				padding: 10px;
				background-color: rgba(0, 0, 0, 0.05);
				border-radius: 50%;
				box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
			}
		}

		.recipe-items {
			margin: 20px 0;
			display: flex;
			flex-wrap: wrap;
			gap: 10px;
		}

		.recipe-stats {
			display: flex;
			gap: 20px;

			.stat-item {
				display: flex;
				align-items: center;
				gap: 5px;
				color: #666;
			}
		}

		.recipe-actions {
			text-align: right;
			margin-top: 20px;
		}
	}

	/* 添加食谱表单样式 */
	.add-recipe-form {
		padding: 20px 0;

		.el-form {
			max-width: 400px;
			margin: 0 auto;
		}

		.el-form-item {
			margin-bottom: 20px;
		}
	}

	/* 不同类型食谱卡片的样式 */
	.recipe-card {
		&.早餐 {
			border-left: 4px solid #ffc107;

			.meal-icon {
				color: #ffc107;
			}
		}

		&.午餐 {
			border-left: 4px solid #4caf50;

			.meal-icon {
				color: #4caf50;
			}
		}

		&.晚餐 {
			border-left: 4px solid #2196f3;

			.meal-icon {
				color: #2196f3;
			}
		}

		&.加餐 {
			border-left: 4px solid #1e88e5;

			.meal-icon {
				color: #1e88e5;
			}
		}
	}

	/* 自定义标签颜色和交互 */
	:deep(.el-tag) {
		transition: all 0.3s ease;
		cursor: pointer;

		&:hover {
			transform: translateY(-2px);
			box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
		}
	}

	:deep(.el-tag--warning) {
		background-color: #fff3e0;
		color: #f57c00;
	}

	:deep(.el-tag--success) {
		background-color: #e8f5e9;
		color: #388e3c;
	}

	:deep(.el-tag--primary) {
		background-color: #e3f2fd;
		color: #1976d2;
	}

	:deep(.el-tag--info) {
		background-color: #e1f5fe;
		color: #0288d1;
	}

	:deep(.el-tag--purple) {
		background-color: #f3e5f5;
		color: #7b1fa2;
	}
	:deep(.el-tag--blue) {
		background-color: #e3f2fd;
		color: #1565c0;
	}

  .recipe-detail-dialog {
    padding: 24px;
    background-color: #f5f7fa;
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto,
      "Helvetica Neue", Arial, sans-serif;
  }

  .detail-header-section {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;
  }

  .favorite-icon {
    font-size: 24px;
    cursor: pointer;
    transition: all 0.3s ease;
    color: #C0C4CC;
  }

  .favorite-icon.active {
    color: #FFD700;
    animation: pulse 0.5s ease;
  }

  .favorite-icon:hover {
    transform: scale(1.1);
  }

  @keyframes pulse {
    0% { transform: scale(1); }
    50% { transform: scale(1.2); }
    100% { transform: scale(1); }
  }

}
</style>
