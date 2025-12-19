package com.gym.my.config;

import com.gym.my.entity.Employee;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // OPTIONS请求直接放行（CORS预检请求）
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }
        
        Employee employee = (Employee) request.getAttribute("currentEmployee");
        
        if (employee == null || !"ADMIN".equals(employee.getRole())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":403,\"message\":\"权限不足，仅管理员可访问\"}");
            return false;
        }
        
        return true;
    }
}
