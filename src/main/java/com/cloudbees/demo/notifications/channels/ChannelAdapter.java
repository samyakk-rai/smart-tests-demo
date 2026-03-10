package com.cloudbees.demo.notifications.channels;

import com.cloudbees.demo.notifications.core.Channel;

public interface ChannelAdapter {
    Channel channel();
    DeliveryResult deliver(String to, String subject, String body);
}
