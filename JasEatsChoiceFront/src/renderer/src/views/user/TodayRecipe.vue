<script setup>
import { ref, computed, onMounted } from "vue";
import { ArrowDown } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import { API_CONFIG } from "../../config";
import axios from "axios";

// 今日食谱数据
const todayRecipes = ref([]);

// 营养摄入数据
const nutritionData = ref({
	calories: 0,
	protein: 0,
	carbs: 0,
	fat: 0,
});

// 筛选条件
const filters = ref({
	mealType: "all",
});

// 加载今日食谱数据
const loadTodayRecipes = () => {
	axios
		.get(API_CONFIG.baseURL + API_CONFIG.recipe.today)
		.then((response) => {
			if (
				response.data.data &&
				response.data.data.recipes &&
				response.data.data.recipes.length > 0
			) {
				// console.log('加载今日食谱成功:', response.data.data.recipes);
				// 确保所有食谱都有items数组
				todayRecipes.value = response.data.data.recipes.map((recipe) => ({
					...recipe,
					items: recipe.items || [],
				}));
				nutritionData.value = response.data.data.nutrition || {
					calories: 0,
					protein: 0,
					carbs: 0,
					fat: 0,
				};
			} else {
				// 后端没有返回数据
				todayRecipes.value = [];
				nutritionData.value = { calories: 0, protein: 0, carbs: 0, fat: 0 };
			}
		})
		.catch((error) => {
			console.error("加载今日食谱失败:", error);
			// 请求失败时，也显示没有数据
			todayRecipes.value = [];
			nutritionData.value = { calories: 0, protein: 0, carbs: 0, fat: 0 };
			ElMessage.error("加载今日食谱失败");
		});
};

// 组件挂载时加载数据
onMounted(() => {
	loadTodayRecipes();
});

// 默认使用一列布局
const layoutType = ref("one-column");

// 获取标签类型
const getTagType = (type) => {
	switch (type) {
		case "breakfast":
			return "warning";
		case "lunch":
			return "success";
		case "dinner":
			return "primary";
		case "afternoon_tea":
		case "tea":
			return "purple";
		case "night_snack":
		case "snack":
			return "blue";
		case "morning_snack":
		case "brunch":
			return "orange";
		case "supper":
		case "midnight_snack":
			return "cyan";
		case "health_snack":
		case "fitness_meal":
			return "green";
		case "dessert":
		case "sweet":
			return "pink";
		case "soup":
		case "porridge":
			return "teal";
		case "salad":
		case "vegetable":
			return "success";
		case "meat":
		case "protein":
			return "brown";
		default:
			return "info";
	}
};

// 模态框状态
const detailDialogVisible = ref(false);
const replaceDialogVisible = ref(false);
const addDishVisible = ref(false);

// 当前选中的食谱和菜品
const selectedRecipe = ref(null);
const selectedDish = ref(null);

// 自定义菜品
const showCustomDishInput = ref(false);
const customDishName = ref("");

// 新菜品输入
const newDish = ref({
	name: "",
	ingredients: [], // 食材列表
});

// 食材输入
const newIngredient = ref("");

// 商家列表和选中商家
const merchants = ref([
	{
		id: 1,
		name: "健康餐厅",
		dishes: [
			{ id: 1, name: "有机蔬菜沙拉", nutrition: "120kcal/份" },
			{ id: 2, name: "烤三文鱼", nutrition: "280kcal/份" },
		],
	},
	{
		id: 2,
		name: "健身餐吧",
		dishes: [
			{ id: 3, name: "鸡胸肉盖饭", nutrition: "450kcal/份" },
			{ id: 4, name: "糙米粥", nutrition: "180kcal/份" },
		],
	},
]);

const selectedMerchant = ref(null);
const merchantDishes = ref([]); // 选中商家的菜品
const selectedMerchantDishes = ref([]); // 选中的导入菜品

// 导入商家菜品对话框
const importMerchantDishVisible = ref(false);

