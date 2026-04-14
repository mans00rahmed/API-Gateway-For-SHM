package com.sigma.smarthome.api_gateway;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
@TestPropertySource(properties = "eureka.client.enabled=false")
class GatewayRoutingTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void routesAuthRequestToUserService() {
        stubFor(post(urlEqualTo("/auth/login"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"token\":\"test-jwt-token\"}")));

        webTestClient.post().uri("/auth/login")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.token").isEqualTo("test-jwt-token");
    }

    @Test
    void routesPropertyRequestToPropertyService() {
        stubFor(get(urlEqualTo("/api/v1/properties/all"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[]")));

        webTestClient.get().uri("/api/v1/properties/all")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void routesMaintenanceRequestToMaintenanceService() {
        stubFor(get(urlEqualTo("/api/v1/maintenance-requests"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[]")));

        webTestClient.get().uri("/api/v1/maintenance-requests")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void returnsNotFoundForUnknownRoute() {
        webTestClient.get().uri("/unknown/route")
                .exchange()
                .expectStatus().isNotFound();
    }
}
