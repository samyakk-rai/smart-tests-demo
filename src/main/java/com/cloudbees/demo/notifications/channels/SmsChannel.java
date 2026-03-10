package com.cloudbees.demo.notifications.channels;

import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.cloudbees.demo.notifications.config.NotificationsProperties;
import com.cloudbees.demo.notifications.core.Channel;

@Component
public class SmsChannel implements ChannelAdapter {

    private static final Pattern E164 = Pattern.compile("^\\+[1-9]\\d{1,14}$");

    private final NotificationsProperties properties;

    public SmsChannel(NotificationsProperties properties) {
        this.properties = properties;
    }

    @Override
    public Channel channel() { return Channel.SMS; }

    @Override
    public DeliveryResult deliver(String to, String subject, String body) {
        if (to == null || !E164.matcher(to).matches()) {
            throw new ChannelException("invalid SMS recipient (must be E.164): " + to);
        }
        if (body == null || body.isEmpty()) {
            return DeliveryResult.failure("empty SMS body");
        }
        if (body.length() > 1600) {
            return DeliveryResult.failure("SMS body exceeds 1600 chars");
        }
        // Sender ID is included in the provider call.
        String senderId = properties.getChannels().getSms().getSenderId();
        return DeliveryResult.success("sms_" + senderId + "_" + UUID.randomUUID());
    }
}
