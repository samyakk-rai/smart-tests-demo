package com.cloudbees.demo.notifications.delivery;

import org.springframework.stereotype.Component;

import com.cloudbees.demo.notifications.config.NotificationsProperties;
import com.cloudbees.demo.notifications.tenants.Tenant;

/**
 * Computes retry backoff for a delivery attempt. Honours per-tenant overrides
 * for {@code maxAttempts} and {@code initialBackoffMs}, falling back to the
 * service defaults from {@link NotificationsProperties}.
 *
 * <p>Backoff is exponential: {@code initial * 2^(attempt-1)}, capped at
 * {@code maxBackoffMs}.
 */
@Component
public class RetryPolicy {

    private final NotificationsProperties properties;

    public RetryPolicy(NotificationsProperties properties) {
        this.properties = properties;
    }

    public int maxAttempts(Tenant tenant) {
        Tenant.DeliveryConfig cfg = tenant.getDeliveryConfig();
        if (cfg != null && cfg.getMaxAttempts() != null) {
            return cfg.getMaxAttempts();
        }
        return properties.getDelivery().getMaxAttempts();
    }

    public long calculateBackoffMs(Tenant tenant, int attemptNumber) {
        if (attemptNumber < 1) {
            throw new IllegalArgumentException("attemptNumber must be >= 1");
        }
        Tenant.DeliveryConfig cfg = tenant.getDeliveryConfig();
        long initial = (cfg != null && cfg.getInitialBackoffMs() != null)
                ? cfg.getInitialBackoffMs()
                : properties.getDelivery().getInitialBackoffMs();
        long backoff = initial * (long) Math.pow(2, attemptNumber - 1);
        return Math.min(backoff, properties.getDelivery().getMaxBackoffMs());
    }

    public boolean shouldRetry(Tenant tenant, int attemptNumber) {
        return attemptNumber < maxAttempts(tenant);
    }
}
