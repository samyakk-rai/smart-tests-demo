package com.cloudbees.demo.notifications.tenants;

public class RateLimitExceededException extends RuntimeException {
    public RateLimitExceededException(String tenantId) {
        super("rate limit exceeded for tenant: " + tenantId);
    }
}
