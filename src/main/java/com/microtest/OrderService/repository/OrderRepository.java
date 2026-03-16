package com.microtest.OrderService.repository;

import com.microtest.OrderService.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Orders, Long> {
}
