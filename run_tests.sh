#!/bin/bash

# 全栈自动化测试执行脚本
# 用于运行前后端自动化测试

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=====================================${NC}"
echo -e "${BLUE}健身房会员管理系统 - 全栈自动化测试${NC}"
echo -e "${BLUE}=====================================${NC}"

# 检查Node.js是否安装
if ! command -v node &> /dev/null; then
    echo -e "${RED}错误: 未找到Node.js，请先安装Node.js${NC}"
    exit 1
fi

# 检查Java是否安装
if ! command -v java &> /dev/null; then
    echo -e "${RED}错误: 未找到Java，请先安装Java${NC}"
    exit 1
fi

# 检查Maven是否安装
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}错误: 未找到Maven，请先安装Maven${NC}"
    exit 1
fi

echo -e "\n${BLUE}1. 运行后端单元测试${NC}"

# 运行后端单元测试
echo -e "${YELLOW}执行后端单元测试...${NC}"
cd /Users/liuchang/Documents/my
mvn test -q

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ 后端单元测试完成${NC}"
else
    echo -e "${RED}✗ 后端单元测试失败${NC}"
    exit 1
fi

echo -e "\n${BLUE}2. 运行前端自动化测试${NC}"

# 运行前端自动化测试
echo -e "${YELLOW}执行前端自动化测试...${NC}"
npm test

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ 前端自动化测试完成${NC}"
else
    echo -e "${RED}✗ 前端自动化测试失败${NC}"
    exit 1
fi

echo -e "\n${BLUE}3. 运行端到端集成测试${NC}"

# 运行端到端测试脚本
echo -e "${YELLOW}执行端到端集成测试...${NC}"
./test_fullstack.sh

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ 端到端集成测试完成${NC}"
else
    echo -e "${RED}✗ 端到端集成测试失败${NC}"
    exit 1
fi

echo -e "\n${GREEN}=====================================${NC}"
echo -e "${GREEN}🎉 所有测试执行完成！${NC}"
echo -e "${GREEN}=====================================${NC}"

# 显示测试报告摘要
echo -e "\n${BLUE}测试报告摘要:${NC}"

# 统计后端测试结果
backend_test_count=$(find target/surefire-reports -name "*.txt" -exec grep -l "Tests run:" {} \; 2>/dev/null | wc -l)
if [ $backend_test_count -gt 0 ]; then
    backend_results=$(find target/surefire-reports -name "*.txt" -exec grep "Tests run:" {} \; 2>/dev/null | tail -1)
    echo "后端测试结果: $backend_results"
else
    echo "后端测试结果: 未找到测试报告"
fi

# 前端测试结果在执行时已显示

echo -e "\n${YELLOW}提示:${NC}"
echo "1. 后端测试报告位置: target/surefire-reports/"
echo "2. 前端测试结果已显示在控制台"
echo "3. 完整的端到端测试报告: test_fullstack.sh 输出"