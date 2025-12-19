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
    private LocalDate transactionDate;
    private String remark;
    private LocalDateTime createdAt;
    
    // 关联对象
    private Member member;
    private CardType cardType;
    private Employee employee;
}
