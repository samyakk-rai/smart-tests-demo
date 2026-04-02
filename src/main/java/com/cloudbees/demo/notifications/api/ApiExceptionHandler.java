package com.cloudbees.demo.notifications.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.cloudbees.demo.notifications.channels.ChannelException;
import com.cloudbees.demo.notifications.templates.TemplateNotFoundException;
import com.cloudbees.demo.notifications.templates.TemplateRenderException;
import com.cloudbees.demo.notifications.tenants.RateLimitExceededException;
import com.cloudbees.demo.notifications.tenants.TenantNotFoundException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(TenantNotFoundException.class)
    public ResponseEntity<ErrorResponse> tenantNotFound(TenantNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("tenant_not_found", e.getMessage()));
    }

    @ExceptionHandler(TemplateNotFoundException.class)
    public ResponseEntity<ErrorResponse> templateNotFound(TemplateNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("template_not_found", e.getMessage()));
    }

    @ExceptionHandler(TemplateRenderException.class)
    public ResponseEntity<ErrorResponse> templateRender(TemplateRenderException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("template_render_error", e.getMessage()));
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> rateLimit(RateLimitExceededException e) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(new ErrorResponse("rate_limit_exceeded", e.getMessage()));
    }

    @ExceptionHandler(ChannelException.class)
    public ResponseEntity<ErrorResponse> channel(ChannelException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("channel_error", e.getMessage()));
    }
}
