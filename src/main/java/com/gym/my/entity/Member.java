package com.gym.my.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Member {
    private Long id;
    private String name;
    private String gender;
    private String phone;
    private String idCard;
    private Long cardTypeId;
    private LocalDate startDate;
    private LocalDate expireDate;
    private Integer remainingTimes; // 剩余次数（次卡使用）
    private Integer isExpired; // 0-未过期，1-已过期
    private Long firstEmployeeId; // 首次开卡员工ID
    private Long lastEmployeeId; // 最新续卡员工ID
    private Integer status; // 1-正常，0-注销
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 关联对象
    private CardType cardType;
    private Employee firstEmployee;
    private Employee lastEmployee;
    
    // 最近签到时间
    private LocalDateTime lastCheckinTime;
    
    // 超过7天未签到（1-是，0-否）
    private Integer noCheckinOver7Days;
}
