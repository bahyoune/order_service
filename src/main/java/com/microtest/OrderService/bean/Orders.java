package com.microtest.OrderService.bean;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String name;

    @Column(nullable = false)
    private double amount;

    private boolean state;

    @Temporal(TemporalType.TIMESTAMP)
    private Date d0;

    @OneToMany(mappedBy = "order")
    private List<Payment> payments;

}
