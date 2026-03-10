package com.cloudbees.demo.notifications.channels;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.cloudbees.demo.notifications.config.NotificationsProperties;
import com.cloudbees.demo.notifications.core.Channel;

@Component
public class EmailChannel implements ChannelAdapter {

    private final NotificationsProperties properties;

    public EmailChannel(NotificationsProperties properties) {
        this.properties = properties;
    }

    @Override
    public Channel channel() {
        return Channel.EMAIL;
    }

    @Override
    public DeliveryResult deliver(String to, String subject, String body) {
        if (to == null || !to.contains("@")) {
            throw new ChannelException("invalid email address: " + to);
        }
        // Simulate provider call. In production this would talk to SES/Sendgrid.
        String from = properties.getChannels().getEmail().getFrom();
        if (from == null) {
            return DeliveryResult.failure("from address not configured");
        }
        return DeliveryResult.success("eml_" + UUID.randomUUID());
    }
}
