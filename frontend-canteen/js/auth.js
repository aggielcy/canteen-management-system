const SESSION_KEY = 'canteen_session';

function saveSession(loginResponse, password) {
  const session = {
    id: loginResponse.id,
    schoolId: loginResponse.schoolId,
    role: loginResponse.role,
    userType: loginResponse.userType,
    password: password,
  };
  localStorage.removeItem('canteen_logged_out');
  localStorage.setItem(SESSION_KEY, JSON.stringify(session));
}

function getSession() {
  try {
    if (localStorage.getItem('canteen_logged_out') === 'true') return null;
    return JSON.parse(localStorage.getItem(SESSION_KEY));
  } catch {
    return null;
  }
}

function clearSession() {
  localStorage.removeItem(SESSION_KEY);
  localStorage.removeItem('canteen_cart');
  localStorage.setItem('canteen_logged_out', 'true');
  location.replace('/login.html');
}

// Redirect to login if not authenticated, or to wrong-role home if role mismatch.
// requiredRole: 'ADMIN' | 'CUSTOMER' | 'KITCHEN' | null (any authenticated)
function guardPage(requiredRole) {
  const session = getSession();
  if (!session) {
    location.replace('/login.html');
    return null;
  }
  if (requiredRole) {
    const allowed = Array.isArray(requiredRole) ? requiredRole : [requiredRole];
    if (!allowed.includes(session.role)) {
      if (session.role === 'ADMIN') location.replace('/admin/dashboard.html');
      else if (session.role === 'KITCHEN') location.replace('/admin/orders.html');
      else location.replace('/customer/menu.html');
      return null;
    }
  }
  return session;
}

function makeAuthHeader() {
  const session = getSession();
  if (!session) return {};
  const token = btoa(`${session.schoolId}:${session.password}`);
  return { Authorization: `Basic ${token}` };
}

function buildAdminSidebar(activeHref) {
  const session = getSession();
  const role = session ? session.role : 'ADMIN';
  const ic = (d) => `<svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="flex-shrink:0">${d}</svg>`;
  const links = [
    {
      href: '/admin/dashboard.html',
      icon: ic('<rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/>'),
      label: 'Dashboard', roles: ['ADMIN'],
    },
    {
      href: '/admin/menus.html',
      icon: ic('<path d="M9 5H7a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2h-2"/><rect x="9" y="3" width="6" height="4" rx="1"/><line x1="9" y1="12" x2="15" y2="12"/><line x1="9" y1="16" x2="13" y2="16"/>'),
      label: 'Menus', roles: ['ADMIN', 'KITCHEN'],
    },
    {
      href: '/admin/items.html',
      icon: ic('<path d="M3 2v7c0 1.1.9 2 2 2h4a2 2 0 0 0 2-2V2"/><path d="M7 2v20"/><path d="M21 15V2a5 5 0 0 0-5 5v6c0 1.1.9 2 2 2h3zm0 0v7"/>'),
      label: 'Items', roles: ['ADMIN', 'KITCHEN'],
    },
    {
      href: '/admin/orders.html',
      icon: ic('<path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/>'),
      label: 'Orders', roles: ['ADMIN', 'KITCHEN'],
    },
    {
      href: '/admin/logs.html',
      icon: ic('<path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/>'),
      label: 'Status Logs', roles: ['ADMIN', 'KITCHEN'],
    },
    {
      href: '/admin/settings.html',
      icon: ic('<circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 2.83-2.83l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z"/>'),
      label: 'Settings', roles: ['ADMIN'],
    },
  ];
  return links
    .filter(link => link.roles.includes(role))
    .map(link => `<li><a href="${link.href}"${link.href === activeHref ? ' class="active"' : ''}>${link.icon}${link.label}</a></li>`)
    .join('\n        ');
}
