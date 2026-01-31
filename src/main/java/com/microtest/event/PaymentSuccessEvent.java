package com.microtest.event;

import lombok.Builder;

@Builder
public record PaymentSuccessEvent(Long OrderId) {
}
