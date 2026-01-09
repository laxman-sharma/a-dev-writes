package com.adev.outbox;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OutboxPublisher {
    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxPublisher(OutboxRepository outboxRepository, KafkaTemplate<String, String> kafkaTemplate) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 5000) // Poll every 5 seconds
    @Transactional
    public void publishEvents() {
        var unpublished = outboxRepository.findTop100ByPublishedFalseOrderByCreatedAtAsc();

        for (var event : unpublished) {
            try {
                // Publish to Kafka
                kafkaTemplate.send("order-events", event.getAggregateId(), event.getPayload()).get();
                
                // Mark as published
                event.setPublished(true);
                outboxRepository.save(event);
                
                System.out.println("Published event: " + event.getId());
            } catch (Exception e) {
                System.err.println("Failed to publish event: " + event.getId() + " - " + e.getMessage());
                // Event stays unpublished, will retry next poll
            }
        }
    }
}
