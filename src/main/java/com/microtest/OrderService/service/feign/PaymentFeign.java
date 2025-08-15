package com.microtest.OrderService.service.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "payment-service")
public interface PaymentFeign {

    @GetMapping("/payment/{productId}/availability")
    ResponseEntity<Boolean> getTestPayment(@PathVariable("productId") String productId);

}
