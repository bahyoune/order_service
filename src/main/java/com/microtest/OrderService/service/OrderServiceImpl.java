package com.microtest.OrderService.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private final RestTemplate restTemplate;

    @Value("${payment.service.url}")
    private String paymentServiceURL;

    public OrderServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String createOrder(String productId) {
        String url = paymentServiceURL +"payment/" + productId + "/availability";

//        Boolean available = restTemplate.getForObject(url, Boolean.class);
        ResponseEntity<Boolean> available = restTemplate.getForEntity(url, Boolean.class);

//        ResponseEntity<Boolean> response   restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), Boolean.class);

        if (Boolean.TRUE.equals(available.getBody())) {
            return "Order placed successfully for " + productId;
        }else {
            return "Product " + productId + " is out of stock";
        }
    }


}
