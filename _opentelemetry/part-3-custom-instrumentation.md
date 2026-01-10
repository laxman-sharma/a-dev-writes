---
layout: dsa_post
course: opentelemetry
title: "Part 3: Custom Instrumentation"
category: "Module 2: Implementation"
order: 3
date: 2026-01-10
author: Laxman Sharma
image: /assets/images/otel-part3-hero.png
categories: [observability, java]
tags: [opentelemetry, java, observability, spring-boot, custom-attributes]
excerpt: "Adding manual spans and attributes."
---

# Adding Custom Instrumentation

*Part 3 of an 8-part series on implementing observability in Java microservices*

---

In [Part 2]({{ "" | relative_url }}/2026/01/09/opentelemetry-java-part-2-zero-to-tracing/), we got distributed tracing working with zero code changes. But the auto-generated spans are generic: `POST /orders`, `GET /inventory`. 

When debugging production issues, you need **business context**: *Which order failed? Which customer? What was the payment amount?*

That's where manual instrumentation comes in.

> [!TIP]
> **Hands-On Example**: The [`otel-demo`](https://github.com/laxman-sharma/otel-demo) services demonstrate real-world custom instrumentation with `@WithSpan`, business attributes, and error handling. See `OrderController.java` for practical examples.

## When Auto-Instrumentation Isn't Enough

Auto-instrumentation is fantastic for:
- HTTP requests/responses
- Database query timing
- Message queue operations

But it **doesn't know** about your business logic:
- Order IDs, customer IDs, SKUs
- Business validation steps
- Internal processing phases
- Custom error contexts

## Adding the OTel SDK Dependency

First, add the OpenTelemetry SDK to your project:

```xml
<!-- Maven -->
<dependency>
    <groupId>io.opentelemetry.instrumentation</groupId>
    <artifactId>opentelemetry-instrumentation-annotations</artifactId>
    <version>2.1.0</version>
</dependency>
```

```groovy
// Gradle
implementation 'io.opentelemetry.instrumentation:opentelemetry-instrumentation-annotations:2.1.0'
```

> **Note**: When using the Java Agent, you don't need the full SDK—just the annotations library. The agent provides the runtime.

## Method 1: @WithSpan Annotation (Easiest)

The simplest way to add custom spans is with the `@WithSpan` annotation:

```java
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;

@Service
public class OrderService {
    
    @WithSpan("process-order")
    public Order processOrder(
            @SpanAttribute("order.customer_id") String customerId,
            @SpanAttribute("order.amount") BigDecimal amount) {
        
        validateOrder(customerId, amount);
        Order order = createOrder(customerId, amount);
        sendConfirmation(order);
        
        return order;
    }
    
    @WithSpan("validate-order")
    private void validateOrder(String customerId, BigDecimal amount) {
        // Validation logic
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOrderException("Amount must be positive");
        }
    }
}
```

Now your trace shows:

```
POST /orders
└── process-order [customer_id: "cust-123", amount: 99.99]
    └── validate-order
```

## Method 2: Programmatic Spans (Full Control)

For more control, create spans programmatically:

```java
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;

@Service
public class PaymentService {
    
    private final Tracer tracer = GlobalOpenTelemetry.getTracer("payment-service");
    
    public PaymentResult processPayment(String orderId, BigDecimal amount) {
        Span span = tracer.spanBuilder("process-payment")
            .setAttribute("order.id", orderId)
            .setAttribute("payment.amount", amount.doubleValue())
            .setAttribute("payment.currency", "USD")
            .startSpan();
        
        try (Scope scope = span.makeCurrent()) {
            // Your payment logic
            PaymentResult result = callPaymentGateway(amount);
            
            span.setAttribute("payment.transaction_id", result.getTransactionId());
            span.setAttribute("payment.status", result.getStatus());
            
            return result;
            
        } catch (PaymentException e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getMessage());
            throw e;
            
        } finally {
            span.end();
        }
    }
}
```

## Adding Events to Spans

Events are timestamped annotations within a span—perfect for marking checkpoints:

```java
@WithSpan("process-order")
public Order processOrder(OrderRequest request) {
    Span currentSpan = Span.current();
    
    // Mark validation complete
    currentSpan.addEvent("order.validated", Attributes.of(
        AttributeKey.stringKey("validation.result"), "passed"
    ));
    
    // Mark inventory reserved
    reserveInventory(request.getSku());
    currentSpan.addEvent("inventory.reserved");
    
    // Mark payment processed
    processPayment(request);
    currentSpan.addEvent("payment.processed", Attributes.of(
        AttributeKey.stringKey("payment.method"), request.getPaymentMethod()
    ));
    
    return createOrder(request);
}
```

In Jaeger, you'll see these events as markers on the span timeline.

## Recording Exceptions

Always record exceptions for debugging:

```java
try {
    riskyOperation();
} catch (Exception e) {
    Span.current().recordException(e, Attributes.of(
        AttributeKey.stringKey("exception.context"), "During inventory check"
    ));
    Span.current().setStatus(StatusCode.ERROR, "Inventory check failed");
    throw e;
}
```

This adds the full stack trace to your span, visible in Jaeger.

## Best Practices

### 1. Use Semantic Conventions

OpenTelemetry defines standard attribute names. Use them:

```java
// Good - follows semantic conventions
span.setAttribute("http.request.method", "POST");
span.setAttribute("db.system", "postgresql");
span.setAttribute("messaging.system", "kafka");

// Avoid - custom names that aren't standard
span.setAttribute("method", "POST");
span.setAttribute("database", "postgresql");
```

### 2. Mind Your Span Names

Span names should be **low-cardinality**:

```java
// Good - fixed name
tracer.spanBuilder("get-order-by-id")

// Bad - high cardinality (includes variable data)
tracer.spanBuilder("get-order-" + orderId)
```

Put variable data in **attributes**, not span names.

### 3. Don't Over-Instrument

Every span has overhead. Focus on:
- Business operations (not every helper method)
- External calls (APIs, databases)
- Long-running processes
- Error-prone code paths

### 4. Add Context, Not Noise

Good attributes help debugging:

```java
span.setAttribute("order.id", orderId);
span.setAttribute("order.item_count", items.size());
span.setAttribute("customer.tier", customer.getTier());
```

## Context Propagation & Baggage (The "Hidden" Power)

One of the most powerful—and often overlooked—features of OpenTelemetry is **Baggage**.

While **Attributes** are attached to a single *Span* (and stay there), **Baggage** travels with the *Context* across process boundaries (HTTP headers).

### The Problem it Solves
Imagine you have a chain of 5 microservices:
`Frontend -> Auth -> Order -> Inventory -> Shipping`

You want to know: *"Which specific customer ID triggered this shipping request?"*
But the `Shipping` service doesn't have the `HttpServletRequest` with the auth token. It just got an internal gRPC call.

Do you change every method signature to pass `customerId` down the stack? **No.** You use Baggage.

```java
import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.context.Scope;

// In Service A (Entry Point)
public void handleRequest(String userId) {
    // Put userId into Baggage
    // It will now auto-propagate to Service B, C, D... via HTTP headers
    try (Scope scope = Baggage.current().toBuilder()
            .put("app.user_id", userId)
            .put("app.is_vip", "true")
            .build()
            .makeCurrent()) {
        
        callDownstreamService();
    }
}

// In Service D (Deep Downstream)
public void processShipment() {
    // Retrieve from Baggage - no method arguments needed!
    String userId = Baggage.current().getEntryValue("app.user_id");
    
    // Add it to the current span so it shows up in Jaeger
    Span.current().setAttribute("user.id", userId);
}
```

> **Warning**: Baggage is serialized into headers. Do not put large objects or sensitive PII (like passwords) in Baggage. Use it for IDs, flags, and trace context.


## Complete Example: Order Processing

Here's a realistic example combining all techniques:

```java
@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final Tracer tracer = GlobalOpenTelemetry.getTracer("order-service");
    private final InventoryClient inventoryClient;
    private final PaymentClient paymentClient;
    
    @WithSpan("create-order")
    public Order createOrder(
            @SpanAttribute("order.customer_id") String customerId,
            @SpanAttribute("order.sku") String sku,
            @SpanAttribute("order.quantity") int quantity) {
        
        Span span = Span.current();
        String orderId = UUID.randomUUID().toString();
        span.setAttribute("order.id", orderId);
        
        // Check inventory
        checkInventory(sku, quantity);
        span.addEvent("inventory.checked");
        
        // Calculate pricing
        BigDecimal total = calculateTotal(sku, quantity);
        span.setAttribute("order.total", total.doubleValue());
        
        // Process payment
        String paymentId = processPayment(customerId, total);
        span.setAttribute("payment.id", paymentId);
        span.addEvent("payment.completed");
        
        // Save order
        Order order = saveOrder(orderId, customerId, sku, quantity, total);
        span.addEvent("order.saved");
        
        return order;
    }
    
    private void checkInventory(String sku, int quantity) {
        Span span = tracer.spanBuilder("check-inventory")
            .setAttribute("inventory.sku", sku)
            .setAttribute("inventory.quantity_requested", quantity)
            .startSpan();
        
        try (Scope scope = span.makeCurrent()) {
            boolean available = inventoryClient.check(sku, quantity);
            span.setAttribute("inventory.available", available);
            
            if (!available) {
                throw new InsufficientInventoryException(sku);
            }
        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR);
            throw e;
        } finally {
            span.end();
        }
    }
}
```

## What's Next

You now have rich, contextual traces. But observability is more than tracing.

In **Part 4**, we'll add **custom metrics**:
- Request counters
- Latency histograms
- Business gauges (orders per minute, active users)

We'll export to Prometheus and build dashboards in Grafana.

---

## Resources

- [OTel Instrumentation Annotations](https://opentelemetry.io/docs/languages/java/automatic/annotations/)
- [Semantic Conventions](https://opentelemetry.io/docs/concepts/semantic-conventions/)
- [Span API Reference](https://opentelemetry.io/docs/languages/java/api/)
