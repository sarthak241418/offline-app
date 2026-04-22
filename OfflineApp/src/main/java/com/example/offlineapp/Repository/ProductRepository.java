package com.example.offlineapp.Repository;

import com.example.offlineapp.Entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product,Long> {
    Optional<Product> findByTagId(String tagId);
}
