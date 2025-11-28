<template>
  <div class="chat-container">
    <div class="chat-header">
      <h3 class="page-title">【聊天消息】</h3>
      <div class="chat-actions">
        <el-button type="primary" size="small" @click="createNewChat">
          + 新建聊天
        </el-button>
        <el-button type="primary" size="small" @click="createNewGroup">
          + 新建群聊
        </el-button>
      </div>
    </div>

    <div class="chat-content">
      <!-- 左侧会话列表 -->
      <div class="conversation-list">
        <div
          v-for="conversation in sortedConversations"
          :key="conversation.id"
          class="conversation-item"
          :class="{ active: selectedConversation?.id === conversation.id, 'pinned-conversation': conversation.pinned }"
          @click="selectConversation(conversation)"
          @contextmenu.prevent="showContextMenu(conversation, $event)"
        >
          <div class="conversation-avatar">
            <div v-if="conversation.avatar && conversation.avatar.match(/^https?:/)">
              <img :src="conversation.avatar" alt="" />
            </div>
            <div v-else class="emoji-avatar">
              {{ conversation.avatar || (conversation.type === 'group' ? '👥' : '💬') }}
            </div>
            <div v-if="conversation.unreadCount > 0" class="unread-count">
              {{ conversation.unreadCount }}
            </div>
          </div>
          <!-- 置顶按钮 - 仅支持私聊 -->
          <div
            v-if="conversation.type !== 'group'"
            class="pin-btn"
            @click.stop="togglePin(conversation)"
            title="{{ conversation.pinned ? '取消置顶' : '置顶会话' }}"
          >
            {{ conversation.pinned ? '📌' : '📌' }}
          </div>
          <div class="conversation-info">
            <div class="name-time">
              <span class="name">
                {{ conversation.name }}
                <span v-if="conversation.type === 'group'" class="member-count"> ({{ conversation.memberCount || '0' }}人)</span>
              </span>
              <span class="time">{{ conversation.time }}</span>
            </div>
            <div class="last-message">{{ conversation.lastMessage || '暂无消息' }}</div>
          </div>
        </div>
      </div>

      <!-- 会话右键菜单 -->
      <div
        v-if="contextMenuVisible && selectedContextConversation"
        class="context-menu"
        :style="{ left: contextMenuPosition.x + 'px', top: contextMenuPosition.y + 'px' }"
        @click.stop
      >
        <div class="menu-item" @click="togglePin(selectedContextConversation)">
          {{ selectedContextConversation.pinned ? '取消置顶' : '置顶会话' }}
        </div>
        <div class="menu-item" @click="deleteConversation(selectedContextConversation)" style="color: #ff4d4f;">
          删除会话
        </div>
      </div>

      <!-- 右侧聊天内容 -->
      <div class="chat-area" v-if="selectedConversation">
        <!-- 聊天头部 -->
        <div class="chat-area-header">
          <div class="conversation-info">
            <div class="name-info">
              <span class="name">{{ selectedConversation.name }}</span>
              <span v-if="selectedConversation.type === 'group'" class="member-count"> ({{ selectedConversation.memberCount || '0' }}人)</span>
            </div>
          </div>
          <!-- 群聊操作 - 创建/加入群订单 -->
          <div class="chat-actions" v-if="selectedConversation.type === 'group'">
            <el-button type="primary" size="small" @click="createGroupOrder">创建群订单</el-button>
            <el-button size="small" @click="joinGroupOrder">加入群订单</el-button>
          </div>
        </div>

        <!-- 悬浮订单按钮 -->
        <div v-if="selectedConversation.type === 'group' && groupOrders[selectedConversation.id]"
             ref="floatBtnRef"
             class="floating-order-btn"
             @click="handleCartClick"
             @mousedown="startDrag"
             @selectstart="handleSelectStart">
          <div class="order-btn-inner">
            <el-icon :size="24" color="white"><ShoppingCart /></el-icon>
            <span class="cart-count" v-if="groupOrders[selectedConversation.id].orderItems.length > 0">
              {{ groupOrders[selectedConversation.id].orderItems.length }}
            </span>
          </div>
        </div>

        <!-- 群订单购物车悬浮窗 -->
        <el-drawer
          v-model="orderDrawerVisible"
          title="当前群订单"
          direction="rtl"
          size="45%"
          :close-on-click-modal="true"
        >
          <div class="order-overview" style="margin-bottom: 20px;">
            <div class="overview-item">
              <span class="info-label">群名称：</span>
              <span class="info-value">{{ groupOrders[selectedConversation.id].groupName }}</span>
            </div>
            <div class="overview-item">
              <span class="info-label">订单创建人：</span>
              <span class="info-value">{{ groupOrders[selectedConversation.id].creator }}</span>
            </div>
            <div class="overview-item" v-if="groupOrders[selectedConversation.id].merchantName">
              <span class="info-label">已选商家：</span>
              <span class="info-value">
                {{ groupOrders[selectedConversation.id].merchantName }}
                <el-button
                  type="text"
                  size="small"
                  style="margin-left: 10px; color: #409EFF;"
                  @click="changeMerchant"
                  v-if="
                    groupOrders[selectedConversation.id].creator === '我' &&
                    groupOrders[selectedConversation.id].orderItems.length === 0 &&
                    groupOrders[selectedConversation.id].status === 'active'
                  "
                >
                  更换商家
                </el-button>
              </span>
            </div>
            <div class="overview-item">
              <span class="info-label">总金额：</span>
              <span class="info-value">¥{{ groupOrders[selectedConversation.id].totalAmount.toFixed(2) }}</span>
            </div>
            <div class="overview-item">
              <span class="info-label">参与人数：</span>
              <span class="info-value">{{ groupOrders[selectedConversation.id].members.length }}人</span>
            </div>
          </div>

          <!-- 快速点餐入口 -->
          <div class="quick-order-entry" v-if="orderingMerchant && groupOrders[selectedConversation.id].status === 'active'">
            <el-button type="primary" size="small" @click="openMerchantSelectDialog">
              + 继续点餐
            </el-button>
          </div>

          <div class="order-items" style="margin-top: 20px;">
            <h4 class="section-title" v-if="groupOrders[selectedConversation.id].orderItems && groupOrders[selectedConversation.id].orderItems.length > 0">订单商品</h4>
            <div class="item-list">
              <el-card v-for="item in groupOrders[selectedConversation.id].orderItems" :key="item.id" class="order-item-card" size="small">
                <div class="order-item-header">
                  <span class="item-name">{{ item.name }}</span>
                  <span class="item-quantity">×{{ item.quantity }}</span>
                  <span class="item-price">¥{{ item.price.toFixed(2) }}</span>
                </div>

                <!-- 必选食材 -->
                <div class="item-ingredients" v-if="item.requiredIngredients && item.requiredIngredients.length > 0">
                  <div class="ingredient-label">必选食材:</div>
                  <div class="ingredient-list">
                    <el-tag v-for="ingredient in item.requiredIngredients" :key="ingredient" size="small" type="info" style="margin: 0 4px 4px 0;">
                      {{ ingredient }}
                    </el-tag>
                  </div>
                </div>

                <!-- 可选食材 -->
                <div class="item-ingredients" v-if="item.selectedOptionalIngredients && item.selectedOptionalIngredients.length > 0">
                  <div class="ingredient-label">已选可选食材:</div>
                  <div class="ingredient-list">
                    <el-tag v-for="ingredient in item.selectedOptionalIngredients" :key="ingredient.id || ingredient" size="small" type="success" style="margin: 0 4px 4px 0;">
                      {{ ingredient.name }}
                    </el-tag>
                  </div>
                </div>

                <!-- 商品备注 -->
                <div class="item-remark" v-if="item.remark">
                  <div class="remark-label">备注:</div>
                  <div class="remark-content">{{ item.remark }}</div>
                </div>
              </el-card>
            </div>
          </div>

          <div style="display: flex; justify-content: flex-end; margin-top: 20px; gap: 10px;">
            <el-button type="primary" @click="openMerchantSelectDialog" v-if="groupOrders[selectedConversation.id]">
              选择商家和商品
            </el-button>
            <el-button type="success" @click="goToOrderConfirmation" v-if="groupOrders[selectedConversation.id]">
              去支付
            </el-button>
          </div>
        </el-drawer>

        <!-- 聊天内容 -->
        <div class="messages-container">
          <div
            v-for="message in chatMessages"
            :key="message.id"
            class="message-item"
            :class="{
              'others-message': message.sender !== '我',
              'my-message': message.sender === '我'
            }"
          >
            <div class="message-header">
              <span class="sender-name">{{ message.sender }}</span>
            </div>
            <div class="message-content">
              {{ message.content }}
              <div class="message-time">{{ message.time }}</div>
            </div>
          </div>
        </div>

        <!-- 消息输入框 -->
        <div class="message-input-container">
          <el-input
            v-model="newMessage"
            type="textarea"
            placeholder="输入消息内容..."
            :rows="2"
            @keyup.enter="sendMessage"
          />
          <el-button type="primary" @click="sendMessage">发送</el-button>
        </div>
      </div>

      <!-- 空选择提示 -->
      <div class="empty-select" v-else>
        <div class="empty-icon">💬</div>
        <p>请选择一个会话开始交流</p>
      </div>
    </div>

    <!-- 新建群聊对话框 -->
    <el-dialog
      v-model="groupDialogVisible"
      title="新建群聊"
      width="400px"
      @close="cancelCreateGroup"
    >
      <el-form :model="groupForm" label-width="80px">
        <el-form-item label="群名称">
          <el-input v-model="groupForm.name" placeholder="请输入群名称" />
        </el-form-item>
        <el-form-item label="成员列表">
          <el-input
            v-model="groupForm.members"
            type="textarea"
            placeholder="请输入成员名称，用逗号分隔"
            :rows="2"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancelCreateGroup">取消</el-button>
          <el-button type="primary" @click="handleCreateGroup">创建</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 商家选择对话框 -->
    <el-dialog
      v-model="merchantSelectDialogVisible"
      title="选择商家"
      width="600px"
    >
      <div class="merchant-list">
        <div
          v-for="merchant in merchants"
          :key="merchant.id"
          class="merchant-item"
          @click="selectMerchant(merchant)"
        >
          <div class="merchant-avatar">{{ merchant.avatar }}</div>
          <div class="merchant-info">
            <h3 class="merchant-name">{{ merchant.name }}</h3>
            <p class="merchant-type">{{ merchant.type }}</p>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="merchantSelectDialogVisible = false">取消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 商品选择对话框 -->
    <el-dialog
      v-model="productSelectDialogVisible"
      :title="`选择 ${selectedMerchant?.name || ''} 的商品`"
      width="600px"
    >
      <div class="product-list" v-if="selectedMerchant">
        <div
          v-for="product in selectedMerchant.products"
          :key="product.id"
          class="product-item"
        >
          <div class="product-info">
            <h4 class="product-name">{{ product.name }}</h4>
            <p class="product-description">{{ product.description }}</p>

            <!-- 必选食材 -->
            <div class="product-ingredients" v-if="product.requiredIngredients && product.requiredIngredients.length > 0">
              <div class="ingredient-label">必选食材:</div>
              <div class="ingredient-list">
                <el-tag v-for="ingredient in product.requiredIngredients" :key="ingredient" size="small" type="info" style="margin: 0 4px 4px 0;">
                  {{ ingredient }}
                </el-tag>
              </div>
            </div>

            <p class="product-price">¥{{ product.price.toFixed(2) }}</p>
          </div>
          <div class="product-actions">
            <el-button
              type="primary"
              size="small"
              @click="toggleProductSelection(product)"
              :class="{ 'is-selected': selectedProducts.some(item => item.id === product.id) }"
            >
              {{ selectedProducts.some(item => item.id === product.id) ? '已选择' : '选择' }}
            </el-button>
            <div class="quantity-control" v-if="selectedProducts.some(item => item.id === product.id)">
              <el-button
                size="small"
                @click="updateProductQuantity(product, -1)"
              >-</el-button>
              <span class="quantity">{{ selectedProducts.find(item => item.id === product.id).quantity }}</span>
              <el-button
                size="small"
                @click="updateProductQuantity(product, 1)"
              >+</el-button>
            </div>
            <!-- 可选食材选择 -->
            <div class="optional-ingredients" v-if="selectedProducts.some(item => item.id === product.id) && product.optionalIngredients && product.optionalIngredients.length > 0">
              <div class="ingredient-label">可选食材:</div>
              <div class="ingredient-list">
                <el-checkbox-group
                  v-model="productSelectedOptionalIngredients[product.id]"
                  @change="updateProductOptionalIngredients(product.id, productSelectedOptionalIngredients[product.id])"
                >
                  <el-checkbox
                    v-for="ingredient in product.optionalIngredients"
                    :key="ingredient.id"
                    :label="ingredient"
                    style="margin: 0 8px 8px 0;"
                  >
                    {{ ingredient.name }}
                  </el-checkbox>
                </el-checkbox-group>
              </div>
            </div>

            <el-input
              v-if="selectedProducts.some(item => item.id === product.id)"
              v-model="productRemarks[product.id]"
              placeholder="添加备注..."
              size="small"
              type="textarea"
              :rows="1"
              @input="updateProductRemark(product.id, productRemarks[product.id])"
              style="width: 100%; margin-top: 8px;"
            />
            <!-- 加入购物车按钮 -->
            <el-button
              v-if="selectedProducts.some(item => item.id === product.id)"
              type="success"
              size="small"
              @click="addProductToCart(product)"
              style="width: 100%; margin-top: 8px;"
            >
              加入购物车
            </el-button>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="productSelectDialogVisible = false">取消</el-button>
          <el-button type="info" @click="productSelectDialogVisible = false">稍后再看</el-button>
          <el-button type="primary" @click="confirmProductSelection">一键加入购物车</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { ShoppingCart } from '@element-plus/icons-vue';