// 订单列表
const orders = ref([
	{
		id: 1,
		orderNo: "ORDER_001",
		totalPrice: 89.5,
		dishes: [
			{ name: "宫保鸡丁", nutrition: "250kcal/份" },
			{ name: "清炒西兰花", nutrition: "120kcal/份" },
			{ name: "米饭", nutrition: "110kcal/碗" },
		],
	},
	{
		id: 2,
		orderNo: "ORDER_002",
		totalPrice: 68.0,
		dishes: [
			{ name: "番茄鸡蛋面", nutrition: "320kcal/份" },
			{ name: "凉拌黄瓜", nutrition: "80kcal/份" },
		],
	},
]);

// 导入订单对话框
const importOrderVisible = ref(false);
const selectedOrder = ref(null);

// 替换菜品列表
const replacementDishes = ref([
	{ id: 1, name: "全麦面包", type: "breakfast", nutrition: "247kcal/片" },
	{ id: 2, name: "蒸南瓜", type: "breakfast", nutrition: "26kcal/100g" },
	{ id: 3, name: "烤鸡胸肉", type: "lunch", nutrition: "165kcal/100g" },
	{ id: 4, name: "西兰花", type: "lunch", nutrition: "34kcal/100g" },
	{ id: 5, name: "清蒸鱼", type: "dinner", nutrition: "105kcal/100g" },
	{ id: 6, name: "炒青菜", type: "dinner", nutrition: "15kcal/100g" },
]);

// 添加菜单
const addMenuVisible = ref(false);
const newMenu = ref({
	name: "",
	type: "",
	items: [],
});

// 查看详情
const viewRecipeDetails = (recipe) => {
	selectedRecipe.value = recipe;
	detailDialogVisible.value = true;
};

// 替换菜品
const replaceDish = (recipe, dish) => {
	selectedRecipe.value = recipe;
	selectedDish.value = dish;
	replaceDialogVisible.value = true;
};

// 确认替换菜品
const confirmReplaceDish = (newDish) => {
	if (selectedRecipe.value && selectedDish.value && selectedRecipe.value.items) {
		// 找到并替换菜品
		const index = selectedRecipe.value.items.indexOf(selectedDish.value);
		if (index !== -1) {
			selectedRecipe.value.items[index] = newDish.name;
			ElMessage.success("菜品已替换");
		}
		replaceDialogVisible.value = false;
		// 重置选中状态
		selectedRecipe.value = null;
		selectedDish.value = null;
	}
};

// 添加菜品
const addDish = (recipe) => {
	selectedRecipe.value = recipe;
	addDishVisible.value = true;
};

// 添加食材
const addIngredient = () => {
	if (newIngredient.value.trim()) {
		// 防止重复添加
		if (!newDish.value.ingredients.includes(newIngredient.value.trim())) {
			newDish.value.ingredients.push(newIngredient.value.trim());
		}
		newIngredient.value = ""; // 清空输入
	}
};

// 移除食材
const removeIngredient = (index) => {
	newDish.value.ingredients.splice(index, 1);
};

// 加载商家菜品
const loadMerchantDishes = () => {
	if (selectedMerchant.value) {
		merchantDishes.value = selectedMerchant.value.dishes;
		selectedMerchantDishes.value = []; // 重置选中菜品
	}
};

// 确认导入商家菜品
const confirmImportMerchantDishes = () => {
	if (selectedMerchantDishes.value.length > 0) {
		// 这里需要知道要导入到哪个食谱，需要先设置 selectedRecipe
		if (selectedRecipe.value) {
			selectedMerchantDishes.value.forEach((dish) => {
				// 将商家菜品转换为食谱需要的格式
				const recipeDish = {
					name: dish.name,
					ingredients: [], // 商家菜品默认没有食材，用户可以后续添加
				};
				selectedRecipe.value.items.push(recipeDish);
			});
			ElMessage.success(`成功导入 ${selectedMerchantDishes.value.length} 道菜品`);
			importMerchantDishVisible.value = false;
			// 重置状态
			selectedMerchant.value = null;
			merchantDishes.value = [];
			selectedMerchantDishes.value = [];
		} else {
			ElMessage.error("请先选择要导入到的食谱");
		}
	} else {
		ElMessage.warning("请先选择要导入的菜品");
	}
};

