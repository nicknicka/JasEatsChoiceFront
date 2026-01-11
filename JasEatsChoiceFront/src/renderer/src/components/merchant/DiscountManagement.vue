<script setup>
import { ref, computed, onMounted, nextTick } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import api from "../../utils/api.js";
import { API_CONFIG } from "../../config/index.js";

const props = defineProps({
	merchantId: {
		type: String,
		required: true,
	},
});

// 优惠活动列表 - 初始化为空，等待后端数据
const discounts = ref([]);

// 优惠管理对话框
const discountDialogVisible = ref(false);
const currentDiscountForm = ref({});
const isEditingDiscount = ref(false);

// 优惠类型单位计算
const discountUnit = computed(() => {
	const type = currentDiscountForm.value?.type;
	if (type === "满减") return "元";
	if (type === "折扣") return "%";
	return "";
});

// 批量操作选中的优惠
const selectedDiscounts = ref([]);

// 处理表格选择变化
const handleSelectionChange = (selection) => {
	selectedDiscounts.value = selection;
};

// 优惠表单验证规则
const discountRules = {
	name: [
		{ required: true, message: "请输入优惠名称", trigger: "blur" },
		{ min: 2, max: 30, message: "长度在 2 到 30 个字符", trigger: "blur" },
	],
	type: [{ required: true, message: "请选择优惠类型", trigger: "change" }],
	discountValue: [{ required: true, message: "请输入优惠力度", trigger: "blur" }],
	description: [
		{ required: true, message: "请输入优惠描述", trigger: "blur" },
		{ min: 5, max: 200, message: "长度在 5 到 200 个字符", trigger: "blur" },
	],
	validityType: [{ required: true, message: "请选择有效期类型", trigger: "change" }],
};

// 批量删除优惠
const batchDeleteDiscounts = () => {
	if (selectedDiscounts.value.length === 0) {
		ElMessage.warning("请先选择要删除的优惠");
		return;
	}

	const discountIds = selectedDiscounts.value.map((discount) => discount.id);

	ElMessageBox.confirm(
		`确定要删除选中的 ${selectedDiscounts.value.length} 个优惠活动吗？`,
		"批量删除",
		{
			confirmButtonText: "确定",
			cancelButtonText: "取消",
			type: "warning",
		}
	)
		.then(() => {
			// 调用后端API批量删除优惠 - 使用新的批量删除endpoint
			api.delete(
				`${API_CONFIG.merchant.discounts.replace(
					"{merchantId}",
					props.merchantId
				)}/batch`,
				{
					params: { ids: discountIds.join(",") }, // 使用查询参数发送ID列表
				}
			)
				.then((response) => {
					if (response && response.success) {
						// 更新本地数据
						discounts.value = discounts.value.filter(
							(discount) => !discountIds.includes(discount.id)
						);
						selectedDiscounts.value = [];
						ElMessage.success("优惠活动批量删除成功");
					} else {
						ElMessage.error(response?.message || "批量删除优惠活动失败");
					}
				})
				.catch((error) => {
					console.error("批量删除优惠活动失败:", error);
					ElMessage.error("批量删除优惠活动失败");
				});
		})
		.catch(() => {
			ElMessage.info("已取消删除");
		});
};

// 批量更新优惠状态
const batchUpdateStatus = (status) => {
	if (selectedDiscounts.value.length === 0) {
		ElMessage.warning("请先选择要操作的优惠");
		return;
	}

	const statusText = status === "active" ? "启用" : "禁用";
	const discountIds = selectedDiscounts.value.map((discount) => discount.id);

	ElMessageBox.confirm(
		`确定要批量${statusText}选中的 ${selectedDiscounts.value.length} 个优惠活动吗？`,
		`批量${statusText}`,
		{
			confirmButtonText: "确定",
			cancelButtonText: "取消",
			type: "warning",
		}
	)
		.then(() => {
			// 调用后端API批量更新状态 - 使用新的批量更新endpoint
			api.put(
				`${API_CONFIG.merchant.discounts.replace(
					"{merchantId}",
					props.merchantId
				)}/batch`,
				{
					discountIds,
					status,
				}
			)
				.then((response) => {
					if (response && response.success) {
						// 更新本地数据
						discounts.value.forEach((discount) => {
							if (discountIds.includes(discount.id)) {
								discount.status = status;
							}
						});
						selectedDiscounts.value = [];
						ElMessage.success(`优惠活动批量${statusText}成功`);
					} else {
						ElMessage.error(
							response?.message || `批量${statusText}优惠活动失败`
						);
					}
				})
				.catch((error) => {
					console.error(`批量${statusText}优惠状态失败:`, error);
					ElMessage.error(`批量${statusText}优惠状态失败`);
				});
		})
		.catch(() => {
			ElMessage.info("已取消操作");
		});
};