const router = useRouter();

// Context menu state
const contextMenuVisible = ref(false);
const selectedContextConversation = ref(null);
const contextMenuPosition = ref({ x: 0, y: 0 });

// 模拟统一的聊天会话列表
const conversations = ref([
  // 商家单聊会话
  {
    id: 1,
    type: 'merchant',
    name: '佳食餐馆',
    avatar: '🏪',
    lastMessage: '您的订单已准备好，请前往取餐',
    time: '2024-11-21 14:30',
    unreadCount: 1,
    merchantId: 101,
    pinned: false
  },
  {
    id: 2,
    type: 'merchant',
    name: '美味小吃店',
    avatar: '🏪',
    lastMessage: '您点的奶茶已完成',
    time: '2024-11-21 14:15',
    unreadCount: 0,
    merchantId: 102,
    pinned: true
  },
  // 系统消息
  {
    id: 3,
    type: 'system',
    name: '系统通知',
    avatar: '📢',
    lastMessage: '您的账户已成功充值',
    time: '2024-11-21 10:00',
    unreadCount: 0,
    pinned: false
  },
  // 群聊会话
  {
    id: 4,
    type: 'group',
    name: '美食爱好者群',
    avatar: '🍴',
    lastMessage: '李四: 我要麻婆豆腐',
    time: '10:33',
    unreadCount: 0,
    memberCount: 10,
    pinned: false
  },
  {
    id: 5,
    type: 'group',
    name: '同事午餐群',
    avatar: '👨‍💼',
    lastMessage: '小明: 今天中午吃什么？',
    time: '09:15',
    unreadCount: 2,
    memberCount: 5,
    pinned: false
  }
]);

