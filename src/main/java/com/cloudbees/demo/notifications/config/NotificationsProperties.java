package com.cloudbees.demo.notifications.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "notifications")
public class NotificationsProperties {

    private Delivery delivery = new Delivery();
    private RateLimit rateLimit = new RateLimit();
    private Channels channels = new Channels();

    public Delivery getDelivery() { return delivery; }
    public RateLimit getRateLimit() { return rateLimit; }
    public Channels getChannels() { return channels; }

    public void setDelivery(Delivery delivery) { this.delivery = delivery; }
    public void setRateLimit(RateLimit rateLimit) { this.rateLimit = rateLimit; }
    public void setChannels(Channels channels) { this.channels = channels; }

    public static class Delivery {
        private int maxAttempts = 5;
        private long initialBackoffMs = 250L;
        private long maxBackoffMs = 30_000L;

        public int getMaxAttempts() { return maxAttempts; }
        public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }
        public long getInitialBackoffMs() { return initialBackoffMs; }
        public void setInitialBackoffMs(long initialBackoffMs) { this.initialBackoffMs = initialBackoffMs; }
        public long getMaxBackoffMs() { return maxBackoffMs; }
        public void setMaxBackoffMs(long maxBackoffMs) { this.maxBackoffMs = maxBackoffMs; }
    }

    public static class RateLimit {
        private int defaultPerMinute = 60;
        public int getDefaultPerMinute() { return defaultPerMinute; }
        public void setDefaultPerMinute(int defaultPerMinute) { this.defaultPerMinute = defaultPerMinute; }
    }

    public static class Channels {
        private Email email = new Email();
        private Sms sms = new Sms();
        private Webhook webhook = new Webhook();

        public Email getEmail() { return email; }
        public Sms getSms() { return sms; }
        public Webhook getWebhook() { return webhook; }
        public void setEmail(Email email) { this.email = email; }
        public void setSms(Sms sms) { this.sms = sms; }
        public void setWebhook(Webhook webhook) { this.webhook = webhook; }

        public static class Email {
            private String from = "noreply@example.com";
            public String getFrom() { return from; }
            public void setFrom(String from) { this.from = from; }
        }

        public static class Sms {
            private String senderId = "ACME";
            public String getSenderId() { return senderId; }
            public void setSenderId(String senderId) { this.senderId = senderId; }
        }

        public static class Webhook {
            private String signingAlgorithm = "HmacSHA256";
            public String getSigningAlgorithm() { return signingAlgorithm; }
            public void setSigningAlgorithm(String signingAlgorithm) { this.signingAlgorithm = signingAlgorithm; }
        }
    }
}
