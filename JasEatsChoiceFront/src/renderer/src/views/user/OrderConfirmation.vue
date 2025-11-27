<template>
  <div class="order-confirmation-container">
    <div class="main-content">
      <!-- 页面标题 -->
      <div class="page-header">
        <h2 class="page-title">订单确认</h2>
      </div>

      <!-- 订单详情卡片 -->
      <el-card class="order-card">
        <template #header>
          <div class="card-header">
            <span class="card-title">订单信息</span>
          </div>
        </template>

        <!-- 配送地址 -->
        <div class="order-section">
          <div class="section-title">配送地址</div>
          <div class="address-info">
            <div class="address-name">{{ address.name }}</div>
            <div class="address-phone">{{ address.phone }}</div>
            <div class="address-detail">{{ address.province }} {{ address.city }} {{ address.district }} {{ address.street }}</div>
          </div>
          <el-button type="text" class="edit-address-btn" @click="editAddress">
            <el-icon>
              <Edit />
            </el-icon>
            编辑地址
          </div>
        </div>

        <!-- 商品清单 -->
        <div class="order-section">
          <div class="section-title">商品清单</div>
          <div class="order-items">
            <div class="order-item" v-for="item in orderItems" :key="item.id">
              <div class="item-info">
                <div class="item-name">{{ item.name }}</div>
                <div class="item-option" v-if="item.selectedOptionalIngredients.length > 0">
                  选加：{{ item.selectedOptionalIngredients.map(ingredient => ingredient.name).join('，') }}
                </div>
              </div>
              <div class="item-quantity-price">
                <div class="item-quantity">×{{ item.quantity }}</div>
                <div class="item-price">¥{{ (item.price * item.quantity).toFixed(2) }}</div>
              </div>
            </div>
          </div>

          <!-- 订单总计 -->
          <div class="order-total">
            <div class="total-info">
              <div class="total-text">订单总额：</div>
              <div class="total-price">¥{{ totalAmount.toFixed(2) }}</div>
            </div>
          </div>
        </div>

        <!-- 支付方式 -->
        <div class="order-section">
          <div class="section-title">支付方式</div>
          <div class="payment-methods">
            <div
              class="payment-method"
              v-for="method in paymentMethods"
              :key="method.id"
              :class="{ active: selectedPaymentMethod.id === method.id }"
              @click="selectedPaymentMethod = method"
            >
              <div class="method-icon">{{ method.icon }}</div>
              <div class="method-name">{{ method.name }}</div>
              <el-radio v-model="selectedPaymentMethod.id" :label="method.id" class="method-radio"></el-radio>
            </div>
          </div>
        </div>

        <!-- 备注信息 -->
        <div class="order-section">
          <div class="section-title">备注</div>
          <el-input
            v-model="orderNote"
            type="textarea"
            placeholder="如有特殊需求，请在此备注"
            :rows="3"
            class="order-note-input"
          />
        </div>
      </el-card>
    </div>

    <!-- 底部支付按钮 -->
    <div class="bottom-action">
      <div class="total-amount-info">
        <div class="total-label">实付金额：</div>
        <div class="total-amount">¥{{ totalAmount.toFixed(2) }}</div>
      </div>
      <el-button type="primary" size="large" class="confirm-order-btn" @click="confirmOrder">
        确认支付
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Edit } from '@element-plus/icons-vue';
import { useRouter } from 'vue-router';

const router = useRouter();

// 模拟配送地址数据
const address = ref({
  name: '张三',
  phone: '13888888888',
  province: '北京市',
  city: '北京市',
  district: '朝阳区',
  street: '中关村大街123号'
});

// 模拟订单商品数据
const orderItems = ref([
  {
    id: 1,
    name: '经典健康套餐',
    price: 36.8,
    quantity: 2,
    selectedOptionalIngredients: [
      { id: 102, name: '煎蛋', price: 2.5 }
    ]
  },
  {
    id: 4,
    name: '可乐',
    price: 5.5,
    quantity: 1,
    selectedOptionalIngredients: []
  }
]);

// 计算订单总额
const totalAmount = ref(
  orderItems.value.reduce((total, item) => total + (item.price * item.quantity), 0)
);

// 支付方式
const paymentMethods = ref([
  { id: 1, name: '微信支付', icon: '💳' },
  { id: 2, name: '支付宝', icon: '📱' },
  { id: 3, name: '货到付款', icon: '🏦' }
]);

// 选中的支付方式
const selectedPaymentMethod = ref(paymentMethods.value[0]);

// 订单备注
const orderNote = ref('');

