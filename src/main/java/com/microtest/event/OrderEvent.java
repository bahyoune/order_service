package com.microtest.event;

import lombok.Builder;

@Builder
public record OrderEvent(
        String orderId,
        String userId,
        double amount
) { }
