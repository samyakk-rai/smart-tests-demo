package com.cloudbees.demo.notifications.audit;

public enum AuditEventType {
    SEND_REQUESTED,
    SEND_QUEUED,
    DELIVERY_ATTEMPTED,
    DELIVERY_SUCCEEDED,
    DELIVERY_FAILED,
    DELIVERY_RETRIED,
    DELIVERY_ABANDONED
}
