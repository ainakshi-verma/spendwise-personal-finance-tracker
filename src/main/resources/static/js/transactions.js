/**
 * SpendWise — Transaction Management Module
 */
const transactions = {
    state: {
        page: 0,
        size: 10,
        sort: 'transactionDate,desc',
        type: '',
        category: '',
        startDate: '',
        endDate: '',
        search: ''
    },

    categories: {
        EXPENSE: ['Food', 'Transport', 'Education', 'Shopping', 'Entertainment', 'Bills', 'Health', 'Other'],
        INCOME: ['Allowance', 'Salary', 'Scholarship', 'Freelance', 'Interest', 'Other Income']
    },

    init() {
        this.bindEvents();
        this.populateCategoryFilters();
    },

    bindEvents() {
        // Search debounce
        let searchTimeout;
        const searchInput = document.getElementById('txSearchInput');
        if (searchInput) {
            searchInput.addEventListener('input', (e) => {
                clearTimeout(searchTimeout);
                searchTimeout = setTimeout(() => {
                    this.state.search = e.target.value.trim();
                    this.state.page = 0;
                    this.load();
                }, 350);
            });
        }

        // Filters
        const typeFilter = document.getElementById('txFilterType');
        if (typeFilter) {
            typeFilter.addEventListener('change', (e) => {
                this.state.type = e.target.value;
                this.state.page = 0;
                this.populateCategoryFilters();
                this.load();
            });
        }

        const catFilter = document.getElementById('txFilterCategory');
        if (catFilter) {
            catFilter.addEventListener('change', (e) => {
                this.state.category = e.target.value;
                this.state.page = 0;
                this.load();
            });
        }

        const startFilter = document.getElementById('txFilterStartDate');
        if (startFilter) {
            startFilter.addEventListener('change', (e) => {
                this.state.startDate = e.target.value;
                this.state.page = 0;
                this.load();
            });
        }

        const endFilter = document.getElementById('txFilterEndDate');
        if (endFilter) {
            endFilter.addEventListener('change', (e) => {
                this.state.endDate = e.target.value;
                this.state.page = 0;
                this.load();
            });
        }

        const btnReset = document.getElementById('btnResetFilters');
        if (btnReset) {
            btnReset.addEventListener('click', () => {
                this.resetFilters();
            });
        }

        // Page Size
        const pageSizeSelect = document.getElementById('txPageSize');
        if (pageSizeSelect) {
            pageSizeSelect.addEventListener('change', (e) => {
                this.state.size = parseInt(e.target.value, 10);
                this.state.page = 0;
                this.load();
            });
        }

        // Table Sorting Header Clicks
        document.querySelectorAll('#transactionsTable th.sortable').forEach(th => {
            th.addEventListener('click', () => {
                const sortField = th.dataset.sort;
                const currentSort = this.state.sort.split(',');
                let direction = 'desc';
                if (currentSort[0] === sortField && currentSort[1] === 'desc') {
                    direction = 'asc';
                }
                this.state.sort = `${sortField},${direction}`;
                this.load();
            });
        });

        // Form Type Segment Toggle
        const btnExp = document.getElementById('btnTypeExpense');
        const btnInc = document.getElementById('btnTypeIncome');
        if (btnExp && btnInc) {
            btnExp.addEventListener('click', () => this.setFormType('EXPENSE'));
            btnInc.addEventListener('click', () => this.setFormType('INCOME'));
        }

        // Form Submit
        const form = document.getElementById('transactionForm');
        if (form) {
            form.addEventListener('submit', (e) => this.handleFormSubmit(e));
        }

        // Modal triggers
        const btnAdd = document.getElementById('btnOpenAddTransactionModal');
        if (btnAdd) {
            btnAdd.addEventListener('click', () => this.openAddModal());
        }

        const btnClose = document.getElementById('btnCloseTxModal');
        const btnCancel = document.getElementById('btnCancelTxModal');
        const modalBackdrop = document.getElementById('transactionModal');

        if (btnClose) btnClose.addEventListener('click', () => this.closeModal());
        if (btnCancel) btnCancel.addEventListener('click', () => this.closeModal());

        if (modalBackdrop) {
            modalBackdrop.addEventListener('click', (e) => {
                if (e.target === modalBackdrop) {
                    this.closeModal();
                }
            });
        }

        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape' && modalBackdrop && !modalBackdrop.classList.contains('hidden')) {
                this.closeModal();
            }
        });
    },

    populateCategoryFilters() {
        const select = document.getElementById('txFilterCategory');
        if (!select) return;

        let list = [];
        if (this.state.type === 'EXPENSE') {
            list = this.categories.EXPENSE;
        } else if (this.state.type === 'INCOME') {
            list = this.categories.INCOME;
        } else {
            list = Array.from(new Set([...this.categories.EXPENSE, ...this.categories.INCOME]));
        }

        select.innerHTML = '<option value="">All Categories</option>' +
            list.map(c => `<option value="${c}">${c}</option>`).join('');

        if (this.state.category) {
            select.value = this.state.category;
        }
    },

    resetFilters() {
        this.state = {
            page: 0,
            size: this.state.size,
            sort: 'transactionDate,desc',
            type: '',
            category: '',
            startDate: '',
            endDate: '',
            search: ''
        };

        document.getElementById('txSearchInput').value = '';
        document.getElementById('txFilterType').value = '';
        document.getElementById('txFilterStartDate').value = '';
        document.getElementById('txFilterEndDate').value = '';
        this.populateCategoryFilters();
        this.load();
    },

    async load() {
        const tbody = document.getElementById('transactionsTableBody');
        if (!tbody) return;

        tbody.innerHTML = '<tr><td colspan="7" class="text-center text-muted py-4">Loading transactions...</td></tr>';

        try {
            const params = {
                page: this.state.page,
                size: this.state.size,
                sort: this.state.sort,
                type: this.state.type || null,
                category: this.state.category || null,
                startDate: this.state.startDate || null,
                endDate: this.state.endDate || null,
                search: this.state.search || null
            };

            const data = await api.get('/transactions', params);
            this.renderTable(data);
            this.renderPagination(data);
        } catch (err) {
            console.error('Failed to load transactions:', err);
            tbody.innerHTML = `<tr><td colspan="7" class="text-center text-expense py-4">${escapeHtml(err.message)}</td></tr>`;
        }
    },

    renderTable(data) {
        const tbody = document.getElementById('transactionsTableBody');
        if (!tbody) return;

        const items = data.content;
        if (!items || items.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" class="text-center text-muted py-4">No transactions matching your criteria</td></tr>';
            return;
        }

        tbody.innerHTML = items.map(tx => `
            <tr>
                <td>${formatDate(tx.transactionDate)}</td>
                <td><strong>${escapeHtml(tx.description)}</strong></td>
                <td><span class="badge badge-category">${escapeHtml(tx.category)}</span></td>
                <td>
                    <span class="badge ${tx.type === 'INCOME' ? 'badge-income' : 'badge-expense'}">
                        ${tx.type}
                    </span>
                </td>
                <td><span class="text-muted text-sm">${escapeHtml(tx.paymentMethod || 'OTHER')}</span></td>
                <td class="text-right">
                    <span class="amount-val ${tx.type === 'INCOME' ? 'amount-income' : 'amount-expense'}">
                        ${tx.type === 'INCOME' ? '+' : '-'}${formatINR(tx.amount)}
                    </span>
                </td>
                <td class="text-center">
                    <button class="btn-action-icon" title="Edit" onclick="transactions.openEditModal(${tx.id})">
                        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <path d="M12 20h9"></path>
                            <path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"></path>
                        </svg>
                    </button>
                    <button class="btn-action-icon delete-icon" title="Delete" onclick="transactions.confirmDelete(${tx.id})">
                        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <polyline points="3 6 5 6 21 6"></polyline>
                            <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                        </svg>
                    </button>
                </td>
            </tr>
        `).join('');
    },

    renderPagination(data) {
        const info = document.getElementById('txPaginationInfo');
        const container = document.getElementById('txPageButtons');
        if (!info || !container) return;

        const total = data.totalElements;
        const totalPages = data.totalPages;
        const current = data.number;
        const size = data.size;

        const start = total === 0 ? 0 : current * size + 1;
        const end = Math.min((current + 1) * size, total);

        info.textContent = `Showing ${start}-${end} of ${total} entries`;

        if (totalPages <= 1) {
            container.innerHTML = '';
            return;
        }

        let buttons = [];
        buttons.push(`
            <button class="page-btn" ${current === 0 ? 'disabled' : ''} onclick="transactions.goToPage(${current - 1})">&larr;</button>
        `);

        for (let i = 0; i < totalPages; i++) {
            if (i === 0 || i === totalPages - 1 || (i >= current - 1 && i <= current + 1)) {
                buttons.push(`
                    <button class="page-btn ${i === current ? 'active' : ''}" onclick="transactions.goToPage(${i})">${i + 1}</button>
                `);
            } else if (i === current - 2 || i === current + 2) {
                buttons.push(`<span class="page-ellipsis" style="padding: 4px;">…</span>`);
            }
        }

        buttons.push(`
            <button class="page-btn" ${current === totalPages - 1 ? 'disabled' : ''} onclick="transactions.goToPage(${current + 1})">&rarr;</button>
        `);

        container.innerHTML = buttons.join('');
    },

    goToPage(page) {
        this.state.page = page;
        this.load();
    },

    // Modal & Form Handling
    setFormType(type) {
        document.getElementById('txTypeInput').value = type;
        const btnExp = document.getElementById('btnTypeExpense');
        const btnInc = document.getElementById('btnTypeIncome');

        if (type === 'EXPENSE') {
            btnExp.classList.add('active');
            btnInc.classList.remove('active');
        } else {
            btnInc.classList.add('active');
            btnExp.classList.remove('active');
        }

        this.updateFormCategories(type);
    },

    updateFormCategories(type, selectedCategory = '') {
        const select = document.getElementById('txCategoryInput');
        const list = this.categories[type] || [];
        select.innerHTML = list.map(c => `<option value="${c}">${c}</option>`).join('');
        if (selectedCategory && list.includes(selectedCategory)) {
            select.value = selectedCategory;
        }
    },

    openAddModal() {
        this.clearFormErrors();
        document.getElementById('transactionModalTitle').textContent = 'Add Transaction';
        document.getElementById('txFormId').value = '';
        document.getElementById('txAmountInput').value = '';
        document.getElementById('txDescInput').value = '';
        document.getElementById('txDateInput').value = new Date().toISOString().split('T')[0];
        document.getElementById('txMethodInput').value = 'UPI';

        this.setFormType('EXPENSE');
        document.body.style.overflow = 'hidden';
        document.getElementById('transactionModal').classList.remove('hidden');
    },

    async openEditModal(id) {
        this.clearFormErrors();
        try {
            const tx = await api.get(`/transactions/${id}`);
            document.getElementById('transactionModalTitle').textContent = 'Edit Transaction';
            document.getElementById('txFormId').value = tx.id;
            document.getElementById('txAmountInput').value = tx.amount;
            document.getElementById('txDescInput').value = tx.description;
            document.getElementById('txDateInput').value = tx.transactionDate;
            document.getElementById('txMethodInput').value = tx.paymentMethod || 'OTHER';

            this.setFormType(tx.type);
            this.updateFormCategories(tx.type, tx.category);

            document.body.style.overflow = 'hidden';
            document.getElementById('transactionModal').classList.remove('hidden');
        } catch (err) {
            showToast(err.message, 'error');
        }
    },

    closeModal() {
        document.body.style.overflow = '';
        document.getElementById('transactionModal').classList.add('hidden');
    },

    clearFormErrors() {
        document.querySelectorAll('#transactionForm .field-error').forEach(el => el.textContent = '');
    },

    async handleFormSubmit(e) {
        e.preventDefault();
        this.clearFormErrors();

        const id = document.getElementById('txFormId').value;
        const type = document.getElementById('txTypeInput').value;
        const amount = document.getElementById('txAmountInput').value;
        const category = document.getElementById('txCategoryInput').value;
        const description = document.getElementById('txDescInput').value.trim();
        const transactionDate = document.getElementById('txDateInput').value;
        const paymentMethod = document.getElementById('txMethodInput').value;

        // Client-side quick checks
        let hasError = false;
        if (!amount || Number(amount) <= 0) {
            document.getElementById('errTxAmount').textContent = 'Please enter a valid amount greater than zero';
            hasError = true;
        }
        if (!description) {
            document.getElementById('errTxDesc').textContent = 'Description is required';
            hasError = true;
        }
        if (!transactionDate) {
            document.getElementById('errTxDate').textContent = 'Date is required';
            hasError = true;
        }
        if (hasError) return;

        const payload = {
            type,
            amount: Number(amount),
            category,
            description,
            transactionDate,
            paymentMethod
        };

        const btnSave = document.getElementById('btnSaveTransaction');
        btnSave.disabled = true;

        try {
            if (id) {
                await api.put(`/transactions/${id}`, payload);
                showToast('Transaction updated successfully', 'success');
            } else {
                await api.post('/transactions', payload);
                showToast('Transaction created successfully', 'success');
            }

            this.closeModal();
            this.load();
            // Refresh dashboard in background if month matches
            app.refreshCurrentMonthData();
        } catch (err) {
            console.error('Save transaction error:', err);
            if (err.data && typeof err.data === 'object') {
                Object.keys(err.data).forEach(field => {
                    const el = document.getElementById(`errTx${field.charAt(0).toUpperCase() + field.slice(1)}`);
                    if (el) el.textContent = err.data[field];
                });
            } else {
                showToast(err.message, 'error');
            }
        } finally {
            btnSave.disabled = false;
        }
    },

    confirmDelete(id) {
        app.showConfirmDialog(
            'Delete Transaction',
            'Are you sure you want to delete this transaction record? This operation permanently removes it from the MySQL database.',
            async () => {
                try {
                    await api.delete(`/transactions/${id}`);
                    showToast('Transaction deleted successfully', 'success');
                    this.load();
                    app.refreshCurrentMonthData();
                } catch (err) {
                    showToast(err.message, 'error');
                }
            }
        );
    }
};
