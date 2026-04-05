package com.sigma.smarthome.api_gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
public class FallbackController {

    @RequestMapping("/fallback/user-service")
    public Mono<ResponseEntity<Map<String, Object>>> userServiceFallback() {
        return buildFallback("user-service");
    }

    @RequestMapping("/fallback/property-service")
    public Mono<ResponseEntity<Map<String, Object>>> propertyServiceFallback() {
        return buildFallback("property-service");
    }

    @RequestMapping("/fallback/maintenance-service")
    public Mono<ResponseEntity<Map<String, Object>>> maintenanceServiceFallback() {
        return buildFallback("maintenance-service");
    }

    @RequestMapping("/fallback/notification-service")
    public Mono<ResponseEntity<Map<String, Object>>> notificationServiceFallback() {
        return buildFallback("notification-service");
    }

    private Mono<ResponseEntity<Map<String, Object>>> buildFallback(String service) {
        Map<String, Object> body = Map.of(
                "status", 503,
                "error", "Service Unavailable",
                "message", "The " + service + " is currently unavailable. Please try again later."
        );
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body));
    }
}
