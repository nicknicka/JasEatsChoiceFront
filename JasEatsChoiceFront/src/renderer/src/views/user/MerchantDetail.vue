<template>
  <div class="merchant-detail-container">
    <el-card class="merchant-detail-card">
      <!-- 返回按钮 -->
      <div class="back-button-container">
        <el-button
          type="text"
          @click="router.back()"
          class="back-button"
        >
          ↩️ 返回
        </el-button>
      </div>

      <!-- 商家头部信息 -->
      <div class="merchant-header">
        <div class="header-left">
          <div class="merchant-name-main">{{ merchant.name }}</div>
          <el-button
            type="text"
            size="small"
            class="favorite-button"
            @click="toggleFavorite"
          >
            {{ isFavorite ? '❤️' : '🤍' }} {{ isFavorite ? '已收藏' : '收藏' }}
          </el-button>
        </div>
        <div class="header-right">
        </div>
      </div>

      <!-- 商家Banner区 -->
      <div class="merchant-banner">
        <div class="banner-content">
          {{ merchant.image }}
        </div>
      </div>

      <!-- 商家基本信息 -->
      <div class="merchant-basic-info">
        <div class="basic-info-section">
          <div class="merchant-rating-main">
            <el-rate
              v-model="merchant.rating"
              :disabled="true"
              show-text
              :max="5"
              :precision="1"
            />
          </div>
          <div class="merchant-location">
            <el-icon class="location-icon">📍</el-icon>
            <span>中关村大街123号</span>
          </div>
          <div class="merchant-hours">
            <el-icon class="clock-icon">⏰</el-icon>
            <span>11:00-22:00</span>
          </div>
          <div class="merchant-average">
            <el-icon class="money-icon">💰</el-icon>
            <span>人均88元</span>
          </div>
        </div>
      </div>

      <!-- 菜单类型切换 -->
      <div class="menu-tabs">
        <el-tabs
          class="merchant-menu-tabs"
          :model-value="activeMenuTab"
          @update:model-value="activeMenuTab = $event"
        >
          <el-tab-pane
            v-for="tab in menuTabs"
            :key="tab.value"
            :label="tab.label"
            :name="tab.value"
          >
            <!-- Tab content will be handled by v-if based on activeMenuTab -->
          </el-tab-pane>
        </el-tabs>
      </div>

      <!-- 菜单展示区 -->
      <div class="menu-display-area">
        <!-- 当前菜单名称 (仅在非用户评价标签时显示) -->
        <div v-if="activeMenuTab !== 'comments'" class="current-menu-name">
          <h2 class="menu-name-title">{{ currentMenuName }}</h2>
        </div>

        <!-- 招牌菜 -->
        <div v-if="activeMenuTab !== 'comments' && menuItems.some(item => item.menuId === activeMenuTab && item.category === 'signature')" class="dish-category-section">
          <h3 class="category-title">🔥 招牌菜</h3>
          <div class="dish-grid">
            <div class="dish-card" v-for="item in menuItems.filter(item => item.menuId === activeMenuTab && item.category === 'signature')" :key="item.id">
              <div class="dish-image">{{ item.image || '🍱' }}</div>
              <div class="dish-name">{{ item.name }}</div>
              <div class="dish-price">¥{{ calculateRealTimePrice(item).toFixed(2) }}</div>
              <div class="dish-desc">{{ item.description }}</div>

              <!-- 食材组成 -->
              <div class="dish-ingredients">
                <div class="ingredient-section">
                  <span class="ingredient-title">必选食材:</span>
                  <div class="ingredient-list">
                    <span class="ingredient-item" v-for="ingredient in item.requiredIngredients" :key="ingredient">{{ ingredient }}</span>
                  </div>
                </div>

                <div class="ingredient-section">
                  <span class="ingredient-title">可选食材:</span>
                  <div class="ingredient-list">
                    <el-checkbox
                      v-for="ingredient in item.optionalIngredients"
                      :key="ingredient.id"
                      v-model="ingredient.selected"
                      class="ingredient-checkbox"
                    >
                      {{ ingredient.name }} <span class="ingredient-price">(+¥{{ ingredient.price.toFixed(2) }})</span>
                    </el-checkbox>
                  </div>
                </div>
              </div>

              <!-- 数量选择 -->
              <div class="dish-quantity">
                <el-input-number
                  v-model="item.quantity"
                  :min="1"
                  :max="10"
                  label="数量"
                  style="width: 100%;"
                />
              </div>

              <el-button
                type="primary"
                size="small"
                @click="addMenuItem(item)"
                style="width: 100%;"
              >
                {{ viewMode === 'order' ? '立即购买' : '加入购物车' }}
              </el-button>
            </div>
          </div>
        </div>

        <!-- 主食 -->
        <div v-if="activeMenuTab !== 'comments' && menuItems.some(item => item.menuId === activeMenuTab && item.category === 'staple')" class="dish-category-section">
          <h3 class="category-title">🍚 主食</h3>
          <div class="dish-grid">
            <div class="dish-card" v-for="item in menuItems.filter(item => item.menuId === activeMenuTab && item.category === 'staple')" :key="item.id">
              <div class="dish-image">🍚</div>
              <div class="dish-name">{{ item.name }}</div>
              <div class="dish-price">¥{{ calculateRealTimePrice(item).toFixed(2) }}</div>

              <!-- 食材组成 -->
              <div class="dish-ingredients">
                <div class="ingredient-section">
                  <span class="ingredient-title">必选食材:</span>
                  <div class="ingredient-list">
                    <span class="ingredient-item" v-for="ingredient in item.requiredIngredients" :key="ingredient">{{ ingredient }}</span>
                  </div>
                </div>

                <div class="ingredient-section">
                  <span class="ingredient-title">可选食材:</span>
                  <div class="ingredient-list">
                    <el-checkbox
                      v-for="ingredient in item.optionalIngredients"
                      :key="ingredient.id"
                      v-model="ingredient.selected"
                      class="ingredient-checkbox"
                    >
                      {{ ingredient.name }} <span class="ingredient-price">(+¥{{ ingredient.price.toFixed(2) }})</span>
                    </el-checkbox>
                  </div>
                </div>
              </div>

              <!-- 数量选择 -->
              <div class="dish-quantity">
                <el-input-number
                  v-model="item.quantity"
                  :min="1"
                  :max="10"
                  label="数量"
                  style="width: 100%;"
                />
              </div>

              <el-button
                type="primary"
                size="small"
                @click="addMenuItem(item)"
                style="width: 100%;"
              >
                {{ viewMode === 'order' ? '立即购买' : '加入购物车' }}
              </el-button>
            </div>
          </div>
        </div>

        <!-- 饮品 -->
        <div v-if="activeMenuTab !== 'comments' && menuItems.some(item => item.menuId === activeMenuTab && item.category === 'drink')" class="dish-category-section">
          <h3 class="category-title">🥤 饮品</h3>
          <div class="dish-grid">
            <div class="dish-card" v-for="item in menuItems.filter(item => item.menuId === activeMenuTab && item.category === 'drink')" :key="item.id">
              <div class="dish-image">🥤</div>
              <div class="dish-name">{{ item.name }}</div>
              <div class="dish-price">¥{{ calculateRealTimePrice(item).toFixed(2) }}</div>

              <!-- 食材组成 -->
              <div class="dish-ingredients">
                <div class="ingredient-section">
                  <span class="ingredient-title">必选食材:</span>
                  <div class="ingredient-list">
                    <span class="ingredient-item" v-for="ingredient in item.requiredIngredients" :key="ingredient">{{ ingredient }}</span>
                  </div>
                </div>

                <div class="ingredient-section">
                  <span class="ingredient-title">可选食材:</span>
                  <div class="ingredient-list">
                    <el-checkbox
                      v-for="ingredient in item.optionalIngredients"
                      :key="ingredient.id"
                      v-model="ingredient.selected"
                      class="ingredient-checkbox"
                    >
                      {{ ingredient.name }} <span class="ingredient-price">(+¥{{ ingredient.price.toFixed(2) }})</span>
                    </el-checkbox>
                  </div>
                </div>
              </div>

              <!-- 数量选择 -->
              <div class="dish-quantity">
                <el-input-number
                  v-model="item.quantity"
                  :min="1"
                  :max="10"
                  label="数量"
                  style="width: 100%;"
                />
              </div>

              <el-button
                type="primary"
                size="small"
                @click="addMenuItem(item)"
                style="width: 100%;"
              >
                {{ viewMode === 'order' ? '立即购买' : '加入购物车' }}
              </el-button>
            </div>
          </div>
        </div>

        <!-- 用户评价 -->
        <div v-else-if="activeMenuTab === 'comments'" class="comments-section">
          <h3 class="category-title">⭐ 用户评价</h3>

          <!-- 商家没有菜单的提示 -->
          <div v-if="!hasMenus" class="no-menus-notice">
            <p class="notice-text">当前商家还没有上架菜单</p>
          </div>

          <div class="comments-list">
            <div class="comment-card" v-for="comment in comments" :key="comment.id">
              <div class="comment-header">
                <div class="comment-user-info">
                  <span class="user-name">{{ comment.userName }}</span>
                  <span class="comment-date">{{ comment.date }}</span>
                </div>
                <el-rate
                  v-model="comment.rating"
                  :disabled="true"
                  size="small"
                  show-text
                />
              </div>
              <div class="comment-content">
                {{ comment.comment }}
              </div>

              <!-- 展开/折叠回复按钮 -->
              <div v-if="comment.replies && comment.replies.length > 0" class="reply-toggle">
                <el-button
                  type="text"
                  size="small"
                  @click="comment.expandReplies = !comment.expandReplies"
                >
                  {{ comment.expandReplies ? '▼ 收起回复' : '▶ 查看回复 (' + comment.replies.length + ')' }}
                </el-button>
              </div>

              <!-- 回复列表 -->
              <div v-if="comment.expandReplies && comment.replies.length > 0" class="replies-list">
                <div
                  class="reply-card"
                  v-for="reply in comment.replies"
                  :key="reply.id"
                  :class="{ 'merchant-reply': reply.type === 'merchant' }"
                >
                  <div class="reply-header">
                    <span class="reply-username">
                      {{ reply.userName }}
                      <span v-if="reply.type === 'merchant'" class="merchant-badge">商家</span>
                    </span>
                    <span class="reply-date">{{ reply.date }}</span>
                  </div>
                  <div class="reply-content">
                    {{ reply.comment }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 立即下单快捷操作区（仅在order模式下显示） -->
      <div
        v-if="viewMode === 'order' && hasMenus"
        class="quick-order-section"
      >
        <el-button type="primary" size="large" class="quick-order-button" @click="goToOrderConfirmation">
          进入订单确认页
        </el-button>
      </div>

      <!-- 可拖动悬浮购物车 -->
      <div
        ref="cartBallRef"
        class="draggable-cart-ball"
        @mousedown="startDrag"
        @click="viewCart"
      >
        <div class="cart-icon">🛒</div>
        <el-badge :value="cartTotalQuantity" class="cart-badge" />
        <div class="cart-amount">¥{{ cartTotalAmount.toFixed(2) }}</div>
      </div>
    </el-card>

    <!-- 购物车弹窗 -->
    <el-dialog
      v-model="cartVisible"
      title="我的购物车"
      width="500px"
      @close="closeCart"
      :lock-scroll="false"
    >
      <div class="cart-content">
        <div v-if="cartItems.length === 0" class="empty-cart">
          <div class="empty-cart-icon">🛒</div>
          <div class="empty-cart-text">购物车是空的</div>
        </div>
        <div v-else class="cart-items">
          <div class="cart-item" v-for="(item, index) in cartItems" :key="item.id">
            <div class="cart-item-info">
              <div class="cart-item-name">{{ item.name }}</div>
              <!-- Optional ingredients display -->
              <div v-if="item.selectedOptionalIngredients && item.selectedOptionalIngredients.length > 0" class="cart-item-ingredients">
                <span v-for="(ingredient, idx) in item.selectedOptionalIngredients" :key="idx" class="ingredient-tag">
                  +{{ ingredient.name }} (¥{{ ingredient.price.toFixed(2) }})
                </span>
              </div>
              <!-- Note display and edit -->
              <div class="cart-item-note">
                <div class="note-display" v-if="!item.isEditingNote">
                  <span v-if="item.note" class="note-text">{{ item.note }}</span>
                  <span v-else class="note-empty">暂无备注</span>
                  <el-button
                    size="small"
                    class="edit-note-btn"
                    @click="item.isEditingNote = true"
                  >
                    <el-icon class="edit-icon">✏️</el-icon>
                  </el-button>
                </div>
                <div class="note-edit" v-else>
                  <el-input
                    v-model="item.tempNote"
                    placeholder="输入备注..."
                    size="small"
                    type="textarea"
                    :rows="1"
                    resize="none"
                    autofocus
                  />
                  <div class="note-actions">
                    <el-button
                      size="small"
                      type="primary"
                      @click="confirmNote(item)"
                    >
                      确认
                    </el-button>
                    <el-button
                      size="small"
                      @click="cancelNote(item)"
                    >
                      取消
                    </el-button>
                  </div>
                </div>
              </div>
              <div class="cart-item-price">¥{{ item.price.toFixed(2) }}</div>
            </div>
            <div class="cart-item-quantity">
              <el-button
                type="text"
                size="small"
                @click="cartItems[index].quantity--; if (cartItems[index].quantity <= 0) cartItems.splice(index, 1)"
              >
                -
              </el-button>
              <span class="quantity">{{ item.quantity }}</span>
              <el-button
                type="text"
                size="small"
                @click="cartItems[index].quantity++"
              >
                +
              </el-button>
            </div>
            <div class="cart-item-total">
              ¥{{ item.totalPrice.toFixed(2) }}
            </div>
          </div>
          <div class="cart-total">
            <div class="total-text">总计:</div>
            <div class="total-price">
              ¥{{ cartItems.reduce((total, item) => total + item.totalPrice, 0).toFixed(2) }}
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="closeCart">取消</el-button>
          <el-button type="primary" v-if="cartItems.length > 0" @click="submitOrder">
            提交订单
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import axios from 'axios';

// 引入API配置
import { API_CONFIG } from '../../config/index.js';

const router = useRouter();
const route = useRoute();

// 商家信息
const merchant = ref({
  id: 0,
  name: '',
  type: '',
  rating: 4.5, // Default to 4.5 for mock data
  distance: '',
  status: '',
  tags: [],
  image: ''
});

// 收藏状态
const isFavorite = ref(false);

// 当前视图模式: details(查看详情) / order(立即下单)
const viewMode = ref(route.query.viewMode || 'order'); // 默认值改为order以显示立即下单按钮

// 提交订单并导航到订单确认页
const submitOrder = () => {
  if (cartItems.value.length === 0) {
    ElMessage.warning('请先添加商品到购物车');
    return;
  }

  // 将订单信息存储到会话存储
  const orderInfo = {
    merchant: merchant.value,
    cartItems: cartItems.value,
    totalAmount: cartItems.value.reduce((total, item) => total + item.totalPrice, 0)
  };
  sessionStorage.setItem('pendingOrder', JSON.stringify(orderInfo));

  // 关闭购物车
  closeCart();

  // 导航到订单确认页面
  router.push({ path: '/user/home/order-confirmation' });
};
// 菜单类型标签
const activeMenuTab = ref('comments'); // 默认显示用户评价

// 菜单类型数据
const menuTabs = ref([
  { value: 'comments', label: '用户评价' }
]);

// 标记商家是否有菜单
const hasMenus = ref(false);

// 计算当前选中的菜单名称
const currentMenuName = computed(() => {
  const activeTab = menuTabs.value.find(tab => tab.value === activeMenuTab.value);
  return activeTab ? activeTab.label : '';
});

// 评价数据
const comments = ref([
  {
    id: 1,
    userName: '张三',
    rating: 5,
    comment: '这家店的健康餐特别好吃，食材新鲜，味道不错！',
    date: '2024-05-20',
    replies: [
      {
        id: 11,
        type: 'customer',
        userName: '张三',
        comment: '追加评论：今天又点了一次，还是一样的好吃！',
        date: '2024-05-21'
      },
      {
        id: 12,
        type: 'merchant',
        userName: 'XX餐厅客服',
        comment: '感谢您的喜爱和追加评价，我们会继续保持品质！',
        date: '2024-05-21'
      }
    ],
    expandReplies: false
  },
  {
    id: 2,
    userName: '李四',
    rating: 4,
    comment: '味道很好，配送也很快，下次还会再来！',
    date: '2024-05-19',
    replies: [
      {
        id: 21,
        type: 'merchant',
        userName: 'XX餐厅客服',
        comment: '感谢您的支持，祝您用餐愉快！',
        date: '2024-05-19'
      }
    ],
    expandReplies: false
  },
  {
    id: 3,
    userName: '王五',
    rating: 3,
    comment: '价格有点贵，但是味道还可以。',
    date: '2024-05-18',
    replies: [],
    expandReplies: false
  }
]);

// 菜单数据
const menuItems = ref([
  {
    id: 1,
    name: '经典健康套餐',
    category: 'signature',
    price: 28.8,
    description: '包含新鲜蔬菜沙拉、烤鸡胸肉和糙米饭',
    requiredIngredients: ['新鲜蔬菜沙拉', '烤鸡胸肉', '糙米饭'],
    optionalIngredients: [
      { id: 101, name: '额外鸡胸肉', price: 8.0, selected: false },
      { id: 102, name: '煎蛋', price: 2.5, selected: false },
      { id: 103, name: '额外蔬菜', price: 3.0, selected: false }
    ]
  },
  {
    id: 2,
    name: '高蛋白健身餐',
    category: 'signature',
    price: 35.0,
    description: '适合增肌人群的高蛋白套餐',
    requiredIngredients: ['烤牛肉', '煮鸡蛋', '西兰花', '糙米饭'],
    optionalIngredients: [
      { id: 201, name: '额外牛肉', price: 12.0 },
      { id: 202, name: '蛋白粉', price: 5.0 }
    ]
  },
  {
    id: 3,
    name: '素食套餐',
    category: 'signature',
    price: 22.5,
    description: '全素食，健康无负担',
    requiredIngredients: ['素食沙拉', '烤蔬菜', '藜麦饭'],
    optionalIngredients: [
      { id: 301, name: '额外素食沙拉', price: 4.0 },
      { id: 302, name: '坚果', price: 3.5 }
    ]
  },
  // 新增饮品数据
  {
    id: 4,
    category: 'drink',
    name: '可乐',
    price: 5.0,
    description: '碳酸饮料',
    requiredIngredients: ['可乐'],
    optionalIngredients: [
      { id: 401, name: '加冰', price: 0.0 },
      { id: 402, name: '加柠檬', price: 0.5 }
    ]
  },
  {
    id: 5,
    category: 'drink',
    name: '雪碧',
    price: 6.0,
    description: '碳酸饮料',
    requiredIngredients: ['雪碧'],
    optionalIngredients: [
      { id: 501, name: '加冰', price: 0.0 },
      { id: 502, name: '加薄荷', price: 0.5 }
    ]
  },
  {
    id: 6,
    category: 'drink',
    name: '酸梅汤',
    price: 7.0,
    description: '传统饮品',
    requiredIngredients: ['酸梅汤'],
    optionalIngredients: [
      { id: 601, name: '加冰', price: 0.0 },
      { id: 602, name: '加桂花', price: 0.5 }
    ]
  },
  // 新增主食数据
  {
    id: 7,
    category: 'staple',
    name: '米饭',
    price: 2.0,
    description: '主食',
    requiredIngredients: ['米饭'],
    optionalIngredients: [
      { id: 701, name: '加量', price: 1.0 },
      { id: 702, name: '小米饭', price: 0.5 }
    ]
  },
  {
    id: 8,
    category: 'staple',
    name: '面条',
    price: 3.0,
    description: '主食',
    requiredIngredients: ['面条'],
    optionalIngredients: [
      { id: 801, name: '加量', price: 1.5 },
      { id: 802, name: '鸡蛋面', price: 1.0 }
    ]
  },
  {
    id: 9,
    category: 'staple',
    name: '烧饼',
    price: 1.5,
    description: '主食',
    requiredIngredients: ['烧饼'],
    optionalIngredients: [
      { id: 901, name: '夹肉', price: 2.0 },
      { id: 902, name: '夹鸡蛋', price: 1.0 }
    ]
  }
]);

// 组件挂载时加载商家信息和恢复购物车
onMounted(() => {
  const savedMerchant = sessionStorage.getItem('selectedMerchant');
  if (savedMerchant) {
    // 从会话存储获取商家基本信息
    const baseMerchantInfo = JSON.parse(savedMerchant);
    merchant.value = { ...baseMerchantInfo };

    // 从后端获取完整的商家详情和菜品信息
    loadMerchantDetails(baseMerchantInfo.id);

    // 加载当前商家的独立购物车
    if (!cartItemsByMerchant.value[merchant.value.id]) {
      cartItemsByMerchant.value[merchant.value.id] = [];
    }
    cartItems.value = cartItemsByMerchant.value[merchant.value.id];
  } else {
    // 如果没有商家信息，返回商家列表
    router.push('/user/home/merchants');
    return;
  }

  // 恢复购物车数据（当从订单确认页返回且未完成支付时）
  const pendingOrder = sessionStorage.getItem('pendingOrder');
  if (pendingOrder) {
    const parsedOrder = JSON.parse(pendingOrder);
    if (parsedOrder.cartItems && parsedOrder.cartItems.length > 0 && parsedOrder.merchant.id === merchant.value.id) {
      // 清空当前购物车
      cartItemsByMerchant.value[merchant.value.id] = [];
      // 恢复购物车项目
      parsedOrder.cartItems.forEach(item => {
        // 确保购物车项目有必要的属性
        const cartItem = {
          ...item,
          note: item.note || '',
          tempNote: item.tempNote || '',
          isEditingNote: item.isEditingNote || false
        };
        cartItemsByMerchant.value[merchant.value.id].push(cartItem);
      });
      // 更新当前购物车引用
      cartItems.value = cartItemsByMerchant.value[merchant.value.id];
      // 更新购物车统计信息
      updateCartStats();
    }
  }
});

// 从后端加载完整的商家详情和菜品信息
const loadMerchantDetails = (merchantId) => {
  axios.get(API_CONFIG.baseURL + API_CONFIG.merchant.detail, {
    params: { merchantId }
  })
    .then(response => {
      // 假设后端返回的数据结构如下：
      // {
      //   data: {
      //     merchant: { ...完整的商家信息... },
      //     menuItems: [ ...菜品列表... ]
      //   }
      // }

      if (response.data && response.data.merchant) {
        // 更新商家信息
        merchant.value = {
          ...merchant.value,
          ...response.data.merchant
        };

        // 更新菜单信息
        if (response.data.menus && response.data.menus.length > 0) {
          // 为菜单项目添加必要的属性
          const allMenuItems = [];

          // 遍历所有菜单
          response.data.menus.forEach(menu => {
            menu.dishes.forEach(dish => {
              allMenuItems.push({
                ...dish,
                menuId: menu.menuId, // 保存菜单ID
                menuName: menu.menuName, // 保存菜单名称
                quantity: 1, // 默认数量为1
                optionalIngredients: dish.optionalIngredients || [], // 确保可选食材数组存在
                selectedOptionalIngredients: [], // 初始化选中的可选食材
                note: '', // 添加备注字段
                tempNote: '', // 添加临时备注字段
                isEditingNote: false // 添加编辑状态字段
              });
            });
          });

          menuItems.value = allMenuItems;

          // 确保可选食材有selected属性
          menuItems.value.forEach(item => {
            item.optionalIngredients.forEach(ingredient => {
              ingredient.selected = ingredient.selected || false;
            });
          });

          // 根据后端返回的菜单生成标签
          menuTabs.value = response.data.menus.map(menu => ({
            value: menu.menuId,
            label: menu.menuName
          }));

          // 添加用户评价标签
          menuTabs.value.push({ value: 'comments', label: '用户评价' });

          // 默认激活第一个菜单
          activeMenuTab.value = response.data.menus[0].menuId;

          hasMenus.value = true;
        } else {
          // 商家没有菜单
          menuItems.value = [];
          menuTabs.value = [{ value: 'comments', label: '用户评价' }];
          activeMenuTab.value = 'comments';
          hasMenus.value = false;
        }
      }
    })
    .catch(error => {
      console.error('加载商家详情和菜单失败:', error);
      // 失败时使用模拟数据作为备份
      ElMessage.warning('加载商家详情失败，将使用模拟数据');
      // 设置hasMenus为true，因为模拟数据有菜单
      hasMenus.value = true;
    });
};

// 切换收藏状态
const toggleFavorite = () => {
  isFavorite.value = !isFavorite.value;
  ElMessage.success(isFavorite.value ? `${merchant.value.name} 已加入收藏` : `${merchant.value.name} 已取消收藏`);
  // 这里可以添加真实的收藏逻辑，比如保存到数据库或本地存储
  console.log('收藏状态:', isFavorite.value);
};

// 购物车数据 - 每个商家有独立的购物车
const cartItemsByMerchant = ref({});

// 当前商家的购物车数据
const cartItems = ref([]);

// 购物车显示状态
const cartVisible = ref(false);

// 计算购物车总数量（当前商家购物车所有商品数量之和）
const cartTotalQuantity = ref(0);

// 计算购物车总金额（当前商家购物车总金额）
const cartTotalAmount = ref(0);

// 可拖动购物车相关
const cartBallRef = ref(null);
let isDragging = false;
let hasDragged = false; // 标记是否有实际拖动
let justDragged = false; // 标记刚刚结束拖动
let startX = 0;
let startY = 0;
let initialX = 0;
let initialY = 0;

// 开始拖动
const startDrag = (e) => {
  if (!cartBallRef.value) return;

  // 阻止文本选择和默认事件
  e.preventDefault();
  e.stopPropagation();

  isDragging = true;
  startX = e.clientX;
  startY = e.clientY;

  // 获取购物车球的初始位置
  const rect = cartBallRef.value.getBoundingClientRect();
  initialX = rect.left;
  initialY = rect.top;

  // 添加事件监听
  document.addEventListener('mousemove', onDrag);
  document.addEventListener('mouseup', stopDrag);
}

// 拖动中
const onDrag = (e) => {
  if (!isDragging || !cartBallRef.value) return;

  hasDragged = true; // 设置为已拖动

  const dx = e.clientX - startX;
  const dy = e.clientY - startY;

  // 计算新位置
  let newX = initialX + dx;
  let newY = initialY + dy;

  // 限制在视窗内
  const windowWidth = window.innerWidth;
  const windowHeight = window.innerHeight;
  const cartWidth = cartBallRef.value.offsetWidth;
  const cartHeight = cartBallRef.value.offsetHeight;

  newX = Math.max(0, Math.min(newX, windowWidth - cartWidth));
  newY = Math.max(0, Math.min(newY, windowHeight - cartHeight));

  // 更新位置
  cartBallRef.value.style.left = `${newX}px`;
  cartBallRef.value.style.top = `${newY}px`;
}

// 停止拖动
const stopDrag = () => {
  // 重置拖动状态
  const wasDragging = hasDragged;
  isDragging = false;
  hasDragged = false;

  document.removeEventListener('mousemove', onDrag);
  document.removeEventListener('mouseup', stopDrag);

  // 如果有拖动，标记刚刚结束拖动
  if (wasDragging) {
    justDragged = true;
    // 设置一个短暂的延迟来重置标记，确保click事件能检测到
    setTimeout(() => {
      justDragged = false;
    }, 100);
  }
}

// 更新购物车统计信息 - 使用当前商家的购物车
const updateCartStats = () => {
  if (!merchant.value || !merchant.value.id) return;

  // 确保当前购物车引用正确
  cartItems.value = cartItemsByMerchant.value[merchant.value.id];

  cartTotalQuantity.value = cartItems.value.reduce((total, item) => total + item.quantity, 0);
  cartTotalAmount.value = cartItems.value.reduce((total, item) => total + item.totalPrice, 0);
};

// 更新购物车 - 使用当前商家的购物车
const updateCart = (item) => {
  if (!merchant.value || !merchant.value.id) return;

  // 获取当前商家的购物车
  const currentMerchantCart = cartItemsByMerchant.value[merchant.value.id];

  // 检查是否有相同的商品和相同的可选食材组合
  const existingItem = currentMerchantCart.find(cartItem =>
    cartItem.id === item.id &&
    JSON.stringify(cartItem.selectedOptionalIngredients) === JSON.stringify(item.selectedOptionalIngredients)
  );

  if (existingItem) {
    // 如果存在相同的组合，增加数量
    existingItem.quantity += item.quantity;
    existingItem.totalPrice += item.totalPrice;
  } else {
    // 如果不存在，添加新的购物车项目
    currentMerchantCart.push({ ...item });
  }

  // 更新购物车统计信息
  updateCartStats();
};

// 初始化数量和可选食材选中状态
menuItems.value.forEach(item => {
  item.quantity = 1;
  item.optionalIngredients.forEach(ingredient => {
    ingredient.selected = false;
  });
});

// 计算实时价格函数
const calculateRealTimePrice = (item) => {
  if (!item) return 0;
  const optionalTotal = item.optionalIngredients.reduce((sum, ingredient) => {
    return sum + (ingredient.selected ? ingredient.price : 0);
  }, 0);
  return item.price + optionalTotal;
};

// 添加菜单项到购物车
const addMenuItem = (item) => {
  // 计算选中的可选食材
  const selectedOptionalIngredients = item.optionalIngredients.filter(ingredient => ingredient.selected);
  const totalPrice = item.price + selectedOptionalIngredients.reduce((sum, ingredient) => sum + ingredient.price, 0);

  // 创建购物车项目
  const cartItem = {
    ...item,
    quantity: item.quantity,
    selectedOptionalIngredients: [...selectedOptionalIngredients],
    totalPrice: totalPrice * item.quantity,
    note: '', // Add note property
    tempNote: '', // Add temporary note property for input
    isEditingNote: false, // Add editing state
  };

  updateCart(cartItem);
  ElMessage.success(`${item.name} 已加入购物车`);

  // 清空配置：重置数量为1，取消选中所有可选食材
  item.quantity = 1;
  item.optionalIngredients.forEach(ingredient => {
    ingredient.selected = false;
  });

  // 这里可以添加真实的购物车逻辑，比如保存到数据库或本地存储
  console.log('加入购物车:', cartItem);
};

// 查看购物车
const viewCart = () => {
  // 如果正在拖动、已经拖动或刚刚结束拖动，不打开购物车
  if (isDragging || hasDragged || justDragged) {
    return;
  }
  cartVisible.value = true;
};

// 关闭购物车
const closeCart = () => {
  cartVisible.value = false;
};

// 确认添加备注
const confirmNote = (item) => {
  item.note = item.tempNote;
  item.isEditingNote = false; // Exit edit mode
  ElMessage.success('备注已保存');
};

// 取消添加备注
const cancelNote = (item) => {
  item.tempNote = item.note; // Reset temp note to current note
  item.isEditingNote = false; // Exit edit mode
  ElMessage.info('已取消备注修改');
};

// 跳转到订单确认页
const goToOrderConfirmation = () => {
  // 将订单信息存储到会话存储
  const orderInfo = {
    merchant: merchant.value,
    cartItems: cartItems.value,
    totalAmount: cartItems.value.reduce((total, item) => total + item.totalPrice, 0),
    // 单聊/店铺直接下单时，设置默认值
    fromChat: false,
    groupName: '默认订单群',
    // 这里可以替换为实际的用户名，假设从用户信息中获取
    userName: '当前用户' // 示例值，实际应从登录信息中获取
  };
  sessionStorage.setItem('pendingOrder', JSON.stringify(orderInfo));

  router.push('/user/home/order-confirmation');
};

// 监听滚动事件的代码已合并到上面的onMounted钩子中
</script>

<style scoped lang="less">
.merchant-detail-container {
  padding: 0;
  min-height: 100vh;
  background-color: #f5f5f5;

  .merchant-detail-card {
    border-radius: 0;
    border: none;
    box-shadow: none;
    padding: 0;

    // 返回按钮
    .back-button-container {
      padding: 12px 24px;
      background-color: #ffffff;

      .back-button {
        font-size: 18px;
        color: #409eff;
        padding: 8px 16px;
        border-radius: 6px;
        transition: all 0.3s ease;

        &:hover {
          color: #66b1ff;
          background-color: rgba(64, 158, 255, 0.1);
        }
      }
    }

    // 商家头部信息
    .merchant-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 16px 24px;
      background-color: #ffffff;

      .header-left {
        display: flex;
        justify-content: space-between;
        align-items: center;
        gap: 16px;
        width: 100%; // Ensure it takes full width

        .back-button {
          font-size: 14px;
          color: #666666;

          &:hover {
            color: #ff6b6b;
          }
        }

        .merchant-name-main {
          font-size: 20px;
          font-weight: bold;
          color: #333333;
        }
      }

      .header-right {
        .favorite-button {
          font-size: 14px;
          color: #666666;

          &:hover {
            color: #ff6b6b;
          }
        }

        .cart-button {
          position: relative;

          .cart-badge {
            position: absolute;
            top: -5px;
            right: -15px;
          }
        }
      }
    }

    // 商家Banner区
    .merchant-banner {
      width: 100%;
      height: 200px;
      background-color: #f0f0f0;
      display: flex;
      align-items: center;
      justify-content: center;

      .banner-content {
        font-size: 48px;
        color: #cccccc;
      }
    }

    // 商家基本信息
    .merchant-basic-info {
      padding: 24px;
      background-color: #ffffff;

      .basic-info-section {
        display: flex;
        flex-wrap: wrap;
        gap: 32px;
        align-items: center;

        .merchant-rating-main {
          .el-rate {
            font-size: 24px;
          }
        }

        .merchant-location,
        .merchant-hours,
        .merchant-average {
          display: flex;
          align-items: center;
          gap: 8px;
          font-size: 14px;
          color: #666666;

          .location-icon,
          .clock-icon,
          .money-icon {
            font-size: 16px;
          }
        }
      }
    }

    // 菜单类型切换
    .menu-tabs {
      padding: 0 24px;
      background-color: #ffffff;
      border-bottom: 1px solid #e8e8e8;

      .merchant-menu-tabs {
        .el-tabs__nav {
          border-bottom: none;
        }

        .el-tabs__item {
          font-size: 14px;
          color: #666666;
          padding: 12px 0;
          transition: all 0.3s ease;

          &.is-active {
            color: #ff6b6b;
            border-bottom: 2px solid #ff6b6b;
            font-weight: 500;
          }
        }
      }
    }

    // 菜单展示区
    .menu-display-area {
      padding: 24px;
      background-color: #ffffff;

      // 当前菜单名称
      .current-menu-name {
        margin-bottom: 24px;

        .menu-name-title {
          font-size: 24px;
          font-weight: bold;
          color: #333;
          padding-bottom: 12px;
          border-bottom: 2px solid #e8e8e8;
        }
      }

      // 没有菜单的提示
      .no-menus-notice {
        margin: 24px 0;
        padding: 20px;
        background-color: #f5f5f5;
        border-radius: 8px;
        text-align: center;

        .notice-text {
          color: #999;
          font-size: 16px;
        }
      }

      // 菜品分类
      .dish-category-section {
        margin-bottom: 32px;

        .category-title {
          font-size: 18px;
          font-weight: bold;
          color: #333333;
          margin-bottom: 16px;
        }

        // 菜品网格布局
        .dish-grid {
          display: grid;
          grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
          gap: 20px;
        }

        // 菜品卡片
        .dish-card {
          border: 1px solid #e8e8e8;
          border-radius: 12px;
          padding: 20px;
          display: flex;
          flex-direction: column;
          align-items: center;
          gap: 12px;
          background-color: #ffffff;
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
          transition: all 0.3s ease;

          &:hover {
            box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
            transform: translateY(-2px);
          }

          .dish-image {
            font-size: 56px;
            margin-bottom: 8px;
            text-align: center;
          }

          .dish-name {
            font-size: 17px;
            font-weight: 600;
            color: #303133;
            text-align: center;
            line-height: 1.4;
          }

          .dish-price {
            font-size: 20px;
            color: #e6a23c;
            font-weight: 600;
          }

          .dish-desc {
            font-size: 14px;
            color: #909399;
            text-align: center;
            margin-bottom: 12px;
            line-height: 1.5;
          }

          // 食材组成
          .dish-ingredients {
            width: 100%;
            margin: 10px 0;
            padding: 12px;
            background-color: #fafafa;
            border-radius: 8px;
            font-size: 13px;

            .ingredient-section {
              margin-bottom: 12px;

              &:last-child {
                margin-bottom: 0;
              }

              .ingredient-title {
                display: block;
                font-weight: 600;
                color: #303133;
                margin-bottom: 6px;
              }

              .ingredient-list {
                display: flex;
                flex-direction: column;
                gap: 6px;

                .ingredient-item {
                  background-color: #ecf5ff;
                  color: #66b1ff;
                  padding: 4px 8px;
                  border-radius: 4px;
                  font-size: 12px;
                  display: inline-block;
                }

                .ingredient-checkbox {
                  .ingredient-price {
                    color: #909399;
                    font-size: 11px;
                  }
                }
              }
            }
          }

          // 数量选择
          .dish-quantity {
            width: 100%;
            margin: 8px 0;
          }

          .el-button {
            width: 100%;
            background-color: #ff6b6b;
            border: none;
            border-radius: 8px;
            height: 38px;
            font-size: 14px;
            font-weight: 500;

            &:hover {
              background-color: #ff5252;
            }
          }
        }
      }

      // 用户评价
      .comments-section {
        margin-bottom: 32px;

        .category-title {
          font-size: 18px;
          font-weight: bold;
          color: #333333;
          margin-bottom: 16px;
        }

        .comments-list {
          display: flex;
          flex-direction: column;
          gap: 16px;
        }

        .comment-card {
          padding: 16px;
          border: 1px solid #e8e8e8;
          border-radius: 8px;

          .comment-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 8px;

            .comment-user-info {
              .user-name {
                font-size: 14px;
                font-weight: bold;
                color: #333333;
                margin-right: 16px;
              }

              .comment-date {
                font-size: 12px;
                color: #999999;
              }
            }
          }

          .comment-content {
            font-size: 14px;
            color: #666666;
            line-height: 1.6;
            margin-bottom: 12px;
          }

          .reply-toggle {
            margin-top: 8px;
            padding: 8px 0;

            button {
              padding: 0;
              font-size: 13px;
              color: #66b1ff;
            }
          }

          .replies-list {
            margin-top: 12px;
            padding-left: 24px;
            border-left: 2px solid #ecf5ff;
            display: flex;
            flex-direction: column;
            gap: 12px;

            .reply-card {
              padding: 14px;
              border-radius: 8px;
              background-color: #fafafa;

              .reply-header {
                display: flex;
                justify-content: space-between;
                align-items: center;
                margin-bottom: 8px;

                .reply-username {
                  font-size: 14px;
                  font-weight: 500;
                  color: #303133;

                  .merchant-badge {
                    display: inline-block;
                    margin-left: 6px;
                    padding: 2px 6px;
                    background-color: #67c23a;
                    color: #ffffff;
                    font-size: 10px;
                    border-radius: 4px;
                  }
                }

                .reply-date {
                  font-size: 11px;
                  color: #c0c4cc;
                }
              }

              .reply-content {
                font-size: 13px;
                color: #606266;
                line-height: 1.6;
              }

              &.merchant-reply {
                background-color: #ecfdf3;
                border-left: 3px solid #67c23a;

                .reply-username {
                  color: #67c23a;
                }
              }
            }
          }
        }
      }
    }

    // 立即下单快捷操作区
    .quick-order-section {
      padding: 24px;
      background-color: #ffffff;

      .quick-order-button {
        width: 100%;
        height: 48px;
        font-size: 16px;
        background-color: #ff6b6b;
        border: none;

        &:hover {
          background-color: #ff5252;
        }
      }
    }
  }
}

