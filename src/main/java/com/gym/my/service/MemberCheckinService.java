package com.gym.my.service;

import com.gym.my.entity.MemberCheckin;
import com.gym.my.mapper.MemberCheckinMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberCheckinService {
    
    private final MemberCheckinMapper checkinMapper;
    
    /**
     * 会员签到
     */
    @Transactional
    public MemberCheckin checkin(Long memberId, Long employeeId, String remark) {
        LocalDate today = LocalDate.now();
        
        // 检查今天是否已经签到
        int count = checkinMapper.countByMemberIdAndDate(memberId, today);
        if (count > 0) {
            throw new RuntimeException("今天已经签到过了");
        }
        
        MemberCheckin checkin = new MemberCheckin();
        checkin.setMemberId(memberId);
        checkin.setCheckinDate(today);
        checkin.setCheckinTime(LocalDateTime.now());
        checkin.setEmployeeId(employeeId);
        checkin.setRemark(remark);
        
        checkinMapper.insert(checkin);
        return checkin;
    }
    
    /**
     * 获取会员的签到记录
     */
    public List<MemberCheckin> getCheckinHistory(Long memberId) {
        return checkinMapper.findByMemberId(memberId);
    }
    
    /**
     * 获取会员今天的签到状态
     */
    public boolean hasCheckedInToday(Long memberId) {
        LocalDate today = LocalDate.now();
        return checkinMapper.countByMemberIdAndDate(memberId, today) > 0;
    }
}

