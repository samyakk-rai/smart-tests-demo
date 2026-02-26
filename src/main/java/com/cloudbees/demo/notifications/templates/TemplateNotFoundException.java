package com.cloudbees.demo.notifications.templates;

public class TemplateNotFoundException extends RuntimeException {
    public TemplateNotFoundException(String key) {
        super("template not found: " + key);
    }
}
