package com.cloudbees.demo.notifications.channels;

import java.net.URI;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.cloudbees.demo.notifications.config.NotificationsProperties;
import com.cloudbees.demo.notifications.core.Channel;
import com.cloudbees.demo.notifications.util.HmacSigner;

@Component
public class WebhookChannel implements ChannelAdapter {

    private final NotificationsProperties properties;

    public WebhookChannel(NotificationsProperties properties) {
        this.properties = properties;
    }

    @Override
    public Channel channel() { return Channel.WEBHOOK; }

    @Override
    public DeliveryResult deliver(String to, String subject, String body) {
        URI uri;
        try {
            uri = URI.create(to);
        } catch (IllegalArgumentException e) {
            throw new ChannelException("invalid webhook URL: " + to, e);
        }
        if (!"http".equals(uri.getScheme()) && !"https".equals(uri.getScheme())) {
            throw new ChannelException("unsupported webhook scheme: " + uri.getScheme());
        }
        String alg = properties.getChannels().getWebhook().getSigningAlgorithm();
        // Sign the body for the receiving endpoint to verify.
        String signature = HmacSigner.sign(alg, "demo-shared-secret", body == null ? "" : body);
        // Simulated POST — in production this is an HTTP call.
        return DeliveryResult.success("whk_" + UUID.randomUUID() + "_sig=" + signature.substring(0, 8));
    }
}
