#!/bin/bash
# 执行交易记录表添加开始和到期日期字段的迁移

echo "📝 正在为transaction_record表添加start_date和expire_date字段..."

mysql -u root -p123456 gym_management <<EOF
-- 为transaction_record表添加开始日期和到期日期字段
ALTER TABLE transaction_record 
ADD COLUMN IF NOT EXISTS start_date DATE COMMENT '卡开始日期' AFTER transaction_date,
ADD COLUMN IF NOT EXISTS expire_date DATE COMMENT '卡到期日期' AFTER start_date;

-- 为现有记录填充数据（从member表获取，使用交易日期作为开始日期，会员表的到期日期作为到期日期）
UPDATE transaction_record tr
INNER JOIN member m ON tr.member_id = m.id
SET tr.start_date = COALESCE(tr.start_date, tr.transaction_date),
    tr.expire_date = COALESCE(tr.expire_date, m.expire_date)
WHERE tr.start_date IS NULL OR tr.expire_date IS NULL;

SELECT '迁移完成！' as result;
EOF

if [ $? -eq 0 ]; then
  echo "✅ 数据库迁移成功！"
else
  echo "❌ 数据库迁移失败，请检查MySQL连接和权限"
fi

