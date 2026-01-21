package com.microtest.OrderService.repo;

import com.microtest.OrderService.bean.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Orders, Long> {
}
