package com.gym.my.controller;

import com.gym.my.dto.ApiResponse;
import com.gym.my.entity.Employee;
import com.gym.my.service.AuthService;
import com.gym.my.service.MemberService;
import com.gym.my.util.PermissionUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    
    private final MemberService memberService;
    private final AuthService authService;
    
    /**
     * 手动触发：更新过期会员（管理员接口）
     */
    @PostMapping("/tasks/update-expired-members")
    public ApiResponse<String> triggerUpdateExpiredMembers(HttpServletRequest request) {
        try {
            Employee employee = PermissionUtil.getCurrentEmployee(request);
            if (!PermissionUtil.isAdmin(employee)) {
                return ApiResponse.error("权限不足：仅管理员可操作");
            }
            
            log.info("管理员 {} 手动触发：更新过期会员", employee.getName());
            memberService.updateExpiredMembers();
            
            return ApiResponse.success("更新过期会员任务执行成功");
        } catch (Exception e) {
            log.error("手动触发更新过期会员任务失败", e);
            return ApiResponse.error("执行失败：" + e.getMessage());
        }
    }
    
    /**
     * 手动触发：激活未生效卡种（管理员接口）
     */
    @PostMapping("/tasks/activate-pending-cards")
    public ApiResponse<String> triggerActivatePendingCards(HttpServletRequest request) {
        try {
            Employee employee = PermissionUtil.getCurrentEmployee(request);
            if (!PermissionUtil.isAdmin(employee)) {
                return ApiResponse.error("权限不足：仅管理员可操作");
            }
            
            log.info("管理员 {} 手动触发：激活未生效卡种", employee.getName());
            int activatedCount = memberService.activatePendingCards();
            
            if (activatedCount > 0) {
                return ApiResponse.success(String.format("成功激活 %d 个未生效的卡种", activatedCount));
            } else {
                return ApiResponse.success("没有需要激活的未生效卡种（所有未生效卡种的开始日期都是未来日期，需要等到开始日期到了才会自动激活）");
            }
        } catch (Exception e) {
            log.error("手动触发激活未生效卡种任务失败", e);
            return ApiResponse.error("执行失败：" + e.getMessage());
        }
    }
    
    /**
     * 手动触发：强制激活所有未生效卡种（不限制开始日期，管理员接口）
     */
    @PostMapping("/tasks/force-activate-pending-cards")
    public ApiResponse<String> triggerForceActivatePendingCards(HttpServletRequest request) {
        try {
            Employee employee = PermissionUtil.getCurrentEmployee(request);
            if (!PermissionUtil.isAdmin(employee)) {
                return ApiResponse.error("权限不足：仅管理员可操作");
            }
            
            log.info("管理员 {} 手动触发：强制激活所有未生效卡种", employee.getName());
            int activatedCount = memberService.forceActivateAllPendingCards();
            
            if (activatedCount > 0) {
                return ApiResponse.success(String.format("强制激活成功，共激活 %d 个未生效的卡种（包括未来日期的卡种）", activatedCount));
            } else {
                return ApiResponse.success("没有需要激活的未生效卡种");
            }
        } catch (Exception e) {
            log.error("手动触发强制激活未生效卡种任务失败", e);
            return ApiResponse.error("执行失败：" + e.getMessage());
        }
    }
    
    /**
     * 手动触发：清理过期Session（管理员接口）
     */
    @PostMapping("/tasks/clean-expired-sessions")
    public ApiResponse<String> triggerCleanExpiredSessions(HttpServletRequest request) {
        try {
            Employee employee = PermissionUtil.getCurrentEmployee(request);
            if (!PermissionUtil.isAdmin(employee)) {
                return ApiResponse.error("权限不足：仅管理员可操作");
            }
            
            log.info("管理员 {} 手动触发：清理过期Session", employee.getName());
            authService.cleanExpiredSessions();
            
            return ApiResponse.success("清理过期Session任务执行成功");
        } catch (Exception e) {
            log.error("手动触发清理过期Session任务失败", e);
            return ApiResponse.error("执行失败：" + e.getMessage());
        }
    }
}

