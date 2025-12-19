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
    private Integer memberCreate; // 会员创建权限：1-有权限，0-无权限
    private Integer memberRead; // 会员查看权限：1-有权限，0-无权限
    private Integer memberUpdate; // 会员更新权限：1-有权限，0-无权限
    private Integer memberDelete; // 会员删除权限：1-有权限，0-无权限
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
