# 快速修复签到表问题

## 问题
点击签到按钮时提示：`Table 'gym_management.member_checkin' doesn't exist`

## 最快解决方法

### 步骤1：执行SQL创建表

使用数据库管理工具执行以下SQL：

```sql
USE gym_management;

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
```

### 步骤2：验证

执行以下SQL验证：
```sql
SHOW TABLES LIKE 'member_checkin';
DESCRIBE member_checkin;
```

### 步骤3：刷新页面

创建表后，刷新前端页面，重新尝试签到功能。

---

**如果使用MySQL Workbench**：
1. 打开 MySQL Workbench
2. 连接到数据库
3. 选择 `gym_management` 数据库
4. 新建查询，粘贴上面的SQL
5. 执行查询

**如果使用命令行**：
```bash
mysql -u root -p123456 gym_management -e "CREATE TABLE IF NOT EXISTS member_checkin (id BIGINT PRIMARY KEY AUTO_INCREMENT, member_id BIGINT NOT NULL COMMENT '会员ID', checkin_date DATE NOT NULL COMMENT '签到日期', checkin_time DATETIME NOT NULL COMMENT '签到时间', employee_id BIGINT COMMENT '签到操作员工ID', remark VARCHAR(255) COMMENT '备注', created_at DATETIME DEFAULT CURRENT_TIMESTAMP, INDEX idx_member (member_id), INDEX idx_checkin_date (checkin_date), INDEX idx_member_date (member_id, checkin_date), FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE, FOREIGN KEY (employee_id) REFERENCES employee(id)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员签到表';"
```

