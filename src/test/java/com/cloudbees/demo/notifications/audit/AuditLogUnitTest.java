package com.cloudbees.demo.notifications.audit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class AuditLogUnitTest {

    private final AuditLog log = new AuditLog();

    @Test
    void recordsAndRetrievesEntries() {
        UUID id = UUID.randomUUID();
        log.record("acme", id, AuditEventType.SEND_REQUESTED, "channel=EMAIL");
        log.record("acme", id, AuditEventType.SEND_QUEUED, "queued");
        assertThat(log.entriesFor(id)).hasSize(2);
    }

    @Test
    void filtersByNotificationId() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        log.record("acme", a, AuditEventType.SEND_REQUESTED, "x");
        log.record("acme", b, AuditEventType.SEND_REQUESTED, "y");
        assertThat(log.entriesFor(a)).hasSize(1);
    }

    @Test
    void sizeTracksTotalEntries() {
        int before = log.size();
        log.record("acme", UUID.randomUUID(), AuditEventType.SEND_REQUESTED, "x");
        assertThat(log.size()).isEqualTo(before + 1);
    }

    @Test
    void preservesEventTypeAndMessage() {
        UUID id = UUID.randomUUID();
        log.record("globex", id, AuditEventType.DELIVERY_FAILED, "timeout");
        AuditEntry e = log.entriesFor(id).get(0);
        assertThat(e.getType()).isEqualTo(AuditEventType.DELIVERY_FAILED);
        assertThat(e.getMessage()).isEqualTo("timeout");
        assertThat(e.getTenantId()).isEqualTo("globex");
    }
}
