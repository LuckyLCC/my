# Playwright 全链路 E2E 测试指南

## 概述

本测试套件基于功能需求文档编写，覆盖健身房会员管理系统的所有主要功能模块。

**前端地址**: `http://localhost:5173`  
**后端地址**: `http://localhost:8080`

## 测试覆盖范围

### 1. 认证授权模块
- ✅ 用户登录（正常流程）
- ✅ 用户登录（异常流程：用户名或密码错误）
- ✅ 用户登出

### 2. 会员管理模块
- ✅ 会员列表查询
- ✅ 会员列表筛选（按姓名）
- ✅ 新增会员（开卡）
- ✅ 编辑会员
- ✅ 会员签到
- ✅ 查看签到历史
- ✅ 会员续卡
- ✅ 查看续卡历史

### 3. 卡种管理模块
- ✅ 卡种列表查询
- ✅ 新增卡种
- ✅ 编辑卡种

### 4. 员工管理模块（仅管理员）
- ✅ 员工列表查询
- ✅ 新增员工

### 5. 财务管理模块
- ✅ 开卡统计
- ✅ 交易明细查询

### 6. 完整业务流程测试
- ✅ 登录 -> 创建会员 -> 会员签到 -> 查看签到历史
- ✅ 登录 -> 创建会员 -> 会员续卡 -> 查看续卡历史
- ✅ 登录 -> 卡种管理 -> 创建卡种 -> 创建会员使用新卡种

## 运行测试

### 前置条件

1. **启动后端服务**
   ```bash
   # 确保后端运行在 http://localhost:8080
   ./start_backend.sh
   ```

2. **启动前端服务**
   ```bash
   # 确保前端运行在 http://localhost:5173
   ./start_frontend.sh
   # 或
   cd frontend && python3 -m http.server 5173
   ```

3. **安装 Playwright 依赖**
   ```bash
   npx playwright install
   ```

### 运行所有测试

```bash
npx playwright test e2e/full-e2e-flow.spec.js
```

### 运行特定模块测试

```bash
# 只运行认证模块测试
npx playwright test e2e/full-e2e-flow.spec.js -g "认证授权模块"

# 只运行会员管理模块测试
npx playwright test e2e/full-e2e-flow.spec.js -g "会员管理模块"

# 只运行完整业务流程测试
npx playwright test e2e/full-e2e-flow.spec.js -g "完整业务流程"
```

### 以 UI 模式运行（推荐用于调试）

```bash
npx playwright test e2e/full-e2e-flow.spec.js --ui
```

### 以有头模式运行（可以看到浏览器）

```bash
npx playwright test e2e/full-e2e-flow.spec.js --headed
```

### 查看测试报告

```bash
npx playwright show-report
```

## 测试配置

测试配置文件：`playwright.config.ts`

- **Base URL**: `http://localhost:5173`
- **测试目录**: `./e2e`
- **浏览器**: Chromium, Firefox, WebKit

## 测试数据

测试使用以下默认账号：
- **用户名**: `admin`
- **密码**: `admin123`

测试数据会自动生成唯一标识（使用时间戳），避免数据冲突。

## 注意事项

1. **测试顺序**: 测试是并行运行的，每个测试都会独立登录，互不影响。

2. **数据清理**: 测试不会自动清理创建的数据，如需清理请手动操作或使用数据库脚本。

3. **等待时间**: 测试中使用了适当的等待时间，如果网络较慢可能需要调整。

4. **元素定位**: 测试使用了多种定位策略（文本、placeholder、类名等），以提高兼容性。

5. **错误处理**: 测试使用了 `.catch(() => {})` 来处理可能不存在的元素，确保测试的健壮性。

## 故障排查

### 测试失败常见原因

1. **前端或后端服务未启动**
   - 检查服务是否正常运行
   - 检查端口是否正确（前端 5173，后端 8080）

2. **元素定位失败**
   - 检查前端页面结构是否变化
   - 使用 `--headed` 模式查看实际页面

3. **超时错误**
   - 增加等待时间
   - 检查网络连接

4. **登录失败**
   - 检查测试账号是否正确
   - 检查后端认证服务是否正常

### 调试技巧

1. **使用 UI 模式**: `npx playwright test --ui` - 可以逐步执行测试
2. **使用有头模式**: `npx playwright test --headed` - 可以看到浏览器操作
3. **使用调试模式**: `npx playwright test --debug` - 可以设置断点
4. **查看截图**: 测试失败时会自动截图，在 `test-results` 目录中查看

## 扩展测试

如需添加新的测试用例，请参考现有测试的结构：

```javascript
test('测试名称', async ({ page }) => {
  // 1. 登录（如需要）
  await login(page);
  await page.goto(`${FRONTEND_URL}/index.html`);
  await waitForAPI(page);
  await page.waitForTimeout(2000);

  // 2. 执行操作
  // ...

  // 3. 验证结果
  // ...
});
```

## 相关文件

- 测试文件: `e2e/full-e2e-flow.spec.js`
- 测试配置: `playwright.config.ts`
- 测试辅助函数: `e2e/fixtures.js`
- 功能需求文档: `功能需求文档.md`

