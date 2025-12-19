#!/bin/bash
# 全栈测试脚本 - 健身房会员管理系统
# 测试后端 API + 前端 UI + 端到端流程
# 使用方法: ./test_fullstack.sh

BASE_URL="http://localhost:8080"
FRONTEND_URL="http://localhost:5173/index.html"
TOKEN=""

# 颜色输出
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 测试计数
BACKEND_PASSED=0
BACKEND_FAILED=0
FRONTEND_PASSED=0
FRONTEND_FAILED=0
E2E_PASSED=0
E2E_FAILED=0

# 打印测试结果
print_result() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✓ PASS${NC}: $2"
        return 0
    else
        echo -e "${RED}✗ FAIL${NC}: $2"
        return 1
    fi
}

# 检查服务是否运行
check_services() {
    echo -e "\n${BLUE}=== 检查服务状态 ===${NC}"
    
    # 检查后端
    if curl -s "$BASE_URL/api/auth/login" > /dev/null 2>&1; then
        echo -e "${GREEN}✓ 后端服务运行中${NC}: $BASE_URL"
    else
        echo -e "${RED}✗ 后端服务未运行${NC}: $BASE_URL"
        echo "  请先启动后端: ./mvnw spring-boot:run"
        exit 1
    fi
    
    # 检查前端
    if curl -s "$FRONTEND_URL" > /dev/null 2>&1; then
        echo -e "${GREEN}✓ 前端服务运行中${NC}: $FRONTEND_URL"
    else
        echo -e "${YELLOW}⚠ 前端服务未运行${NC}: $FRONTEND_URL"
        echo "  提示: 前端测试将跳过，请运行: cd frontend && python3 -m http.server 5173"
        FRONTEND_AVAILABLE=false
    fi
}

# ============================================
# 阶段 1: 后端 API 测试
# ============================================
test_backend() {
    echo -e "\n${BLUE}========================================${NC}"
    echo -e "${BLUE}  阶段 1: 后端 API 测试${NC}"
    echo -e "${BLUE}========================================${NC}"
    
    # 测试登录
    echo -e "\n${YELLOW}测试登录${NC}"
    RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
        -H "Content-Type: application/json" \
        -d '{"username":"admin","password":"admin123"}')
    
    CODE=$(echo $RESPONSE | grep -o '"code":[0-9]*' | cut -d':' -f2)
    if [ "$CODE" = "200" ]; then
        TOKEN=$(echo $RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)
        if print_result 0 "登录成功，获取 Token"; then
            ((BACKEND_PASSED++))
        else
            ((BACKEND_FAILED++))
        fi
    else
        if print_result 1 "登录失败"; then
            ((BACKEND_PASSED++))
        else
            ((BACKEND_FAILED++))
        fi
        echo "Response: $RESPONSE"
        return 1
    fi
    
    # 测试查询会员列表
    echo -e "\n${YELLOW}测试查询会员列表${NC}"
    RESPONSE=$(curl -s -X GET "$BASE_URL/api/members" \
        -H "Authorization: Bearer $TOKEN")
    CODE=$(echo $RESPONSE | grep -o '"code":[0-9]*' | cut -d':' -f2)
    if [ "$CODE" = "200" ]; then
        if print_result 0 "查询会员列表成功"; then
            ((BACKEND_PASSED++))
        else
            ((BACKEND_FAILED++))
        fi
    else
        if print_result 1 "查询会员列表失败"; then
            ((BACKEND_PASSED++))
        else
            ((BACKEND_FAILED++))
        fi
    fi
    
    # 测试查询卡种列表
    echo -e "\n${YELLOW}测试查询卡种列表${NC}"
    RESPONSE=$(curl -s -X GET "$BASE_URL/api/card-types" \
        -H "Authorization: Bearer $TOKEN")
    CODE=$(echo $RESPONSE | grep -o '"code":[0-9]*' | cut -d':' -f2)
    if [ "$CODE" = "200" ]; then
        COUNT=$(echo $RESPONSE | grep -o '"id":[0-9]*' | wc -l | tr -d ' ')
        if print_result 0 "查询卡种列表成功（找到 $COUNT 个卡种）"; then
            ((BACKEND_PASSED++))
        else
            ((BACKEND_FAILED++))
        fi
    else
        if print_result 1 "查询卡种列表失败"; then
            ((BACKEND_PASSED++))
        else
            ((BACKEND_FAILED++))
        fi
    fi
    
    # 测试新增会员
    echo -e "\n${YELLOW}测试新增会员${NC}"
    MEMBER_DATA='{
        "name": "全栈测试会员'$(date +%s)'",
        "gender": "男",
        "phone": "138'$(date +%s | tail -c 8)'",
        "idCard": "110101199001011234",
        "cardTypeId": 1,
        "startDate": "'$(date +%Y-%m-%d)'",
        "remainingTimes": 20,
        "employeeId": 1
    }'
    
    RESPONSE=$(curl -s -X POST "$BASE_URL/api/members" \
        -H "Authorization: Bearer $TOKEN" \
        -H "Content-Type: application/json" \
        -d "$MEMBER_DATA")
    
    CODE=$(echo $RESPONSE | grep -o '"code":[0-9]*' | cut -d':' -f2)
    if [ "$CODE" = "200" ]; then
        MEMBER_ID=$(echo $RESPONSE | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
        if print_result 0 "新增会员成功（ID: $MEMBER_ID）"; then
            ((BACKEND_PASSED++))
        else
            ((BACKEND_FAILED++))
        fi
        export TEST_MEMBER_ID=$MEMBER_ID
    else
        if print_result 1 "新增会员失败"; then
            ((BACKEND_PASSED++))
        else
            ((BACKEND_FAILED++))
        fi
        echo "Response: $RESPONSE"
    fi
    
    # 测试权限控制
    echo -e "\n${YELLOW}测试权限控制${NC}"
    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X GET "$BASE_URL/api/employees" \
        -H "Authorization: Bearer invalid_token")
    if [ "$HTTP_CODE" = "401" ] || [ "$HTTP_CODE" = "403" ]; then
        if print_result 0 "权限控制正常（返回 $HTTP_CODE）"; then
            ((BACKEND_PASSED++))
        else
            ((BACKEND_FAILED++))
        fi
    else
        if print_result 1 "权限控制异常（返回 $HTTP_CODE）"; then
            ((BACKEND_PASSED++))
        else
            ((BACKEND_FAILED++))
        fi
    fi
}

