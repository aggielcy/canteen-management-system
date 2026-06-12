# Stripe Wallet Top-Up Integration Plan

## Context

The canteen system has an existing wallet where students top up their balance. The current
`CREDIT_CARD` payment method in `WalletServiceImpl` immediately marks the transaction as
`COMPLETED` without a real payment gateway. This plan wires in **Stripe Checkout** so real
card payments credit the wallet, following the same redirect + idempotent webhook pattern
already proven in `demo-stripe-backend`.

---

## How the New Flow Works

```
wallet.html  ──POST /api/v1/stripe/create-topup-session──▶  Spring Boot
                                                             • Creates Stripe Checkout Session (HKD)
                                                             • Saves TrxEntity (PENDING, stripeSessionId)
             ◀──────────────────── { sessionUrl } ──────────

Browser redirects to Stripe-hosted Checkout page
User pays with test card  4242 4242 4242 4242

Stripe redirects to  wallet-topup-success.html?session_id=cs_xxx
             ──GET /api/v1/stripe/verify?sessionId=cs_xxx──▶  Spring Boot
                                                             • Retrieves session from Stripe API
                                                             • If paid → marks TrxEntity COMPLETED, credits wallet
             ◀──────────── { success:true, newBalance } ────

Separately, Stripe also POSTs to /api/v1/stripe/webhook     Spring Boot
                                                             • Validates signature
                                                             • Calls same doFulfill() — idempotent no-op if already done
```

---

## Files to Create / Modify

| Action | File |
|--------|------|
| Modify | `canteen-system/pom.xml` |
| Modify | `canteen-system/src/main/resources/application.yml` |
| Modify | `entity/TrxEntity.java` — add `stripeSessionId` field |
| Modify | `repository/TrxRepository.java` — add `findByStripeSessionId()` |
| Modify | `config/SecurityConfig.java` — permit `/api/v1/stripe/webhook` |
| Modify | `frontend-canteen/js/api.js` — 2 new methods |
| Modify | `frontend-canteen/customer/wallet.html` — Stripe branch in submit handler |
| Create | `config/StripeProperties.java` |
| Create | `service/StripePaymentService.java` |
| Create | `controller/impl/StripePaymentController.java` |
| Create | `frontend-canteen/customer/wallet-topup-success.html` |
| Create | `frontend-canteen/customer/wallet-topup-cancel.html` |

---

## Backend Steps

### Step 1 — Add stripe-java to pom.xml

Open `canteen-system/pom.xml`. Inside `<dependencies>` add:

```xml
<dependency>
    <groupId>com.stripe</groupId>
    <artifactId>stripe-java</artifactId>
    <version>32.1.0</version>
</dependency>
```

---

### Step 2 — Add Stripe config to application.yml

Open `canteen-system/src/main/resources/application.yml`. Add at root level:

```yaml
stripe:
  secret-key: sk_test_YOUR_KEY_HERE
  webhook-secret: whsec_YOUR_WEBHOOK_SECRET_HERE
  success-url: http://localhost:5173/customer/wallet-topup-success.html?session_id={CHECKOUT_SESSION_ID}
  cancel-url: http://localhost:5173/customer/wallet-topup-cancel.html
```

> Get `sk_test_...` from https://dashboard.stripe.com/test/apikeys  
> Get `whsec_...` after running `stripe listen` (Step 12)  
> Adjust port 5173 to match your frontend dev server

---

### Step 3 — Create StripeProperties.java

Create in `com.canteen.bc.canteen_system.config`:

```java
package com.canteen.bc.canteen_system.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "stripe")
public class StripeProperties {
    private String secretKey;
    private String webhookSecret;
    private String successUrl;
    private String cancelUrl;
}
```

---

### Step 4 — Add stripeSessionId field to TrxEntity

Open `entity/TrxEntity.java`. Add one new field:

```java
@Column(name = "stripe_session_id", unique = true)
private String stripeSessionId;   // nullable — only set for Stripe top-ups
```

If `ddl-auto` is not `update`/`create`, run this SQL migration:

```sql
ALTER TABLE financial_transactions ADD COLUMN stripe_session_id VARCHAR(255) UNIQUE;
```

---

### Step 5 — Add lookup to TrxRepository

Open `repository/TrxRepository.java`. Add:

```java
Optional<TrxEntity> findByStripeSessionId(String stripeSessionId);
```

---

### Step 6 — Create StripePaymentService.java

Create `service/StripePaymentService.java`.
Reference: `demo-stripe-backend/.../service/StripeService.java`

