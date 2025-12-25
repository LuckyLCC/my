package com.gym.my;

import com.gym.my.entity.Employee;
import com.gym.my.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetEmployees() throws Exception {
        Employee employee1 = new Employee();
        employee1.setId(1L);
        employee1.setUsername("admin");
        employee1.setName("管理员");
        employee1.setRole("ADMIN");
        employee1.setStatus(1);

        List<Employee> employees = Arrays.asList(employee1);

        when(employeeService.getEmployees()).thenReturn(employees);

        mockMvc.perform(get("/api/employees")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].username").value("admin"));
    }

    @Test
    void testCreateEmployee() throws Exception {
        Employee request = new Employee();
        request.setUsername("testuser");
        request.setPassword("testpass");
        request.setName("测试用户");
        request.setRole("STAFF");
        request.setStatus(1);

        Employee savedEmployee = new Employee();
        savedEmployee.setId(2L);
        savedEmployee.setUsername("testuser");
        savedEmployee.setName("测试用户");
        savedEmployee.setRole("STAFF");
        savedEmployee.setStatus(1);

        when(employeeService.createEmployee(any(Employee.class))).thenReturn(savedEmployee);

        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    @Test
    void testUpdateEmployee() throws Exception {
        Employee request = new Employee();
        request.setName("测试用户更新");
        request.setRole("STAFF");
        request.setStatus(1);

        Employee updatedEmployee = new Employee();
        updatedEmployee.setId(1L);
        updatedEmployee.setUsername("testuser");
        updatedEmployee.setName("测试用户更新");
        updatedEmployee.setRole("STAFF");
        updatedEmployee.setStatus(1);

        when(employeeService.updateEmployee(eq(1L), any(Employee.class))).thenReturn(updatedEmployee);

        mockMvc.perform(put("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("测试用户更新"));
    }

    @Test
    void testDeleteEmployee() throws Exception {
        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}