# 佳食宜选后端API文档

## 1. 基础信息

### 1.1 接口风格
- RESTful风格
- 请求/响应格式：JSON
- 统一返回格式：
  ```json
  {
    "code": "string",      // 返回码，200表示成功，其他表示失败
    "message": "string",   // 返回信息描述
    "data": {}             // 返回数据
  }
  ```

### 1.2 认证方式
- 所有接口需在请求头中携带JWT令牌：`Authorization: Bearer <token>`

### 1.3 版本控制
- URL路径中包含版本号：`/api/v1/...`

---

## 2. 用户模块

### 2.1 用户注册
- **接口地址**：`POST /api/v1/users/register`
- **请求参数**：
  ```json
  {
    "phone": "string",          // 手机号
    "password": "string",       // 密码（MD5加密）
    "nickname": "string",       // 昵称
    "height": "number",         // 身高（cm）
    "weight": "number",         // 体重（kg）
    "dietGoal": "string",       // 饮食目标（lose_weight/keep_fit/gain_weight）
    "allergies": ["string"]     // 过敏食材列表
  }
  ```
- **返回结果**：用户信息及JWT令牌

### 2.2 用户登录
- **接口地址**：`POST /api/v1/users/login`
- **请求参数**：
  ```json
  {
    "phone": "string",
    "password": "string"
  }
  ```
- **返回结果**：用户信息及JWT令牌

### 2.3 获取用户信息
- **接口地址**：`GET /api/v1/users/{userId}`
- **返回结果**：用户详细信息

---

## 3. 个性化推荐模块

### 3.1 获取个性化推荐菜品
- **接口地址**：`GET /api/v1/recommend/{userId}`
- **请求参数**：
  - `longitude`: 经度（可选，自动定位获取）
  - `latitude`: 纬度（可选，自动定位获取）
- **返回结果**：推荐菜品列表（包含卡路里、推荐理由）

### 3.2 设置推荐偏好
- **接口地址**：`PUT /api/v1/users/{userId}/prefer`
- **请求参数**：
  ```json
  {
    "tags": {"string": number},  // 标签权重，如 {"spicy": 0.1, "sweet": 0.8}
    "disableWeatherRecommend": boolean  // 是否关闭天气推荐
  }
  ```

### 3.3 替换推荐菜品
- **接口地址**：`POST /api/v1/recommend/{userId}/replace`
- **请求参数**：
  ```json
  {
    "dishId": "string",        // 要替换的菜品ID
    "mealTime": "string"       // 餐次（breakfast/lunch/dinner/snack）
  }
  ```
- **返回结果**：相似菜品列表

### 3.4 筛选推荐菜品
- **接口地址**：`POST /api/v1/recommend/{userId}/filter`
- **请求参数**：
  ```json
  {
    "calorieRange": {"min": number, "max": number},  // 卡路里范围
    "ingredients": ["string"],  // 食材筛选
    "taste": ["string"]         // 口味筛选
  }
  ```
- **返回结果**：筛选后的菜品列表

### 3.5 一键生成购物清单
- **接口地址**：`GET /api/v1/recipe/{userId}/shopping-list`
- **请求参数**：
  - `date`: 日期（YYYY-MM-DD，默认今日）
- **返回结果**：购物清单（按食材分类）

### 3.6 记录推荐拒绝行为
- **接口地址**：`POST /api/v1/recommend/{userId}/reject`
- **请求参数**：
  ```json
  {
    "dishId": "string",        // 拒绝的菜品ID
    "reason": "string"         // 拒绝原因（可选）
  }
  ```
- **返回结果**：操作成功提示

---
## 4. 卡路里统计模块


### 4.1 获取今日卡路里摄入情况
- **接口地址**：`GET /api/v1/calorie/{userId}/today`
- **返回结果**：
  ```json
  {
    "target": number,       // 今日目标摄入量
    "consumed": number,     // 今日已摄入量
    "remaining": number,    // 今日剩余可摄入量
    "balance": "string"     // 均衡度（high/medium/low）
  }
  ```

### 4.2 获取饮食趋势数据
- **接口地址**：`GET /api/v1/calorie/{userId}/trend`
- **请求参数**：
  - `type`: 类型（week/month，默认week）
  - `startDate`: 开始日期（YYYY-MM-DD）
  - `endDate`: 结束日期（YYYY-MM-DD）
