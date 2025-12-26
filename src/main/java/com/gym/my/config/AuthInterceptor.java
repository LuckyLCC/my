package com.gym.my.config;

import com.gym.my.entity.Employee;
import com.gym.my.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {
    
    private final AuthService authService;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // OPTIONS请求直接放行
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }
        
        // 获取Token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        if (token == null || token.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未登录或登录已过期\"}");
            return false;
        }
        
        try {
            // 验证Token并刷新过期时间
            AuthService.TokenValidationResult result = authService.validateAndRefreshToken(token);
            Employee employee = result.getEmployee();
            
            // 如果Token被刷新，将新Token存入request属性，供ResponseBodyAdvice使用
            if (result.isTokenRefreshed()) {
                request.setAttribute("newToken", result.getNewToken());
                // 同时在响应头中也返回，方便前端处理
                response.setHeader("X-New-Token", result.getNewToken());
            }
            
            // 将用户信息存入request
            request.setAttribute("currentEmployee", employee);
            return true;
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"" + e.getMessage() + "\"}");
            return false;
        }
    }
}