# ============================================
# 阶段 2: 前端 UI 测试（需要浏览器工具）
# ============================================
test_frontend() {
    echo -e "\n${BLUE}========================================${NC}"
    echo -e "${BLUE}  阶段 2: 前端 UI 测试${NC}"
    echo -e "${BLUE}========================================${NC}"
    
    if [ "$FRONTEND_AVAILABLE" = "false" ]; then
        echo -e "${YELLOW}⚠ 前端服务未运行，跳过前端测试${NC}"
        echo "  提示: 前端测试需要使用浏览器工具或手动执行"
        echo "  参考: FULL_STACK_TEST_PLAN.md 中的前端测试用例"
        return
    fi
    
    echo -e "${YELLOW}前端 UI 测试需要使用浏览器工具${NC}"
    echo "  可以使用以下方式："
    echo "  1. 使用 Cursor 浏览器 MCP 工具（如果已配置）"
    echo "  2. 使用 Playwright/Selenium（需要 Node.js）"
    echo "  3. 手动测试（参考 FULL_STACK_TEST_PLAN.md）"
    echo ""
    echo "  前端测试用例："
    echo "  - TC-UI-001: 前端页面加载"
    echo "  - TC-UI-002: 登录功能"
    echo "  - TC-UI-003: 会员管理 - 列表加载"
    echo "  - TC-UI-004: 会员管理 - 新增会员"
    echo "  - TC-UI-005: 会员管理 - 编辑会员"
    echo "  - TC-UI-006: 会员管理 - 删除会员"
    echo "  - TC-UI-007: 卡种管理 - 状态显示"
    echo "  - TC-UI-008: 卡种管理 - 编辑状态"
}

