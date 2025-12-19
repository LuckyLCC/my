package com.gym.my.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 卡种-会员关联信息
 */
@Data
public class CardMemberInfo {
    private Long memberId;
    private String memberName;
    private String memberPhone;
    private Long cardTypeId;
    private String cardTypeName;
    private BigDecimal amount; // 开卡金额
    private LocalDate transactionDate; // 交易日期
    private String transactionType; // 交易类型：NEW-新开卡，RENEW-续费
    private LocalDateTime createdAt; // 创建时间（记录创建时间）
}

