# API Gateway Documentation

## Overview

The API Gateway serves as the single entry point for all external client requests to the Smart Home Maintenance Platform. It routes incoming HTTP requests to the appropriate backend microservices using Spring Cloud Gateway and Eureka service discovery.

## Architecture

```
Client  -->  API Gateway (port 8085)  -->  Eureka Server (port 8761)
                  |                              |
                  +--- resolves service via ------+
                  |
                  +-----> User Service (lb://user-service)
                  +-----> Property Service (lb://property-service)
                  +-----> Maintenance Service (lb://maintenance-service)
                  +-----> Notification Service (lb://notification-service)
```

## Configuration

### Gateway Port

The gateway runs on **port 8085**, configured in `application.yml`:

```yaml
server:
  port: 8085
```

### Service Discovery Integration

The gateway uses **Eureka** for dynamic service resolution. No hardcoded host or port values are used. All routes use `lb://` (load-balanced) URIs, which are resolved at runtime through the Eureka registry.

```yaml
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka
  instance:
    prefer-ip-address: true
```

The discovery locator is also enabled, which allows automatic routing to any registered service by its service ID:

```yaml
spring.cloud.gateway.discovery.locator:
  enabled: true
  lower-case-service-id: true
```

## Routing Configuration

All routes are defined in `src/main/resources/application.yml`.

| Route ID | Path Pattern | Target Service |
|---|---|---|
| `user-service-auth` | `/auth/**` | `lb://user-service` |
| `user-service-api` | `/api/v1/users/**` | `lb://user-service` |
| `property-service` | `/api/v1/properties/**` | `lb://property-service` |
| `maintenance-service` | `/api/v1/maintenance-requests/**` | `lb://maintenance-service` |
| `notification-service` | `/api/v1/notifications/**` | `lb://notification-service` |

### How Routing Works

1. A client sends a request to the gateway (e.g., `GET http://localhost:8085/api/v1/properties`)
2. The gateway matches the request path against the configured predicates
3. The matched route's `lb://` URI is resolved through Eureka to find a running instance
4. The request (including all headers such as `Authorization`) is forwarded to the target service
5. The response is returned to the client

## Error Handling

### GatewayErrorHandler

The `GatewayErrorHandler` class (`@Order(-1)`) is a global error handler that intercepts all exceptions during request routing and returns structured JSON error responses.

| Exception Type | HTTP Status | Message |
|---|---|---|
| `ConnectException` | 503 Service Unavailable | Service is currently unavailable |
| `TimeoutException` | 504 Gateway Timeout | The request timed out |
| `ResponseStatusException` | Varies | Propagates original status and reason |
| Any other exception | 500 Internal Server Error | An unexpected error occurred |

**Response format:**

```json
{
  "status": 503,
  "error": "Service Unavailable",
  "message": "Service is currently unavailable. Please try again later.",
  "path": "/api/v1/properties"
}
```

### FallbackController

The `FallbackController` provides dedicated fallback endpoints for each service. These return a 503 response with the specific service name:

```json
{
  "status": 503,
  "error": "Service Unavailable",
  "message": "The property-service is currently unavailable. Please try again later."
}
```

## Request Logging

The `RequestLoggingFilter` is a global filter that logs all incoming requests and outgoing responses, including:
- HTTP method and path
- Response status code

This is useful for debugging routing issues and monitoring traffic.

## Health Monitoring

The gateway exposes actuator endpoints for health monitoring:

- `GET /actuator/health` - Overall health status
- `GET /actuator/info` - Application info

These support Kubernetes-style liveness and readiness probes.

## Starting the Gateway

### Prerequisites
- Eureka Discovery Service must be running on port 8761
- At least one backend service should be registered with Eureka

### Startup Order
1. Discovery Service (port 8761)
2. Backend services (User, Property, Maintenance, Notification)
3. API Gateway (port 8085)

Allow ~30 seconds after starting backend services for them to register with Eureka before sending requests through the gateway.

## Testing

### Verify gateway is running
```
GET http://localhost:8085/actuator/health
```

### Verify routing through gateway
```
POST http://localhost:8085/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password"
}
```

### Verify error handling (stop a service, then call its route)
```
GET http://localhost:8085/api/v1/properties
# Expected: 503 with JSON error body
```
