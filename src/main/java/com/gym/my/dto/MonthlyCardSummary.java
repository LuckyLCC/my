package com.gym.my.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 月度开卡汇总
 */
@Data
public class MonthlyCardSummary {
    private String month; // 格式：2024-01
    private BigDecimal totalCardAmount; // 所有员工当月总共开卡金额
    private Integer totalCardCount; // 所有员工当月总共开卡数量
    private List<EmployeeCardStats> employeeStats; // 各员工开卡统计
}

