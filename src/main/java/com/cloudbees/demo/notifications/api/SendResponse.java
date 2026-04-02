package com.cloudbees.demo.notifications.api;

import java.util.UUID;

public class SendResponse {
    private final UUID id;
    private final String status;

    public SendResponse(UUID id, String status) {
        this.id = id;
        this.status = status;
    }

    public UUID getId() { return id; }
    public String getStatus() { return status; }
}
