package com.PlayForYouApp.project.common;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.PlayForYouApp.project.dto.common.ApiResponse;

@ControllerAdvice
public class StandardResponseAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        Class<?> returnTypeClass = returnType.getParameterType();
        if (ResponseEntity.class.isAssignableFrom(returnTypeClass)) {
            return false;
        }
        String returnTypeName = returnTypeClass.getSimpleName();
        return !returnTypeName.contains("Resource") && !returnTypeName.contains("ResourceRegion");
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
            ServerHttpResponse response) {

        if (body instanceof ApiResponse || body == null) {
            return body;
        }

        return ApiResponse.success(body);
    }
}
