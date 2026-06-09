package com.cloudbees.demo.notifications.core;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.cloudbees.demo.notifications.audit.AuditEventType;
import com.cloudbees.demo.notifications.audit.AuditLog;
import com.cloudbees.demo.notifications.channels.ChannelAdapter;
import com.cloudbees.demo.notifications.channels.ChannelException;
import com.cloudbees.demo.notifications.channels.DeliveryResult;
import com.cloudbees.demo.notifications.delivery.DeliveryAttempt;
import com.cloudbees.demo.notifications.delivery.DeliveryTracker;
import com.cloudbees.demo.notifications.delivery.RetryPolicy;
import com.cloudbees.demo.notifications.templates.Template;
import com.cloudbees.demo.notifications.templates.TemplateEngine;
import com.cloudbees.demo.notifications.templates.TemplateRepository;
import com.cloudbees.demo.notifications.tenants.RateLimiter;
import com.cloudbees.demo.notifications.tenants.Tenant;
import com.cloudbees.demo.notifications.tenants.TenantService;

@Service
public class NotificationService {

    private final TenantService tenants;
    private final RateLimiter rateLimiter;
    private final TemplateRepository templates;
    private final TemplateEngine templateEngine;
    private final RetryPolicy retryPolicy;
    private final DeliveryTracker tracker;
    private final AuditLog audit;
    private final Map<Channel, ChannelAdapter> adapters;

    public NotificationService(TenantService tenants,
                               RateLimiter rateLimiter,
                               TemplateRepository templates,
                               TemplateEngine templateEngine,
                               RetryPolicy retryPolicy,
                               DeliveryTracker tracker,
                               AuditLog audit,
                               List<ChannelAdapter> channelAdapters) {
        this.tenants = tenants;
        this.rateLimiter = rateLimiter;
        this.templates = templates;
        this.templateEngine = templateEngine;
        this.retryPolicy = retryPolicy;
        this.tracker = tracker;
        this.audit = audit;
        this.adapters = new HashMap<>();
        for (ChannelAdapter a : channelAdapters) {
            this.adapters.put(a.channel(), a);
        }
    }

    public UUID send(String tenantId, Channel channel, String to,
                     String templateKey, Map<String, Object> variables) {
        Tenant tenant = tenants.get(tenantId);
        rateLimiter.check(tenantId);

        UUID id = UUID.randomUUID();
        Notification n = new Notification(id, tenantId, channel, to, templateKey, variables, Instant.now());

        audit.record(tenantId, id, AuditEventType.SEND_REQUESTED,
                "channel=" + channel + " template=" + templateKey);

        Template template = templates.get(templateKey);
        String subject = templateEngine.render(template.getSubject(), variables);
        String body = templateEngine.render(template.getBody(), variables);

        tracker.open(id);
        audit.record(tenantId, id, AuditEventType.SEND_QUEUED, "queued for " + channel);

        ChannelAdapter adapter = adapters.get(channel);
        if (adapter == null) {
            throw new ChannelException("no adapter configured for channel " + channel);
        }

        attemptDelivery(tenant, n, adapter, subject, body);
        return id;
    }

    private void attemptDelivery(Tenant tenant, Notification n, ChannelAdapter adapter,
                                 String subject, String body) {
        int attempt = 1;
        while (true) {
            Instant start = Instant.now();
            audit.record(tenant.getId(), n.getId(), AuditEventType.DELIVERY_ATTEMPTED,
                    "attempt=" + attempt);
            try {
                DeliveryResult result = adapter.deliver(n.getTo(), subject, body);
                Instant end = Instant.now();
                if (result.isSuccess()) {
                    tracker.update(n.getId(), NotificationStatus.DELIVERED,
                            new DeliveryAttempt(attempt, start, end, true, null));
                    audit.record(tenant.getId(), n.getId(), AuditEventType.DELIVERY_SUCCEEDED,
                            "provider=" + result.getProviderMessageId());
                    return;
                }
                tracker.update(n.getId(), NotificationStatus.RETRYING,
                        new DeliveryAttempt(attempt, start, end, false, result.getErrorMessage()));
                audit.record(tenant.getId(), n.getId(), AuditEventType.DELIVERY_FAILED,
                        result.getErrorMessage());
            } catch (ChannelException e) {
                throw e;
            } catch (RuntimeException e) {
                Instant end = Instant.now();
                tracker.update(n.getId(), NotificationStatus.RETRYING,
                        new DeliveryAttempt(attempt, start, end, false, e.getMessage()));
                audit.record(tenant.getId(), n.getId(), AuditEventType.DELIVERY_FAILED, e.getMessage());
            }
            if (!retryPolicy.shouldRetry(tenant, attempt)) {
                tracker.update(n.getId(), NotificationStatus.FAILED, null);
                audit.record(tenant.getId(), n.getId(), AuditEventType.DELIVERY_ABANDONED,
                        "max attempts reached");
                return;
            }
            long backoff = retryPolicy.calculateBackoffMs(tenant, attempt);
            sleepQuietly(backoff);
            audit.record(tenant.getId(), n.getId(), AuditEventType.DELIVERY_RETRIED,
                    "after " + backoff + "ms");
            attempt++;
        }
    }

    private void sleepQuietly(long ms) {
        try { Thread.sleep(ms); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
