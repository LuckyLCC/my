package com.gym.my.service;

import com.gym.my.entity.CommissionRule;
import com.gym.my.mapper.CommissionRuleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommissionService {
    
    private final CommissionRuleMapper commissionRuleMapper;
    
    /**
     * 计算提成金额
     */
    public BigDecimal calculateCommission(Long cardTypeId, String transactionType, BigDecimal amount) {
        CommissionRule rule = commissionRuleMapper.findByCardTypeAndType(cardTypeId, transactionType);
        if (rule == null) {
            return BigDecimal.ZERO;
        }
        
        // 优先使用固定金额
        if (rule.getFixedAmount() != null) {
            return rule.getFixedAmount();
        }
        
        // 使用百分比计算
        if (rule.getCommissionRate() != null) {
            return amount.multiply(rule.getCommissionRate())
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        }
        
        return BigDecimal.ZERO;
    }
    
    public List<CommissionRule> getAllRules() {
        return commissionRuleMapper.findAll();
    }
    
    public CommissionRule getRule(Long id) {
        return commissionRuleMapper.findById(id);
    }
    
    public CommissionRule createRule(CommissionRule rule) {
        // 验证：至少需要fixedAmount或commissionRate之一
        if (rule.getFixedAmount() == null && rule.getCommissionRate() == null) {
            throw new RuntimeException("必须设置固定金额或提成比例之一");
        }
        commissionRuleMapper.insert(rule);
        return rule;
    }
    
    public CommissionRule updateRule(CommissionRule rule) {
        // 验证：至少需要fixedAmount或commissionRate之一
        if (rule.getFixedAmount() == null && rule.getCommissionRate() == null) {
            throw new RuntimeException("必须设置固定金额或提成比例之一");
        }
        commissionRuleMapper.update(rule);
        return commissionRuleMapper.findById(rule.getId());
    }
    
    public void deleteRule(Long id) {
        commissionRuleMapper.deleteById(id);
    }
}
