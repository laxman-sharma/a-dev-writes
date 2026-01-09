---
layout: post
title: "Spring Boot Production Checklist: 15 Critical Settings"
date: 2026-02-28
author: Laxman Sharma
image: /assets/images/production-checklist-hero.png
categories: [java, spring-boot, devops]
tags: [java, spring-boot, production, devops, best-practices]
excerpt: "Don't deploy Spring Boot to production without these 15 essential configurations. Covers health checks, metrics, security headers, connection pools, and more."
---

# Production-Ready Spring Boot: A Checklist

Your app works in development. It'll crash in production unless you configure these **15 critical settings**.

---

![Production Deployment Checklist]({{ "" | relative_url }}/assets/images/production-checklist-hero.png)

---

## 1. Actuator Health Checks

**Why**: Kubernetes/AWS need to know when your app is ready.

```yaml
management:
  endpoint:
    health:
      enabled: true
      show-details: always
  health:
    livenessState:
      enabled: true
    readinessState:
      enabled: true
```

---

## 2. Graceful Shutdown

**Why**: Prevent dropped requests during deployment.

```yaml
server:
  shutdown: graceful
spring:
  lifecycle:
    timeout-per-shutdown-phase: 30s
```

---

## 3. Connection Pool Tuning

**Why**: Default pool size (10) is too small for production.

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
```

**Rule of Thumb**: `max-pool-size = (num_cpu_cores * 2) + effective_spindle_count`

---

## 4. Logging Configuration

**Why**: Debug logs in production kill performance.

```yaml
logging:
  level:
    root: WARN
    com.yourcompany: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
  file:
    name: /var/log/app.log
    max-size: 10MB
    max-history: 30
```

---

## 5. Security Headers

**Why**: Protect against XSS, clickjacking, MIME sniffing.

```java
@Bean
SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http
        .headers(headers -> headers
            .contentSecurityPolicy("default-src 'self'")
            .frameOptions().deny()
            .xssProtection()
        )
        .build();
}
```

---

## 6. Rate Limiting

**Why**: Prevent API abuse and DDoS.

```java
@Bean
RateLimiterRegistry rateLimiterRegistry() {
    return RateLimiterRegistry.of(RateLimiterConfig.custom()
        .limitForPeriod(100)  // 100 requests
        .limitRefreshPeriod(Duration.ofMinutes(1))
        .build());
}
```

---

## 7. Metrics Export

**Why**: You can't fix what you can't measure.

```yaml
management:
  metrics:
    export:
      prometheus:
        enabled: true
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
```

---

## 8. Thread Pool Configuration

**Why**: Default thread pools aren't optimized for high load.

```yaml
server:
  tomcat:
    threads:
      max: 200
      min-spare: 10
```

---

## 9. Timeout Settings

**Why**: Prevent hanging requests from consuming resources.

```yaml
spring:
  mvc:
    async:
      request-timeout: 30000
  webflux:
    timeout: 30s
```

---

## 10. Compression

**Why**: Reduce bandwidth usage by 70%.

```yaml
server:
  compression:
    enabled: true
    mime-types: text/html,text/xml,text/plain,application/json
    min-response-size: 1024
```

---

## 11. JVM Flags

**Why**: Default JVM settings aren't production-ready.

```bash
java -Xms2G -Xmx2G \              # Set heap size
     -XX:+UseG1GC \                # Use G1 collector
     -XX:MaxGCPauseMillis=200 \    # GC pause target
     -XX:+HeapDumpOnOutOfMemoryError \  # Debug OOM
     -jar app.jar
```

---

## 12. Error Handling

**Why**: Don't leak stack traces to users.

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleError(Exception e) {
        log.error("Unhandled exception", e);
        return ResponseEntity
            .status(500)
            .body(new ErrorResponse("Internal server error"));
    }
}
```

---

## 13. Database Migrations

**Why**: Track schema changes across environments.

```yaml
spring:
  liquibase:
    enabled: true
    change-log: classpath:db/changelog/db.changelog-master.yaml
```

---

## 14. Feature Flags

**Why**: Deploy code without activating features.

```java
@ConditionalOnProperty("feature.newDashboard.enabled")
@RestController
public class NewDashboardController {
    // Only active if feature.newDashboard.enabled=true
}
```

---

## 15. Distributed Tracing

**Why**: Debug cross-service issues.

```yaml
management:
  tracing:
    sampling:
      probability: 0.1  # Sample 10% of requests
  zipkin:
    tracing:
      endpoint: http://zipkin:9411/api/v2/spans
```

---

## Pre-Deployment Checklist

| Setting | Status |
| :--- | :--- |
| ☐ Health checks configured | |
| ☐ Graceful shutdown enabled | |
| ☐ Connection pool tuned | |
| ☐ Logging set to WARN/INFO | |
| ☐ Security headers enabled | |
| ☐ Rate limiting configured | |
| ☐ Metrics exported | |
| ☐ Timeouts set | |
| ☐ Compression enabled | |
| ☐ JVM flags optimized | |
| ☐ Error handling implemented | |
| ☐ Database migrations ready | |
| ☐ Feature flags in place | |
| ☐ Distributed tracing enabled | |

---

## Conclusion

Production readiness isn't optional. Use this checklist before every deployment to avoid outages, performance issues, and security vulnerabilities.

*Deploying to Kubernetes? Check out [K8s-Native Spring Boot]({{ "" | relative_url }}/2026/02/17/kubernetes-native-spring-boot/) or [Zero-Downtime Migrations]({{ "" | relative_url }}/2026/02/02/zero-downtime-database-migrations/).*
