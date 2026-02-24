package com.microtest.OrderService.service.feign;

import com.microtest.event.PaymentStatusEvent;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "payment-service")
public interface PaymentFeign {

    //old_clean_code: getTestPayment
    //new_clean_code: isIdPaymentExist
    @GetMapping("/internal/v1/payment/{productId}/availability")
    ResponseEntity<Boolean> isIdPaymentExist(@PathVariable("productId") String productId);


    @GetMapping("/internal/v1/payment/{orderId}")
    PaymentStatusEvent getPaymentStatus(@PathVariable Long orderId);

}
