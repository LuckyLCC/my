package com.gym.my.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CommissionRule {
    private Long id;
    private Long cardTypeId;
    private String transactionType; // NEW-新开卡，RENEW-续费
    private BigDecimal commissionRate; // 提成比例（%）
    private BigDecimal fixedAmount; // 固定提成金额
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
