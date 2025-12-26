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
        // 如果前端明确传递了权限值（包括0），就使用该值；如果为null，才使用默认值
        if (employee.getMemberCreate() == null) {
            // 创建权限：默认所有员工都有
            employee.setMemberCreate(1);
        }
        if (employee.getMemberRead() == null) {
            // 查看权限：默认所有员工都有（这是基本权限）
            employee.setMemberRead(1);
        }
        if (employee.getMemberUpdate() == null) {
            // 更新权限：ADMIN默认有，STAFF默认无
            employee.setMemberUpdate("ADMIN".equals(employee.getRole()) ? 1 : 0);
        }
        if (employee.getMemberDelete() == null) {
            // 删除权限：ADMIN默认有，STAFF默认无
            employee.setMemberDelete("ADMIN".equals(employee.getRole()) ? 1 : 0);
        }
        
        // ADMIN角色自动拥有所有权限（即使前端传递了0，也要强制为1）
        if ("ADMIN".equals(employee.getRole())) {
            employee.setMemberCreate(1);
            employee.setMemberRead(1);
            employee.setMemberUpdate(1);
            employee.setMemberDelete(1);
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
        
        // 更新时，如果前端明确传递了权限值（包括0），就使用该值；如果为null，保持原值不变
        // ADMIN角色自动拥有所有权限（即使前端传递了0，也要强制为1）
        if ("ADMIN".equals(employee.getRole())) {
            employee.setMemberCreate(1);
            employee.setMemberRead(1);
            employee.setMemberUpdate(1);
            employee.setMemberDelete(1);
        } else {
            // 非ADMIN员工：如果权限字段为null，保持数据库中的原值（不更新）
            // 如果前端明确传递了值（包括0），就使用该值
            // 这里不需要强制设置，因为前端已经传递了明确的值
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
