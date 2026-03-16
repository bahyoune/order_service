package com.microtest.OrderService.repository;

import com.microtest.OrderService.entity.Orders;
import com.microtest.OrderService.enums.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
public class OrderRepositoryTest {

    @Autowired
    private OrderRepository repository;
    private Orders orders;


    @BeforeEach
    void setup() {
        orders = Orders.builder()
                .amount(501)
                .status(OrderStatus.CREATED)
                .userId("1")
                .build();
    }


}
