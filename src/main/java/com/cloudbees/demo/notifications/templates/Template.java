package com.cloudbees.demo.notifications.templates;

public class Template {
    private final String key;
    private final String subject;
    private final String body;

    public Template(String key, String subject, String body) {
        this.key = key;
        this.subject = subject;
        this.body = body;
    }

    public String getKey() { return key; }
    public String getSubject() { return subject; }
    public String getBody() { return body; }
}
