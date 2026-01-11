<script setup>
import { ref, computed, onMounted } from "vue";
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

// 优惠表单验证规则引用
const discountFormRef = ref(null);

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

onMounted(() => {
	fetchDiscounts();
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
				:data="discounts"
				:default-sort="{ prop: 'createTime', order: 'descending' }"
				@selection-change="handleSelectionChange"
				style="width: 100%"
				:row-style="{ height: '60px' }"
				:cell-style="{ padding: '8px' }"
				:table-layout="'auto'"
			>
				<el-table-column
					type="selection"
					width="50"
					align="center"
					fixed="left"
				/>
				<el-table-column
					prop="name"
					label="优惠名称"
					min-width="150"
					width="180"
					show-overflow-tooltip
				/>
				<el-table-column prop="type" label="类型" width="80" align="center">
					<template #default="scope">
						<el-tag :type="getDiscountTypeTag(scope.row.type)" size="small">
							{{ scope.row.type }}
						</el-tag>
					</template>
				</el-table-column>
				<el-table-column label="优惠规则" min-width="160" width="200">
					<template #default="scope">
						<div class="discount-rule">
							<template
								v-if="scope.row.type === '满减' && scope.row.discountValue"
							>
								<span class="rule-highlight"
									>满 {{ scope.row.minAmount || 0 }}</span
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
					min-width="200"
					width="250"
					show-overflow-tooltip
				/>
				<el-table-column label="使用情况" width="110" align="center">
					<template #default="scope">
						<div class="usage-stats">
							<div class="usage-item">
								<el-icon><User /></el-icon>
								<span>{{ scope.row.usedCount || 0 }}次</span>
							</div>
						</div>
					</template>
				</el-table-column>
				<el-table-column prop="status" label="状态" width="80" align="center">
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
					width="150"
					align="center"
					class-name="time-column"
				>
					<template #default="scope">
						{{ formatDateTime(scope.row.createTime) }}
					</template>
				</el-table-column>
				<el-table-column
					label="操作"
					width="180"
					fixed="right"
					align="center"
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
	border: 2px solid #409eff; /* 主蓝色 */
	border-radius: 12px;
	background-color: #ffffff;
	box-shadow: 0 4px 20px rgba(64, 158, 255, 0.1);

	.discounts-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 20px;
		flex-wrap: wrap;
		gap: 16px;

		.discount-title {
			.card-title {
				margin: 0;
				font-size: 20px;
				font-weight: 700;
			}

			.active-discounts {
				font-size: 14px;
				color: #909399;
				margin-top: 4px;
			}
		}

		.discount-actions {
			display: flex;
			gap: 12px;
			flex-wrap: wrap;
		}
	}

	.discounts-table-container {
		width: 100%;
		overflow-x: auto;
		overflow-y: visible;

		:deep(.el-table) {
			font-size: 13px;
			table-layout: auto;

			.el-table__header-wrapper {
				th {
					background-color: #f5f7fa;
					color: #303133;
					font-weight: 600;
					font-size: 13px;
					padding: 12px 0;
					white-space: nowrap;
				}
			}

			.el-table__body-wrapper {
				.el-table__row {
					&:hover {
						background-color: #f5f7fa;
					}
				}

				td {
					padding: 10px 0;
				}
			}

			.el-table__cell {
				overflow: hidden;
				text-overflow: ellipsis;
				white-space: nowrap;
			}
		}
	}

	.usage-stats {
		display: flex;
		flex-direction: column;
		gap: 4px;

		.usage-item {
			display: flex;
			align-items: center;
			gap: 4px;
			font-size: 13px;
			color: #606266;

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
		font-size: 14px;
		font-weight: 500;

		.rule-highlight {
			color: #606266;
			font-weight: normal;
		}

		.rule-divider {
			color: #909399;
			margin: 0 2px;
		}

		.rule-value {
			color: #f56c6c;
			font-weight: 600;
			font-size: 15px;
		}

		.rule-empty {
			color: #c0c4cc;
		}
	}

	.empty-discount-state {
		padding: 40px 20px;

		.empty-tips {
			margin-top: 8px;
			color: #909399;
			font-size: 14px;
		}
	}

	:deep(.time-column) {
		font-size: 12px;
		color: #909399;

		.cell {
			padding: 8px 0;
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
			gap: 8px;
			height: 100%;
			padding: 8px 0;

			.el-button {
				display: inline-flex;
				align-items: center;
				justify-content: center;
				margin: 0;
				vertical-align: middle;
				height: 24px;
				line-height: 24px;
			}
		}
	}

	.discount-dialog-content {
		display: flex;
		gap: 24px;

		.el-form {
			flex: 1;
		}

		.unit-text {
			margin-left: 8px;
			color: #909399;
			font-size: 14px;
		}

		.discount-preview {
			width: 280px;
			flex-shrink: 0;

			.preview-label {
				font-size: 14px;
				font-weight: 600;
				color: #303133;
				margin-bottom: 12px;
			}

			.preview-card {
				background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
				border-radius: 12px;
				padding: 20px;
				color: white;
				box-shadow: 0 4px 16px rgba(102, 126, 234, 0.3);
				transition: all 0.3s ease;

				&:hover {
					transform: translateY(-4px);
					box-shadow: 0 8px 24px rgba(102, 126, 234, 0.4);
				}

				&.type-满减 {
					background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
				}

				&.type-折扣 {
					background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
				}

				&.type-买赠 {
					background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
				}

				&.type-特价 {
					background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
				}

				.preview-header {
					display: flex;
					justify-content: space-between;
					align-items: center;
					margin-bottom: 16px;

					.preview-badge {
						background: rgba(255, 255, 255, 0.25);
						padding: 4px 12px;
						border-radius: 12px;
						font-size: 12px;
						font-weight: 500;
						backdrop-filter: blur(10px);
					}

					.preview-name {
						font-size: 16px;
						font-weight: 600;
						flex: 1;
						text-align: right;
					}
				}

				.preview-value {
					font-size: 32px;
					font-weight: 700;
					margin-bottom: 12px;
					text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
				}

				.preview-desc {
					font-size: 14px;
					opacity: 0.95;
					margin-bottom: 12px;
					line-height: 1.6;
				}

				.preview-footer {
					display: flex;
					align-items: center;
					gap: 6px;
					font-size: 12px;
					opacity: 0.9;
					padding-top: 12px;
					border-top: 1px solid rgba(255, 255, 255, 0.2);

					.el-icon {
						font-size: 14px;
					}
				}
			}
		}
	}
}
</style>
