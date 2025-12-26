-- 添加首次开卡日期字段
-- 为会员表添加 first_card_date 字段，用于保存首次开卡日期
-- 续卡时，start_date 保持不变（仍为首次开卡日期），expire_date 更新为新的到期日期

ALTER TABLE member 
ADD COLUMN first_card_date DATE COMMENT '首次开卡日期' AFTER start_date;

-- 对于现有数据，将 start_date 的值复制到 first_card_date（假设现有会员的 start_date 就是首次开卡日期）
UPDATE member SET first_card_date = start_date WHERE first_card_date IS NULL;

