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
}
