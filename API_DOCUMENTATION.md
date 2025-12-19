# 健身房会员管理系统 API 文档

## 系统说明

这是一个功能完善的健身房会员管理系统，包含以下核心功能：

1. **会员管理**：增删改查，支持记录首次开卡人和最新续卡人
2. **有效期联动**：选择卡种自动计算到期日，过期自动标红
3. **提成体系**：支持新开卡和续费不同提成，支持手动指定业绩归属员工
4. **权限管理**：Admin可管理员工账号权限，普通员工只能操作业务
5. **免登陆**：使用Token + 数据库Session实现30分钟免登陆
6. **财务统计**：按月统计员工提成，支持穿透查询明细，支持导出Excel

## 快速开始

### 1. 数据库初始化

```bash
# 执行 src/main/resources/schema.sql 创建数据库和表
mysql -u root -p < src/main/resources/schema.sql
```

默认管理员账号：
- 用户名: `admin`
- 密码: `admin123`

### 2. 配置数据库

修改 `src/main/resources/application.properties` 中的数据库配置：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/gym_management
spring.datasource.username=root
spring.datasource.password=your_password
```

### 3. 启动应用

```bash
mvn spring-boot:run
```

应用将在 http://localhost:8080 启动

## API 接口文档

### 1. 认证接口

#### 1.1 登录
```
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}

响应:
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1...",
    "employeeId": 1,
    "username": "admin",
    "name": "系统管理员",
    "role": "ADMIN",
    "expiresIn": 1800
  }
}
```

#### 1.2 登出
```
POST /api/auth/logout
Authorization: Bearer {token}
```

### 2. 会员管理接口

所有接口需要在Header中携带Token：`Authorization: Bearer {token}`

#### 2.1 查询会员列表
```
GET /api/members?name={姓名}&phone={手机号}&isExpired={0/1}
```

#### 2.2 查询会员详情
```
GET /api/members/{id}
```

#### 2.3 新增会员（开卡）
```
POST /api/members
Content-Type: application/json

{
  "name": "张三",
  "gender": "男",
  "phone": "13800138000",
  "idCard": "110101199001011234",
  "cardTypeId": 1,
  "startDate": "2024-01-01",
  "remainingTimes": 20,
  "employeeId": 1
}
```

#### 2.4 更新会员信息
```
PUT /api/members/{id}
Content-Type: application/json

{
  "name": "张三",
  "gender": "男",
  "phone": "13800138000",
  "idCard": "110101199001011234"
}
```

#### 2.5 删除会员（软删除）
```
DELETE /api/members/{id}
```

#### 2.6 会员续费
```
POST /api/members/renew
Content-Type: application/json

{
  "memberId": 1,
  "cardTypeId": 1,
  "renewDate": "2024-12-01",
  "employeeId": 1,
  "remark": "续费备注"
}
```

### 3. 卡种管理接口

#### 3.1 查询卡种列表
```
GET /api/card-types
```

#### 3.2 查询卡种详情
```
GET /api/card-types/{id}
```

#### 3.3 新增卡种
```
POST /api/card-types
Content-Type: application/json

{
  "name": "半年卡",
  "type": "MONTH",
  "duration": 6,
  "price": 1499.00,
  "status": 1
}

卡种类型说明：
- MONTH: 月卡（duration为月数）
- QUARTER: 季卡（duration为月数）
- YEAR: 年卡（duration为年数）
- TIMES: 次卡（duration为次数）
```

#### 3.4 更新卡种
```
PUT /api/card-types/{id}
```

#### 3.5 删除卡种
```
DELETE /api/card-types/{id}
```

### 4. 员工管理接口（仅管理员）

#### 4.1 查询员工列表
```
GET /api/employees
```

#### 4.2 查询员工详情
```
GET /api/employees/{id}
```

#### 4.3 新增员工
```
POST /api/employees
Content-Type: application/json

{
  "username": "staff01",
  "password": "123456",
  "name": "员工01",
  "phone": "13900139000",
  "role": "STAFF",
  "status": 1
}

角色说明：
- ADMIN: 管理员（可管理员工和提成规则）
- STAFF: 普通员工（只能操作业务）
```

#### 4.4 更新员工
```
PUT /api/employees/{id}
```

#### 4.5 删除员工
```
DELETE /api/employees/{id}
```

### 5. 提成规则管理接口（仅管理员）

#### 5.1 查询提成规则列表
```
GET /api/commission-rules
```

#### 5.2 查询提成规则详情
```
GET /api/commission-rules/{id}
```

#### 5.3 新增提成规则
```
POST /api/commission-rules
Content-Type: application/json

