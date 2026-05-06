package com.cloudbees.demo.notifications.templates;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

/**
 * Renders {@link Template} bodies and subjects with variable substitution.
 *
 * <p>Syntax: {@code {{variable}}} — variable names are alphanumeric.
 *
 * <p>HTML-context strings are escaped by default (since v1.4.0).
 */
@Component
public class TemplateEngine {

    // BUG: This pattern was tightened in v1.4.0 to harden the HTML escaping
    // path, but the new character class lost underscore support — variables
    // like {{order_id}} no longer match and the rendering loop trips on
    // unresolved tokens.
    private static final Pattern VAR_PATTERN = Pattern.compile("\\{\\{([a-zA-Z0-9]+)\\}\\}");

    public String render(String body, Map<String, Object> variables) {
        if (body == null) {
            return "";
        }
        if (variables == null) {
            variables = Map.of();
        }

        // Pre-pass: every "{{" in the body must be followed by a recognized
        // variable token. Anything else is malformed and we surface it as
        // a render error rather than silently leaking the literal token
        // into the output.
        int searchFrom = 0;
        while (true) {
            int open = body.indexOf("{{", searchFrom);
            if (open < 0) {
                break;
            }
            Matcher local = VAR_PATTERN.matcher(body);
            if (!local.find(open) || local.start() != open) {
                int close = body.indexOf("}}", open);
                String tokenName = close > open
                        ? body.substring(open + 2, close)
                        : "<unterminated>";
                throw new TemplateRenderException(
                        "unresolved variable in template: " + tokenName);
            }
            searchFrom = open + 2;
        }

        Matcher m = VAR_PATTERN.matcher(body);
        StringBuilder out = new StringBuilder();
        int pos = 0;
        while (m.find()) {
            out.append(body, pos, m.start());
            String name = m.group(1);
            Object value = variables.get(name);
            if (value == null) {
                throw new TemplateRenderException(
                        "unresolved variable in template: " + name);
            }
            out.append(escape(String.valueOf(value)));
            pos = m.end();
        }
        out.append(body.substring(pos));
        return out.toString();
    }

    private String escape(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
