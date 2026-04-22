let currentOrderId = null;

// ✅ Updated backend URL (HOTSPOT IP)
const BASE_URL = "http://172.20.10.2:8080";

function showLoader(show) {
    document.getElementById("loader").classList.toggle("hidden", !show);
}

// ✅ QR Scan Success
function onScanSuccess(decodedText) {

    showLoader(true);

    fetch(`${BASE_URL}/api/scan/${decodedText}`)
        .then(res => res.json())
        .then(data => {

            showLoader(false);
            currentOrderId = data.id;

            document.getElementById("container").innerHTML = `
                <div class="card">
                    <h2>${data.items[0].product.name}</h2>
                    <p><strong>Price:</strong> ₹${data.totalAmount}</p>

                    <input type="email" id="email" placeholder="Enter your email">

                    <button onclick="pay()">💳 Pay Now</button>
                </div>
            `;
        })
        .catch(() => {
            showLoader(false);
            document.getElementById("container").innerHTML = `
                <div class="card error">❌ Product not found</div>
            `;
        });
}

// ✅ Payment Function
function pay() {

    let email = document.getElementById("email").value;

    if (!email) {
        alert("Enter email");
        return;
    }

    showLoader(true);

    // ✅ Step 1: Create Razorpay Order
    fetch(`${BASE_URL}/api/payment/create-order/${currentOrderId}`, {
        method: "POST"
    })
        .then(res => res.json())
        .then(order => {

            showLoader(false);

            var options = {
                key: "rzp_test_SgRdbfoiG3LmdE",
                amount: order.amount,
                currency: "INR",
                name: "Smart Checkout",
                description: "Cloth Purchase",
                order_id: order.id,

                handler: function (response) {

                    // ✅ Step 2: Confirm payment in backend
                    fetch(`${BASE_URL}/api/payment/${currentOrderId}?email=${email}`, {
                        method: "POST"
                    })
                        .then(res => res.text())
                        .then(msg => {

                            document.getElementById("container").innerHTML = `
                                <div class="card success">
                                    <h2>✅ Payment Successful</h2>
                                    <p>${msg}</p>
                                    <p><strong>Payment ID:</strong> ${response.razorpay_payment_id}</p>
                                </div>
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

            var rzp = new Razorpay(options);
            rzp.open();
        })
        .catch(() => {
            showLoader(false);
            document.getElementById("container").innerHTML = `
                <div class="card error">❌ Payment failed</div>
            `;
        });
}

// ✅ Start Scanner
let scanner = new Html5QrcodeScanner("reader", {
    fps: 10,
    qrbox: 250
});

scanner.render(onScanSuccess);