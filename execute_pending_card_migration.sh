#!/bin/bash

# 执行未生效卡种字段迁移脚本
# 为会员表添加未生效卡种相关字段

set -e

DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-gym}"
DB_USER="${DB_USER:-root}"
DB_PASSWORD="${DB_PASSWORD:-}"

echo "开始执行未生效卡种字段迁移..."
echo "数据库: ${DB_USER}@${DB_HOST}:${DB_PORT}/${DB_NAME}"

if [ -z "$DB_PASSWORD" ]; then
    MYSQL_CMD="mysql -h${DB_HOST} -P${DB_PORT} -u${DB_USER} ${DB_NAME}"
else
    MYSQL_CMD="mysql -h${DB_HOST} -P${DB_PORT} -u${DB_USER} -p${DB_PASSWORD} ${DB_NAME}"
fi

# 检查字段是否已存在
FIELD_EXISTS=$(echo "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='${DB_NAME}' AND TABLE_NAME='member' AND COLUMN_NAME='pending_card_start_date';" | $MYSQL_CMD -s -N)

if [ "$FIELD_EXISTS" -eq "1" ]; then
    echo "字段 pending_card_start_date 已存在，跳过迁移"
    exit 0
fi

echo "正在添加未生效卡种相关字段..."
$MYSQL_CMD <<EOF
-- 添加未生效卡种相关字段
ALTER TABLE member 
ADD COLUMN pending_card_start_date DATE COMMENT '未生效卡种开始日期（未来生效的续卡日期）' AFTER expire_date,
ADD COLUMN pending_card_type_id BIGINT COMMENT '未生效卡种ID' AFTER pending_card_start_date,
ADD COLUMN pending_card_expire_date DATE COMMENT '未生效卡种到期日期（从开始日期+卡种时长计算得出）' AFTER pending_card_type_id,
ADD INDEX idx_pending_card_start_date (pending_card_start_date),
ADD FOREIGN KEY (pending_card_type_id) REFERENCES card_type(id);

SELECT '迁移完成！' AS result;
EOF

echo "未生效卡种字段迁移完成！"

