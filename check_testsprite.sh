#!/bin/bash
# TestSprite MCP 配置诊断脚本

echo "=== TestSprite MCP 配置诊断 ==="
echo ""

# 1. 检查 Node.js 和 npm
echo "1. 检查 Node.js 环境:"
if command -v node &> /dev/null; then
    echo "   ✅ Node.js: $(node --version)"
else
    echo "   ❌ Node.js 未安装"
fi

if command -v npm &> /dev/null; then
    echo "   ✅ npm: $(npm --version)"
else
    echo "   ❌ npm 未安装"
fi

echo ""

# 2. 检查 TestSprite MCP 包
echo "2. 检查 TestSprite MCP 包:"
if npm list -g @testsprite/mcp-server &> /dev/null 2>&1; then
    echo "   ✅ @testsprite/mcp-server 已全局安装"
else
    echo "   ⚠️  @testsprite/mcp-server 未全局安装（可以使用 npx）"
fi

echo ""

# 3. 检查 Cursor 配置文件
echo "3. 检查 Cursor 配置:"
CURSOR_SETTINGS="$HOME/Library/Application Support/Cursor/User/settings.json"
if [ -f "$CURSOR_SETTINGS" ]; then
    echo "   ✅ 配置文件存在: $CURSOR_SETTINGS"
    if grep -q "testsprit\|TestSprite" "$CURSOR_SETTINGS" 2>/dev/null; then
        echo "   ✅ 找到 TestSprite 配置"
        grep -A 5 "testsprit\|TestSprite" "$CURSOR_SETTINGS" | head -10
    else
        echo "   ⚠️  配置文件中未找到 TestSprite 配置"
    fi
else
    echo "   ⚠️  配置文件不存在"
fi

echo ""

# 4. 检查 MCP 服务器状态
echo "4. 检查 MCP 服务器状态:"
MCP_DIR="$HOME/.cursor/projects/Users-liuchang-Documents-my/mcps/user-TestSprite"
if [ -d "$MCP_DIR" ]; then
    echo "   ✅ MCP 目录存在: $MCP_DIR"
    if [ -f "$MCP_DIR/STATUS.md" ]; then
        echo "   状态文件内容:"
        cat "$MCP_DIR/STATUS.md" | sed 's/^/   /'
    fi
else
    echo "   ⚠️  MCP 目录不存在"
fi

echo ""
echo "=== 诊断完成 ==="
echo ""
echo "下一步:"
echo "1. 访问 https://www.testsprite.com 获取 API Key"
echo "2. 在 Cursor Settings → MCP 中配置 TestSprite"
echo "3. 或编辑配置文件添加 MCP 配置（参考 TESTSPRITE_SETUP.md）"
echo "4. 重启 Cursor"