{
  "cardTypeId": 1,
  "transactionType": "NEW",
  "commissionRate": 15.00,
  "fixedAmount": null
}

说明：
- transactionType: NEW-新开卡, RENEW-续费
- commissionRate: 提成比例（%），与fixedAmount二选一
- fixedAmount: 固定提成金额，优先级高于比例
```

#### 5.4 更新提成规则
```
PUT /api/commission-rules/{id}
```

#### 5.5 删除提成规则
```
DELETE /api/commission-rules/{id}
```

### 6. 财务统计接口

#### 6.1 查询月度提成统计
```
GET /api/financial/commission-stats?month=2024-12

响应示例：
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "employeeId": 1,
      "employeeName": "员工01",
      "month": "2024-12",
      "totalCommission": 1500.00,
      "newCardCount": 5,
      "newCardCommission": 900.00,
      "renewCount": 3,
      "renewCommission": 600.00
    }
  ]
}
```

#### 6.2 查询交易明细（穿透查询）
```
GET /api/financial/transactions?employeeId={员工ID}&month=2024-12&transactionType={NEW/RENEW}
```

#### 6.3 导出提成统计Excel
```
GET /api/financial/export/commission-stats?month=2024-12

返回Excel文件下载
```

#### 6.4 导出交易明细Excel
```
GET /api/financial/export/transactions?employeeId={员工ID}&month=2024-12&transactionType={NEW/RENEW}

返回Excel文件下载
```

## 业务逻辑说明

### 1. 有效期计算规则

- **月卡/季卡**：从开始日期起加对应月数
- **年卡**：从开始日期起加对应年数
- **次卡**：默认有效期1年，使用次数单独计数

### 2. 续费逻辑

- 如果会员卡未过期，从原到期日开始计算新的到期日
- 如果会员卡已过期，从续费日期开始计算新的到期日
- 次卡续费会累加剩余次数

### 3. 提成计算

- 优先使用固定提成金额（如果设置了）
- 否则使用提成比例计算：交易金额 × 提成比例%
- 新开卡和续费使用不同的提成规则

### 4. 过期检测

- 系统每天凌晨1点自动检查并标记过期会员
- 前端可根据 `isExpired` 字段标红显示

### 5. 会话管理

- Token有效期30分钟
- 每次请求自动刷新Token过期时间
- 系统每小时自动清理过期Session

## 权限控制

- **公开接口**：`/api/auth/login`
- **认证接口**：所有 `/api/**` 接口（除登录外）
- **管理员接口**：`/api/employees/**`, `/api/commission-rules/**`

## 响应格式

所有接口统一返回格式：

```json
{
  "code": 200,
  "message": "success",
  "data": {...}
}
```

错误响应：
```json
{
  "code": 500,
  "message": "错误信息",
  "data": null
}
```

状态码说明：
- 200: 成功
- 401: 未认证
- 403: 权限不足
- 500: 服务器错误

## 前端集成示例

```javascript
// 登录
const login = async (username, password) => {
  const response = await fetch('http://localhost:8080/api/auth/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ username, password })
  });
  const result = await response.json();
  if (result.code === 200) {
    localStorage.setItem('token', result.data.token);
    return result.data;
  }
  throw new Error(result.message);
};

// 带Token的请求
const fetchWithAuth = async (url, options = {}) => {
  const token = localStorage.getItem('token');
  const response = await fetch(url, {
    ...options,
    headers: {
      ...options.headers,
      'Authorization': `Bearer ${token}`
    }
  });
  return response.json();
};

// 查询会员列表
const getMembers = async () => {
  return fetchWithAuth('http://localhost:8080/api/members');
};

// 新增会员
const createMember = async (memberData) => {
  return fetchWithAuth('http://localhost:8080/api/members', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(memberData)
  });
};
```

## 技术栈

- **后端框架**: Spring Boot 4.0.0
- **持久层**: MyBatis 3.0.3
- **数据库**: MySQL 8.0+
- **认证**: JWT (JJWT 0.12.5)
- **Excel导出**: Apache POI 5.2.5
- **Java版本**: JDK 17

## 注意事项

1. 首次运行需要执行 `schema.sql` 初始化数据库
2. 修改 `application.properties` 中的数据库连接信息
3. 管理员密码已使用BCrypt加密（示例中为简化使用明文，生产环境请改用加密）
4. Token密钥应该配置在配置文件中，不要硬编码
5. 定时任务时间可在 `ScheduledTasks` 类中调整
6. 过期会员标红功能需前端配合实现（根据 `isExpired` 字段）
