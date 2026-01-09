package com.adev.outbox;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/orders")
    public String createOrder(@RequestParam String customerId) {
        String orderId = "ORD-" + System.currentTimeMillis();
        orderService.createOrder(orderId, customerId);
        return "Order created: " + orderId;
    }
}