```java
package com.canteen.bc.canteen_system.service;

import com.canteen.bc.canteen_system.config.StripeProperties;
import com.canteen.bc.canteen_system.entity.TrxEntity;
import com.canteen.bc.canteen_system.entity.UserEntity;
import com.canteen.bc.canteen_system.entity.WalletEntity;
import com.canteen.bc.canteen_system.model.PaymentMethod;
import com.canteen.bc.canteen_system.model.TrxStatus;
import com.canteen.bc.canteen_system.model.TrxType;
import com.canteen.bc.canteen_system.repository.TrxRepository;
import com.canteen.bc.canteen_system.repository.UserRepository;
import com.canteen.bc.canteen_system.repository.WalletRepository;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StripePaymentService {

    private final StripeProperties stripeProperties;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TrxRepository trxRepository;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeProperties.getSecretKey();
    }

    // ── Create Checkout Session ──────────────────────────────────────────────

    @Transactional
    public String createTopUpSession(Long userId, BigDecimal amountHkd) throws Exception {
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // Stripe requires smallest currency unit — HKD uses cents (×100)
        long amountCents = amountHkd.multiply(BigDecimal.valueOf(100)).longValueExact();

        SessionCreateParams params = SessionCreateParams.builder()
            .setMode(SessionCreateParams.Mode.PAYMENT)
            .setSuccessUrl(stripeProperties.getSuccessUrl())
            .setCancelUrl(stripeProperties.getCancelUrl())
            .addLineItem(SessionCreateParams.LineItem.builder()
                .setQuantity(1L)
                .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                    .setCurrency("hkd")
                    .setUnitAmount(amountCents)
                    .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                        .setName("Canteen Wallet Top-Up")
                        .setDescription("Top up HKD " + amountHkd + " to your canteen wallet")
                        .build())
                    .build())
                .build())
            .putMetadata("userId", String.valueOf(userId))
            .putMetadata("amountHkd", amountHkd.toPlainString())
            .build();

        Session session = Session.create(params);

        // Save a PENDING transaction to fulfill later
        TrxEntity trx = new TrxEntity();
        trx.setUser(user);
        trx.setTransactionType(TrxType.TOP_UP);
        trx.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        trx.setAmount(amountHkd);
        trx.setStatus(TrxStatus.PENDING);
        trx.setStripeSessionId(session.getId());
        trx.setCreatedAt(LocalDateTime.now());
        trxRepository.save(trx);

        return session.getUrl();
    }

    // ── Verify after redirect ────────────────────────────────────────────────

    @Transactional
    public Map<String, Object> verifyAndFulfill(String sessionId) throws Exception {
        Session session = Session.retrieve(sessionId);
        if (!"paid".equals(session.getPaymentStatus())) {
            return Map.of("success", false, "message", "Payment not completed");
        }
        Long userId = Long.valueOf(session.getMetadata().get("userId"));
        BigDecimal amount = new BigDecimal(session.getMetadata().get("amountHkd"));
        doFulfill(sessionId, userId, amount, session.getPaymentIntent());
        WalletEntity wallet = walletRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Wallet not found"));
        return Map.of("success", true, "newBalance", wallet.getBalance());
    }

    // ── Webhook fulfillment ──────────────────────────────────────────────────

    public void handleWebhook(byte[] payload, String sigHeader) throws Exception {
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, stripeProperties.getWebhookSecret());
        } catch (SignatureVerificationException e) {
            throw new RuntimeException("Invalid Stripe signature");
        }

        if ("checkout.session.completed".equals(event.getType())) {
            Session session = (Session) event.getDataObjectDeserializer()
                .getObject().orElseThrow();
            if ("paid".equals(session.getPaymentStatus())) {
                Long userId = Long.valueOf(session.getMetadata().get("userId"));
                BigDecimal amount = new BigDecimal(session.getMetadata().get("amountHkd"));
                doFulfill(session.getId(), userId, amount, session.getPaymentIntent());
            }
        }
    }

    // ── Shared idempotent core (whichever arrives first wins; second is no-op) ──

    @Transactional
    protected void doFulfill(String sessionId, Long userId, BigDecimal amount, String paymentIntentId) {
        TrxEntity trx = trxRepository.findByStripeSessionId(sessionId)
            .orElseThrow(() -> new RuntimeException("Transaction not found for session: " + sessionId));

        if (TrxStatus.COMPLETED.equals(trx.getStatus())) {
            return; // Already fulfilled — idempotent no-op
        }

        // Credit the wallet
        WalletEntity wallet = walletRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Wallet not found for user: " + userId));
        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);

        // Mark transaction complete
        trx.setStatus(TrxStatus.COMPLETED);
        trxRepository.save(trx);
    }
}
```