// 模拟聊天记录 - 根据不同会话存储不同的聊天记录
const chatHistory = ref({
  1: [ // 佳食餐馆的聊天记录
    { id: 1, sender: 'user', content: '这个麻辣香锅饭太好吃了！', time: '2024-11-21 14:30', isRead: false },
    { id: 2, sender: 'merchant', content: '感谢您的好评！', time: '2024-11-21 14:31', isRead: true }
  ],
  2: [ // 美味小吃店的聊天记录
    { id: 1, sender: 'merchant', content: '您点的奶茶已完成', time: '2024-11-21 14:15', isRead: true }
  ],
  3: [ // 系统通知的聊天记录
    { id: 1, sender: 'system', content: '您的账户已成功充值', time: '2024-11-21 10:00', isRead: true }
  ],
  4: [ // 美食爱好者群的聊天记录
    { id: 1, sender: '系统', content: '李四加入了群聊', time: '10:30' },
    { id: 2, sender: '张三', content: '大家一起点个外卖吧！', time: '10:31' },
    { id: 3, sender: '王五', content: '好啊，我要宫保鸡丁', time: '10:32' },
    { id: 4, sender: '李四', content: '我要麻婆豆腐', time: '10:33' }
  ],
  5: [ // 同事午餐群的聊天记录
    { id: 1, sender: '小明', content: '今天中午吃什么？', time: '09:15' },
    { id: 2, sender: '小红', content: '我要一份红烧肉盖饭', time: '09:16' }
  ]
});

// 当前显示的聊天记录
const chatMessages = ref([]);

// 当前选中的会话
const selectedConversation = ref(null);

// 模拟群订单数据
// 群订单 - 改为对象存储，key为群聊会话ID，实现多群订单独立
const groupOrders = ref({});
// 群订单购物车悬浮窗可见性
const orderDrawerVisible = ref(false);

// 悬浮按钮拖拽功能
const floatBtnRef = ref(null); // 按钮容器ref
const isDragging = ref(false);
const hasDragged = ref(false); // 用于判断是否是拖拽操作还是点击操作
const startX = ref(0);
const startY = ref(0);

// 拖拽事件处理函数引用，用于移除事件监听器
let handleMouseMove = null;
let handleMouseUp = null;

// 点击悬浮按钮处理函数
const handleCartClick = () => {
  // 如果是拖拽操作后的松绑, 不触发点击
  if (hasDragged.value) {
    hasDragged.value = false;
    return;
  }

  // 只有在不是拖拽状态下才打开抽屉
  if (!isDragging.value) {
    orderDrawerVisible.value = true;
  }
};


// 拖拽过程中
const onDrag = (e) => {
  hasDragged.value = true; // 标记为拖拽操作
  if (isDragging.value && floatBtnRef.value) {
    const floatBtn = floatBtnRef.value;
    // 计算新位置
    let newX = e.clientX - startX.value;
    let newY = e.clientY - startY.value;

    // 限制按钮在视窗内
    const windowWidth = window.innerWidth;
    const windowHeight = window.innerHeight;
    const btnWidth = floatBtn.offsetWidth;
    const btnHeight = floatBtn.offsetHeight;

    newX = Math.max(0, Math.min(newX, windowWidth - btnWidth));
    newY = Math.max(0, Math.min(newY, windowHeight - btnHeight));

    // 更新按钮位置
    floatBtn.style.left = newX + 'px';
    floatBtn.style.top = newY + 'px';
    floatBtn.style.bottom = 'auto';
    floatBtn.style.right = 'auto';

    e.preventDefault();
  }
};

// 声明模块级事件处理器变量
let handleMouseMoveFn = null;
let handleMouseUpFn = null;

// 开始拖拽
const startDrag = (e) => {
  if (!floatBtnRef.value) return;

  isDragging.value = true;
  // 记录初始位置，确保使用整个按钮容器进行计算
  startX.value = e.clientX - floatBtnRef.value.offsetLeft;
  startY.value = e.clientY - floatBtnRef.value.offsetTop;

  // 将拖拽事件绑定到document以避免阻尼效果
  handleMouseMoveFn = (moveEvent) => {
    onDrag(moveEvent);
  };

  handleMouseUpFn = () => {
    stopDrag();
  };

  // 添加事件监听器
  document.addEventListener('mousemove', handleMouseMoveFn);
  document.addEventListener('mouseup', handleMouseUpFn);

  // 防止默认的文本选择行为
  e.preventDefault();
};

