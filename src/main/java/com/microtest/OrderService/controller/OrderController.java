package com.microtest.OrderService.controller;


import com.microtest.OrderService.bean.Orders;
import com.microtest.event.OrderCreateEvent;
import com.microtest.event.OrderEvent;
import com.microtest.OrderService.service.OrderService;
import com.microtest.OrderService.service.feign.PaymentFeignService;
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

    @Autowired
    private PaymentFeignService paymentFeignService;


    @Value("${custom.msg}")
    private String keyFix;


    //Test of Feign message without a circuit breaker
    //if productId = check, order placed : order cancel
    //Data is not persist in database
    @PostMapping("/feign/{productId}")
    public String createPayment(@PathVariable String productId) {
        return paymentFeignService.createPayment(productId);
    }

    //Feign message with circuit breaker, retry and time limiter
    @GetMapping("/{id}/payment-status")
    public CompletableFuture<PaymentStatusEvent> paymentStatus(@PathVariable Long id) {
        return paymentFeignService.getPaymentStatus(id);
    }




    //Test for Kafka message is working
    //if orderid = 1, trigger DLT error
    //Data is not persist in database
    @PostMapping("/order")
    public String sendEvent(@RequestBody OrderEvent msg) {
        orderService.sendOrder(msg);
        return "✅ Service A sent: " + msg;
    }

    //Saga Pattern for Kafka
    //if Order is create check
    //Payment  confirm, and send an event to confirm the order
    //Payment failed, send an event to cancel the order
    //amount > 500 ? Payment confirm : Payment Failed
    //Data in database
    @PostMapping("/saga")
    public String sendEvent(@RequestBody OrderCreateEvent msg) {
        Orders od = orderService.createOrder(msg.userId(), msg.amount());
        return "✅ Service A sent: " + od.toString();
    }

    //Show fixed response
    @GetMapping("/msg")
    public String showMsg() {
        return keyFix;
    }

}
