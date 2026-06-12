# Backend vs Frontend API Comparison

## Backend Endpoints (All Controllers)

### Users — `/api/v1/users`

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/users/login` | Login, returns `LoginResponse` (id, schoolId, role, userType) |
| POST | `/api/v1/users/register` | Register new account, returns `RegisterResponse` |
| GET | `/api/v1/users/{schoolId}` | Get raw `UserEntity` by school ID ⚠️ see note below |
| GET | `/api/v1/users/profile?schoolId=` | Get user profile, returns `UserProfileResponse` |
| PUT | `/api/v1/users/{schoolId}` | Update profile name/userType |
| PATCH | `/api/v1/users/{schoolId}/password` | Change password |

### Menus — `/api/v1/menus`

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/menus` | Get all menus with items (`MenuWithItemsDto[]`) |
| POST | `/api/v1/menus/menu` | Create menu, returns `MenuEntity` |
| PATCH | `/api/v1/menus/menu/{id}` | Update menu name/active status, returns `MenuRespDTO` |

### Items — (no `/api/v1` prefix)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/item` | Create item, returns `ItemRespDTO` |
| PATCH | `/item/{id}` | Update item, returns `ItemRespDTO` |
| POST | `/menu_item?menuId=&itemId=` | Link item to a menu |

### Orders — `/api/v1/orders`

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/orders/order?userId=` | Place order, returns `OrderRespDTO` |
| POST | `/api/v1/orders/cancel/{orderId}?userId=` | Cancel order, returns `OrderCancelRespDTO` |
| GET | `/api/v1/orders/track/{orderId}` | Track order, returns `OrderTrackingRespDTO` (status history) |
| GET | `/api/v1/orders/history?userId=` | Order history, returns `List<OrderRespDTO>` |
| GET | `/api/v1/orders/kitchen/active` | Active kitchen tickets, returns `List<KitchenOrderRespDTO>` |
| POST | `/api/v1/orders/kitchen/{orderId}/prepare` | Move order → PREPARING (void) |
| POST | `/api/v1/orders/kitchen/{orderId}/ready` | Move order → READY_FOR_PICK_UP (void) |
| POST | `/api/v1/orders/kitchen/{orderId}/complete` | Move order → PICKED_UP, returns String |

### Wallet — `/api/v1/wallets`

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/wallets/{userId}` | Get wallet balance, returns `WalletResponse` |
| POST | `/api/v1/wallets/top-up` | Top up wallet, returns `WalletTransactionDTO` |