// 打开优惠管理对话框
const openDiscountDialog = (discount = null) => {
	discountDialogVisible.value = true;
	if (discount) {
		// 编辑模式
		isEditingDiscount.value = true;
		currentDiscountForm.value = { ...discount };
	} else {
		// 新增模式
		isEditingDiscount.value = false;
		currentDiscountForm.value = {
			name: "",
			type: "满减",
			discountValue: 0,
			minAmount: 0,
			limitPerUser: 1,
			validityType: "permanent",
			validityPeriod: null,
			validDays: 30,
			usageNotes: "",
			description: "",
			status: "active",
		};
	}
};

// 保存优惠
const saveDiscount = () => {
	// 简单的表单验证
	if (
		!currentDiscountForm.value.name ||
		!currentDiscountForm.value.description
	) {
		ElMessage.error("请填写完整的优惠信息");
		return;
	}

	if (isEditingDiscount.value) {
		// 编辑模式 - 更新现有优惠 - 使用新的路由包含 discountId
		api.put(
			`${API_CONFIG.merchant.discounts.replace(
				"{merchantId}",
				props.merchantId
			)}/${currentDiscountForm.value.id}`,
			currentDiscountForm.value
		)
			.then((response) => {
				if (response && response.success) {
					// 更新本地数据
					const index = discounts.value.findIndex(
						(d) => d.id === currentDiscountForm.value.id
					);
					if (index !== -1) {
						discounts.value[index] = { ...currentDiscountForm.value };
					}
					ElMessage.success("优惠活动已更新");
					discountDialogVisible.value = false;
				} else {
					ElMessage.error(response?.message || "更新优惠活动失败");
				}
			})
			.catch((error) => {
				console.error("更新优惠活动失败:", error);
				ElMessage.error("更新优惠活动失败");
			});
	} else {
		// 新增模式 - 添加新优惠
		api.post(
			API_CONFIG.merchant.discounts.replace("{merchantId}", props.merchantId),
			currentDiscountForm.value
		)
			.then((response) => {
				if (response && response.success) {
					ElMessage.success("优惠活动已添加");
					discountDialogVisible.value = false;
					// 刷新优惠列表以确保数据格式一致
					fetchDiscounts();
				} else {
					ElMessage.error(response?.message || "添加优惠活动失败");
				}
			})
			.catch((error) => {
				console.error("添加优惠活动失败:", error);
				ElMessage.error("添加优惠活动失败");
			});
	}

	currentDiscountForm.value = {};
};

// 删除单个优惠
const deleteDiscount = (discount) => {
	ElMessageBox.confirm(
		`确定要删除优惠活动 "${discount.name}" 吗？`,
		"删除优惠",
		{
			confirmButtonText: "确定",
			cancelButtonText: "取消",
			type: "warning",
		}
	)
		.then(() => {
			// 调用后端API删除优惠
			api.delete(
				`${API_CONFIG.merchant.discounts.replace(
					"{merchantId}",
					props.merchantId
				)}/${discount.id}`
			)
				.then((response) => {
					if (response && response.success) {
						const index = discounts.value.findIndex(
							(d) => d.id === discount.id
						);
						if (index !== -1) {
							discounts.value.splice(index, 1);
						}
						ElMessage.success("优惠活动删除成功");
					}
				})
				.catch((error) => {
					console.error("删除优惠活动失败:", error);
					ElMessage.error("删除优惠活动失败");
				});
		})
		.catch(() => {
			ElMessage.info("已取消删除");
		});
};

// 切换优惠状态
const toggleDiscountStatus = (discount) => {
	const newStatus = discount.status === "active" ? "inactive" : "active";
	const statusText = newStatus === "active" ? "启用" : "禁用";

	ElMessageBox.confirm(
		`确定要${statusText}优惠活动 "${discount.name}" 吗？`,
		`${statusText}优惠`,
		{
			confirmButtonText: "确定",
			cancelButtonText: "取消",
			type: "warning",
		}
	)
		.then(() => {
			api.put(
				`${API_CONFIG.merchant.discounts.replace(
					"{merchantId}",
					props.merchantId
				)}/${discount.id}/status`,
				{ status: newStatus }
			)
				.then((response) => {
					if (response && response.success) {
						discount.status = newStatus;
						ElMessage.success(`优惠活动已${statusText}`);
					}
				})
				.catch((error) => {
					console.error("切换优惠状态失败:", error);
					ElMessage.error("切换优惠状态失败");
				});
		})
		.catch(() => {
			ElMessage.info("已取消操作");
		});
};

// 获取优惠类型标签颜色
const getDiscountTypeTag = (type) => {
	const typeMap = {
		满减: "danger",
		折扣: "warning",
		买赠: "success",
		特价: "primary",
	};
	return typeMap[type] || "";
};