// 确认从订单导入食谱
const confirmImportOrder = () => {
	if (selectedOrder.value) {
		// 创建新食谱
		const newRecipe = {
			id: Date.now(),
			name: `订单-${selectedOrder.value.orderNo}`,
			type: "dinner", // 默认类型，可根据实际情况调整
			items: selectedOrder.value.dishes.map((dish) => ({
				name: dish.name,
				ingredients: [],
			})),
		};

		// 添加到食谱列表
		todayRecipes.value.push(newRecipe);

		ElMessage.success("订单已成功导入为新食谱");
		importOrderVisible.value = false;
		selectedOrder.value = null;
	}
};

// 确认添加菜品
const confirmAddDish = () => {
	if (selectedRecipe.value && newDish.value.name.trim()) {
		// 验证菜品名称格式
		if (!isValidDishName(newDish.value.name)) {
			ElMessage.error("菜品名称只能包含中文、英文、数字和常见符号");
			return;
		}

		// 如果有食材，将菜品和食材一起保存
		const dishWithIngredients = {
			name: newDish.value.name,
			ingredients: [...newDish.value.ingredients],
		};

		selectedRecipe.value.items.push(dishWithIngredients);
		ElMessage.success("菜品已添加");
		addDishVisible.value = false;

		// 重置表单
		newDish.value = {
			name: "",
			ingredients: [],
		};
		newIngredient.value = "";

		selectedRecipe.value = null;
	} else {
		ElMessage.error("请输入菜品名称");
	}
};

// 验证菜品名称格式的函数
const isValidDishName = (name) => {
	// 允许中文、英文、数字、空格以及常见的标点符号
	const nameRegex = /^[\u4e00-\u9fa5a-zA-Z0-9\s\-\_\(\)\[\]\{\}\/\.\,，。！？；：]*$/;
	return nameRegex.test(name.trim());
};

// 删除菜品
const deleteDish = (recipe, dish) => {
	if (recipe && dish && recipe.items) {
		const index = recipe.items.indexOf(dish);
		if (index !== -1) {
			recipe.items.splice(index, 1);
			ElMessage.success("菜品已删除");
		}
	}
};

// 添加新菜单
const addNewMenu = () => {
	if (newMenu.value.name.trim() && newMenu.value.type.trim()) {
		const menu = {
			id: Date.now(), // 使用时间戳作为唯一ID
			name: newMenu.value.name.trim(),
			type: newMenu.value.type.trim().toLowerCase(),
			items: ["待添加菜品"], // 初始默认菜品
		};

		todayRecipes.value.push(menu);
		ElMessage.success("菜单已添加");

		// 重置表单
		newMenu.value = {
			name: "",
			type: "",
			items: [],
		};

		// 关闭模态框
		addMenuVisible.value = false;
	}
};

// 处理自定义菜品替换
const handleCustomDishReplacement = () => {
	if (customDishName.value.trim()) {
		// 验证菜品名称格式
		if (!isValidDishName(customDishName.value)) {
			ElMessage.error("菜品名称只能包含中文、英文、数字和常见符号");
			return;
		}

		confirmReplaceDish({
			name: customDishName.value.trim(),
			type: selectedRecipe.value.type,
		});
		customDishName.value = "";
	}
};

// 筛选后的食谱列表
const filteredRecipes = computed(() => {
	let filtered = [...todayRecipes.value];

	// 餐型筛选
	if (filters.value.mealType !== "all") {
		if (filters.value.mealType === "snack") {
			// 加餐包含所有零食类餐型
			filtered = filtered.filter((recipe) =>
				[
					"snack",
					"night_snack",
					"morning_snack",
					"afternoon_tea",
					"tea",
					"brunch",
					"midnight_snack",
				].includes(recipe.type)
			);
		} else {
			filtered = filtered.filter(
				(recipe) => recipe.type === filters.value.mealType
			);
		}
	}

	return filtered;
});
</script>

