#!/bin/bash

# 执行首次开卡日期字段迁移脚本
# 此脚本为会员表添加 first_card_date 字段，用于保存首次开卡日期
# 续卡时，start_date 保持不变（仍为首次开卡日期），expire_date 更新为新的到期日期

set -e

DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-gym}"
DB_USER="${DB_USER:-root}"
DB_PASSWORD="${DB_PASSWORD:-}"

echo "开始执行首次开卡日期字段迁移..."
echo "数据库: ${DB_USER}@${DB_HOST}:${DB_PORT}/${DB_NAME}"

if [ -z "$DB_PASSWORD" ]; then
    MYSQL_CMD="mysql -h${DB_HOST} -P${DB_PORT} -u${DB_USER} ${DB_NAME}"
else
    MYSQL_CMD="mysql -h${DB_HOST} -P${DB_PORT} -u${DB_USER} -p${DB_PASSWORD} ${DB_NAME}"
fi

# 检查字段是否已存在
FIELD_EXISTS=$(echo "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='${DB_NAME}' AND TABLE_NAME='member' AND COLUMN_NAME='first_card_date';" | $MYSQL_CMD -s -N)

if [ "$FIELD_EXISTS" -eq "1" ]; then
    echo "字段 first_card_date 已存在，跳过迁移"
    exit 0
fi

echo "正在添加 first_card_date 字段..."
$MYSQL_CMD <<EOF
-- 添加首次开卡日期字段
ALTER TABLE member 
ADD COLUMN first_card_date DATE COMMENT '首次开卡日期' AFTER start_date;

-- 对于现有数据，将 start_date 的值复制到 first_card_date（假设现有会员的 start_date 就是首次开卡日期）
UPDATE member SET first_card_date = start_date WHERE first_card_date IS NULL;

SELECT '迁移完成！' AS result;
EOF

echo "首次开卡日期字段迁移完成！"
echo ""
echo "说明："
echo "- start_date: 开始日期（首次开卡日期，续卡时保持不变）"
echo "- first_card_date: 首次开卡日期（与start_date保持一致，用于明确语义）"
echo "- expire_date: 到期日期（当前卡的到期日期，续卡时更新）"

