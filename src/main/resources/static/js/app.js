/**
 * SpendWise — Core Application Orchestrator
 */
const app = {
    currentView: 'dashboard',
    currentMonth: '',
    confirmCallback: null,
    isInitialized: false,

    viewMeta: {
        dashboard: { title: 'Dashboard', caption: 'Your money, at a glance' },
        transactions: { title: 'Transactions', caption: 'Record and organize your income and expenses' },
        budgets: { title: 'Monthly Budgets', caption: 'Set category limits and track actual spending' },
        reports: { title: 'Reports & Analytics', caption: 'Financial insights, comparisons, and historical trends' },
        settings: { title: 'Settings', caption: 'System preferences and demonstration controls' }
    },

    init() {
        auth.init();
        this.initCurrentMonth();
        this.bindGlobalEvents();

        // Initialize child modules
        transactions.init();
        budgets.init();
        reports.init();
        settings.init();

        // Check authentication state on page load
        auth.checkSession();
    },

    initAfterAuth() {
        if (!this.isInitialized) {
            this.isInitialized = true;
            window.addEventListener('hashchange', () => this.handleRouting());
        }
        this.handleRouting();
    },

    initCurrentMonth() {
        const now = new Date();
        const year = now.getFullYear();
        const month = String(now.getMonth() + 1).padStart(2, '0');
        this.currentMonth = `${year}-${month}`;

        const picker = document.getElementById('globalMonthPicker');
        if (picker) {
            picker.value = this.currentMonth;
            picker.addEventListener('change', (e) => {
                this.currentMonth = e.target.value;
                this.refreshCurrentMonthData();
            });
        }
    },

    bindGlobalEvents() {
        // Sidebar navigation links
        document.querySelectorAll('.sidebar-nav .nav-item').forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                const view = link.dataset.view;
                window.location.hash = view;
            });
        });

        // Mobile drawer controls
        const btnMobileMenu = document.getElementById('mobileMenuBtn');
        const btnMobileClose = document.getElementById('mobileCloseBtn');
        const sidebar = document.getElementById('sidebar');

        if (btnMobileMenu && sidebar) {
            btnMobileMenu.addEventListener('click', () => sidebar.classList.add('open'));
        }
        if (btnMobileClose && sidebar) {
            btnMobileClose.addEventListener('click', () => sidebar.classList.remove('open'));
        }

        // Topbar Add Transaction button (THE single primary action)
        const btnOpenAddTx = document.getElementById('btnOpenAddTransactionModal');
        if (btnOpenAddTx) {
            btnOpenAddTx.addEventListener('click', () => transactions.openAddModal());
        }

        // Dashboard quick navigation links
        const btnViewAll = document.getElementById('btnViewAllTransactions');
        if (btnViewAll) {
            btnViewAll.addEventListener('click', () => this.navigateTo('transactions'));
        }

        const btnManageBudgets = document.getElementById('btnManageBudgets');
        if (btnManageBudgets) {
            btnManageBudgets.addEventListener('click', () => this.navigateTo('budgets'));
        }

        // Empty banner demo seed button
        const btnBannerSeed = document.getElementById('btnSeedDemoFromBanner');
        if (btnBannerSeed) {
            btnBannerSeed.addEventListener('click', () => settings.seedDemoData());
        }

        // Confirmation modal buttons & backdrop/esc key
        const btnCloseConfirm = document.getElementById('btnCloseConfirmModal');
        const btnCancelConfirm = document.getElementById('btnCancelConfirm');
        const btnExecConfirm = document.getElementById('btnExecuteConfirm');
        const confirmBackdrop = document.getElementById('confirmModal');

        if (btnCloseConfirm) btnCloseConfirm.addEventListener('click', () => this.closeConfirmDialog());
        if (btnCancelConfirm) btnCancelConfirm.addEventListener('click', () => this.closeConfirmDialog());
        if (confirmBackdrop) {
            confirmBackdrop.addEventListener('click', (e) => {
                if (e.target === confirmBackdrop) this.closeConfirmDialog();
            });
        }
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape' && confirmBackdrop && !confirmBackdrop.classList.contains('hidden')) {
                this.closeConfirmDialog();
            }
        });

        if (btnExecConfirm) {
            btnExecConfirm.addEventListener('click', () => {
                if (typeof this.confirmCallback === 'function') {
                    this.confirmCallback();
                }
                this.closeConfirmDialog();
            });
        }
    },

    handleRouting() {
        let hash = window.location.hash.replace('#', '').trim();
        if (!hash || !this.viewMeta[hash]) {
            hash = 'dashboard';
        }
        this.navigateTo(hash);
    },

    navigateTo(viewName) {
        this.currentView = viewName;

        // Close mobile drawer if open
        const sidebar = document.getElementById('sidebar');
        if (sidebar) sidebar.classList.remove('open');

        // Update active sidebar nav
        document.querySelectorAll('.sidebar-nav .nav-item').forEach(item => {
            if (item.dataset.view === viewName) {
                item.classList.add('active');
            } else {
                item.classList.remove('active');
            }
        });

        // Switch visible view section
        document.querySelectorAll('.view-section').forEach(sec => {
            sec.classList.remove('active');
        });
        const targetSection = document.getElementById(`view-${viewName}`);
        if (targetSection) {
            targetSection.classList.add('active');
        }

        // Update header title & caption
        const meta = this.viewMeta[viewName];
        if (meta) {
            document.getElementById('pageTitle').textContent = meta.title;
            document.getElementById('pageCaption').textContent = meta.caption;
        }

        // Load active view data
        this.loadActiveViewData();
    },

    loadActiveViewData() {
        // Only load if authenticated
        if (!auth.currentUser) return;

        switch (this.currentView) {
            case 'dashboard':
                dashboard.load(this.currentMonth);
                break;
            case 'transactions':
                transactions.load();
                break;
            case 'budgets':
                budgets.load(this.currentMonth);
                break;
            case 'reports':
                reports.load(this.currentMonth);
                break;
            case 'settings':
                // Settings view has static specs and action triggers
                break;
        }
    },

    refreshCurrentMonthData() {
        if (!auth.currentUser) return;
        this.loadActiveViewData();
        // Also refresh dashboard in background if not on dashboard
        if (this.currentView !== 'dashboard') {
            dashboard.load(this.currentMonth);
        }
    },

    showConfirmDialog(title, message, callback) {
        document.getElementById('confirmModalTitle').textContent = title;
        document.getElementById('confirmModalMessage').textContent = message;
        this.confirmCallback = callback;
        document.body.style.overflow = 'hidden';
        document.getElementById('confirmModal').classList.remove('hidden');
    },

    closeConfirmDialog() {
        document.body.style.overflow = '';
        document.getElementById('confirmModal').classList.add('hidden');
        this.confirmCallback = null;
    }
};

// Bootstrap application on DOM ready
document.addEventListener('DOMContentLoaded', () => {
    app.init();
});