<template>
	<div class="today-recipe-container">
		<div class="recipe-header">
			<h2>今日食谱</h2>
			<div class="filter-container">
				<!-- 餐型筛选 -->
				<div class="meal-type-tabs">
					<el-button
						type="primary"
						:plain="filters.mealType !== 'all'"
						@click="filters.mealType = 'all'"
						size="small"
					>
						全部
					</el-button>
					<el-button
						type="primary"
						:plain="filters.mealType !== 'breakfast'"
						@click="filters.mealType = 'breakfast'"
						size="small"
					>
						早餐
					</el-button>
					<el-button
						type="primary"
						:plain="filters.mealType !== 'lunch'"
						@click="filters.mealType = 'lunch'"
						size="small"
					>
						午餐
					</el-button>
					<el-button
						type="primary"
						:plain="filters.mealType !== 'dinner'"
						@click="filters.mealType = 'dinner'"
						size="small"
					>
						晚餐
					</el-button>
					<el-button
						type="primary"
						:plain="filters.mealType !== 'snack'"
						@click="filters.mealType = 'snack'"
						size="small"
					>
						加餐
					</el-button>
				</div>
			</div>
		</div>

		<!-- 营养摄入统计 -->
		<el-card class="nutrition-card">
			<template #header>
				<div class="card-header">营养摄入统计</div>
			</template>
			<div class="nutrition-stats">
				<div class="stat-item">
					<div class="stat-label">卡路里</div>
					<div class="stat-value">{{ nutritionData.calories }} kcal</div>
				</div>
				<div class="stat-item">
					<div class="stat-label">蛋白质</div>
					<div class="stat-value">{{ nutritionData.protein }} g</div>
				</div>
				<div class="stat-item">
					<div class="stat-label">碳水化合物</div>
					<div class="stat-value">{{ nutritionData.carbs }} g</div>
				</div>
				<div class="stat-item">
					<div class="stat-label">脂肪</div>
					<div class="stat-value">{{ nutritionData.fat }} g</div>
				</div>
			</div>
		</el-card>

		<!-- 添加菜单按钮和布局切换 -->
		<div class="add-recipe-section">
			<el-button type="primary" size="small" @click="addMenuVisible = true">
				➕ 添加食谱
			</el-button>

			<el-button type="success" size="small" @click="importOrderVisible = true">
				➕ 从订单导入
			</el-button>
		</div>
		<!-- 食谱列表 -->
		<div :class="['recipe-list', layoutType]">
			<div v-if="filteredRecipes.length === 0" class="no-recipes-message">
				<el-empty description="今日没有食谱数据"></el-empty>
			</div>
			<el-card
				v-else
				v-for="recipe in filteredRecipes"
				:key="recipe.id"
				class="recipe-card"
				:class="recipe.type"
			>
				<template #header>
					<div class="card-header">
						<span :class="`meal-icon ${recipe.type}`">
							{{
								recipe.type === "breakfast"
									? "🥣"
									: recipe.type === "lunch"
									? "🍚"
									: recipe.type === "dinner"
									? "🍱"
									: recipe.type === "afternoon_tea" ||
									  recipe.type === "tea"
									? "🍵"
									: recipe.type === "night_snack" ||
									  recipe.type === "snack"
									? "🍪"
									: recipe.type === "morning_snack" ||
									  recipe.type === "brunch"
									? "🥐"
									: recipe.type === "supper" ||
									  recipe.type === "midnight_snack"
									? "🌙"
									: recipe.type === "health_snack" ||
									  recipe.type === "fitness_meal"
									? "💪"
									: recipe.type === "dessert" || recipe.type === "sweet"
									? "🍰"
									: recipe.type === "soup" || recipe.type === "porridge"
									? "🍲"
									: recipe.type === "salad" ||
									  recipe.type === "vegetable"
									? "🥗"
									: recipe.type === "meat" || recipe.type === "protein"
									? "🥩"
									: "🍴"
							}}
						</span>
						{{ recipe.name }}
					</div>
				</template>
				<div class="recipe-items">
					<el-tag
						v-for="(item, index) in recipe.items && recipe.items.length > 0
							? recipe.items
							: ['待添加菜品']"
						:key="index"
						:type="getTagType(recipe.type)"
					>
						{{ typeof item === "object" ? item.name : item }}
					</el-tag>
				</div>
				<div class="recipe-actions">
					<el-button type="text" size="small" @click="viewRecipeDetails(recipe)"
						>查看详情</el-button
					>
					<el-button type="text" size="small" @click="addDish(recipe)"
						>添加菜品</el-button
					>
					<el-button
						type="text"
						size="small"
						@click="
							selectedRecipe = recipe;
							importMerchantDishVisible = true;
						"
						>导入商家菜品</el-button
					>
					<el-dropdown trigger="click">
						<el-button type="text" size="small">
							替换菜品
							<el-icon class="el-icon--right"><ArrowDown /></el-icon>
						</el-button>
						<template #dropdown>
							<el-dropdown-menu>
								<el-dropdown-item
									v-for="dish in recipe.items || []"
									:key="dish"
									@click="replaceDish(recipe, dish)"
								>
									{{ typeof dish === "object" ? dish.name : dish }}
								</el-dropdown-item>
							</el-dropdown-menu>
						</template>
					</el-dropdown>
					<el-dropdown trigger="click">
						<el-button type="text" size="small">
							删除菜品
							<el-icon class="el-icon--right"><ArrowDown /></el-icon>
						</el-button>
						<template #dropdown>
							<el-dropdown-menu>
								<el-dropdown-item
									v-for="dish in recipe.items || []"
									:key="dish"
									@click="deleteDish(recipe, dish)"
								>
									{{ typeof dish === "object" ? dish.name : dish }}
								</el-dropdown-item>
							</el-dropdown-menu>
						</template>
					</el-dropdown>
				</div>
			</el-card>
		</div>
	</div>

	<!-- 查看详情对话框 -->
	<el-dialog
		v-model="detailDialogVisible"
		:title="selectedRecipe ? `${selectedRecipe.name} 详情` : '食谱详情'"
		width="600px"
		top="10%"
	>
		<div v-if="selectedRecipe" class="recipe-details">
			<div class="detail-item">
				<span class="detail-label">餐型:</span>
				<span class="detail-value">{{ selectedRecipe.name }}</span>
			</div>
			<div class="detail-item">
				<span class="detail-label">菜品:</span>
				<div class="detail-value">
					<el-tag
						v-for="item in selectedRecipe.items"
						:key="item"
						:type="getTagType(selectedRecipe.type)"
						style="margin-right: 10px; margin-bottom: 10px"
					>
						{{ item }}
					</el-tag>
				</div>
			</div>
			<div class="detail-item">
				<span class="detail-label">营养信息:</span>
				<div class="detail-value nutrition-info">
					<div class="nutrition-item">
						<span class="nutrition-label">卡路里:</span>
						<span class="nutrition-value"
							>{{ nutritionData.calories }} kcal</span
						>
					</div>
					<div class="nutrition-item">
						<span class="nutrition-label">蛋白质:</span>
						<span class="nutrition-value">{{ nutritionData.protein }} g</span>
					</div>
					<div class="nutrition-item">
						<span class="nutrition-label">碳水化合物:</span>
						<span class="nutrition-value">{{ nutritionData.carbs }} g</span>
					</div>
					<div class="nutrition-item">
						<span class="nutrition-label">脂肪:</span>
						<span class="nutrition-value">{{ nutritionData.fat }} g</span>
					</div>
				</div>
			</div>
		</div>
	</el-dialog>

	<!-- 替换菜品对话框 -->
	<el-dialog
		v-model="replaceDialogVisible"
		:title="selectedDish ? `替换 ${selectedDish}` : '替换菜品'"
		width="600px"
		top="10%"
	>
		<div v-if="selectedDish" class="replace-dish-container">
			<div class="current-dish">
				<span class="detail-label">当前菜品:</span>
				<span class="detail-value">{{ selectedDish }}</span>
			</div>

			<div class="available-dishes">
				<span class="detail-label">可选菜品:</span>
				<div class="dish-list">
					<el-card
						v-for="dish in replacementDishes"
						:key="dish.id"
						:class="dish.type"
						class="dish-card"
						@click="confirmReplaceDish(dish)"
					>
						<div class="dish-name">{{ dish.name }}</div>
						<div class="dish-nutrition">{{ dish.nutrition }}</div>
					</el-card>
				</div>
			</div>

			<el-divider />

			<div class="custom-dish-section">
				<el-button
					type="text"
					@click="showCustomDishInput = !showCustomDishInput"
				>
					{{ showCustomDishInput ? "使用预设菜品" : "自定义菜品" }}
				</el-button>

				<div v-if="showCustomDishInput" class="custom-dish-input">
					<el-input
						v-model="customDishName"
						placeholder="请输入自定义菜品名称"
						clearable
						style="margin-bottom: 10px"
					/>
					<el-button
						type="primary"
						size="small"
						@click="handleCustomDishReplacement"
						:disabled="!customDishName.trim()"
					>
						确认替换为自定义菜品
					</el-button>
				</div>
			</div>
		</div>
	</el-dialog>

	<!-- 添加菜品对话框 -->
	<el-dialog
		v-model="addDishVisible"
		:title="selectedRecipe ? `为${selectedRecipe.name}添加菜品` : '添加菜品'"
		width="500px"
		top="20%"
	>
		<div v-if="selectedRecipe" class="add-dish-form">
			<el-form class="form-container" ref="dishForm">
				<el-form-item
					label="菜品名称"
					prop="name"
					class="is-required"
				>
					<el-input
						v-model="newDish.name"
						placeholder="请输入新菜品名称"
						@blur="() => {
							if (newDish.name.trim() && !isValidDishName(newDish.name)) {
								ElMessage.error('菜品名称只能包含中文、英文、数字和常见符号');
							}
						}"
					/>
				</el-form-item>

				<!-- 食材输入区域 -->
				<el-form-item label="食材（非必选）">
					<div class="ingredients-input">
						<el-input
							v-model="newIngredient"
							placeholder="请输入食材名称"
							@keyup.enter="addIngredient"
						/>
						<el-button type="primary" size="small" @click="addIngredient">
							添加食材
						</el-button>
					</div>

					<!-- 食材列表 -->
					<div class="ingredients-list" v-if="newDish.ingredients.length > 0">
						<el-tag
							v-for="(ingredient, index) in newDish.ingredients"
							:key="index"
							closable
							@close="removeIngredient(index)"
						>
							{{ ingredient }}
						</el-tag>
					</div>
				</el-form-item>
			</el-form>
		</div>

		<template #footer>
			<el-button @click="addDishVisible = false">取消</el-button>
			<el-button type="primary" @click="confirmAddDish"> 确定 </el-button>
		</template>
	</el-dialog>

	<!-- 导入商家菜品对话框 -->
	<el-dialog
		v-model="importMerchantDishVisible"
		title="导入商家菜品"
		width="600px"
		top="10%"
	>
		<div class="import-merchant-dish-container">
			<!-- 商家选择 -->
			<el-form-item label="选择商家">
				<el-select
					v-model="selectedMerchant"
					placeholder="请选择商家"
					style="width: 100%"
					@change="loadMerchantDishes"
				>
					<el-option
						v-for="merchant in merchants"
						:key="merchant.id"
						:label="merchant.name"
						:value="merchant"
					/>
				</el-select>
			</el-form-item>

			<!-- 菜品列表 -->
			<div v-if="merchantDishes.length > 0" class="merchant-dishes-list">
				<h4>{{ selectedMerchant?.name }} 的菜品</h4>
				<el-checkbox-group v-model="selectedMerchantDishes">
					<div v-for="dish in merchantDishes" :key="dish.id" class="dish-item">
						<el-checkbox :label="dish">{{ dish.name }}</el-checkbox>
						<span class="dish-nutrition">{{ dish.nutrition }}</span>
					</div>
				</el-checkbox-group>
			</div>
		</div>

		<template #footer>
			<el-button @click="importMerchantDishVisible = false">取消</el-button>
			<el-button type="primary" @click="confirmImportMerchantDishes">
				导入选中菜品
			</el-button>
		</template>
	</el-dialog>

	<!-- 添加食谱对话框 -->
	<el-dialog v-model="addMenuVisible" title="添加新食谱" width="400px" top="20%">
		<el-form :model="newMenu" class="add-menu-form">
			<el-form-item label="食谱名称" prop="name" required>
				<el-input
					v-model="newMenu.name"
					placeholder="请输入食谱名称（如：下午茶、夜宵）"
				/>
			</el-form-item>

			<el-form-item label="类型标识" prop="type" required>
				<el-select
					v-model="newMenu.type"
					placeholder="请选择类型标识"
					style="width: 100%"
				>
					<el-option label="早餐 (breakfast)" value="breakfast" />
					<el-option label="午餐 (lunch)" value="lunch" />
					<el-option label="晚餐 (dinner)" value="dinner" />
					<el-option label="下午茶 (afternoon_tea)" value="afternoon_tea" />
					<el-option label="夜宵 (night_snack)" value="night_snack" />
					<el-option label="上午加餐 (morning_snack)" value="morning_snack" />
					<el-option label="早午餐 (brunch)" value="brunch" />
					<el-option label="宵夜 (midnight_snack)" value="midnight_snack" />
				</el-select>
			</el-form-item>
		</el-form>

		<template #footer>
			<el-button @click="addMenuVisible = false">取消</el-button>
			<el-button
				type="primary"
				@click="addNewMenu"
				:disabled="!newMenu.name.trim() || !newMenu.type.trim()"
			>
				确定
			</el-button>
		</template>
	</el-dialog>