// 停止拖拽
const stopDrag = () => {
  isDragging.value = false;

  // 移除document上的事件监听器
  if (handleMouseMoveFn) {
    document.removeEventListener('mousemove', handleMouseMoveFn);
    handleMouseMoveFn = null;
  }
  if (handleMouseUpFn) {
    document.removeEventListener('mouseup', handleMouseUpFn);
    handleMouseUpFn = null;
  }
};

// 阻止文本选择
const handleSelectStart = (e) => {
  e.preventDefault();
};

// 商家选择相关
const merchantSelectDialogVisible = ref(false);
const productSelectDialogVisible = ref(false);
const selectedMerchant = ref(null);

// 模拟商家列表 - 包含食材信息
const merchants = ref([
  { id: 101, name: '佳食餐馆', avatar: '🏪', type: 'Chinese', products: [
    { id: 1, name: '麻婆豆腐', price: 18.8, description: '麻辣鲜香', requiredIngredients: ['豆腐', '牛肉末', '豆瓣酱'], optionalIngredients: [{ id: 1, name: '加麻 (+1.0)' }, { id: 2, name: '加辣 (+1.0)' }, { id: 3, name: '加葱花 (+0.5)' }] },
    { id: 2, name: '宫保鸡丁', price: 22.8, description: '酸甜可口', requiredIngredients: ['鸡肉', '花生', '辣椒'], optionalIngredients: [{ id: 1, name: '加花生 (+2.0)' }, { id: 2, name: '加辣椒 (+1.0)' }] },
    { id: 3, name: '回锅肉', price: 24.8, description: '经典川菜', requiredIngredients: ['五花肉', '蒜苗', '豆瓣酱'], optionalIngredients: [{ id: 1, name: '加蒜苗 (+1.0)' }, { id: 2, name: '加木耳 (+1.5)' }] }
  ]},
  { id: 102, name: '美味小吃店', avatar: '🏪', type: 'Snack', products: [
    { id: 1, name: '奶茶', price: 12.8, description: '珍珠奶茶', requiredIngredients: ['牛奶', '茶', '珍珠'], optionalIngredients: [{ id: 1, name: '加冰 (+0.0)' }, { id: 2, name: '加珍珠 (+1.0)' }, { id: 3, name: '加椰果 (+1.0)' }] },
    { id: 2, name: '汉堡', price: 15.8, description: '牛肉汉堡', requiredIngredients: ['面包', '牛肉', '生菜'], optionalIngredients: [{ id: 1, name: '加芝士 (+2.0)' }, { id: 2, name: '加番茄 (+0.5)' }, { id: 3, name: '加酱料 (+0.0)' }] },
    { id: 3, name: '炸鸡', price: 18.8, description: '香脆炸鸡', requiredIngredients: ['鸡肉', '面粉', '油'], optionalIngredients: [{ id: 1, name: '加番茄酱 (+0.5)' }, { id: 2, name: '加孜然 (+0.5)' }, { id: 3, name: '加辣椒 (+0.5)' }] }
  ]}
]);

// 选中的商品列表 - 包含备注
const selectedProducts = ref([]);

// 更换商家
const changeMerchant = () => {
  if (!selectedConversation.value) return;

  // 直接打开商家选择对话框
  merchantSelectDialogVisible.value = true;

  // 在选择新商家时，会自动覆盖旧的商家信息
  // 订单商品和总金额将在 confirmProductSelection 中重新计算，但我们也可以提前清空
  const currentOrder = groupOrders.value[selectedConversation.value.id];
  if (currentOrder) {
    // 提前清空订单商品和总金额
    currentOrder.orderItems = [];
    currentOrder.totalAmount = 0;
  }
};

// 打开商家/商品选择对话框
const openMerchantSelectDialog = () => {
  if (!selectedConversation.value || !groupOrders.value[selectedConversation.value.id]) {
    ElMessage.error('请先创建群订单');
    return;
  }

  // 如果已经有选中的商家，直接打开商品选择对话框
  if (orderingMerchant.value) {
    // 恢复selectedMerchant，以便商品对话框能正确显示
    selectedMerchant.value = orderingMerchant.value;
    productSelectDialogVisible.value = true;
  } else {
    // 否则打开商家选择对话框
    merchantSelectDialogVisible.value = true;
  }
};

// 已选择的下单商家
const orderingMerchant = ref(null);

// 选择商家
const selectMerchant = (merchant) => {
  selectedMerchant.value = merchant;
  orderingMerchant.value = merchant; // 标记该商家为群订单的下单商家
  selectedProducts.value = []; // 清空已选商品
  productRemarks.value = {}; // 清空商品备注
  merchantSelectDialogVisible.value = false;

  // 更新群订单信息
  if (selectedConversation.value && groupOrders.value[selectedConversation.value.id]) {
    const currentOrder = groupOrders.value[selectedConversation.value.id];
    currentOrder.merchantId = merchant.id;
    currentOrder.merchantName = merchant.name;
  }

  // 发送系统消息通知群成员已选择/更换商家
  const action = groupOrders.value[selectedConversation.value.id].merchantId ? '更换' : '选择';
  const merchantSelectedMsg = {
    id: chatMessages.value.length + 1,
    sender: '系统',
    content: `已${action}商家：${merchant.name}${action === '更换' ? '，购物车已清空' : '，大家可以开始点餐了'}！`,
    time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  };
  chatMessages.value.push(merchantSelectedMsg);

  // 更新会话列表的最后一条消息
  selectedConversation.value.lastMessage = `系统: 已选择商家：${merchant.name}`;
  selectedConversation.value.time = merchantSelectedMsg.time;

  // 打开商品选择对话框
  productSelectDialogVisible.value = true;
};

// 商品备注
const productRemarks = ref({});

// 商品选中的可选食材
const productSelectedOptionalIngredients = ref({});

// 切换商品选择
const toggleProductSelection = (product) => {
  const index = selectedProducts.value.findIndex(item => item.id === product.id);
  if (index === -1) {
    // 初始化可选食材为空数组
    productSelectedOptionalIngredients.value[product.id] = productSelectedOptionalIngredients.value[product.id] || [];
    selectedProducts.value.push({
      ...product,
      quantity: 1,
      remark: productRemarks.value[product.id] || '',
      requiredIngredients: [...product.requiredIngredients], // 复制必选食材
      selectedOptionalIngredients: productSelectedOptionalIngredients.value[product.id] || [] // 已选可选食材
    });
  } else {
    selectedProducts.value.splice(index, 1);
  }
};

