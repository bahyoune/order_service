package com.microtest.OrderService.controller;


import com.microtest.event.OrderCreateEvent;
import com.microtest.event.OrderEvent;
import com.microtest.OrderService.service.OrderService;
import com.microtest.OrderService.service.feign.OrderFeignService;
import com.microtest.event.PaymentStatusEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/internal/v1/orders")
@RefreshScope
public class OrderController {

    @Autowired
    private OrderService orderService;

    //old_clean_code: paymentFeignService
    //new_clean_code: orderFeignService
    @Autowired
    private OrderFeignService orderFeignService;


    //old_clean_code: keyFix
    //new_clean_code: messageFix
    @Value("${custom.msg}")
    private String messageFix;


    //Test of Feign message without a circuit breaker
    //if productId = check, order placed : order cancel
    //Data is not persist in database
    //---------------------
    //old_clean_code: createPayment
    //new_clean_code: findOrderForProductExist
    //in Prod adding feign in not recommended
    // /{productId}/exist
    @PostMapping("/feign/{productId}")
    public String findOrderForProductExist(@PathVariable String productId) {
        return orderFeignService.findOrderForProductExist(productId);
    }

    //Feign message with circuit breaker, retry and time limiter
    @GetMapping("/{id}/payment-status")
    public CompletableFuture<PaymentStatusEvent> paymentStatus(@PathVariable Long id) {
        return orderFeignService.getPaymentStatus(id);
    }




    //Test for Kafka message is working
    //if orderId = 1, trigger DLT error
    //Data is not persist in database
    //-------------
    //old_clean_code: sendEvent
    //new_clean_code: publishOrderInKafka
    //in Prod replace /order with ""
    @PostMapping("/order")
    public CompletableFuture<PaymentStatusEvent> publishOrderInKafka(@RequestBody OrderEvent msg) {
      return   orderService.publishOrderInKafka(msg);
    }

    //Saga Pattern for Kafka
    //if Order is create check
    //Payment  confirm, and send an event to confirm the order
    //Payment failed, send an event to cancel the order
    //amount > 500 ? Payment confirm : Payment Failed
    //Data in database
    //-------------
    //old_clean_code: sendEvent
    //new_clean_code: createOrderWithSagaPattern
    //in Prod replace /saga with ""
    @PostMapping("/saga")
    public CompletableFuture<PaymentStatusEvent> createOrderWithSagaPattern(@RequestBody OrderCreateEvent msg) {
        return orderService.createOrderWithSagaPattern(msg.userId(), msg.amount());
    }

    //Show fixed response
    @GetMapping("/msg")
    public String showMsg() {
        return messageFix;
    }

}
