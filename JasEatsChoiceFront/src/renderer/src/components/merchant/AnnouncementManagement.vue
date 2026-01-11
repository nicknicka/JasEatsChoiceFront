<script setup>
import { ref, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import api from "../../utils/api.js";
import { API_CONFIG } from "../../config/index.js";

const props = defineProps({
	merchantId: {
		type: String,
		required: true,
	},
});

// 公告栏配置
const announcements = ref([]);
const announcementDialogVisible = ref(false);
const currentAnnouncement = ref({
	title: "",
	content: "",
	status: "active",
	startTime: null,
	endTime: null,
});
const isEditingAnnouncement = ref(false);

// 获取公告列表
const getAnnouncements = () => {
	let url = API_CONFIG.merchant.announcements;
	url = url.replace("{merchantId}", props.merchantId);
	api
		.get(url)
		.then(function (response) {
			if (response.data && response.data.success) {
				announcements.value = response.data.data;
			}
		})
		.catch(function (error) {
			console.error("获取公告列表失败:", error);
		});
};

// 打开公告编辑对话框
const openAnnouncementDialog = function (announcement = null) {
	announcementDialogVisible.value = true;
	if (announcement) {
		isEditingAnnouncement.value = true;
		currentAnnouncement.value = JSON.parse(JSON.stringify(announcement));
	} else {
		isEditingAnnouncement.value = false;
		currentAnnouncement.value = {
			title: "",
			content: "",
			status: "active",
			startTime: null,
			endTime: null,
		};
	}
};

// 保存公告
const saveAnnouncement = function () {
	// 简单验证
	if (!currentAnnouncement.value.title || !currentAnnouncement.value.content) {
		ElMessage.error("请填写完整的公告信息");
		return;
	}

	let apiMethod = isEditingAnnouncement.value ? api.put : api.post;
	let apiUrl = API_CONFIG.merchant.announcements.replace(
		"{merchantId}",
		props.merchantId
	);
	if (isEditingAnnouncement.value) {
		apiUrl = apiUrl + "/" + currentAnnouncement.value.id;
	}

	apiMethod(apiUrl, currentAnnouncement.value)
		.then(function (response) {
			if (response.data && response.data.success) {
				let message = isEditingAnnouncement.value ? "公告已更新" : "公告已添加";
				ElMessage.success(message);
				getAnnouncements(); // 刷新公告列表
				announcementDialogVisible.value = false;
			}
		})
		.catch(function (error) {
			console.error("保存公告失败:", error);
			ElMessage.error("保存公告失败");
		});
};

// 删除公告
const deleteAnnouncement = function (announcement) {
	ElMessageBox.confirm(
		`确定要删除公告 '${announcement.title}' 吗？`,
		"删除公告",
		{
			confirmButtonText: "确定",
			cancelButtonText: "取消",
			type: "warning",
		}
	)
		.then(function () {
			let url = API_CONFIG.merchant.announcements.replace(
				"{merchantId}",
				props.merchantId
			);
			url = url + "/" + announcement.id;
			api
				.delete(url)
				.then(function (response) {
					if (response.data && response.data.success) {
						ElMessage.success("公告已删除");
						getAnnouncements(); // 刷新公告列表
					}
				})
				.catch(function (error) {
					console.error("删除公告失败:", error);
					ElMessage.error("删除公告失败");
				});
		})
		.catch(function () {
			ElMessage.info("已取消删除");
		});
};

// 切换公告状态
const toggleAnnouncementStatus = function (announcement) {
	let newStatus = announcement.status === "active" ? "inactive" : "active";
	let statusText = newStatus === "active" ? "已启用" : "已禁用";

	let url = API_CONFIG.merchant.announcements.replace(
		"{merchantId}",
		props.merchantId
	);
	url = url + "/" + announcement.id + "/status";

	api
		.put(url, { status: newStatus })
		.then(function (response) {
			if (response.data && response.data.success) {
				announcement.status = newStatus;
				ElMessage.success("公告已" + statusText);
			}
		})
		.catch(function (error) {
			console.error("切换公告状态失败:", error);
			ElMessage.error("切换公告状态失败");
		});
};

onMounted(() => {
	getAnnouncements();
});
</script>

<template>
	<div class="announcement-section">
		<div class="announcement-header">
			<h3 class="card-title">📢 公告栏管理</h3>
			<el-button type="primary" size="small" @click="openAnnouncementDialog()">
				<el-icon><Plus /></el-icon> 添加公告
			</el-button>
		</div>
		<div class="announcement-table-container">
			<el-table
				:data="announcements"
				:default-sort="{ prop: 'createdTime', order: 'descending' }"
			>
				<el-table-column prop="title" label="公告标题" min-width="200" />
				<el-table-column prop="status" label="状态" width="100">
					<template #default="scope">
						<el-tag
							:type="scope.row.status === 'active' ? 'success' : 'warning'"
						>
							{{ scope.row.status === "active" ? "已启用" : "已禁用" }}
						</el-tag>
					</template>
				</el-table-column>
				<el-table-column prop="startTime" label="开始时间" width="180" />
				<el-table-column prop="endTime" label="结束时间" width="180" />
				<el-table-column label="操作" width="200" fixed="right">
					<template #default="scope">
						<el-button
							type="primary"
							size="small"
							@click="openAnnouncementDialog(scope.row)"
						>
							编辑
						</el-button>
						<el-button
							:type="scope.row.status === 'active' ? 'warning' : 'success'"
							size="small"
							@click="toggleAnnouncementStatus(scope.row)"
						>
							{{ scope.row.status === "active" ? "禁用" : "启用" }}
						</el-button>
						<el-button
							type="danger"
							size="small"
							@click="() => deleteAnnouncement(scope.row)"
						>
							删除
						</el-button>
					</template>
				</el-table-column>
				<template #empty>
					<div class="empty-state">
						<span class="el-icon-info" />
						<p>暂无公告，请点击右上角"添加公告"创建</p>
					</div>
				</template>
			</el-table>
		</div>

		<!-- 公告编辑对话框 -->
		<el-dialog
			v-model="announcementDialogVisible"
			:title="isEditingAnnouncement ? '编辑公告' : '添加公告'"
			width="600px"
			top="10%"
		>
			<el-form :model="currentAnnouncement" label-width="100px" status-icon>
				<el-form-item label="公告标题" prop="title" required>
					<el-input
						v-model="currentAnnouncement.title"
						placeholder="请输入公告标题"
					/>
				</el-form-item>
				<el-form-item label="公告内容" prop="content" required>
					<el-input
						v-model="currentAnnouncement.content"
						placeholder="请输入公告内容"
						type="textarea"
						:rows="4"
					/>
				</el-form-item>
				<el-form-item label="状态" prop="status" required>
					<el-select
						v-model="currentAnnouncement.status"
						placeholder="请选择公告状态"
					>
						<el-option label="已启用" value="active" />
						<el-option label="已禁用" value="inactive" />
					</el-select>
				</el-form-item>
				<el-form-item label="开始时间" prop="startTime">
					<el-date-picker
						v-model="currentAnnouncement.startTime"
						type="datetime"
						placeholder="选择开始时间"
						style="width: 100%"
					/>
				</el-form-item>
				<el-form-item label="结束时间" prop="endTime">
					<el-date-picker
						v-model="currentAnnouncement.endTime"
						type="datetime"
						placeholder="选择结束时间"
						style="width: 100%"
					/>
				</el-form-item>
			</el-form>
			<template #footer>
				<span class="dialog-footer">
					<el-button @click="announcementDialogVisible = false">取消</el-button>
					<el-button type="primary" @click="saveAnnouncement">确定</el-button>
				</span>
			</template>
		</el-dialog>
	</div>
</template>

<style scoped lang="less">
.announcement-section {
	margin-bottom: 24px;
	padding: 24px;
	border: 2px solid #909399; /* 中性灰 */
	border-radius: 12px;
	background-color: #ffffff;
	box-shadow: 0 4px 20px rgba(144, 147, 153, 0.1);

	.announcement-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 20px;

		.card-title {
			margin: 0;
			font-size: 20px;
			font-weight: 700;
		}
	}
}
</style>
