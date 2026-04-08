package com.cloudbees.demo.notifications.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class HmacSignerUnitTest {

    @Test
    void producesNonEmptyHex() {
        String sig = HmacSigner.sign("HmacSHA256", "key", "msg");
        assertThat(sig).hasSize(64);
        assertThat(sig).matches("[0-9a-f]+");
    }

    @Test
    void isDeterministicForSameInputs() {
        String a = HmacSigner.sign("HmacSHA256", "key", "msg");
        String b = HmacSigner.sign("HmacSHA256", "key", "msg");
        assertThat(a).isEqualTo(b);
    }

    @Test
    void changesWithDifferentKey() {
        String a = HmacSigner.sign("HmacSHA256", "key1", "msg");
        String b = HmacSigner.sign("HmacSHA256", "key2", "msg");
        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void changesWithDifferentMessage() {
        String a = HmacSigner.sign("HmacSHA256", "key", "msg1");
        String b = HmacSigner.sign("HmacSHA256", "key", "msg2");
        assertThat(a).isNotEqualTo(b);
    }
}