- **返回结果**：饮食趋势数据（按日期分组）

### 4.3 获取营养均衡分析
- **接口地址**：`GET /api/v1/calorie/{userId}/nutrition`
- **请求参数**：
  - `date`: 日期（YYYY-MM-DD，默认今日）
- **返回结果**：蛋白质、脂肪、碳水化合物的摄入比例分析

---

## 5. 定位与天气模块

### 5.1 获取当前定位
- **接口地址**：`GET /api/v1/location`
- **请求参数**：无
- **返回结果**：当前位置信息（城市、商圈等）

### 5.2 获取天气信息
- **接口地址**：`GET /api/v1/weather`
- **请求参数**：
  - `city`: 城市名
- **返回结果**：实时天气信息（温度、湿度等）

---

## 6. 菜品与菜单模块

### 6.1 获取菜品列表
- **接口地址**：`GET /api/v1/dishes`
- **请求参数**：
  - `category`: 菜品分类（可选）
  - `keyword`: 搜索关键词（可选）
- **返回结果**：菜品列表（包含卡路里、食材、价格等）

### 6.2 获取菜品详情
- **接口地址**：`GET /api/v1/dishes/{dishId}`
- **返回结果**：菜品详细信息（包含卡路里明细、制作方法等）

### 6.3 获取商家菜单
- **接口地址**：`GET /api/v1/merchants/{merchantId}/menu`
- **返回结果**：商家菜单列表

---

## 7. 订单模块

### 7.1 创建订单
- **接口地址**：`POST /api/v1/orders`
- **请求参数**：
  ```json
  {
    "userId": "string",
    "merchantId": "string",
    "dishItems": [
      {
        "dishId": "string",
        "quantity": number,
        "customization": "string"  // 定制要求
      }
    ],
    "addressId": "string",
    "remark": "string"  // 订单备注
  }
  ```

### 7.2 获取订单列表
- **接口地址**：`GET /api/v1/users/{userId}/orders`
- **请求参数**：
  - `status`: 订单状态（可选）
  - `page`: 页码
  - `size`: 每页数量
- **返回结果**：分页订单列表

### 7.3 获取订单详情
- **接口地址**：`GET /api/v1/orders/{orderId}`
- **返回结果**：订单详细信息

### 7.4 更新订单状态（商家端）
- **接口地址**：`PUT /api/v1/orders/{orderId}/status`
- **权限**：商家
- **请求参数**：
  ```json
  {
    "status": number,  // 订单状态定义：0-待支付、1-待接单、2-备菜中、3-烹饪中、4-待上菜、5-已完成、6-已取消
    "remark": "string"  // 状态变更备注
  }
  ```

### 7.5 取消订单
- **接口地址**：`PUT /api/v1/orders/{orderId}/cancel`

### 7.6 "再来一单"智能复购
- **接口地址**：`POST /api/v1/orders/{orderId}/reorder`
- **请求参数**：
  ```json
  {
    "addressId": "string"  // 可选，默认使用原订单地址
  }
  ```
- **返回结果**：新创建的订单信息（自动处理下架菜品提示和价格变动）

---

### 7.8 批量标记菜品状态（商家端）
- **接口地址**：`PUT /api/v1/merchants/{merchantId}/dish-status`
- **权限**：商家
- **请求参数**：
  ```json
  {
    "dishIds": ["string"],  // 菜品ID列表
    "status": "string"      // 状态：preparing/cooking/ready 等
  }
  ```
- **返回结果**：操作成功的菜品数量和失败列表

---

## 7.7 群订单模块

### 7.7.1 创建群订单
- **接口地址**：`POST /api/v1/group-orders`
- **请求参数**：
  ```json
  {
    "initiatorId": "string",  // 群订单发起者ID
    "merchantId": "string",
    "groupId": "string",
    "dishItems": [
      {
        "dishId": "string",
        "quantity": number,
        "customization": "string",
        "userId": "string"  // 菜品所属用户
      }
    ],
    "addressId": "string",
    "remark": "string"
  }
  ```

### 7.7.2 获取群订单列表
- **接口地址**：`GET /api/v1/groups/{groupId}/orders`
- **请求参数**：
  - `status`: 订单状态（可选）
  - `page`: 页码
  - `size`: 每页数量

