package com.cloudbees.demo.notifications.delivery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.cloudbees.demo.notifications.config.NotificationsProperties;
import com.cloudbees.demo.notifications.tenants.Tenant;

class RetryPolicyUnitTest {

    private RetryPolicy policy;

    private final Tenant configured =
            new Tenant("acme", "Acme", 120, new Tenant.DeliveryConfig(5, 250L));

    // Tenant whose per-tenant DeliveryConfig hasn't been provisioned yet
    // (the v1.4.x rollout is gradual — see CHANGELOG).
    private final Tenant unconfigured =
            new Tenant("initech", "Initech", 30, null);

    @BeforeEach
    void setUp() {
        policy = new RetryPolicy(new NotificationsProperties());
    }

    @Test
    void maxAttemptsForConfiguredTenant() {
        assertThat(policy.maxAttempts(configured)).isEqualTo(5);
    }

    @Test
    void calculateBackoffMsAttemptOne() {
        assertThat(policy.calculateBackoffMs(configured, 1)).isEqualTo(250L);
    }

    @Test
    void calculateBackoffMsAttemptTwoIsDoubled() {
        assertThat(policy.calculateBackoffMs(configured, 2)).isEqualTo(500L);
    }

    @Test
    void calculateBackoffMsCapsAtMax() {
        // After enough doublings we cap at maxBackoffMs from properties (30s).
        assertThat(policy.calculateBackoffMs(configured, 10)).isEqualTo(30_000L);
    }

    @Test
    void shouldRetryWhileUnderMaxAttempts() {
        assertThat(policy.shouldRetry(configured, 1)).isTrue();
        assertThat(policy.shouldRetry(configured, 4)).isTrue();
        assertThat(policy.shouldRetry(configured, 5)).isFalse();
    }

    @Test
    void rejectsAttemptNumberLessThanOne() {
        assertThatThrownBy(() -> policy.calculateBackoffMs(configured, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ============================================================
    // Failing tests on main — root cause: RetryPolicy assumes
    // tenant.getDeliveryConfig() is always non-null. For tenants
    // where the per-tenant override isn't provisioned yet
    // (e.g. "initech"), every call throws NullPointerException.
    // ============================================================

    @Test
    void maxAttemptsForTenantWithoutOverride() {
        // Should fall back to the service default (5).
        assertThat(policy.maxAttempts(unconfigured)).isEqualTo(5);
    }

    @Test
    void calculateBackoffForTenantWithoutOverride() {
        // Should fall back to the service default initial backoff (250ms).
        assertThat(policy.calculateBackoffMs(unconfigured, 1)).isEqualTo(250L);
    }

    @Test
    void shouldRetryForTenantWithoutOverride() {
        assertThat(policy.shouldRetry(unconfigured, 1)).isTrue();
    }
}
