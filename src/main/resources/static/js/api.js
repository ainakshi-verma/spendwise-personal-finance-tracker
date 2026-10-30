/**
 * SpendWise API Client & Shared Utilities
 */
const API_BASE_URL = '/api';

function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) {
        return decodeURIComponent(parts.pop().split(';').shift());
    }
    return null;
}

const api = {
    async request(endpoint, options = {}) {
        const url = new URL(API_BASE_URL + endpoint, window.location.origin);

        if (options.params) {
            Object.keys(options.params).forEach(key => {
                if (options.params[key] !== null && options.params[key] !== undefined && options.params[key] !== '') {
                    url.searchParams.append(key, options.params[key]);
                }
            });
        }

        const method = (options.method || 'GET').toUpperCase();
        const headers = {
            'Content-Type': 'application/json',
            'Accept': 'application/json',
            ...options.headers
        };

        // Attach CSRF token on mutating HTTP methods
        if (!['GET', 'HEAD', 'OPTIONS', 'TRACE'].includes(method)) {
            const csrfToken = getCookie('XSRF-TOKEN');
            if (csrfToken) {
                headers['X-XSRF-TOKEN'] = csrfToken;
            }
        }

        const config = {
            method,
            credentials: 'same-origin',
            headers,
            ...options
        };

        if (config.body && typeof config.body === 'object') {
            config.body = JSON.stringify(config.body);
        }

        try {
            const response = await fetch(url.toString(), config);
            let json;
            try {
                json = await response.json();
            } catch (e) {
                json = null;
            }

            if (!response.ok) {
                // If unauthorized on protected routes, redirect to login
                if (response.status === 401 && !endpoint.startsWith('/auth/')) {
                    if (typeof auth !== 'undefined' && auth.onUnauthenticated) {
                        auth.onUnauthenticated();
                    }
                }

                const errorMessage = (json && json.message)
                    ? json.message
                    : `Server returned HTTP ${response.status} (${response.statusText})`;
                const error = new Error(errorMessage);
                error.status = response.status;
                error.data = json ? json.data : null;
                throw error;
            }

            return json ? json.data : null;
        } catch (err) {
            if (err.name === 'TypeError' && err.message.includes('fetch')) {
                throw new Error('Unable to connect to the SpendWise backend server. Please check that Spring Boot is running on port 8080.');
            }
            throw err;
        }
    },

    get(endpoint, params = {}) {
        return this.request(endpoint, { method: 'GET', params });
    },

    post(endpoint, body = {}) {
        return this.request(endpoint, { method: 'POST', body });
    },

    put(endpoint, body = {}) {
        return this.request(endpoint, { method: 'PUT', body });
    },

    delete(endpoint) {
        return this.request(endpoint, { method: 'DELETE' });
    }
};

/**
 * Format monetary amount into Indian Rupee standard format (e.g. ₹10,500.00)
 */
function formatINR(amount) {
    if (amount === null || amount === undefined || isNaN(amount)) {
        return '₹0.00';
    }
    const num = Number(amount);
    const isNegative = num < 0;
    const absVal = Math.abs(num);

    const formatted = new Intl.NumberFormat('en-IN', {
        style: 'currency',
        currency: 'INR',
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    }).format(absVal);

    return isNegative ? `-${formatted}` : formatted;
}

/**
 * Format ISO date string into human-friendly format
 */
function formatDate(dateStr) {
    if (!dateStr) return '—';
    try {
        const parts = dateStr.split('-');
        if (parts.length === 3) {
            const year = parts[0];
            const monthNames = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
            const month = monthNames[parseInt(parts[1], 10) - 1];
            const day = parts[2];
            return `${day} ${month} ${year}`;
        }
        return dateStr;
    } catch (e) {
        return dateStr;
    }
}

/**
 * Toast Notification Dispatcher
 */
function showToast(message, type = 'success') {
    const container = document.getElementById('toastContainer');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.textContent = message;

    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(-10px)';
        toast.style.transition = 'all 0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, 3800);
}
