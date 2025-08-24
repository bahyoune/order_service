package com.microtest.OrderService.bean;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
@Entity
@Table(name = "PAYMENT")
public class Payment {

    private static final long serialVersionID = 1L;


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private double amount;

    @Temporal(TemporalType.TIMESTAMP)
    private Date d0;

    @ManyToOne
    private Orders order;
}
