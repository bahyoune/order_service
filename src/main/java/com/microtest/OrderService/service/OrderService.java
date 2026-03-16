package com.microtest.OrderService.service;

import com.microtest.OrderService.exception.KafkaErrorPublishException;
import com.microtest.event.OrderCreateEvent;
import com.microtest.event.OrderEvent;
import com.microtest.event.PaymentStatusEvent;

public interface OrderService {


    void handlePaymentSuccess(PaymentStatusEvent event);

    void handlePaymentFailed(PaymentStatusEvent event);

    PaymentStatusEvent publishOrderInKafka(OrderEvent event) throws KafkaErrorPublishException;

    PaymentStatusEvent createOrderWithSagaPattern(OrderCreateEvent event) throws KafkaErrorPublishException;
}
