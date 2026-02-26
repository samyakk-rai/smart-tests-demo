package com.cloudbees.demo.notifications.tenants;

public class Tenant {
    private final String id;
    private final String name;
    private final int ratePerMinute;
    private final DeliveryConfig deliveryConfig;

    public Tenant(String id, String name, int ratePerMinute, DeliveryConfig deliveryConfig) {
        this.id = id;
        this.name = name;
        this.ratePerMinute = ratePerMinute;
        this.deliveryConfig = deliveryConfig;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getRatePerMinute() { return ratePerMinute; }
    public DeliveryConfig getDeliveryConfig() { return deliveryConfig; }

    /** Per-tenant overrides for delivery (rolling out — may be null). */
    public static class DeliveryConfig {
        private final Integer maxAttempts;
        private final Long initialBackoffMs;

        public DeliveryConfig(Integer maxAttempts, Long initialBackoffMs) {
            this.maxAttempts = maxAttempts;
            this.initialBackoffMs = initialBackoffMs;
        }

        public Integer getMaxAttempts() { return maxAttempts; }
        public Long getInitialBackoffMs() { return initialBackoffMs; }
    }
}
