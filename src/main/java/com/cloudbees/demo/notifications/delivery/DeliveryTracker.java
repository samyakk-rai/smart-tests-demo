package com.cloudbees.demo.notifications.delivery;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.cloudbees.demo.notifications.core.NotificationStatus;

@Component
public class DeliveryTracker {

    private final Map<UUID, DeliveryRecord> records = new HashMap<>();

    public synchronized DeliveryRecord open(UUID notificationId) {
        DeliveryRecord r = new DeliveryRecord(notificationId);
        records.put(notificationId, r);
        return r;
    }

    public synchronized DeliveryRecord get(UUID notificationId) {
        return records.get(notificationId);
    }

    public synchronized void update(UUID notificationId, NotificationStatus status, DeliveryAttempt attempt) {
        DeliveryRecord r = records.get(notificationId);
        if (r == null) {
            r = new DeliveryRecord(notificationId);
            records.put(notificationId, r);
        }
        r.setStatus(status);
        if (attempt != null) {
            r.addAttempt(attempt);
        }
    }

    public synchronized int size() {
        return records.size();
    }
}
