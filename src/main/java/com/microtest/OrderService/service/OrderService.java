package com.microtest.OrderService.service;

import com.microtest.OrderService.bean.Orders;
import com.microtest.event.OrderEvent;
import com.microtest.event.PaymentStatusEvent;
import com.microtest.event.PaymentSuccessEvent;

import java.util.concurrent.CompletableFuture;

public interface OrderService {


    void handlePaymentSuccess(PaymentStatusEvent event);

    void handlePaymentFailed(PaymentStatusEvent event);

    CompletableFuture<PaymentStatusEvent> sendOrder(OrderEvent event);

    CompletableFuture<PaymentStatusEvent> createOrder(String userId, double amount);
}
