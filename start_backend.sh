#!/bin/bash
# 启动后端服务脚本
# 使用方法: ./start_backend.sh

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PORT=8080

echo "🚀 启动后端服务..."
echo "📁 项目目录: $SCRIPT_DIR"
echo "🌐 后端地址: http://localhost:$PORT"
echo ""
echo "按 Ctrl+C 停止服务"
echo ""

cd "$SCRIPT_DIR"

# 检查Java是否安装
if ! command -v java &> /dev/null; then
    echo "❌ 错误: 未找到Java，请先安装JDK 17+"
    exit 1
fi

# 检查Maven是否安装
if command -v mvn &> /dev/null; then
    echo "使用系统Maven..."
    mvn spring-boot:run
elif [ -f "./mvnw" ]; then
    echo "使用Maven Wrapper..."
    chmod +x ./mvnw
    ./mvnw spring-boot:run
else
    echo "❌ 错误: 未找到Maven，请安装Maven或使用Maven Wrapper"
    exit 1
fi

