package com.cloudbees.demo.notifications.audit;

import java.time.Instant;
import java.util.UUID;

public class AuditEntry {
    private final Instant at;
    private final String tenantId;
    private final UUID notificationId;
    private final AuditEventType type;
    private final String message;

    public AuditEntry(Instant at, String tenantId, UUID notificationId,
                      AuditEventType type, String message) {
        this.at = at;
        this.tenantId = tenantId;
        this.notificationId = notificationId;
        this.type = type;
        this.message = message;
    }

    public Instant getAt() { return at; }
    public String getTenantId() { return tenantId; }
    public UUID getNotificationId() { return notificationId; }
    public AuditEventType getType() { return type; }
    public String getMessage() { return message; }
}
