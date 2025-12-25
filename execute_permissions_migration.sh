#!/bin/bash
# 执行员工权限字段迁移脚本

# 从application.properties读取数据库配置（如果存在）
# 如果没有，请手动修改下面的数据库连接信息

DB_NAME="gym_management"
DB_USER="root"
DB_PASS=""

# 如果application.properties存在，尝试读取配置
if [ -f "src/main/resources/application.properties" ]; then
    # 简单提取数据库名（需要根据实际情况调整）
    echo "请确保数据库连接信息正确"
fi

echo "正在执行数据库迁移..."
echo "数据库: $DB_NAME"
echo ""

# 执行SQL脚本
if [ -z "$DB_PASS" ]; then
    mysql -u "$DB_USER" "$DB_NAME" < add_employee_permissions.sql
else
    mysql -u "$DB_USER" -p"$DB_PASS" "$DB_NAME" < add_employee_permissions.sql
fi

if [ $? -eq 0 ]; then
    echo "✅ 数据库迁移成功！"
    echo "权限字段已添加到employee表"
else
    echo "❌ 数据库迁移失败，请检查："
    echo "1. 数据库连接信息是否正确"
    echo "2. 数据库用户是否有ALTER TABLE权限"
    echo "3. 字段是否已经存在（如果已存在，可以忽略此错误）"
fi

