package com.cloudbees.demo.notifications.tenants;

public class TenantNotFoundException extends RuntimeException {
    public TenantNotFoundException(String id) {
        super("tenant not found: " + id);
    }
}
