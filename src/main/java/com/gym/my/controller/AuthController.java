package com.gym.my.controller;

import com.gym.my.dto.ApiResponse;
import com.gym.my.dto.LoginRequest;
import com.gym.my.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@Validated @RequestBody LoginRequest request) {
        try {
            Map<String, Object> result = authService.login(request.getUsername(), request.getPassword());
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                authService.logout(token);
            }
            return ApiResponse.success("退出成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
