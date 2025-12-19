# 前端启动指南

## 快速启动

### 方法 1: 使用启动脚本（推荐）

```bash
cd /Users/liuchang/Documents/my
./start_frontend.sh
```

### 方法 2: 手动启动

```bash
cd /Users/liuchang/Documents/my/frontend
python3 -m http.server 5173
```

## 访问地址

启动后，在浏览器中访问：
- **前端地址**: http://localhost:5173/index.html
- **后端地址**: http://localhost:8080（需要单独启动）

## 完整启动流程

### 1. 启动后端服务

```bash
cd /Users/liuchang/Documents/my
./mvnw spring-boot:run
# 或
mvn spring-boot:run
```

后端将在 `http://localhost:8080` 启动

### 2. 启动前端服务

在新的终端窗口中：

```bash
cd /Users/liuchang/Documents/my
./start_frontend.sh
```

前端将在 `http://localhost:5173` 启动

### 3. 访问系统

1. 打开浏览器
2. 访问: http://localhost:5173/index.html
3. 使用默认账号登录：
   - 用户名: `admin`
   - 密码: `admin123`

## 其他启动方式

### 使用 Node.js http-server（如果已安装）

```bash
cd /Users/liuchang/Documents/my/frontend
npx http-server -p 5173
```

### 使用 PHP 内置服务器

```bash
cd /Users/liuchang/Documents/my/frontend
php -S localhost:5173
```

## 注意事项

1. **前后端分离**: 前端和后端需要分别启动
2. **端口配置**: 
   - 前端: 5173
   - 后端: 8080
3. **CORS 配置**: 后端已配置 CORS，允许前端跨域访问
4. **必须使用 HTTP 服务器**: 不能直接用 `file://` 协议打开 HTML 文件，因为：
   - 需要加载 CDN 资源
   - 需要发送跨域请求到后端 API
   - 浏览器的安全策略限制

## 停止服务

在运行前端服务的终端中按 `Ctrl+C` 即可停止

