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
        
        employeeMapper.insert(employee);
        return employee;
    }
    
    @Transactional
    public Employee updateEmployee(Employee employee) {
        Employee existing = employeeMapper.findById(employee.getId());
        if (existing == null) {
            throw new RuntimeException("员工不存在");
        }
        
        employeeMapper.update(employee);
        return employeeMapper.findById(employee.getId());
    }
    
    @Transactional
    public void deleteEmployee(Long id) {
        employeeMapper.deleteById(id);
    }
    
    @Transactional
    public void updatePassword(Long id, String newPassword) {
        employeeMapper.updatePassword(id, newPassword);
    }
}