// 格式化日期时间
const formatDateTime = (dateTime) => {
	if (!dateTime) return "-";
	const date = new Date(dateTime);
	const year = date.getFullYear();
	const month = String(date.getMonth() + 1).padStart(2, "0");
	const day = String(date.getDate()).padStart(2, "0");
	const hours = String(date.getHours()).padStart(2, "0");
	const minutes = String(date.getMinutes()).padStart(2, "0");
	return `${year}-${month}-${day} ${hours}:${minutes}`;
};

// 获取优惠活动列表
const fetchDiscounts = () => {
	api
		.get(API_CONFIG.merchant.discounts.replace("{merchantId}", props.merchantId))
		.then((response) => {
			if (response && response.success && response.data) {
				// 确保数字字段正确转换
				discounts.value = response.data.map((discount) => ({
					...discount,
					discountValue:
						discount.discountValue !== null &&
						discount.discountValue !== undefined
							? Number(discount.discountValue)
							: 0,
					minAmount:
						discount.minAmount !== null && discount.minAmount !== undefined
							? Number(discount.minAmount)
							: 0,
					limitPerUser: discount.limitPerUser || 1,
					usedCount: discount.usedCount || 0,
					validDays: discount.validDays || 30,
				}));
			} else {
				discounts.value = [];
			}
		})
		.catch((error) => {
			console.error("获取优惠活动列表失败:", error);
			discounts.value = [];
		});
};

// 表格滚动同步
const tableRef = ref(null);
let scrollSynced = false;

const syncScroll = () => {
	// 防止重复绑定事件
	if (scrollSynced) return;

	const table = tableRef.value?.$el;
	if (!table) return;

	const headerWrapper = table.querySelector('.el-table__header-wrapper');
	const bodyWrapper = table.querySelector('.el-table__body-wrapper');

	if (headerWrapper && bodyWrapper) {
		// 用于防止循环触发
		let isHeaderSyncing = false;
		let isBodySyncing = false;

		// 表头滚动时，同步表体
		const headerScrollHandler = () => {
			if (isHeaderSyncing) return;
			isBodySyncing = true;
			bodyWrapper.scrollLeft = headerWrapper.scrollLeft;
			// 使用 setTimeout 确保 scroll 事件处理完成后再重置标志
			setTimeout(() => {
				isBodySyncing = false;
			}, 0);
		};

		// 表体滚动时，同步表头
		const bodyScrollHandler = () => {
			if (isBodySyncing) return;
			isHeaderSyncing = true;
			headerWrapper.scrollLeft = bodyWrapper.scrollLeft;
			setTimeout(() => {
				isHeaderSyncing = false;
			}, 0);
		};

		headerWrapper.addEventListener('scroll', headerScrollHandler);
		bodyWrapper.addEventListener('scroll', bodyScrollHandler);

		// 表头拖动滚动功能
		let isDragging = false;
		let startX = 0;
		let bodyStartScroll = 0;
		let animationFrame = null;

		headerWrapper.style.cursor = 'grab';

		headerWrapper.addEventListener('mousedown', (e) => {
			// 只在左键点击时启用拖动
			if (e.button !== 0) return;

			isDragging = true;
			startX = e.pageX;
			bodyStartScroll = bodyWrapper.scrollLeft;
			headerWrapper.style.cursor = 'grabbing';
			e.preventDefault();
		});

		const endDrag = () => {
			if (isDragging) {
				isDragging = false;
				headerWrapper.style.cursor = 'grab';
				if (animationFrame) {
					cancelAnimationFrame(animationFrame);
					animationFrame = null;
				}
			}
		};

		headerWrapper.addEventListener('mouseleave', endDrag);
		headerWrapper.addEventListener('mouseup', endDrag);

		headerWrapper.addEventListener('mousemove', (e) => {
			if (!isDragging) return;

			if (animationFrame) {
				cancelAnimationFrame(animationFrame);
			}

			animationFrame = requestAnimationFrame(() => {
				const x = e.pageX;
				const walk = (x - startX) * 1.5; // 滚动速度倍数

				// 同时更新表头和表体的滚动位置
				const newScrollLeft = Math.max(0, bodyStartScroll - walk);
				headerWrapper.scrollLeft = newScrollLeft;
				bodyWrapper.scrollLeft = newScrollLeft;
			});
		});

		// 表头滚轮滚动支持
		headerWrapper.addEventListener('wheel', (e) => {
			// 只处理横向滚轮事件
			if (e.deltaY === 0 && e.deltaX !== 0) {
				e.preventDefault();
				const newScrollLeft = Math.max(
					0,
					Math.min(
						headerWrapper.scrollLeft + e.deltaX,
						headerWrapper.scrollWidth - headerWrapper.clientWidth
					)
				);
				headerWrapper.scrollLeft = newScrollLeft;
				bodyWrapper.scrollLeft = newScrollLeft;
			}
		}, { passive: false });

		scrollSynced = true;
	}
};

