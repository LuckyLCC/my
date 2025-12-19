package com.gym.my.service;

import com.gym.my.entity.Employee;
import com.gym.my.entity.UserSession;
import com.gym.my.mapper.EmployeeMapper;
import com.gym.my.mapper.UserSessionMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final EmployeeMapper employeeMapper;
    private final UserSessionMapper sessionMapper;
    
    // JWT密钥，生产环境应该从配置文件读取
    private static final String JWT_SECRET = "gym-management-system-secret-key-should-be-at-least-256-bits-long-for-security";
    private static final int SESSION_TIMEOUT_MINUTES = 30;
    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * 用户登录
     */
    @Transactional
    public Map<String, Object> login(String username, String password) {
        Employee employee = employeeMapper.findByUsername(username);
        if (employee == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        if (employee.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }
        
        // 密码验证：兼容明文/BCrypt（schema.sql 里的 admin 默认是 BCrypt）
        String stored = employee.getPassword();
        boolean ok;
        if (stored != null && stored.startsWith("$2a$")) {
            ok = PASSWORD_ENCODER.matches(password, stored);
        } else {
            ok = password != null && password.equals(stored);
        }
        if (!ok) throw new RuntimeException("用户名或密码错误");
        
        // 生成JWT Token
        String token = generateToken(employee);
        
        // 保存Session到数据库
        UserSession session = new UserSession();
        session.setEmployeeId(employee.getId());
        session.setToken(token);
        session.setExpiresAt(LocalDateTime.now().plusMinutes(SESSION_TIMEOUT_MINUTES));
        sessionMapper.insert(session);
        
        // 返回登录信息
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("employeeId", employee.getId());
        result.put("username", employee.getUsername());
        result.put("name", employee.getName());
        result.put("role", employee.getRole());
        result.put("expiresIn", SESSION_TIMEOUT_MINUTES * 60); // 秒
        // 返回权限信息
        result.put("memberCreate", employee.getMemberCreate() != null && employee.getMemberCreate() == 1);
        result.put("memberRead", employee.getMemberRead() != null && employee.getMemberRead() == 1);
        result.put("memberUpdate", employee.getMemberUpdate() != null && employee.getMemberUpdate() == 1);
        result.put("memberDelete", employee.getMemberDelete() != null && employee.getMemberDelete() == 1);
        
        return result;
    }
    
    /**
     * 生成JWT Token
     */
    private String generateToken(Employee employee) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("employeeId", employee.getId());
        claims.put("username", employee.getUsername());
        claims.put("role", employee.getRole());
        
        Date now = new Date();
        Date expiration = new Date(now.getTime() + SESSION_TIMEOUT_MINUTES * 60 * 1000);
        
        return Jwts.builder()
                .claims(claims)
                .subject(employee.getUsername())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();
    }
    
    /**
     * 验证Token并刷新过期时间
     */
    @Transactional
    public Employee validateAndRefreshToken(String token) {
        // 从数据库查询Session
        UserSession session = sessionMapper.findByToken(token);
        if (session == null) {
            throw new RuntimeException("无效的Token或已过期");
        }
        
        // 刷新过期时间
        session.setExpiresAt(LocalDateTime.now().plusMinutes(SESSION_TIMEOUT_MINUTES));
        sessionMapper.updateExpiration(token, session.getExpiresAt());
        
        // 返回员工信息
        return employeeMapper.findById(session.getEmployeeId());
    }
    
    /**
     * 登出
     */
    @Transactional
    public void logout(String token) {
        sessionMapper.deleteByToken(token);
    }
    
    /**
     * 清理过期Session
     */
    @Transactional
    public void cleanExpiredSessions() {
        sessionMapper.deleteExpired();
    }
}
