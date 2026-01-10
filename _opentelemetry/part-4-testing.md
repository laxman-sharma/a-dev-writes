---
layout: dsa_post
course: opentelemetry
title: "Part 4: Testing & Verification"
category: "Module 2: Implementation"
order: 4
date: 2026-01-11
author: Laxman Sharma
image: /assets/images/otel-part4-hero.png
categories: [observability, java, testing]
tags: [opentelemetry, java, junit, testing, quality]
excerpt: "Unit testing your tracing instrumentation."
---

# Testing Your Observability

*Part 4 of an 8-part series on implementing observability in Java microservices*

---

You write unit tests for your business logic. You write integration tests for your database. **Do you test your Observability?**

If a tree falls in a forest and no one traces it, did it make a sound? If your payment service crashes but the `error=true` attribute wasn't set on the span, your on-call engineer will be flying blind.

In this module, we'll treat **Instrumentation as Code** and test it.

## The Testing SDK

OpenTelemetry provides a lightweight in-memory SDK specifically for testing.

### 1. Add Dependencies

```xml
<dependency>
    <groupId>io.opentelemetry</groupId>
    <artifactId>opentelemetry-sdk-testing</artifactId>
    <version>1.34.1</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-api</artifactId>
    <version>5.10.1</version>
    <scope>test</scope>
</dependency>
```

### 2. Setting up the Test Setup

We use `JUnit 5 Extensions` to spin up a real OTel SDK instance that writes to a `List<SpanData>` in memory instead of Jaeger.

```java
import io.opentelemetry.sdk.testing.junit5.OpenTelemetryExtension;
import org.junit.jupiter.api.RegisterExtension;
import org.junit.jupiter.api.Test;

class OrderServiceTest {

    @RegisterExtension
    static final OpenTelemetryExtension otelTesting = OpenTelemetryExtension.create();

    // Inject the Tracer from our Test SDK
    private final OrderService orderService = new OrderService(otelTesting.getOpenTelemetry().getTracer("test"));

    @Test
    void processOrder_ShouldCreateCorrectSpans() {
        // execute business logic
        orderService.processOrder("user-123", new BigDecimal("99.00"));

        // Assertions
        List<SpanData> spans = otelTesting.getSpans();
        
        // 1. Check a span was created
        assertEquals(1, spans.size());
        
        SpanData span = spans.get(0);
        
        // 2. Check Span Name
        assertEquals("process-order", span.getName());
        
        // 3. Check Critical Attributes
        assertEquals("user-123", span.getAttributes().get(AttributeKey.stringKey("order.customer_id")));
        assertEquals(99.00, span.getAttributes().get(AttributeKey.doubleKey("order.amount")));
        
        // 4. Verify no errors
        assertEquals(StatusCode.UNSET, span.getStatus().getStatusCode());
    }
}
```

## Testing Error Scenarios

The most critical thing to test is **failure visibility**.

```java
@Test
void processOrder_OnError_ShouldRecordException() {
    // Force an error in logic
    assertThrows(InvalidOrderException.class, () -> {
        orderService.processOrder(null, null); 
    });

    List<SpanData> spans = otelTesting.getSpans();
    SpanData span = spans.get(0);

    // Assert that the span is marked as ERROR
    assertEquals(StatusCode.ERROR, span.getStatus().getStatusCode());
    
    // Assert that the exception event was recorded
    boolean hasExceptionEvent = span.getEvents().stream()
        .anyMatch(event -> event.getName().equals("exception"));
        
    assertTrue(hasExceptionEvent, "Span should record exception event");
}
```

## Integration Testing (Spring Boot)

For integration tests where you spin up the full context, you can define a `@TestConfiguration` bean:

```java
@TestConfiguration
public class ObservabilityTestConfig {
    
    @Bean
    public OpenTelemetry openTelemetry() {
        // Returns a testing implementation that prevents OTel 
        // from trying to export to localhost:4317 during CI/CD
        return OpenTelemetry.noop(); 
    }
}
```

## Tips for Testing

1. **Wait for Spans**: The OTel SDK is asynchronous. In tests, use a short `sleep` or an await library (e.g., Awaitility) to ensure spans have been flushed to the in-memory exporter.
2. **Context Propagation**: Verify that the `Trace ID` is consistent across child spans to ensure your context propagation (Baggage/ParentContext) is working as expected.

## Summary

Treat tracing like any other feature. If it's critical for debugging (and it is), it deserves a test.

---