### 7.7.3 获取群订单详情
- **接口地址**：`GET /api/v1/group-orders/{groupOrderId}`

### 7.7.4 同步群订单消息
- **接口地址**：`POST /api/v1/group-orders/{groupOrderId}/sync-message`
- **请求参数**：
  ```json
  {
    "fromId": "string",
    "content": "string"
  }
  ```
- **说明**：商家与群发起者会话的消息自动同步至群聊，以「【订单同步】」开头

---

## 7. 支付模块

### 7.1 生成支付订单
- **接口地址**：`POST /api/v1/payments`
- **请求参数**：
  ```json
  {
    "orderId": "string",
    "payType": "string"  // platform_coins/wechat/alipay
  }
  ```

### 7.2 支付回调
- **接口地址**：`POST /api/v1/payments/callback`
- **请求参数**：由支付平台提供，包含订单号、支付状态等

---

## 8. 地址管理模块

### 8.1 获取地址列表
- **接口地址**：`GET /api/v1/users/{userId}/addresses`

### 8.2 新增地址
- **接口地址**：`POST /api/v1/users/{userId}/addresses`

### 8.3 更新地址
- **接口地址**：`PUT /api/v1/users/{userId}/addresses/{addressId}`

### 8.4 删除地址
- **接口地址**：`DELETE /api/v1/users/{userId}/addresses/{addressId}`

---

## 9. 消息与聊天模块

### 9.1 获取聊天会话列表
- **接口地址**：`GET /api/v1/users/{userId}/chat-sessions`

### 9.2 获取聊天记录
- **接口地址**：`GET /api/v1/chat/{sessionId}/messages`
- **请求参数**：
  - `page`: 页码
  - `size`: 每页数量
  - `startTime`: 开始时间（可选）

### 9.3 发送消息
- **接口地址**：`POST /api/v1/chat/messages`
- **请求参数**：
  ```json
  {
    "fromId": "string",
    "toId": "string",
    "msgType": "string",  // single/group/order_sync
    "content": "string"
  }
  ```

### 9.4 标记消息已读
- **接口地址**：`PUT /api/v1/chat/messages/{messageId}/read`

---

## 10. 商家端模块

### 10.1 商家登录
- **接口地址**：`POST /api/v1/merchants/login`
- **请求参数**：
  ```json
  {
    "merchantId": "string",
    "password": "string"
  }
  ```

### 10.2 获取商家今日订单
- **接口地址**：`GET /api/v1/merchants/{merchantId}/today-orders`
- **请求参数**：
  - `status`: 订单状态（可选）

### 10.3 菜单管理
- **接口地址**：`GET /api/v1/merchants/{merchantId}/menu`  // 获取菜单
- **接口地址**：`POST /api/v1/merchants/{merchantId}/menu`  // 新增菜单
- **接口地址**：`PUT /api/v1/merchants/{merchantId}/menu/{menuId}`  // 更新菜单
- **接口地址**：`DELETE /api/v1/merchants/{merchantId}/menu/{menuId}`  // 删除菜单

### 10.4 菜品管理
- **接口地址**：`GET /api/v1/merchants/{merchantId}/dishes`  // 获取商家菜品
- **接口地址**：`POST /api/v1/merchants/{merchantId}/dishes`  // 新增菜品
- **接口地址**：`PUT /api/v1/merchants/{merchantId}/dishes/{dishId}`  // 更新菜品
- **接口地址**：`DELETE /api/v1/merchants/{merchantId}/dishes/{dishId}`  // 删除菜品
- **接口地址**：`PUT /api/v1/merchants/{merchantId}/dishes/{dishId}/status`  // 上下架菜品

### 10.6 菜单搜索与筛选
- **接口地址**：`GET /api/v1/merchants/{merchantId}/menu/search`  // 菜单搜索
- **请求参数**：
  - `keyword`: 搜索关键词
  - `status`: 菜单状态（all/active/draft/inactive，默认all）

### 10.7 菜单自动上下架设置
- **接口地址**：`PUT /api/v1/merchants/{merchantId}/menu/{menuId}/schedule`  // 设置自动上下架
- **请求参数**：
  ```json
  {
    "autoStartTime": "string",  // 自动上架时间（YYYY-MM-DD HH:mm:ss）
    "autoEndTime": "string"     // 自动下架时间（YYYY-MM-DD HH:mm:ss）
  }
  ```

