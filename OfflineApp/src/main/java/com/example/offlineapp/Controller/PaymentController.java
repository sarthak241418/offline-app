package com.example.offlineapp.Controller;

import com.example.offlineapp.Entity.Order;
import com.example.offlineapp.Repository.OrderRepository;
import com.example.offlineapp.Service.EmailService;
import com.razorpay.RazorpayClient;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@CrossOrigin(origins = "*") // Restrict to your domain in production
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
        options.put("amount", (int)(order.getTotalAmount() * 100)); // FIX: cast to int for paise
        options.put("currency", "INR");
        options.put("receipt", "order_" + orderId);

        return client.orders.create(options).toString();
    }

    // Mark Payment Success — with Razorpay signature verification
    @PostMapping("/{orderId}")
    public String makePayment(@PathVariable Long orderId,
                              @RequestParam String email,
                              @RequestParam String razorpayPaymentId,
                              @RequestParam String razorpayOrderId,
                              @RequestParam String razorpaySignature) throws Exception {

        // FIX: Verify Razorpay signature before marking order as paid
        String data = razorpayOrderId + "|" + razorpayPaymentId;
        String generatedSignature = hmacSHA256(data, keySecret);

        if (!generatedSignature.equals(razorpaySignature)) {
            throw new RuntimeException("Invalid payment signature. Payment verification failed.");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus().equals("PAID")) {
            return "Order already paid";
        }

        order.setStatus("PAID");
        orderRepository.save(order);

        // FIX: Email body formatting — added \n between timestamp and name
        emailService.sendEmail(
                email,
                "Order Confirmation - #" + orderId,
                "Dear Customer,\n\n" +
                        "Thank you for shopping with us! Your order #" + orderId + " has been successfully placed.\n\n" +
                        "Order Details:\n" +
                        "Amount: ₹" + order.getTotalAmount() + "\n" +
                        "Payment ID: " + razorpayPaymentId + "\n\n" +
                        "We will notify you once your order has been shipped.\n\n" +
                        "Best regards,\n" +
                        "Sarthak\n" +
                        "Ordered at: " + order.getCreatedAt()
        );

        return "Payment Successful";
    }

    // HMAC-SHA256 utility for Razorpay signature verification
    private String hmacSHA256(String data, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}