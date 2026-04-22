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
class TemplateRenderingIntegrationTest {

    @Autowired NotificationService service;
    @Autowired DeliveryTracker tracker;

    @Test
    void rendersWelcomeTemplateEndToEnd() {
        SimulatedLatency.standard();
        UUID id = service.send("acme", Channel.EMAIL, "alex@example.com",
                "welcome", Map.of("name", "Alex", "product", "Acme"));
        assertThat(tracker.get(id).getStatus()).isEqualTo(NotificationStatus.DELIVERED);
    }

    @Test
    void rendersWelcomeAcrossChannels() {
        SimulatedLatency.standard();
        UUID e = service.send("acme", Channel.EMAIL, "alex@example.com",
                "welcome", Map.of("name", "Alex", "product", "Acme"));
        UUID s = service.send("acme", Channel.SMS, "+14155551212",
                "welcome", Map.of("name", "Alex", "product", "Acme"));
        UUID w = service.send("acme", Channel.WEBHOOK, "https://hooks.example.com/x",
                "welcome", Map.of("name", "Alex", "product", "Acme"));
        assertThat(tracker.get(e).getStatus()).isEqualTo(NotificationStatus.DELIVERED);
        assertThat(tracker.get(s).getStatus()).isEqualTo(NotificationStatus.DELIVERED);
        assertThat(tracker.get(w).getStatus()).isEqualTo(NotificationStatus.DELIVERED);
    }

    @Test
    void rendersWelcomeForMultipleTenants() {
        SimulatedLatency.standard();
        for (String t : new String[]{"acme", "globex", "hooli"}) {
            UUID id = service.send(t, Channel.EMAIL, "alex@example.com",
                    "welcome", Map.of("name", "Alex", "product", t));
            assertThat(tracker.get(id).getStatus()).isEqualTo(NotificationStatus.DELIVERED);
        }
    }

    @Test
    void rendersTrialEndingTemplate() {
        SimulatedLatency.standard();
        UUID id = service.send("acme", Channel.EMAIL, "alex@example.com",
                "trial-ending", Map.of(
                        "name", "Alex",
                        "product", "Acme",
                        "enddate", "2026-06-01"));
        assertThat(tracker.get(id).getStatus()).isEqualTo(NotificationStatus.DELIVERED);
    }

    // ============================================================
    // Failing test on main — order-confirmation template uses
    // {{order_id}}, which the v1.4.0 escaping tightening dropped
    // from the variable regex. Renders fail with
    // TemplateRenderException → the send aborts at template render
    // before any channel delivery happens.
    // ============================================================

    @Test
    void sendsOrderConfirmationEndToEnd() {
        SimulatedLatency.standard();
        UUID id = service.send("acme", Channel.EMAIL, "alex@example.com",
                "order-confirmation", Map.of(
                        "name", "Alex",
                        "order_id", "1234",
                        "amount", "$42.00"));
        assertThat(tracker.get(id).getStatus()).isEqualTo(NotificationStatus.DELIVERED);
    }
}
