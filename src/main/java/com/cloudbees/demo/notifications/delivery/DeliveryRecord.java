package com.cloudbees.demo.notifications.delivery;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.cloudbees.demo.notifications.core.NotificationStatus;

public class DeliveryRecord {
    private final UUID notificationId;
    private NotificationStatus status;
    private final List<DeliveryAttempt> attempts = new ArrayList<>();

    public DeliveryRecord(UUID notificationId) {
        this.notificationId = notificationId;
        this.status = NotificationStatus.QUEUED;
    }

    public UUID getNotificationId() { return notificationId; }
    public NotificationStatus getStatus() { return status; }
    public void setStatus(NotificationStatus status) { this.status = status; }
    public List<DeliveryAttempt> getAttempts() { return attempts; }
    public void addAttempt(DeliveryAttempt attempt) { attempts.add(attempt); }
}
