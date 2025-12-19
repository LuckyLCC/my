# 健身房会员管理系统

一个功能完善的健身房会员管理系统，基于 Spring Boot + MyBatis + MySQL 开发。

## ✨ 核心功能

### 1. 会员管理
- ✅ 会员增删改查
- ✅ 记录首次开卡人和最新续卡人
- ✅ 支持按姓名、手机号、过期状态筛选
- ✅ 会员详情查看（包含卡种、员工信息）

### 2. 有效期联动
- ✅ 选择卡种自动计算到期日
  - 月卡/季卡：按月累加
  - 年卡：按年累加
  - 次卡：默认1年有效期
- ✅ 过期自动标记（`isExpired` 字段）
- ✅ 定时任务每日检查过期状态

### 3. 提成体系
- ✅ 内置详细的提成规则字典
- ✅ 支持新开卡和续费不同提成
- ✅ 支持比例提成和固定金额提成
- ✅ 支持手动指定业绩归属员工
- ✅ 自动计算提成金额并记录

### 4. 权限管理
- ✅ Admin 角色：可管理员工账号和提成规则
- ✅ Staff 角色：只能操作业务（会员、卡种）
- ✅ 基于拦截器的权限控制
- ✅ 接口级权限校验

### 5. 免登陆
- ✅ 使用 JWT Token 认证
- ✅ Token + 数据库 Session 双重验证
- ✅ 30分钟自动刷新机制
- ✅ 每次请求自动延长过期时间
- ✅ 定时清理过期Session

### 6. 财务统计
- ✅ 按月统计员工提成
- ✅ 区分新开卡和续费提成
- ✅ 支持穿透查询交易明细
- ✅ 导出 Excel 报表
  - 提成统计报表
  - 交易明细报表

## 🏗️ 技术架构

### 技术栈
- **后端框架**: Spring Boot 4.0.0
- **持久层**: MyBatis 3.0.3
- **数据库**: MySQL 8.0+
- **认证**: JWT (JJWT 0.12.5)
- **Excel**: Apache POI 5.2.5
- **Java**: JDK 17

### 项目结构
```
src/main/java/com/gym/my/
├── MyApplication.java          # 主启动类
├── config/                     # 配置类
│   ├── AuthInterceptor.java    # 认证拦截器
│   ├── AdminInterceptor.java   # 管理员权限拦截器
│   └── WebConfig.java          # Web配置（CORS、拦截器）
├── controller/                 # 控制器层
│   ├── AuthController.java     # 认证接口
│   ├── MemberController.java   # 会员管理接口
│   ├── CardTypeController.java # 卡种管理接口
│   ├── EmployeeController.java # 员工管理接口（仅管理员）
│   ├── CommissionRuleController.java # 提成规则接口（仅管理员）
│   └── FinancialController.java # 财务统计接口
├── dto/                        # 数据传输对象
│   ├── LoginRequest.java       # 登录请求
│   ├── MemberRequest.java      # 会员请求
│   ├── RenewRequest.java       # 续费请求
│   ├── ApiResponse.java        # 统一响应
│   └── CommissionStats.java    # 提成统计
├── entity/                     # 实体类
│   ├── Employee.java           # 员工
│   ├── UserSession.java        # 用户会话
│   ├── CardType.java           # 卡种
│   ├── Member.java             # 会员
│   ├── CommissionRule.java     # 提成规则
│   └── TransactionRecord.java  # 交易记录
├── exception/                  # 异常处理
│   └── GlobalExceptionHandler.java # 全局异常处理器
├── mapper/                     # MyBatis Mapper
│   ├── EmployeeMapper.java
│   ├── UserSessionMapper.java
│   ├── CardTypeMapper.java
│   ├── MemberMapper.java
│   ├── CommissionRuleMapper.java
│   └── TransactionRecordMapper.java
├── service/                    # 业务逻辑层
│   ├── AuthService.java        # 认证服务
│   ├── EmployeeService.java    # 员工服务
│   ├── CardTypeService.java    # 卡种服务
│   ├── MemberService.java      # 会员服务
│   ├── CommissionService.java  # 提成服务
│   └── FinancialService.java   # 财务统计服务
└── task/                       # 定时任务
    └── ScheduledTasks.java     # 定时任务（过期检查、Session清理）

src/main/resources/
├── application.properties      # 应用配置
└── schema.sql                 # 数据库初始化脚本
```

## 📊 数据库设计

### 核心表结构

1. **employee** - 员工表
   - 用户名、密码、姓名、角色（ADMIN/STAFF）

2. **user_session** - 会话表
   - Token、过期时间（30分钟免登陆）

3. **card_type** - 卡种表
   - 卡种名称、类型（月/季/年/次）、时长、价格

