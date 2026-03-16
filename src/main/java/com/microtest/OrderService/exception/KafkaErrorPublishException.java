package com.microtest.OrderService.exception;

public class KafkaErrorPublishException extends Exception {

    public KafkaErrorPublishException(Long orderId) {
        super("Kafka publish failed orderId =" + orderId);
    }


}
