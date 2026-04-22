package com.example.offlineapp.Service;

import com.example.offlineapp.Entity.Order;
import com.example.offlineapp.Entity.OrderItem;
import com.example.offlineapp.Entity.Product;
import com.example.offlineapp.Repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    public ProductService productService;

    @Autowired
    public OrderRepository orderRepository;

    public Order createOrder(String tagId) {

        Product product = productService.getProductByTagId(tagId);

        Order order = new Order();
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus("PENDING");

        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setQuantity(1);
        item.setPrice(product.getPrice());
        item.setOrder(order);

        order.setItems(List.of(item));
        order.setTotalAmount(product.getPrice());

        return orderRepository.save(order);
    }
}
