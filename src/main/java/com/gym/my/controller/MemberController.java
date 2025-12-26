package com.gym.my.controller;

import com.gym.my.dto.ApiResponse;
import com.gym.my.dto.MemberRequest;
import com.gym.my.dto.RenewRequest;
import com.gym.my.entity.Employee;
import com.gym.my.entity.Member;
import com.gym.my.entity.TransactionRecord;
import com.gym.my.service.MemberService;
import com.gym.my.util.PermissionUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {
    
    private final MemberService memberService;
    
    @GetMapping
    public ApiResponse<List<Member>> getAllMembers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Integer isExpired,
            @RequestParam(required = false) Integer expireWithinDays,
            HttpServletRequest request) {
        try {
            Employee employee = PermissionUtil.getCurrentEmployee(request);
            if (!PermissionUtil.hasMemberReadPermission(employee)) {
                return ApiResponse.error("权限不足：无会员查看权限");
            }
            List<Member> members = memberService.getAllMembers(name, phone, isExpired, expireWithinDays);
            return ApiResponse.success(members);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    public ApiResponse<Member> getMember(@PathVariable Long id, HttpServletRequest request) {
        try {
            Employee employee = PermissionUtil.getCurrentEmployee(request);
            if (!PermissionUtil.hasMemberReadPermission(employee)) {
                return ApiResponse.error("权限不足：无会员查看权限");
            }
            Member member = memberService.getMember(id);
            if (member == null) {
                return ApiResponse.error("会员不存在");
            }
            return ApiResponse.success(member);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping
    public ApiResponse<Member> createMember(@Validated @RequestBody MemberRequest request, HttpServletRequest httpRequest) {
        try {
            Employee employee = PermissionUtil.getCurrentEmployee(httpRequest);
            if (!PermissionUtil.hasMemberCreatePermission(employee)) {
                return ApiResponse.error("权限不足：无会员创建权限");
            }
            Member member = memberService.createMember(request);
            return ApiResponse.success("开卡成功", member);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public ApiResponse<Member> updateMember(@PathVariable Long id, @Validated @RequestBody MemberRequest request, HttpServletRequest httpRequest) {
        try {
            Employee employee = PermissionUtil.getCurrentEmployee(httpRequest);
            if (!PermissionUtil.hasMemberUpdatePermission(employee)) {
                return ApiResponse.error("权限不足：无会员更新权限");
            }
            request.setId(id);
            Member member = memberService.updateMember(request);
            return ApiResponse.success("更新成功", member);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteMember(@PathVariable Long id, HttpServletRequest request) {
        try {
            Employee employee = PermissionUtil.getCurrentEmployee(request);
            if (!PermissionUtil.hasMemberDeletePermission(employee)) {
                return ApiResponse.error("权限不足：无会员删除权限");
            }
            memberService.deleteMember(id);
            return ApiResponse.success("删除成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/renew")
    public ApiResponse<Member> renewMember(@Validated @RequestBody RenewRequest request) {
        log.info("收到续卡请求，会员ID: {}, 卡种ID: {}, 续卡日期: {}, 员工ID: {}", 
            request.getMemberId(), request.getCardTypeId(), request.getRenewDate(), request.getEmployeeId());
        try {
            Member member = memberService.renewMember(request);
            log.info("续卡成功，会员ID: {}", request.getMemberId());
            return ApiResponse.success("续费成功", member);
        } catch (Exception e) {
            log.error("续卡失败，会员ID: {}, 错误: {}", request.getMemberId(), e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/{id}/renew-history")
    public ApiResponse<List<TransactionRecord>> getRenewHistory(@PathVariable Long id) {
        try {
            List<TransactionRecord> records = memberService.getRenewHistory(id);
            return ApiResponse.success(records);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
