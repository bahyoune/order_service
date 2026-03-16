package com.microtest.OrderService.service.impl;

import com.microtest.OrderService.service.OrderFeignService;
import com.microtest.OrderService.feign.PaymentFeign;
import com.microtest.event.PaymentStatusEvent;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;


@Service
public class OrderFeignServiceImpl implements OrderFeignService {

    @Autowired
    private PaymentFeign paymentClient;

    //<editor-fold defaultState="collapsed" desc="">
    //</editor-fold>

    //<editor-fold defaultState="collapsed" desc="Test simple of feign communication">
    public String findOrderForProductExist(String productId) {
        ResponseEntity<Boolean> available = paymentClient.isIdPaymentExist(productId);

        if (Boolean.TRUE.equals(available.getBody())) {
            return "Order placed successfully for " + productId;
        } else {
            return "Product " + productId + " is out of stock";
        }
    }
    //</editor-fold>


    //<editor-fold defaultState="collapsed" desc="Feign communication with circuit breaker">
    @Retry(name = "paymentStatus")
    @CircuitBreaker(name = "paymentStatus", fallbackMethod = "paymentStatusFallback")
    @TimeLimiter(name = "paymentStatus")
    public CompletableFuture<PaymentStatusEvent> getPaymentStatus(Long orderId) {
        return  CompletableFuture.supplyAsync(
                () -> paymentClient.getPaymentStatus(orderId)
        );
    }


    public CompletableFuture<PaymentStatusEvent> paymentStatusFallback(Long orderId, Throwable ex) {
        //Payment Service not available
        return  CompletableFuture.completedFuture(
                PaymentStatusEvent.builder()
                        .OrderId(orderId)
                        .reason("Payment Service not available")
                        .build()
        );
    }
    //</editor-fold>

}
