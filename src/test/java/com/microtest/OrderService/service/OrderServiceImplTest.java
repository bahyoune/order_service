package com.microtest.OrderService.service;

import com.microtest.OrderService.entity.Orders;
import com.microtest.OrderService.exception.KafkaErrorPublishException;
import com.microtest.OrderService.repository.OrderRepository;
import com.microtest.OrderService.service.impl.OrderServiceImpl;
import com.microtest.OrderService.kafka.KafkaPublisher;
import com.microtest.OrderService.support.TestEventFactory;
import com.microtest.event.OrderCreateEvent;
import com.microtest.event.OrderEvent;
import com.microtest.event.PaymentStatusEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class OrderServiceImplTest {

    @Mock
    private OrderRepository repository;

    @Mock
    private KafkaPublisher eventPublisher;

    //    @Spy
    @InjectMocks
    private OrderServiceImpl orderService;


    @Test
    public void testPublishOrderInKafka_valid() throws KafkaErrorPublishException {
        //GIVEN
        OrderEvent event = TestEventFactory.orderEvent();

        //WHEN
        var result = orderService.publishOrderInKafka(event);

        //THEN
        Assertions.assertNotNull(result);
        Mockito.verify(eventPublisher, Mockito.timeout(1))
                .publishSimpleOrder(Mockito.any(OrderEvent.class), Mockito.anyString());
    }

    @Test
    public void testPublishOrderInKafka_notValid() throws KafkaErrorPublishException {
        //GIVEN
        OrderEvent event = TestEventFactory.orderEvent();

        //Stubber
        Mockito.doThrow(new RuntimeException("kafka down"))
                .when(eventPublisher)
                .publishSimpleOrder(Mockito.any(OrderEvent.class), Mockito.anyString());

        //WHEN + THEN
        KafkaErrorPublishException exception = Assertions
                .assertThrows(
                        KafkaErrorPublishException.class
                        ,() -> orderService
                                .publishOrderInKafka(event)
                );


        Assertions.assertTrue(exception.getMessage().contains("Kafka publish failed orderId =" + 1L));
    }

    @Test
    public void testCreateOrderWithSagaPattern_valid() throws KafkaErrorPublishException {
        //GIVEN
        var order = TestEventFactory.orders();
        OrderCreateEvent event = TestEventFactory.orderCreateEvent();
        Mockito.when(repository.save(Mockito.any(Orders.class)))
                .thenReturn(order);

        //WHEN
        var result = orderService.createOrderWithSagaPattern(event);

        //THEN
        Assertions.assertNotNull(result);
        Mockito.verify(eventPublisher, Mockito.timeout(1)).publishSagaPattern(Mockito.any(), Mockito.anyLong());
    }

    @Test
    public void testCreateOrderWithSagaPattern_failed() throws KafkaErrorPublishException {
        //GIVEN
        var order = TestEventFactory.orders();
        OrderCreateEvent event = TestEventFactory.orderCreateEvent();
        Mockito.when(repository.save(Mockito.any(Orders.class)))
                .thenReturn(order);

        //Stubber
        Mockito.doThrow(new RuntimeException("kafka down"))
                .when(eventPublisher)
                .publishSagaPattern(Mockito.any(OrderCreateEvent.class), Mockito.anyLong());

        //WHEN + THEN
        KafkaErrorPublishException exception = Assertions.assertThrows(
                KafkaErrorPublishException.class,
                () -> orderService.createOrderWithSagaPattern(event)
        );

        Assertions.assertTrue(exception.getMessage().contains("Kafka publish failed orderId =" + 1L));

    }

    @Test
    public void testCreateOrder_valid() {
        //GIVEN
        var order = TestEventFactory.orders();
        Mockito.when(
                repository.save(Mockito.any(Orders.class))).thenReturn(order);

        //WHEN
        var result = orderService.createOrder_sagaPattern("1", 501);

        //THEN
        Assertions.assertNotNull(result);
        Mockito.verify(repository, Mockito.timeout(1))
                .save(Mockito.any(Orders.class));
    }

    @Test
    public void testHandlePaymentSuccess()  {
        //GIVEN
        var order = TestEventFactory.orders();
        PaymentStatusEvent event = TestEventFactory.paymentStatusEvent_without_reason();


        Mockito.when(repository.findById(Mockito.any(Long.class)))
                .thenReturn(Optional.of(order));

        //WHEN
        orderService.handlePaymentSuccess(event);

        //THEN
        Mockito.verify(repository, Mockito.timeout(1))
                .save(Mockito.any(Orders.class));
    }


    @Test
    public void testHandlePaymentFailed() {
        //GIVEN
        var order = TestEventFactory.orders();
        PaymentStatusEvent event = TestEventFactory.paymentStatusEvent_without_reason();

        Mockito.when(repository.findById(Mockito.any(Long.class)))
                .thenReturn(Optional.of(order));

        //WHEN
        orderService.handlePaymentFailed(event);

        //THEN
        Mockito.verify(repository, Mockito.timeout(1))
                .save(Mockito.any(Orders.class));
    }

}
