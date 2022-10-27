package org.inmods.orderprocessing.repository;

import org.inmods.orderprocessing.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {}

