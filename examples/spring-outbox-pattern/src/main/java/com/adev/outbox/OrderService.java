package com.adev.outbox;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {
    private final OutboxRepository outboxRepository;

    public OrderService(OutboxRepository outboxRepository) {
        this.outboxRepository = outboxRepository;
    }

    @Transactional
    public void createOrder(String orderId, String customerId) {
        // 1. Save order to database (simulated)
        System.out.println("Order saved: " + orderId);

        // 2. Write event to outbox table in SAME transaction
        var event = new OutboxEvent(
            orderId,
            "OrderCreated",
            "{\"orderId\":\"" + orderId + "\",\"customerId\":\"" + customerId + "\"}"
        );
        outboxRepository.save(event);

        // Both DB and outbox are committed atomically!
    }
}