// 更新商品备注
const updateProductRemark = (productId, remark) => {
  productRemarks.value[productId] = remark;

  // 更新已选商品列表中的备注
  const index = selectedProducts.value.findIndex(item => item.id === productId);
  if (index !== -1) {
    selectedProducts.value[index].remark = remark;
  }
};

// 更新商品可选食材
const updateProductOptionalIngredients = (productId, ingredients) => {
  productSelectedOptionalIngredients.value[productId] = ingredients;

  // 更新已选商品列表中的可选食材
  const index = selectedProducts.value.findIndex(item => item.id === productId);
  if (index !== -1) {
    selectedProducts.value[index].selectedOptionalIngredients = ingredients;
  }
};

// 更新商品数量
const updateProductQuantity = (product, change) => {
  const index = selectedProducts.value.findIndex(item => item.id === product.id);
  if (index !== -1) {
    selectedProducts.value[index].quantity += change;
    if (selectedProducts.value[index].quantity <= 0) {
      selectedProducts.value.splice(index, 1);
    }
  }
};

// 单个商品加入购物车
const addProductToCart = (product) => {
  // 找到该商品在已选商品列表中的位置
  const selectedProductIndex = selectedProducts.value.findIndex(item => item.id === product.id);
  if (selectedProductIndex === -1) return;

  // 获取完整的商品信息，包括自定义
  const customizedProduct = selectedProducts.value[selectedProductIndex];

  // 更新群订单
  if (selectedConversation.value && groupOrders.value[selectedConversation.value.id]) {
    const currentOrder = groupOrders.value[selectedConversation.value.id];

    // 检查该商品是否已经在订单中，需要比较ID、可选食材和备注是否完全相同
    const existingItemIndex = currentOrder.orderItems.findIndex(item =>
      item.id === customizedProduct.id &&
      JSON.stringify(item.selectedOptionalIngredients) === JSON.stringify(customizedProduct.selectedOptionalIngredients) &&
      item.remark === customizedProduct.remark
    );

    if (existingItemIndex === -1) {
      // 如果没有完全相同的商品，直接添加新的商品项
      currentOrder.orderItems.push({ ...customizedProduct });
    } else {
      // 如果有完全相同的商品，更新现有商品数量
      currentOrder.orderItems[existingItemIndex].quantity += customizedProduct.quantity;
    }

    // 更新总金额
    currentOrder.totalAmount = currentOrder.orderItems.reduce((total, item) => {
      return total + (item.price * item.quantity);
    }, 0);

    ElMessage.success('商品已加入购物车');
  }

  // 清空该商品的配置
  clearProductConfiguration(product.id);
};

// 清空商品配置
const clearProductConfiguration = (productId) => {
  // 从已选商品列表中移除
  const index = selectedProducts.value.findIndex(item => item.id === productId);
  if (index !== -1) {
    selectedProducts.value.splice(index, 1);
  }

  // 清空可选食材
  productSelectedOptionalIngredients.value[productId] = [];

  // 清空备注
  productRemarks.value[productId] = '';
};

// 确认选择商品
const confirmProductSelection = () => {
  if (selectedProducts.value.length === 0) {
    ElMessage.error('请至少选择一个商品');
    return;
  }

  // 更新群订单
  if (selectedConversation.value && groupOrders.value[selectedConversation.value.id]) {
    const currentOrder = groupOrders.value[selectedConversation.value.id];
    // 将商品添加到群订单
    selectedProducts.value.forEach(product => {
      // 检查该商品是否已经在订单中，需要比较ID、可选食材和备注是否完全相同
      const existingItemIndex = currentOrder.orderItems.findIndex(item =>
        item.id === product.id &&
        JSON.stringify(item.selectedOptionalIngredients) === JSON.stringify(product.selectedOptionalIngredients) &&
        item.remark === product.remark
      );

      if (existingItemIndex === -1) {
        // 如果没有完全相同的商品，直接添加新的商品项
        currentOrder.orderItems.push({ ...product });
      } else {
        // 如果有完全相同的商品，更新现有商品数量
        currentOrder.orderItems[existingItemIndex].quantity += product.quantity;
      }
    });

    // 更新总金额
    currentOrder.totalAmount = currentOrder.orderItems.reduce((total, item) => {
      return total + (item.price * item.quantity);
    }, 0);

    ElMessage.success('商品已添加到群订单');
  }

  // 关闭对话框
  productSelectDialogVisible.value = false;
  selectedProducts.value = [];
  selectedMerchant.value = null;
};

// 新消息内容
const newMessage = ref('');

// 排序后的会话列表 - 置顶会话在前，然后按时间降序排列
const sortedConversations = computed(() => {
  return [...conversations.value].sort((a, b) => {
    // 置顶会话在前
    if (a.pinned && !b.pinned) return -1;
    if (!a.pinned && b.pinned) return 1;

    // 按时间降序排列
    return new Date(b.time) - new Date(a.time);
  });
});


// 页面加载
onMounted(() => {
  // 默认选中第一个会话
  if (sortedConversations.value.length > 0) {
    selectedConversation.value = sortedConversations.value[0];
    // 加载对应的聊天记录
    chatMessages.value = chatHistory.value[selectedConversation.value.id] || [];
  }

  // 点击页面其他地方关闭右键菜单
  const closeContextMenu = () => {
    contextMenuVisible.value = false;
    selectedContextConversation.value = null;
  };

  // 添加全局点击事件监听器
  document.addEventListener('click', closeContextMenu);

  // 在组件卸载时移除事件监听器
  onBeforeUnmount(() => {
    document.removeEventListener('click', closeContextMenu);
  });
});

// 显示右键菜单
const showContextMenu = (conversation, event) => {
  selectedContextConversation.value = conversation;
  contextMenuPosition.value = {
    x: event.clientX,
    y: event.clientY
  };
  contextMenuVisible.value = true;
};

// 切换置顶状态
const togglePin = (conversation) => {
  conversation.pinned = !conversation.pinned;
  // 更新localStorage或其他持久化存储
  contextMenuVisible.value = false; // 关闭右键菜单
  selectedContextConversation.value = null;
  ElMessage({
    message: conversation.pinned ? '会话已置顶' : '会话已取消置顶',
    type: 'success'
  });
};