// 编辑地址
const editAddress = () => {
  ElMessage.info('编辑地址功能开发中');
};

// 确认支付
const confirmOrder = () => {
  ElMessageBox.confirm('请确认订单信息无误后支付', '订单确认', {
    confirmButtonText: '立即支付',
    cancelButtonText: '取消',
    type: 'warning'
  })
  .then(() => {
    // 这里可以添加真实的支付逻辑
    ElMessage.success('支付成功！您的订单正在处理中');
    // 支付成功后跳转到订单列表页面
    setTimeout(() => {
      router.push('/user/orders');
    }, 1500);
  })
  .catch(() => {
    ElMessage.info('已取消支付');
  });
};
</script>

<style scoped lang="less">
.order-confirmation-container {
  min-height: 100vh;
  background-color: #f5f5f5;

  .main-content {
    max-width: 800px;
    margin: 0 auto;
    padding: 20px;

    .page-header {
      margin-bottom: 20px;

      .page-title {
        font-size: 20px;
        font-weight: 600;
        margin: 0;
        color: #333;
      }
    }

    .order-card {
      margin-bottom: 20px;
      border-radius: 8px;

      .card-header {
        .card-title {
          font-size: 16px;
          font-weight: 600;
          color: #333;
        }
      }

      .order-section {
        margin-bottom: 32px;

        &:last-child {
          margin-bottom: 0;
        }

        .section-title {
          font-size: 14px;
          font-weight: 500;
          color: #606266;
          margin-bottom: 16px;
        }

        // 配送地址
        .address-info {
          margin-bottom: 16px;

          .address-name {
            font-size: 16px;
            font-weight: 500;
            margin-bottom: 4px;
          }

          .address-phone {
            font-size: 14px;
            margin-bottom: 4px;
            color: #666;
          }

          .address-detail {
            font-size: 14px;
            color: #666;
          }
        }

        .edit-address-btn {
          color: #409eff;
        }

        // 商品清单
        .order-items {
          margin-bottom: 24px;

          .order-item {
            display: flex;
            justify-content: space-between;
            margin-bottom: 12px;
            padding-bottom: 12px;
            border-bottom: 1px solid #eee;

            &:last-child {
              margin-bottom: 0;
              padding-bottom: 0;
              border-bottom: none;
            }

            .item-info {
              .item-name {
                font-size: 16px;
                margin-bottom: 4px;
              }

              .item-option {
                font-size: 12px;
                color: #999;
              }
            }

            .item-quantity-price {
              display: flex;
              align-items: center;
              gap: 16px;

              .item-quantity {
                font-size: 14px;
                color: #666;
              }

              .item-price {
                font-size: 16px;
                font-weight: 500;
                color: #ff6b6b;
              }
            }
          }
        }

        .order-total {
          display: flex;
          justify-content: flex-end;
          padding: 16px 0;
          border-top: 1px solid #eee;

          .total-info {
            display: flex;
            gap: 16px;
            align-items: center;

            .total-text {
              font-size: 16px;
              font-weight: 500;
            }

            .total-price {
              font-size: 24px;
              font-weight: 600;
              color: #ff6b6b;
            }
          }
        }

        // 支付方式
        .payment-methods {
          display: flex;
          flex-direction: column;
          gap: 12px;

          .payment-method {
            display: flex;
            align-items: center;
            gap: 16px;
            padding: 12px;
            border-radius: 8px;
            border: 2px solid #e8e8e8;
            cursor: pointer;
            transition: all 0.3s ease;

            &:hover {
              border-color: #409eff;
            }

            &.active {
              border-color: #409eff;
              background-color: rgba(64, 158, 255, 0.1);
            }

            .method-icon {
              font-size: 24px;
            }

            .method-name {
              flex: 1;
              font-size: 16px;
            }

            .method-radio {
              margin-left: auto;
            }
          }
        }

        // 订单备注
        .order-note-input {
          width: 100%;
        }
      }
    }
  }

  // 底部支付按钮
  .bottom-action {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 20px;
    background-color: #fff;
    box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.05);

    .total-amount-info {
      display: flex;
      align-items: center;
      gap: 8px;

      .total-label {
        font-size: 14px;
        color: #666;
      }

      .total-amount {
        font-size: 24px;
        font-weight: 600;
        color: #ff6b6b;
      }
    }

    .confirm-order-btn {
      background-color: #ff6b6b;
      border-color: #ff6b6b;
      padding: 12px 32px;
      font-size: 16px;
      font-weight: 500;

      &:hover {
        background-color: #ff5252;
        border-color: #ff5252;
      }
    }
  }
}
</style>