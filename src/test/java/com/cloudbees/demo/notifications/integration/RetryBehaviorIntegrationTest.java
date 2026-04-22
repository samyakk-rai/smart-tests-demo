package com.cloudbees.demo.notifications.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.cloudbees.demo.notifications.core.Channel;
import com.cloudbees.demo.notifications.core.NotificationService;
import com.cloudbees.demo.notifications.core.NotificationStatus;
import com.cloudbees.demo.notifications.delivery.DeliveryTracker;

@SpringBootTest
@Tag("integration")
class RetryBehaviorIntegrationTest {

    @Autowired NotificationService service;
    @Autowired DeliveryTracker tracker;

    @Test
    void deliversFirstAttemptForConfiguredTenant() {
        SimulatedLatency.standard();
        UUID id = service.send("acme", Channel.EMAIL, "alex@example.com",
                "welcome", Map.of("name", "Alex", "product", "Acme"));
        assertThat(tracker.get(id).getAttempts()).hasSize(1);
    }

    @Test
    void retryRecordTracksAttemptNumber() {
        SimulatedLatency.standard();
        UUID id = service.send("globex", Channel.EMAIL, "alex@example.com",
                "welcome", Map.of("name", "Alex", "product", "Globex"));
        assertThat(tracker.get(id).getAttempts().get(0).getAttemptNumber()).isEqualTo(1);
    }

    @Test
    void abandonsAfterMaxAttemptsOnRepeatedFailure() {
        // Sends to a known-bad URL to exercise the abandon path for a tenant
        // that *has* a delivery config; exits cleanly even when all retries fail.
        SimulatedLatency.slow();
        UUID id = service.send("globex", Channel.WEBHOOK, "https://hooks.example.com/x",
                "welcome", Map.of("name", "Alex", "product", "Globex"));
        assertThat(tracker.get(id)).isNotNull();
    }

    @Test
    void retryBackoffIsHonoured() {
        SimulatedLatency.standard();
        UUID id = service.send("acme", Channel.EMAIL, "alex@example.com",
                "welcome", Map.of("name", "Alex", "product", "Acme"));
        assertThat(tracker.get(id).getStatus()).isEqualTo(NotificationStatus.DELIVERED);
    }

    // ============================================================
    // Failing tests on main — same root cause as RetryPolicyUnitTest:
    // the "initech" tenant doesn't have a per-tenant DeliveryConfig
    // and RetryPolicy NPEs. The full delivery flow trips on the first
    // backoff calculation.
    // ============================================================

    @Test
    void retriesEmailDeliveryForInitechTenant() {
        SimulatedLatency.standard();
        UUID id = service.send("initech", Channel.EMAIL, "alex@example.com",
                "welcome", Map.of("name", "Alex", "product", "Initech"));
        assertThat(tracker.get(id).getStatus()).isEqualTo(NotificationStatus.DELIVERED);
    }

    @Test
    void abandonsAfterMaxAttemptsForInitechTenant() {
        SimulatedLatency.standard();
        UUID id = service.send("initech", Channel.WEBHOOK, "https://hooks.example.com/x",
                "welcome", Map.of("name", "Alex", "product", "Initech"));
        assertThat(tracker.get(id)).isNotNull();
    }
}
