---
layout: post
title: "The Transactional Outbox Pattern: Guaranteed Message Delivery"
date: 2026-01-30
author: Laxman Sharma
image: /assets/images/outbox-pattern-hero.png
categories: [java, distributed-systems, spring-boot]
tags: [java, spring-boot, kafka, microservices, distributed-systems, patterns]
excerpt: "Solve the 'DB updated but Kafka failed' problem in microservices. Learn how the Transactional Outbox Pattern guarantees message delivery without distributed transactions."
---

# The Dual-Write Problem

You've built a microservice that handles order creation. The requirements are simple:
1.  Save the order to the database.
2.  Send an `OrderCreated` event to Kafka so other services can react.

The naive implementation looks like this:

```java
@Transactional
public void createOrder(Order order) {
    orderRepository.save(order);       // Step 1: DB write
    kafkaTemplate.send("orders", order); // Step 2: Kafka write
}
```

**What could go wrong?**

### Failure Scenario 1: Kafka is Down
The database commits successfully, but Kafka is unavailable. The order exists in your DB, but no event was published. Other services (inventory, shipping) never find out about this order.

### Failure Scenario 2: App Crashes Between Steps
The database transaction commits, but the app crashes before the Kafka call. Same result: **data inconsistency**.

This is the **Dual-Write Problem**: You cannot atomically commit to two separate systems (DB + Kafka).

---

![Dual-Write Problem vs. Outbox Solution]({{ "" | relative_url }}/assets/images/outbox-pattern-hero.png)

---

## The Solution: Transactional Outbox

The Outbox Pattern solves this by moving both writes into a **single database transaction**:

1.  Save the order to the `orders` table.
2.  Save the event to an `outbox` table (in the same transaction).
3.  A background process polls the `outbox` table and publishes events to Kafka.

If the transaction fails, both the order and the event are rolled back. If it succeeds, the event is guaranteed to eventually reach Kafka.

### The Outbox Table Schema

```sql
CREATE TABLE outbox (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    aggregate_id VARCHAR(255),
    event_type VARCHAR(255),
    payload TEXT,
    created_at TIMESTAMP,
    published BOOLEAN DEFAULT FALSE
);
```

---

## Implementation with Spring Boot

Our demo project (`examples/spring-outbox-pattern/`) demonstrates a complete implementation.

### Step 1: Write to Outbox in the Same Transaction

```java
@Service
public class OrderService {
    private final OutboxRepository outboxRepository;

    @Transactional
    public void createOrder(String orderId, String customerId) {
        // 1. Save business data (simulated)
        System.out.println("Order saved: " + orderId);

        // 2. Write event to outbox (SAME transaction)
        var event = new OutboxEvent(
            orderId,
            "OrderCreated",
            "{\"orderId\":\"" + orderId + "\",\"customerId\":\"" + customerId + "\"}"
        );
        outboxRepository.save(event);

        // Both commit atomically via @Transactional
    }
}
```

### Step 2: Scheduled Poller Publishes Events

```java
@Service
public class OutboxPublisher {
    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 5000) // Poll every 5 seconds
    @Transactional
    public void publishEvents() {
        var unpublished = outboxRepository
            .findTop100ByPublishedFalseOrderByCreatedAtAsc();

        for (var event : unpublished) {
            try {
                kafkaTemplate.send("order-events", event.getPayload()).get();
                event.setPublished(true);
                outboxRepository.save(event);
            } catch (Exception e) {
                // Event stays unpublished, will retry next poll
            }
        }
    }
}
```

---

## Why This Works

### Atomicity Guarantee
Since the outbox write is in the same transaction as the business data, either both succeed or both fail. You can never have a partial state.

### Eventual Consistency
The event might not reach Kafka immediately (if Kafka is down), but it will eventually get there once the publisher succeeds.

### At-Least-Once Delivery
If the publisher crashes after sending to Kafka but before marking the event as `published`, the event will be sent again. Consumers must be **idempotent**.

---

## Alternatives and Trade-offs

| Pattern | Atomicity | Complexity | Latency |
| :--- | :--- | :--- | :--- |
| **Dual-Write** | ❌ No Guarantee | Low | Immediate |
| **Transactional Outbox** | ✅ Guaranteed | Medium | Seconds (polling delay) |
| **Change Data Capture (Debezium)** | ✅ Guaranteed | High | Near real-time |

**When to use Outbox**:
-   You need guaranteed delivery without CDC infrastructure.
-   Polling latency (5-10 seconds) is acceptable.
-   You want to avoid distributed transactions (2PC).

**When to use CDC instead**:
-   You need sub-second latency.
-   You already have Debezium/Kafka Connect set up.

---

## Conclusion: Reliability Over Speed

The Transactional Outbox Pattern trades a small latency increase for **bulletproof reliability**. In distributed systems, this is almost always the right trade-off.

If you're building microservices that need to reliably propagate events, the Outbox Pattern should be your default choice.

---

*Ready to build resilient systems? Check out our next article on [Zero-Downtime Database Migrations](#) or revisit [Structured Concurrency]({{ "" | relative_url }}/2026/01/27/structured-concurrency-java/) for safe parallelism.*
