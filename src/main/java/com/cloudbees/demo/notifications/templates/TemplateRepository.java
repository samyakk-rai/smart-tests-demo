package com.cloudbees.demo.notifications.templates;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class TemplateRepository {

    private final Map<String, Template> store = new HashMap<>();

    public TemplateRepository() {
        // Seed a handful of common templates so the service is usable out of the box.
        store.put("welcome",
                new Template("welcome",
                        "Welcome to {{product}}",
                        "Hi {{name}}, thanks for signing up to {{product}}."));
        store.put("order-confirmation",
                new Template("order-confirmation",
                        "Order {{order_id}} confirmed",
                        "Hi {{name}}, your order {{order_id}} for {{amount}} has been confirmed."));
        store.put("password-reset",
                new Template("password-reset",
                        "Reset your password",
                        "Hi {{name}}, click here to reset your password: {{reset_link}}"));
        store.put("invoice-paid",
                new Template("invoice-paid",
                        "Invoice {{invoice_id}} paid",
                        "Hi {{name}}, we received {{amount}} for invoice {{invoice_id}}."));
        store.put("trial-ending",
                new Template("trial-ending",
                        "Your trial ends {{enddate}}",
                        "Hi {{name}}, your trial of {{product}} ends on {{enddate}}."));
    }

    public Template get(String key) {
        Template t = store.get(key);
        if (t == null) {
            throw new TemplateNotFoundException(key);
        }
        return t;
    }

    public void put(Template template) {
        store.put(template.getKey(), template);
    }

    public int size() {
        return store.size();
    }
}
