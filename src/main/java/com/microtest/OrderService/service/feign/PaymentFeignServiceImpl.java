package com.microtest.OrderService.service.feign;

import com.microtest.event.PaymentStatusEvent;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;


@Service
public class PaymentFeignServiceImpl implements PaymentFeignService {

    @Autowired
    private PaymentFeign paymentClient;

    //<editor-fold defaultState="collapsed" desc="Change, Forgot Password">


    //</editor-fold>

    //<editor-fold defaultState="collapsed" desc="Test simple of feign communication">
    public String createPayment(String productId) {

        ResponseEntity<Boolean> available = paymentClient.getTestPayment(productId);

        if (Boolean.TRUE.equals(available.getBody())) {
            return "Order placed successfully for " + productId;
        } else {
            return "Product " + productId + " is out of stock";
        }
    }
    //</editor-fold>


    //<editor-fold defaultState="collapsed" desc="Feign communication with circuit breaker">
    @CircuitBreaker(name = "paymentService", fallbackMethod = "paymentFallback")
    @Retry(name = "paymentService")
    @TimeLimiter(name = "paymentService")
    public CompletableFuture<PaymentStatusEvent> getPaymentStatus(Long orderId) {
        return CompletableFuture.supplyAsync(() ->
                paymentClient.getPaymentStatus(orderId)
        );
    }

    private CompletableFuture<PaymentStatusEvent> paymentFallback(
            Long orderId, Throwable ex) {
        return CompletableFuture.completedFuture(
                new PaymentStatusEvent(orderId, "UNKNOWN")
        );
    }
    //</editor-fold>

}
