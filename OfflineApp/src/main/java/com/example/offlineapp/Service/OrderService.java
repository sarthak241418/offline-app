package com.example.offlineapp.Service;

import com.example.offlineapp.Entity.Order;
import com.example.offlineapp.Entity.OrderItem;
import com.example.offlineapp.Entity.Product;
import com.example.offlineapp.Repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    @Autowired
    public ProductService productService;

    @Autowired
    public OrderRepository orderRepository;

    public Order createOrder(String tagId) {

        Product product = productService.getProductByTagId(tagId);

        // FIX: Check stock before creating order
        if (product.getStock() <= 0) {
            throw new RuntimeException("Product '" + product.getName() + "' is out of stock.");
        }

        // FIX: Return existing PENDING order for this tagId instead of creating a duplicate
        Optional<Order> existingOrder = orderRepository.findByTagIdAndStatus(tagId, "PENDING");
        if (existingOrder.isPresent()) {
            return existingOrder.get();
        }

        Order order = new Order();
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus("PENDING");
        order.setTagId(tagId); // FIX: Store tagId on order for deduplication

        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setQuantity(1);
        item.setPrice(product.getPrice());
        item.setOrder(order);

        order.setItems(List.of(item));
        order.setTotalAmount(product.getPrice());

        // FIX: Decrement stock after creating order
        product.setStock(product.getStock() - 1);
        productService.saveProduct(product);

        return orderRepository.save(order);
    }
}