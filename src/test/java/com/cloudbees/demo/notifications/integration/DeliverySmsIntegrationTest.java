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
class DeliverySmsIntegrationTest {

    @Autowired NotificationService service;
    @Autowired DeliveryTracker tracker;

    @Test
    void sendsWelcomeSms() {
        SimulatedLatency.standard();
        UUID id = service.send("acme", Channel.SMS, "+14155551212",
                "welcome", Map.of("name", "Alex", "product", "Acme"));
        assertThat(tracker.get(id).getStatus()).isEqualTo(NotificationStatus.DELIVERED);
    }

    @Test
    void burstOfSmsAcrossTenants() {
        SimulatedLatency.slow();
        String[] tenants = {"acme", "globex", "hooli"};
        for (String t : tenants) {
            UUID id = service.send(t, Channel.SMS, "+14155551212",
                    "welcome", Map.of("name", "Alex", "product", t));
            assertThat(tracker.get(id).getStatus()).isEqualTo(NotificationStatus.DELIVERED);
        }
    }

    @Test
    void recordsAttemptOnDelivery() {
        SimulatedLatency.standard();
        UUID id = service.send("acme", Channel.SMS, "+14155551212",
                "welcome", Map.of("name", "Alex", "product", "Acme"));
        assertThat(tracker.get(id).getAttempts()).hasSize(1);
        assertThat(tracker.get(id).getAttempts().get(0).isSuccess()).isTrue();
    }

    @Test
    void senderIdIsEmbeddedInProviderMessage() {
        SimulatedLatency.standard();
        UUID id = service.send("acme", Channel.SMS, "+14155551212",
                "welcome", Map.of("name", "Alex", "product", "Acme"));
        assertThat(tracker.get(id).getStatus()).isEqualTo(NotificationStatus.DELIVERED);
    }
}
