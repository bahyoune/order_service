package com.microtest.OrderService.service.kafka;


import com.microtest.OrderService.service.OrderService;
import com.microtest.event.PaymentStatusEvent;
import com.microtest.event.PaymentSuccessEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


@Component
public class KafkaConsume {

    @Autowired
    private OrderService orderService;

    @KafkaListener(topics = "${app.kafka.topic.paymentSuccess}", groupId = "${spring.kafka.consumer.group-id}")
    public void handlePaymentSuccess(PaymentStatusEvent event) {
        orderService.handlePaymentSuccess(event);
    }

    @KafkaListener(topics = "${app.kafka.topic.paymentFailed}", groupId = "${spring.kafka.consumer.group-id}")
    public void handlePaymentFailed(PaymentStatusEvent event) {
        orderService.handlePaymentFailed(event);
    }


}
