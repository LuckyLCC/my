-- 添加未生效卡种相关字段
-- 用于存储未来生效的续卡信息

ALTER TABLE member 
ADD COLUMN pending_card_start_date DATE COMMENT '未生效卡种开始日期（未来生效的续卡日期）' AFTER expire_date,
ADD COLUMN pending_card_type_id BIGINT COMMENT '未生效卡种ID' AFTER pending_card_start_date,
ADD COLUMN pending_card_expire_date DATE COMMENT '未生效卡种到期日期（从开始日期+卡种时长计算得出）' AFTER pending_card_type_id,
ADD INDEX idx_pending_card_start_date (pending_card_start_date),
ADD FOREIGN KEY (pending_card_type_id) REFERENCES card_type(id);

