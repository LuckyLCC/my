package com.gym.my.task;

import com.gym.my.service.AuthService;
import com.gym.my.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledTasks {
    
    private final MemberService memberService;
    private final AuthService authService;
    
    /**
     * 每天凌晨1点检查并更新过期会员
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void updateExpiredMembers() {
        log.info("开始执行定时任务：更新过期会员");
        try {
            memberService.updateExpiredMembers();
            log.info("定时任务执行成功：更新过期会员");
        } catch (Exception e) {
            log.error("定时任务执行失败：更新过期会员", e);
        }
    }
    
    /**
     * 每天凌晨1点检查并激活未生效卡种
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void activatePendingCards() {
        log.info("开始执行定时任务：激活未生效卡种");
        try {
            memberService.activatePendingCards();
            log.info("定时任务执行成功：激活未生效卡种");
        } catch (Exception e) {
            log.error("定时任务执行失败：激活未生效卡种", e);
        }
    }
    
    /**
     * 每小时清理过期的Session
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void cleanExpiredSessions() {
        log.info("开始执行定时任务：清理过期Session");
        try {
            authService.cleanExpiredSessions();
            log.info("定时任务执行成功：清理过期Session");
        } catch (Exception e) {
            log.error("定时任务执行失败：清理过期Session", e);
        }
    }
}
