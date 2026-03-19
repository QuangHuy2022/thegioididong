package com.example.demo_3001.repository;

import com.example.demo_3001.model.Order;
import com.example.demo_3001.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);
    java.util.Optional<Order> findByMomoOrderId(String momoOrderId);
}
