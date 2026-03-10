package com.cloudbees.demo.notifications.delivery;

import java.time.Instant;

public class DeliveryAttempt {
    private final int attemptNumber;
    private final Instant startedAt;
    private final Instant finishedAt;
    private final boolean success;
    private final String errorMessage;

    public DeliveryAttempt(int attemptNumber, Instant startedAt, Instant finishedAt,
                           boolean success, String errorMessage) {
        this.attemptNumber = attemptNumber;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.success = success;
        this.errorMessage = errorMessage;
    }

    public int getAttemptNumber() { return attemptNumber; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getFinishedAt() { return finishedAt; }
    public boolean isSuccess() { return success; }
    public String getErrorMessage() { return errorMessage; }
}
