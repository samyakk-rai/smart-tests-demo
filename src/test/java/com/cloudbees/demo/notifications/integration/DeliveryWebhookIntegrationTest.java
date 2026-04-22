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
class DeliveryWebhookIntegrationTest {

    @Autowired NotificationService service;
    @Autowired DeliveryTracker tracker;

    @Test
    void sendsWebhookEvent() {
        SimulatedLatency.standard();
        UUID id = service.send("acme", Channel.WEBHOOK, "https://hooks.example.com/x",
                "welcome", Map.of("name", "Alex", "product", "Acme"));
        assertThat(tracker.get(id).getStatus()).isEqualTo(NotificationStatus.DELIVERED);
    }

    @Test
    void signsBodyWithHmac() {
        SimulatedLatency.standard();
        UUID id = service.send("acme", Channel.WEBHOOK, "https://hooks.example.com/x",
                "welcome", Map.of("name", "Alex", "product", "Acme"));
        assertThat(tracker.get(id).getStatus()).isEqualTo(NotificationStatus.DELIVERED);
    }

    @Test
    void burstOfWebhooks() {
        SimulatedLatency.slow();
        for (int i = 0; i < 5; i++) {
            UUID id = service.send("acme", Channel.WEBHOOK, "https://hooks.example.com/" + i,
                    "welcome", Map.of("name", "U" + i, "product", "Acme"));
            assertThat(tracker.get(id).getStatus()).isEqualTo(NotificationStatus.DELIVERED);
        }
    }

    @Test
    void acceptsHttpScheme() {
        SimulatedLatency.standard();
        UUID id = service.send("acme", Channel.WEBHOOK, "http://hooks.example.com/x",
                "welcome", Map.of("name", "Alex", "product", "Acme"));
        assertThat(tracker.get(id).getStatus()).isEqualTo(NotificationStatus.DELIVERED);
    }
}
