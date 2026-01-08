---
layout: post
title: "OpenTelemetry with Java: Part 5 - Logs in Context"
date: 2026-01-12
author: Laxman Sharma
image: /assets/images/otel-part5-hero.png
categories: [observability, java]
tags: [opentelemetry, logging, structured-logging, trace-correlation]
excerpt: "Unify your logs with traces. Learn to add trace IDs to log output, configure structured JSON logging, and correlate logs with distributed traces."
---

# Logs in Context

*Part 5 of a 6-part series on implementing observability in Java microservices*

---

You have traces showing request flow and metrics showing system health. But when something goes wrong, you still `grep` through logs. Wouldn't it be nice if those logs were automatically connected to traces?

That's what we're building today.

## The Problem: Disconnected Logs

Traditional logging:

```
2026-01-12 10:15:30 [order-service] ERROR Payment failed for order
2026-01-12 10:15:30 [payment-service] ERROR Insufficient funds
```

Questions this doesn't answer:
- Were these for the same request?
- What was the full request flow?
- What else happened in this transaction?

## The Solution: Trace-Correlated Logs

With OpenTelemetry context:

```
2026-01-12 10:15:30 [order-service] traceId=abc123 spanId=def456 ERROR Payment failed for order
2026-01-12 10:15:30 [payment-service] traceId=abc123 spanId=ghi789 ERROR Insufficient funds
```

Now you can:
- Filter all logs for a single trace
- Jump from a trace to its logs
- See the complete picture

## How It Works

The OTel Java Agent automatically puts trace context into the MDC (Mapped Diagnostic Context). Your logging framework reads from MDC.

```
┌─────────────────────────────────────────────────┐
│               Your Application                   │
│  ┌─────────────────────────────────────────┐    │
│  │         OTel Java Agent                  │    │
│  │    (populates MDC with trace context)    │    │
│  └─────────────────────────────────────────┘    │
│                       ↓                          │
│  ┌─────────────────────────────────────────┐    │
│  │    Logback / Log4j2                      │    │
│  │    (reads trace_id, span_id from MDC)    │    │
│  └─────────────────────────────────────────┘    │
└─────────────────────────────────────────────────┘
```

## Configuration: Logback

Create or update `src/main/resources/logback-spring.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    
    <!-- Console output with trace context -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>
                %d{ISO8601} [%thread] %-5level %logger{36} - traceId=%X{trace_id} spanId=%X{span_id} - %msg%n
            </pattern>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
    </root>
    
</configuration>
```

Now every log line includes trace context:

```
2026-01-12T10:15:30.123 [main] INFO  c.e.OrderService - traceId=a1b2c3d4e5f6 spanId=1a2b3c4d - Processing order
```

## JSON Structured Logging

For log aggregation systems (ELK, Loki, Datadog), use JSON:

### Add Logstash Encoder

```xml
<dependency>
    <groupId>net.logstash.logback</groupId>
    <artifactId>logstash-logback-encoder</artifactId>
    <version>7.4</version>
</dependency>
```

### Configure JSON Output

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    
    <appender name="JSON" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <includeMdcKeyName>trace_id</includeMdcKeyName>
            <includeMdcKeyName>span_id</includeMdcKeyName>
            <includeMdcKeyName>trace_flags</includeMdcKeyName>
            
            <customFields>{"service":"order-service"}</customFields>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="JSON"/>
    </root>
    
</configuration>
```

Output:

```json
{
  "@timestamp": "2026-01-12T10:15:30.123Z",
  "level": "INFO",
  "logger_name": "com.example.OrderService",
  "message": "Processing order",
  "trace_id": "a1b2c3d4e5f67890a1b2c3d4e5f67890",
  "span_id": "1a2b3c4d5e6f7890",
  "service": "order-service"
}
```

## Configuration: Log4j2

If you use Log4j2, here's the equivalent:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration>
    <Appenders>
        <Console name="Console" target="SYSTEM_OUT">
            <PatternLayout pattern="%d{ISO8601} [%t] %-5level %logger{36} - traceId=%X{trace_id} spanId=%X{span_id} - %msg%n"/>
        </Console>
    </Appenders>
    
    <Loggers>
        <Root level="info">
            <AppenderRef ref="Console"/>
        </Root>
    </Loggers>
</Configuration>
```

