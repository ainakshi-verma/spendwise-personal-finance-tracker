/**
 * SpendWise — Financial Reports & Analytics Module
 */
let chartReportTrendInstance = null;

const reports = {
    init() {
        this.bindEvents();
    },

    bindEvents() {
        const btnGen = document.getElementById('btnGenerateReport');
        if (btnGen) {
            btnGen.addEventListener('click', () => {
                const month = document.getElementById('reportMonthSelector').value;
                this.load(month);
            });
        }

        const btnPrint = document.getElementById('btnPrintReport');
        if (btnPrint) {
            btnPrint.addEventListener('click', () => {
                window.print();
            });
        }
    },

    async load(month) {
        const targetMonth = month || document.getElementById('globalMonthPicker').value;
        const monthInput = document.getElementById('reportMonthSelector');
        if (monthInput) monthInput.value = targetMonth;

        try {
            const report = await api.get('/reports/monthly', { month: targetMonth });
            this.renderReport(report);
            await this.loadReportTrendChart();
        } catch (err) {
            console.error('Failed to load report:', err);
            showToast(err.message, 'error');
        }
    },

    renderReport(data) {
        if (!data) return;

        document.getElementById('reportPeriodText').textContent =
            `Reporting Period: ${formatDate(data.startDate)} to ${formatDate(data.endDate)}`;

        // Main Stats
        document.getElementById('repIncome').textContent = formatINR(data.totalIncome);
        document.getElementById('repExpense').textContent = formatINR(data.totalExpenses);
        document.getElementById('repNetSavings').textContent = formatINR(data.netSavings);
        document.getElementById('repSavingsRate').textContent = `${data.savingsRate.toFixed(1)}%`;
        document.getElementById('repHighestCategory').textContent = data.highestSpendingCategory || 'None';
        document.getElementById('repHighestAmount').textContent = formatINR(data.highestSpendingAmount);

        // Deltas vs Previous Month
        const incDeltaEl = document.getElementById('repIncomeDelta');
        const expDeltaEl = document.getElementById('repExpenseDelta');

        if (data.hasPreviousMonthData) {
            const incDiff = Number(data.incomeChange);
            const incSign = incDiff >= 0 ? '+' : '';
            incDeltaEl.textContent = `${incSign}${formatINR(incDiff)} (${incSign}${data.incomeChangePct.toFixed(1)}%) vs last month`;
            incDeltaEl.className = incDiff >= 0 ? 'stat-delta positive' : 'stat-delta negative';

            const expDiff = Number(data.expenseChange);
            const expSign = expDiff >= 0 ? '+' : '';
            expDeltaEl.textContent = `${expSign}${formatINR(expDiff)} (${expSign}${data.expenseChangePct.toFixed(1)}%) vs last month`;
            // For expenses, an increase is negative/red, a decrease is positive/green
            expDeltaEl.className = expDiff <= 0 ? 'stat-delta positive' : 'stat-delta negative';
        } else {
            incDeltaEl.textContent = 'No previous month data';
            incDeltaEl.className = 'stat-delta';
            expDeltaEl.textContent = 'No previous month data';
            expDeltaEl.className = 'stat-delta';
        }

        // Category Breakdown Table
        const tbody = document.getElementById('reportCategoryTableBody');
        if (!tbody) return;

        const categories = data.categoryBreakdown;
        if (!categories || categories.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4" class="text-center text-muted py-4">No expense transactions recorded in this period</td></tr>';
            return;
        }

        tbody.innerHTML = categories.map(c => `
            <tr>
                <td><strong>${escapeHtml(c.category)}</strong></td>
                <td class="text-right amount-val text-expense">${formatINR(c.amount)}</td>
                <td class="text-right">
                    <span class="badge badge-category">${c.percentage.toFixed(1)}%</span>
                </td>
                <td class="text-right text-muted">${c.count} transactions</td>
            </tr>
        `).join('');
    },

    async loadReportTrendChart() {
        const canvas = document.getElementById('chartReportTrend');
        if (!canvas) return;

        try {
            const trends = await api.get('/reports/monthly-trend', { months: 6 });
            const labels = trends.map(t => t.monthLabel);
            const incomeData = trends.map(t => Number(t.income));
            const expenseData = trends.map(t => Number(t.expense));

            if (chartReportTrendInstance) {
                chartReportTrendInstance.destroy();
            }

            const ctx = canvas.getContext('2d');
            chartReportTrendInstance = new Chart(ctx, {
                type: 'bar',
                data: {
                    labels: labels,
                    datasets: [
                        {
                            label: 'Monthly Income',
                            data: incomeData,
                            backgroundColor: '#276749',
                            borderRadius: 4,
                            barPercentage: 0.6,
                            categoryPercentage: 0.7
                        },
                        {
                            label: 'Monthly Expenses',
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
            console.error('Failed to load report trend chart:', err);
        }
    }
};
