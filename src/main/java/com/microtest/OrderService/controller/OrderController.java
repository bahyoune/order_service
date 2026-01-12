package com.microtest.OrderService.controller;


import com.microtest.event.OrderEvent;
import com.microtest.OrderService.service.OrderService;
import com.microtest.OrderService.service.feign.Payment0Service;
import com.microtest.OrderService.service.kafka.KafkaEvent1;
import com.microtest.OrderService.service.kafka.KafkaEvent2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/v1/orders")
@RefreshScope
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private Payment0Service payment0Service;

    @Autowired
    private KafkaEvent1 kafkaEvent1;

    @Autowired
    private KafkaEvent2 kafkaEvent2;


    @Value("${custom.msg}")
    private String keyFix;

    @PostMapping("/{productId}")
    public String placePayment(@PathVariable String productId) {
        return orderService.createOrder(productId);
    }

    @PostMapping("/feign0/{productId}")
    public String createPayment(@PathVariable String productId) {
        System.out.println("enter");
        return payment0Service.createPayment(productId);
    }

    @PostMapping("/send")
    public String sendEvent(@RequestBody String msg) {
        kafkaEvent1.sendEvent(msg);
        return "✅ Service A sent: " + msg;
    }

    @PostMapping("/order")
    public String sendEvent(@RequestBody OrderEvent msg) {
        kafkaEvent2.sendOrder(msg);
        return "✅ Service A sent: " + msg;
    }

    @GetMapping("/msg")
    public String showMsg() {
        return keyFix;
    }

}
