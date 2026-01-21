package com.microtest.event;

public record PaymentStatusEvent(Long OrderId, String reason) { }
