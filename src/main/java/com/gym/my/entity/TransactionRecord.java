package com.gym.my.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class TransactionRecord {
    private Long id;
    private Long memberId;
    private Long cardTypeId;
    private String transactionType; // NEW-新开卡，RENEW-续费
    private BigDecimal amount;
    private BigDecimal commissionAmount;
    private Long employeeId;
    private LocalDateTime transactionDate; // 交易日期时间（精确到秒）
    private LocalDate startDate; // 卡开始日期
    private LocalDate expireDate; // 卡到期日期
    private String remark;
    private LocalDateTime createdAt;
    
    // 关联对象
    private Member member;
    private CardType cardType;
    private Employee employee;
}
