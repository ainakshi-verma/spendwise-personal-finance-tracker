/**
 * SpendWise — Monthly Budget Planner Module
 */
const budgets = {
    init() {
        this.bindEvents();
    },

    bindEvents() {
        const btnOpen = document.getElementById('btnOpenAddBudgetModal');
        if (btnOpen) {
            btnOpen.addEventListener('click', () => this.openAddModal());
        }

        const btnClose = document.getElementById('btnCloseBudgetModal');
        const btnCancel = document.getElementById('btnCancelBudgetModal');
        const modalBackdrop = document.getElementById('budgetModal');

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

        const form = document.getElementById('budgetForm');
        if (form) {
            form.addEventListener('submit', (e) => this.handleFormSubmit(e));
        }
    },

    async load(month) {
        const targetMonth = month || document.getElementById('globalMonthPicker').value;
        const container = document.getElementById('budgetsCardsGrid');
        if (!container) return;

        container.innerHTML = '<div class="text-center text-muted py-4" style="grid-column: 1 / -1;">Loading category budgets...</div>';

        try {
            const list = await api.get('/budgets', { month: targetMonth });
            this.renderOverview(list);
            this.renderCards(list);
        } catch (err) {
            console.error('Failed to load budgets:', err);
            container.innerHTML = `<div class="text-center text-expense py-4" style="grid-column: 1 / -1;">${escapeHtml(err.message)}</div>`;
        }
    },

    renderOverview(list) {
        let totalAllocated = 0;
        let totalSpent = 0;

        if (list && list.length > 0) {
            list.forEach(b => {
                totalAllocated += Number(b.limitAmount);
                totalSpent += Number(b.actualSpent);
            });
        }

        const remaining = totalAllocated - totalSpent;
        const util = totalAllocated > 0 ? (totalSpent / totalAllocated) * 100 : 0;

        document.getElementById('budgetTotalAllocated').textContent = formatINR(totalAllocated);
        document.getElementById('budgetTotalSpent').textContent = formatINR(totalSpent);
        document.getElementById('budgetTotalRemaining').textContent = formatINR(remaining);
        document.getElementById('budgetOverallUtilization').textContent = `${util.toFixed(1)}%`;
    },

    renderCards(list) {
        const container = document.getElementById('budgetsCardsGrid');
        if (!container) return;

        if (!list || list.length === 0) {
            container.innerHTML = `
                <div class="card" style="grid-column: 1 / -1; text-align: center; padding: 40px 20px;">
                    <h3 style="font-size: 16px; margin-bottom: 8px;">No budgets planned for this month</h3>
                    <p class="text-muted" style="margin-bottom: 20px; font-size: 13px;">Create category limits (e.g. Food, Transport, Education) to keep your monthly spending within goals.</p>
                    <button class="btn btn-primary" onclick="budgets.openAddModal()">Set First Budget</button>
                </div>
            `;
            return;
        }

        container.innerHTML = list.map(b => {
            let statusBadge = '<span class="badge badge-status status-normal">Normal</span>';
            let statusClass = 'normal';

            if (b.status === 'WARNING') {
                statusBadge = '<span class="badge badge-status status-warning">Warning (≥80%)</span>';
                statusClass = 'warning';
            } else if (b.status === 'EXCEEDED') {
                statusBadge = '<span class="badge badge-status status-exceeded">Exceeded (>100%)</span>';
                statusClass = 'exceeded';
            }

            const fillWidth = Math.min(Math.max(b.percentageUsed, 0), 100);

            return `
                <div class="budget-card-item">
                    <div class="budget-card-top">
                        <span class="budget-cat-name">${escapeHtml(b.category)}</span>
                        ${statusBadge}
                    </div>

                    <div style="margin: 8px 0;">
                        <span style="font-size: 22px; font-weight: 700;">${formatINR(b.actualSpent)}</span>
                        <span class="text-muted" style="font-size: 13px;"> of ${formatINR(b.limitAmount)}</span>
                    </div>

                    <div class="progress-track">
                        <div class="progress-fill ${statusClass}" style="width: ${fillWidth}%;"></div>
                    </div>

                    <div class="budget-card-meta">
                        <span><strong>${b.percentageUsed.toFixed(1)}%</strong> utilized</span>
                        <span class="${b.isExceeded ? 'text-expense text-bold' : ''}">
                            ${b.isExceeded ? 'Over by ' + formatINR(Math.abs(b.remainingAmount)) : formatINR(b.remainingAmount) + ' remaining'}
                        </span>
                    </div>

                    <div class="budget-card-actions">
                        <button class="btn btn-secondary btn-sm" onclick="budgets.openEditModal(${b.id}, '${escapeHtml(b.category)}', '${b.month}', ${b.limitAmount})">Edit Limit</button>
                        <button class="btn btn-action-icon delete-icon" title="Delete Budget" onclick="budgets.confirmDelete(${b.id}, '${escapeHtml(b.category)}')">
                            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <polyline points="3 6 5 6 21 6"></polyline>
                                <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                            </svg>
                        </button>
                    </div>
                </div>
            `;
        }).join('');
    },

    openAddModal() {
        this.clearFormErrors();
        document.getElementById('budgetModalTitle').textContent = 'Set Category Budget';
        document.getElementById('budgetFormId').value = '';
        document.getElementById('budgetMonthInput').value = document.getElementById('globalMonthPicker').value;
        document.getElementById('budgetCategoryInput').value = 'Food';
        document.getElementById('budgetLimitInput').value = '';

        document.body.style.overflow = 'hidden';
        document.getElementById('budgetModal').classList.remove('hidden');
    },

    openEditModal(id, category, month, limitAmount) {
        this.clearFormErrors();
        document.getElementById('budgetModalTitle').textContent = 'Edit Budget Limit';
        document.getElementById('budgetFormId').value = id;
        document.getElementById('budgetMonthInput').value = month;
        document.getElementById('budgetCategoryInput').value = category;
        document.getElementById('budgetLimitInput').value = limitAmount;

        document.body.style.overflow = 'hidden';
        document.getElementById('budgetModal').classList.remove('hidden');
    },

    closeModal() {
        document.body.style.overflow = '';
        document.getElementById('budgetModal').classList.add('hidden');
    },

    clearFormErrors() {
        document.querySelectorAll('#budgetForm .field-error').forEach(el => el.textContent = '');
    },

    async handleFormSubmit(e) {
        e.preventDefault();
        this.clearFormErrors();

        const id = document.getElementById('budgetFormId').value;
        const month = document.getElementById('budgetMonthInput').value;
        const category = document.getElementById('budgetCategoryInput').value;
        const limitAmount = document.getElementById('budgetLimitInput').value;

        if (!limitAmount || Number(limitAmount) <= 0) {
            document.getElementById('errBudgetLimit').textContent = 'Please enter a valid spending limit greater than zero';
            return;
        }

        const payload = {
            category,
            month,
            limitAmount: Number(limitAmount)
        };

        const btnSave = document.getElementById('btnSaveBudget');
        btnSave.disabled = true;

        try {
            if (id) {
                await api.put(`/budgets/${id}`, payload);
                showToast('Budget updated successfully', 'success');
            } else {
                await api.post('/budgets', payload);
                showToast('Budget created successfully', 'success');
            }

            this.closeModal();
            this.load();
            app.refreshCurrentMonthData();
        } catch (err) {
            console.error('Save budget error:', err);
            showToast(err.message, 'error');
        } finally {
            btnSave.disabled = false;
        }
    },

    confirmDelete(id, category) {
        app.showConfirmDialog(
            'Delete Budget',
            `Are you sure you want to delete the budget limit for "${category}"?`,
            async () => {
                try {
                    await api.delete(`/budgets/${id}`);
                    showToast('Budget deleted successfully', 'success');
                    this.load();
                    app.refreshCurrentMonthData();
                } catch (err) {
                    showToast(err.message, 'error');
                }
            }
        );
    }
};
