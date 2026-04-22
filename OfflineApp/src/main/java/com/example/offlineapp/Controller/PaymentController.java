package com.example.offlineapp.Controller;
import org.springframework.beans.factory.annotation.Value;
import com.example.offlineapp.Entity.Order;
import com.example.offlineapp.Repository.OrderRepository;
import com.example.offlineapp.Service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.razorpay.RazorpayClient;
import org.json.JSONObject;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private EmailService emailService;

    @Value("${razorpay.key_id}")
    private String keyId;

    @Value("${razorpay.key_secret}")
    private String keySecret;

    // Create Razorpay Order
    @PostMapping("/create-order/{orderId}")
    public String createOrder(@PathVariable Long orderId) throws Exception {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        RazorpayClient client = new RazorpayClient(keyId, keySecret);

        JSONObject options = new JSONObject();
        options.put("amount", order.getTotalAmount() * 100);
        options.put("currency", "INR");
        options.put("receipt", "order_" + orderId);

        return client.orders.create(options).toString();
    }

    // Mark Payment Success
    @PostMapping("/{orderId}")
    public String makePayment(@PathVariable Long orderId,
                              @RequestParam String email) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus().equals("PAID")) {
            return "Order already paid";
        }

        order.setStatus("PAID");
        orderRepository.save(order);

        emailService.sendEmail(
                email,
                "Order Confirmation",
                "Your order " + orderId + " is successful.\nAmount: " + order.getTotalAmount()
        );

        return "Payment Successful";
    }
}