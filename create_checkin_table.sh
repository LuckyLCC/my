#!/bin/bash

# 创建会员签到表的脚本
# 使用方法: ./create_checkin_table.sh

DB_NAME="gym_management"
DB_USER="root"
DB_PASSWORD="123456"

echo "正在创建 member_checkin 表..."

# 检查是否安装了 mysql 客户端
if ! command -v mysql &> /dev/null; then
    echo "错误: 未找到 mysql 客户端命令"
    echo "请手动执行以下SQL语句:"
    echo ""
    cat create_checkin_table.sql
    echo ""
    echo "或者使用数据库管理工具（如 MySQL Workbench, phpMyAdmin 等）执行 create_checkin_table.sql 文件"
    exit 1
fi

# 执行SQL脚本
mysql -u"$DB_USER" -p"$DB_PASSWORD" < create_checkin_table.sql

if [ $? -eq 0 ]; then
    echo "✅ member_checkin 表创建成功！"
    echo ""
    echo "验证表结构:"
    mysql -u"$DB_USER" -p"$DB_PASSWORD" -e "USE $DB_NAME; DESCRIBE member_checkin;"
else
    echo "❌ 创建表失败，请检查数据库连接和权限"
    exit 1
fi

