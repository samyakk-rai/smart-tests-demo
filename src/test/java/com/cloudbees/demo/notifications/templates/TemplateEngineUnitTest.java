package com.cloudbees.demo.notifications.templates;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;

import org.junit.jupiter.api.Test;

class TemplateEngineUnitTest {

    private final TemplateEngine engine = new TemplateEngine();

    @Test
    void rendersPlainBodyWithNoVariables() {
        assertThat(engine.render("hello world", Map.of())).isEqualTo("hello world");
    }

    @Test
    void rendersSingleAlphanumericVariable() {
        String body = "hi {{name}}";
        assertThat(engine.render(body, Map.of("name", "Alex"))).isEqualTo("hi Alex");
    }

    @Test
    void rendersMultipleAlphanumericVariables() {
        String body = "hi {{name}}, welcome to {{product}}";
        assertThat(engine.render(body, Map.of("name", "Alex", "product", "Acme")))
                .isEqualTo("hi Alex, welcome to Acme");
    }

    @Test
    void escapesHtmlInVariableValues() {
        String body = "msg: {{content}}";
        String out = engine.render(body, Map.of("content", "<script>alert(1)</script>"));
        assertThat(out).isEqualTo("msg: &lt;script&gt;alert(1)&lt;/script&gt;");
    }

    @Test
    void throwsWhenVariableIsMissing() {
        assertThatThrownBy(() -> engine.render("hi {{name}}", Map.of()))
                .isInstanceOf(TemplateRenderException.class)
                .hasMessageContaining("name");
    }

    @Test
    void returnsEmptyForNullBody() {
        assertThat(engine.render(null, Map.of("x", "y"))).isEqualTo("");
    }

    // ============================================================
    // Failing tests on main — root cause: TemplateEngine.VAR_PATTERN
    // dropped underscore support in the v1.4.0 escaping change. These
    // tests use variable names with underscores (order_id, reset_link,
    // invoice_id) and trip "unresolved variable" on render.
    // ============================================================

    @Test
    void rendersOrderConfirmationWithUnderscoreVariables() {
        String body = "Hi {{name}}, your order {{order_id}} for {{amount}} has been confirmed.";
        String out = engine.render(body, Map.of(
                "name", "Alex",
                "order_id", "1234",
                "amount", "$42.00"));
        assertThat(out).contains("order 1234").contains("Alex");
    }

    @Test
    void rendersPasswordResetWithResetLink() {
        String body = "Hi {{name}}, click here to reset your password: {{reset_link}}";
        String out = engine.render(body, Map.of(
                "name", "Alex",
                "reset_link", "https://example.com/r/abc"));
        assertThat(out).contains("https://example.com/r/abc");
    }

    @Test
    void rendersInvoicePaidWithInvoiceId() {
        String body = "Hi {{name}}, we received {{amount}} for invoice {{invoice_id}}.";
        String out = engine.render(body, Map.of(
                "name", "Alex",
                "amount", "$99.00",
                "invoice_id", "INV-7788"));
        assertThat(out).contains("INV-7788");
    }
}
