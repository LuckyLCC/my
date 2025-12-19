package com.gym.my.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    
    private final AuthInterceptor authInterceptor;
    private final AdminInterceptor adminInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 认证拦截器 - 排除登录接口
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login");
        
        // 权限拦截器 - 仅管理员接口
        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/api/employees/**", "/api/commission-rules/**");
    }
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
