package com.microtest.OrderService.service.feign;


import com.microtest.event.PaymentStatusEvent;

import java.util.concurrent.CompletableFuture;

//old_clean_code: PaymentFeignService
//new_clean_code: OrderFeignService
public interface OrderFeignService {
    String findOrderForProductExist(String productId);

    CompletableFuture<PaymentStatusEvent> getPaymentStatus(Long orderId);
}
