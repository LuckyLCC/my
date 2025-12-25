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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
            HttpServletRequest request) {
        try {
            Employee employee = PermissionUtil.getCurrentEmployee(request);
            if (!PermissionUtil.hasMemberReadPermission(employee)) {
                return ApiResponse.error("权限不足：无会员查看权限");
            }
            List<Member> members = memberService.getAllMembers(name, phone, isExpired);
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
        try {
            Member member = memberService.renewMember(request);
            return ApiResponse.success("续费成功", member);
        } catch (Exception e) {
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
