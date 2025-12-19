# 安装 Node.js 以使用 TestSprite MCP

## 问题
TestSprite MCP 需要 `npx` 命令，但系统未安装 Node.js。

## 解决方案

### 方法 1：使用 Homebrew 安装（推荐，macOS）

```bash
# 1. 安装 Homebrew（如果还没有）
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# 2. 安装 Node.js（包含 npm 和 npx）
brew install node

# 3. 验证安装
node --version
npm --version
npx --version
```

### 方法 2：从官网下载安装包

1. 访问：https://nodejs.org/
2. 下载 LTS 版本（推荐）
3. 运行安装包
4. 重启终端或 Cursor

### 方法 3：使用 nvm（Node Version Manager）

```bash
# 1. 安装 nvm
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash

# 2. 重新加载 shell 配置
source ~/.zshrc  # 或 ~/.bash_profile

# 3. 安装 Node.js LTS
nvm install --lts

# 4. 使用 Node.js
nvm use --lts
```

## 安装后验证

```bash
# 检查版本
node --version   # 应该显示 v20.x.x 或更高
npm --version    # 应该显示 10.x.x 或更高
npx --version    # 应该显示 10.x.x 或更高
```

## 配置 TestSprite MCP

安装 Node.js 后：

1. **重启 Cursor**（完全退出并重新打开）
2. Cursor 会自动检测到 `npx` 并启动 TestSprite MCP
3. 检查 Cursor Settings → MCP，TestSprite 状态应该变为正常

## 如果仍有问题

如果安装 Node.js 后 TestSprite 仍无法启动：

1. 确认 API Key 已正确配置
2. 检查 Cursor Settings → MCP 中的 TestSprite 配置
3. 查看 Cursor 的日志文件（通常在 `~/Library/Application Support/Cursor/logs/`）

---

**注意**：安装 Node.js 后，需要重启 Cursor 才能让 MCP 服务器检测到 `npx`。



