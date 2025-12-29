package com.gym.my.util;

import com.gym.my.entity.Employee;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 权限工具类
 */
public class PermissionUtil {
    
    /**
     * 检查员工是否有会员创建权限
     */
    public static boolean hasMemberCreatePermission(Employee employee) {
        if (employee == null) return false;
        // ADMIN拥有所有权限
        if ("ADMIN".equals(employee.getRole())) {
            return true;
        }
        return employee.getMemberCreate() != null && employee.getMemberCreate() == 1;
    }
    
    /**
     * 检查员工是否有会员查看权限
     */
    public static boolean hasMemberReadPermission(Employee employee) {
        if (employee == null) return false;
        // ADMIN拥有所有权限
        if ("ADMIN".equals(employee.getRole())) {
            return true;
        }
        return employee.getMemberRead() != null && employee.getMemberRead() == 1;
    }
    
    /**
     * 检查员工是否有会员更新权限
     */
    public static boolean hasMemberUpdatePermission(Employee employee) {
        if (employee == null) return false;
        // ADMIN拥有所有权限
        if ("ADMIN".equals(employee.getRole())) {
            return true;
        }
        return employee.getMemberUpdate() != null && employee.getMemberUpdate() == 1;
    }
    
    /**
     * 检查员工是否有会员删除权限
     */
    public static boolean hasMemberDeletePermission(Employee employee) {
        if (employee == null) return false;
        // ADMIN拥有所有权限
        if ("ADMIN".equals(employee.getRole())) {
            return true;
        }
        return employee.getMemberDelete() != null && employee.getMemberDelete() == 1;
    }
    
    /**
     * 检查员工是否为管理员
     */
    public static boolean isAdmin(Employee employee) {
        if (employee == null) return false;
        return "ADMIN".equals(employee.getRole());
    }
    
    /**
     * 从request中获取当前员工
     */
    public static Employee getCurrentEmployee(HttpServletRequest request) {
        return (Employee) request.getAttribute("currentEmployee");
    }
}

