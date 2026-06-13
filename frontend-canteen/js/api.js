const BASE_URL = 'https://canteen-management-system-production-f6cd.up.railway.app';
 
async function _req(method, path, body) {
  const opts = {
    method,
    headers: { 'Content-Type': 'application/json', ...makeAuthHeader() },
  };
  if (body !== undefined) opts.body = JSON.stringify(body);
  const res = await fetch(BASE_URL + path, opts);
  if (res.status === 204) return null;
  const text = await res.text();
  if (!text) return null;
  let data;
  try {
    data = JSON.parse(text);
  } catch {
    if (res.ok) return text;
    throw new Error(`HTTP ${res.status}: ${text}`);
  }
  if (!res.ok) throw new Error(data?.message || `HTTP ${res.status}`);
  // Normalize isSuccess → success so all existing HTML checks work
  if (data && typeof data === 'object' && !Array.isArray(data) && 'isSuccess' in data) {
    data.success = data.isSuccess;
  }
  return data;
}

const api = {

  // ── Users /api/v1/users ──────────────────────────────────────────────────
  login: (schoolId, password) =>
    _req('POST', '/api/v1/users/login', { schoolId, password }),

  register: (data) =>
    _req('POST', '/api/v1/users/register', { ...data, role: data.userType === 'STAFF' ? 'ADMIN' : 'CUSTOMER' }),

  getProfile: (schoolId) =>
    _req('GET', `/api/v1/users/profile?schoolId=${encodeURIComponent(schoolId)}`),

  updateProfile: (schoolId, data) =>
    _req('PUT', `/api/v1/users/${encodeURIComponent(schoolId)}`, data),

  changePassword: (schoolId, data) =>
    _req('PATCH', `/api/v1/users/${encodeURIComponent(schoolId)}/password`, data),

  // ── Menus /api/v1/menus ──────────────────────────────────────────────────
  getMenus: () =>
    _req('GET', '/api/v1/menus'),

  createMenu: (data) =>
    _req('POST', '/api/v1/menus/menu', { name: data.name, isActive: data.isActive ?? true }),

  updateMenu: (id, data) =>
    _req('PATCH', `/api/v1/menus/menu/${id}`, { name: data.name, isActive: data.isActive }),

  deleteMenu: (id) =>
    _req('DELETE', `/api/v1/menus/menu/${id}`),

  // ── Items /item ───────────────────────────────────────────────────────────
  getItems: () =>
    _req('GET', '/item'),

  createItem: (data) =>
    _req('POST', '/item', data),

  updateItem: (id, data) =>
    _req('PATCH', `/item/${id}`, data),

  deleteItem: (id) =>
    _req('DELETE', `/item/${id}`),

  linkMenuItem: (menuId, itemId) =>
    _req('POST', `/menu_item?menuId=${menuId}&itemId=${itemId}`),

  unlinkMenuItem: (menuId, itemId) =>
    _req('DELETE', `/menu_item?menuId=${menuId}&itemId=${itemId}`),

  // ── Orders /api/v1/orders ─────────────────────────────────────────────────
  placeOrder: (userId, orderData) =>
    _req('POST', `/api/v1/orders/order?userId=${userId}`, {
      userId,
      orderTime: new Date().toISOString(),
      totalAmount: orderData.totalAmount,
      items: (orderData.items || []).map(i => ({
        menuItemId: i.itemId,
        name: i.name,
        priceAtPurchase: i.price,
        quantity: i.quantity,
        subTotal: +(i.price * i.quantity).toFixed(2),
      })),
      paymentMethod: orderData.paymentMethod,
      remark: orderData.remark || '',
    }),

  cancelOrder: (userId, orderId) =>
    _req('POST', `/api/v1/orders/cancel/${orderId}?userId=${userId}`),

  trackOrder: (orderId) =>
    _req('GET', `/api/v1/orders/track/${orderId}`),

  getOrderHistory: (userId) =>
    _req('GET', `/api/v1/orders/history?userId=${userId}`),

  // ── Kitchen /api/v1/orders/kitchen ────────────────────────────────────────
  getKitchenOrders: () =>
    _req('GET', '/api/v1/orders/kitchen/active'),

  prepareOrder: (orderId, actorId) =>
    _req('POST', `/api/v1/orders/kitchen/${orderId}/prepare?actorId=${actorId}`),

  markReady: (orderId, actorId) =>
    _req('POST', `/api/v1/orders/kitchen/${orderId}/ready?actorId=${actorId}`),

  completeOrder: (orderId, actorId) =>
    _req('POST', `/api/v1/orders/kitchen/${orderId}/complete?actorId=${actorId}`),

  rejectOrder: (orderId, actorId) =>
    _req('POST', `/api/v1/orders/kitchen/${orderId}/reject?actorId=${actorId}`),

  // ── Wallet /api/v1/wallets ────────────────────────────────────────────────
  getWallet: (userId) =>
    _req('GET', `/api/v1/wallets/${userId}`),

  getTransactionHistory: (userId) =>
    _req('GET', `/api/v1/wallets/${userId}/transactions`),

  topUp: (data) =>
    _req('POST', '/api/v1/wallets/top-up', data),

  refund: (orderId, adminId) =>
    _req('POST', '/api/v1/wallets/refund', { orderId, adminId }),

  // ── Admin /api/v1/admin ───────────────────────────────────────────────────
  getCutOff: () =>
    _req('GET', '/api/v1/admin/get-cut-off'),

  setCutOff: (newCutOffTime) =>
    _req('POST', `/api/v1/admin/set-cut-off?newCutOffTime=${encodeURIComponent(newCutOffTime)}`),

  getOrderWindow: () =>
    _req('GET', '/api/v1/admin/get-order-window'),

  setOrderWindow: (openTime, closeTime) =>
    _req('POST', `/api/v1/admin/set-order-window?openTime=${encodeURIComponent(openTime)}&closeTime=${encodeURIComponent(closeTime)}`),

  adminSetStatus: (orderId, status, actorId) =>
    _req('POST', `/api/v1/orders/admin/${orderId}/status?status=${encodeURIComponent(status)}&actorId=${actorId}`),

  getStatusLogs: () =>
    _req('GET', '/api/v1/orders/status-logs'),

  getAllOrders: () =>
    _req('GET', '/api/v1/admin/orders'),

  getAllUsers: () =>
    _req('GET', '/api/v1/admin/users'),

  deleteUser: (id) =>
    _req('DELETE', `/api/v1/admin/users/${id}`),

  getDashboardStats: () =>
    _req('GET', '/api/v1/admin/dashboard'),
};
