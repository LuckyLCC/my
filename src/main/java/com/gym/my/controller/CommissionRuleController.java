package com.gym.my.controller;

import com.gym.my.dto.ApiResponse;
import com.gym.my.entity.CommissionRule;
import com.gym.my.service.CommissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/commission-rules")
@RequiredArgsConstructor
public class CommissionRuleController {
    
    private final CommissionService commissionService;
    
    @GetMapping
    public ApiResponse<List<CommissionRule>> getAllRules() {
        try {
            List<CommissionRule> rules = commissionService.getAllRules();
            return ApiResponse.success(rules);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    public ApiResponse<CommissionRule> getRule(@PathVariable Long id) {
        try {
            CommissionRule rule = commissionService.getRule(id);
            if (rule == null) {
                return ApiResponse.error("规则不存在");
            }
            return ApiResponse.success(rule);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping
    public ApiResponse<CommissionRule> createRule(@RequestBody CommissionRule rule) {
        try {
            CommissionRule created = commissionService.createRule(rule);
            return ApiResponse.success("创建成功", created);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public ApiResponse<CommissionRule> updateRule(@PathVariable Long id, @RequestBody CommissionRule rule) {
        try {
            rule.setId(id);
            CommissionRule updated = commissionService.updateRule(rule);
            return ApiResponse.success("更新成功", updated);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteRule(@PathVariable Long id) {
        try {
            commissionService.deleteRule(id);
            return ApiResponse.success("删除成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
