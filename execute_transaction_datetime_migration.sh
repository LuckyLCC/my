#!/bin/bash
# 执行交易记录表transaction_date字段从DATE改为DATETIME的迁移

echo "📝 正在将transaction_record表的transaction_date字段改为DATETIME类型..."

mysql -u root -p123456 gym_management <<EOF
-- 将transaction_date字段从DATE改为DATETIME
ALTER TABLE transaction_record 
MODIFY COLUMN transaction_date DATETIME NOT NULL COMMENT '交易日期时间（精确到秒）';

-- 为现有记录填充时间（使用created_at的时间部分，如果没有则使用当前时间）
UPDATE transaction_record 
SET transaction_date = CONCAT(DATE(transaction_date), ' ', TIME(COALESCE(created_at, NOW())))
WHERE TIME(transaction_date) = '00:00:00' OR transaction_date IS NULL;

SELECT '迁移完成！transaction_date字段已改为DATETIME类型' as result;
EOF

if [ $? -eq 0 ]; then
  echo "✅ 数据库迁移成功！"
  echo "💡 请重启后端服务以使新代码生效"
else
  echo "❌ 数据库迁移失败，请检查MySQL连接和权限"
fi