// 删除会话
const deleteConversation = (conversation) => {
  const index = conversations.value.findIndex(item => item.id === conversation.id);
  if (index !== -1) {
    conversations.value.splice(index, 1);
    // 更新localStorage或其他持久化存储
    contextMenuVisible.value = false; // 关闭右键菜单
    selectedContextConversation.value = null;
    // 如果删除的是当前选中的会话，清空选中状态
    if (selectedConversation.value?.id === conversation.id) {
      selectedConversation.value = null;
    }
    ElMessage({
      message: '会话已删除',
      type: 'success'
    });
  }
};

// 选择会话
const selectConversation = (conversation) => {
  selectedConversation.value = conversation;

  // 切换会话时，重置商家和商品选择状态
  selectedMerchant.value = null;
  orderingMerchant.value = null;

  // 清空未读消息
  if (conversation.unreadCount > 0) {
    conversation.unreadCount = 0;
    ElMessage.success('消息已标记为已读');
  }

  // 根据会话ID加载对应的聊天记录
  chatMessages.value = chatHistory.value[conversation.id] || [];

  // 加载群订单信息（如果是群聊）
  if (conversation.type === 'group') {
    // 这里可以添加实际的API请求
    // groupOrders.value[conversation.id] = await axios.get(`/api/group-orders/${conversation.id}`);
  }
};

// 新建聊天
const createNewChat = () => {
  // 模拟联系人列表对话框
  ElMessageBox({
    title: '选择联系人',
    message: `
      <div style="max-height: 300px; overflow-y: auto;">
        <div class="contact-item" style="padding: 10px; cursor: pointer;">👨‍💼 张三</div>
        <div class="contact-item" style="padding: 10px; cursor: pointer;">👩‍💼 李四</div>
        <div class="contact-item" style="padding: 10px; cursor: pointer;">👨‍🍳 王五</div>
        <div class="contact-item" style="padding: 10px; cursor: pointer;">👩‍🔧 赵六</div>
      </div>
    `,
    dangerouslyUseHTMLString: true,
    showCancelButton: true,
    confirmButtonText: '开始聊天',
    cancelButtonText: '取消'
  }).then(() => {
    ElMessage.success('聊天功能已实现，将跳转到聊天界面');
  }).catch(() => {
    // 取消操作
  });
};

// 新建群聊对话框可见性
const groupDialogVisible = ref(false);
// 新建群聊表单数据
const groupForm = ref({
  name: '',
  members: '' // 用逗号分隔的成员列表
});

// 新建群聊
const createNewGroup = () => {
  groupDialogVisible.value = true;
};

// 创建群聊
const handleCreateGroup = () => {
  if (!groupForm.value.name.trim()) {
    ElMessage.error('请输入群名称');
    return;
  }

  // 生成唯一ID
  const newGroupId = Date.now();

  // 创建新群聊
  const newGroup = {
    id: newGroupId,
    type: 'group',
    name: groupForm.value.name.trim(),
    avatar: '👥',
    lastMessage: '暂无消息',
    time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    unreadCount: 0,
    memberCount: (groupForm.value.members ? groupForm.value.members.split(',').length + 1 : 1), // 包括创建者
    pinned: false
  };

  // 添加到会话列表
  conversations.value.push(newGroup);

  // 初始化聊天历史
  chatHistory.value[newGroupId] = [];

  // 添加系统消息
  const systemMsg = {
    id: 1,
    sender: '系统',
    content: `群聊 "${newGroup.name}" 已创建`,
    time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  };
  chatHistory.value[newGroupId].push(systemMsg);

  // 更新会话的最后一条消息
  newGroup.lastMessage = systemMsg.content;

  // 关闭对话框
  groupDialogVisible.value = false;

  // 重置表单
  groupForm.value = {
    name: '',
    members: ''
  };

  ElMessage.success('群聊已创建');
};

// 取消创建群聊
const cancelCreateGroup = () => {
  groupDialogVisible.value = false;
  // 重置表单
  groupForm.value = {
    name: '',
    members: ''
  };
};

// 发送消息
const sendMessage = () => {
  if (!newMessage.value.trim() || !selectedConversation.value) {
    return;
  }

  // 创建新消息
  const message = {
    id: Date.now(),
    sender: '我',
    content: newMessage.value.trim(),
    time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    isRead: true
  };

  // 添加到聊天记录
  chatMessages.value.push(message);

  // 更新会话列表的最后一条消息
  selectedConversation.value.lastMessage = message.content;
  selectedConversation.value.time = message.time;

  // 将消息保存到对应的聊天历史中
  chatHistory.value[selectedConversation.value.id] = chatMessages.value;

  // 清空输入框
  newMessage.value = '';
};

// 创建群订单
const createGroupOrder = () => {
  // 创建一个新的群订单
  if (selectedConversation.value) {
    const order = {
      orderId: `GO${Date.now()}`, // 生成唯一的群订单ID
      groupId: selectedConversation.value.id,
      groupName: selectedConversation.value.name,
      creator: '我', // 当前登录用户
      members: ['我'],
      orderItems: [],
      totalAmount: 0.0,
      status: 'active', // 订单状态：active（活动）、closed（已关闭）、paid（已支付）
      createTime: new Date().toISOString()
    };

    // 这里可以添加实际的API请求
    // await axios.post('/api/group-orders', order);

    groupOrders.value[selectedConversation.value.id] = order;
    ElMessage.success('群订单已创建');

    // 更新群聊消息
    const orderMsg = {
      id: chatMessages.value.length + 1,
      sender: '系统',
      content: '我创建了一个群订单，大家可以加入并添加商品',
      time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
    };
    chatMessages.value.push(orderMsg);

    // 更新会话列表的最后一条消息
    selectedConversation.value.lastMessage = '系统: 我创建了一个群订单';
    selectedConversation.value.time = orderMsg.time;
  } else {
    ElMessage.error('请先选择一个群聊');
  }
};

// 加入群订单
const joinGroupOrder = () => {
  // 加入一个已存在的群订单
  if (selectedConversation.value) {
    // 检查是否当前有群订单
    const conversationOrder = groupOrders.value[selectedConversation.value.id];
    if (conversationOrder) {
      if (conversationOrder.status === 'active') { // 只有活动状态的订单才能加入
        // 检查是否已经在群订单中
        if (!conversationOrder.members.includes('我')) {
          conversationOrder.members.push('我');
          ElMessage.success('已加入群订单');

          // 更新群聊消息
          const joinMsg = {
            id: chatMessages.value.length + 1,
            sender: '系统',
            content: '我加入了群订单',
            time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
          };
          chatMessages.value.push(joinMsg);

          // 更新会话列表的最后一条消息
          selectedConversation.value.lastMessage = '系统: 我加入了群订单';
          selectedConversation.value.lastTime = joinMsg.time;
        } else {
          ElMessage.warning('你已经在群订单中了');
        }
      } else {
        ElMessage.error('该群订单已关闭或已支付，无法加入');
      }
    } else {
      ElMessage.error('当前群没有订单，请先创建群订单');
    }
  } else {
    ElMessage.error('请先选择一个群聊');
  }
};


