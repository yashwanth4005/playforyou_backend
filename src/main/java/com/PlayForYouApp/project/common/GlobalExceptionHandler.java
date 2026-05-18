package com.PlayForYouApp.project.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.PlayForYouApp.project.dto.common.ApiResponse;
import com.PlayForYouApp.project.exception.ApiException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Object>> handleApiException(ApiException ex) {
        ApiResponse<Object> response = new ApiResponse<>((ex.getStatus().is2xxSuccessful() ? "success" : "error"), null,
                ex.getMessage());
        return ResponseEntity.status(ex.getStatus()).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleAnyException(Exception ex) {
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage() == null ? "Unexpected error" : ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
