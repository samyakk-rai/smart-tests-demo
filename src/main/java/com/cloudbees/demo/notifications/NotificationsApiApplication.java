package com.cloudbees.demo.notifications;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class NotificationsApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotificationsApiApplication.class, args);
    }
}