</template>

<style scoped lang="less">
.today-recipe-container {
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

		.meal-type-tabs {
			gap: 10px;
		}
	}

	.nutrition-card {
		margin-bottom: 24px;
		background: rgba(255, 255, 255, 0.95) !important;
		border-radius: 16px !important;
		box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);

		.card-header {
			font-size: 18px;
			font-weight: 700;
		}

		.nutrition-stats {
			display: flex;
			justify-content: space-around;
			padding: 20px;

			.stat-item {
				text-align: center;
				min-width: 120px;

				.stat-label {
					font-size: 14px;
					color: #666;
					margin-bottom: 8px;
				}

				.stat-value {
					font-size: 28px;
					font-weight: 700;
					color: #ff6b6b;
					margin-bottom: 12px;
				}
			}
		}
	}

	.recipe-list {
		display: flex;
		flex-direction: column;
		width: 100%;
		gap: 20px;

		.recipe-card {
			flex: 1 1 100%;
			max-width: 100%;
			min-width: 280px;
			box-sizing: border-box;
			margin: 0;
		}
	}

	.recipe-card {
		margin-bottom: 24px;
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

		.recipe-actions {
			text-align: right;
			margin-top: 20px;
		}

		&.breakfast {
			border-left: 4px solid #ffc107;

			.meal-icon.breakfast {
				color: #ffc107;
			}
		}

		&.lunch {
			border-left: 4px solid #4caf50;

			.meal-icon.lunch {
				color: #4caf50;
			}
		}

		&.dinner {
			border-left: 4px solid #2196f3;

			.meal-icon.dinner {
				color: #2196f3;
			}
		}

		// 自定义菜单类型样式
		&.afternoon_tea,
		&.tea {
			border-left: 4px solid #9c27b0;

			.meal-icon.afternoon_tea,
			.meal-icon.tea {
				color: #9c27b0;
				font-size: 24px;
			}
		}

		&.night_snack,
		&.snack {
			border-left: 4px solid #1e88e5;

			.meal-icon.night_snack,
			.meal-icon.snack {
				color: #1e88e5;
				font-size: 24px;
			}
		}

		&.morning_snack,
		&.brunch {
			border-left: 4px solid #ff9800;

			.meal-icon.morning_snack,
			.meal-icon.brunch {
				color: #ff9800;
				font-size: 24px;
			}
		}

		&.supper,
		&.midnight_snack {
			border-left: 4px solid #00bcd4;

			.meal-icon.supper,
			.meal-icon.midnight_snack {
				color: #00bcd4;
				font-size: 24px;
			}
		}

		&.health_snack,
		&.fitness_meal {
			border-left: 4px solid #4caf50;

			.meal-icon.health_snack,
			.meal-icon.fitness_meal {
				color: #4caf50;
				font-size: 24px;
			}
		}

		&.dessert,
		&.sweet {
			border-left: 4px solid #e91e63;

			.meal-icon.dessert,
			.meal-icon.sweet {
				color: #e91e63;
				font-size: 24px;
			}
		}

		&.soup,
		&.porridge {
			border-left: 4px solid #009688;

			.meal-icon.soup,
			.meal-icon.porridge {
				color: #009688;
				font-size: 24px;
			}
		}

		&.salad,
		&.vegetable {
			border-left: 4px solid #8bc34a;

			.meal-icon.salad,
			.meal-icon.vegetable {
				color: #8bc34a;
				font-size: 24px;
			}
		}

		&.meat,
		&.protein {
			border-left: 4px solid #795548;

			.meal-icon.meat,
			.meal-icon.protein {
				color: #795548;
				font-size: 24px;
			}
		}

		// 默认样式
		&.info {
			border-left: 4px solid #00bcd4;

			.meal-icon.info {
				color: #00bcd4;
				font-size: 24px;
			}
		}
	}

	.add-recipe-section {
		display: flex;
		justify-content: flex-start;
		align-items: center;
		margin-bottom: 24px;

		.el-button {
			border-radius: 24px !important;
			padding: 10px 24px !important;
			font-weight: 600 !important;
		}
	}
}

