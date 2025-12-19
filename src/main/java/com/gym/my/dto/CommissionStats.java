package com.gym.my.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CommissionStats {
    private Long employeeId;
    private String employeeName;
    private String month; // 格式：2024-01
    private BigDecimal totalCommission;
    private Integer newCardCount;
    private BigDecimal newCardCommission;
    private Integer renewCount;
    private BigDecimal renewCommission;
}
