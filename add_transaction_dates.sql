-- 为transaction_record表添加开始日期和到期日期字段
ALTER TABLE transaction_record 
ADD COLUMN start_date DATE COMMENT '卡开始日期' AFTER transaction_date,
ADD COLUMN expire_date DATE COMMENT '卡到期日期' AFTER start_date;

-- 为现有记录填充数据（从member表获取）
UPDATE transaction_record tr
INNER JOIN member m ON tr.member_id = m.id
SET tr.start_date = m.start_date,
    tr.expire_date = m.expire_date
WHERE tr.start_date IS NULL OR tr.expire_date IS NULL;

