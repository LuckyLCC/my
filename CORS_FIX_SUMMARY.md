# CORS 错误修复总结

## 问题描述
新增员工时出现 "Network Error"，控制台显示 CORS 错误：
```
Access to XMLHttpRequest at 'http://localhost:8080/api/employees' from origin 'http://localhost:5173' 
has been blocked by CORS policy: Response to preflight request doesn't pass access control check: 
It does not have HTTP ok status.
```

## 问题原因
`AdminInterceptor` 拦截器没有处理 OPTIONS 预检请求，导致：
1. 浏览器发送 OPTIONS 预检请求
2. `AdminInterceptor` 检查 `currentEmployee` 属性（OPTIONS 请求没有这个属性）
3. 返回 403 Forbidden
4. 浏览器认为预检失败，阻止后续 POST 请求

## 修复方案

### 1. 修复 AdminInterceptor
在 `AdminInterceptor` 中添加 OPTIONS 请求放行逻辑：

```java
@Override
public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    // OPTIONS请求直接放行（CORS预检请求）
    if ("OPTIONS".equals(request.getMethod())) {
        return true;
    }
    
    // ... 其他逻辑
}
```

### 2. 删除重复的 CORS 配置
删除了 `CorsConfig.java`，保留 `WebConfig.java` 中的 CORS 配置，避免配置冲突。

## 修改文件

1. **src/main/java/com/gym/my/config/AdminInterceptor.java**
   - 添加 OPTIONS 请求放行逻辑

2. **src/main/java/com/gym/my/config/CorsConfig.java**
   - 已删除（避免与 WebConfig 中的 CORS 配置冲突）

## 验证步骤

修复后需要：
1. **重启 Spring Boot 应用**（修改已生效）
2. 刷新前端页面
3. 尝试新增员工，应该不再出现 CORS 错误

## 技术说明

### CORS 预检请求（Preflight Request）
当浏览器发送跨域请求时，如果满足以下条件之一，会先发送 OPTIONS 预检请求：
- 使用了非简单请求方法（如 POST、PUT、DELETE）
- 使用了自定义请求头（如 Authorization）
- 使用了 Content-Type: application/json

### 拦截器执行顺序
1. `AuthInterceptor` - 已正确处理 OPTIONS 请求（返回 true）
2. `AdminInterceptor` - **已修复**，现在也正确处理 OPTIONS 请求

### CORS 配置
`WebConfig.java` 中已正确配置：
- 允许所有来源（`allowedOriginPatterns("*")`）
- 允许所有方法（包括 OPTIONS）
- 允许所有请求头
- 允许携带凭证（`allowCredentials(true)`）

---

**修复日期**: 2025-12-19  
**修复状态**: ✅ 已完成  
**需要操作**: 重启 Spring Boot 应用使修改生效

