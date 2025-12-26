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
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
        member.setStartDate(request.getStartDate()); // 开始日期（首次开卡日期）
        member.setFirstCardDate(request.getStartDate()); // 首次开卡日期（与startDate保持一致）
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
        record.setTransactionDate(LocalDateTime.now()); // 交易日期时间（当前时间精确到秒）
        record.setStartDate(request.getStartDate()); // 卡开始日期
        record.setExpireDate(expireDate); // 卡到期日期
        
        transactionRecordMapper.insert(record);
        
        return memberMapper.findById(member.getId());
    }
    
    /**
     * 会员续费
     */
    @Transactional
    public Member renewMember(RenewRequest request) {
        log.info("开始执行续卡，会员ID: {}, 卡种ID: {}, 续卡日期: {}", 
            request.getMemberId(), request.getCardTypeId(), request.getRenewDate());
        
        Member member = memberMapper.findById(request.getMemberId());
        if (member == null) {
            log.error("续卡失败：会员不存在，会员ID: {}", request.getMemberId());
            throw new RuntimeException("会员不存在");
        }
        
        log.info("找到会员，姓名: {}, 当前到期日期: {}, 未生效卡种开始日期: {}", 
            member.getName(), member.getExpireDate(), member.getPendingCardStartDate());
        
        CardType cardType = cardTypeMapper.findById(request.getCardTypeId());
        if (cardType == null) {
            throw new RuntimeException("卡种不存在");
        }
        
        // 总有效期显示策略：列表页展示会员当前的"有效权益周期"
        LocalDate currentExpireDate = member.getExpireDate();
        LocalDate currentStartDate = member.getStartDate();
        LocalDate newStartDate;
        LocalDate newExpireDate;
        
        // 交易记录中该笔订单的实际时间范围
        LocalDate orderStartDate;
        LocalDate orderExpireDate;
        
        // 续卡日期表示"新卡开始生效的日期"
        // 判断逻辑：
        // - 如果续卡日期 > 当前到期日期+1天，说明是中断续卡（卡已过期后重新办卡）
        // - 如果续卡日期 <= 当前到期日期+1天（包括续卡日期 = 当前到期日期+1天），说明是提前续卡（顺延逻辑）
        if (currentExpireDate != null && request.getRenewDate().isAfter(currentExpireDate.plusDays(1))) {
            // 场景二：卡已过期，重新办卡（重置逻辑）
            // 续卡日期 > 当前到期日期+1天，说明是中断续卡
            // 开始日期 = 续卡日期，到期日期 = 续卡日期 + 新购时长
            newStartDate = request.getRenewDate();
            newExpireDate = calculateExpireDate(newStartDate, cardType);
            
            // 交易记录：保存该笔订单的实际时间范围
            orderStartDate = newStartDate;
            orderExpireDate = newExpireDate;
        } else {
            // 场景一：卡在有效期内续卡（顺延逻辑）
            // 续卡日期 <= 当前到期日期+1天（包括续卡日期 = 当前到期日期+1天，这是正常的提前续卡）
            // 开始日期保持不变，到期日期 = 原到期日期 + 新购时长
            newStartDate = currentStartDate; // 保持不变
            
            // 从原到期日期开始加新购时长
            if (currentExpireDate != null) {
                newExpireDate = addDurationToDate(currentExpireDate, cardType);
            } else {
                // 如果没有当前到期日期（理论上不应该发生），从续卡日期开始计算
                newStartDate = request.getRenewDate();
                newExpireDate = calculateExpireDate(newStartDate, cardType);
            }
            
            // 交易记录：该笔订单从续卡日期开始生效（续卡日期就是新卡开始生效的日期）
            if (currentExpireDate != null) {
                orderStartDate = request.getRenewDate(); // 订单开始日期 = 续卡日期（新卡开始生效的日期）
                orderExpireDate = calculateExpireDate(orderStartDate, cardType); // 订单到期日期
            } else {
                orderStartDate = request.getRenewDate();
                orderExpireDate = newExpireDate;
            }
        }
        
        // 更新会员信息
        LocalDate today = LocalDate.now();
        boolean isFutureRenewal = request.getRenewDate().isAfter(today);
        log.info("续卡日期判断，续卡日期: {}, 今天: {}, 是否为未来续卡: {}", 
            request.getRenewDate(), today, isFutureRenewal);
        
        // 判断是否是修改未生效卡种（之前已有未生效卡种，且新续卡日期也是未来日期）
        boolean isModifyingPendingCard = isFutureRenewal && member.getPendingCardStartDate() != null;
        LocalDate oldPendingCardStartDate = isModifyingPendingCard ? member.getPendingCardStartDate() : null; // 保存旧的未生效卡种开始日期，用于查找对应的交易记录
        
        if (isFutureRenewal) {
            // 特殊情况：续卡日期是未来日期（未生效的续卡）
            // 例如：顾客指定"下个月1号才开始生效"
            // 保存到未生效卡种字段，不更新当前的开始日期和到期日期
            // 注意：如果会员之前已有未生效卡种，这里会直接覆盖（支持改卡功能）
            member.setPendingCardStartDate(request.getRenewDate()); // 未生效卡种开始日期（覆盖之前的）
            member.setPendingCardTypeId(cardType.getId()); // 未生效卡种ID（覆盖之前的，支持从月卡改为季卡）
            member.setPendingCardExpireDate(calculateExpireDate(request.getRenewDate(), cardType)); // 未生效卡种到期日期（覆盖之前的）
            
            // 保持当前的开始日期和到期日期不变
            // member.setStartDate() 和 member.setExpireDate() 不需要更新
            
            // 过期状态判断：使用当前的到期日期
            int isExpired = (member.getExpireDate() != null && member.getExpireDate().isBefore(today)) ? 1 : 0;
            member.setIsExpired(isExpired);
        } else {
            // 正常情况：续卡日期不是未来日期，立即生效
            // 更新当前的开始日期和到期日期
            member.setCardTypeId(cardType.getId());
            member.setStartDate(newStartDate); // 更新开始日期（场景一保持不变，场景二更新为续卡日期）
            member.setExpireDate(newExpireDate); // 更新到期日期
            
            // 清空未生效卡种字段（如果之前有未生效卡种，现在立即生效了，所以清空）
            member.setPendingCardStartDate(null);
            member.setPendingCardTypeId(null);
            member.setPendingCardExpireDate(null);
            
            int isExpired = newExpireDate.isBefore(today) ? 1 : 0;
            member.setIsExpired(isExpired);
        }
        
        member.setLastEmployeeId(request.getEmployeeId());
        
        // 次卡：只在非未来续卡时增加次数（未来续卡时，等激活后再增加）
        if ("TIMES".equals(cardType.getType()) && !isFutureRenewal) {
            // 次卡：增加次数
            int currentTimes = member.getRemainingTimes() != null ? member.getRemainingTimes() : 0;
            member.setRemainingTimes(currentTimes + cardType.getDuration());
        }
        
        memberMapper.update(member);
        
        // 记录未生效卡种信息（用于调试）
        if (isFutureRenewal) {
            log.info("续卡后设置未生效卡种，会员ID: {}, 开始日期: {}, 卡种ID: {}, 到期日期: {}", 
                member.getId(), member.getPendingCardStartDate(), member.getPendingCardTypeId(), member.getPendingCardExpireDate());
        }
        
        if (isModifyingPendingCard) {
            // 修改未生效卡种：更新之前对应的交易记录
            // 查找之前未生效卡种对应的交易记录（通过startDate匹配）
            TransactionRecord oldRecord = transactionRecordMapper.findByMemberIdAndStartDate(
                    member.getId(), oldPendingCardStartDate);
            
            if (oldRecord != null) {
                // 更新交易记录：卡种、金额、开始日期、到期日期
                BigDecimal commissionAmount = commissionService.calculateCommission(
                        cardType.getId(), "RENEW", cardType.getPrice());
                
                oldRecord.setCardTypeId(cardType.getId());
                oldRecord.setAmount(cardType.getPrice());
                oldRecord.setCommissionAmount(commissionAmount);
                oldRecord.setStartDate(orderStartDate); // 新的开始日期
                oldRecord.setExpireDate(orderExpireDate); // 新的到期日期
                if (request.getRemark() != null && !request.getRemark().isEmpty()) {
                    oldRecord.setRemark(request.getRemark());
                }
                
                transactionRecordMapper.update(oldRecord);
                log.info("已更新未生效卡种对应的交易记录，会员ID: {}, 旧开始日期: {}, 新卡种ID: {}", 
                    member.getId(), oldPendingCardStartDate, cardType.getId());
            } else {
                // 如果没有找到对应的交易记录，创建新的交易记录（兼容旧数据）
                log.warn("未找到未生效卡种对应的交易记录，会员ID: {}, 开始日期: {}，将创建新记录", 
                    member.getId(), oldPendingCardStartDate);
                BigDecimal commissionAmount = commissionService.calculateCommission(
                        cardType.getId(), "RENEW", cardType.getPrice());
                
                TransactionRecord record = new TransactionRecord();
                record.setMemberId(member.getId());
                record.setCardTypeId(cardType.getId());
                record.setTransactionType("RENEW");
                record.setAmount(cardType.getPrice());
                record.setCommissionAmount(commissionAmount);
                record.setEmployeeId(request.getEmployeeId());
                record.setTransactionDate(LocalDateTime.now());
                record.setStartDate(orderStartDate);
                record.setExpireDate(orderExpireDate);
                record.setRemark(request.getRemark());
                
                transactionRecordMapper.insert(record);
            }
        } else {
            // 创建新的交易记录
            BigDecimal commissionAmount = commissionService.calculateCommission(
                    cardType.getId(), "RENEW", cardType.getPrice());
            
            TransactionRecord record = new TransactionRecord();
            record.setMemberId(member.getId());
            record.setCardTypeId(cardType.getId());
            record.setTransactionType("RENEW");
            record.setAmount(cardType.getPrice());
            record.setCommissionAmount(commissionAmount);
            record.setEmployeeId(request.getEmployeeId());
            record.setTransactionDate(LocalDateTime.now()); // 交易日期时间（当前时间精确到秒）
            record.setStartDate(orderStartDate); // 该笔订单的实际开始日期
            record.setExpireDate(orderExpireDate); // 该笔订单的实际到期日期
            record.setRemark(request.getRemark());
            
            transactionRecordMapper.insert(record);
        }
        
        // 重新查询会员信息，确保返回最新的未生效卡种信息
        Member updatedMember = memberMapper.findById(member.getId());
        if (updatedMember != null && isFutureRenewal) {
            log.info("续卡后查询会员信息，会员ID: {}, 未生效卡种开始日期: {}, 卡种ID: {}, 到期日期: {}", 
                updatedMember.getId(), updatedMember.getPendingCardStartDate(), 
                updatedMember.getPendingCardTypeId(), updatedMember.getPendingCardExpireDate());
        }
        return updatedMember;
    }
    
    /**
     * 计算到期日期（从开始日期计算）
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
    
    /**
     * 从指定日期开始加时长（用于顺延续卡：在原到期日期基础上加新购时长）
     */
    private LocalDate addDurationToDate(LocalDate baseDate, CardType cardType) {
        switch (cardType.getType()) {
            case "MONTH":
                return baseDate.plusMonths(cardType.getDuration());
            case "QUARTER":
                return baseDate.plusMonths(cardType.getDuration());
            case "YEAR":
                return baseDate.plusYears(cardType.getDuration());
            case "TIMES":
                // 次卡有效期默认1年
                return baseDate.plusYears(1);
            default:
                throw new RuntimeException("未知的卡种类型");
        }
    }
    
    public List<Member> getAllMembers(String name, String phone, Integer isExpired, Integer expireWithinDays) {
        // 如果查询涉及过期状态筛选，先更新过期会员状态（确保数据一致性）
        // 这样可以确保筛选结果准确，即使定时任务还没运行
        if (isExpired != null || expireWithinDays != null) {
            memberMapper.updateExpiredMembers();
        }
        // 然后查询
        List<Member> members = memberMapper.findAll(name, phone, isExpired);
        
        // 如果指定了"X天内到期"筛选，进一步过滤
        if (expireWithinDays != null) {
            LocalDate today = LocalDate.now();
            LocalDate targetDate = today.plusDays(expireWithinDays);
            members = members.stream()
                    .filter(m -> {
                        if (m.getExpireDate() == null) return false;
                        LocalDate expireDate = m.getExpireDate();
                        // 筛选：到期日期在今天和targetDate之间（包括今天和targetDate）
                        return !expireDate.isBefore(today) && !expireDate.isAfter(targetDate);
                    })
                    .collect(Collectors.toList());
        }
        
        return members;
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
    
    /**
     * 激活未生效的卡种（定时任务调用）
     * 检查所有未生效卡种的开始日期，如果 <= 今天，则激活该卡种
     */
    @Transactional
    public void activatePendingCards() {
        List<Member> membersToActivate = memberMapper.findMembersWithPendingCardsToActivate();
        
        for (Member member : membersToActivate) {
            if (member.getPendingCardStartDate() != null 
                && member.getPendingCardTypeId() != null 
                && member.getPendingCardExpireDate() != null) {
                
                // 如果是次卡，需要在激活前计算需要增加的次数
                CardType pendingCardType = cardTypeMapper.findById(member.getPendingCardTypeId());
                Integer newRemainingTimes = null;
                
                if (pendingCardType != null && "TIMES".equals(pendingCardType.getType())) {
                    // 次卡：需要增加次数
                    int currentTimes = member.getRemainingTimes() != null ? member.getRemainingTimes() : 0;
                    newRemainingTimes = currentTimes + pendingCardType.getDuration();
                }
                
                // 激活未生效的卡种
                memberMapper.activatePendingCard(member);
                
                // 如果是次卡，更新剩余次数
                if (newRemainingTimes != null) {
                    memberMapper.updateRemainingTimes(member.getId(), newRemainingTimes);
                }
                
                log.info("会员 {} (ID: {}) 的未生效卡种已激活，开始日期: {}, 到期日期: {}", 
                    member.getName(), member.getId(), 
                    member.getPendingCardStartDate(), 
                    member.getPendingCardExpireDate());
            }
        }
        
        if (!membersToActivate.isEmpty()) {
            log.info("共激活 {} 个未生效的卡种", membersToActivate.size());
        }
    }
}
