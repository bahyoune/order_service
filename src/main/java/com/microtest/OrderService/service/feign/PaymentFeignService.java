package com.microtest.OrderService.service.feign;


import com.microtest.event.PaymentStatusEvent;

import java.util.concurrent.CompletableFuture;

public interface PaymentFeignService {
    String createPayment(String productId);

    CompletableFuture<PaymentStatusEvent> getPaymentStatus(Long orderId);
}
