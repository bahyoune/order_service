package com.microtest.OrderService.service;

import com.microtest.OrderService.bean.Orders;
import com.microtest.event.OrderEvent;
import com.microtest.event.PaymentStatusEvent;
import com.microtest.event.PaymentSuccessEvent;

import java.util.concurrent.CompletableFuture;

public interface OrderService {


    void handlePaymentSuccess(PaymentStatusEvent event);

    void handlePaymentFailed(PaymentStatusEvent event);

    CompletableFuture<PaymentStatusEvent> publishOrderInKafka(OrderEvent event);

    CompletableFuture<PaymentStatusEvent> createOrderWithSagaPattern(String userId, double amount);
}
