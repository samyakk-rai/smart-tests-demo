package com.cloudbees.demo.notifications.tenants;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RateLimiterUnitTest {

    private TenantService tenants;
    private RateLimiter limiter;

    @BeforeEach
    void setUp() {
        tenants = new TenantService();
        // Slot a low-rate tenant for the test.
        tenants.put(new Tenant("ratetest", "RateTest", 3,
                new Tenant.DeliveryConfig(3, 100L)));
        limiter = new RateLimiter(tenants);
    }

    @Test
    void allowsUpToLimit() {
        for (int i = 0; i < 3; i++) limiter.check("ratetest");
        assertThat(limiter.countInWindow("ratetest")).isEqualTo(3);
    }

    @Test
    void throwsOverLimit() {
        for (int i = 0; i < 3; i++) limiter.check("ratetest");
        assertThatThrownBy(() -> limiter.check("ratetest"))
                .isInstanceOf(RateLimitExceededException.class);
    }

    @Test
    void resetClearsCounter() {
        limiter.check("ratetest");
        limiter.check("ratetest");
        limiter.reset("ratetest");
        assertThat(limiter.countInWindow("ratetest")).isZero();
    }

    @Test
    void unknownTenantThrowsTenantNotFound() {
        assertThatThrownBy(() -> limiter.check("nope"))
                .isInstanceOf(TenantNotFoundException.class);
    }

    @Test
    void countInWindowZeroForFreshTenant() {
        assertThat(limiter.countInWindow("acme")).isZero();
    }

    @Test
    void separateTenantsTrackedIndependently() {
        limiter.check("ratetest");
        assertThat(limiter.countInWindow("acme")).isZero();
        assertThat(limiter.countInWindow("ratetest")).isEqualTo(1);
    }
}