onMounted(() => {
	fetchDiscounts();
	// 等待 DOM 更新后同步滚动
	nextTick(() => {
		syncScroll();
	});
});
</script>

<template>
	<div class="discounts-section">
		<div class="discounts-header">
			<div class="discount-title">
				<h3 class="card-title">💰 优惠活动管理</h3>
				<div class="active-discounts">{{ discounts.length }}个活动</div>
			</div>
			<div class="discount-actions">
				<el-button type="primary" size="small" @click="openDiscountDialog()">
					<el-icon><Plus /></el-icon> 添加优惠
				</el-button>
				<el-button
					type="success"
					size="small"
					@click="batchUpdateStatus('active')"
					:disabled="selectedDiscounts.length === 0"
				>
					批量启用
				</el-button>
				<el-button
					type="warning"
					size="small"
					@click="batchUpdateStatus('inactive')"
					:disabled="selectedDiscounts.length === 0"
				>
					批量禁用
				</el-button>
				<el-button
					type="danger"
					size="small"
					@click="batchDeleteDiscounts()"
					:disabled="selectedDiscounts.length === 0"
				>
					批量删除
				</el-button>
			</div>
		</div>
		<div class="discounts-table-container">
			<el-table
				ref="tableRef"
				:data="discounts"
				:default-sort="{ prop: 'createTime', order: 'descending' }"
				@selection-change="handleSelectionChange"
				style="width: 100%"
				:row-style="{ height: '60px' }"
				:cell-style="{ padding: '8px' }"
				flexible
			>
				<el-table-column
					type="selection"
					width="55"
					align="center"
					fixed
				/>
				<el-table-column
					prop="name"
					label="优惠名称"
					min-width="120"
					show-overflow-tooltip
				/>
				<el-table-column prop="type" label="类型" width="75" align="center">
					<template #default="scope">
						<el-tag :type="getDiscountTypeTag(scope.row.type)" size="small">
							{{ scope.row.type }}
						</el-tag>
					</template>
				</el-table-column>
				<el-table-column label="优惠规则" min-width="130" show-overflow-tooltip>
					<template #default="scope">
						<div class="discount-rule">
							<template
								v-if="scope.row.type === '满减' && scope.row.discountValue"
							>
								<span class="rule-highlight"
									>满{{ scope.row.minAmount || 0 }}</span
								>
								<span class="rule-divider">减</span>
								<span class="rule-value">{{ scope.row.discountValue }}元</span>
							</template>
							<template
								v-else-if="scope.row.type === '折扣' && scope.row.discountValue"
							>
								<span class="rule-value">{{ scope.row.discountValue }}折</span>
							</template>
							<template
								v-else-if="scope.row.type === '特价' && scope.row.discountValue"
							>
								<span class="rule-value">{{ scope.row.discountValue }}元</span>
							</template>
							<template v-else>
								<span class="rule-empty">-</span>
							</template>
						</div>
					</template>
				</el-table-column>
				<el-table-column
					prop="description"
					label="优惠描述"
					min-width="150"
					show-overflow-tooltip
				/>
				<el-table-column label="使用情况" width="90" align="center">
					<template #default="scope">
						<div class="usage-stats">
							<div class="usage-item">
								<el-icon><User /></el-icon>
								<span>{{ scope.row.usedCount || 0 }}</span>
							</div>
						</div>
					</template>
				</el-table-column>
				<el-table-column prop="status" label="状态" width="75" align="center">
					<template #default="scope">
						<el-tag
							:type="scope.row.status === 'active' ? 'success' : 'info'"
							size="small"
						>
							{{ scope.row.status === "active" ? "启用" : "禁用" }}
						</el-tag>
					</template>
				</el-table-column>
				<el-table-column
					prop="createTime"
					label="创建时间"
					width="160"
					align="center"
					class-name="time-column"
				>
					<template #default="scope">
						{{ formatDateTime(scope.row.createTime) }}
					</template>
				</el-table-column>
				<el-table-column
					label="操作"
					width="185"
					align="center"
					fixed="right"
					class-name="operation-column"
				>
					<template #default="scope">
						<div class="operation-buttons">
							<el-button
								:type="scope.row.status === 'active' ? 'warning' : 'success'"
								size="small"
								@click="toggleDiscountStatus(scope.row)"
								link
							>
								{{ scope.row.status === "active" ? "禁用" : "启用" }}
							</el-button>
							<el-button
								type="primary"
								size="small"
								@click="openDiscountDialog(scope.row)"
								link
							>
								编辑
							</el-button>
							<el-button
								type="danger"
								size="small"
								@click="() => deleteDiscount(scope.row)"
								link
							>
								删除
							</el-button>
						</div>
					</template>
				</el-table-column>

				<!-- 优化的空状态提示 -->
				<template #empty>
					<div class="empty-discount-state">
						<el-result icon="info" title="暂无优惠活动">
							<template #sub-title>
								<p>还没有创建任何优惠活动</p>
								<p class="empty-tips">
									💡 添加优惠活动可以吸引更多用户下单哦～
								</p>
							</template>
							<template #extra>
								<el-button type="primary" @click="openDiscountDialog()">
									立即创建优惠
								</el-button>
							</template>
						</el-result>
					</div>
				</template>
			</el-table>
		</div>

		<!-- 优惠管理对话框 -->
		<el-dialog
			v-model="discountDialogVisible"
			:title="isEditingDiscount ? '编辑优惠活动' : '添加优惠活动'"
			width="700px"
			top="5%"
		>
			<div class="discount-dialog-content">
				<el-form
					ref="discountFormRef"
					:model="currentDiscountForm"
					:rules="discountRules"
					label-width="120px"
					status-icon
				>
					<el-form-item label="优惠名称" prop="name" required>
						<el-input
							v-model="currentDiscountForm.name"
							placeholder="请输入优惠名称"
						/>
					</el-form-item>
					<el-form-item label="优惠类型" prop="type" required>
						<el-select
							v-model="currentDiscountForm.type"
							placeholder="请选择优惠类型"
						>
							<el-option label="满减" value="满减" />
							<el-option label="折扣" value="折扣" />
							<el-option label="买赠" value="买赠" />
							<el-option label="特价" value="特价" />
						</el-select>
					</el-form-item>
					<el-form-item label="优惠力度" prop="discountValue" required>
						<el-input-number
							v-model="currentDiscountForm.discountValue"
							:min="0"
							:max="100"
							:precision="2"
							:step="1"
							controls-position="right"
						/>
						<span class="unit-text">{{ discountUnit }}</span>
					</el-form-item>
					<el-form-item
						label="最低消费"
						prop="minAmount"
						v-if="currentDiscountForm.type === '满减'"
					>
						<el-input-number
							v-model="currentDiscountForm.minAmount"
							:min="0"
							:precision="2"
							controls-position="right"
							placeholder="满多少可用"
						/>
						<span class="unit-text">元</span>
					</el-form-item>
					<el-form-item label="每人限领" prop="limitPerUser">
						<el-input-number
							v-model="currentDiscountForm.limitPerUser"
							:min="1"
							:max="99"
							controls-position="right"
						/>
						<span class="unit-text">张</span>
					</el-form-item>
					<el-form-item label="有效期类型" prop="validityType" required>
						<el-radio-group v-model="currentDiscountForm.validityType">
							<el-radio value="permanent">永久有效</el-radio>
							<el-radio value="time_range">时间段</el-radio>
							<el-radio value="days">领取后天数</el-radio>
						</el-radio-group>
					</el-form-item>
					<el-form-item
						label="有效期"
						prop="validityPeriod"
						v-if="currentDiscountForm.validityType === 'time_range'"
					>
						<el-date-picker
							v-model="currentDiscountForm.validityPeriod"
							type="datetimerange"
							range-separator="至"
							start-placeholder="开始时间"
							end-placeholder="结束时间"
							style="width: 100%"
						/>
					</el-form-item>
					<el-form-item
						label="有效天数"
						prop="validDays"
						v-if="currentDiscountForm.validityType === 'days'"
					>
						<el-input-number
							v-model="currentDiscountForm.validDays"
							:min="1"
							:max="365"
							controls-position="right"
						/>
						<span class="unit-text">天</span>
					</el-form-item>
					<el-form-item label="使用说明" prop="usageNotes">
						<el-input
							v-model="currentDiscountForm.usageNotes"
							type="textarea"
							:rows="2"
							placeholder="如：仅限堂食、不可与其他优惠同享等"
						/>
					</el-form-item>
					<el-form-item label="优惠描述" prop="description" required>
						<el-input
							v-model="currentDiscountForm.description"
							placeholder="请输入优惠描述"
							type="textarea"
							:rows="3"
						/>
					</el-form-item>
					<el-form-item label="优惠状态" prop="status" required>
						<el-select
							v-model="currentDiscountForm.status"
							placeholder="请选择优惠状态"
						>
							<el-option label="已启用" value="active" />
							<el-option label="已禁用" value="inactive" />
						</el-select>
					</el-form-item>
				</el-form>

				<!-- 优惠预览卡片 -->
				<div class="discount-preview">
					<div class="preview-label">💳 优惠预览</div>
					<div
						class="preview-card"
						:class="`type-${currentDiscountForm.type}`"
					>
						<div class="preview-header">
							<span class="preview-badge">{{
								currentDiscountForm.type || "类型"
							}}</span>
							<span class="preview-name">{{
								currentDiscountForm.name || "优惠名称"
							}}</span>
						</div>
						<div class="preview-value" v-if="currentDiscountForm.discountValue">
							<template v-if="currentDiscountForm.type === '满减'">
								满{{ currentDiscountForm.minAmount }}减{{
									currentDiscountForm.discountValue
								}}元
							</template>
							<template v-else-if="currentDiscountForm.type === '折扣'">
								{{ currentDiscountForm.discountValue }}折
							</template>
							<template v-else-if="currentDiscountForm.type === '买赠'">
								买一送一
							</template>
							<template v-else-if="currentDiscountForm.type === '特价'">
								{{ currentDiscountForm.discountValue }}元特价
							</template>
						</div>
						<div class="preview-desc">
							{{ currentDiscountForm.description || "优惠描述" }}
						</div>
						<div
							class="preview-footer"
							v-if="currentDiscountForm.usageNotes"
						>
							<el-icon><InfoFilled /></el-icon>
							<span>{{ currentDiscountForm.usageNotes }}</span>
						</div>
					</div>
				</div>
			</div>
			<template #footer>
				<span class="dialog-footer">
					<el-button @click="discountDialogVisible = false">取消</el-button>
					<el-button type="primary" @click="saveDiscount">确定</el-button>
				</span>
			</template>
		</el-dialog>
	</div>
