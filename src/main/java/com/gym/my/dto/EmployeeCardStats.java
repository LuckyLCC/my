package com.gym.my.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 员工开卡统计DTO
 */
@Data
public class EmployeeCardStats {
    private Long employeeId;
    private String employeeName;
    private String month; // 格式：2024-01
    private Integer cardCount; // 开卡数量（只统计NEW类型）
    private BigDecimal totalCardAmount; // 该员工当月开卡总金额
    private BigDecimal totalCommissionAmount; // 该员工当月提成总金额
    private List<CardMemberInfo> cardMembers; // 开卡详情列表（关联会员信息）
}

