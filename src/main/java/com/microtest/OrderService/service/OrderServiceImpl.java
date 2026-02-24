package com.microtest.OrderService.service;

import com.microtest.OrderService.bean.Orders;
import com.microtest.OrderService.enums.OrderStatus;
import com.microtest.OrderService.repo.OrderRepository;
import com.microtest.event.OrderCreateEvent;
import com.microtest.event.OrderEvent;
import com.microtest.event.PaymentStatusEvent;
import com.microtest.event.PaymentSuccessEvent;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;


@Service
@Slf4j
public class OrderServiceImpl implements OrderService {


    @Autowired
    private OrderRepository orderRepository;

    @Value("${app.kafka.topic.order}")
    private String topic;
    @Value("${app.kafka.topic.ordercreate}")
    private String topic1;

    private final KafkaTemplate<String, Object> kafkaTemplate;


    public OrderServiceImpl(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    //<editor-fold defaultState="collapsed" desc="">
    //</editor-fold>

    //<editor-fold defaultState="collapsed" desc="Saga -> Payment : Success or Failed">
    @Transactional
    public void handlePaymentSuccess(PaymentStatusEvent event) {

        Orders order = orderRepository.findById(event.OrderId()).orElseThrow();

        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);

        log.info("Order {} marked as PAID", order.getId());
    }

    @Transactional
    public void handlePaymentFailed(PaymentStatusEvent event) {

        Orders order = orderRepository.findById(event.OrderId()).orElseThrow();

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        log.warn("Order {} CANCELLED due to payment failure, reason: {}", order.getId(), event.reason());
    }
//</editor-fold>

    //<editor-fold defaultState="collapsed" desc="Test simple for Kafka event">
    //if pass, send success
    //else send DLT error message
    // ---------------------
    //old_clean_code: sendOrder
    //new_clean_code: publishOrderInKafka
    public CompletableFuture<PaymentStatusEvent> publishOrderInKafka(OrderEvent event) {
        return kafkaTemplate.send(topic1, event.getOrderId(), event)
                .thenApply(result -> PaymentStatusEvent.builder()
                        .OrderId(Long.valueOf(event.getOrderId()))
                        .reason("📤 Order event published successfully")
                        .build()

                )
                .exceptionally(ex -> PaymentStatusEvent.builder()
                        .OrderId(Long.valueOf(event.getOrderId()))
                        .reason("❌ Failed to publish Order event: " + ex.getMessage())
                        .build()
                );

    }
    //</editor-fold>

    //<editor-fold defaultState="collapsed" desc="Saga Pattern of Kafka">
    //old_clean_code: createOrder
    //new_clean_code: createOrderWithSagaPattern
    public CompletableFuture<PaymentStatusEvent> createOrderWithSagaPattern(String userId, double amount) {
        Orders order = Orders.builder()
                .userId(userId)
                .amount(amount)
                .status(OrderStatus.CREATED).build();

        order = orderRepository.save(order);

        Orders finalOrder = order;

        return kafkaTemplate.send(topic, order.getId().toString(), OrderCreateEvent.builder()
                        .orderId(order.getId())
                        .userId(userId)
                        .amount(amount).build())
                .thenApply(result -> PaymentStatusEvent.builder()
                        .OrderId(finalOrder.getId())
                        .reason("📤 Order event with saga pattern published successfully")
                        .build()
                )
                .exceptionally(ex -> PaymentStatusEvent.builder()
                        .OrderId(finalOrder.getId())
                        .reason("❌ Failed to publish order  event with saga pattern: " + ex.getMessage())
                        .build()
                );
    }
    //</editor-fold>


}