// 购物车弹窗样式
.cart-content {
  padding: 20px;

  .empty-cart {
    text-align: center;
    padding: 40px 0;

    .empty-cart-icon {
      font-size: 48px;
      color: #e8e8e8;
      margin-bottom: 16px;
    }

    .empty-cart-text {
      font-size: 16px;
      color: #999999;
    }
  }

  .cart-items {
    max-height: 400px;  // 设置购物车最大高度
    overflow-y: auto;  // 超出部分显示滚动条
    padding-right: 8px;  // 为滚动条预留空间
    .cart-item {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      padding: 18px;
      margin-bottom: 16px;
      background-color: #fafafa;
      border-radius: 12px;
      transition: all 0.3s ease;

      &:hover {
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
      }

      .cart-item-info {
        flex: 1;
        min-width: 0;
        margin-right: 16px;

        .cart-item-name {
          font-size: 15px;
          font-weight: 600;
          color: #333333;
          margin-bottom: 8px;
        }

        .cart-item-price {
          font-size: 13px;
          color: #ff6b6b;
          font-weight: 500;
          margin-bottom: 4px;
        }
      }

      .cart-item-quantity {
        display: flex;
        align-items: center;
        gap: 8px;
        margin: 0 24px;

        .quantity {
          width: 40px;
          text-align: center;
          font-size: 14px;
          font-weight: 500;
        }
      }

      .cart-item-total {
        font-size: 16px;
        font-weight: bold;
        color: #ff6b6b;
        white-space: nowrap;
      }
    }

    .cart-total {
      display: flex;
      justify-content: flex-end;
      align-items: center;
      gap: 20px;
      padding: 20px 0;
      border-top: 2px solid #ff6b6b;
      margin-top: 20px;

      .total-text {
        font-size: 18px;
        font-weight: bold;
        color: #333333;
      }

      .total-price {
        font-size: 24px;
        font-weight: bold;
        color: #ff6b6b;
      }
    }

    // Optional ingredients styles
    .cart-item-ingredients {
      margin: 4px 0;

      .ingredient-tag {
        font-size: 12px;
        color: #909399;
        background-color: #ecf5ff;
        padding: 2px 6px;
        border-radius: 4px;
        margin-right: 4px;
      }
    }

    // Note input styles
    .cart-item-note {
      margin: 4px 0;
      position: relative;
      max-width: 100%; /* 确保不超过容器宽度 */

      .note-display {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 4px 8px;
        background-color: #f5f7fa;
        border-radius: 4px;
        min-height: 24px;
        justify-content: space-between;

        .note-text {
          font-size: 12px;
          color: #333;
          max-height: 54px; /* 约三行高度 */
          overflow: hidden;
          text-overflow: ellipsis;
          display: -webkit-box;
          -webkit-line-clamp: 3;
          -webkit-box-orient: vertical;
          line-clamp: 3; /* 标准属性 */
          box-orient: vertical; /* 标准属性 */
          flex: 1;
          word-wrap: break-word; /* 自动换行 */
          word-break: break-all; /* 确保长单词也能换行 */
        }

        .note-empty {
          font-size: 12px;
          color: #999;
          flex: 1;
        }

        .edit-note-btn {
          padding: 2px 4px;
          height: auto;
          background-color: #ccc; /* 灰色背景 */
          border-color: #ccc; /* 灰色边框 */

          .edit-icon {
            font-size: 12px;
            color: #fff; /* 白色图标 */
          }

          &:hover {
            background-color: #999; /*  hover时加深灰色 */
            border-color: #999;
          }
        }
      }

      .note-edit {
        .el-input__inner {
          font-size: 12px;
          height: 32px;
          border-radius: 6px;
        }

        .note-actions {
          display: flex;
          gap: 8px;
          margin-top: 4px;
        }
      }
    }
  }
}

// 可拖动悬浮购物车
.draggable-cart-ball {
  position: fixed;
  right: 24px;
  bottom: 80px; // 向上调整避免被可能的底部导航遮挡
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
  z-index: 9999; // 设置更高的z-index确保浮在所有内容之上
  border: 3px solid rgba(255, 255, 255, 0.9); // 添加白色边框增强圆形感
  backdrop-filter: blur(10px); // 添加轻微模糊效果增强层次感

  &:active {
    cursor: grabbing;
    transform: scale(1.05);
    box-shadow: 0 8px 32px rgba(102, 126, 234, 0.6);
  }

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 32px rgba(102, 126, 234, 0.6);
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
</style>
