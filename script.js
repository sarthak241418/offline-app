let currentOrderId = null;

// FIX: Change this to your deployed backend URL when not running locally
// e.g. "http://192.168.1.10:8080" or "https://your-backend.com"
const BASE_URL = "http://localhost:8080";

// FIX: Razorpay key fetched from a variable — replace with your key from application.properties
// Do NOT hardcode the live key here
const RAZORPAY_KEY = "rzp_test_SgRdbfoiG3LmdE";

function showLoader(show) {
    document.getElementById("loader").classList.toggle("hidden", !show);
}

// Scan success
function onScanSuccess(decodedText) {
    const tagId = decodedText.trim();
    showLoader(true);

    // FIX: Stop scanner immediately after a successful scan
    scanner.clear().catch(err => console.warn("Scanner clear error:", err));

    fetch(`${BASE_URL}/api/scan/${encodeURIComponent(tagId)}`)
        .then(res => {
            if (!res.ok) {
                return res.json().then(err => { throw new Error(err.message || "Scan request failed"); });
            }
            return res.json();
        })
        .then(data => {
            showLoader(false);
            currentOrderId = data.id;

            if (!data.items || data.items.length === 0) {
                document.getElementById("container").innerHTML = `
                    <div class="card error">❌ Cart is empty</div>
                `;
                return;
            }

            document.getElementById("container").innerHTML = `
                <div class="card">
                    <h2>${data.items[0].product.name}</h2>
                    <p><strong>Price:</strong> ₹${data.totalAmount}</p>
                    <input type="email" id="email" placeholder="Enter your email">
                    <button onclick="pay()">💳 Pay Now</button>
                </div>
            `;
        })
        .catch(err => {
            console.error("Scan error:", err);
            showLoader(false);
            document.getElementById("container").innerHTML = `
                <div class="card error">❌ ${err.message || "Product not found"}</div>
            `;
        });
}

// Payment
function pay() {
    let email = document.getElementById("email").value.trim();

    if (!email) {
        alert("Please enter your email");
        return;
    }

    showLoader(true);

    fetch(`${BASE_URL}/api/payment/create-order/${currentOrderId}`, {
        method: "POST"
    })
        .then(res => {
            if (!res.ok) {
                return res.json().then(err => { throw new Error(err.message || "Create order failed"); });
            }
            return res.json();
        })
        .then(order => {
            showLoader(false);

            const options = {
                key: RAZORPAY_KEY,
                amount: order.amount,
                currency: "INR",
                name: "Smart Checkout",
                description: "Cloth Purchase",
                order_id: order.id,

                handler: function (response) {
                    // FIX: Send all 3 Razorpay params for backend signature verification
                    const params = new URLSearchParams({
                        email: email,
                        razorpayPaymentId: response.razorpay_payment_id,
                        razorpayOrderId: response.razorpay_order_id,
                        razorpaySignature: response.razorpay_signature
                    });

                    fetch(`${BASE_URL}/api/payment/${currentOrderId}?${params.toString()}`, {
                        method: "POST"
                    })
                        .then(res => {
                            if (!res.ok) {
                                return res.json().then(err => { throw new Error(err.message || "Payment confirmation failed"); });
                            }
                            return res.text();
                        })
                        .then(msg => {
                            document.getElementById("container").innerHTML = `
                                <div class="card success">
                                    <h2>✅ Payment Successful</h2>
                                    <p>${msg}</p>
                                    <p><strong>Payment ID:</strong> ${response.razorpay_payment_id}</p>
                                </div>
                            `;
                        })
                        .catch(err => {
                            console.error("Confirm payment error:", err);
                            document.getElementById("container").innerHTML = `
                                <div class="card error">❌ ${err.message || "Payment done but confirmation failed"}</div>
                            `;
                        });
                },

                modal: {
                    ondismiss: function () {
                        document.getElementById("container").innerHTML = `
                            <div class="card error">❌ Payment cancelled</div>
                        `;
                    }
                }
            };

            const rzp = new Razorpay(options);
            rzp.open();
        })
        .catch(err => {
            console.error("Payment error:", err);
            showLoader(false);
            document.getElementById("container").innerHTML = `
                <div class="card error">❌ ${err.message || "Payment failed"}</div>
            `;
        });
}

// Start scanner
let scanner = new Html5QrcodeScanner("reader", {
    fps: 10,
    qrbox: 250
});

scanner.render(onScanSuccess);