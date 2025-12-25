-- 为employee表添加会员权限字段
-- 执行此脚本前请备份数据库

ALTER TABLE employee 
ADD COLUMN member_create TINYINT DEFAULT 1 COMMENT '会员创建权限：1-有权限，0-无权限' AFTER status,
ADD COLUMN member_read TINYINT DEFAULT 1 COMMENT '会员查看权限：1-有权限，0-无权限' AFTER member_create,
ADD COLUMN member_update TINYINT DEFAULT 0 COMMENT '会员更新权限：1-有权限，0-无权限' AFTER member_read,
ADD COLUMN member_delete TINYINT DEFAULT 0 COMMENT '会员删除权限：1-有权限，0-无权限' AFTER member_update;

-- 设置现有数据的默认权限
-- ADMIN拥有所有权限
UPDATE employee SET 
  member_create = 1,
  member_read = 1,
  member_update = 1,
  member_delete = 1
WHERE role = 'ADMIN';

-- STAFF默认只有创建和查看权限
UPDATE employee SET 
  member_create = 1,
  member_read = 1,
  member_update = 0,
  member_delete = 0
WHERE role = 'STAFF' AND (member_create IS NULL OR member_read IS NULL OR member_update IS NULL OR member_delete IS NULL);

