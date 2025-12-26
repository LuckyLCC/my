#!/bin/bash
# 测试 staff 账号权限功能

BASE_URL="http://localhost:8080"

echo "=========================================="
echo "🧪 Staff 账号权限测试"
echo "=========================================="
echo ""

# 1. 使用 admin 账号登录，创建 staff 账号
echo "📝 步骤 1: 使用 admin 登录并创建 staff 账号..."
ADMIN_TOKEN=$(curl -s "$BASE_URL/api/auth/login" -X POST \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | \
  python3 -c "import sys, json; print(json.load(sys.stdin).get('data',{}).get('token',''))" 2>/dev/null)

if [ -z "$ADMIN_TOKEN" ]; then
  echo "❌ Admin 登录失败，请检查后端服务是否运行"
  exit 1
fi

echo "✅ Admin 登录成功"
echo ""

# 创建 staff 账号（只有创建和查看权限，没有编辑和删除权限）
echo "📝 创建 staff 账号（username: staff01, 权限: 创建✓ 查看✓ 编辑✗ 删除✗）..."
CREATE_RESULT=$(curl -s -X POST "$BASE_URL/api/employees" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "staff01",
    "password": "staff123",
    "name": "测试员工",
    "phone": "13800000001",
    "role": "STAFF",
    "status": 1,
    "memberCreate": 1,
    "memberRead": 1,
    "memberUpdate": 0,
    "memberDelete": 0
  }' | python3 -c "
