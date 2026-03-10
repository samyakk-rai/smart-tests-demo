package com.cloudbees.demo.notifications.channels;

public class DeliveryResult {
    private final boolean success;
    private final String providerMessageId;
    private final String errorMessage;

    private DeliveryResult(boolean success, String providerMessageId, String errorMessage) {
        this.success = success;
        this.providerMessageId = providerMessageId;
        this.errorMessage = errorMessage;
    }

    public static DeliveryResult success(String providerMessageId) {
        return new DeliveryResult(true, providerMessageId, null);
    }

    public static DeliveryResult failure(String errorMessage) {
        return new DeliveryResult(false, null, errorMessage);
    }

    public boolean isSuccess() { return success; }
    public String getProviderMessageId() { return providerMessageId; }
    public String getErrorMessage() { return errorMessage; }
}
