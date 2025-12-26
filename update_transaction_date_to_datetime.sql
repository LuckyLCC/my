-- 将transaction_record表的transaction_date字段从DATE改为DATETIME
-- 这样交易日期可以精确到秒

ALTER TABLE transaction_record 
MODIFY COLUMN transaction_date DATETIME NOT NULL COMMENT '交易日期时间（精确到秒）';

-- 为现有记录填充时间（使用created_at的时间部分，如果没有则使用当前时间）
UPDATE transaction_record 
SET transaction_date = CONCAT(DATE(transaction_date), ' ', TIME(COALESCE(created_at, NOW())))
WHERE TIME(transaction_date) = '00:00:00' OR transaction_date IS NULL;

