package com.microtest.OrderService.service.kafka;

import com.microtest.event.OrderEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaEvent2 {

    @Value("${app.kafka.topic.order}")
    private String topic;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaEvent2(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendOrder(OrderEvent event) {
//        kafkaTemplate.send
        kafkaTemplate.send(topic, event.getOrderId(), event)
                .whenComplete((result, ex) ->{
                    if(ex == null) {
                        System.out.println("📤 Sent: " + event);
                    }else {
                        System.err.println("❌ Failed to send event: " + ex.getMessage());
                    }
                });


    }

}
