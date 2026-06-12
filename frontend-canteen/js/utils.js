function formatDate(isoString) {
  if (!isoString) return '-';
  return new Date(isoString).toLocaleString('en-HK', {
    day: '2-digit', month: 'short', year: 'numeric',
    hour: '2-digit', minute: '2-digit',
    timeZone: 'Asia/Hong_Kong',
  });
}

function formatTime(isoString) {
  if (!isoString) return '-';
  return new Date(isoString).toLocaleTimeString('en-HK', {
    hour: '2-digit', minute: '2-digit',
    timeZone: 'Asia/Hong_Kong',
  });
}

function formatCurrency(amount) {
  return 'HKD ' + Number(amount).toFixed(2);
}

const STATUS_LABELS = {
  ORDERED: 'Ordered',
  PREPARING: 'Preparing',
  READY_FOR_PICK_UP: 'Ready for Pick-up',
  PICKED_UP: 'Picked Up',
  CANCELLED: 'Cancelled',
  REJECTED: 'Rejected',
  ABANDONED: 'Abandoned',
};

function statusBadgeHTML(status) {
  const label = STATUS_LABELS[status] || status;
  return `<span class="badge badge--${status.toLowerCase()}">${label}</span>`;
}

// ── Cart Store ─────────────────────────────────────────────────────────────
const CART_KEY = 'canteen_cart';

const cartStore = {
  get() {
    try { return JSON.parse(localStorage.getItem(CART_KEY)) || []; } catch { return []; }
  },
  save(items) {
    localStorage.setItem(CART_KEY, JSON.stringify(items));
  },
  add(item) {
    const cart = this.get();
    const existing = cart.find(i => i.itemId === item.itemId);
    if (existing) {
      existing.quantity += 1;
    } else {
      cart.push({ ...item, quantity: 1 });
    }
    this.save(cart);
  },
  remove(itemId) {
    this.save(this.get().filter(i => i.itemId !== itemId));
  },
  updateQty(itemId, qty) {
    const cart = this.get();
    const item = cart.find(i => i.itemId === itemId);
    if (item) {
      if (qty <= 0) return this.remove(itemId);
      item.quantity = qty;
      this.save(cart);
    }
  },
  clear() {
    localStorage.removeItem(CART_KEY);
  },
  total() {
    return this.get().reduce((sum, i) => sum + i.price * i.quantity, 0);
  },
  count() {
    return this.get().reduce((sum, i) => sum + i.quantity, 0);
  },
};

// ── Misc helpers ───────────────────────────────────────────────────────────
function showToast(message, type = 'info') {
  let container = document.getElementById('toast-container');
  if (!container) {
    container = document.createElement('div');
    container.id = 'toast-container';
    document.body.appendChild(container);
  }
  const toast = document.createElement('div');
  toast.className = `toast toast--${type}`;
  toast.textContent = message;
  container.appendChild(toast);
  setTimeout(() => toast.classList.add('toast--visible'), 10);
  setTimeout(() => {
    toast.classList.remove('toast--visible');
    setTimeout(() => toast.remove(), 300);
  }, 3000);
}

function setLoading(btn, loading) {
  if (loading) {
    btn.dataset.originalText = btn.textContent;
    btn.disabled = true;
    btn.textContent = 'Loading…';
  } else {
    btn.disabled = false;
    btn.textContent = btn.dataset.originalText || btn.textContent;
  }
}

function getParam(name) {
  return new URLSearchParams(location.search).get(name);
}
