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
    
    @Select("SELECT * FROM employee WHERE status = 1")
    List<Employee> findAll();
    
    @Insert("INSERT INTO employee (username, password, name, phone, role, status) " +
            "VALUES (#{username}, #{password}, #{name}, #{phone}, #{role}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Employee employee);
    
    @Update("UPDATE employee SET name = #{name}, phone = #{phone}, role = #{role}, " +
            "status = #{status} WHERE id = #{id}")
    int update(Employee employee);
    
    @Update("UPDATE employee SET password = #{password} WHERE id = #{id}")
    int updatePassword(@Param("id") Long id, @Param("password") String password);
    
    @Delete("DELETE FROM employee WHERE id = #{id}")
    int deleteById(Long id);
}
