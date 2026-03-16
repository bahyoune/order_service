package com.microtest.OrderService.support;

import com.microtest.OrderService.entity.Orders;
import com.microtest.OrderService.enums.OrderStatus;
import com.microtest.event.OrderCreateEvent;
import com.microtest.event.OrderEvent;
import com.microtest.event.PaymentStatusEvent;

public class TestEventFactory {

    public static OrderEvent orderEvent() {
        return OrderEvent.builder()
                .orderId("1")
                .userId("1")
                .amount(501)
                .build();
    }
    public static OrderEvent orderEvent_request() {
        return OrderEvent.builder()
                .userId("1")
                .amount(501)
                .build();
    }

    public static OrderCreateEvent orderCreateEvent() {
        return OrderCreateEvent.builder()
                .userId("1")
                .amount(123)
                .build();
    }

    public static OrderCreateEvent orderCreateEvent_with_orderId() {
        return OrderCreateEvent.builder()
                .userId("1")
                .amount(501)
                .orderId(1L)
                .build();
    }

    public static Orders orders() {
        return Orders.builder()
                .userId("userId")
                .amount(501)
                .id(1L)
                .status(OrderStatus.CREATED)
                .build();
    }

    public static PaymentStatusEvent paymentStatusEvent() {
        return PaymentStatusEvent.builder()
                .OrderId(1L)
                .reason("SUCCESS")
                .build();
    }

    public static PaymentStatusEvent paymentStatusEvent_for_error(){
        return PaymentStatusEvent.builder()
                .OrderId(1L)
                .reason("Payment is not available")
                .build();
    }

    public static PaymentStatusEvent paymentStatusEvent_without_reason() {
        return PaymentStatusEvent.builder()
                .OrderId(1L)
                .build();
    }

}
