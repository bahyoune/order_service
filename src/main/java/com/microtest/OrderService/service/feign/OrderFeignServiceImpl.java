package com.microtest.OrderService.service.feign;

import com.microtest.event.PaymentStatusEvent;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;


//old_clean_code: PaymentFeignServiceImpl
//new_clean_code: OrderFeignServiceImpl
@Service
public class OrderFeignServiceImpl implements OrderFeignService {

    @Autowired
    private PaymentFeign paymentClient;

    //<editor-fold defaultState="collapsed" desc="">
    //</editor-fold>

    //<editor-fold defaultState="collapsed" desc="Test simple of feign communication">
    //old_clean_code: createPayment
    //new_clean_code: findOrderForProductExist
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
    //old_clean_code: paymentService
    //new_clean_code: paymentStatus
    @CircuitBreaker(name = "paymentStatus", fallbackMethod = "paymentStatusFallback")
    @Retry(name = "paymentStatus")
    @TimeLimiter(name = "paymentStatus")
    public CompletableFuture<PaymentStatusEvent> getPaymentStatus(Long orderId) {
        return CompletableFuture.supplyAsync(() ->
                paymentClient.getPaymentStatus(orderId)
        );
    }

    //old_clean_code: paymentFallback
    //new_clean_code: paymentStatusFallback
    private CompletableFuture<PaymentStatusEvent> paymentStatusFallback(
            Long orderId, Throwable ex) {
        //old_clean_code: Service not available
        //new_clean_code: Payment Service not available
        return CompletableFuture.completedFuture(
                new PaymentStatusEvent(orderId, "Payment Service not available")
        );
    }
    //</editor-fold>

}
