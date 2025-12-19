-- 删除员工数据的 SQL 脚本
-- 注意：由于外键约束，需要先处理关联数据

USE gym_management;

-- 1. 查看当前员工数据
SELECT id, username, name, role, status FROM employee;

-- 2. 查看关联数据（会员表中的员工引用）
SELECT COUNT(*) as member_count FROM member WHERE first_employee_id IS NOT NULL OR last_employee_id IS NOT NULL;

-- 3. 查看交易记录中的员工引用
SELECT COUNT(*) as transaction_count FROM transaction_record WHERE employee_id IS NOT NULL;

-- 4. 删除策略选项：

-- 选项 A：只删除非 admin 的员工，保留 admin 账号
-- 先清空会员表中的员工引用（设置为 NULL）
UPDATE member SET first_employee_id = NULL WHERE first_employee_id IS NOT NULL AND first_employee_id != 1;
UPDATE member SET last_employee_id = NULL WHERE last_employee_id IS NOT NULL AND last_employee_id != 1;

-- 删除交易记录（如果不需要保留历史数据）
-- DELETE FROM transaction_record WHERE employee_id IS NOT NULL AND employee_id != 1;

-- 删除非 admin 员工
-- DELETE FROM employee WHERE id != 1;

-- 选项 B：删除所有员工（包括 admin，需要重新创建）
-- 先删除所有关联数据
-- DELETE FROM transaction_record;
-- UPDATE member SET first_employee_id = NULL, last_employee_id = NULL;
-- DELETE FROM user_session;  -- 这个有 CASCADE，会自动删除
-- DELETE FROM employee;

-- 选项 C：只删除特定员工（替换 {employee_id} 为实际 ID）
-- UPDATE member SET first_employee_id = NULL WHERE first_employee_id = {employee_id};
-- UPDATE member SET last_employee_id = NULL WHERE last_employee_id = {employee_id};
-- DELETE FROM transaction_record WHERE employee_id = {employee_id};
-- DELETE FROM employee WHERE id = {employee_id};

-- 执行删除后，重新创建 admin 账号（如果需要）
-- INSERT INTO employee (username, password, name, role) VALUES 
-- ('admin', 'admin123', '系统管理员', 'ADMIN');

