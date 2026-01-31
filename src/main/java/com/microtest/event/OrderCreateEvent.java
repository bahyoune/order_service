package com.microtest.event;

import lombok.Builder;

@Builder
public record OrderCreateEvent(
        Long orderId,
        String userId,
        double amount
) { }
