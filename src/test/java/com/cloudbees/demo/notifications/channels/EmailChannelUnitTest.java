package com.cloudbees.demo.notifications.channels;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.cloudbees.demo.notifications.config.NotificationsProperties;
import com.cloudbees.demo.notifications.core.Channel;

class EmailChannelUnitTest {

    private EmailChannel channel;

    @BeforeEach
    void setUp() {
        channel = new EmailChannel(new NotificationsProperties());
    }

    @Test
    void identifiesAsEmailChannel() {
        assertThat(channel.channel()).isEqualTo(Channel.EMAIL);
    }

    @Test
    void deliversValidEmail() {
        DeliveryResult r = channel.deliver("alex@example.com", "Hi", "body");
        assertThat(r.isSuccess()).isTrue();
        assertThat(r.getProviderMessageId()).startsWith("eml_");
    }

    @Test
    void rejectsInvalidEmailAddress() {
        assertThatThrownBy(() -> channel.deliver("not-an-email", "s", "b"))
                .isInstanceOf(ChannelException.class);
    }

    @Test
    void rejectsNullRecipient() {
        assertThatThrownBy(() -> channel.deliver(null, "s", "b"))
                .isInstanceOf(ChannelException.class);
    }

    @Test
    void failsWhenFromAddressNotConfigured() {
        NotificationsProperties props = new NotificationsProperties();
        props.getChannels().getEmail().setFrom(null);
        EmailChannel c = new EmailChannel(props);
        DeliveryResult r = c.deliver("a@b.com", "s", "b");
        assertThat(r.isSuccess()).isFalse();
        assertThat(r.getErrorMessage()).contains("from address");
    }

    @Test
    void messageIdsAreUnique() {
        String id1 = channel.deliver("a@b.com", "s", "b").getProviderMessageId();
        String id2 = channel.deliver("a@b.com", "s", "b").getProviderMessageId();
        assertThat(id1).isNotEqualTo(id2);
    }
}
