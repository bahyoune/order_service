package com.microtest.OrderService.service;


import com.microtest.event.PaymentStatusEvent;

import java.util.concurrent.CompletableFuture;


public interface OrderFeignService {
    String findOrderForProductExist(String productId);

    CompletableFuture<PaymentStatusEvent> getPaymentStatus(Long orderId);
}