</template>

<style scoped lang="less">
.discounts-section {
	margin-bottom: 24px;
	padding: 24px;
	border: 2px solid #409eff;
	border-radius: 12px;
	background: linear-gradient(135deg, #ffffff 0%, #f0f7ff 100%);
	box-shadow: 0 4px 20px rgba(64, 158, 255, 0.15);

	.discounts-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 24px;
		padding-bottom: 20px;
		border-bottom: 2px solid #d4e7ff;
		flex-wrap: wrap;
		gap: 16px;

		.discount-title {
			.card-title {
				margin: 0;
				font-size: 22px;
				font-weight: 700;
				color: #409eff;
				text-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
				display: flex;
				align-items: center;
				gap: 10px;
			}

			.active-discounts {
				font-size: 14px;
				color: #909399;
				margin-top: 6px;
				font-weight: 500;
				padding: 4px 12px;
				background: linear-gradient(135deg, #e8f4ff 0%, #ffffff 100%);
				border-radius: 12px;
				display: inline-block;
			}
		}

		.discount-actions {
			display: flex;
			gap: 12px;
			flex-wrap: wrap;

			:deep(.el-button) {
				border-radius: 8px;
				font-weight: 600;
				transition: all 0.3s ease;
				border: none;

				&:hover:not(:disabled) {
					transform: translateY(-2px);
					box-shadow: 0 6px 16px rgba(64, 158, 255, 0.3);
				}

				&:disabled {
					opacity: 0.5;
				}

				&.el-button--primary {
					background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);

					&:hover:not(:disabled) {
						background: linear-gradient(135deg, #66b1ff 0%, #409eff 100%);
					}
				}

				&.el-button--success {
					background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);

					&:hover:not(:disabled) {
						background: linear-gradient(135deg, #85ce61 0%, #67c23a 100%);
					}
				}

				&.el-button--warning {
					background: linear-gradient(135deg, #e6a23c 0%, #f0c78a 100%);

					&:hover:not(:disabled) {
						background: linear-gradient(135deg, #f0c78a 0%, #e6a23c 100%);
					}
				}

				&.el-button--danger {
					background: linear-gradient(135deg, #f56c6c 0%, #f89898 100%);

					&:hover:not(:disabled) {
						background: linear-gradient(135deg, #f89898 0%, #f56c6c 100%);
					}
				}
			}
		}
	}

	.discounts-table-container {
		width: 100%;
		max-width: 100%;
		overflow-x: auto;
		overflow-y: visible;
		background: linear-gradient(135deg, #ffffff 0%, #fafbff 100%);
		border-radius: 12px;
		padding: 20px;
		box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
		border: 1px solid #e4e7ed;
		box-sizing: border-box;

		:deep(.el-table) {
			font-size: 14px;
			table-layout: auto;
			border-radius: 8px;
			overflow: hidden;
			max-width: 100%;

			&::before {
				display: none;
			}

			.el-table__header-wrapper {
				/* 隐藏表头滚动条但保持滚动功能 */
				scrollbar-width: none; /* Firefox */
				-ms-overflow-style: none; /* IE and Edge */

				&::-webkit-scrollbar {
					display: none; /* Chrome, Safari, Opera */
				}

				th {
					background: linear-gradient(135deg, #e8f4ff 0%, #d4e7ff 100%);
					color: #303133;
					font-weight: 700;
					font-size: 14px;
					padding: 14px 12px;
					white-space: nowrap;
					border-bottom: 2px solid #b3d8ff;
					text-align: center;
				}
			}

			.el-table__body-wrapper {
				.el-table__row {
					transition: all 0.3s ease;

					&:hover {
						background: linear-gradient(90deg, #f0f7ff 0%, #ffffff 100%);
						transform: scale(1.002);
						box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
					}

					td {
					padding: 12px 12px;
					border-bottom: 1px solid #f0f0f0;
				}
				}
			}

			.el-table__cell {
				overflow: hidden;
				text-overflow: ellipsis;
				white-space: nowrap;
				padding: 0 12px;
				word-break: break-all;
			}

			.el-table__header-wrapper,
			.el-table__body-wrapper {
				overflow-x: auto;
			}

			.el-tag {
				border-radius: 6px;
				padding: 6px 12px;
				font-weight: 600;
				border: none;
				box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
			}
		}
	}

	.usage-stats {
		display: flex;
		flex-direction: column;
		gap: 6px;

		.usage-item {
			display: flex;
			align-items: center;
			justify-content: center;
			gap: 4px;
			font-size: 12px;
			color: #606266;
			font-weight: 500;
			padding: 4px 6px;
			background: linear-gradient(135deg, #f0f7ff 0%, #ffffff 100%);
			border-radius: 6px;

			.el-icon {
				font-size: 14px;
				color: #409eff;
			}
		}
	}

	.discount-rule {
		display: flex;
		align-items: center;
		gap: 4px;
		font-size: 13px;
		font-weight: 500;
		padding: 4px 6px;
		background: linear-gradient(135deg, #fff5f5 0%, #ffffff 100%);
		border-radius: 6px;
		border: 1px solid #ffebeb;

		.rule-highlight {
			color: #606266;
			font-weight: 500;
		}

		.rule-divider {
			color: #909399;
			margin: 0 2px;
			font-weight: 400;
			font-size: 12px;
		}

		.rule-value {
			color: #f56c6c;
			font-weight: 700;
			font-size: 14px;
			text-shadow: 0 1px 2px rgba(245, 108, 108, 0.2);
		}

		.rule-empty {
			color: #c0c4cc;
		}
	}

	.empty-discount-state {
		padding: 60px 20px;
		background: linear-gradient(135deg, #f9f9f9 0%, #ffffff 100%);
		border-radius: 12px;
		border: 2px dashed #d4e7ff;

		.empty-tips {
			margin-top: 12px;
			color: #909399;
			font-size: 15px;
			font-weight: 500;
		}

		:deep(.el-result) {
			.el-result__title {
				font-size: 18px;
				font-weight: 600;
				color: #606266;
			}

			.el-result__subtitle {
				p {
					margin: 8px 0;
					line-height: 1.8;
				}
			}

			.el-button {
				border-radius: 8px;
				font-weight: 600;
				padding: 12px 24px;
				background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
				border: none;
				transition: all 0.3s ease;

				&:hover {
					transform: translateY(-2px);
					box-shadow: 0 6px 16px rgba(64, 158, 255, 0.4);
					background: linear-gradient(135deg, #66b1ff 0%, #409eff 100%);
				}
			}
		}
	}

	:deep(.time-column) {
		font-size: 13px;
		color: #909399;
		font-weight: 500;

		.cell {
			padding: 10px 0;
		}
	}

	:deep(.operation-column) {
		.cell {
			padding: 0;
		}

		.operation-buttons {
			display: flex;
			align-items: center;
			justify-content: center;
			gap: 6px;
			height: 100%;
			padding: 8px 0;

			.el-button {
				display: inline-flex;
				align-items: center;
				justify-content: center;
				margin: 0;
				vertical-align: middle;
				height: 26px;
				line-height: 26px;
				font-weight: 600;
				border-radius: 6px;
				padding: 0 10px;
				font-size: 13px;
				transition: all 0.3s ease;

				&:hover {
					transform: translateY(-2px);
					box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
				}
			}
		}
	}

	.discount-dialog-content {
		display: flex;
		gap: 32px;

		.el-form {
			flex: 1;

			.el-form-item {
				margin-bottom: 22px;

				.el-form-item__label {
					font-weight: 600;
					color: #606266;
					font-size: 14px;
				}

				.el-input__inner,
				.el-textarea__inner {
					border-radius: 8px;
					border: 2px solid #dcdfe6;
					transition: all 0.3s ease;
					font-size: 14px;

					&:focus {
						border-color: #409eff;
						box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.1);
					}

					&:hover {
						border-color: #c0c4cc;
					}
				}

				.el-input-number {
					width: 100%;

					.el-input__inner {
						text-align: left;
					}
				}

				.el-select {
					width: 100%;
				}

				.el-radio-group {
					display: flex;
					gap: 16px;

					.el-radio {
						margin-right: 0;

						.el-radio__label {
							font-weight: 500;
							padding-left: 8px;
						}
					}
				}
			}
		}

		.unit-text {
			margin-left: 12px;
			color: #909399;
			font-size: 14px;
			font-weight: 500;
		}

		.discount-preview {
			width: 300px;
			flex-shrink: 0;

			.preview-label {
				font-size: 16px;
				font-weight: 700;
				color: #303133;
				margin-bottom: 16px;
				display: flex;
				align-items: center;
				gap: 8px;
			}

			.preview-card {
				background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
				border-radius: 16px;
				padding: 24px;
				color: white;
				box-shadow: 0 8px 24px rgba(102, 126, 234, 0.4);
				transition: all 0.4s cubic-bezier(0.68, -0.55, 0.265, 1.55);
				position: relative;
				overflow: hidden;

				&::before {
					content: "";
					position: absolute;
					top: -50%;
					right: -50%;
					width: 200%;
					height: 200%;
					background: radial-gradient(
						circle,
						rgba(255, 255, 255, 0.1) 0%,
						transparent 70%
					);
					transition: all 0.6s ease;
				}

				&:hover {
					transform: translateY(-8px) scale(1.02);
					box-shadow: 0 12px 32px rgba(102, 126, 234, 0.5);

					&::before {
						top: -30%;
						right: -30%;
					}
				}

				&.type-满减 {
					background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
					box-shadow: 0 8px 24px rgba(245, 87, 108, 0.4);

					&:hover {
						box-shadow: 0 12px 32px rgba(245, 87, 108, 0.5);
					}
				}

				&.type-折扣 {
					background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
					box-shadow: 0 8px 24px rgba(79, 172, 254, 0.4);

					&:hover {
						box-shadow: 0 12px 32px rgba(79, 172, 254, 0.5);
					}
				}

				&.type-买赠 {
					background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
					box-shadow: 0 8px 24px rgba(67, 233, 123, 0.4);

					&:hover {
						box-shadow: 0 12px 32px rgba(67, 233, 123, 0.5);
					}
				}

				&.type-特价 {
					background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
					box-shadow: 0 8px 24px rgba(250, 112, 154, 0.4);

					&:hover {
						box-shadow: 0 12px 32px rgba(250, 112, 154, 0.5);
					}
				}

				.preview-header {
					display: flex;
					justify-content: space-between;
					align-items: center;
					margin-bottom: 20px;
					position: relative;
					z-index: 1;

					.preview-badge {
						background: rgba(255, 255, 255, 0.3);
						padding: 6px 14px;
						border-radius: 16px;
						font-size: 12px;
						font-weight: 600;
						backdrop-filter: blur(10px);
						box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
					}

					.preview-name {
						font-size: 16px;
						font-weight: 600;
						flex: 1;
						text-align: right;
						text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
					}
				}

				.preview-value {
					font-size: 36px;
					font-weight: 800;
					margin-bottom: 16px;
					text-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
					position: relative;
					z-index: 1;
					line-height: 1.2;
				}

				.preview-desc {
					font-size: 14px;
					opacity: 0.95;
					margin-bottom: 16px;
					line-height: 1.6;
					position: relative;
					z-index: 1;
				}

				.preview-footer {
					display: flex;
					align-items: center;
					gap: 8px;
					font-size: 12px;
					opacity: 0.9;
					padding-top: 16px;
					border-top: 1px solid rgba(255, 255, 255, 0.25);
					position: relative;
					z-index: 1;

					.el-icon {
						font-size: 15px;
					}
				}
			}
		}
	}

	:deep(.el-dialog) {
		border-radius: 16px;
		box-shadow: 0 12px 48px rgba(0, 0, 0, 0.2);
		overflow: hidden;

		.el-dialog__header {
			padding: 24px 28px;
			border-bottom: 2px solid #e4e7ed;
			background: linear-gradient(135deg, #f0f7ff 0%, #ffffff 100%);

			.el-dialog__title {
				font-size: 20px;
				font-weight: 700;
				color: #409eff;
			}

			.el-dialog__headerbtn {
				top: 24px;
				right: 24px;
				width: 32px;
				height: 32px;
				border-radius: 50%;
				transition: all 0.3s ease;

				&:hover {
					background-color: #f0f7ff;
				}

				.el-dialog__close {
					font-size: 18px;
					color: #909399;
				}
			}
		}

		.el-dialog__body {
			padding: 28px;
		}

		.el-dialog__footer {
			padding: 20px 28px;
			border-top: 2px solid #e4e7ed;
			background: linear-gradient(135deg, #ffffff 0%, #f9f9f9 100%);

			.dialog-footer {
				display: flex;
				justify-content: flex-end;
				gap: 16px;

				.el-button {
					border-radius: 8px;
					padding: 12px 28px;
					font-weight: 600;
					font-size: 14px;
					transition: all 0.3s ease;
					border: 2px solid transparent;

					&:hover {
						transform: translateY(-2px);
						box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
					}

					&.el-button--primary {
						background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
						border: none;

						&:hover {
							background: linear-gradient(135deg, #66b1ff 0%, #409eff 100%);
						}
					}

					&.el-button--default {
						border-color: #dcdfe6;

						&:hover {
							border-color: #c0c4cc;
							background-color: #f5f7fa;
						}
					}
				}
			}
		}
	}
}
</style>
