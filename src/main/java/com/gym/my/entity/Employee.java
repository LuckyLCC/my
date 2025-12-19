package com.gym.my.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Employee {
    private Long id;
    private String username;
    private String password;
    private String name;
    private String phone;
    private String role; // ADMIN, STAFF
    private Integer status; // 1-启用，0-禁用
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
