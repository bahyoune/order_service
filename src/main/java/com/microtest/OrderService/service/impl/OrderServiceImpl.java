package com.microtest.OrderService.service.impl;

import com.microtest.OrderService.entity.Orders;
import com.microtest.OrderService.enums.OrderStatus;
import com.microtest.OrderService.exception.KafkaErrorPublishException;
import com.microtest.OrderService.repository.OrderRepository;
import com.microtest.OrderService.service.OrderService;
import com.microtest.OrderService.kafka.KafkaPublisher;
import com.microtest.event.OrderCreateEvent;
import com.microtest.event.OrderEvent;
import com.microtest.event.PaymentStatusEvent;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@Slf4j
public class OrderServiceImpl implements OrderService {


    @Autowired
    private OrderRepository orderRepository;

    @Autowired
   private KafkaPublisher eventPublisher;


    //<editor-fold defaultState="collapsed" desc="">
    //</editor-fold>

    //<editor-fold defaultState="collapsed" desc="Saga -> Payment : Success or Failed">
    @Transactional
    public void handlePaymentSuccess(PaymentStatusEvent event)  {
        Optional<Orders> optional = orderRepository.findById(event.OrderId());

        if (optional.isPresent()) {
            Orders order = optional.get();

            order.setStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);

            log.info("Order {} marked as PAID", event.OrderId());
        }else {
            log.warn("HandlePaymentSuccess -> OrderId {} not exist in database", event.OrderId());
        }

    }

    @Transactional
    public void handlePaymentFailed(PaymentStatusEvent event)  {

        Optional<Orders> optional = orderRepository.findById(event.OrderId());

        if (optional.isPresent()) {
            Orders order = optional.get();

            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);

            log.warn("Order {} CANCELLED due to payment failure, reason: {}", order.getId(), event.reason());
        }else {
            log.warn("HandlePaymentFailed -> OrderId {} not exist in database", event.OrderId());
        }
    }
//</editor-fold>

    //<editor-fold defaultState="collapsed" desc="Test simple for Kafka event">
    //if pass, send success
    //else send DLT error message
    public PaymentStatusEvent publishOrderInKafka(OrderEvent event) throws KafkaErrorPublishException {

        try {
            eventPublisher.publishSimpleOrder(event, event.orderId());

            log.info("PublishOrderInKafka sent orderId={}", event.orderId());
        } catch (Exception e) {
            log.error("PublishOrderInKafka: kafka publish failed orderId={}, error ", event.orderId(), e);

            throw new KafkaErrorPublishException(Long.valueOf(event.orderId()));
        }

        return PaymentStatusEvent.builder()
                .OrderId(Long.valueOf(event.orderId()))
                .reason("Order Created")
                .build();
    }
    //</editor-fold>

    //<editor-fold defaultState="collapsed" desc="Saga Pattern of Kafka">
    public PaymentStatusEvent createOrderWithSagaPattern(OrderCreateEvent event) throws KafkaErrorPublishException {

        Orders finalOrder = createOrder_sagaPattern(event.userId(), event.amount());

        try {
            eventPublisher.publishSagaPattern( OrderCreateEvent.builder()
                    .orderId(finalOrder.getId())
                    .userId(finalOrder.getUserId())
                    .amount(finalOrder.getAmount())
                    .build()
                    ,finalOrder.getId()
            );

            log.info("CreateOrderWithSagaPattern sent orderId={}", finalOrder.getId());

        }catch (Exception ex) {
            log.error("CreateOrderWithSagaPattern: kafka publish failed orderId={}, error ", event.orderId(), ex);

            finalOrder.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(finalOrder);

            throw new KafkaErrorPublishException(finalOrder.getId());
        }

        return PaymentStatusEvent.builder()
                .OrderId(finalOrder.getId())
                .reason("Order Created")
                .build();
    }

    public Orders createOrder_sagaPattern(String userId, double amount) {
        Orders order = Orders.builder()
                .userId(userId)
                .amount(amount)
                .status(OrderStatus.CREATED)
                .build();

        return orderRepository.save(order);
    }
    //</editor-fold>


}
