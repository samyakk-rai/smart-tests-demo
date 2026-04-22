package com.cloudbees.demo.notifications.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Tag("integration")
class EndToEndApiIntegrationTest {

    @Autowired MockMvc mvc;

    @Test
    void postSendReturnsAccepted() throws Exception {
        SimulatedLatency.standard();
        mvc.perform(post("/v1/send")
                        .header("X-Tenant-Id", "acme")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "channel": "EMAIL",
                                  "to": "alex@example.com",
                                  "template": "welcome",
                                  "variables": { "name": "Alex", "product": "Acme" }
                                }
                                """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("queued"));
    }

    @Test
    void unknownTenantReturns404() throws Exception {
        SimulatedLatency.standard();
        mvc.perform(post("/v1/send")
                        .header("X-Tenant-Id", "no-such-tenant")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "channel": "EMAIL",
                                  "to": "alex@example.com",
                                  "template": "welcome",
                                  "variables": { "name": "Alex", "product": "Acme" }
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void unknownTemplateReturns404() throws Exception {
        SimulatedLatency.standard();
        mvc.perform(post("/v1/send")
                        .header("X-Tenant-Id", "acme")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "channel": "EMAIL",
                                  "to": "alex@example.com",
                                  "template": "no-such-template",
                                  "variables": {}
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void invalidEmailReturns400() throws Exception {
        SimulatedLatency.standard();
        mvc.perform(post("/v1/send")
                        .header("X-Tenant-Id", "acme")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "channel": "EMAIL",
                                  "to": "not-an-email",
                                  "template": "welcome",
                                  "variables": { "name": "Alex", "product": "Acme" }
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}
