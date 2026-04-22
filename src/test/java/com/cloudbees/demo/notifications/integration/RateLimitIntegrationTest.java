package com.cloudbees.demo.notifications.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.cloudbees.demo.notifications.core.Channel;
import com.cloudbees.demo.notifications.core.NotificationService;
import com.cloudbees.demo.notifications.tenants.RateLimitExceededException;
import com.cloudbees.demo.notifications.tenants.RateLimiter;
import com.cloudbees.demo.notifications.tenants.Tenant;
import com.cloudbees.demo.notifications.tenants.TenantService;

@SpringBootTest
@Tag("integration")
class RateLimitIntegrationTest {

    @Autowired NotificationService service;
    @Autowired RateLimiter limiter;
    @Autowired TenantService tenants;

    @Test
    void allowsBurstUpToLimit() {
        SimulatedLatency.slow();
        tenants.put(new Tenant("rl1", "RL1", 3, new Tenant.DeliveryConfig(3, 100L)));
        limiter.reset("rl1");
        for (int i = 0; i < 3; i++) {
            service.send("rl1", Channel.EMAIL, "alex@example.com",
                    "welcome", Map.of("name", "Alex", "product", "Acme"));
        }
        assertThat(limiter.countInWindow("rl1")).isEqualTo(3);
    }

    @Test
    void rejectsOverLimit() {
        SimulatedLatency.slow();
        tenants.put(new Tenant("rl2", "RL2", 2, new Tenant.DeliveryConfig(3, 100L)));
        limiter.reset("rl2");
        service.send("rl2", Channel.EMAIL, "alex@example.com",
                "welcome", Map.of("name", "Alex", "product", "Acme"));
        service.send("rl2", Channel.EMAIL, "alex@example.com",
                "welcome", Map.of("name", "Alex", "product", "Acme"));
        assertThatThrownBy(() -> service.send("rl2", Channel.EMAIL, "alex@example.com",
                "welcome", Map.of("name", "Alex", "product", "Acme")))
                .isInstanceOf(RateLimitExceededException.class);
    }

    @Test
    void tenantsAreIndependent() {
        SimulatedLatency.slow();
        tenants.put(new Tenant("rl3", "RL3", 2, new Tenant.DeliveryConfig(3, 100L)));
        tenants.put(new Tenant("rl4", "RL4", 2, new Tenant.DeliveryConfig(3, 100L)));
        limiter.reset("rl3"); limiter.reset("rl4");
        service.send("rl3", Channel.EMAIL, "a@b.com",
                "welcome", Map.of("name", "A", "product", "X"));
        assertThat(limiter.countInWindow("rl4")).isZero();
    }
}
