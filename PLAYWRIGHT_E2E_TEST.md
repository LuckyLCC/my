# Playwright 全链路端到端测试指南

## 概述

本项目使用 Playwright 进行真正的前后端"全链路"联调测试，覆盖从用户登录到业务操作的完整流程。

## 测试架构

```
┌─────────────────────────────────────────────────┐
│         Playwright 全链路测试流程                │
├─────────────────────────────────────────────────┤
│  1. 自动启动后端服务 (Spring Boot)              │
│     └─ 端口: 8080                               │
│                                                  │
│  2. 自动启动前端服务 (HTTP Server)              │
│     └─ 端口: 5173                               │
│                                                  │
│  3. 执行端到端测试                              │
│     ├─ 认证测试 (auth.spec.js)                  │
│     ├─ 会员管理测试 (member.spec.js)            │
│     ├─ 卡种管理测试 (card-type.spec.js)         │
│     └─ 完整业务流程测试 (full-flow.spec.js)    │
│                                                  │
│  4. 生成测试报告                                │
│     └─ HTML 报告 + JSON 结果                    │
└─────────────────────────────────────────────────┘
```

## 前置条件

### 1. 安装 Node.js

确保已安装 Node.js (推荐 v16+):

```bash
node --version
npm --version
```

如果未安装，参考 `INSTALL_NODEJS.md`。

### 2. 安装 Python

前端服务需要 Python 3:

```bash
python3 --version
```

### 3. 数据库配置

确保 MySQL 数据库已配置并运行，配置在 `application.properties` 中。

## 快速开始

### 方法 1: 使用自动化脚本（推荐）

```bash
cd /Users/liuchang/Documents/my
./run-e2e-tests.sh
```

这个脚本会：
1. 自动检查并安装依赖
2. 自动启动后端服务
3. 自动启动前端服务
4. 运行所有测试
5. 测试完成后自动清理服务

### 方法 2: 手动启动服务

#### 步骤 1: 安装依赖

```bash
cd /Users/liuchang/Documents/my
npm install
npx playwright install chromium
```

#### 步骤 2: 启动后端服务

在一个终端窗口：

```bash
cd /Users/liuchang/Documents/my
./mvnw spring-boot:run
```

等待后端启动完成（看到 "Started MyApplication" 日志）。

#### 步骤 3: 启动前端服务

在另一个终端窗口：

```bash
cd /Users/liuchang/Documents/my/frontend
python3 -m http.server 5173
```

#### 步骤 4: 运行测试

在第三个终端窗口：

```bash
cd /Users/liuchang/Documents/my
npx playwright test
```

## 测试命令

### 运行所有测试

```bash
npx playwright test
```

### 运行特定测试文件

```bash
# 只运行认证测试
npx playwright test e2e/auth.spec.js

# 只运行会员管理测试
npx playwright test e2e/member.spec.js

# 只运行完整流程测试
npx playwright test e2e/full-flow.spec.js
```

### 运行特定测试用例

```bash
# 使用测试名称过滤
npx playwright test -g "应该能够成功登录"
```

### 以 UI 模式运行（调试）

```bash
npx playwright test --ui
```

### 以有头模式运行（查看浏览器）

```bash
npx playwright test --headed
```

### 调试模式

```bash
npx playwright test --debug
```

### 查看测试报告

```bash
npx playwright show-report
```

## 测试用例

### 认证模块 (auth.spec.js)

- ✅ 应该能够成功登录
- ✅ 登录失败应该显示错误信息
- ✅ 登录后应该能够访问受保护的页面

### 会员管理模块 (member.spec.js)

- ✅ 应该能够查看会员列表
- ✅ 应该能够创建新会员
- ✅ 应该能够搜索会员
- ✅ 应该能够查看会员详情

### 卡种管理模块 (card-type.spec.js)

- ✅ 应该能够查看卡种列表
- ✅ 应该能够看到卡种的基本信息

### 完整业务流程 (full-flow.spec.js)

- ✅ 完整流程：登录 -> 查看会员列表 -> 创建会员 -> 查看详情
- ✅ 完整流程：登录 -> 查看卡种 -> 创建会员 -> 会员续费
- ✅ 完整流程：登录 -> 会员签到

## 测试配置

### Playwright 配置 (playwright.config.js)

- **测试目录**: `./e2e`
- **基础 URL**: `http://localhost:5173`
- **浏览器**: Chromium (可扩展)
- **超时设置**:
  - 操作超时: 10秒
  - 导航超时: 30秒
- **报告**: HTML + JSON

### 测试工具函数 (e2e/fixtures.js)

提供以下辅助函数：

