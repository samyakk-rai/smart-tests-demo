package com.cloudbees.demo.notifications.delivery;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.cloudbees.demo.notifications.core.NotificationStatus;

class DeliveryTrackerUnitTest {

    private final DeliveryTracker tracker = new DeliveryTracker();

    @Test
    void opensRecordWithQueuedStatus() {
        UUID id = UUID.randomUUID();
        DeliveryRecord r = tracker.open(id);
        assertThat(r.getStatus()).isEqualTo(NotificationStatus.QUEUED);
        assertThat(r.getAttempts()).isEmpty();
    }

    @Test
    void appendsAttemptOnUpdate() {
        UUID id = UUID.randomUUID();
        tracker.open(id);
        tracker.update(id, NotificationStatus.DELIVERED,
                new DeliveryAttempt(1, Instant.now(), Instant.now(), true, null));
        DeliveryRecord r = tracker.get(id);
        assertThat(r.getStatus()).isEqualTo(NotificationStatus.DELIVERED);
        assertThat(r.getAttempts()).hasSize(1);
    }

    @Test
    void returnsNullForUnknownId() {
        assertThat(tracker.get(UUID.randomUUID())).isNull();
    }

    @Test
    void tracksMultipleAttempts() {
        UUID id = UUID.randomUUID();
        tracker.open(id);
        for (int i = 1; i <= 3; i++) {
            tracker.update(id, NotificationStatus.RETRYING,
                    new DeliveryAttempt(i, Instant.now(), Instant.now(), false, "transient"));
        }
        assertThat(tracker.get(id).getAttempts()).hasSize(3);
    }

    @Test
    void sizeReflectsOpenRecords() {
        int before = tracker.size();
        tracker.open(UUID.randomUUID());
        tracker.open(UUID.randomUUID());
        assertThat(tracker.size()).isEqualTo(before + 2);
    }
}
