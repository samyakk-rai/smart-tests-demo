package com.cloudbees.demo.notifications.channels;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.cloudbees.demo.notifications.config.NotificationsProperties;
import com.cloudbees.demo.notifications.core.Channel;

class WebhookChannelUnitTest {

    private WebhookChannel channel;

    @BeforeEach
    void setUp() { channel = new WebhookChannel(new NotificationsProperties()); }

    @Test
    void identifiesAsWebhookChannel() {
        assertThat(channel.channel()).isEqualTo(Channel.WEBHOOK);
    }

    @Test
    void acceptsHttpsUrl() {
        DeliveryResult r = channel.deliver("https://example.com/hook", null, "body");
        assertThat(r.isSuccess()).isTrue();
    }

    @Test
    void acceptsHttpUrl() {
        DeliveryResult r = channel.deliver("http://example.com/hook", null, "body");
        assertThat(r.isSuccess()).isTrue();
    }

    @Test
    void rejectsUnsupportedScheme() {
        assertThatThrownBy(() -> channel.deliver("ftp://example.com/x", null, "body"))
                .isInstanceOf(ChannelException.class);
    }

    @Test
    void includesSignatureInProviderMessageId() {
        DeliveryResult r = channel.deliver("https://example.com/hook", null, "body");
        assertThat(r.getProviderMessageId()).contains("sig=");
    }
}