- `login(page, username, password)`: 登录辅助函数
- `waitForAPI(page)`: 等待 API 请求完成
- `waitForElement(page, selector)`: 等待元素可见
- `waitForAPIResponse(page, urlPattern)`: 等待特定 API 响应

## 测试数据

### 默认测试用户

- **用户名**: `admin`
- **密码**: `admin123`
- **角色**: ADMIN

可以在 `e2e/fixtures.js` 中修改测试用户凭据。

## 测试报告

测试完成后，会生成以下报告：

1. **HTML 报告**: `playwright-report/index.html`
   - 可视化测试结果
   - 截图和视频（失败时）
   - 执行时间统计

2. **JSON 结果**: `playwright-report/results.json`
   - 机器可读的测试结果
   - 可用于 CI/CD 集成

查看报告：

```bash
npx playwright show-report
```

## 故障排查

### 问题 1: 后端服务启动失败

**症状**: 测试脚本报错 "后端服务启动超时"

**解决方案**:
1. 检查端口 8080 是否被占用: `lsof -i :8080`
2. 检查数据库连接配置
3. 查看后端日志: `cat backend.log`
4. 手动启动后端: `./mvnw spring-boot:run`

### 问题 2: 前端服务启动失败

**症状**: 测试脚本报错 "前端服务启动超时"

**解决方案**:
1. 检查端口 5173 是否被占用: `lsof -i :5173`
2. 检查 Python 3 是否安装: `python3 --version`
3. 查看前端日志: `cat frontend.log`
4. 手动启动前端: `cd frontend && python3 -m http.server 5173`

### 问题 3: 测试失败 - 找不到元素

**症状**: 测试报错 "Element not found"

**解决方案**:
1. 使用 `--headed` 模式运行，观察浏览器行为
2. 使用 `--debug` 模式，逐步调试
3. 检查前端 UI 结构是否变化
4. 增加等待时间或使用更通用的选择器

### 问题 4: 登录失败

**症状**: 登录测试失败

**解决方案**:
1. 检查默认管理员账号是否存在
2. 检查后端 API 是否正常: `curl http://localhost:8080/api/auth/login`
3. 检查 CORS 配置
4. 查看浏览器控制台错误信息

### 问题 5: Playwright 浏览器未安装

**症状**: 报错 "Executable doesn't exist"

**解决方案**:

```bash
npx playwright install chromium
```

## CI/CD 集成

### GitHub Actions 示例

```yaml
name: E2E Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-node@v3
        with:
          node-version: '18'
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
      - name: Install dependencies
        run: |
          npm install
          npx playwright install --with-deps chromium
      - name: Start services and run tests
        run: ./run-e2e-tests.sh
      - name: Upload test results
        if: always()
        uses: actions/upload-artifact@v3
        with:
          name: playwright-report
          path: playwright-report/
```

## 最佳实践

1. **测试隔离**: 每个测试用例应该是独立的，不依赖其他测试的执行顺序

2. **等待策略**: 使用 `waitForAPI` 和 `waitForElement` 而不是固定的 `sleep`

3. **选择器**: 优先使用稳定的选择器（如 `data-test` 属性），避免使用易变的 CSS 类名

4. **错误处理**: 测试应该能够优雅地处理 UI 结构变化（使用条件检查）

5. **数据清理**: 测试创建的数据应该能够自动清理或使用唯一标识符

6. **并行执行**: 默认配置支持并行执行，但要注意测试之间的数据冲突

## 扩展测试

### 添加新的测试用例

1. 在 `e2e/` 目录下创建新的测试文件，例如 `e2e/financial.spec.js`
2. 使用 `test.describe` 组织测试套件
3. 使用 `test` 定义测试用例
4. 使用 `fixtures.js` 中的辅助函数

示例：

```javascript
import { test, expect } from '@playwright/test';
import { login, waitForAPI } from './fixtures.js';

test.describe('财务统计模块', () => {
  test.beforeEach(async ({ page }) => {
    await login(page);
    await page.goto('/index.html');
    await waitForAPI(page);
  });

  test('应该能够查看月度提成统计', async ({ page }) => {
    // 测试代码
  });
});
```

## 相关文档

- [Playwright 官方文档](https://playwright.dev/)
- [项目需求文档](./需求文档.md)
- [API 文档](./API_DOCUMENTATION.md)
- [全栈测试计划](./FULL_STACK_TEST_PLAN.md)

## 支持

如有问题，请查看：
1. 测试日志文件: `backend.log`, `frontend.log`
2. Playwright 报告: `playwright-report/index.html`
3. 浏览器控制台错误信息

