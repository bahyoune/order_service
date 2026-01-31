package com.microtest.event;

import lombok.Builder;

@Builder
public record PaymentStatusEvent(Long OrderId, String reason) { }
