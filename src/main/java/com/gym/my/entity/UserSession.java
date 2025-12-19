package com.gym.my.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserSession {
    private Long id;
    private Long employeeId;
    private String token;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
}