> **Before coding:** Verify the exact method name used to find a wallet by user ID in
> `WalletRepository` (e.g. `findByUserId` or `findByUser_Id`). Adjust the calls above if needed.

---

### Step 7 — Create StripePaymentController.java

Create `controller/impl/StripePaymentController.java`.
Reference: `demo-stripe-backend/.../controller/PaymentController.java` and `StripeWebhookController.java`

```java
package com.canteen.bc.canteen_system.controller.impl;

import com.canteen.bc.canteen_system.service.StripePaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/stripe")
@RequiredArgsConstructor
public class StripePaymentController {

    private final StripePaymentService stripePaymentService;

    // Called by wallet.html after user enters top-up amount
    @PostMapping("/create-topup-session")
    public ResponseEntity<?> createTopUpSession(@RequestBody Map<String, Object> body) {
        try {
            Long userId = Long.valueOf(body.get("userId").toString());
            BigDecimal amount = new BigDecimal(body.get("amountHkd").toString());
            String sessionUrl = stripePaymentService.createTopUpSession(userId, amount);
            return ResponseEntity.ok(Map.of("sessionUrl", sessionUrl));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Called by wallet-topup-success.html after Stripe redirect
    @GetMapping("/verify")
    public ResponseEntity<?> verify(@RequestParam String sessionId) {
        try {
            Map<String, Object> result = stripePaymentService.verifyAndFulfill(sessionId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // Stripe calls this directly — must NOT require auth, receives raw bytes for signature check
    @PostMapping("/webhook")
    public ResponseEntity<String> webhook(
            @RequestBody byte[] payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            stripePaymentService.handleWebhook(payload, sigHeader);
            return ResponseEntity.ok("Received");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
```

---

### Step 8 — Update SecurityConfig.java

Open your existing security config (`config/SecurityConfig.java`).
Find the `SecurityFilterChain` bean and make two changes:

**A. Permit the webhook without authentication:**
```java
.requestMatchers("/api/v1/stripe/webhook").permitAll()
```

**B. Exclude webhook from CSRF protection** (if CSRF is enabled):
```java
.csrf(csrf -> csrf.ignoringRequestMatchers("/api/v1/stripe/webhook"))
```

If CSRF is already globally disabled for your REST API, no change needed for (B).

---

## Frontend Steps

### Step 9 — Add two methods to api.js

Open `frontend-canteen/js/api.js`. Inside the `api` object add:

```javascript
createStripeTopUpSession: (userId, amountHkd) =>
    _req('POST', '/api/v1/stripe/create-topup-session', { userId, amountHkd }),

verifyStripeTopUp: (sessionId) =>
    _req('GET', `/api/v1/stripe/verify?sessionId=${sessionId}`),
```

---

### Step 10 — Update wallet.html

Open `frontend-canteen/customer/wallet.html`.

**A. Change the payment method option:**
```html
<option value="STRIPE">Credit / Debit Card (Stripe)</option>
```

**B. Add a Stripe branch in the form submit handler:**

Inside your existing submit handler, before calling `api.topUp(...)`, add:

```javascript
if (paymentMethod === 'STRIPE') {
    try {
        setLoading(submitBtn, true);
        const { sessionUrl } = await api.createStripeTopUpSession(session.id, amount);
        window.location.href = sessionUrl;  // Redirect to Stripe Checkout
    } catch (err) {
        showToast(err.message || 'Failed to start payment', 'error');
        setLoading(submitBtn, false);
    }
    return; // Do not fall through to existing topUp logic
}
```

---

### Step 11 — Create wallet-topup-success.html

Create `frontend-canteen/customer/wallet-topup-success.html`:

