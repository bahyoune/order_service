package com.microtest.OrderService.service;

import com.microtest.OrderService.bean.Orders;
import com.microtest.event.OrderEvent;

public interface OrderService {


    void sendOrder(OrderEvent event);

    Orders createOrder(String userId, double amount);
}
