# ID统一处理手动修改指南

## 1. 需要手动修改的实体列表

| 序号 | 实体名称               | 需要修改的字段 | 文件位置                                      |
|------|------------------------|----------------|-----------------------------------------------|
| 1    | GroupOrder.java        | merchantId     | src/main/java/.../entity/GroupOrder.java      |
| 2    | GroupOrder.java        | groupId        | src/main/java/.../entity/GroupOrder.java      |
| 3    | GroupOrderDish.java    | userId         | src/main/java/.../entity/GroupOrderDish.java  |
| 4    | ConsumeHistory.java    | userId         | src/main/java/.../entity/ConsumeHistory.java  |
| 5    | UserCollection.java    | userId         | src/main/java/.../entity/UserCollection.java  |
| 6    | Address.java           | userId         | src/main/java/.../entity/Address.java         |
| 7    | Notification.java      | userId         | src/main/java/.../entity/Notification.java    |
| 8    | Recipe.java            | userId         | src/main/java/.../entity/Recipe.java          |
| 9    | WantToEat.java         | userId         | src/main/java/.../entity/WantToEat.java       |

## 2. 手动修改方法

### 2.1 修改字段类型
对于每个实体文件中的指定字段，将 `private Long` 改为 `private String`。

**示例：**
```java
// 修改前
private Long merchantId;

// 修改后
private String merchantId;
```

### 2.2 修改注意事项
- 确保只修改指定的字段，不要误改其他Long类型字段
- 保留原有注释和注解
- 修改后保存文件

## 3. 完成实体修改后

### 3.1 更新Service层
- **检查ID生成逻辑**：确保所有新生成的ID都使用 `IdGenerator` 工具类
- **修改参数类型**：将Service方法中的Long类型ID参数改为String类型
- **修改返回类型**：确保返回的ID是String类型

**示例：**
```java
// 使用统一ID生成器
String groupId = IdGenerator.generateGroupId();
group.setId(groupId);
```

### 3.2 更新Controller层
- **修改请求参数类型**：将@PathVariable和@RequestBody中的Long类型ID参数改为String类型

**示例：**
```java
// 修改前
public ResponseResult<?> getGroup(@PathVariable Long groupId) {
    // ...
}

// 修改后
public ResponseResult<?> getGroup(@PathVariable String groupId) {
    // ...
}
```

### 3.3 数据库表字段同步
- 执行SQL语句将数据库表中的字段类型从bigint/varchar(20)改为varchar(255)
- **示例SQL：**
  ```sql
  ALTER TABLE t_group_order MODIFY COLUMN merchant_id VARCHAR(255) NOT NULL;
  ALTER TABLE t_group_order MODIFY COLUMN group_id VARCHAR(255) NOT NULL;
  ```

## 4. 验证与测试

### 4.1 编译验证
```bash
# 在项目根目录执行
cd JasEatsChoiceJava && ./mvnw compile -DskipTests
```

### 4.2 功能验证
- 测试所有涉及ID的CRUD操作
- 测试用户注册、登录功能
- 测试商家注册、登录功能
- 测试订单生成和查询功能

### 4.3 接口验证
- 使用Postman或其他API测试工具
- 确保所有API接口的ID参数和返回值均为String类型

## 5. 技术支持

如果您在修改过程中遇到任何问题：
- **编译错误**：检查是否有未修改的字段或类型不匹配
- **运行时错误**：检查ID生成逻辑是否正确
- **数据库错误**：检查数据库表字段类型是否与实体类一致

请随时联系技术支持获取帮助。

---
## 完成情况检查清单

[ ] 实体字段修改
[ ] GroupOrder.java - merchantId ✓
[ ] GroupOrder.java - groupId ✓
[ ] GroupOrderDish.java - userId ✓
[ ] ConsumeHistory.java - userId ✓
[ ] UserCollection.java - userId ✓
[ ] Address.java - userId ✓
[ ] Notification.java - userId ✓
[ ] Recipe.java - userId ✓
[ ] WantToEat.java - userId ✓

[ ] Service层更新
[ ] Controller层更新
[ ] 数据库表字段同步
[ ] 编译验证
[ ] 功能验证
[ ] 接口验证
