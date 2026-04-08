package com.cloudbees.demo.notifications.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.cloudbees.demo.notifications.audit.AuditEventType;
import com.cloudbees.demo.notifications.audit.AuditLog;
import com.cloudbees.demo.notifications.channels.ChannelAdapter;
import com.cloudbees.demo.notifications.channels.DeliveryResult;
import com.cloudbees.demo.notifications.channels.EmailChannel;
import com.cloudbees.demo.notifications.channels.SmsChannel;
import com.cloudbees.demo.notifications.channels.WebhookChannel;
import com.cloudbees.demo.notifications.config.NotificationsProperties;
import com.cloudbees.demo.notifications.delivery.DeliveryTracker;
import com.cloudbees.demo.notifications.delivery.RetryPolicy;
import com.cloudbees.demo.notifications.templates.TemplateEngine;
import com.cloudbees.demo.notifications.templates.TemplateRepository;
import com.cloudbees.demo.notifications.tenants.RateLimiter;
import com.cloudbees.demo.notifications.tenants.TenantService;

class NotificationServiceUnitTest {

    private NotificationService service;
    private DeliveryTracker tracker;
    private AuditLog audit;

    @BeforeEach
    void setUp() {
        NotificationsProperties props = new NotificationsProperties();
        TenantService tenants = new TenantService();
        RateLimiter rateLimiter = new RateLimiter(tenants);
        TemplateRepository templates = new TemplateRepository();
        TemplateEngine engine = new TemplateEngine();
        RetryPolicy policy = new RetryPolicy(props);
        tracker = new DeliveryTracker();
        audit = new AuditLog();

        List<ChannelAdapter> adapters = List.of(
                new EmailChannel(props),
                new SmsChannel(props),
                new WebhookChannel(props));

        service = new NotificationService(tenants, rateLimiter, templates, engine,
                policy, tracker, audit, adapters);
    }

    @Test
    void sendsWelcomeEmailSuccessfully() {
        UUID id = service.send("acme", Channel.EMAIL, "alex@example.com",
                "welcome", Map.of("name", "Alex", "product", "Acme"));
        assertThat(tracker.get(id).getStatus()).isEqualTo(NotificationStatus.DELIVERED);
    }

    @Test
    void sendsWebhookSuccessfully() {
        UUID id = service.send("acme", Channel.WEBHOOK, "https://hooks.example.com/x",
                "welcome", Map.of("name", "Alex", "product", "Acme"));
        assertThat(tracker.get(id).getStatus()).isEqualTo(NotificationStatus.DELIVERED);
    }

    @Test
    void recordsAuditEventsForSend() {
        UUID id = service.send("acme", Channel.EMAIL, "alex@example.com",
                "welcome", Map.of("name", "Alex", "product", "Acme"));
        List<com.cloudbees.demo.notifications.audit.AuditEntry> entries = audit.entriesFor(id);
        assertThat(entries).extracting("type")
                .contains(AuditEventType.SEND_REQUESTED, AuditEventType.SEND_QUEUED,
                        AuditEventType.DELIVERY_SUCCEEDED);
    }

    @Test
    void throwsForUnknownTenant() {
        assertThatThrownBy(() -> service.send("nope", Channel.EMAIL, "a@b.com",
                "welcome", Map.of("name", "x", "product", "y")))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void throwsForUnknownTemplate() {
        assertThatThrownBy(() -> service.send("acme", Channel.EMAIL, "a@b.com",
                "nope", Map.of()))
                .isInstanceOf(RuntimeException.class);
    }
}
