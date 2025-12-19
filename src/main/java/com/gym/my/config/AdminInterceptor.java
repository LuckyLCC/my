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
        
        // GET 请求到 /api/employees 允许所有已登录用户访问（用于会员管理显示员工姓名）
        String path = request.getRequestURI();
        if ("GET".equals(request.getMethod()) && "/api/employees".equals(path)) {
            return true; // 允许所有已登录用户查看员工列表
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