### Admin — `/api/v1/admin`

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/admin/get-cut-off` | Get ordering cut-off time (`LocalTime`) |
| POST | `/api/v1/admin/set-cut-off?newCutOffTime=` | Set ordering cut-off time |

---

## Frontend vs Backend Comparison

| Backend Endpoint | Frontend `api.js` Call | Status | Remark |
|---|---|---|---|
| POST `/api/v1/users/login` | `api.login()` | ✅ Match | |
| POST `/api/v1/users/register` | `api.register()` | ✅ Match | |
| GET `/api/v1/users/{schoolId}` | — | ⚠️ No frontend call | Returns raw `UserEntity` including hashed password — security risk, should be removed or restricted to admin only |
| GET `/api/v1/users/profile` | `api.getProfile()` | ✅ Match | |
| PUT `/api/v1/users/{schoolId}` | `api.updateProfile()` | ✅ Match | |
| PATCH `/api/v1/users/{schoolId}/password` | `api.changePassword()` | ✅ Match | |
| GET `/api/v1/menus` | `api.getMenus()` | ✅ Match | |
| POST `/api/v1/menus/menu` | `api.createMenu()` | ✅ Match | |
| PATCH `/api/v1/menus/menu/{id}` | `api.updateMenu()` | ✅ Match | |
| POST `/item` | `api.createItem()` | ✅ Match | |
| PATCH `/item/{id}` | `api.updateItem()` | ✅ Match | |
| POST `/menu_item` | `api.linkMenuItem()` | ✅ Match | |
| POST `/api/v1/orders/order` | `api.placeOrder()` | ✅ Match | |
| POST `/api/v1/orders/cancel/{orderId}` | `api.cancelOrder()` | ✅ Match | |
| GET `/api/v1/orders/track/{orderId}` | `api.trackOrder()` | ✅ Match | |
| GET `/api/v1/orders/history` | `api.getOrderHistory()` | ✅ Match | |
| GET `/api/v1/orders/kitchen/active` | `api.getKitchenOrders()` | ✅ Match | |
| POST `/api/v1/orders/kitchen/{orderId}/prepare` | `api.prepareOrder()` | ✅ Match | |
| POST `/api/v1/orders/kitchen/{orderId}/ready` | `api.markReady()` | ✅ Match | |
| POST `/api/v1/orders/kitchen/{orderId}/complete` | `api.completeOrder()` | ✅ Match | |
| GET `/api/v1/wallets/{userId}` | `api.getWallet()` | ✅ Match | |
| POST `/api/v1/wallets/top-up` | `api.topUp()` | ✅ Match | |
| GET `/api/v1/admin/get-cut-off` | `api.getCutOff()` | ✅ Match | |
| POST `/api/v1/admin/set-cut-off` | `api.setCutOff()` | ✅ Match | |

---

## About `GET /api/v1/users/{schoolId}`

This endpoint returns the raw `UserEntity` object directly from the database. It exposes:
- Hashed password
- Internal database ID
- Role and userType

It has **no frontend caller** and appears to be an early debug endpoint that was never removed. It is a **security risk** — even with a hashed password, exposing it unnecessarily is bad practice. Recommendation: either delete it, or replace it with a safe admin-only user lookup that returns a DTO without the password field.

---

## Missing Functions — Comparison Against a Typical Canteen System

### 🔴 High Priority (Core functionality gaps)

| Area | Missing Function | Why It Matters |
|------|-----------------|----------------|
| Items | `DELETE /item/{id}` — delete an item | Admin cannot remove outdated or discontinued items |
| Items | `GET /api/v1/items` — list all items | Admin items page has no way to load the item list |
| Items | `PATCH /item/{id}/visibility` — toggle item visibility | No dedicated endpoint; must use the full update endpoint |
| Items | `DELETE /menu_item?menuId=&itemId=` — unlink item from menu | Items cannot be removed from a menu once linked |
| Menu | `DELETE /api/v1/menus/menu/{id}` — delete a menu | Admin cannot remove a menu, only deactivate it |
| Orders | `POST /api/v1/orders/kitchen/{orderId}/reject` — reject an order | `OrderStatus.REJECTED` exists in the enum but there is no endpoint to trigger it |
| Wallet | `GET /api/v1/wallets/{userId}/transactions` — transaction history | `WalletTransactionDTO` exists but no endpoint returns a list of past transactions; the wallet page only shows the balance |
| Kitchen | No `KITCHEN` role | `Role` enum only has `ADMIN` and `CUSTOMER`. The kitchen board uses `guardPage(null)` meaning any logged-in user can access it — there is no proper access control for kitchen staff |

### 🟡 Medium Priority (Important for real-world use)

| Area | Missing Function | Why It Matters |
|------|-----------------|----------------|
| Orders | `GET /api/v1/orders/kitchen/{orderId}/reject` — reject with reason | Kitchen staff need to reject orders they cannot fulfil (e.g. item sold out) |
| Orders | `GET /api/v1/admin/orders` — view all orders (admin) | Admin has no way to view all orders across all users |
| Admin | Dashboard statistics — total orders today, revenue, popular items | Every canteen system needs a summary dashboard for management |
| Admin | `GET /api/v1/admin/users` — list all users | Admin cannot view or manage registered accounts |
| Admin | `PATCH /api/v1/admin/users/{schoolId}/deactivate` — deactivate user | No way to suspend a problematic account |
| Wallet | `POST /api/v1/wallets/refund` — issue a refund to wallet | `TrxType.REFUND` exists in the enum but there is no endpoint to trigger a refund |
| Orders | `OrderStatus.ABANDONED` — mark order as abandoned | Status exists in the enum but has no endpoint or logic to set it |

### 🟢 Low Priority (Nice to have for a complete product)

| Area | Missing Function | Why It Matters |
|------|-----------------|----------------|
| Users | Forgot password / password reset | Users who forget their password have no recovery path |
| Items | Item categories / tags | Helps customers filter items (e.g. vegetarian, halal) |
| Orders | Order rating / feedback after pickup | Useful for canteen management to improve service |
| Menu | Scheduled menu activation (e.g. active only on weekdays) | Avoids manual toggling every day |
| Admin | Sales report export (CSV/PDF) | Finance and management reporting |
| Notifications | Order status push / email notification | Customers currently must manually refresh the tracking page |

---

## Summary

| Category | Implemented | Missing |
|----------|-------------|---------|
| User management | 5 endpoints | 3 missing (delete, list all, password reset) |
| Menu management | 3 endpoints | 1 missing (delete) |
| Item management | 3 endpoints | 4 missing (list, delete, unlink, visibility toggle) |
| Order management | 8 endpoints | 3 missing (reject, admin view all, abandoned) |
| Wallet | 2 endpoints | 2 missing (transaction history, refund) |
| Admin | 2 endpoints | 3 missing (dashboard stats, user management, reports) |
| Roles | ADMIN, CUSTOMER | KITCHEN role missing |
| Security | — | `GET /api/v1/users/{schoolId}` exposes raw entity with password hash |
