package com.gym.my.config;

import com.gym.my.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 响应体增强器：在响应体中添加新Token（如果Token被刷新）
 */
@Slf4j
@ControllerAdvice
public class TokenRefreshResponseAdvice implements ResponseBodyAdvice<Object> {
    
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // 只处理返回ApiResponse的接口
        return ApiResponse.class.isAssignableFrom(returnType.getParameterType());
    }
    
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        
        if (body instanceof ApiResponse) {
            ApiResponse<?> apiResponse = (ApiResponse<?>) body;
            
            // 从请求中获取新Token（由AuthInterceptor设置）
            if (request instanceof ServletServerHttpRequest) {
                ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
                HttpServletRequest httpRequest = servletRequest.getServletRequest();
                String newToken = (String) httpRequest.getAttribute("newToken");
                
                if (newToken != null && !newToken.isEmpty()) {
                    // 如果响应体是Map类型，添加新Token
                    if (apiResponse.getData() instanceof java.util.Map) {
                        @SuppressWarnings("unchecked")
                        java.util.Map<String, Object> dataMap = (java.util.Map<String, Object>) apiResponse.getData();
                        dataMap.put("token", newToken);
                        dataMap.put("tokenRefreshed", true);
                        log.debug("Token已刷新，新Token已添加到响应体");
                    }
                }
            }
        }
        
        return body;
    }
}

