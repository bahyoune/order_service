package com.microtest.OrderService.controller;


import com.microtest.OrderService.exception.KafkaErrorPublishException;
import com.microtest.event.OrderCreateEvent;
import com.microtest.OrderService.service.OrderService;
import com.microtest.OrderService.service.OrderFeignService;
import com.microtest.event.OrderEvent;
import com.microtest.event.PaymentStatusEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/internal/v1/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderFeignService orderFeignService;

    @Value("${custom.msg}")
    private String messageFix;

    //Test of Feign message without a circuit breaker
    //if productId = check, order placed : order cancel
    //Data is not persist in database
    //---------------------
    //in Prod adding feign in not recommended
    // /{productId}/exist
    @GetMapping("/feign/{productId}")
    public String findOrderForProductExist(@PathVariable String productId) {
        return orderFeignService.findOrderForProductExist(productId);
    }

    //Feign message with circuit breaker, retry and time limiter
    @GetMapping("/{orderId}/payment-status")
    public CompletableFuture<PaymentStatusEvent> paymentStatus(@PathVariable Long orderId) {
       return orderFeignService.getPaymentStatus(orderId);
    }


    //Test for Kafka message is working
    //if orderId = 1, trigger DLT error
    //Data is not persist in database
    //-------------
    //in Prod replace /order with ""
    @PostMapping("/order")
    public ResponseEntity<?> publishOrderInKafka(@RequestBody OrderEvent msg) {
        try {
            PaymentStatusEvent result = orderService.publishOrderInKafka(msg);

            return ResponseEntity.ok(result);
        } catch (KafkaErrorPublishException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("Error", "The Service is not available to process Order"));
        }

    }

    //Saga Pattern for Kafka
    //if Order is create check
    //Payment  confirm, and send an event to confirm the order
    //Payment failed, send an event to cancel the order
    //amount > 500 ? Payment confirm : Payment Failed
    //Data in database
    @PostMapping("/saga")
    public ResponseEntity<?> createOrderWithSagaPattern(@RequestBody OrderCreateEvent event) {
        try {
            PaymentStatusEvent result = orderService.createOrderWithSagaPattern(event);

            return ResponseEntity.ok(result);
        } catch (KafkaErrorPublishException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("Error", "The Service is not available to process Order"));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    //Show fixed response
    @GetMapping("/msg")
    public String showMsg() {
        return messageFix;
    }

}
