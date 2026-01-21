package com.microtest.OrderService.service.kafka;

import com.microtest.OrderService.bean.Orders;
import com.microtest.OrderService.enums.OrderStatus;
import com.microtest.OrderService.repo.OrderRepository;
import com.microtest.event.PaymentStatusEvent;
import com.microtest.event.PaymentSuccessEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsume {

    @Autowired
    private OrderRepository orderRepository;

    @KafkaListener(topics = "${app.kafka.topic.paymentSuccess}", groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory")
    public void handlePaymentSuccess(PaymentSuccessEvent event) {

        Orders order = orderRepository.findById(event.OrderId())
                .orElseThrow();

        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);
    }

    @KafkaListener(topics = "${app.kafka.topic.paymentFailed}", groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory")
    public void handlePaymentFailed(PaymentStatusEvent event) {

        Orders order = orderRepository.findById(event.OrderId())
                .orElseThrow();

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        System.err.println("❌ Failed to send event: " + event.reason());
    }


}
