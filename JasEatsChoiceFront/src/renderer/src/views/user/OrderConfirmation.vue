<template>
  <div class="order-confirmation-container">
    <div class="main-content">
      <div class="page-header">
        <common-back-button type="text" size="small" />
        <h2 class="page-title">
          <template v-if="fromChat">
            <span class="chat-indicator">{{ fromSingleChat ? '👤 ' : '👥 ' }}</span>
            来自{{ fromSingleChat ? '单聊' : '群聊' }}的订单确认
          </template>
          <template v-else> 订单确认 </template>
        </h2>
      </div>

      <el-card class="order-card">
        <template #header>
          <div class="card-header">
            <span class="card-title">订单信息</span>
          </div>
        </template>

        <!-- 订单概览 -->
        <div class="order-section order-overview">
          <div class="section-title">📋 订单概览</div>
          <div class="overview-info">
            <div class="overview-item">
              <span class="info-label">订单号：</span>
              <span class="info-value">{{ orderInfo.orderId }}</span>
            </div>
            <div class="overview-item">
              <span class="info-label">{{ isGroupOrder ? '群名称' : '用户名' }}：</span>
              <span class="info-value">{{
                isGroupOrder ? orderInfo.groupName : orderInfo.userName || '未知用户'
              }}</span>
            </div>
            <div class="overview-item paid-amount">
              <span class="info-label">已支付金额：</span>
              <span class="info-value">{{ orderInfo.totalPaid.toFixed(2) }}元</span>
              <span class="payee-info" v-if="orderInfo.paidItems.length > 0"
                >({{ orderInfo.paidItems[0].payee }}·个人支付)</span
              >
            </div>
            <div class="overview-item unpaid-amount">
              <span class="info-label">未支付金额：</span>
              <span class="info-value">{{ orderInfo.totalUnpaid.toFixed(2) }}元</span>
              <span class="payment-note">(仅需支付未支付部分)</span>
            </div>
          </div>
        </div>

        <!-- 已支付订单 -->
        <div class="order-section paid-orders">
          <div class="section-title">✅ 已支付订单（已锁定不可修改）</div>
          <div class="order-items">
            <div class="order-item" v-for="item in orderInfo.paidItems" :key="item.id">
              <div class="item-info">
                <div class="item-name">📌 {{ item.name }}</div>
                <div class="item-details">
                  <span class="item-quantity">×{{ item.quantity }}</span>
                  <span class="item-price">→ {{ item.price.toFixed(2) }}元/份</span>
                  <span class="item-total"
                    >→
                    {{
                      (item.totalPrice || item.total || item.price * item.quantity).toFixed(2)
                    }}元</span
                  >
                </div>

                <!-- 食材组成 -->
                <div
                  class="item-ingredients"
                  v-if="item.requiredIngredients || item.selectedOptionalIngredients"
                >
                  <div
                    v-if="item.requiredIngredients && item.requiredIngredients.length > 0"
                    class="ingredient-group"
                  >
                    <span class="ingredient-label">必选食材:</span>
                    <div class="ingredient-list">
                      <span
                        class="ingredient-item"
                        v-for="ingredient in item.requiredIngredients"
                        :key="ingredient"
                        >{{ ingredient }}</span
                      >
                    </div>
                  </div>
                  <div
                    v-if="
                      item.selectedOptionalIngredients &&
                      item.selectedOptionalIngredients.length > 0
                    "
                    class="ingredient-group"
                  >
                    <span class="ingredient-label">可选食材:</span>
                    <div class="ingredient-list">
                      <span
                        class="ingredient-item"
                        v-for="ingredient in item.selectedOptionalIngredients"
                        :key="ingredient.id || ingredient"
                        >{{ ingredient.name || ingredient }}</span
                      >
                    </div>
                  </div>
                </div>

                <!-- 菜品备注 -->
                <div class="item-note" v-if="item.note">
                  <span class="note-label">备注:</span>
                  <span class="note-content">{{ item.note }}</span>
                </div>
              </div>
              <div class="payment-info">
                <div class="payee">👤 支付人：{{ item.payee }}</div>
                <div class="payment-method">💳 支付方式：{{ item.paymentMethod }}</div>
                <div class="payment-status">✅ 已完成</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 未支付订单 -->
        <div class="order-section unpaid-orders">
          <div class="section-title">⏳ 未支付订单（可支付/修改）</div>
          <div class="order-items">
            <div class="order-item" v-for="item in orderInfo.unpaidItems" :key="item.id">
              <div class="item-info">
                <div class="item-name">🔥 {{ item.name }}</div>
                <div class="item-details">
                  <span class="item-quantity">×{{ item.quantity }}</span>
                  <span class="item-price">→ {{ item.price.toFixed(2) }}元/份</span>
                  <span class="item-total"
                    >→ {{ (item.totalPrice || item.price * item.quantity).toFixed(2) }}元</span
                  >
                </div>

                <!-- 食材组成 -->
                <div
                  class="item-ingredients"
                  v-if="item.requiredIngredients || item.selectedOptionalIngredients"
                >
                  <div
                    v-if="item.requiredIngredients && item.requiredIngredients.length > 0"
                    class="ingredient-group"
                  >
                    <span class="ingredient-label">必选食材:</span>
                    <div class="ingredient-list">
                      <span
                        class="ingredient-item"
                        v-for="ingredient in item.requiredIngredients"
                        :key="ingredient"
                        >{{ ingredient }}</span
                      >
                    </div>
                  </div>
                  <div
                    v-if="
                      item.selectedOptionalIngredients &&
                      item.selectedOptionalIngredients.length > 0
                    "
                    class="ingredient-group"
                  >
                    <span class="ingredient-label">可选食材:</span>
                    <div class="ingredient-list">
                      <span
                        class="ingredient-item"
                        v-for="ingredient in item.selectedOptionalIngredients"
                        :key="ingredient.id || ingredient"
                        >{{ ingredient.name || ingredient }}</span
                      >
                    </div>
                  </div>
                </div>

                <!-- 菜品备注 -->
                <div class="item-note" v-if="item.note">
                  <span class="note-label">备注:</span>
                  <span class="note-content">{{ item.note }}</span>
                </div>
              </div>
            </div>
          </div>

          <div class="unpaid-total">
            <div class="price-breakdown">
              <span class="total-label">💰 未支付总价：</span>
              <span class="total-value">{{ orderInfo.totalUnpaid.toFixed(2) }}元</span>
            </div>

            <!-- 折叠价格组成详情 -->
            <el-collapse-transition>
              <div v-show="isPriceDetailsOpen" class="price-details-container">
                <div class="price-details">
                  <div
                    class="detail-item"
                    v-for="(item, index) in orderInfo.unpaidItems"
                    :key="index"
                  >
                    <span class="item-name">{{ item.name }} ×{{ item.quantity }}</span>
                    <span class="item-amount"
                      >¥{{ (item.totalPrice || item.price * item.quantity).toFixed(2) }}</span
                    >
                  </div>

                  <!-- 优惠信息 -->
                  <div
                    v-if="
                      orderInfo.originalTotal && orderInfo.originalTotal > orderInfo.totalUnpaid
                    "
                    class="detail-item discount-item"
                  >
                    <span class="item-name">优惠:</span>
                    <span class="item-amount discount-amount"
                      >-¥{{ (orderInfo.originalTotal - orderInfo.totalUnpaid).toFixed(2) }}</span
                    >
                  </div>
                </div>
              </div>
            </el-collapse-transition>

            <!-- 折叠按钮 -->
            <div class="price-details-toggle">
              <el-button type="text" size="small" @click="isPriceDetailsOpen = !isPriceDetailsOpen">
                {{ isPriceDetailsOpen ? '▲' : '▶' }}
                {{ isPriceDetailsOpen ? '收起详情' : '展开详情' }}
              </el-button>
            </div>
          </div>
        </div>

        <!-- 支付规则说明 -->
        <div class="order-section rules">
          <div class="section-title">💡 支付规则说明</div>
          <div class="rule-item">
            <span class="rule-icon">✅ </span>
            <span class="rule-text">已支付订单：已完成支付，不可修改或取消</span>
          </div>
          <div class="rule-item">
            <span class="rule-icon">⏳ </span>
            <span class="rule-text">未支付订单：仅需支付此部分金额，支持修改菜品和支付方式</span>
          </div>
        </div>

        <!-- 支付方式 -->
        <div class="order-section payment-methods-section">
          <div class="section-title">🎯 未支付订单支付方式</div>
          <div class="payment-options">
            <div
              class="payment-option"
              v-for="option in paymentMethods"
              :key="option.id"
              :class="{ active: selectedPaymentMethod.id === option.id }"
              @click="selectedPaymentMethod = option"
            >
              <div class="option-icon">{{ option.icon }}</div>
              <div class="option-name">{{ option.name }}</div>
              <el-radio
                v-model="selectedPaymentMethod.id"
                :label="option.id"
                class="option-radio"
              ></el-radio>
            </div>
          </div>
        </div>

        <!-- 可用优惠 -->
        <div class="order-section discounts">
          <div class="section-title">📥 可用优惠</div>
          <div class="discount-item" v-for="discount in discounts" :key="discount.id">
            <div class="discount-info">
              <span class="discount-icon">🎁 </span>
              <span class="discount-text">{{ discount.name }}</span>
            </div>
            <div v-if="!discount.used">
              <el-button type="text" class="use-discount" @click="useDiscount">立即使用</el-button>
            </div>
            <div v-else>
              <span class="discount-used-text">已使用</span>
              <el-button type="text" class="cancel-discount" @click="cancelDiscount"
                >取消</el-button
              >
            </div>
          </div>
        </div>

        <!-- 支付渠道 -->
        <div class="order-section payment-channels">
          <div class="section-title">📱 支付渠道</div>
          <div class="channel-item">
            <div class="channel-left">
              <span class="channel-icon">💳 </span>
              <span class="channel-text">平台币支付</span>
            </div>
            <span class="channel-balance">可用余额：{{ platformBalance.toFixed(2) }}元</span>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 可拖动悬浮购物车 - 订单确认页面隐藏该按钮 -->
    <div
      ref="cartBallRef"
      class="draggable-cart-ball"
      @mousedown="startDrag"
      @click="viewCart"
      style="display: none"
    >
      <div class="cart-icon">🛒</div>
      <el-badge :value="cartItems.length" class="cart-badge" />
      <div class="cart-amount">¥{{ totalAmount.toFixed(2) }}</div>
    </div>

    <!-- 底部支付按钮 -->
    <div class="bottom-action">
      <div class="total-amount-info">
        <div class="total-label">实付金额：</div>
        <div class="total-amount">¥{{ orderInfo.totalUnpaid.toFixed(2) }}</div>
      </div>
      <el-button type="primary" size="large" class="confirm-order-btn" @click="confirmOrder">
        确认支付未支付订单
      </el-button>
    </div>

    <!-- 购物车弹窗 -->
    <el-dialog v-model="cartVisible" title="我的购物车" width="500px" @close="closeCart">
      <div v-if="cartItems.length === 0" class="empty-cart">
        <div class="empty-cart-icon">🛒</div>
        <div class="empty-cart-text">购物车是空的</div>
      </div>
      <div v-else class="cart-items">
        <div class="cart-item" v-for="(item, index) in cartItems" :key="item.id">
          <div class="cart-item-info">
            <div class="cart-item-name">{{ item.name }}</div>
            <!-- Optional ingredients display -->
            <div
              v-if="item.selectedOptionalIngredients && item.selectedOptionalIngredients.length > 0"
              class="cart-item-ingredients"
            >
              <span
                v-for="(ingredient, idx) in item.selectedOptionalIngredients"
                :key="idx"
                class="ingredient-tag"
              >
                +{{ ingredient.name }} (¥{{ ingredient.price.toFixed(2) }})
              </span>
            </div>
            <!-- Note input -->
            <div class="cart-item-note">
              <el-input
                v-model="item.note"
                placeholder="添加备注..."
                size="small"
                type="textarea"
                :rows="1"
                resize="none"
              />
            </div>
            <div class="cart-item-price">¥{{ item.price.toFixed(2) }}</div>
          </div>
          <div class="cart-item-quantity">
            <el-button
              type="text"
              size="small"
              @click="
                () => {
                  cartItems[index].quantity--
                  if (cartItems[index].quantity <= 0) cartItems.splice(index, 1)
                }
              "
            >
              -
            </el-button>
            <span class="quantity-value">{{ item.quantity }}</span>
            <el-button type="text" size="small" @click="cartItems[index].quantity++"> + </el-button>
          </div>
          <div class="cart-item-total">¥{{ item.totalPrice.toFixed(2) }}</div>
        </div>
        <div class="cart-total">
          <div class="total-text">总计:</div>
          <div class="total-price">
            ¥{{ cartItems.reduce((total, item) => total + item.totalPrice, 0).toFixed(2) }}
          </div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="closeCart">取消</el-button>
          <el-button type="primary" v-if="cartItems.length > 0" @click="updateOrderInfo">
            更新订单
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- AA支付模态框 -->
    <el-dialog v-model="aaPaymentModalVisible" title="AA支付确认" width="400px">
      <div class="aa-payment-content">
        <div class="aa-info">
          <div class="info-item">
            <span class="info-label">订单总金额:</span>
            <span class="info-value">¥{{ orderInfo.totalUnpaid.toFixed(2) }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">参与人数:</span>
            <span class="info-value">{{ orderInfo.members.length }}人</span>
          </div>
          <div class="info-item">
            <span class="info-label">每人需支付:</span>
            <span class="info-value highlight">¥{{ aaShareAmount.toFixed(2) }}</span>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="aaPaymentModalVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmAAPayment">确认AA支付</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 自定义分摊模态框 -->
    <el-dialog v-model="customShareModalVisible" title="自定义分摊" width="500px">
      <div class="custom-share-content">
        <div class="custom-info">
          <div class="info-item">
            <span class="info-label">订单总金额:</span>
            <span class="info-value">¥{{ orderInfo.totalUnpaid.toFixed(2) }}</span>
          </div>
        </div>

        <div class="share-list">
          <div class="share-item" v-for="(share, index) in customShares" :key="index">
            <div class="member-name">{{ share.member }}</div>
            <el-input-number
              v-model="share.amount"
              :min="0"
              :precision="2"
              :step="0.01"
              style="width: 120px"
              @change="updateCustomShare(index, share.amount)"
            />
          </div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="customShareModalVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmCustomShare">确认自定义分摊</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import CommonBackButton from '../../components/common/CommonBackButton.vue'

const router = useRouter()

// 从会话存储获取订单信息
const pendingOrder = JSON.parse(sessionStorage.getItem('pendingOrder')) || {}

// 检查订单是否为空
const isEmptyOrder = !pendingOrder.cartItems || pendingOrder.cartItems.length === 0

// 订单信息（将购物车商品作为未支付订单）
const orderInfo = ref({
  orderId: `JD${new Date().getTime()}`,
  groupName: pendingOrder.groupName || '默认订单群',
  userName: pendingOrder.userName || '',
  creator: pendingOrder.creator || '', // 添加订单创建者信息
  paidItems: [],
  unpaidItems: pendingOrder.cartItems || [],
  totalPaid: 0.0,
  totalUnpaid:
    pendingOrder.totalAmount ||
    (pendingOrder.cartItems || []).reduce(
      (total, item) => total + (item.totalPrice || item.price * item.quantity),
      0
    )
})

// 如果订单为空，返回上一页并提示
if (isEmptyOrder) {
  ElMessage.warning('购物车为空，无法进行订单确认')
  router.back()
}

// 购物车数据（用于悬浮购物车显示）
const cartItems = ref(pendingOrder.cartItems || [])
const totalAmount = ref(pendingOrder.totalAmount || 0)
const cartVisible = ref(false)

// 可拖动购物车相关
const cartBallRef = ref(null)
let isDragging = false
let startX = 0
let startY = 0
let initialX = 0
let initialY = 0

// 开始拖动
const startDrag = (e) => {
  if (!cartBallRef.value) return

  isDragging = true
  startX = e.clientX
  startY = e.clientY

  // 获取购物车球的初始位置
  const rect = cartBallRef.value.getBoundingClientRect()
  initialX = rect.left
  initialY = rect.top

  // 添加事件监听
  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', stopDrag)

  // 阻止点击事件
  e.stopPropagation()
}

// 拖动中
const onDrag = (e) => {
  if (!isDragging || !cartBallRef.value) return

  const dx = e.clientX - startX
  const dy = e.clientY - startY

  // 计算新位置
  let newX = initialX + dx
  let newY = initialY + dy

  // 限制在视窗内
  const windowWidth = window.innerWidth
  const windowHeight = window.innerHeight
  const cartWidth = cartBallRef.value.offsetWidth
  const cartHeight = cartBallRef.value.offsetHeight

  newX = Math.max(0, Math.min(newX, windowWidth - cartWidth))
  newY = Math.max(0, Math.min(newY, windowHeight - cartHeight))

  // 更新位置
  cartBallRef.value.style.left = `${newX}px`
  cartBallRef.value.style.top = `${newY}px`
}

// 停止拖动
const stopDrag = () => {
  isDragging = false
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
}

// 查看购物车
const viewCart = () => {
  if (!isDragging) {
    cartVisible.value = true
  }
}

// 关闭购物车
const closeCart = () => {
  cartVisible.value = false
}

// 更新订单信息
const updateOrderInfo = () => {
  // 更新订单信息
  orderInfo.value.unpaidItems = cartItems.value
  orderInfo.value.totalUnpaid = cartItems.reduce((total, item) => total + item.totalPrice, 0)

  // 保存更新后的购物车到会话存储
  const updatedOrder = { ...pendingOrder }
  updatedOrder.cartItems = cartItems.value
  updatedOrder.totalAmount = orderInfo.value.totalUnpaid
  sessionStorage.setItem('pendingOrder', JSON.stringify(updatedOrder))

  // 关闭购物车并提示
  closeCart()
  ElMessage.success('订单已更新')
}

// 检测订单类型：群聊订单或单独订单
const isGroupOrder = ref(orderInfo.value.groupName !== '默认订单群')
// 检测是否来自聊天（群聊或单聊）
const fromChat = ref(pendingOrder.fromChat || false)
// 检测是否来自单聊
const fromSingleChat = ref(fromChat.value && !isGroupOrder.value)

// 支付方式 - 根据订单类型和身份动态显示
// 仅订单发起者可以看到AA支付和自定义分摊
const paymentMethods = ref(
  isGroupOrder.value
    ? [
        { id: 1, name: '个人下单', icon: '👤' },
        // 仅订单发起者显示其他支付方式
        ...(orderInfo.value.creator === '我'
          ? [
              { id: 2, name: '统一提交集中支付', icon: '🧮' },
              { id: 3, name: 'AA自动拆分', icon: '🎉' },
              { id: 4, name: '自定义分摊', icon: '📝' }
            ]
          : [])
      ]
    : [
        { id: 1, name: '个人支付', icon: '💳' },
        { id: 2, name: '他人代付', icon: '🤝' }
      ]
)

// 更新默认选中支付方式
const selectedPaymentMethod = ref(paymentMethods.value[0])

// AA支付相关
const aaPaymentModalVisible = ref(false)
const aaShareAmount = ref(0)

// 自定义分摊相关
const customShareModalVisible = ref(false)
const customShares = ref([])

// 计算AA支付每人金额
const calculateAAShare = () => {
  if (orderInfo.value && orderInfo.value.totalUnpaid && orderInfo.value.members.length > 0) {
    const share = orderInfo.value.totalUnpaid / orderInfo.value.members.length
    aaShareAmount.value = parseFloat(share.toFixed(2))
  }
}

// 初始化自定义分摊
const initCustomShares = () => {
  if (orderInfo.value && orderInfo.value.members.length > 0) {
    customShares.value = orderInfo.value.members.map((member) => ({
      member,
      amount: parseFloat((orderInfo.value.totalUnpaid / orderInfo.value.members.length).toFixed(2))
    }))
  }
}

// 打开AA支付模态框
const openAAPaymentModal = () => {
  calculateAAShare()
  aaPaymentModalVisible.value = true
}

// 打开自定义分摊模态框
const openCustomShareModal = () => {
  initCustomShares()
  customShareModalVisible.value = true
}

// 确认AA支付
const confirmAAPayment = () => {
  // 这里可以添加AA支付的实际逻辑
  aaPaymentModalVisible.value = false
  ElMessage.success('AA支付已发起，将自动为每位成员创建支付订单')

  // 清除会话存储中的未完成订单
  sessionStorage.removeItem('pendingOrder')

  // 跳转到订单列表页
  setTimeout(() => {
    router.push('/user/home/orders')
  }, 1500)
}

// 确认自定义分摊
const confirmCustomShare = () => {
  // 验证分摊总额是否等于订单总额
  const totalShare = customShares.value.reduce((sum, share) => sum + share.amount, 0)
  if (Math.abs(totalShare - orderInfo.value.totalUnpaid) > 0.01) {
    ElMessage.error('分摊总额必须等于订单总额')
    return
  }

  // 这里可以添加自定义分摊的实际逻辑
  customShareModalVisible.value = false
  ElMessage.success('自定义分摊已发起，将为每位成员创建对应金额的支付订单')

  // 清除会话存储中的未完成订单
  sessionStorage.removeItem('pendingOrder')

  // 跳转到订单列表页
  setTimeout(() => {
    router.push('/user/home/orders')
  }, 1500)
}

// 更新自定义分摊金额
const updateCustomShare = (index, amount) => {
  customShares.value[index].amount = parseFloat(amount)
}

// 平台币余额
const platformBalance = ref(125.0)

// 可用优惠
const discounts = ref([
  {
    id: 1,
    name: '新用户专享50元优惠券',
    amount: 50.0,
    available: true,
    used: false
  }
])

// 已选择的优惠
const selectedDiscount = ref(null)

// 价格详情折叠状态
const isPriceDetailsOpen = ref(true)

// 使用优惠
const useDiscount = () => {
  const discount = discounts.value[0]
  if (!discount || !discount.available || discount.used) return

  // 应用优惠
  selectedDiscount.value = discount
  discount.used = true

  // 保存原价
  if (!orderInfo.value.originalTotal) {
    orderInfo.value.originalTotal = orderInfo.value.totalUnpaid
  }

  // 更新订单金额
  const discountAmount = Math.min(discount.amount, orderInfo.value.totalUnpaid)
  orderInfo.value.totalUnpaid -= discountAmount

  ElMessage.success('优惠已使用')
}

// 取消使用优惠
const cancelDiscount = () => {
  if (!selectedDiscount.value) return

  // 恢复订单金额
  const discountAmount = Math.min(
    selectedDiscount.value.amount,
    orderInfo.value.totalUnpaid + selectedDiscount.value.amount
  )
  orderInfo.value.totalUnpaid += discountAmount

  // 移除原价记录
  delete orderInfo.value.originalTotal

  // 标记优惠为未使用
  selectedDiscount.value.used = false
  selectedDiscount.value = null

  ElMessage.success('优惠已取消')
}

const confirmOrder = () => {
  // 根据不同支付方式处理
  switch (selectedPaymentMethod.value.id) {
    case 2: // 统一提交集中支付
      // 普通支付流程
      ElMessageBox.confirm('请确认订单信息无误后支付', '订单确认', {
        confirmButtonText: '立即支付',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => {
          // 清除会话存储中的未完成订单
          sessionStorage.removeItem('pendingOrder')

          ElMessage.success('支付成功！您的订单正在处理中')
          setTimeout(() => {
            router.push('/user/home/orders')
          }, 1500)
        })
        .catch(() => {
          ElMessage.info('已取消支付')
        })
      break

    case 3: // AA自动拆分
      openAAPaymentModal()
      break

    case 4: // 自定义分摊
      openCustomShareModal()
      break

    case 2: // 他人代付
      // 现有他人代付逻辑保持不变
      ElMessageBox.prompt('请输入代付人手机号码或昵称:', '他人代付', {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        inputPattern: /^1[3456789]\d{9}$|^[\u4e00-\u9fa5]{2,8}$/,
        inputErrorMessage: '请输入有效的手机号码或2-8位中文昵称'
      })
        .then(({ value }) => {
          // 这里可以添加发送代付请求的逻辑
          // 清除会话存储中的未完成订单
          sessionStorage.removeItem('pendingOrder')

          ElMessage.success(`代付请求已发送给${value}！`)
          setTimeout(() => {
            router.push('/user/home/orders')
          }, 1500)
        })
        .catch(() => {
          ElMessage.info('已取消代付')
        })
      break

    default: // 个人支付
      // 普通支付流程
      ElMessageBox.confirm('请确认订单信息无误后支付', '订单确认', {
        confirmButtonText: '立即支付',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => {
          // 清除会话存储中的未完成订单
          sessionStorage.removeItem('pendingOrder')

          ElMessage.success('支付成功！您的订单正在处理中')
          setTimeout(() => {
            router.push('/user/home/orders')
          }, 1500)
        })
        .catch(() => {
          ElMessage.info('已取消支付')
        })
      break
  }
}
</script>

<style scoped lang="less">
.order-confirmation-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding-bottom: 100px;

  .main-content {
    max-width: 900px;
    margin: 30px auto;
    padding: 20px;

    .page-header {
      margin-bottom: 25px;
      padding: 20px;
      background: rgba(255, 255, 255, 0.95);
      border-radius: 12px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

      .back-button {
        font-size: 18px;
        margin-bottom: 10px;
        color: #409eff;
        &:hover {
          color: #66b1ff;
        }
      }

      .page-title {
        font-size: 24px;
        font-weight: 700;
        margin: 0;
        color: #2c3e50;

        .chat-indicator {
          font-size: 28px;
          margin-right: 8px;
        }
      }
    }

    .order-card {
      margin-bottom: 20px;
      border-radius: 12px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);

      .card-header {
        .card-title {
          font-size: 18px;
          font-weight: 600;
          color: #2c3e50;
        }
      }

      .order-section {
        margin-bottom: 36px;

        &:last-child {
          margin-bottom: 0;
        }

        .section-title {
          font-size: 16px;
          font-weight: 600;
          color: #34495e;
          margin-bottom: 20px;
          display: flex;
          align-items: center;
          gap: 8px;
        }

        // 订单概览
        &.order-overview {
          .overview-info {
            .overview-item {
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
                min-width: 100px;
              }

              .info-value {
                color: #2c3e50;
                font-size: 15px;
              }

              &.paid-amount {
                border-left: 4px solid #67c23a; // 绿色左边框

                .info-value {
                  color: #67c23a; // 绿色
                  font-weight: 600;
                }
              }

              &.unpaid-amount {
                border-left: 4px solid #e6a23c; // 黄色左边框

                .info-value {
                  color: #e6a23c; // 黄色
                  font-weight: 600;
                }
              }

              .payee-info,
              .payment-note {
                font-size: 13px;
                color: #909399;
                margin-left: 10px;
              }
            }
          }
        }

        // 订单列表
        .order-items {
          margin-bottom: 28px;

          .order-item {
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
                margin-bottom: 10px;
              }

              .item-ingredients {
                background: rgba(240, 242, 245, 0.7);
                padding: 10px 12px;
                border-radius: 6px;
                margin-bottom: 10px;
                font-size: 14px;

                .ingredient-group {
                  margin-bottom: 6px;

                  &:last-child {
                    margin-bottom: 0;
                  }
                }

                .ingredient-label {
                  font-weight: 600;
                  color: #333;
                  margin-right: 8px;
                }

                .ingredient-list {
                  display: inline;

                  .ingredient-item {
                    margin-right: 8px;
                    padding: 4px 8px;
                    background: rgba(255, 255, 255, 0.8);
                    border-radius: 4px;
                    color: #555;
                    font-size: 13px;
                  }
                }
              }

              .item-note {
                background: rgba(255, 248, 225, 0.7);
                padding: 10px 12px;
                border-radius: 6px;
                font-size: 14px;
                display: flex;
                align-items: flex-start;

                .note-label {
                  font-weight: 600;
                  color: #856404;
                  margin-right: 8px;
                  white-space: nowrap;
                }

                .note-content {
                  color: #856404;
                  flex: 1;
                }
              }
            }

            .payment-info {
              font-size: 13px;
              text-align: right;
              display: flex;
              flex-direction: column;
              gap: 4px;
              color: #666;

              .payee {
                font-weight: 600;
                color: #333;
              }
            }
          }
        }

        // 未支付订单
        &.unpaid-orders {
          .unpaid-total {
            padding: 20px;
            background: rgba(255, 248, 225, 0.9);
            border-radius: 8px;
            border: 1px solid #fff3cd;

            .price-breakdown {
              display: flex;
              justify-content: flex-end;
              align-items: center;
              margin-bottom: 0;
            }

            .price-details-toggle {
              text-align: right;
              margin-top: 8px;
            }

            .price-details-container {
              margin-top: 16px;
              border-top: 1px dashed #ffeeba;
              padding-top: 12px;
            }

            .price-details {
              .detail-item {
                display: flex;
                justify-content: space-between;
                font-size: 14px;
                margin-bottom: 8px;
                color: #666;

                &.discount-item {
                  color: #67c23a;

                  .item-amount.discount-amount {
                    color: #f56c6c;
                  }
                }
              }
            }

            .total-label {
              font-size: 18px;
              font-weight: 600;
              color: #856404;
            }

            .total-value {
              font-size: 32px;
              font-weight: 700;
              color: #e6a23c; // 黄色
              margin-left: 20px;
            }
          }
        }

        // 规则说明
        &.rules {
          .rule-item {
            margin-bottom: 12px;
            font-size: 14px;
            padding: 10px 14px;
            background: rgba(255, 255, 255, 0.85);
            border-radius: 6px;
            display: flex;
            align-items: center;
            gap: 10px;
            transition: all 0.3s ease;

            &:hover {
              background: rgba(255, 255, 255, 1);
            }

            .rule-icon {
              font-weight: bold;
              color: #409eff;
            }

            .rule-text {
              color: #555;
            }
          }
        }

        // 支付方式
        .payment-options {
          display: flex;
          flex-direction: column;
          gap: 15px;

          .payment-option {
            display: flex;
            align-items: center;
            gap: 20px;
            padding: 18px 20px;
            border-radius: 12px;
            border: 2px solid #e0e0e0;
            cursor: pointer;
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            background: rgba(255, 255, 255, 0.95);

            &:hover {
              border-color: #409eff;
              transform: translateY(-2px);
              box-shadow: 0 4px 16px rgba(64, 158, 255, 0.2);
            }

            &.active {
              border-color: #409eff;
              background-color: rgba(64, 158, 255, 0.08);
              box-shadow: 0 4px 16px rgba(64, 158, 255, 0.2);
            }

            .option-icon {
              font-size: 28px;
              line-height: 1;
            }

            .option-name {
              flex: 1;
              font-size: 17px;
              font-weight: 500;
              color: #2c3e50;
            }

            .option-radio {
              margin-left: auto;
            }
          }
        }

        // 优惠
        &.discounts {
          .discount-item {
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 16px;
            padding: 18px 20px;
            background: linear-gradient(135deg, #fffbe8 0%, #fef9c3 100%);
            border: 2px solid #ffeeba;
            border-radius: 10px;
            box-shadow: 0 2px 8px rgba(234, 162, 60, 0.1);

            .discount-info {
              display: flex;
              align-items: center;
              gap: 12px;
            }

            .discount-icon {
              font-size: 24px;
            }

            .discount-text {
              font-size: 15px;
              font-weight: 600;
              color: #856404;
            }

            .use-discount {
              color: #e6a23c;
              font-weight: 600;
              transition: color 0.3s ease;

              &:hover {
                color: #d89a33;
              }
            }

            .discount-used-text {
              color: #67c23a;
              font-weight: 600;
              margin-right: 8px;
            }

            .cancel-discount {
              color: #f56c6c;
              font-weight: 600;
              transition: color 0.3s ease;

              &:hover {
                color: #f78989;
              }
            }
          }
        }

        // 支付渠道
        &.payment-channels {
          .channel-item {
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 12px;
            padding: 16px 20px;
            background: rgba(255, 255, 255, 0.9);
            border-radius: 10px;

            .channel-left {
              display: flex;
              align-items: center;
              gap: 14px;
            }

            .channel-icon {
              font-size: 24px;
            }

            .channel-text {
              font-size: 16px;
              font-weight: 500;
            }

            .channel-balance {
              color: #67c23a;
              font-weight: 600;
              font-size: 16px;
            }
          }
        }
      }
    }
  }

  .bottom-action {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px 40px;
    background: rgba(255, 255, 255, 0.98);
    box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.1);
    backdrop-filter: blur(10px);

    .total-amount-info {
      display: flex;
      flex-direction: column;
      gap: 4px;

      .total-label {
        font-size: 14px;
        color: #7f8c8d;
      }

      .total-amount {
        font-size: 36px;
        font-weight: 700;
        color: #e6a23c; // 黄色
      }
    }

    .confirm-order-btn {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border: none;
      padding: 16px 48px;
      font-size: 18px;
      font-weight: 600;
      color: white;
      border-radius: 50px;
      box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4);
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

      &:hover {
        background: linear-gradient(135deg, #7c8ff0 0%, #865aba 100%);
        box-shadow: 0 6px 24px rgba(102, 126, 234, 0.6);
        transform: translateY(-2px);
      }

      &:active {
        transform: translateY(0);
      }
    }
  }

  // 可拖动悬浮购物车
  .draggable-cart-ball {
    position: fixed;
    right: 24px;
    bottom: 80px;
    width: 88px;
    height: 88px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 50%;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    cursor: grab;
    box-shadow: 0 8px 32px rgba(102, 126, 234, 0.6);
    color: white;
    font-size: 12px;
    transition: all 0.3s ease;
    z-index: 9999;
    border: 3px solid rgba(255, 255, 255, 0.9);
    backdrop-filter: blur(10px);

    &:active {
      cursor: grabbing;
      transform: scale(1.05);
      box-shadow: 0 12px 48px rgba(102, 126, 234, 0.8);
    }

    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 12px 48px rgba(102, 126, 234, 0.8);
    }

    .cart-icon {
      font-size: 32px;
      margin-bottom: 2px;
      position: relative;
    }

    .cart-badge {
      position: absolute;
      top: -8px;
      right: -8px;
    }

    .cart-amount {
      font-size: 11px;
      font-weight: 600;
    }
  }

  // 购物车列表样式
  .cart-items {
    max-height: 300px; /* 设置购物车最大高度 */
    overflow-y: auto; /* 超出部分显示滚动条 */
    padding-right: 8px; /* 为滚动条预留空间 */
  }
}

/* AA支付模态框样式 */
.aa-payment-content {
  padding: 20px 0;

  .aa-info {
    .info-item {
      margin-bottom: 15px;
      display: flex;
      justify-content: space-between;

      .info-label {
        font-weight: 600;
      }

      .info-value {
        font-size: 16px;
        color: #303133;

        &.highlight {
          color: #e6a23c;
          font-weight: 600;
          font-size: 20px;
        }
      }
    }
  }
}

/* 自定义分摊模态框样式 */
.custom-share-content {
  padding: 20px 0;

  .custom-info {
    margin-bottom: 20px;

    .info-item {
      display: flex;
      justify-content: space-between;

      .info-label {
        font-weight: 600;
      }

      .info-value {
        font-size: 16px;
        color: #303133;
      }
    }
  }

  .share-list {
    .share-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 15px;

      .member-name {
        font-weight: 500;
      }
    }
  }
}
</style>
