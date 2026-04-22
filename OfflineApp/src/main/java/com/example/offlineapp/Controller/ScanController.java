package com.example.offlineapp.Controller;

import com.example.offlineapp.Entity.Order;
import com.example.offlineapp.Entity.Product;
import com.example.offlineapp.Service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")
public class ScanController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/scan/{tagId}")
    public Order scanProduct(@PathVariable String tagId) {
        return orderService.createOrder(tagId);
    }
}
