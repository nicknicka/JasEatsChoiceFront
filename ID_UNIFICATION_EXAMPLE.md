# ID统一处理使用示例

## 1. ID生成与前缀添加

### 1.1 生成用户ID
```java
// 在UserServiceImpl中
public User register(User user) {
    // 生成带前缀的用户ID
    String userId = IdGenerator.generateUserId();
    user.setUserId(userId);

    // ... 其他注册逻辑
    return user;
}

// IdGenerator实现
public static String generateUserId() {
    return IdPrefixUtil.addUserPrefix(generateId());
}
```

### 1.2 生成商家ID
```java
// 在MerchantServiceImpl中
public Merchant register(Merchant merchant) {
    // 生成带前缀的商家ID
    String merchantId = IdGenerator.generateMerchantId();
    merchant.setId(merchantId);

    // ... 其他注册逻辑
    return merchant;
}

// IdGenerator实现
public static String generateMerchantId() {
    return IdPrefixUtil.addMerchantPrefix(generateId());
}
```

## 2. Service层修改示例

### 2.1 UserService.java
```java
// 修改前
public User getUserById(Long userId);

// 修改后
public User getUserById(String userId);
```

### 2.2 GroupService.java
```java
// 修改前
public boolean updateGroup(Long groupId, Group group);

// 修改后
public boolean updateGroup(String groupId, Group group);
```

## 3. Controller层修改示例

### 3.1 UserController.java
```java
// 修改前
@GetMapping("/{userId}")
public ResponseResult<?> getUser(@PathVariable Long userId) {
    User user = userService.getUserById(userId);
    // ...
}

// 修改后
@GetMapping("/{userId}")
public ResponseResult<?> getUser(@PathVariable String userId) {
    User user = userService.getUserById(userId);
    // ...
}
```

### 3.2 GroupController.java
```java
// 修改前
@PutMapping("/{groupId}")
public ResponseResult<?> updateGroup(@PathVariable Long groupId, @RequestBody Group group) {
    boolean success = groupService.updateById(group);
    // ...
}

// 修改后
@PutMapping("/{groupId}")
public ResponseResult<?> updateGroup(@PathVariable String groupId, @RequestBody Group group) {
    boolean success = groupService.updateById(group);
    // ...
}
```

## 4. 数据库表修改示例

### 4.1 修改用户表
```sql
-- 用户表
ALTER TABLE t_user MODIFY COLUMN user_id VARCHAR(255) NOT NULL;
ALTER TABLE t_user MODIFY COLUMN merchant_id VARCHAR(255) NULL;

-- 商家表
ALTER TABLE t_merchant MODIFY COLUMN id VARCHAR(255) NOT NULL;

-- 订单表
ALTER TABLE t_order MODIFY COLUMN id VARCHAR(255) NOT NULL;
ALTER TABLE t_order MODIFY COLUMN user_id VARCHAR(255) NOT NULL;
ALTER TABLE t_order MODIFY COLUMN merchant_id VARCHAR(255) NOT NULL;

-- 群表
ALTER TABLE t_group MODIFY COLUMN id VARCHAR(255) NOT NULL;
ALTER TABLE t_group MODIFY COLUMN creator_id VARCHAR(255) NOT NULL;

-- 群订单表
ALTER TABLE t_group_order MODIFY COLUMN id VARCHAR(255) NOT NULL;
ALTER TABLE t_group_order MODIFY COLUMN merchant_id VARCHAR(255) NOT NULL;
ALTER TABLE t_group_order MODIFY COLUMN group_id VARCHAR(255) NOT NULL;
```

## 5. DTO转换示例

### 5.1 实体转DTO
```java
public static UserDTO convertToDTO(User user) {
    UserDTO dto = new UserDTO();
    dto.setUserId(user.getUserId()); // 自动包含前缀U
    dto.setNickname(user.getNickname());
    dto.setEmail(user.getEmail());
    // ... 其他字段转换
    return dto;
}
```

### 5.2 DTO转实体
```java
public static User convertToEntity(UserDTO dto) {
    User user = new User();
    user.setUserId(dto.getUserId()); // 直接使用带前缀的ID
    user.setNickname(dto.getNickname());
    user.setEmail(dto.getEmail());
    // ... 其他字段转换
    return user;
}
```

## 6. 注意事项

1. **不要在前端处理ID前缀**：前端应将ID视为完整的字符串
2. **数据库存储完整ID**：将带前缀的ID直接存储在数据库中
3. **不要手动拼接前缀**：始终使用IdPrefixUtil
4. **保持一致性**：所有ID操作都应使用统一的工具类

---

按照这个示例和之前提供的修改指南，您将能够完成项目的ID统一处理修改。如果遇到任何问题，请随时联系。
