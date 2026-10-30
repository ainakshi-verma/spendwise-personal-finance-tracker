/**
 * SpendWise — Settings & Demo Data Management Module
 */
const settings = {
    init() {
        this.bindEvents();
    },

    bindEvents() {
        const btnSeed = document.getElementById('btnSeedDemoData');
        if (btnSeed) {
            btnSeed.addEventListener('click', () => this.seedDemoData());
        }

        const btnClearDemo = document.getElementById('btnClearDemoData');
        if (btnClearDemo) {
            btnClearDemo.addEventListener('click', () => this.confirmClearDemo());
        }

        const btnClearAll = document.getElementById('btnClearAllData');
        if (btnClearAll) {
            btnClearAll.addEventListener('click', () => this.confirmResetAll());
        }
    },

    async seedDemoData() {
        const btn = document.getElementById('btnSeedDemoData');
        if (btn) btn.disabled = true;

        try {
            await api.post('/demo/seed');
            showToast('Sample demo data loaded successfully', 'success');
            app.refreshCurrentMonthData();
        } catch (err) {
            console.error('Seed error:', err);
            showToast(err.message, 'error');
        } finally {
            if (btn) btn.disabled = false;
        }
    },

    confirmClearDemo() {
        app.showConfirmDialog(
            'Clear Sample Demo Records',
            'This will delete ONLY the sample demo transactions and budgets from your account. Any real personal transactions you recorded will remain completely intact.',
            async () => {
                const btn = document.getElementById('btnClearDemoData');
                if (btn) btn.disabled = true;

                try {
                    await api.post('/demo/clear-demo');
                    showToast('Sample demo records removed. Your personal records are preserved.', 'success');
                    app.refreshCurrentMonthData();
                } catch (err) {
                    console.error('Clear demo error:', err);
                    showToast(err.message, 'error');
                } finally {
                    if (btn) btn.disabled = false;
                }
            }
        );
    },

    confirmResetAll() {
        app.showConfirmDialog(
            'Reset All Application Data',
            'This action will permanently delete all transaction records and category budgets (both personal and demo) for your account from the MySQL database. This action cannot be reversed.',
            async () => {
                const btn = document.getElementById('btnClearAllData');
                if (btn) btn.disabled = true;

                try {
                    await api.post('/demo/reset');
                    showToast('All database records cleared successfully', 'success');
                    app.refreshCurrentMonthData();
                } catch (err) {
                    console.error('Reset error:', err);
                    showToast(err.message, 'error');
                } finally {
                    if (btn) btn.disabled = false;
                }
            }
        );
    }
};
