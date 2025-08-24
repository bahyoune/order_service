package com.microtest.OrderService.service.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaEvent1 {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaEvent1(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendEvent(String message) {
        kafkaTemplate.send("order-topic", message);
        System.out.println("📤 Service A sent event: " + message);
    }


}
