package com.example.offlineapp.Service;

import com.example.offlineapp.Entity.Product;
import com.example.offlineapp.Repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    @Autowired
    public ProductRepository productRepository;

    public Product getProductByTagId(String tagId) {
        return productRepository.findByTagId(tagId)
                .orElseThrow(() -> new RuntimeException("Product not found for tag: " + tagId));
    }

    // FIX: Added saveProduct so OrderService can decrement stock
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }
}