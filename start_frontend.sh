#!/bin/bash
# 启动前端服务脚本
# 使用方法: ./start_frontend.sh

FRONTEND_DIR="$(cd "$(dirname "$0")/frontend" && pwd)"
PORT=5173

echo "🚀 启动前端服务..."
echo "📁 前端目录: $FRONTEND_DIR"
echo "🌐 访问地址: http://localhost:$PORT/index.html"
echo ""
echo "按 Ctrl+C 停止服务"
echo ""

cd "$FRONTEND_DIR"
python3 -m http.server $PORT

