package com.cloudbees.demo.notifications.api;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudbees.demo.notifications.core.NotificationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/v1")
public class SendController {

    private final NotificationService service;

    public SendController(NotificationService service) {
        this.service = service;
    }

    @PostMapping("/send")
    public ResponseEntity<SendResponse> send(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @Valid @RequestBody SendRequest req) {
        UUID id = service.send(tenantId, req.getChannel(), req.getTo(),
                req.getTemplate(), req.getVariables());
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(new SendResponse(id, "queued"));
    }
}
