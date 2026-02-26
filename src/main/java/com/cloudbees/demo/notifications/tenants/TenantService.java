package com.cloudbees.demo.notifications.tenants;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class TenantService {

    private final Map<String, Tenant> store = new HashMap<>();

    public TenantService() {
        // Seed a few realistic-looking tenants.
        store.put("acme",
                new Tenant("acme", "Acme Corp", 120,
                        new Tenant.DeliveryConfig(5, 250L)));
        store.put("globex",
                new Tenant("globex", "Globex Industries", 60,
                        new Tenant.DeliveryConfig(3, 500L)));
        store.put("initech",
                new Tenant("initech", "Initech Software", 30,
                        // Per-tenant override not yet rolled out for this tenant.
                        null));
        store.put("hooli",
                new Tenant("hooli", "Hooli Inc", 240,
                        new Tenant.DeliveryConfig(7, 100L)));
    }

    public Tenant get(String id) {
        Tenant t = store.get(id);
        if (t == null) {
            throw new TenantNotFoundException(id);
        }
        return t;
    }

    public void put(Tenant tenant) {
        store.put(tenant.getId(), tenant);
    }

    public boolean exists(String id) {
        return store.containsKey(id);
    }
}
