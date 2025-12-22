package com.gym.my.controller;

import com.gym.my.dto.ApiResponse;
import com.gym.my.dto.LoginRequest;
import com.gym.my.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@Validated @RequestBody LoginRequest request) {
        try {
            Map<String, Object> result = authService.login(request.getUsername(), request.getPassword());
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (RuntimeException e) {
            // 登录失败返回401状态码
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "登录失败：" + e.getMessage()));
        }
    }
    
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentUser(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            if (token == null || token.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error(401, "未登录或登录已过期"));
            }
            
            // 验证token并获取用户信息
            com.gym.my.entity.Employee employee = authService.validateAndRefreshToken(token);
            
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("employeeId", employee.getId());
            result.put("username", employee.getUsername());
            result.put("name", employee.getName());
            result.put("role", employee.getRole());
            result.put("memberCreate", employee.getMemberCreate() != null && employee.getMemberCreate() == 1);
            result.put("memberRead", employee.getMemberRead() != null && employee.getMemberRead() == 1);
            result.put("memberUpdate", employee.getMemberUpdate() != null && employee.getMemberUpdate() == 1);
            result.put("memberDelete", employee.getMemberDelete() != null && employee.getMemberDelete() == 1);
            
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取用户信息失败：" + e.getMessage()));
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
