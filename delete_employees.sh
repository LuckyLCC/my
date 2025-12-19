#!/bin/bash
# 删除员工管理中的数据（保留 admin 账号）

BASE_URL="http://localhost:8080"

echo "🔐 正在登录获取 Token..."
TOKEN=$(curl -s "$BASE_URL/api/auth/login" -X POST \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | \
  python3 -c "import sys, json; print(json.load(sys.stdin).get('data',{}).get('token',''))" 2>/dev/null)

if [ -z "$TOKEN" ]; then
  echo "❌ 登录失败，请检查后端服务是否运行"
  exit 1
fi

echo "✅ 登录成功"
echo ""
echo "📋 获取员工列表..."
EMPLOYEES=$(curl -s "$BASE_URL/api/employees" \
  -H "Authorization: Bearer $TOKEN" | \
  python3 -c "
import sys, json
data = json.load(sys.stdin)
if data.get('code') == 200:
    employees = data.get('data', [])
    for emp in employees:
        if emp.get('id') != 1:  # 排除 admin
            print(emp.get('id'))
" 2>/dev/null)

if [ -z "$EMPLOYEES" ]; then
  echo "✅ 没有需要删除的员工（只有 admin 账号）"
  exit 0
fi

echo "找到以下员工（将删除，保留 admin）："
for emp_id in $EMPLOYEES; do
  EMP_INFO=$(curl -s "$BASE_URL/api/employees/$emp_id" \
    -H "Authorization: Bearer $TOKEN" | \
    python3 -c "
import sys, json
data = json.load(sys.stdin)
if data.get('code') == 200:
    emp = data.get('data', {})
    print(f\"  - ID: {emp.get('id')}, 用户名: {emp.get('username')}, 姓名: {emp.get('name')}\")
" 2>/dev/null)
  echo "$EMP_INFO"
done

echo ""
read -p "确认删除这些员工吗？(y/N): " confirm
if [ "$confirm" != "y" ] && [ "$confirm" != "Y" ]; then
  echo "❌ 已取消"
  exit 0
fi

echo ""
echo "🗑️  正在删除员工..."
DELETED=0
FAILED=0

for emp_id in $EMPLOYEES; do
  RESULT=$(curl -s -X DELETE "$BASE_URL/api/employees/$emp_id" \
    -H "Authorization: Bearer $TOKEN" | \
    python3 -c "
import sys, json
data = json.load(sys.stdin)
if data.get('code') == 200:
    print('success')
else:
    print('failed: ' + data.get('message', '未知错误'))
" 2>/dev/null)
  
  if [ "$RESULT" = "success" ]; then
    echo "  ✅ 已删除员工 ID: $emp_id"
    DELETED=$((DELETED + 1))
  else
    echo "  ❌ 删除失败 ID: $emp_id - $RESULT"
    FAILED=$((FAILED + 1))
  fi
done

echo ""
echo "📊 删除完成："
echo "  ✅ 成功: $DELETED"
echo "  ❌ 失败: $FAILED"