# ============================================
# 阶段 3: 端到端测试
# ============================================
test_e2e() {
    echo -e "\n${BLUE}========================================${NC}"
    echo -e "${BLUE}  阶段 3: 端到端测试 (E2E)${NC}"
    echo -e "${BLUE}========================================${NC}"
    
    # E2E-001: 验证前后端数据一致性
    echo -e "\n${YELLOW}E2E-001: 验证前后端数据一致性${NC}"
    if [ -n "$TEST_MEMBER_ID" ]; then
        # 后端查询会员详情
        RESPONSE=$(curl -s -X GET "$BASE_URL/api/members/$TEST_MEMBER_ID" \
            -H "Authorization: Bearer $TOKEN")
        CODE=$(echo $RESPONSE | grep -o '"code":[0-9]*' | cut -d':' -f2)
        if [ "$CODE" = "200" ]; then
            MEMBER_NAME=$(echo $RESPONSE | grep -o '"name":"[^"]*' | cut -d'"' -f4)
            if print_result 0 "后端数据验证成功（会员: $MEMBER_NAME）"; then
                ((E2E_PASSED++))
            else
                ((E2E_FAILED++))
            fi
        else
            if print_result 1 "后端数据验证失败"; then
                ((E2E_PASSED++))
            else
                ((E2E_FAILED++))
            fi
        fi
    else
        echo -e "${YELLOW}⚠ 跳过（未创建测试会员）${NC}"
    fi
    
    # E2E-002: 验证交易记录创建
    echo -e "\n${YELLOW}E2E-002: 验证交易记录创建${NC}"
    if [ -n "$TEST_MEMBER_ID" ]; then
        # 查询交易记录（通过财务接口）
        RESPONSE=$(curl -s -X GET "$BASE_URL/api/financial/transactions?memberId=$TEST_MEMBER_ID" \
            -H "Authorization: Bearer $TOKEN")
        CODE=$(echo $RESPONSE | grep -o '"code":[0-9]*' | cut -d':' -f2)
        if [ "$CODE" = "200" ]; then
            TRANSACTION_COUNT=$(echo $RESPONSE | grep -o '"id":[0-9]*' | wc -l | tr -d ' ')
            if [ "$TRANSACTION_COUNT" -gt 0 ]; then
                if print_result 0 "交易记录已创建（$TRANSACTION_COUNT 条）"; then
                    ((E2E_PASSED++))
                else
                    ((E2E_FAILED++))
                fi
            else
                if print_result 1 "未找到交易记录"; then
                    ((E2E_PASSED++))
                else
                    ((E2E_FAILED++))
                fi
            fi
        else
            if print_result 1 "查询交易记录失败"; then
                ((E2E_PASSED++))
            else
                ((E2E_FAILED++))
            fi
        fi
    else
        echo -e "${YELLOW}⚠ 跳过（未创建测试会员）${NC}"
    fi
}

# ============================================
# 生成测试报告
# ============================================
generate_report() {
    echo -e "\n${BLUE}========================================${NC}"
    echo -e "${BLUE}  测试结果汇总${NC}"
    echo -e "${BLUE}========================================${NC}"
    
    echo -e "\n${YELLOW}后端 API 测试:${NC}"
    echo -e "  ${GREEN}通过: $BACKEND_PASSED${NC}"
    echo -e "  ${RED}失败: $BACKEND_FAILED${NC}"
    echo -e "  总计: $((BACKEND_PASSED + BACKEND_FAILED))"
    
    echo -e "\n${YELLOW}前端 UI 测试:${NC}"
    if [ "$FRONTEND_AVAILABLE" = "false" ]; then
        echo -e "  ${YELLOW}跳过（前端服务未运行）${NC}"
    else
        echo -e "  ${YELLOW}需要浏览器工具或手动执行${NC}"
    fi
    
    echo -e "\n${YELLOW}端到端测试:${NC}"
    echo -e "  ${GREEN}通过: $E2E_PASSED${NC}"
    echo -e "  ${RED}失败: $E2E_FAILED${NC}"
    echo -e "  总计: $((E2E_PASSED + E2E_FAILED))"
    
    TOTAL_PASSED=$((BACKEND_PASSED + E2E_PASSED))
    TOTAL_FAILED=$((BACKEND_FAILED + E2E_FAILED))
    
    echo -e "\n${YELLOW}总计:${NC}"
    echo -e "  ${GREEN}通过: $TOTAL_PASSED${NC}"
    echo -e "  ${RED}失败: $TOTAL_FAILED${NC}"
    echo -e "  总计: $((TOTAL_PASSED + TOTAL_FAILED))"
    
    if [ $TOTAL_FAILED -eq 0 ]; then
        echo -e "\n${GREEN}✓ 所有测试通过！${NC}"
        exit 0
    else
        echo -e "\n${RED}✗ 部分测试失败${NC}"
        exit 1
    fi
}

# ============================================
# 主函数
# ============================================
main() {
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}  全栈测试 - 健身房会员管理系统${NC}"
    echo -e "${BLUE}========================================${NC}"
    echo "后端地址: $BASE_URL"
    echo "前端地址: $FRONTEND_URL"
    echo ""
    
    FRONTEND_AVAILABLE=true
    check_services
    
    test_backend
    test_frontend
    test_e2e
    generate_report
}

# 运行主函数
main



