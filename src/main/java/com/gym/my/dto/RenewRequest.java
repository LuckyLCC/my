package com.gym.my.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
public class RenewRequest {
    @NotNull(message = "会员ID不能为空")
    private Long memberId;
    
    @NotNull(message = "卡种ID不能为空")
    private Long cardTypeId;
    
    @NotNull(message = "续费日期不能为空")
    private LocalDate renewDate;
    
    @NotNull(message = "员工ID不能为空")
    private Long employeeId; // 业绩归属员工ID
    
    private String remark;
}
