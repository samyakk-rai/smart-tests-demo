package com.cloudbees.demo.notifications.core;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class Notification {
    private final UUID id;
    private final String tenantId;
    private final Channel channel;
    private final String to;
    private final String templateKey;
    private final Map<String, Object> variables;
    private final Instant createdAt;

    public Notification(UUID id, String tenantId, Channel channel, String to,
                        String templateKey, Map<String, Object> variables, Instant createdAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.channel = channel;
        this.to = to;
        this.templateKey = templateKey;
        this.variables = variables;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getTenantId() { return tenantId; }
    public Channel getChannel() { return channel; }
    public String getTo() { return to; }
    public String getTemplateKey() { return templateKey; }
    public Map<String, Object> getVariables() { return variables; }
    public Instant getCreatedAt() { return createdAt; }
}
