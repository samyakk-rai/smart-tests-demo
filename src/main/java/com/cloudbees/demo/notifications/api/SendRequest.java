package com.cloudbees.demo.notifications.api;

import java.util.Map;

import com.cloudbees.demo.notifications.core.Channel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SendRequest {
    @NotNull
    private Channel channel;

    @NotBlank
    private String to;

    @NotBlank
    private String template;

    private Map<String, Object> variables;

    public Channel getChannel() { return channel; }
    public void setChannel(Channel channel) { this.channel = channel; }
    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }
    public String getTemplate() { return template; }
    public void setTemplate(String template) { this.template = template; }
    public Map<String, Object> getVariables() { return variables; }
    public void setVariables(Map<String, Object> variables) { this.variables = variables; }
}
