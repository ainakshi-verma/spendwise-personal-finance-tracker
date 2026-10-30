/**
 * SpendWise — Authentication & Session Management Module
 */
const auth = {
    currentUser: null,

    init() {
        this.bindEvents();
    },

    bindEvents() {
        // Toggle between Login and Register views
        const linkToRegister = document.getElementById('linkToRegister');
        const linkToLogin = document.getElementById('linkToLogin');

        if (linkToRegister) {
            linkToRegister.addEventListener('click', (e) => {
                e.preventDefault();
                this.switchMode('register');
            });
        }

        if (linkToLogin) {
            linkToLogin.addEventListener('click', (e) => {
                e.preventDefault();
                this.switchMode('login');
            });
        }

        // Login Form Submission
        const loginForm = document.getElementById('loginForm');
        if (loginForm) {
            loginForm.addEventListener('submit', (e) => this.handleLogin(e));
        }

        // Register Form Submission
        const registerForm = document.getElementById('registerForm');
        if (registerForm) {
            registerForm.addEventListener('submit', (e) => this.handleRegister(e));
        }

        // Sidebar Logout Button
        const btnLogout = document.getElementById('sidebarLogoutBtn');
        if (btnLogout) {
            btnLogout.addEventListener('click', (e) => {
                e.preventDefault();
                this.logout();
            });
        }
    },

    switchMode(mode) {
        this.clearErrors();
        const loginCard = document.getElementById('authLoginCard');
        const registerCard = document.getElementById('authRegisterCard');

        if (mode === 'register') {
            if (loginCard) loginCard.classList.add('hidden');
            if (registerCard) registerCard.classList.remove('hidden');
        } else {
            if (registerCard) registerCard.classList.add('hidden');
            if (loginCard) loginCard.classList.remove('hidden');
        }
    },

    clearErrors() {
        const loginAlert = document.getElementById('loginErrorAlert');
        const registerAlert = document.getElementById('registerErrorAlert');
        if (loginAlert) {
            loginAlert.textContent = '';
            loginAlert.classList.add('hidden');
        }
        if (registerAlert) {
            registerAlert.textContent = '';
            registerAlert.classList.add('hidden');
        }
    },

    showError(alertId, message) {
        const alertEl = document.getElementById(alertId);
        if (alertEl) {
            alertEl.textContent = message;
            alertEl.classList.remove('hidden');
        }
    },

    async checkSession() {
        try {
            const user = await api.get('/auth/me');
            if (user && user.id) {
                this.onLoginSuccess(user);
                return true;
            }
        } catch (err) {
            // Not authenticated - normal flow for logged-out state
        }
        this.showAuthView();
        return false;
    },

    async handleLogin(e) {
        e.preventDefault();
        this.clearErrors();

        const email = document.getElementById('loginEmailInput').value.trim();
        const password = document.getElementById('loginPasswordInput').value;
        const btnSubmit = document.getElementById('btnLoginSubmit');

        if (!email || !password) {
            this.showError('loginErrorAlert', 'Please enter both email and password');
            return;
        }

        if (btnSubmit) btnSubmit.disabled = true;

        try {
            const user = await api.post('/auth/login', { email, password });
            showToast(`Welcome back, ${user.name}!`, 'success');
            this.onLoginSuccess(user);
        } catch (err) {
            this.showError('loginErrorAlert', err.message || 'Invalid email or password');
        } finally {
            if (btnSubmit) btnSubmit.disabled = false;
        }
    },

    async handleRegister(e) {
        e.preventDefault();
        this.clearErrors();

        const name = document.getElementById('regNameInput').value.trim();
        const email = document.getElementById('regEmailInput').value.trim();
        const password = document.getElementById('regPasswordInput').value;
        const confirmPassword = document.getElementById('regConfirmPasswordInput').value;
        const btnSubmit = document.getElementById('btnRegisterSubmit');

        if (!name || !email || !password || !confirmPassword) {
            this.showError('registerErrorAlert', 'Please complete all required fields');
            return;
        }

        if (password.length < 6) {
            this.showError('registerErrorAlert', 'Password must be at least 6 characters long');
            return;
        }

        if (password !== confirmPassword) {
            this.showError('registerErrorAlert', 'Password and confirm password do not match');
            return;
        }

        if (btnSubmit) btnSubmit.disabled = true;

        try {
            await api.post('/auth/register', { name, email, password, confirmPassword });
            showToast('Account created successfully! Logging you in...', 'success');

            // Auto-login after registration
            const user = await api.post('/auth/login', { email, password });
            this.onLoginSuccess(user);
        } catch (err) {
            this.showError('registerErrorAlert', err.message || 'Registration failed');
        } finally {
            if (btnSubmit) btnSubmit.disabled = false;
        }
    },

    async logout() {
        try {
            await api.post('/auth/logout');
            showToast('You have been logged out', 'info');
        } catch (err) {
            console.error('Logout error:', err);
        } finally {
            this.currentUser = null;
            this.showAuthView();
        }
    },

    onLoginSuccess(user) {
        this.currentUser = user;

        // Hide auth screen, reveal main application layout
        const authView = document.getElementById('view-auth');
        const appLayout = document.querySelector('.app-layout');

        if (authView) authView.classList.add('hidden');
        if (appLayout) appLayout.classList.remove('hidden');

        // Populate user details in sidebar profile card
        const nameEl = document.getElementById('sidebarUserName');
        const emailEl = document.getElementById('sidebarUserEmail');
        const avatarEl = document.getElementById('sidebarUserAvatar');

        if (nameEl) nameEl.textContent = user.name;
        if (emailEl) emailEl.textContent = user.email;
        if (avatarEl) {
            const initial = user.name ? user.name.trim().charAt(0).toUpperCase() : 'U';
            avatarEl.textContent = initial;
        }

        // Initialize application views with user data
        if (typeof app !== 'undefined' && app.initAfterAuth) {
            app.initAfterAuth();
        }
    },

    showAuthView() {
        this.currentUser = null;
        const authView = document.getElementById('view-auth');
        const appLayout = document.querySelector('.app-layout');

        if (appLayout) appLayout.classList.add('hidden');
        if (authView) authView.classList.remove('hidden');

        this.switchMode('login');
    },

    onUnauthenticated() {
        if (this.currentUser) {
            showToast('Your session has expired. Please log in again.', 'warning');
        }
        this.showAuthView();
    }
};
