package com.gym.my.service;

import com.gym.my.entity.Employee;
import com.gym.my.mapper.EmployeeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    
    private final EmployeeMapper employeeMapper;
    
    public List<Employee> getAllEmployees() {
        return employeeMapper.findAll();
    }
    
    public Employee getEmployee(Long id) {
        return employeeMapper.findById(id);
    }
    
    @Transactional
    public Employee createEmployee(Employee employee) {
        // 检查用户名是否已存在
        Employee existing = employeeMapper.findByUsername(employee.getUsername());
        if (existing != null) {
            throw new RuntimeException("用户名已存在");
        }
        
        if (employee.getStatus() == null) {
            employee.setStatus(1);
        }
        
        // 设置默认权限：ADMIN拥有所有权限，STAFF默认只有查看和创建权限
        // 注意：如果前端提交的是0（switch关闭），我们也需要确保基本权限（创建和查看）至少为1
        if (employee.getMemberCreate() == null || employee.getMemberCreate() == 0) {
            // 创建权限：默认所有员工都有
            employee.setMemberCreate(1);
        }
        if (employee.getMemberRead() == null || employee.getMemberRead() == 0) {
            // 查看权限：默认所有员工都有（这是基本权限，不能为0）
            employee.setMemberRead(1);
        }
        if (employee.getMemberUpdate() == null) {
            employee.setMemberUpdate("ADMIN".equals(employee.getRole()) ? 1 : 0);
        }
        if (employee.getMemberDelete() == null) {
            employee.setMemberDelete("ADMIN".equals(employee.getRole()) ? 1 : 0);
        }
        
        employeeMapper.insert(employee);
        return employee;
    }
    
    @Transactional
    public Employee updateEmployee(Employee employee) {
        Employee existing = employeeMapper.findById(employee.getId());
        if (existing == null) {
            throw new RuntimeException("员工不存在");
        }
        
        // 更新时，确保基本权限（创建和查看）不能为0（除非是ADMIN，ADMIN有特殊处理）
        if (!"ADMIN".equals(employee.getRole())) {
            if (employee.getMemberRead() == null || employee.getMemberRead() == 0) {
                // 非ADMIN员工的查看权限不能为0
                employee.setMemberRead(1);
            }
            // 创建权限也建议至少为1
            if (employee.getMemberCreate() == null || employee.getMemberCreate() == 0) {
                employee.setMemberCreate(1);
            }
        }
        
        employeeMapper.update(employee);
        return employeeMapper.findById(employee.getId());
    }
    
    @Transactional
    public void deleteEmployee(Long id) {
        // 检查员工是否存在
        Employee employee = employeeMapper.findById(id);
        if (employee == null) {
            throw new RuntimeException("员工不存在");
        }
        
        // 检查是否有会员记录关联到此员工
        int memberCount = employeeMapper.countMembersByEmployeeId(id);
        if (memberCount > 0) {
            throw new RuntimeException("无法删除：该员工已被 " + memberCount + " 条会员记录关联（首次开卡或最新续卡）。请先处理相关会员记录后再删除。");
        }
        
        // 执行删除
        employeeMapper.deleteById(id);
    }
    
    @Transactional
    public void updatePassword(Long id, String newPassword) {
        employeeMapper.updatePassword(id, newPassword);
    }
}