// 自定义标签颜色和交互
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

// 食材输入区域样式
.ingredients-input {
	display: flex;
	gap: 10px;
	margin-bottom: 15px;
	align-items: center;

	.el-input {
		flex: 1;
	}
}

.ingredients-list {
	display: flex;
	flex-wrap: wrap;
	gap: 10px;
	width: 100%;
}

// 食谱详情对话框样式
.recipe-details {
	.detail-item {
		margin-bottom: 20px;

		.detail-label {
			font-weight: bold;
			font-size: 14px;
			color: #333;
			margin-right: 10px;
		}

		.detail-value {
			font-size: 14px;
			color: #666;
		}

		.nutrition-info {
			display: flex;
			flex-wrap: wrap;
			gap: 20px;

			.nutrition-item {
				margin-bottom: 10px;

				.nutrition-label {
					font-weight: bold;
				}

				.nutrition-value {
					color: #ff6b6b;
					font-weight: bold;
					margin-left: 5px;
				}
			}
		}
	}
}

// 替换菜品对话框样式
.replace-dish-container {
	.current-dish {
		margin-bottom: 20px;

		.detail-label {
			font-weight: bold;
		}

		.detail-value {
			color: #ff6b6b;
			font-weight: bold;
			margin-left: 10px;
		}
	}

	.available-dishes {
		.detail-label {
			font-weight: bold;
			display: block;
			margin-bottom: 15px;
		}

		.dish-list {
			display: grid;
			grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
			gap: 15px;
		}

		.dish-card {
			cursor: pointer;
			border-left: 4px solid #ccc;
			transition: all 0.3s ease;

			&:hover {
				transform: translateY(-5px);
				box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
			}

			.dish-name {
				font-size: 16px;
				font-weight: bold;
				margin-bottom: 5px;
			}

			.dish-nutrition {
				font-size: 14px;
				color: #999;
			}

			&.breakfast {
				border-left-color: #ffc107;
			}

			&.lunch {
				border-left-color: #4caf50;
			}

			&.dinner {
				border-left-color: #2196f3;
			}
		}
	}
}
</style>
