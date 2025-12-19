# 修复签到表缺失问题

## 问题描述

点击签到按钮时出现错误：
```
Table 'gym_management.member_checkin' doesn't exist
```

## 原因

数据库表 `member_checkin` 尚未创建。虽然 `schema.sql` 中已经定义了创建表的SQL语句，但由于 `application.properties` 中设置了 `spring.sql.init.mode=never`，Spring Boot 不会自动执行 schema.sql。

## 解决方案

### 方案1：手动执行SQL脚本（推荐）

1. **使用命令行执行**：
   ```bash
   ./create_checkin_table.sh
   ```

2. **或者手动执行SQL**：
   ```bash
   mysql -u root -p gym_management < create_checkin_table.sql
   ```

3. **或者使用数据库管理工具**：
   - 打开 MySQL Workbench、phpMyAdmin 或其他数据库管理工具
   - 连接到 `gym_management` 数据库
   - 执行 `create_checkin_table.sql` 文件中的SQL语句

### 方案2：修改配置自动执行（需要重启应用）

如果你想让 Spring Boot 自动执行 schema.sql，可以修改 `application.properties`：

```properties
spring.sql.init.mode=always
spring.sql.init.schema-locations=classpath:schema.sql
```

然后重启 Spring Boot 应用。

**注意**：如果数据库中已有数据，自动执行可能会因为 `DROP TABLE` 语句而删除现有数据，请谨慎使用。

### 方案3：使用 Spring Boot 的初始化脚本

如果表已存在，Spring Boot 不会重新创建。你可以：

1. 临时修改 `application.properties`：
   ```properties
   spring.sql.init.mode=always
   ```

2. 重启应用，让 Spring Boot 执行 schema.sql

3. 执行完成后，改回：
   ```properties
   spring.sql.init.mode=never
   ```

## 验证

执行以下SQL验证表是否创建成功：

```sql
USE gym_management;
SHOW TABLES LIKE 'member_checkin';
DESCRIBE member_checkin;
```

如果看到表结构，说明创建成功。

## 表结构

`member_checkin` 表包含以下字段：
- `id`: 主键
- `member_id`: 会员ID（外键）
- `checkin_date`: 签到日期
- `checkin_time`: 签到时间
- `employee_id`: 操作员工ID（外键）
- `remark`: 备注
- `created_at`: 创建时间

## 执行后

创建表后，请：
1. 刷新前端页面
2. 重新尝试签到功能
3. 如果还有问题，检查后端日志

---

**创建时间**: 2025-12-19  
**状态**: 待执行

