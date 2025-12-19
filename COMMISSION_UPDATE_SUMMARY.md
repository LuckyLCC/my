# 提成规则统计功能更新说明

## 更新日期
2025-12-19

## 功能变更

### 原功能
- 按提成比例计算员工提成
- 统计新开卡和续费的提成金额

### 新功能
- **统计员工开了多少张卡**：只统计NEW类型（新开卡）的交易
- **关联具体会员**：每张卡关联的会员信息（姓名、手机号、卡种、金额、日期）
- **统计所有员工当月总开卡金额**：汇总所有员工当月的开卡总金额和总数量

## 后端修改

### 1. 新增DTO类

#### EmployeeCardStats.java
- `employeeId`: 员工ID
- `employeeName`: 员工姓名
- `month`: 月份
- `cardCount`: 开卡数量
- `totalCardAmount`: 该员工当月开卡总金额
- `cardMembers`: 开卡详情列表（关联会员信息）

#### CardMemberInfo.java
- `memberId`: 会员ID
- `memberName`: 会员姓名
- `memberPhone`: 会员手机号
- `cardTypeId`: 卡种ID
- `cardTypeName`: 卡种名称
- `amount`: 开卡金额
- `transactionDate`: 开卡日期

#### MonthlyCardSummary.java
- `month`: 月份
- `totalCardAmount`: 所有员工当月总共开卡金额
- `totalCardCount`: 所有员工当月总共开卡数量
- `employeeStats`: 各员工开卡统计列表

### 2. 修改Mapper

**TransactionRecordMapper.java** 新增方法：
- `getEmployeeCardStatsByMonth()`: 获取员工开卡统计（只统计NEW类型）
- `getCardMemberDetails()`: 获取员工开卡明细（关联会员信息）
- `getMonthlyTotalCardAmount()`: 获取当月所有员工总共开卡金额和数量

### 3. 修改Service

**FinancialService.java** 新增方法：
- `getMonthlyCardSummary()`: 获取月度开卡统计汇总
  - 调用Mapper获取各员工统计
  - 为每个员工加载开卡明细（关联会员）
  - 计算总开卡金额和数量
  - 返回完整的MonthlyCardSummary对象

### 4. 修改Controller

**FinancialController.java** 新增接口：
- `GET /api/financial/card-summary?month=2024-12`
  - 返回月度开卡统计汇总
  - 包含汇总信息和各员工详细统计

## 前端修改

### 1. 新增模块

在 `frontend/index.html` 中添加了"开卡统计"模块：
- **模块key**: `cardSummary`
- **API**: `/api/financial/card-summary`
- **列定义**:
  - 员工ID
  - 员工姓名
  - 月份
  - 开卡数量
  - 开卡金额（格式化显示为¥）
  - 开卡明细（显示会员姓名、手机号、卡种、金额）

### 2. 汇总信息显示

在表格上方显示汇总卡片：
- **月份**: 当前查询的月份
- **总开卡数量**: 所有员工当月总共开卡数量（蓝色高亮）
- **总开卡金额**: 所有员工当月总共开卡金额（绿色高亮，格式化为¥）

### 3. 数据处理

- 特殊处理 `cardSummary` 模块的API响应
- 从 `MonthlyCardSummary` 对象中提取汇总信息和员工列表
- 在表格中显示每个员工的开卡数量和金额
- 在"开卡明细"列中显示关联的会员信息

## API接口

### 新接口

```
GET /api/financial/card-summary?month=2024-12
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "month": "2024-12",
    "totalCardAmount": 50000.00,
    "totalCardCount": 25,
    "employeeStats": [
      {
        "employeeId": 1,
        "employeeName": "张三",
        "month": "2024-12",
        "cardCount": 10,
        "totalCardAmount": 20000.00,
        "cardMembers": [
          {
            "memberId": 1,
            "memberName": "会员A",
            "memberPhone": "13800138000",
            "cardTypeId": 1,
            "cardTypeName": "月卡",
            "amount": 2000.00,
            "transactionDate": "2024-12-01"
          }
        ]
      }
    ]
  }
}
```

## 使用说明

1. **访问开卡统计**：
   - 在左侧菜单点击"开卡统计"
   - 选择要查询的月份
   - 点击"刷新"按钮

2. **查看汇总信息**：
   - 表格上方显示汇总卡片
   - 显示总开卡数量和总开卡金额

3. **查看员工统计**：
   - 表格中显示每个员工的开卡数量
   - 显示每个员工的开卡总金额
   - 在"开卡明细"列中可以看到该员工开了哪些卡，关联了哪些会员

4. **开卡明细格式**：
   - 格式：`会员姓名(手机号) - 卡种名称 - ¥金额`
   - 多个明细用分号分隔

## 注意事项

1. **只统计NEW类型**：只统计新开卡（transaction_type='NEW'），不包含续费
2. **数据来源**：数据来自 `transaction_record` 表
3. **兼容性**：保留了原有的 `/api/financial/commission-stats` 接口，不影响现有功能
4. **前端显示**：开卡明细列可能较长，建议使用横向滚动查看

## 文件清单

### 后端文件
- `src/main/java/com/gym/my/dto/EmployeeCardStats.java` - 员工开卡统计DTO
- `src/main/java/com/gym/my/dto/CardMemberInfo.java` - 卡种-会员关联信息DTO
- `src/main/java/com/gym/my/dto/MonthlyCardSummary.java` - 月度开卡汇总DTO
- `src/main/java/com/gym/my/mapper/TransactionRecordMapper.java` - 新增查询方法
- `src/main/java/com/gym/my/service/FinancialService.java` - 新增统计方法
- `src/main/java/com/gym/my/controller/FinancialController.java` - 新增API接口

### 前端文件
- `frontend/index.html` - 添加开卡统计模块和汇总信息显示

---

**更新时间**: 2025-12-19  
**状态**: 已完成

