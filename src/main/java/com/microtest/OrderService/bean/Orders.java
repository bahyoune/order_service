package com.microtest.OrderService.bean;

import com.microtest.OrderService.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Builder
@Entity
@Table(name = "ORDERS")
public class Orders {

    private static final long serialVersionID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    private String userId;
    private double amount;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

}
