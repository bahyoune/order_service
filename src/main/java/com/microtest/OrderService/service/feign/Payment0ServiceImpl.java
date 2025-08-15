package com.microtest.OrderService.service.feign;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


@Service
public class Payment0ServiceImpl implements Payment0Service {

    @Autowired
    private PaymentFeign paymentClient;

    public String createPayment(String productId) {

        ResponseEntity<Boolean> available = paymentClient.getTestPayment(productId);

        if (Boolean.TRUE.equals(available.getBody())) {
            return "Order placed successfully for " + productId;
        }else {
            return "Product " + productId + " is out of stock";
        }
    }


}
