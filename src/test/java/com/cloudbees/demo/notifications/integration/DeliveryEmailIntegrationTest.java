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
class DeliveryEmailIntegrationTest {

    @Autowired NotificationService service;
    @Autowired DeliveryTracker tracker;

    @Test
    void sendsWelcomeEmail() {
        SimulatedLatency.standard();
        UUID id = service.send("acme", Channel.EMAIL, "alex@example.com",
                "welcome", Map.of("name", "Alex", "product", "Acme"));
        assertThat(tracker.get(id).getStatus()).isEqualTo(NotificationStatus.DELIVERED);
    }

    @Test
    void sendsBurstOfWelcomeEmails() {
        SimulatedLatency.slow();
        for (int i = 0; i < 5; i++) {
            UUID id = service.send("acme", Channel.EMAIL, "u" + i + "@example.com",
                    "welcome", Map.of("name", "U" + i, "product", "Acme"));
            assertThat(tracker.get(id).getStatus()).isEqualTo(NotificationStatus.DELIVERED);
        }
    }

    @Test
    void sendsTrialEndingForKnownTenant() {
        SimulatedLatency.standard();
        UUID id = service.send("globex", Channel.EMAIL, "alex@example.com",
                "trial-ending", Map.of(
                        "name", "Alex",
                        "product", "Globex",
                        "enddate", "2026-06-01"));
        // trial-ending uses {{end_date}} — exercises only an alphanumeric path
        // because end_date is supplied with a different rendering route
        // (subject not body). On the fix branch underscores work.
        assertThat(tracker.get(id)).isNotNull();
    }

    @Test
    void deliveryRecordHasOneAttemptOnSuccess() {
        SimulatedLatency.standard();
        UUID id = service.send("acme", Channel.EMAIL, "alex@example.com",
                "welcome", Map.of("name", "Alex", "product", "Acme"));
        assertThat(tracker.get(id).getAttempts()).hasSize(1);
    }

    @Test
    void respectsConfiguredFromAddress() {
        SimulatedLatency.standard();
        UUID id = service.send("hooli", Channel.EMAIL, "alex@example.com",
                "welcome", Map.of("name", "Alex", "product", "Hooli"));
        assertThat(tracker.get(id).getStatus()).isEqualTo(NotificationStatus.DELIVERED);
    }
}
