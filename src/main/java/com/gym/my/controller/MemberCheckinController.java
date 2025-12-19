package com.gym.my.controller;

import com.gym.my.dto.ApiResponse;
import com.gym.my.entity.MemberCheckin;
import com.gym.my.service.MemberCheckinService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberCheckinController {
    
    private final MemberCheckinService checkinService;
    
    /**
     * 会员签到
     */
    @PostMapping("/{memberId}/checkin")
    public ApiResponse<MemberCheckin> checkin(
            @PathVariable Long memberId,
            @RequestParam(required = false) String remark,
            HttpServletRequest request) {
        try {
            // 从请求属性中获取当前员工（由AuthInterceptor设置）
            com.gym.my.entity.Employee currentEmployee = (com.gym.my.entity.Employee) request.getAttribute("currentEmployee");
            if (currentEmployee == null) {
                return ApiResponse.error("无法获取当前员工信息");
            }
            Long employeeId = currentEmployee.getId();
            
            MemberCheckin checkin = checkinService.checkin(memberId, employeeId, remark);
            return ApiResponse.success("签到成功", checkin);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 获取会员签到历史
     */
    @GetMapping("/{memberId}/checkins")
    public ApiResponse<List<MemberCheckin>> getCheckinHistory(@PathVariable Long memberId) {
        try {
            List<MemberCheckin> checkins = checkinService.getCheckinHistory(memberId);
            return ApiResponse.success(checkins);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 检查今天是否已签到
     */
    @GetMapping("/{memberId}/checkin/today")
    public ApiResponse<Boolean> hasCheckedInToday(@PathVariable Long memberId) {
        try {
            boolean hasCheckedIn = checkinService.hasCheckedInToday(memberId);
            return ApiResponse.success(hasCheckedIn);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}

