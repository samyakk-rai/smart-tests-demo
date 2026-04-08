package com.cloudbees.demo.notifications.channels;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.cloudbees.demo.notifications.config.NotificationsProperties;
import com.cloudbees.demo.notifications.core.Channel;

class SmsChannelUnitTest {

    private SmsChannel channel;

    @BeforeEach
    void setUp() { channel = new SmsChannel(new NotificationsProperties()); }

    @Test
    void identifiesAsSmsChannel() {
        assertThat(channel.channel()).isEqualTo(Channel.SMS);
    }

    @Test
    void acceptsE164Number() {
        DeliveryResult r = channel.deliver("+14155551212", null, "hi");
        assertThat(r.isSuccess()).isTrue();
    }

    @Test
    void rejectsNonE164Number() {
        assertThatThrownBy(() -> channel.deliver("4155551212", null, "hi"))
                .isInstanceOf(ChannelException.class);
    }

    @Test
    void rejectsEmptyBody() {
        DeliveryResult r = channel.deliver("+14155551212", null, "");
        assertThat(r.isSuccess()).isFalse();
    }

    @Test
    void rejectsTooLongBody() {
        String body = "x".repeat(2000);
        DeliveryResult r = channel.deliver("+14155551212", null, body);
        assertThat(r.isSuccess()).isFalse();
    }

    @Test
    void embedsSenderIdInProviderMessage() {
        DeliveryResult r = channel.deliver("+14155551212", null, "hi");
        assertThat(r.getProviderMessageId()).contains("ACME");
    }
}
