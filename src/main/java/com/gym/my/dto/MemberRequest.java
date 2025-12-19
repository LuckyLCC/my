package com.gym.my.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
public class MemberRequest {
    private Long id;
    
    @NotBlank(message = "会员姓名不能为空")
    private String name;
    
    private String gender;
    
    @NotBlank(message = "手机号不能为空")
    private String phone;
    
    private String idCard;
    
    @NotNull(message = "卡种ID不能为空")
    private Long cardTypeId;
    
    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;
    
    private Integer remainingTimes; // 次卡的次数
    
    @NotNull(message = "员工ID不能为空")
    private Long employeeId; // 业绩归属员工ID
}
