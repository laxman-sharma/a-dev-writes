---
layout: post
title: "Sagas for Java Microservices: Orchestration vs. Choreography"
date: 2026-02-14
author: Laxman Sharma
image: /assets/images/sagas-hero.png
categories: [java, distributed-systems, microservices]
tags: [java, microservices, saga, distributed-transactions, events]
excerpt: "Handle distributed transactions without the performance hit of 2PC. Learn when to use Saga orchestration vs choreography for reliable cross-service workflows."
---

# Distributed Transactions: The 2PC Trap

You need to create an order that:
1. Reserves inventory
2. Charges payment
3. Ships the order

If any step fails, you need to undo the previous steps. **Two-Phase Commit (2PC)** can do this, but it:
- Locks resources across services
- Fails if any participant is unavailable
- Kills performance

**Sagas** are the modern alternative.

---

![Saga Pattern Visualization]({{ "" | relative_url }}/assets/images/sagas-hero.png)

---

## What is a Saga?

A Saga is a sequence of local transactions where each step publishes an event. If a step fails, **compensating transactions** undo the previous steps.

### Example: Order Saga
1. **Reserve Inventory** → Success
2. **Charge Payment** → Fails
3. **Compensate: Release Inventory** → Rollback complete

---

## Orchestration vs. Choreography

### Orchestration: Central Coordinator
A single service (orchestrator) controls the workflow.

```java
@Service
public class OrderSagaOrchestrator {
    
    public void createOrder(Order order) {
        try {
            inventoryService.reserve(order.getItems());
            paymentService.charge(order.getTotal());
            shippingService.ship(order);
        } catch (Exception e) {
            // Compensate in reverse order
            paymentService.refund(order.getTotal());
            inventoryService.release(order.getItems());
            throw new SagaFailedException(e);
        }
    }
}
```

**Pros**: Easy to understand, centralized failure handling  
**Cons**: Single point of failure, orchestrator becomes a bottleneck

### Choreography: Event-Driven
Services listen to events and react independently.

```java
@Service
public class InventoryService {
    
    @EventListener
    public void onOrderCreated(OrderCreatedEvent event) {
        try {
            reserveInventory(event.getOrder());
            eventBus.publish(new InventoryReservedEvent(event.getOrderId()));
        } catch (Exception e) {
            eventBus.publish(new InventoryReservationFailedEvent(event.getOrderId()));
        }
    }
    
    @EventListener
    public void onPaymentFailed(PaymentFailedEvent event) {
        releaseInventory(event.getOrderId());  // Compensate
    }
}
```

**Pros**: No single point of failure, scales better  
**Cons**: Harder to debug, eventual consistency

---

## Implementing with Spring State Machine

For orchestration, Spring State Machine is ideal:

```java
@Configuration
@EnableStateMachine
public class OrderSagaConfig extends StateMachineConfigurerAdapter<States, Events> {

    @Override
    public void configure(StateMachineStateConfigurer<States, Events> states) throws Exception {
        states
            .withStates()
            .initial(States.PENDING)
            .state(States.INVENTORY_RESERVED)
            .state(States.PAYMENT_CHARGED)
            .end(States.COMPLETED)
            .end(States.FAILED);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<States, Events> transitions) throws Exception {
        transitions
            .withExternal()
                .source(States.PENDING).target(States.INVENTORY_RESERVED)
                .event(Events.RESERVE_INVENTORY)
                .action(context -> inventoryService.reserve())
            .and()
            .withExternal()
                .source(States.INVENTORY_RESERVED).target(States.PAYMENT_CHARGED)
                .event(Events.CHARGE_PAYMENT)
                .action(context -> paymentService.charge())
            .and()
            .withExternal()
                .source(States.PAYMENT_CHARGED).target(States.COMPLETED)
                .event(Events.SHIP_ORDER)
                .action(context -> shippingService.ship());
    }
}
```

---

## Compensation Strategies

| Failure Point | Compensation |
| :--- | :--- |
| **Inventory reserved, payment fails** | Release inventory |
| **Payment charged, shipping fails** | Refund payment, release inventory |
| **All steps succeed, user cancels** | Full rollback via compensations |

---

## When to Choose What

| Pattern | Best For |
| :--- | :--- |
| **Orchestration** | Complex workflows with many conditional branches |
| **Choreography** | Simple, linear workflows; high-scale event-driven systems |

---

## Conclusion

Sagas sacrifice immediate consistency for availability and performance. By accepting **eventual consistency** and designing proper compensations, you can build resilient distributed systems without the baggage of 2PC.

*Need more distributed patterns? Check out [Transactional Outbox]({{ "" | relative_url }}/2026/01/30/transactional-outbox-pattern/) or [Zero-Downtime Migrations]({{ "" | relative_url }}/2026/02/02/zero-downtime-database-migrations/).*
