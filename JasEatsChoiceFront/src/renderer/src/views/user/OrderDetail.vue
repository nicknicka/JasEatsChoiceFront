<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { API_CONFIG } from '../../config/index.js'
import { ElMessage } from 'element-plus'
import CommonBackButton from '../../components/common/CommonBackButton.vue'

const router = useRouter()
const route = useRoute()

// 订单详情数据
const orderDetail = ref(null)

onMounted(() => {
  // 从路由参数获取订单ID
  const orderId = parseInt(route.params.id)

  // 从API获取订单详情
  axios
    .get(API_CONFIG.baseURL + API_CONFIG.order.detail + orderId)
    .then((response) => {
      if (response.data && response.data.success) {
        orderDetail.value = response.data.data
      } else {
        // 如果API返回错误，跳回订单列表并提示
        router.push('/user/home/orders')
        ElMessage.error('订单详情获取失败')
      }
    })
    .catch((error) => {
      console.error('加载订单详情失败:', error)
      // 如果请求失败，跳回订单列表并提示
      router.push('/user/home/orders')
      ElMessage.error('订单详情获取失败')
    })
})

// 返回订单列表
const backToOrders = () => {
  router.push('/user/home/orders')
}
</script>

<template>
  <div class="order-detail-container">
    <div class="page-header">
      <common-back-button
        type="text"
        size="small"
        text="返回订单列表"
        @click="backToOrders"
        :use-router-back="false"
      />
      <h3 class="page-title">订单详情</h3>
    </div>

    <el-card v-if="orderDetail" class="order-detail-card">
      <!-- 订单基本信息 -->
      <div class="order-info-section">
        <h4 class="section-title">📋 订单基本信息</h4>
        <div class="info-row">
          <span class="info-label">订单号:</span>
          <span class="info-value">{{ orderDetail.orderNo }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">商家:</span>
          <span class="info-value">{{ orderDetail.merchant }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">订单状态:</span>
          <el-tag
            :type="
              orderDetail.status === 'processing'
                ? 'warning'
                : orderDetail.status === 'delivered'
                  ? 'success'
                  : orderDetail.status === 'completed'
                    ? 'info'
                    : 'danger'
            "
          >
            {{
              orderDetail.status === 'processing'
                ? '处理中'
                : orderDetail.status === 'delivered'
                  ? '已送达'
                  : orderDetail.status === 'completed'
                    ? '已完成'
                    : '已取消'
            }}
          </el-tag>
        </div>
        <div class="info-row">
          <span class="info-label">下单时间:</span>
          <span class="info-value">{{ orderDetail.time }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">配送地址:</span>
          <span class="info-value">{{ orderDetail.address }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">支付方式:</span>
          <span class="info-value">{{ orderDetail.paymentMethod }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">配送时间:</span>
          <span class="info-value">{{ orderDetail.deliveryTime || '配送中' }}</span>
        </div>
      </div>

      <!-- 商品信息 -->
      <div class="order-items-section">
        <h4 class="section-title">🛒 商品信息</h4>
        <div class="items-list">
          <div class="item-card" v-for="item in orderDetail.orderItems" :key="item.id">
            <div class="item-info">
              <div class="item-name">{{ item.name }}</div>
              <div class="item-details">
                <span class="item-quantity">×{{ item.quantity }}</span>
                <span class="item-price">→ {{ item.price.toFixed(2) }}元/份</span>
                <span class="item-total">→ {{ (item.price * item.quantity).toFixed(2) }}元</span>
              </div>
              <div class="item-status">
                <el-tag
                  :type="
                    item.status === 'preparing'
                      ? 'warning'
                      : item.status === 'delivered'
                        ? 'success'
                        : item.status === 'completed'
                          ? 'info'
                          : 'danger'
                  "
                >
                  {{
                    item.status === 'preparing'
                      ? '制作中'
                      : item.status === 'delivered'
                        ? '已送达'
                        : item.status === 'completed'
                          ? '已完成'
                          : '已取消'
                  }}
                </el-tag>
              </div>
            </div>
            <div class="item-price-total">¥{{ (item.price * item.quantity).toFixed(2) }}</div>
          </div>
        </div>
      </div>

      <!-- 订单金额 -->
      <div class="order-total-section">
        <div class="total-row">
          <span class="total-label">💰 订单总金额:</span>
          <span class="total-value">¥{{ orderDetail.total.toFixed(2) }}</span>
        </div>
      </div>

      <!-- 商家信息 -->
      <div class="merchant-info-section">
        <h4 class="section-title">🏪 商家信息</h4>
        <div class="info-row">
          <span class="info-label">商家名称:</span>
          <span class="info-value">{{ orderDetail.merchantInfo.name }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">联系电话:</span>
          <span class="info-value">{{ orderDetail.merchantInfo.phone }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">商家地址:</span>
          <span class="info-value">{{ orderDetail.merchantInfo.address }}</span>
        </div>
      </div>

      <!-- 订单操作记录 -->
      <div class="order-records-section">
        <h4 class="section-title">📜 订单操作记录</h4>
        <div class="records-timeline">
          <div class="record-item" v-for="record in orderDetail.operationRecords" :key="record.id">
            <div class="record-time">{{ record.time }}</div>
            <div class="record-content">
              <div class="record-action">{{ record.action }}</div>
              <div class="record-description">{{ record.description }}</div>
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<style scoped lang="less">
.order-detail-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding-bottom: 60px;

  .page-header {
    display: flex;
    align-items: center;
    gap: 16px;
    margin: 0 auto;
    max-width: 900px;
    padding: 20px;
    background: rgba(255, 255, 255, 0.95);
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    margin-bottom: 30px;

    .page-title {
      font-size: 24px;
      font-weight: 700;
      margin: 0;
      color: #2c3e50;
    }
  }

  .order-detail-card {
    max-width: 900px;
    margin: 0 auto;
    margin-bottom: 20px;
    border-radius: 12px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);

    .section-title {
      font-size: 16px;
      font-weight: 600;
      color: #34495e;
      margin-bottom: 20px;
      display: flex;
      align-items: center;
      gap: 8px;
      padding-bottom: 8px;
      border-bottom: 2px solid #e0e0e0;
    }

    .order-info-section {
      padding: 24px;

      .info-row {
        margin-bottom: 14px;
        display: flex;
        flex-wrap: wrap;
        align-items: center;
        padding: 10px 12px;
        background: rgba(255, 255, 255, 0.85);
        border-radius: 8px;
        transition: all 0.3s ease;

        &:hover {
          background: rgba(255, 255, 255, 1);
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
        }

        .info-label {
          font-weight: 600;
          color: #555;
          min-width: 120px;
        }

        .info-value {
          color: #2c3e50;
          font-size: 15px;
        }
      }
    }

    .order-items-section {
      padding: 24px;
      margin: 0;

      .items-list {
        margin-bottom: 28px;
      }

      .item-card {
        display: flex;
        justify-content: space-between;
        margin-bottom: 18px;
        padding: 16px;
        background: rgba(255, 255, 255, 0.9);
        border-radius: 10px;
        box-shadow: 0 2px 6px rgba(0, 0, 0, 0.04);
        transition: all 0.3s ease;

        &:hover {
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
          transform: translateY(-1px);
        }

        &:last-child {
          margin-bottom: 0;
        }

        .item-info {
          .item-name {
            font-size: 17px;
            font-weight: 600;
            margin-bottom: 6px;
            color: #2c3e50;
          }

          .item-details {
            font-size: 14px;
            color: #666;
            gap: 12px;
            display: flex;
            flex-wrap: wrap;
          }
        }

        .item-price-total {
          font-size: 18px;
          font-weight: 600;
          color: #ff6b6b;
        }
      }
    }

    .order-total-section {
      padding: 24px;
      text-align: right;
      background-color: rgba(255, 248, 225, 0.9);
      border-radius: 8px;
      border: 1px solid #fff3cd;
      margin-top: -1px;

      .total-row {
        display: flex;
        justify-content: flex-end;
        align-items: center;

        .total-label {
          font-size: 18px;
          font-weight: 600;
          color: #856404;
          margin-right: 20px;
        }

        .total-value {
          font-size: 32px;
          font-weight: 700;
          color: #e6a23c; // 黄色
        }
      }
    }

    .merchant-info-section {
      padding: 24px;
      margin-top: -1px;

      .info-row {
        margin-bottom: 12px;
        display: flex;
        gap: 12px;

        .info-label {
          width: 120px;
          font-weight: 600;
          color: #333;
        }

        .info-value {
          color: #666;
        }
      }
    }

    .order-records-section {
      padding: 24px;
      margin-top: -1px;

      .records-timeline {
        border-left: 2px solid #e0e0e0;
        padding-left: 20px;
        position: relative;

        .record-item {
          margin-bottom: 24px;
          position: relative;

          &:last-child {
            margin-bottom: 0;
          }

          // Timeline dot
          &::before {
            content: '';
            position: absolute;
            left: -29px;
            top: 4px;
            width: 12px;
            height: 12px;
            background-color: #409eff;
            border-radius: 50%;
            border: 4px solid #fff;
            box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
          }

          .record-time {
            font-size: 13px;
            color: #909399;
            margin-bottom: 8px;
          }

          .record-content {
            background-color: rgba(255, 255, 255, 0.85);
            padding: 16px;
            border-radius: 8px;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
            transition: all 0.3s ease;

            &:hover {
              box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
            }

            .record-action {
              font-weight: 600;
              color: #2c3e50;
              margin-bottom: 4px;
            }

            .record-description {
              font-size: 14px;
              color: #606266;
            }
          }
        }
      }
    }
  }
}
</style>
