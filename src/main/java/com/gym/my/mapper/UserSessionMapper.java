package com.gym.my.mapper;

import com.gym.my.entity.UserSession;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;

@Mapper
public interface UserSessionMapper {
    
    @Select("SELECT * FROM user_session WHERE token = #{token} AND expires_at > NOW()")
    UserSession findByToken(String token);
    
    @Insert("INSERT INTO user_session (employee_id, token, expires_at) " +
            "VALUES (#{employeeId}, #{token}, #{expiresAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserSession session);
    
    @Update("UPDATE user_session SET expires_at = #{expiresAt} WHERE token = #{token}")
    int updateExpiration(@Param("token") String token, @Param("expiresAt") LocalDateTime expiresAt);
    
    @Delete("DELETE FROM user_session WHERE token = #{token}")
    int deleteByToken(String token);
    
    @Delete("DELETE FROM user_session WHERE expires_at < NOW()")
    int deleteExpired();
    
    @Delete("DELETE FROM user_session WHERE employee_id = #{employeeId}")
    int deleteByEmployeeId(Long employeeId);
    
    @Select("SELECT * FROM user_session WHERE employee_id = #{employeeId} LIMIT 1")
    UserSession findByEmployeeId(Long employeeId);
    
    @Update("UPDATE user_session SET token = #{token}, expires_at = #{expiresAt} WHERE employee_id = #{employeeId}")
    int updateByEmployeeId(@Param("employeeId") Long employeeId, @Param("token") String token, @Param("expiresAt") LocalDateTime expiresAt);
    
    // 使用INSERT ... ON DUPLICATE KEY UPDATE避免死锁
    @Insert("INSERT INTO user_session (employee_id, token, expires_at) " +
            "VALUES (#{employeeId}, #{token}, #{expiresAt}) " +
            "ON DUPLICATE KEY UPDATE token = #{token}, expires_at = #{expiresAt}")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertOrUpdate(UserSession session);
}
