package com.cloudbees.demo.notifications.tenants;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class TenantServiceUnitTest {

    private final TenantService svc = new TenantService();

    @Test
    void resolvesSeededTenants() {
        assertThat(svc.get("acme").getName()).isEqualTo("Acme Corp");
        assertThat(svc.get("globex").getName()).isEqualTo("Globex Industries");
        assertThat(svc.get("hooli").getRatePerMinute()).isEqualTo(240);
    }

    @Test
    void existsReturnsTrueForKnown() {
        assertThat(svc.exists("acme")).isTrue();
    }

    @Test
    void existsReturnsFalseForUnknown() {
        assertThat(svc.exists("unknown")).isFalse();
    }

    @Test
    void throwsForUnknownTenant() {
        assertThatThrownBy(() -> svc.get("nope"))
                .isInstanceOf(TenantNotFoundException.class);
    }

    @Test
    void canAddNewTenant() {
        Tenant t = new Tenant("new", "New", 50, new Tenant.DeliveryConfig(3, 100L));
        svc.put(t);
        assertThat(svc.get("new")).isSameAs(t);
    }
}