### 10.5 营业统计
- **接口地址**：`GET /api/v1/merchants/{merchantId}/stats`
- **请求参数**：
  - `startDate`: 开始日期（YYYY-MM-DD）
  - `endDate`: 结束日期（YYYY-MM-DD）

### 10.6 菜单搜索与筛选
- **接口地址**：`GET /api/v1/merchants/{merchantId}/menu/search`  // 菜单搜索
- **请求参数**：
  - `keyword`: 搜索关键词
  - `status`: 菜单状态（all/active/draft/inactive，默认all）

### 10.7 菜单自动上下架设置
- **接口地址**：`PUT /api/v1/merchants/{merchantId}/menu/{menuId}/schedule`  // 设置自动上下架
- **请求参数**：
  ```json
  {
    "autoStartTime": "string",  // 自动上架时间（YYYY-MM-DD HH:mm:ss）
    "autoEndTime": "string"     // 自动下架时间（YYYY-MM-DD HH:mm:ss）
  }
  ```

### 10.8 菜单批量操作
- **接口地址**：`POST /api/v1/merchants/{merchantId}/menu/batch`  // 批量操作
- **请求参数**：
  ```json
  {
    "menuIds": ["string"],  // 菜单ID列表
    "action": "string"      // 操作类型：activate/deactivate/delete
  }
  ```
- **返回结果**：操作结果

### 10.9 "想吃列表"审核（商家端）
- **接口地址**：`PUT /api/v1/merchants/{merchantId}/want-to-eat/{itemId}`
- **权限**：商家
- **请求参数**：
  ```json
  {
    "status": "string",  // 审核状态：approved/rejected
    "remark": "string"   // 审核备注（可选）
  }
  ```
- **返回结果**：操作成功提示

---

## 11. AI能力模块

### 11.1 AI菜品识别
- **接口地址**：`POST /api/v1/ai/dish-recognize`
- **请求参数**：
  ```json
  {
    "type": "string",      // 识别类型：image/video/article
    "url": "string",       // 图片/视频/文章URL
    "content": "string"    // 文章内容（type=article时可选，优先使用url）
  }
  ```

### 11.2 AI食谱上传与优化
- **接口地址**：`POST /api/v1/ai/recipe-upload`
- **请求参数**：
  ```json
  {
    "userId": "string",
    "recipe": {
      "name": "string",        // 食谱名称
      "ingredients": [{"name": "string", "quantity": "string"}],  // 食材列表
      "steps": ["string"],     // 制作步骤
      "description": "string"  // 食谱描述
    },
    "optimize": boolean        // 是否需要AI优化
  }
  ```

---

## 12. 系统模块

### 12.1 获取系统通知
- **接口地址**：`GET /api/v1/users/{userId}/notifications`
- **请求参数**：
  - `page`: 页码
  - `size`: 每页数量

### 12.2 标记通知已读
- **接口地址**：`PUT /api/v1/notifications/{notificationId}/read`

### 12.3 上传文件
- **接口地址**：`POST /api/v1/upload`
- **请求参数**：FormData形式的文件
- **返回结果**：文件URL

---

## 13. WebSocket接口（实时通信）

### 13.1 建立连接
- **接口地址**：`ws://{host}:{port}/websocket?token={token}`

### 13.2 消息格式
```json
{
  "msgType": "string",      // single/group/order_sync/order_status
  "fromId": "string",       // 发送方ID
  "toId": "string",         // 接收方ID
  "content": "string",      // 消息内容
  "timestamp": "long",      // 时间戳
  "ack": "boolean"          // 确认标识
}
```

---

## 14. API使用示例

### 14.1 获取个性化推荐
```bash
curl -X GET "http://localhost:8080/api/v1/recommend/123456?longitude=121.4737&latitude=31.2304" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### 14.2 创建订单
```bash
curl -X POST "http://localhost:8080/api/v1/orders" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "123456",
    "merchantId": "m7890",
    "dishItems": [
      {"dishId": "d123", "quantity": 2, "customization": "少辣"}
    ],
    "addressId": "a456",
    "remark": ""
  }'
```
