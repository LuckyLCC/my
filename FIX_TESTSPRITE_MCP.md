# 修复 TestSprite MCP 配置

## 问题
Cursor 启动 TestSprite MCP 时找不到 `npx`，报错：`spawn npx ENOENT`

## 原因
Cursor 启动时的 PATH 环境变量可能不包含 `/usr/local/bin/`，导致找不到 `npx`。

## 解决方案

### 方法 1：使用完整路径（推荐）

编辑 Cursor 配置文件：`~/Library/Application Support/Cursor/User/settings.json`

将配置中的 `"command": "npx"` 改为使用完整路径：

```json
{
  "window.commandCenter": true,
  "mcp.servers": {
    "user-TestSprite": {
      "command": "/usr/local/bin/npx",
      "args": [
        "-y",
        "@testsprite/testsprite-mcp@latest"
      ],
      "env": {
        "TESTSPRITE_API_KEY": "你的API_KEY"
      }
    }
  }
}
```

**注意**：包名应该是 `@testsprite/testsprite-mcp@latest`（根据错误日志）

### 方法 2：通过 Cursor UI 配置

1. 打开 Cursor Settings (`Cmd + ,`)
2. 搜索 "MCP" 或 "Model Context Protocol"
3. 找到 TestSprite 配置
4. 在 "Command" 字段填入：`/usr/local/bin/npx`
5. 在 "Args" 字段填入：`-y`, `@testsprite/testsprite-mcp@latest`
6. 在环境变量中添加：`TESTSPRITE_API_KEY` = 你的 API Key

### 方法 3：设置 PATH 环境变量

如果方法 1 不行，可以在配置中添加 PATH：

```json
{
  "mcp.servers": {
    "user-TestSprite": {
      "command": "npx",
      "args": ["-y", "@testsprite/testsprite-mcp@latest"],
      "env": {
        "TESTSPRITE_API_KEY": "你的API_KEY",
        "PATH": "/usr/local/bin:/opt/homebrew/bin:/usr/bin:/bin"
      }
    }
  }
}
```

## 验证修复

1. **保存配置文件**
2. **完全重启 Cursor**（退出并重新打开）
3. 打开 Cursor Settings → MCP
4. 检查 TestSprite 状态：
   - ✅ 绿色 = 正常
   - ❌ 红色 = 仍有问题

## 如果仍有问题

1. 检查 API Key 是否正确
2. 查看 Cursor 日志：`~/Library/Application Support/Cursor/logs/`
3. 尝试手动运行测试：
   ```bash
   /usr/local/bin/npx -y @testsprite/testsprite-mcp@latest
   ```

## 获取 TestSprite API Key

如果还没有 API Key：

1. 访问：https://www.testsprite.com
2. 登录/注册
3. 进入 SETTINGS > API Keys
4. 生成新 Key 并复制



