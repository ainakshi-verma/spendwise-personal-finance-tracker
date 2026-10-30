/**
 * SpendWise — Dashboard Module
 */
let chartOverviewInstance = null;
let chartCategoryInstance = null;

const dashboard = {
    async load(month) {
        const targetMonth = month || document.getElementById('globalMonthPicker').value;

        try {
            const data = await api.get('/dashboard/summary', { month: targetMonth });
            this.renderSummary(data);
            this.renderRecentTransactions(data.recentTransactions);
            this.renderBudgetSummaries(data.budgetSummaries);
            this.renderCategoryChart(data.categoryBreakdown);
            await this.loadOverviewChart();
        } catch (err) {
            console.error('Failed to load dashboard:', err);
            showToast(err.message, 'error');
        }
    },

    renderSummary(data) {
        if (!data) return;

        // Metrics
        document.getElementById('metricTotalIncome').textContent = formatINR(data.totalIncome);
        document.getElementById('metricTotalExpenses').textContent = formatINR(data.totalExpenses);
        document.getElementById('metricNetSavings').textContent = formatINR(data.netSavings);
        document.getElementById('metricAllTimeBalance').textContent = formatINR(data.allTimeBalance);
        document.getElementById('metricRemainingBudget').textContent = formatINR(data.remainingBudget);
        document.getElementById('metricTotalBudgetLimit').textContent = formatINR(data.totalBudgetLimit);

        // Net savings badge style
        const netSavingsBadge = document.getElementById('netSavingsBadge');
        if (Number(data.netSavings) < 0) {
            netSavingsBadge.textContent = 'Deficit';
            netSavingsBadge.className = 'metric-badge expense-badge';
        } else {
            netSavingsBadge.textContent = 'Savings';
            netSavingsBadge.className = 'metric-badge income-badge';
        }

        // Budget metric badge style
        const budgetBadge = document.getElementById('budgetMetricBadge');
        if (Number(data.remainingBudget) < 0) {
            budgetBadge.textContent = 'Over Budget';
            budgetBadge.className = 'metric-badge expense-badge';
        } else {
            budgetBadge.textContent = 'On Track';
            budgetBadge.className = 'metric-badge budget-badge';
        }

        // Empty state banner
        const banner = document.getElementById('dashboardEmptyBanner');
        if (data.transactionCount === 0) {
            banner.classList.remove('hidden');
        } else {
            banner.classList.add('hidden');
        }
    },

    renderRecentTransactions(transactions) {
        const tbody = document.getElementById('recentTransactionsTableBody');
        if (!tbody) return;

        if (!transactions || transactions.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted py-4">No transactions recorded yet</td></tr>';
            return;
        }

        tbody.innerHTML = transactions.map(tx => `
            <tr>
                <td>${formatDate(tx.transactionDate)}</td>
                <td><strong>${escapeHtml(tx.description)}</strong></td>
                <td><span class="badge badge-category">${escapeHtml(tx.category)}</span></td>
                <td>
                    <span class="badge ${tx.type === 'INCOME' ? 'badge-income' : 'badge-expense'}">
                        ${tx.type}
                    </span>
                </td>
                <td class="text-right">
                    <span class="amount-val ${tx.type === 'INCOME' ? 'amount-income' : 'amount-expense'}">
                        ${tx.type === 'INCOME' ? '+' : '-'}${formatINR(tx.amount)}
                    </span>
                </td>
            </tr>
        `).join('');
    },

    renderBudgetSummaries(budgets) {
        const container = document.getElementById('dashboardBudgetList');
        if (!container) return;

        if (!budgets || budgets.length === 0) {
            container.innerHTML = `
                <div class="text-center text-muted py-4">
                    <p style="margin-bottom: 8px;">No budgets configured for this month</p>
                    <button class="btn btn-outline" onclick="app.navigateTo('budgets')">Set Category Budget</button>
                </div>
            `;
            return;
        }

        container.innerHTML = budgets.map(b => {
            let statusClass = 'normal';
            if (b.status === 'WARNING') statusClass = 'warning';
            if (b.status === 'EXCEEDED') statusClass = 'exceeded';

            const fillWidth = Math.min(Math.max(b.percentageUsed, 0), 100);

            return `
                <div class="budget-mini-item">
                    <div class="budget-mini-header">
                        <span>${escapeHtml(b.category)}</span>
                        <span>${formatINR(b.actualSpent)} / <span class="text-muted">${formatINR(b.limitAmount)}</span></span>
                    </div>
                    <div class="progress-track">
                        <div class="progress-fill ${statusClass}" style="width: ${fillWidth}%;"></div>
                    </div>
                    <div style="display: flex; justify-content: space-between; font-size: 11.5px; color: var(--text-secondary);">
                        <span>${b.percentageUsed.toFixed(1)}% used</span>
                        <span class="${b.isExceeded ? 'text-expense text-bold' : ''}">
                            ${b.isExceeded ? 'Exceeded by ' + formatINR(Math.abs(b.remainingAmount)) : formatINR(b.remainingAmount) + ' left'}
                        </span>
                    </div>
                </div>
            `;
        }).join('');
    },

    async loadOverviewChart() {
        const canvas = document.getElementById('chartOverview');
        if (!canvas) return;

        try {
            const trends = await api.get('/reports/monthly-trend', { months: 6 });
            const labels = trends.map(t => t.monthLabel);
            const incomeData = trends.map(t => Number(t.income));
            const expenseData = trends.map(t => Number(t.expense));

            if (chartOverviewInstance) {
                chartOverviewInstance.destroy();
            }

            const ctx = canvas.getContext('2d');
            chartOverviewInstance = new Chart(ctx, {
                type: 'bar',
                data: {
                    labels: labels,
                    datasets: [
                        {
                            label: 'Income',
                            data: incomeData,
                            backgroundColor: '#276749',
                            borderRadius: 4,
                            barPercentage: 0.6,
                            categoryPercentage: 0.7
                        },
                        {
                            label: 'Expenses',
                            data: expenseData,
                            backgroundColor: '#B85C50',
                            borderRadius: 4,
                            barPercentage: 0.6,
                            categoryPercentage: 0.7
                        }
                    ]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: {
                            position: 'top',
                            align: 'end',
                            labels: {
                                boxWidth: 12,
                                boxHeight: 12,
                                font: { family: 'Inter, sans-serif', size: 12 }
                            }
                        },
                        tooltip: {
                            callbacks: {
                                label: function(context) {
                                    return `${context.dataset.label}: ${formatINR(context.raw)}`;
                                }
                            }
                        }
                    },
                    scales: {
                        x: {
                            grid: { display: false },
                            ticks: { font: { family: 'Inter, sans-serif', size: 11 } }
                        },
                        y: {
                            beginAtZero: true,
                            grid: { color: '#EAECE8' },
                            ticks: {
                                callback: function(value) {
                                    return '₹' + value.toLocaleString('en-IN');
                                },
                                font: { family: 'Inter, sans-serif', size: 11 }
                            }
                        }
                    }
                }
            });
        } catch (err) {
            console.error('Failed to render overview chart:', err);
        }
    },

    renderCategoryChart(categories) {
        const canvas = document.getElementById('chartCategory');
        const emptyText = document.getElementById('emptyCategoryChartText');
        if (!canvas) return;

        if (!categories || categories.length === 0) {
            if (chartCategoryInstance) {
                chartCategoryInstance.destroy();
                chartCategoryInstance = null;
            }
            emptyText.classList.remove('hidden');
            return;
        }

        emptyText.classList.add('hidden');

        const labels = categories.map(c => c.category);
        const data = categories.map(c => Number(c.amount));
        const colors = [
            '#276749', '#3182CE', '#D69E2E', '#E53E3E',
            '#805AD5', '#DD6B20', '#319795', '#718096'
        ];

        if (chartCategoryInstance) {
            chartCategoryInstance.destroy();
        }

        const ctx = canvas.getContext('2d');
        chartCategoryInstance = new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: labels,
                datasets: [{
                    data: data,
                    backgroundColor: colors.slice(0, labels.length),
                    borderWidth: 2,
                    borderColor: '#FFFFFF'
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                cutout: '68%',
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: {
                            boxWidth: 10,
                            boxHeight: 10,
                            font: { family: 'Inter, sans-serif', size: 11 }
                        }
                    },
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                const cat = categories[context.dataIndex];
                                return `${context.label}: ${formatINR(context.raw)} (${cat.percentage}%)`;
                            }
                        }
                    }
                }
            }
        });
    }
};

function escapeHtml(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}