// 跳转到订单确认页
const goToOrderConfirmation = () => {
  // 存储群订单信息到会话存储
  if (selectedConversation.value && groupOrders.value[selectedConversation.value.id]) {
    const currentOrder = groupOrders.value[selectedConversation.value.id];
    const pendingOrder = {
      cartItems: currentOrder.orderItems.map(item => ({
        ...item,
        price: item.price || 22.2, // 使用商品自身价格或默认价格
        remark: item.remark || '' // 添加商品备注
      })),
      totalAmount: currentOrder.totalAmount,
      fromChat: true,
      groupName: currentOrder.groupName,
      orderId: currentOrder.orderId, // 添加群订单ID
      creator: currentOrder.creator, // 添加订单创建人
      members: currentOrder.members // 添加订单成员列表
    };

    sessionStorage.setItem('pendingOrder', JSON.stringify(pendingOrder));

    // 跳转到订单确认页
    router.push('/user/home/order-confirmation');
  } else {
    ElMessage.error('当前没有群订单');
  }
};
</script>

<style scoped lang="less">
.chat-container {
  padding: 0 20px 20px 20px;
  height: calc(100vh - 60px);

  .chat-header {
    margin-bottom: 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    .page-title {
      font-size: 18px;
      font-weight: 600;
      margin: 0;
    }

    .chat-actions {
      display: flex;
      gap: 10px;
    }
  }

  .chat-content {
    display: flex;
    gap: 20px;
    height: calc(100vh - 120px);

    .conversation-list {
      width: 37%; /* 固定宽度 */
      border: 1px solid #e4e7ed;
      border-radius: 4px;
      overflow : hidden;
      white-space: nowrap;
      text-overflow: ellipsis;

      .conversation-item {
        display: flex;
        align-items: center;
        padding: 16px; /* 调整内边距 */
        cursor: pointer;
        border-bottom: 1px solid #e4e7ed;
        transition: background-color 0.3s;
        position: relative; /* 为未读消息红点定位提供参考 */

        &:hover {
          background-color: #f5f7fa;
        }

        &.active {
          background-color: #ecf5ff;
        }

        &.pinned-conversation {
          background-color: #fffbe6; /* 置顶会话背景色 */
          border-left: 3px solid #ffd591; /* 左侧标记条 */
        }

        .pin-btn {
          position: absolute;
          top: 8px;
          right: 8px;
          font-size: 14px;
          cursor: pointer;
          opacity: 0.5;
          transition: opacity 0.2s;

          &:hover {
            opacity: 1;
          }
        }

        .conversation-avatar {
          margin-right: 11px; /* 调整头像右侧间距 */
          position: relative; /* 为未读消息红点定位提供参考 */

          img {
            width: 35px; /* 调整头像大小 */
            height: 35px; /* 调整头像大小 */
            border-radius: 7px;
            object-fit: contain;
            aspect-ratio: 1 / 1; /* 确保长宽比为1:1 */
          }

          .emoji-avatar {
            width: 35px;
            height: 35px;
            border-radius: 7px;
            background-color: #f0f0f0;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 24px; /* 调整emoji大小 */
            text-align: center;
          }
        }

        .conversation-info {
          flex: 1;
          min-width: 0; /* 确保flex元素能正确收缩，让省略号生效 */

          .name-time {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 4px;
            font-size: 14px;

            .name {
              font-weight: 500;
              white-space: nowrap;
              overflow: hidden;
              text-overflow: ellipsis;
              flex: 1; /* 让名称占据剩余空间 */
              margin-right: 8px; /* 与时间保持一定距离 */

              .member-count {
                font-size: 8px;
                color: #909399;
              }
            }

            .time {
              font-size: 8px;
              white-space: nowrap; /* 时间不换行 */
              color: #909399;
            }
          }

          .last-message {
            font-size: 10px;
            color: #606266;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
          }
        }

        .unread-count {
          background-color: #f56c6c;
          width: 10px ;
          height: 10px;
          color: #fff;
          border-radius: 50%;
          padding: 2px; /* 减小内边距，使红点更紧凑 */
          font-size: 7px; /* 减小字体大小 */
          position: absolute; /* 绝对定位 */
          top: 0; /* 根据头像大小精确计算位置 */
          right: 0; /* 根据头像大小精确计算位置 */
          transform: translate(50%, -50%); /* 使红点中心对齐到头像右上角 */
          z-index: 1; /* 确保红点在最上层 */
          min-height: 7px; /* 设置最小高度，确保单个数字也能显示为圆形 */
          min-width: 7px; /* 设置最小宽度，确保单个数字也能显示为圆形 */
          text-align: center; /* 文字居中 */
        }
      }
    }

    .chat-area {
      flex: 1;
      border: 1px solid #e4e7ed;
      border-radius: 4px;
      display: flex;
      flex-direction: column;

      .chat-area-header {
        padding: 12px;
        border-bottom: 1px solid #e4e7ed;
        display: flex;
        justify-content: space-between;
        align-items: center;

        .conversation-info {
          display: flex;
          align-items: center;

          .name-info {
            .name {
              font-weight: 500;
              white-space: nowrap;
              overflow: hidden;
              text-overflow: ellipsis;

              .member-count {
                font-size: 12px;
                color: #909399;
              }
            }
          }
        }
      }

      .group-order-info {
        padding: 15px; /* 减少内边距 */
      }

      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
      }

      .order-overview {
        margin-bottom: 16px;

        .overview-item {
          margin-bottom: 8px;
          font-size: 14px; /* 缩小订单概览文字 */

          .info-label {
            font-weight: 600;
            color: #555;
            margin-right: 8px;
          }
        }
      }

      .order-actions {
        display: flex;
        gap: 10px;
      }

      .quick-order-entry {
        margin: 15px 0;
      }

      /* 悬浮订单按钮样式 */
      .floating-order-btn {
        position: fixed;
        bottom: 80px;
        right: 40px;
        width: 60px;
        height: 60px;
        background-color: #67c23a;
        color: white;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        box-shadow: 0 3px 15px 0 rgba(103, 194, 58, 0.4);
        z-index: 1000;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

        .order-btn-inner {
          display: flex;
          align-items: center;
          justify-content: center;
          width: 100%;
          height: 100%;
          position: relative;
          font-size: 24px;
        }

        &:hover {
          background-color: #85ce61;
          transform: translateY(-3px);
          box-shadow: 0 5px 20px 0 rgba(103, 194, 58, 0.5);
        }

        /* 购物车商品数量标记 */
        .cart-count {
          position: absolute;
          top: -5px;
          right: -5px;
          background: linear-gradient(135deg, #f56c6c, #ff8787);
          color: white;
          font-size: 13px;
          font-weight: 600;
          width: 24px;
          height: 24px;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          box-shadow: 0 2px 8px rgba(245, 108, 108, 0.3);
        }
      }

      /* 订单详情美化 */
      .order-overview {
        background-color: #f8f9fa;
        padding: 20px;
        border-radius: 10px;
        margin-bottom: 25px;
        box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.08);

        .overview-item {
          margin-bottom: 15px;
          font-size: 14px;
          line-height: 1.6;

          &:last-child {
            margin-bottom: 0;
          }

          .info-label {
            font-weight: 600;
            color: #333;
            margin-right: 10px;
            min-width: 80px;
          }

          .info-value {
            color: #555;
          }
        }
      }

      .order-items {
        margin-top: 20px;
        max-height: 300px; /* 适当增加订单商品最大高度 */
        overflow-y: auto; /* 超出部分显示滚动条 */
        padding-right: 5px; /* 为滚动条预留空间 */

        .section-title {
          font-size: 14px; /* 缩小标题文字 */
          font-weight: 600;
          margin-bottom: 10px;
          color: #34495e;
        }

        .item-list {
          display: flex;
          flex-direction: column; /* 纵向排列以节省空间 */
          gap: 5px;
        }

        .order-item-card {
          padding: 15px;
          min-height: auto;
          margin-bottom: 12px;
          border-radius: 10px;
          background-color: #fff;
          box-shadow: 0 1px 8px 0 rgba(0, 0, 0, 0.06);
          transition: box-shadow 0.2s ease;

          &:hover {
            box-shadow: 0 3px 15px 0 rgba(0, 0, 0, 0.1);
          }

          .order-item-header {
            margin-bottom: 5px; /* 减少底部间距 */

            .item-name {
              font-size: 13px; /* 缩小商品名称 */
              font-weight: 500;
            }

            .item-quantity,
            .item-price {
              font-size: 12px; /* 缩小数量和价格 */
              color: #666;
            }
          }

          .item-ingredients {
            margin-bottom: 5px; /* 减少底部间距 */

            .ingredient-label {
              font-size: 12px; /* 缩小食材标签 */
              font-weight: 500;
              margin-bottom: 3px;
            }

            .ingredient-list {
              display: flex;
              flex-wrap: wrap;
              gap: 3px; /* 减少标签间距 */
            }
          }

          .item-remark {
            margin-top: 5px;

            .remark-label {
              font-size: 12px;
              font-weight: 500;
              margin-bottom: 3px;
            }

            .remark-content {
              font-size: 12px;
              color: #666;
            }
          }
        }
      }

      .messages-container {
        flex: 1;
        padding: 11px;
        overflow-y: auto;
        display: flex;
        flex-direction: column;

        .message-item {
          margin-bottom: 16px;
          max-width: 70%;

          .message-header {
            margin-bottom: 4px;
            .sender-name {
              font-size: 12px;
              color: #666;
            }
          }

          .message-content {
            border-radius: 10px;
            padding: 7px;
            font-size: 12px;

            .message-time {
              text-align: right;
              font-size: 10px;
              margin-top: 4px;
            }
          }

          &.others-message {
            align-self: flex-start;

            .message-content {
              background-color: #fff;
              border: 1px solid #ddd;

              .message-time {
                color: #909399;
              }
            }
          }

          &.my-message {
            align-self: flex-end;

            .message-content {
              background-color: #67c23a;
              color: #fff;

              .message-time {
                opacity: 0.8;
              }
            }
          }
        }
      }

      .message-input-container {
        padding: 12px;
        border-top: 1px solid #e4e7ed;
        display: flex;
        gap: 12px;

        .el-input {
          flex: 1;
        }

        button {
          align-self: flex-end;
        }
      }
    }
  }

  /* 商家选择对话框样式 */
  .merchant-list {
    display: flex;
    flex-direction: column;
    gap: 10px;
  }

  .merchant-item {
    display: flex;
    align-items: center;
    padding: 15px;
    border: 1px solid #e4e7ed;
    border-radius: 4px;
    cursor: pointer;
    transition: background-color 0.3s;

    &:hover {
      background-color: #f5f7fa;
    }
  }

  .merchant-avatar {
    font-size: 32px;
    margin-right: 15px;
  }

  .merchant-info {
    flex: 1;

    .merchant-name {
      margin: 0 0 5px 0;
      font-weight: 500;
    }

    .merchant-type {
      margin: 0;
      font-size: 12px;
      color: #909399;
    }
  }

  /* 商品选择对话框样式 */
  .product-list {
    display: flex;
    flex-direction: column;
    gap: 15px;
    max-height: 400px;
    overflow-y: auto;
  }

  .product-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 15px;
    border: 1px solid #e4e7ed;
    border-radius: 4px;
  }

  .product-info {
    flex: 1;

    .product-name {
      margin: 0 0 5px 0;
      font-weight: 500;
    }

    .product-description {
      margin: 0 0 5px 0;
      font-size: 12px;
      color: #606266;
    }

    .product-price {
      margin: 0;
      font-size: 14px;
      font-weight: 600;
      color: #e6a23c;
    }
  }

  .product-actions {
    display: flex;
    flex-direction: column;
    gap: 10px;
    align-items: flex-end;
  }

  .quantity-control {
    display: flex;
    align-items: center;
    gap: 5px;

    .quantity {
      width: 30px;
      text-align: center;
    }
  }

  /* 空选择提示 */
  .empty-select {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    background-color: #fafafa;
    color: #999;

    .empty-icon {
      font-size: 48px;
      margin-bottom: 16px;
    }
  }

  /* 右键菜单样式 */
  .context-menu {
    position: fixed;
    z-index: 10000;
    background-color: #fff;
    border: 1px solid #ebeef5;
    border-radius: 4px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    min-width: 160px;
    padding: 8px 0;
  }

  .menu-item {
    padding: 10px 20px;
    cursor: pointer;
    font-size: 14px;
    color: #303133;
    white-space: nowrap;
    transition: background-color 0.3s ease;

    &:hover {
      background-color: #f5f7fa;
    }
  }
}</style>
