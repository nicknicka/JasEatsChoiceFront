<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import api from "../../utils/api.js";
import { API_CONFIG } from "../../config/index.js";
import { useAuthStore } from "../../store/authStore";

const router = useRouter();
const authStore = useAuthStore();

let merchantId = authStore.merchantId;

// 如果 Pinia 中没有商家ID，尝试从 localStorage 读取
if (!merchantId) {
    const localStorageMerchantId = localStorage.getItem("auth_merchantId");
    if (localStorageMerchantId) {
        merchantId = localStorageMerchantId;
        authStore.setMerchantId(localStorageMerchantId); // 更新到 Pinia 中
    }
}

// 所有订单数据
const allOrders = ref([]);

// 筛选后的订单
const filteredOrders = ref([]);

// 当前激活的筛选条件
const activeFilter = ref("today");

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

// 页面跳转
const navigateToOrders = () => {
    router.push("/merchant/home/orders");
};

// 查看订单详情
const viewOrderDetails = (order) => {
    // 跳转到订单详情页面
    router.push(`/merchant/home/orders/details?orderId=${order.id}`);
};

// 更新订单状态
const updateOrderStatus = (order) => {
    // 定义订单状态流转逻辑
    const statusFlow = {
        1: 2, // 待处理 -> 备菜中
        2: 3, // 备菜中 -> 烹饪中
        3: 4, // 烹饪中 -> 待配送
        4: 5, // 待配送 -> 已完成
        5: 5, // 已完成 -> 已完成（不可再改）
        6: 6, // 已取消 -> 已取消（不可再改）
    };

    const nextStatus = statusFlow[order.status] || order.status;

    // 如果状态没有变化
    if (nextStatus === order.status) {
        ElMessage.warning(`订单 ${order.id} 当前状态不可变更`);
        return;
    }

    // 调用API更新订单状态
    const updateData = {
        orderId: order.id,
        status: nextStatus,
    };

    api.put(
        API_CONFIG.merchant.updateOrderStatus.replace("{orderId}", order.id),
        updateData
    )
        .then((response) => {
            if (response.data && response.data.success) {
                // 更新本地订单状态
                order.status = nextStatus;
                ElMessage.success(
                    `订单 ${order.id} 状态已更新为 ${orderStatusMap[nextStatus]}`
                );
            }
        })
        .catch((error) => {
            console.error("更新订单状态失败:", error);
            ElMessage.error("更新订单状态失败");
        });
};

// 通知用户
const notifyUser = (order) => {
    // 调用API通知用户
    const notifyData = {
        orderId: order.id,
        message: `您的订单 ${order.id} 状态已更新为 ${orderStatusMap[order.status]}`,
    };

    api.post(API_CONFIG.merchant.notifyUser.replace("{orderId}", order.id), notifyData)
        .then((response) => {
            if (response.data && response.data.success) {
                ElMessage.success(`已成功通知用户订单 ${order.id} 的最新状态`);
            }
        })
        .catch((error) => {
            console.error("通知用户失败:", error);
            ElMessage.error("通知用户失败");
        });
};

// 获取订单列表
const fetchOrders = () => {
    api.get(`/v1/orders/merchant/${merchantId}`)
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
};

onMounted(() => {
    fetchOrders();
});
</script>

<template>
    <div class="orders-card">
        <div class="orders-header">
            <h3 class="card-title">📋 订单中心</h3>
            <div class="filter-section">
                <el-tag
                    type="success"
                    effect="light"
                    class="order-filter-tag"
                    :class="{ active: activeFilter === 'today' }"
                    @click="filterOrders('today')"
                    >今日订单</el-tag
                >
                <el-tag
                    type="info"
                    effect="light"
                    class="order-filter-tag"
                    :class="{ active: activeFilter === 'week' }"
                    @click="filterOrders('week')"
                    >本周订单</el-tag
                >
                <el-tag
                    type="warning"
                    effect="light"
                    class="order-filter-tag"
                    :class="{ active: activeFilter === 'month' }"
                    @click="filterOrders('month')"
                    >本月订单</el-tag
                >
                <el-tag
                    type="primary"
                    effect="light"
                    class="order-filter-tag"
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
</template>

<style scoped lang="less">
.orders-card {
    margin-bottom: 24px;
    padding: 24px;
    border: 2px solid #409eff;
    border-radius: 12px;
    background-color: #ffffff;
    box-shadow: 0 4px 20px rgba(64, 158, 255, 0.1);

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
            .order-filter-tag {
                margin-right: 10px;
                cursor: pointer;
                transition: all 0.3s ease;
                border-radius: 20px;

                &:hover {
                    transform: translateY(-2px);
                    box-shadow: 0 3px 12px rgba(0, 0, 0, 0.15);
                }

                &.active {
                    transform: translateY(-1px);
                    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
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
</style>
