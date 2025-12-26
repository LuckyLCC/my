package com.gym.my.mapper;

import com.gym.my.entity.Employee;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface EmployeeMapper {
    
    @Select("SELECT * FROM employee WHERE username = #{username}")
    Employee findByUsername(String username);
    
    @Select("SELECT * FROM employee WHERE id = #{id}")
    Employee findById(Long id);
    
    @Select("SELECT * FROM employee ORDER BY id DESC")
    List<Employee> findAll();
    
    @Insert("INSERT INTO employee (username, password, name, phone, role, status, " +
            "member_create, member_read, member_update, member_delete) " +
            "VALUES (#{username}, #{password}, #{name}, #{phone}, #{role}, #{status}, " +
            "#{memberCreate}, #{memberRead}, #{memberUpdate}, #{memberDelete})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Employee employee);
    
    @Update("UPDATE employee SET name = #{name}, phone = #{phone}, role = #{role}, " +
            "status = #{status}, member_create = #{memberCreate}, member_read = #{memberRead}, " +
            "member_update = #{memberUpdate}, member_delete = #{memberDelete} WHERE id = #{id}")
    int update(Employee employee);
    
    @Update("UPDATE employee SET password = #{password} WHERE id = #{id}")
    int updatePassword(@Param("id") Long id, @Param("password") String password);
    
    @Delete("DELETE FROM employee WHERE id = #{id}")
    int deleteById(Long id);
    
    /**
     * 检查员工是否被会员记录引用
     * @param employeeId 员工ID
     * @return 引用该员工的会员数量
     */
    @Select("SELECT COUNT(*) FROM member WHERE first_employee_id = #{employeeId} OR last_employee_id = #{employeeId}")
    int countMembersByEmployeeId(@Param("employeeId") Long employeeId);
}
