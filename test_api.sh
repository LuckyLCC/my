#!/bin/bash
# API 测试脚本 - 健身房会员管理系统
# 使用方法: ./test_api.sh

BASE_URL="http://localhost:8080"
TOKEN=""

# 颜色输出
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 测试计数
PASSED=0
FAILED=0

# 打印测试结果
print_result() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✓ PASS${NC}: $2"
        ((PASSED++))
    else
        echo -e "${RED}✗ FAIL${NC}: $2"
        ((FAILED++))
    fi
}

# 测试登录
test_login() {
    echo -e "\n${YELLOW}=== 测试登录 ===${NC}"
    RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
        -H "Content-Type: application/json" \
        -d '{"username":"admin","password":"admin123"}')
    
    CODE=$(echo $RESPONSE | grep -o '"code":[0-9]*' | cut -d':' -f2)
    if [ "$CODE" = "200" ]; then
        TOKEN=$(echo $RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)
        print_result 0 "登录成功，获取 Token"
        echo "Token: ${TOKEN:0:20}..."
    else
        print_result 1 "登录失败"
        echo "Response: $RESPONSE"
        exit 1
    fi
}

# 测试查询会员列表
test_get_members() {
    echo -e "\n${YELLOW}=== 测试查询会员列表 ===${NC}"
    RESPONSE=$(curl -s -X GET "$BASE_URL/api/members" \
        -H "Authorization: Bearer $TOKEN")
    
    CODE=$(echo $RESPONSE | grep -o '"code":[0-9]*' | cut -d':' -f2)
    if [ "$CODE" = "200" ]; then
        print_result 0 "查询会员列表成功"
    else
        print_result 1 "查询会员列表失败"
        echo "Response: $RESPONSE"
    fi
}

# 测试查询卡种列表
test_get_card_types() {
    echo -e "\n${YELLOW}=== 测试查询卡种列表 ===${NC}"
    RESPONSE=$(curl -s -X GET "$BASE_URL/api/card-types" \
        -H "Authorization: Bearer $TOKEN")
    
    CODE=$(echo $RESPONSE | grep -o '"code":[0-9]*' | cut -d':' -f2)
    if [ "$CODE" = "200" ]; then
        print_result 0 "查询卡种列表成功"
        COUNT=$(echo $RESPONSE | grep -o '"id":[0-9]*' | wc -l | tr -d ' ')
        echo "  找到 $COUNT 个卡种"
    else
        print_result 1 "查询卡种列表失败"
        echo "Response: $RESPONSE"
    fi
}

# 测试新增会员
test_create_member() {
    echo -e "\n${YELLOW}=== 测试新增会员 ===${NC}"
    MEMBER_DATA='{
        "name": "测试会员'$(date +%s)'",
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
        print_result 0 "新增会员成功"
        MEMBER_ID=$(echo $RESPONSE | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
        echo "  会员ID: $MEMBER_ID"
        export TEST_MEMBER_ID=$MEMBER_ID
    else
        print_result 1 "新增会员失败"
        echo "Response: $RESPONSE"
    fi
}

# 测试查询会员详情
test_get_member_detail() {
    if [ -z "$TEST_MEMBER_ID" ]; then
        echo -e "\n${YELLOW}=== 跳过查询会员详情（未创建测试会员） ===${NC}"
        return
    fi
    
    echo -e "\n${YELLOW}=== 测试查询会员详情 ===${NC}"
    RESPONSE=$(curl -s -X GET "$BASE_URL/api/members/$TEST_MEMBER_ID" \
        -H "Authorization: Bearer $TOKEN")
    
    CODE=$(echo $RESPONSE | grep -o '"code":[0-9]*' | cut -d':' -f2)
    if [ "$CODE" = "200" ]; then
        print_result 0 "查询会员详情成功"
    else
        print_result 1 "查询会员详情失败"
        echo "Response: $RESPONSE"
    fi
}

# 测试更新会员
test_update_member() {
    if [ -z "$TEST_MEMBER_ID" ]; then
        echo -e "\n${YELLOW}=== 跳过更新会员（未创建测试会员） ===${NC}"
        return
    fi
    
    echo -e "\n${YELLOW}=== 测试更新会员 ===${NC}"
    UPDATE_DATA='{
        "name": "更新后的姓名",
        "phone": "13900139000"
    }'
    
    RESPONSE=$(curl -s -X PUT "$BASE_URL/api/members/$TEST_MEMBER_ID" \
        -H "Authorization: Bearer $TOKEN" \
        -H "Content-Type: application/json" \
        -d "$UPDATE_DATA")
    
    CODE=$(echo $RESPONSE | grep -o '"code":[0-9]*' | cut -d':' -f2)
    if [ "$CODE" = "200" ]; then
        print_result 0 "更新会员成功"
    else
        print_result 1 "更新会员失败"
        echo "Response: $RESPONSE"
    fi
}

# 测试权限（普通员工无法访问员工管理）
test_permission() {
    echo -e "\n${YELLOW}=== 测试权限控制 ===${NC}"
    # 注意：这里需要先创建一个普通员工账号并登录获取 token
    # 简化测试：直接测试未授权访问
    RESPONSE=$(curl -s -X GET "$BASE_URL/api/employees" \
        -H "Authorization: Bearer invalid_token")
    
    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X GET "$BASE_URL/api/employees" \
        -H "Authorization: Bearer invalid_token")
    
    if [ "$HTTP_CODE" = "401" ] || [ "$HTTP_CODE" = "403" ]; then
        print_result 0 "权限控制正常（返回 $HTTP_CODE）"
    else
        print_result 1 "权限控制异常（返回 $HTTP_CODE）"
    fi
}

# 测试登出
test_logout() {
    echo -e "\n${YELLOW}=== 测试登出 ===${NC}"
    RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/logout" \
        -H "Authorization: Bearer $TOKEN")
    
    CODE=$(echo $RESPONSE | grep -o '"code":[0-9]*' | cut -d':' -f2)
    if [ "$CODE" = "200" ]; then
        print_result 0 "登出成功"
    else
        print_result 1 "登出失败"
        echo "Response: $RESPONSE"
    fi
}

# 主函数
main() {
    echo -e "${YELLOW}========================================${NC}"
    echo -e "${YELLOW}  健身房会员管理系统 API 测试${NC}"
    echo -e "${YELLOW}========================================${NC}"
    echo "Base URL: $BASE_URL"
    echo ""
    
    # 检查后端是否运行
    if ! curl -s "$BASE_URL/api/auth/login" > /dev/null 2>&1; then
        echo -e "${RED}错误: 无法连接到后端服务 ($BASE_URL)${NC}"
        echo "请确保后端服务正在运行"
        exit 1
    fi
    
    # 执行测试
    test_login
    test_get_members
    test_get_card_types
    test_create_member
    test_get_member_detail
    test_update_member
    test_permission
    test_logout
    
    # 输出测试结果
    echo -e "\n${YELLOW}========================================${NC}"
    echo -e "${YELLOW}  测试结果汇总${NC}"
    echo -e "${YELLOW}========================================${NC}"
    echo -e "${GREEN}通过: $PASSED${NC}"
    echo -e "${RED}失败: $FAILED${NC}"
    echo -e "总计: $((PASSED + FAILED))"
    
    if [ $FAILED -eq 0 ]; then
        exit 0
    else
        exit 1
    fi
}

# 运行主函数
main

