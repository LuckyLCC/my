DROP TABLE IF EXISTS member_checkin;
DROP TABLE IF EXISTS transaction_record;
DROP TABLE IF EXISTS commission_rule;
DROP TABLE IF EXISTS member;
DROP TABLE IF EXISTS card_type;
DROP TABLE IF EXISTS user_session;
DROP TABLE IF EXISTS employee;

-- Employee table (员工表)
CREATE TABLE IF NOT EXISTS employee (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    phone VARCHAR(20) COMMENT '手机号',
    role VARCHAR(20) NOT NULL DEFAULT 'STAFF' COMMENT '角色：ADMIN/STAFF',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    member_create TINYINT DEFAULT 1 COMMENT '会员创建权限：1-有权限，0-无权限',
    member_read TINYINT DEFAULT 1 COMMENT '会员查看权限：1-有权限，0-无权限',
    member_update TINYINT DEFAULT 1 COMMENT '会员更新权限：1-有权限，0-无权限',
    member_delete TINYINT DEFAULT 0 COMMENT '会员删除权限：1-有权限，0-无权限',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工表';

-- Session table (会话表 - 用于免登陆)
CREATE TABLE IF NOT EXISTS user_session (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL COMMENT '员工ID',
    token VARCHAR(500) NOT NULL UNIQUE COMMENT 'JWT Token',
    expires_at DATETIME NOT NULL COMMENT '过期时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_employee (employee_id),
    INDEX idx_token (token),
    INDEX idx_expires (expires_at),
    FOREIGN KEY (employee_id) REFERENCES employee(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户会话表';

-- Card type table (卡种表)
CREATE TABLE IF NOT EXISTS card_type (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL COMMENT '卡种名称',
    type VARCHAR(20) NOT NULL COMMENT '类型：MONTH-月卡，QUARTER-季卡，YEAR-年卡，TIMES-次卡',
    duration INT COMMENT '时长（月/年/次数）',
    price DECIMAL(10,2) NOT NULL COMMENT '价格',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='卡种表';

-- Member table (会员表)
CREATE TABLE IF NOT EXISTS member (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL COMMENT '会员姓名',
    gender VARCHAR(10) COMMENT '性别',
    phone VARCHAR(20) NOT NULL COMMENT '手机号',
    id_card VARCHAR(50) COMMENT '身份证号',
    card_type_id BIGINT NOT NULL COMMENT '卡种ID',
    start_date DATE NOT NULL COMMENT '开始日期',
    expire_date DATE NOT NULL COMMENT '到期日期',
    remaining_times INT COMMENT '剩余次数（次卡使用）',
    is_expired TINYINT DEFAULT 0 COMMENT '是否过期：1-是，0-否',
    first_employee_id BIGINT COMMENT '首次开卡员工ID',
    last_employee_id BIGINT COMMENT '最新续卡员工ID',
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常，0-注销',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_phone (phone),
    INDEX idx_expire_date (expire_date),
    INDEX idx_is_expired (is_expired),
    FOREIGN KEY (card_type_id) REFERENCES card_type(id),
    FOREIGN KEY (first_employee_id) REFERENCES employee(id),
    FOREIGN KEY (last_employee_id) REFERENCES employee(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员表';

-- Commission rule table (提成规则表)
CREATE TABLE IF NOT EXISTS commission_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    card_type_id BIGINT NOT NULL COMMENT '卡种ID',
    transaction_type VARCHAR(20) NOT NULL COMMENT '交易类型：NEW-新开卡，RENEW-续费',
    commission_rate DECIMAL(5,2) NOT NULL COMMENT '提成比例（%）',
    fixed_amount DECIMAL(10,2) COMMENT '固定提成金额',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_card_transaction (card_type_id, transaction_type),
    FOREIGN KEY (card_type_id) REFERENCES card_type(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提成规则表';

-- Transaction record table (交易记录表)
CREATE TABLE IF NOT EXISTS transaction_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    member_id BIGINT NOT NULL COMMENT '会员ID',
    card_type_id BIGINT NOT NULL COMMENT '卡种ID',
    transaction_type VARCHAR(20) NOT NULL COMMENT '交易类型：NEW-新开卡，RENEW-续费',
    amount DECIMAL(10,2) NOT NULL COMMENT '交易金额',
    commission_amount DECIMAL(10,2) NOT NULL COMMENT '提成金额',
    employee_id BIGINT NOT NULL COMMENT '业绩归属员工ID',
    transaction_date DATE NOT NULL COMMENT '交易日期',
    remark TEXT COMMENT '备注',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_member (member_id),
    INDEX idx_employee (employee_id),
    INDEX idx_transaction_date (transaction_date),
    FOREIGN KEY (member_id) REFERENCES member(id),
    FOREIGN KEY (card_type_id) REFERENCES card_type(id),
    FOREIGN KEY (employee_id) REFERENCES employee(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易记录表';

-- Member check-in table (会员签到表)
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

-- Insert default admin user (password: admin123)
INSERT INTO employee (username, password, name, role) VALUES 
('admin', 'admin123', '系统管理员', 'ADMIN');

-- Insert sample card types
INSERT INTO card_type (name, type, duration, price) VALUES 
('月卡', 'MONTH', 1, 3980.00),
('季卡', 'QUARTER', 3, 10800.00),
('半年卡', 'MONTH', 6, 19800.00),
('年卡', 'YEAR', 1, 29800.00),
('2人半年卡', 'MONTH', 6, 23800.00),
('2人年卡', 'YEAR', 1, 39800.00),
('3人年卡', 'YEAR', 1, 55800.00),
('4人年卡', 'YEAR', 1, 69800.00),
('家庭100次卡', 'TIMES', 100, 19800.00),
('家庭200次卡', 'TIMES', 200, 36800.00);

-- Insert sample commission rules
INSERT INTO commission_rule (card_type_id, transaction_type, commission_rate, fixed_amount) VALUES 
(1, 'NEW', 15.00, NULL),
(1, 'RENEW', 10.00, NULL),
(2, 'NEW', 18.00, NULL),
(2, 'RENEW', 12.00, NULL),
(3, 'NEW', 18.00, NULL),
(3, 'RENEW', 12.00, NULL),
(4, 'NEW', 20.00, NULL),
(4, 'RENEW', 15.00, NULL),
(5, 'NEW', 18.00, NULL),
(5, 'RENEW', 12.00, NULL),
(6, 'NEW', 20.00, NULL),
(6, 'RENEW', 15.00, NULL),
(7, 'NEW', 20.00, NULL),
(7, 'RENEW', 15.00, NULL),
(8, 'NEW', 20.00, NULL),
(8, 'RENEW', 15.00, NULL),
(9, 'NEW', 12.00, NULL),
(9, 'RENEW', 8.00, NULL),
(10, 'NEW', 12.00, NULL),
(10, 'RENEW', 8.00, NULL);