```html
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Top-Up Successful</title>
  <link rel="stylesheet" href="../css/main.css">
</head>
<body>
  <nav class="navbar">
    <a class="navbar__brand" href="/customer/menu.html">Canteen</a>
  </nav>

  <div class="page" style="max-width:480px;text-align:center;padding-top:3rem">
    <div id="state-loading">
      <div class="spinner"></div>
      <p class="text-muted" style="margin-top:1rem">Verifying payment…</p>
    </div>
    <div id="state-success" class="hidden">
      <div style="font-size:3rem;margin-bottom:1rem">&#10003;</div>
      <h2 style="font-weight:800;margin-bottom:.5rem">Top-up successful!</h2>
      <p class="text-muted" id="success-msg"></p>
      <a href="/customer/wallet.html" class="btn btn--primary" style="margin-top:1.5rem">Back to Wallet</a>
    </div>
    <div id="state-error" class="hidden">
      <div style="font-size:3rem;margin-bottom:1rem">&#10007;</div>
      <h2 style="font-weight:800;margin-bottom:.5rem">Verification failed</h2>
      <p class="text-muted" id="error-msg"></p>
      <a href="/customer/wallet.html" class="btn btn--secondary" style="margin-top:1.5rem">Back to Wallet</a>
    </div>
  </div>

  <script src="../js/auth.js"></script>
  <script src="../js/api.js"></script>
  <script src="../js/utils.js"></script>
  <script>
    const session = guardPage('CUSTOMER');
    const sessionId = getParam('session_id');

    function show(state, msg) {
      ['loading','success','error'].forEach(s =>
        document.getElementById('state-' + s).classList.toggle('hidden', s !== state));
      if (msg) document.getElementById(state === 'error' ? 'error-msg' : 'success-msg').textContent = msg;
    }

    async function verify() {
      if (!sessionId) { show('error', 'No session ID found in URL.'); return; }
      try {
        const result = await api.verifyStripeTopUp(sessionId);
        if (result.success) {
          show('success', 'New balance: ' + formatCurrency(result.newBalance));
        } else {
          show('error', result.message || 'Payment could not be verified.');
        }
      } catch (err) {
        show('error', err.message || 'Verification request failed.');
      }
    }

    verify();
  </script>
</body>
</html>
```

---

### Step 12 — Create wallet-topup-cancel.html

Create `frontend-canteen/customer/wallet-topup-cancel.html`:

```html
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Payment Cancelled</title>
  <link rel="stylesheet" href="../css/main.css">
</head>
<body>
  <nav class="navbar">
    <a class="navbar__brand" href="/customer/menu.html">Canteen</a>
  </nav>

  <div class="page" style="max-width:480px;text-align:center;padding-top:3rem">
    <div style="font-size:3rem;margin-bottom:1rem">&#10006;</div>
    <h2 style="font-weight:800;margin-bottom:.5rem">Payment cancelled</h2>
    <p class="text-muted">No worries — you haven't been charged.</p>
    <div style="display:flex;gap:1rem;justify-content:center;margin-top:1.5rem">
      <a href="/customer/wallet.html" class="btn btn--primary">Try Again</a>
      <a href="/customer/menu.html" class="btn btn--secondary">Back to Menu</a>
    </div>
  </div>

  <script src="../js/auth.js"></script>
  <script>guardPage('CUSTOMER');</script>
</body>
</html>
```

---

## Testing

### Step 13 — Set up Stripe CLI for local webhooks

Install the [Stripe CLI](https://docs.stripe.com/stripe-cli), then run:

```bash
stripe login
stripe listen --forward-to localhost:8080/api/v1/stripe/webhook
```

Copy the `whsec_...` value printed and put it in `application.yml` as `stripe.webhook-secret`.

### Step 14 — Start everything

```bash
# Terminal 1 — Backend
cd canteen-system
mvn spring-boot:run

# Terminal 2 — Stripe CLI (from Step 13)
stripe listen --forward-to localhost:8080/api/v1/stripe/webhook

# Terminal 3 — Frontend (open in browser or start dev server)
```

### Step 15 — Run the test flow

1. Log in as a customer → go to Wallet page
2. Enter an amount (e.g. 50) → select "Credit / Debit Card (Stripe)" → click top-up
3. Browser redirects to Stripe Checkout
4. Pay with test card: **4242 4242 4242 4242**, any future expiry, any 3-digit CVC
5. Stripe redirects to `wallet-topup-success.html` — should show new balance
6. Check Terminal 2 — `checkout.session.completed` should appear
7. Verify DB: `financial_transactions` should have a COMPLETED row with a `stripe_session_id`

### Step 16 — Test idempotency

Call `GET /api/v1/stripe/verify?sessionId=<same_id>` a second time.
Balance must **not** increase again (`doFulfill()` exits early if status is already COMPLETED).

---

## Key References in the Demo

| What you need | Where to look in demo |
|---|---|
| Stripe Maven dependency | `demo-stripe-backend/pom.xml` |
| Session creation code | `demo-stripe-backend/.../service/StripeService.java` → `createCheckoutSession()` |
| Webhook signature check | `demo-stripe-backend/.../controller/StripeWebhookController.java` |
| Idempotent fulfill logic | `demo-stripe-backend/.../service/StripeService.java` → `doFulfill()` |
| Success page verify call | `demo-stripe-frontend/src/ts/success.ts` |
| Cancel page layout | `demo-stripe-frontend/cancel.html` |
