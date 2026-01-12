package com.microtest.event;

public record OrderCreateEvent(
        Long orderId,
        String userId,
        double amount
) { }