import sys, json
data = json.load(sys.stdin)
if data.get('code') == 200:
    emp = data.get('data', {})
    print(f\"success|{emp.get('id')}|{emp.get('username')}\")
else:
    print(f\"failed|{data.get('message', '未知错误')}\")
" 2>/dev/null)

IFS='|' read -r STATUS STAFF_ID STAFF_USERNAME <<< "$CREATE_RESULT"

if [ "$STATUS" != "success" ]; then
  if [[ "$CREATE_RESULT" == *"用户名已存在"* ]]; then
    echo "⚠️  Staff 账号已存在，继续使用现有账号..."
    # 获取现有 staff 账号信息
    STAFF_ID=$(curl -s "$BASE_URL/api/employees" \
      -H "Authorization: Bearer $ADMIN_TOKEN" | \
      python3 -c "
import sys, json
data = json.load(sys.stdin)
if data.get('code') == 200:
    employees = data.get('data', [])
    for emp in employees:
        if emp.get('username') == 'staff01':
            print(emp.get('id'))
            break
" 2>/dev/null)
  else
    echo "❌ 创建 staff 账号失败: $CREATE_RESULT"
    exit 1
  fi
else
  echo "✅ Staff 账号创建成功 (ID: $STAFF_ID, Username: $STAFF_USERNAME)"
fi

echo ""

# 2. 使用 staff 账号登录
echo "📝 步骤 2: 使用 staff 账号登录..."
STAFF_TOKEN=$(curl -s "$BASE_URL/api/auth/login" -X POST \
  -H "Content-Type: application/json" \
  -d '{"username":"staff01","password":"staff123"}' | \
  python3 -c "import sys, json; print(json.load(sys.stdin).get('data',{}).get('token',''))" 2>/dev/null)

if [ -z "$STAFF_TOKEN" ]; then
  echo "❌ Staff 登录失败"
  exit 1
fi

echo "✅ Staff 登录成功"
echo ""

# 3. 获取 staff 账号的权限信息
echo "📝 步骤 3: 获取 staff 账号权限信息..."
STAFF_INFO=$(curl -s "$BASE_URL/api/auth/me" \
  -H "Authorization: Bearer $STAFF_TOKEN" | \
  python3 -c "
import sys, json
data = json.load(sys.stdin)
if data.get('code') == 200:
    user = data.get('data', {})
    print(f\"username|{user.get('username', '')}\")
    print(f\"name|{user.get('name', '')}\")
    print(f\"role|{user.get('role', '')}\")
    print(f\"memberCreate|{user.get('memberCreate', False)}\")
    print(f\"memberRead|{user.get('memberRead', False)}\")
    print(f\"memberUpdate|{user.get('memberUpdate', False)}\")
    print(f\"memberDelete|{user.get('memberDelete', False)}\")
" 2>/dev/null)

echo "📊 Staff 账号信息:"
echo "$STAFF_INFO" | while IFS='|' read -r key value; do
  if [ "$key" = "memberCreate" ] || [ "$key" = "memberRead" ] || [ "$key" = "memberUpdate" ] || [ "$key" = "memberDelete" ]; then
    if [ "$value" = "True" ] || [ "$value" = "1" ] || [ "$value" = "true" ]; then
      echo "  ✅ $key: 有权限"
    else
      echo "  ❌ $key: 无权限"
    fi
  else
    echo "  $key: $value"
  fi
done

echo ""

# 4. 测试权限：尝试查看会员列表（应该有权限）
echo "📝 步骤 4: 测试查看会员列表权限（应该有权限）..."
MEMBER_LIST_RESULT=$(curl -s -X GET "$BASE_URL/api/members" \
  -H "Authorization: Bearer $STAFF_TOKEN" | \
  python3 -c "
import sys, json
data = json.load(sys.stdin)
if data.get('code') == 200:
    print('success')
else:
    print(f\"failed|{data.get('message', '未知错误')}\")
" 2>/dev/null)

if [[ "$MEMBER_LIST_RESULT" == "success" ]]; then
  echo "✅ 查看会员列表：成功（权限正常）"
else
  echo "❌ 查看会员列表：失败 - $MEMBER_LIST_RESULT"
fi

echo ""

# 5. 测试权限：尝试更新会员（应该无权限）
echo "📝 步骤 5: 测试更新会员权限（应该无权限）..."
# 先获取一个会员ID
MEMBER_ID=$(curl -s -X GET "$BASE_URL/api/members" \
  -H "Authorization: Bearer $STAFF_TOKEN" | \
  python3 -c "
import sys, json
data = json.load(sys.stdin)
if data.get('code') == 200:
    members = data.get('data', [])
    if members and len(members) > 0:
        print(members[0].get('id'))
    else:
        print('no_members')
else:
    print('error')
" 2>/dev/null)

if [ "$MEMBER_ID" != "no_members" ] && [ "$MEMBER_ID" != "error" ] && [ -n "$MEMBER_ID" ]; then
  UPDATE_RESULT=$(curl -s -X PUT "$BASE_URL/api/members/$MEMBER_ID" \
    -H "Authorization: Bearer $STAFF_TOKEN" \
    -H "Content-Type: application/json" \
    -d '{"name":"测试更新"}' | \
    python3 -c "
import sys, json
data = json.load(sys.stdin)
if data.get('code') == 200:
    print('success')
else:
    print(f\"failed|{data.get('message', '未知错误')}\")
" 2>/dev/null)

  if [[ "$UPDATE_RESULT" == *"权限不足"* ]] || [[ "$UPDATE_RESULT" == *"无会员更新权限"* ]]; then
    echo "✅ 更新会员：正确拒绝（权限控制正常）"
  elif [[ "$UPDATE_RESULT" == "success" ]]; then
    echo "❌ 更新会员：不应该成功（权限控制失效）"
  else
    echo "⚠️  更新会员：$UPDATE_RESULT"
  fi
else
  echo "⚠️  无法测试更新权限（没有会员数据）"
fi

echo ""

# 6. 测试权限：尝试删除会员（应该无权限）
echo "📝 步骤 6: 测试删除会员权限（应该无权限）..."
if [ "$MEMBER_ID" != "no_members" ] && [ "$MEMBER_ID" != "error" ] && [ -n "$MEMBER_ID" ]; then
  DELETE_RESULT=$(curl -s -X DELETE "$BASE_URL/api/members/$MEMBER_ID" \
    -H "Authorization: Bearer $STAFF_TOKEN" | \
    python3 -c "
import sys, json
data = json.load(sys.stdin)
if data.get('code') == 200:
    print('success')
else:
    print(f\"failed|{data.get('message', '未知错误')}\")
" 2>/dev/null)

  if [[ "$DELETE_RESULT" == *"权限不足"* ]] || [[ "$DELETE_RESULT" == *"无会员删除权限"* ]]; then
    echo "✅ 删除会员：正确拒绝（权限控制正常）"
  elif [[ "$DELETE_RESULT" == "success" ]]; then
    echo "❌ 删除会员：不应该成功（权限控制失效）"
  else
    echo "⚠️  删除会员：$DELETE_RESULT"
  fi
else
  echo "⚠️  无法测试删除权限（没有会员数据）"
fi

echo ""

# 7. 测试权限：尝试创建会员（应该有权限）
echo "📝 步骤 7: 测试创建会员权限（应该有权限）..."
# 先获取卡种列表
CARD_TYPE_ID=$(curl -s -X GET "$BASE_URL/api/card-types" \
  -H "Authorization: Bearer $STAFF_TOKEN" | \
  python3 -c "
import sys, json
data = json.load(sys.stdin)
if data.get('code') == 200:
    cardTypes = data.get('data', [])
    if cardTypes and len(cardTypes) > 0:
        print(cardTypes[0].get('id'))
    else:
        print('no_card_types')
else:
    print('error')
" 2>/dev/null)

if [ "$CARD_TYPE_ID" != "no_card_types" ] && [ "$CARD_TYPE_ID" != "error" ] && [ -n "$CARD_TYPE_ID" ]; then
  CREATE_MEMBER_RESULT=$(curl -s -X POST "$BASE_URL/api/members" \
    -H "Authorization: Bearer $STAFF_TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
      \"name\": \"测试会员_$(date +%s)\",
      \"gender\": \"男\",
      \"phone\": \"138$(date +%s | tail -c 8)\",
      \"idCard\": \"110101199001011234\",
      \"cardTypeId\": $CARD_TYPE_ID,
      \"startDate\": \"$(date +%Y-%m-%d)\",
      \"remainingTimes\": null
    }" | \
    python3 -c "
import sys, json
data = json.load(sys.stdin)
if data.get('code') == 200:
    print('success')
else:
    print(f\"failed|{data.get('message', '未知错误')}\")
" 2>/dev/null)

  if [[ "$CREATE_MEMBER_RESULT" == "success" ]]; then
    echo "✅ 创建会员：成功（权限正常）"
  elif [[ "$CREATE_MEMBER_RESULT" == *"权限不足"* ]] || [[ "$CREATE_MEMBER_RESULT" == *"无会员创建权限"* ]]; then
    echo "❌ 创建会员：不应该被拒绝（权限控制异常）"
  else
    echo "⚠️  创建会员：$CREATE_MEMBER_RESULT"
  fi
else
  echo "⚠️  无法测试创建权限（没有卡种数据）"
fi

echo ""
echo "=========================================="
echo "✅ 权限测试完成"
echo "=========================================="
echo ""
echo "📝 测试账号信息:"
echo "  用户名: staff01"
echo "  密码: staff123"
echo "  角色: STAFF"
echo "  权限: 创建✓ 查看✓ 编辑✗ 删除✗"
echo ""
echo "💡 提示: 可以在前端使用此账号登录，验证会员管理页面是否正确隐藏了编辑和删除按钮"

