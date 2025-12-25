# JWT 认证修复总结

## 修复日期
2025-12-20

## 问题描述
根据 TestSprite 测试结果，发现多个测试用例失败，主要问题是 JWT 认证相关：
1. TC001 - 登录后无法验证 JWT token
2. 多个测试用例显示登录失败，无法获取 Authorization Bearer Token
3. Token 验证逻辑不完整，只检查数据库 session，未验证 JWT token 本身的有效性

## 修复内容

### 1. 修复 `AuthService.validateAndRefreshToken()` 方法

**问题**：原方法只检查数据库中的 session，没有验证 JWT token 的签名和过期时间，存在安全漏洞。

**修复**：
- 添加 JWT token 签名验证
- 添加 JWT token 过期时间验证
- 同时验证数据库 session 和 JWT token
- 添加详细的错误处理，区分不同类型的错误（过期、签名无效、格式错误等）
- 当 token 过期时自动清理数据库中的 session

**代码位置**：`src/main/java/com/gym/my/service/AuthService.java`

**关键改进**：
```java
// 首先验证JWT token的签名和过期时间
io.jsonwebtoken.Claims claims = Jwts.parser()
        .verifyWith(getSigningKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();

// 检查token是否过期
Date expiration = claims.getExpiration();
if (expiration != null && expiration.before(new Date())) {
    throw new RuntimeException("Token已过期");
}

// 从数据库查询Session，确保token在有效session列表中
UserSession session = sessionMapper.findByToken(token);
if (session == null) {
    throw new RuntimeException("无效的Token或Session已失效");
}

// 检查数据库中的session是否过期
if (session.getExpiresAt() != null && session.getExpiresAt().isBefore(LocalDateTime.now())) {
    sessionMapper.deleteByToken(token);
    throw new RuntimeException("Session已过期，请重新登录");
}
```

### 2. 修复 `/api/auth/me` 接口

**问题**：接口返回类型不一致，错误处理不完善。

**修复**：
- 统一使用 `ResponseEntity` 返回类型
- 添加正确的 HTTP 状态码（401 for unauthorized, 500 for internal error）
- 改进错误消息

**代码位置**：`src/main/java/com/gym/my/controller/AuthController.java`

### 3. 更新 WebConfig 拦截器配置

**问题**：`/api/auth/me` 接口没有被排除在 AuthInterceptor 之外，导致可能被拦截器重复验证。

**修复**：
- 将 `/api/auth/me` 添加到排除路径列表
- 该接口自己处理 token 验证逻辑

**代码位置**：`src/main/java/com/gym/my/config/WebConfig.java`

### 4. 优化前端错误处理

**问题**：401 错误时没有清除 token 和用户信息。

**修复**：
- 在响应拦截器中，当收到 401 错误时，自动清除 token 和用户信息
- 确保用户状态正确重置

**代码位置**：`frontend/index.html`

**关键改进**：
```javascript
if (status === 401) {
  // 清除token和用户信息
  clearToken();
  // 清除当前用户信息
  currentUser.username = '';
  currentUser.name = '';
  currentUser.role = '';
  // ... 其他字段
  ElementPlus.ElMessage.warning('登录已过期，请重新登录');
  openLogin();
}
```

## 安全改进

1. **双重验证**：现在同时验证 JWT token（签名、过期时间）和数据库 session
2. **自动清理**：过期或无效的 token 会自动从数据库中删除
3. **详细错误信息**：区分不同类型的错误，便于调试和用户理解
4. **状态检查**：验证员工账号状态，禁用账号无法使用

## 测试建议

1. **正常登录流程**：
   - 使用有效用户名和密码登录
   - 验证返回的 token 格式正确
   - 使用 token 访问受保护的接口

2. **Token 验证**：
   - 使用过期 token 访问接口，应返回 401
   - 使用无效签名 token 访问接口，应返回 401
   - 使用有效 token 但数据库 session 已删除，应返回 401

3. **错误处理**：
   - 测试各种错误场景（过期、无效、缺失）
   - 验证前端正确显示错误消息
   - 验证 token 和用户信息被正确清除

## 相关文件

- `src/main/java/com/gym/my/service/AuthService.java` - JWT token 验证逻辑
- `src/main/java/com/gym/my/controller/AuthController.java` - 认证接口
- `src/main/java/com/gym/my/config/AuthInterceptor.java` - 认证拦截器
- `src/main/java/com/gym/my/config/WebConfig.java` - Web 配置
- `frontend/index.html` - 前端代码

## 注意事项

1. JWT secret 在生产环境中应该从配置文件读取，而不是硬编码
2. Session 超时时间（30分钟）可以根据需求调整
3. 建议添加 token 刷新机制，避免用户频繁重新登录
4. 考虑添加登录失败次数限制，防止暴力破解

