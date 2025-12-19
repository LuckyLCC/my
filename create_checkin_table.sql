-- 创建会员签到表
USE gym_management;

-- 如果表已存在则删除（谨慎使用）
-- DROP TABLE IF EXISTS member_checkin;

-- 创建会员签到表
CREATE TABLE IF NOT EXISTS member_checkin (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    member_id BIGINT NOT NULL COMMENT '会员ID',
    checkin_date DATE NOT NULL COMMENT '签到日期',
    checkin_time DATETIME NOT NULL COMMENT '签到时间',
    employee_id BIGINT COMMENT '签到操作员工ID',
    remark VARCHAR(255) COMMENT '备注',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_member (member_id),
    INDEX idx_checkin_date (checkin_date),
    INDEX idx_member_date (member_id, checkin_date),
    FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE,
    FOREIGN KEY (employee_id) REFERENCES employee(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员签到表';

-- 验证表是否创建成功
SELECT 'member_checkin table created successfully' AS status;