4. **member** - 会员表
   - 会员信息、卡种、有效期、首次开卡人、最新续卡人

5. **commission_rule** - 提成规则表
   - 卡种、交易类型（新开卡/续费）、提成比例/固定金额

6. **transaction_record** - 交易记录表
   - 交易信息、提成金额、业绩归属员工

## 🚀 快速开始

### 1. 环境要求
- JDK 17+
- Maven 3.6+
- MySQL 8.0+

### 2. 数据库初始化

```bash
# 登录MySQL
mysql -u root -p

# 执行初始化脚本
source src/main/resources/schema.sql
```

### 3. 配置数据库连接

编辑 `src/main/resources/application.properties`：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/gym_management
spring.datasource.username=root
spring.datasource.password=your_password
```

### 4. 编译运行

```bash
# 使用Maven编译
mvn clean package

# 运行应用
mvn spring-boot:run

# 或直接运行jar包
java -jar target/my-0.0.1-SNAPSHOT.jar
```

应用将在 http://localhost:8080 启动

### 5. 默认账号

- 用户名: `admin`
- 密码: `admin123`
- 角色: ADMIN

## 📖 API文档

详细API文档请查看：[API_DOCUMENTATION.md](API_DOCUMENTATION.md)

### 快速示例

#### 登录
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

#### 查询会员列表
```bash
curl -X GET http://localhost:8080/api/members \
  -H "Authorization: Bearer {your_token}"
```

#### 新增会员
```bash
curl -X POST http://localhost:8080/api/members \
  -H "Authorization: Bearer {your_token}" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "张三",
    "phone": "13800138000",
    "cardTypeId": 1,
    "startDate": "2024-12-18",
    "employeeId": 1
  }'
```

## 🔐 权限说明

### 接口权限分类

1. **公开接口**
   - `/api/auth/login` - 登录

2. **认证用户接口**（需要Token）
   - `/api/members/**` - 会员管理
   - `/api/card-types/**` - 卡种管理
   - `/api/financial/**` - 财务统计
   - `/api/auth/logout` - 登出

3. **管理员专属接口**（需要ADMIN角色）
   - `/api/employees/**` - 员工管理
   - `/api/commission-rules/**` - 提成规则管理

## 🔄 业务流程

### 开卡流程
1. 选择会员信息 + 卡种
2. 系统自动计算到期日期
3. 根据提成规则计算提成
4. 创建会员记录 + 交易记录
5. 记录首次开卡人

### 续费流程
1. 选择会员 + 新卡种
2. 判断原卡是否过期
   - 未过期：从原到期日延长
   - 已过期：从续费日期开始
3. 计算续费提成
4. 更新会员到期日 + 最新续卡人
5. 创建交易记录

### 提成计算
1. 查询提成规则（卡种 + 交易类型）
2. 优先使用固定金额
3. 否则使用比例计算：金额 × 比例%
4. 记录到交易记录

## ⏰ 定时任务

### 1. 过期会员检查
- **时间**: 每天凌晨 1:00
- **功能**: 自动标记过期会员（`isExpired = 1`）

### 2. Session清理
- **时间**: 每小时整点
- **功能**: 删除过期的Token Session

## 📊 数据初始化

系统已预置以下数据：

### 卡种
- 月卡（299元，1个月）
- 季卡（799元，3个月）
- 年卡（2999元，12个月）
- 20次卡（599元，20次）
- 50次卡（1299元，50次）

### 提成规则
- 新开卡：15%-20%
- 续费：8%-15%

## 🛡️ 安全性

1. **密码加密**: 使用BCrypt加密（示例中为简化，生产环境需修改）
2. **JWT认证**: Token签名验证
3. **Session管理**: 数据库Session + 自动过期
4. **权限控制**: 基于角色的访问控制
5. **参数校验**: JSR-303验证

## 📝 开发建议

### 生产环境优化

1. **密码加密**
```java
// 使用BCrypt加密密码
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
String hashedPassword = encoder.encode(password);
boolean matches = encoder.matches(rawPassword, hashedPassword);
```

2. **JWT密钥配置**
```properties
# application.properties
jwt.secret=your-secret-key-at-least-256-bits
jwt.expiration=1800000
```

3. **数据库连接池**
```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>druid-spring-boot-starter</artifactId>
    <version>1.2.20</version>
</dependency>
```

4. **日志配置**
```properties
logging.level.com.gym.my=INFO
logging.file.name=logs/gym-management.log
```

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📄 许可证

MIT License

## 📞 联系方式

如有问题，请提交 Issue 或联系开发团队。

---

**注意**: 此项目为示例项目，生产环境使用前请务必：
1. 修改JWT密钥
2. 启用密码加密
3. 配置数据库连接池
4. 添加日志记录
5. 完善异常处理
6. 添加单元测试
