package com.microtest.OrderService.controller;


import com.microtest.OrderService.service.OrderService;
import com.microtest.OrderService.service.feign.Payment0Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private Payment0Service payment0Service;

    @PostMapping("/{productId}")
    public String placePayment(@PathVariable String productId) {
        return orderService.createOrder(productId);
    }

    @PostMapping("/{productId}/feign0")
    public String createPayment(@PathVariable String productId) {
        return payment0Service.createPayment(productId);
    }

}
