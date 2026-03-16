package com.microtest.OrderService.kafka;

import com.microtest.event.OrderCreateEvent;
import com.microtest.event.OrderEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topic.order}")
    private String topic;
    @Value("${app.kafka.topic.ordercreate}")
    private String topic1;


    public KafkaPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishSagaPattern(OrderCreateEvent event, Long orderId) {
        kafkaTemplate.send(topic, orderId.toString(),event);
    }

    public void publishSimpleOrder(OrderEvent event, String orderId) {
        kafkaTemplate.send(topic1, orderId, event);
    }


}
