package com.cloudbees.demo.notifications.audit;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class AuditLog {

    private final List<AuditEntry> entries = Collections.synchronizedList(new ArrayList<>());

    public void record(String tenantId, UUID notificationId, AuditEventType type, String message) {
        entries.add(new AuditEntry(Instant.now(), tenantId, notificationId, type, message));
    }

    public List<AuditEntry> entriesFor(UUID notificationId) {
        synchronized (entries) {
            return entries.stream()
                    .filter(e -> notificationId.equals(e.getNotificationId()))
                    .toList();
        }
    }

    public int size() {
        return entries.size();
    }
}
