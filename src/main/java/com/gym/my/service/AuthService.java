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
        
        // 使用更安全的方式更新Session，避免死锁
        // 先尝试使用INSERT ... ON DUPLICATE KEY UPDATE（如果唯一约束存在）
        // 如果失败，回退到先删除再插入的方式
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(SESSION_TIMEOUT_MINUTES);
        UserSession session = new UserSession();
        session.setEmployeeId(employee.getId());
        session.setToken(token);
        session.setExpiresAt(expiresAt);
        
        try {
            // 先尝试使用insertOrUpdate（需要唯一约束）
            sessionMapper.insertOrUpdate(session);
        } catch (Exception e) {
            // 如果失败（可能是唯一约束不存在或死锁），使用删除+插入方式
            // 添加重试机制处理可能的死锁
            int maxRetries = 3;
            boolean success = false;
            for (int i = 0; i < maxRetries && !success; i++) {
                try {
                    // 删除该员工的所有旧Session（使用LIMIT避免锁定太多记录）
                    sessionMapper.deleteByEmployeeId(employee.getId());
                    // 插入新Session
                    sessionMapper.insert(session);
                    success = true;
                } catch (Exception ex) {
                    if (i < maxRetries - 1) {
                        // 等待一小段时间后重试
                        try {
                            Thread.sleep(50 * (i + 1)); // 递增等待时间
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException("登录失败，请稍后重试", ie);
                        }
                    } else {
                        throw new RuntimeException("登录失败，请稍后重试", ex);
                    }
                }
            }
        }
        
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
        if (token == null || token.isEmpty()) {
            throw new RuntimeException("Token不能为空");
        }
        
        // 首先验证JWT token的签名和过期时间
        try {
            io.jsonwebtoken.Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            
            // 检查token是否过期（JWT库会自动检查，但我们可以额外验证）
            Date expiration = claims.getExpiration();
            if (expiration != null && expiration.before(new Date())) {
                throw new RuntimeException("Token已过期");
            }
            
            // 从数据库查询Session，确保token在有效session列表中
            UserSession session = sessionMapper.findByToken(token);
            if (session == null) {
                throw new RuntimeException("无效的Token或Session已失效");
            }
            
            // 检查数据库中的session是否过期
            if (session.getExpiresAt() != null && session.getExpiresAt().isBefore(LocalDateTime.now())) {
                // Session已过期，删除它
                sessionMapper.deleteByToken(token);
                throw new RuntimeException("Session已过期，请重新登录");
            }
            
            // 刷新过期时间
            session.setExpiresAt(LocalDateTime.now().plusMinutes(SESSION_TIMEOUT_MINUTES));
            sessionMapper.updateExpiration(token, session.getExpiresAt());
            
            // 返回员工信息
            Employee employee = employeeMapper.findById(session.getEmployeeId());
            if (employee == null) {
                throw new RuntimeException("用户不存在");
            }
            
            // 检查员工状态
            if (employee.getStatus() == 0) {
                throw new RuntimeException("账号已被禁用");
            }
            
            return employee;
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            // JWT token已过期，清理数据库session
            sessionMapper.deleteByToken(token);
            throw new RuntimeException("Token已过期，请重新登录");
        } catch (io.jsonwebtoken.security.SignatureException | io.jsonwebtoken.MalformedJwtException e) {
            throw new RuntimeException("无效的Token签名");
        } catch (io.jsonwebtoken.UnsupportedJwtException e) {
            throw new RuntimeException("不支持的Token格式");
        } catch (io.jsonwebtoken.JwtException e) {
            throw new RuntimeException("Token验证失败：" + e.getMessage());
        } catch (RuntimeException e) {
            // 重新抛出业务异常
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Token验证失败：" + e.getMessage());
        }
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
