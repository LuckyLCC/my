#!/bin/bash
# Playwright 全链路测试启动脚本
# 自动启动前后端服务并运行端到端测试

set -e

# 颜色输出
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
BACKEND_PORT=8080
FRONTEND_PORT=5173
BACKEND_PID=""
FRONTEND_PID=""

# 清理函数
cleanup() {
    echo -e "\n${YELLOW}正在清理服务...${NC}"
    
    if [ ! -z "$BACKEND_PID" ]; then
        echo -e "${YELLOW}停止后端服务 (PID: $BACKEND_PID)${NC}"
        kill $BACKEND_PID 2>/dev/null || true
    fi
    
    if [ ! -z "$FRONTEND_PID" ]; then
        echo -e "${YELLOW}停止前端服务 (PID: $FRONTEND_PID)${NC}"
        kill $FRONTEND_PID 2>/dev/null || true
    fi
    
    # 清理可能残留的进程
    pkill -f "spring-boot:run" 2>/dev/null || true
    pkill -f "http.server $FRONTEND_PORT" 2>/dev/null || true
    
    echo -e "${GREEN}清理完成${NC}"
}

# 注册清理函数
trap cleanup EXIT INT TERM

# 检查端口是否被占用
check_port() {
    local port=$1
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1 ; then
        return 0  # 端口被占用
    else
        return 1  # 端口空闲
    fi
}

# 等待服务启动
wait_for_service() {
    local url=$1
    local service_name=$2
    local max_attempts=60
    local attempt=0
    
    echo -e "${YELLOW}等待 $service_name 启动...${NC}"
    
    while [ $attempt -lt $max_attempts ]; do
        if curl -s "$url" > /dev/null 2>&1; then
            echo -e "${GREEN}✓ $service_name 已启动${NC}"
            return 0
        fi
        attempt=$((attempt + 1))
        sleep 1
    done
    
    echo -e "${RED}✗ $service_name 启动超时${NC}"
    return 1
}

# 启动后端服务
start_backend() {
    echo -e "\n${BLUE}=== 启动后端服务 ===${NC}"
    
    # 检查端口是否已被占用
    if check_port $BACKEND_PORT; then
        echo -e "${YELLOW}⚠ 端口 $BACKEND_PORT 已被占用，假设后端服务已在运行${NC}"
        return 0
    fi
    
    echo -e "${YELLOW}启动 Spring Boot 应用...${NC}"
    cd "$PROJECT_DIR"
    
    # 启动后端（后台运行）
    nohup ./mvnw spring-boot:run > backend.log 2>&1 &
    BACKEND_PID=$!
    
    echo -e "${YELLOW}后端服务启动中 (PID: $BACKEND_PID)${NC}"
    echo -e "${YELLOW}日志文件: $PROJECT_DIR/backend.log${NC}"
    
    # 等待后端启动
    if wait_for_service "http://localhost:$BACKEND_PORT/api/auth/login" "后端服务"; then
        return 0
    else
        echo -e "${RED}后端服务启动失败，请检查日志: $PROJECT_DIR/backend.log${NC}"
        return 1
    fi
}

# 启动前端服务
start_frontend() {
    echo -e "\n${BLUE}=== 启动前端服务 ===${NC}"
    
    # 检查端口是否已被占用
    if check_port $FRONTEND_PORT; then
        echo -e "${YELLOW}⚠ 端口 $FRONTEND_PORT 已被占用，假设前端服务已在运行${NC}"
        return 0
    fi
    
    echo -e "${YELLOW}启动前端 HTTP 服务器...${NC}"
    cd "$PROJECT_DIR/frontend"
    
    # 启动前端（后台运行）
    nohup python3 -m http.server $FRONTEND_PORT > ../frontend.log 2>&1 &
    FRONTEND_PID=$!
    
    echo -e "${YELLOW}前端服务启动中 (PID: $FRONTEND_PID)${NC}"
    echo -e "${YELLOW}日志文件: $PROJECT_DIR/frontend.log${NC}"
    
    # 等待前端启动
    if wait_for_service "http://localhost:$FRONTEND_PORT/index.html" "前端服务"; then
        return 0
    else
        echo -e "${RED}前端服务启动失败，请检查日志: $PROJECT_DIR/frontend.log${NC}"
        return 1
    fi
}

# 检查 Node.js 和 npm
check_node() {
    if ! command -v node &> /dev/null; then
        echo -e "${RED}✗ 未找到 Node.js，请先安装 Node.js${NC}"
        echo -e "${YELLOW}参考: INSTALL_NODEJS.md${NC}"
        exit 1
    fi
    
    if ! command -v npm &> /dev/null; then
        echo -e "${RED}✗ 未找到 npm，请先安装 npm${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}✓ Node.js 版本: $(node --version)${NC}"
    echo -e "${GREEN}✓ npm 版本: $(npm --version)${NC}"
}

# 安装依赖
install_dependencies() {
    echo -e "\n${BLUE}=== 检查依赖 ===${NC}"
    
    if [ ! -d "node_modules" ]; then
        echo -e "${YELLOW}安装 npm 依赖...${NC}"
        npm install
    else
        echo -e "${GREEN}✓ 依赖已安装${NC}"
    fi
    
    # 检查 Playwright 浏览器是否已安装
    if [ ! -d "node_modules/@playwright/test" ]; then
        echo -e "${YELLOW}安装 Playwright 浏览器...${NC}"
        npx playwright install chromium
    else
        echo -e "${GREEN}✓ Playwright 已安装${NC}"
    fi
}

# 运行测试
run_tests() {
    echo -e "\n${BLUE}=== 运行 Playwright 测试 ===${NC}"
    echo -e "${YELLOW}后端地址: http://localhost:$BACKEND_PORT${NC}"
    echo -e "${YELLOW}前端地址: http://localhost:$FRONTEND_PORT${NC}"
    echo ""
    
    cd "$PROJECT_DIR"
    
    # 运行测试
    npx playwright test "$@"
    
    TEST_EXIT_CODE=$?
    
    if [ $TEST_EXIT_CODE -eq 0 ]; then
        echo -e "\n${GREEN}✓ 所有测试通过！${NC}"
    else
        echo -e "\n${RED}✗ 部分测试失败${NC}"
    fi
    
    return $TEST_EXIT_CODE
}

# 主函数
main() {
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}  Playwright 全链路测试${NC}"
    echo -e "${BLUE}========================================${NC}"
    echo ""
    
    # 检查 Node.js
    check_node
    
    # 安装依赖
    install_dependencies
    
    # 启动后端服务
    if ! start_backend; then
        exit 1
    fi
    
    # 启动前端服务
    if ! start_frontend; then
        exit 1
    fi
    
    # 等待服务完全就绪
    echo -e "\n${YELLOW}等待服务完全就绪...${NC}"
    sleep 3
    
    # 运行测试
    run_tests "$@"
    
    # 清理函数会自动执行
}

# 运行主函数
main "$@"

