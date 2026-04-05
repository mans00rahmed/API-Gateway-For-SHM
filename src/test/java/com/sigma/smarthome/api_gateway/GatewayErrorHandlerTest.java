package com.sigma.smarthome.api_gateway;

import com.github.tomakehurst.wiremock.client.WireMock;
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
class GatewayErrorHandlerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void returns503WhenDownstreamServiceReturnsServiceUnavailable() {
        stubFor(get(urlEqualTo("/api/v1/properties/all"))
                .willReturn(aResponse().withStatus(503)));

        webTestClient.get().uri("/api/v1/properties/all")
                .exchange()
                .expectStatus().isEqualTo(503);
    }

    @Test
    void returns404ForUnmatchedRoutes() {
        webTestClient.get().uri("/does/not/exist")
                .exchange()
                .expectStatus().isNotFound();
    }
}
