# Token/Session 管理问题修复说明

## 修复内容

### 1. 实现Token自动刷新机制

**问题：** JWT Token过期时间固定为30分钟，无法刷新。即使数据库Session被刷新，Token过期后仍会验证失败，导致测试过程中频繁需要重新登录。

**解决方案：**
- 在`AuthService.validateAndRefreshToken()`方法中，当Token剩余时间少于5分钟时，自动生成新Token
- 更新数据库Session，使用新Token替换旧Token
- 在响应头中返回新Token（`X-New-Token`），前端可以自动更新

### 2. 优化Token验证逻辑

**改进：**
- 创建`TokenValidationResult`类，包含Employee信息和新Token（如果被刷新）
- 当JWT Token过期但数据库Session仍然有效时，自动生成新Token并恢复会话
- 优化过期Token的处理逻辑，提高用户体验

### 3. 修改的文件

1. **src/main/java/com/gym/my/service/AuthService.java**
   - 添加`TokenValidationResult`内部类
   - 修改`validateAndRefreshToken()`方法，返回`TokenValidationResult`而不是`Employee`
   - 实现Token自动刷新逻辑（剩余时间<5分钟时自动刷新）
   - 添加`validateToken()`方法作为兼容接口

2. **src/main/java/com/gym/my/config/AuthInterceptor.java**
   - 修改为使用`TokenValidationResult`
   - 在响应头中添加`X-New-Token`（如果Token被刷新）

3. **src/main/java/com/gym/my/controller/AuthController.java**
   - 修改`getCurrentUser()`方法，支持返回新Token

## 应用修复

### 方法1：重启后端服务（推荐）

如果后端服务是通过IDE（如IntelliJ IDEA）启动的：
1. 在IDE中停止当前运行的服务
2. 重新编译项目（如果IDE支持）
3. 重新启动服务

如果后端服务是通过命令行启动的：
```bash
# 停止服务（Ctrl+C）
# 然后重新启动
cd /Users/liuchang/Documents/my2
./mvnw spring-boot:run
```

### 方法2：使用Maven重新编译（如果项目可以编译）

```bash
cd /Users/liuchang/Documents/my2
./mvnw clean compile
# 然后重启服务
```

## 验证修复

修复后，Token会在剩余时间少于5分钟时自动刷新，避免测试过程中频繁过期。

可以通过以下方式验证：
1. 登录系统
2. 等待25分钟（接近30分钟过期时间）
3. 执行任何API请求
4. 检查响应头中是否包含`X-New-Token`
5. 继续使用新Token，无需重新登录

## 注意事项

- Token刷新阈值设置为5分钟，可以根据需要调整`AuthService`中的`refreshThresholdMillis`变量
- 如果Token已完全过期但数据库Session仍然有效，系统会尝试自动恢复会话
- 前端需要处理响应头中的`X-New-Token`，自动更新本地存储的Token

