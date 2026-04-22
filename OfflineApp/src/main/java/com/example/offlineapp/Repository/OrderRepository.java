package com.example.offlineapp.Repository;

import com.example.offlineapp.Entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // FIX: Added method to find existing PENDING order by tagId — prevents duplicate orders on re-scan
    Optional<Order> findByTagIdAndStatus(String tagId, String status);
}