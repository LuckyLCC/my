package com.gym.my.controller;

import com.gym.my.dto.ApiResponse;
import com.gym.my.entity.Employee;
import com.gym.my.service.EmployeeService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {
    
    private final EmployeeService employeeService;
    
    @GetMapping
    public ApiResponse<List<Employee>> getAllEmployees() {
        try {
            List<Employee> employees = employeeService.getAllEmployees();
            // 不返回密码
            employees.forEach(e -> e.setPassword(null));
            return ApiResponse.success(employees);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    public ApiResponse<Employee> getEmployee(@PathVariable Long id) {
        try {
            Employee employee = employeeService.getEmployee(id);
            if (employee == null) {
                return ApiResponse.error("员工不存在");
            }
            employee.setPassword(null);
            return ApiResponse.success(employee);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping
    public ApiResponse<Employee> createEmployee(@RequestBody Employee employee, HttpServletRequest request) {
        // 检查管理员权限
        Employee currentEmployee = (Employee) request.getAttribute("currentEmployee");
        if (currentEmployee == null || !"ADMIN".equals(currentEmployee.getRole())) {
            return ApiResponse.error("权限不足，仅管理员可创建员工");
        }
        try {
            Employee created = employeeService.createEmployee(employee);
            created.setPassword(null);
            return ApiResponse.success("创建成功", created);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public ApiResponse<Employee> updateEmployee(@PathVariable Long id, @RequestBody Employee employee, HttpServletRequest request) {
        // 检查管理员权限
        Employee currentEmployee = (Employee) request.getAttribute("currentEmployee");
        if (currentEmployee == null || !"ADMIN".equals(currentEmployee.getRole())) {
            return ApiResponse.error("权限不足，仅管理员可更新员工");
        }
        try {
            employee.setId(id);
            Employee updated = employeeService.updateEmployee(employee);
            updated.setPassword(null);
            return ApiResponse.success("更新成功", updated);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteEmployee(@PathVariable Long id, HttpServletRequest request) {
        // 检查管理员权限
        Employee currentEmployee = (Employee) request.getAttribute("currentEmployee");
        if (currentEmployee == null || !"ADMIN".equals(currentEmployee.getRole())) {
            return ApiResponse.error("权限不足，仅管理员可删除员工");
        }
        try {
            employeeService.deleteEmployee(id);
            return ApiResponse.success("删除成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
