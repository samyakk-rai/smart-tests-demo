package com.cloudbees.demo.notifications.templates;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;

import org.junit.jupiter.api.Test;

class TemplateRepositoryUnitTest {

    private final TemplateRepository repo = new TemplateRepository();
    private final TemplateEngine engine = new TemplateEngine();

    @Test
    void seedsCommonTemplates() {
        assertThat(repo.size()).isGreaterThanOrEqualTo(5);
        assertThat(repo.get("welcome").getKey()).isEqualTo("welcome");
    }

    @Test
    void throwsForUnknownKey() {
        assertThatThrownBy(() -> repo.get("nope"))
                .isInstanceOf(TemplateNotFoundException.class);
    }

    @Test
    void canStoreAndRetrieveCustomTemplate() {
        Template t = new Template("custom", "Hi {{name}}", "Body for {{name}}");
        repo.put(t);
        assertThat(repo.get("custom")).isSameAs(t);
    }

    @Test
    void welcomeTemplateRendersCleanly() {
        Template t = repo.get("welcome");
        String out = engine.render(t.getBody(), Map.of("name", "Alex", "product", "Acme"));
        assertThat(out).isEqualTo("Hi Alex, thanks for signing up to Acme.");
    }

    /**
     * Failing test on main — order-confirmation template includes {{order_id}}
     * and {{amount}}, and order_id trips the underscore bug in the renderer.
     */
    @Test
    void orderConfirmationTemplateRendersCleanly() {
        Template t = repo.get("order-confirmation");
        String out = engine.render(t.getBody(), Map.of(
                "name", "Alex",
                "order_id", "1234",
                "amount", "$42.00"));
        assertThat(out).contains("1234");
    }
}
