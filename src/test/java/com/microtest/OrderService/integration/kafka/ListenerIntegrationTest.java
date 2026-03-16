package com.microtest.OrderService.integration.kafka;

import com.microtest.OrderService.entity.Orders;
import com.microtest.OrderService.enums.OrderStatus;
import com.microtest.OrderService.repository.OrderRepository;
import com.microtest.OrderService.service.impl.OrderServiceImpl;
import com.microtest.OrderService.support.TestEventFactory;
import com.microtest.event.PaymentStatusEvent;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;


import java.time.Duration;

//ActiveProfiles: test
public class ListenerIntegrationTest extends ContainerKafkaTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private OrderServiceImpl orderService;

    @Autowired
    private OrderRepository repository;


    @Test
    public void test_handlePaymentSuccess() {
        //GIVEN
        //Create Order
        orderService.createOrder_sagaPattern("1", 501);

        PaymentStatusEvent event = TestEventFactory.paymentStatusEvent();

        //WHEN
        kafkaTemplate.send("payment-success", event);

        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() ->
                {
                    Orders saved = repository.findById(1L)
                            .orElseThrow();

                    Assertions.assertEquals(OrderStatus.CONFIRMED, saved.getStatus());
                });
    }

    @Test
    public void test_handlePaymentFailed() {
        //GIVEN
        //Create Order
        orderService.createOrder_sagaPattern("1", 403);

        PaymentStatusEvent event = TestEventFactory.paymentStatusEvent_for_error();

        //WHEN
        kafkaTemplate.send("payment-failed", event);

        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() ->
                {
                    Orders saved = repository.findById(1L)
                            .orElseThrow();

                    Assertions.assertEquals(OrderStatus.CANCELLED, saved.getStatus());
                });


    }

}