## Adding Context to Logs Programmatically

Add business context alongside trace context:

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

@Service
public class OrderService {
    
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    
    public Order createOrder(OrderRequest request) {
        // Add business context to MDC
        MDC.put("order.id", request.getOrderId());
        MDC.put("customer.id", request.getCustomerId());
        
        try {
            log.info("Starting order processing");
            
            validateOrder(request);
            log.info("Order validated");
            
            processPayment(request);
            log.info("Payment processed");
            
            return saveOrder(request);
            
        } catch (Exception e) {
            log.error("Order processing failed", e);
            throw e;
            
        } finally {
            // Clean up MDC
            MDC.remove("order.id");
            MDC.remove("customer.id");
        }
    }
}
```

Output includes both trace and business context:

```json
{
  "message": "Payment processed",
  "trace_id": "a1b2c3d4e5f67890",
  "span_id": "1a2b3c4d5e6f7890",
  "order.id": "ORD-12345",
  "customer.id": "CUST-67890"
}
```

## Viewing Logs with Traces

### In Jaeger

Jaeger 1.35+ supports logs. Configure your collector to receive logs:

```yaml
receivers:
  otlp:
    protocols:
      grpc:

exporters:
  jaeger:
    endpoint: jaeger:14250

service:
  pipelines:
    logs:
      receivers: [otlp]
      exporters: [jaeger]
```

### In Grafana (with Loki)

```yaml
# Add Loki to docker-compose.yml
loki:
  image: grafana/loki:2.9.0
  ports:
    - "3100:3100"
```

In Grafana, you can then:
1. View a trace
2. Click "Logs for this trace"
3. See all logs with matching trace_id

## Best Practices

### 1. Log at Appropriate Levels

```java
log.debug("Detailed processing info: {}", details);  // Dev debugging
log.info("Order created: {}", orderId);              // Normal operations
log.warn("Retry attempt {} for payment", attempt);   // Potential issues
log.error("Payment gateway timeout", exception);      // Errors
```

### 2. Use Structured Fields

```java
// Good - structured
log.info("Order processed", kv("orderId", orderId), kv("amount", amount));

// Avoid - unstructured
log.info("Order " + orderId + " processed for $" + amount);
```

### 3. Don't Log Sensitive Data

```java
// Bad
log.info("User authenticated with password: {}", password);

// Good
log.info("User authenticated: userId={}", userId);
```

### 4. Include Error Context

```java
try {
    processPayment(order);
} catch (PaymentException e) {
    log.error("Payment failed for order={}, amount={}", 
              order.getId(), order.getAmount(), e);
    throw e;
}
```

## What's Next

You now have the complete observability trifecta:
- **Traces**: Request flow across services
- **Metrics**: System-wide measurements
- **Logs**: Detailed event records

All correlated by trace ID.

In **Part 6**, we'll productionize everything with the **OpenTelemetry Collector**:
- Centralized telemetry pipeline
- Sampling strategies
- Multi-backend export

---

*Previous: [Part 4 - Metrics That Matter]({{ site.url }}{{ site.baseurl }}/2026/01/11/opentelemetry-java-part-4-metrics/)*

*Next: [Part 6 - Production Patterns]({{ site.url }}{{ site.baseurl }}/2026/01/13/opentelemetry-java-part-6-production/)*

---

## Resources

- [OTel Logging](https://opentelemetry.io/docs/specs/otel/logs/)
- [Logback MDC](https://logback.qos.ch/manual/mdc.html)
- [Structured Logging Best Practices](https://www.slf4j.org/faq.html#logging_performance)
