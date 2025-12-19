package com.gym.my.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class MemberCheckin {
    private Long id;
    private Long memberId;
    private LocalDate checkinDate;
    private LocalDateTime checkinTime;
    private Long employeeId;
    private String remark;
    private LocalDateTime createdAt;
    
    // 关联对象
    private Member member;
    private Employee employee;
}

