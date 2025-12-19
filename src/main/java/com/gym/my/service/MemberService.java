package com.gym.my.service;

import com.gym.my.dto.MemberRequest;
import com.gym.my.dto.RenewRequest;
import com.gym.my.entity.CardType;
import com.gym.my.entity.Member;
import com.gym.my.entity.TransactionRecord;
import com.gym.my.mapper.CardTypeMapper;
import com.gym.my.mapper.MemberMapper;
import com.gym.my.mapper.TransactionRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {
    
    private final MemberMapper memberMapper;
    private final CardTypeMapper cardTypeMapper;
    private final TransactionRecordMapper transactionRecordMapper;
    private final CommissionService commissionService;
    
    /**
     * 新增会员（开卡）
     */
    @Transactional
    public Member createMember(MemberRequest request) {
        CardType cardType = cardTypeMapper.findById(request.getCardTypeId());
        if (cardType == null) {
            throw new RuntimeException("卡种不存在");
        }
        
        // 计算到期日期
        LocalDate expireDate = calculateExpireDate(request.getStartDate(), cardType);
        
        // 检查是否已过期
        LocalDate today = LocalDate.now();
        int isExpired = expireDate.isBefore(today) ? 1 : 0;
        
        // 创建会员
        Member member = new Member();
        member.setName(request.getName());
        member.setGender(request.getGender());
        member.setPhone(request.getPhone());
        member.setIdCard(request.getIdCard());
        member.setCardTypeId(request.getCardTypeId());
        member.setStartDate(request.getStartDate());
        member.setExpireDate(expireDate);
        member.setRemainingTimes(request.getRemainingTimes());
        member.setIsExpired(isExpired);
        member.setFirstEmployeeId(request.getEmployeeId());
        member.setLastEmployeeId(request.getEmployeeId());
        member.setStatus(1);
        
        memberMapper.insert(member);
        
        // 创建交易记录
        BigDecimal commissionAmount = commissionService.calculateCommission(
                cardType.getId(), "NEW", cardType.getPrice());
        
        TransactionRecord record = new TransactionRecord();
        record.setMemberId(member.getId());
        record.setCardTypeId(cardType.getId());
        record.setTransactionType("NEW");
        record.setAmount(cardType.getPrice());
        record.setCommissionAmount(commissionAmount);
        record.setEmployeeId(request.getEmployeeId());
        record.setTransactionDate(request.getStartDate());
        
        transactionRecordMapper.insert(record);
        
        return memberMapper.findById(member.getId());
    }
    
    /**
     * 会员续费
     */
    @Transactional
    public Member renewMember(RenewRequest request) {
        Member member = memberMapper.findById(request.getMemberId());
        if (member == null) {
            throw new RuntimeException("会员不存在");
        }
        
        CardType cardType = cardTypeMapper.findById(request.getCardTypeId());
        if (cardType == null) {
            throw new RuntimeException("卡种不存在");
        }
        
        // 计算新的到期日期（从原到期日或当前日期开始）
        LocalDate baseDate = member.getExpireDate().isAfter(request.getRenewDate()) 
                ? member.getExpireDate() 
                : request.getRenewDate();
        LocalDate newExpireDate = calculateExpireDate(baseDate, cardType);
        
        // 更新会员信息
        LocalDate today = LocalDate.now();
        int isExpired = newExpireDate.isBefore(today) ? 1 : 0;
        member.setCardTypeId(cardType.getId());
        member.setExpireDate(newExpireDate);
        member.setIsExpired(isExpired);
        member.setLastEmployeeId(request.getEmployeeId());
        
        if ("TIMES".equals(cardType.getType())) {
            // 次卡：增加次数
            int currentTimes = member.getRemainingTimes() != null ? member.getRemainingTimes() : 0;
            member.setRemainingTimes(currentTimes + cardType.getDuration());
        }
        
        memberMapper.update(member);
        
        // 创建交易记录
        BigDecimal commissionAmount = commissionService.calculateCommission(
                cardType.getId(), "RENEW", cardType.getPrice());
        
        TransactionRecord record = new TransactionRecord();
        record.setMemberId(member.getId());
        record.setCardTypeId(cardType.getId());
        record.setTransactionType("RENEW");
        record.setAmount(cardType.getPrice());
        record.setCommissionAmount(commissionAmount);
        record.setEmployeeId(request.getEmployeeId());
        record.setTransactionDate(request.getRenewDate());
        record.setRemark(request.getRemark());
        
        transactionRecordMapper.insert(record);
        
        return memberMapper.findById(member.getId());
    }
    
    /**
     * 计算到期日期
     */
    private LocalDate calculateExpireDate(LocalDate startDate, CardType cardType) {
        switch (cardType.getType()) {
            case "MONTH":
                return startDate.plusMonths(cardType.getDuration());
            case "QUARTER":
                return startDate.plusMonths(cardType.getDuration());
            case "YEAR":
                return startDate.plusYears(cardType.getDuration());
            case "TIMES":
                // 次卡有效期默认1年
                return startDate.plusYears(1);
            default:
                throw new RuntimeException("未知的卡种类型");
        }
    }
    
    public List<Member> getAllMembers(String name, String phone, Integer isExpired) {
        // 如果查询涉及过期状态筛选，先更新过期会员状态（确保数据一致性）
        // 这样可以确保筛选结果准确，即使定时任务还没运行
        if (isExpired != null) {
            memberMapper.updateExpiredMembers();
        }
        // 然后查询
        return memberMapper.findAll(name, phone, isExpired);
    }
    
    public Member getMember(Long id) {
        return memberMapper.findById(id);
    }
    
    @Transactional
    public Member updateMember(MemberRequest request) {
        Member member = memberMapper.findById(request.getId());
        if (member == null) {
            throw new RuntimeException("会员不存在");
        }
        
        member.setName(request.getName());
        member.setGender(request.getGender());
        member.setPhone(request.getPhone());
        member.setIdCard(request.getIdCard());
        
        memberMapper.update(member);
        return memberMapper.findById(member.getId());
    }
    
    @Transactional
    public void deleteMember(Long id) {
        memberMapper.deleteById(id);
    }
    
    /**
     * 更新过期会员状态
     */
    @Transactional
    public void updateExpiredMembers() {
        memberMapper.updateExpiredMembers();
    }
    
    /**
     * 获取会员续卡历史（包含首次办卡和续卡记录）
     */
    public List<TransactionRecord> getRenewHistory(Long memberId) {
        // 返回该会员的所有交易记录（NEW和RENEW），按时间倒序排列
        return transactionRecordMapper.findByMemberId(memberId);
    }
}
