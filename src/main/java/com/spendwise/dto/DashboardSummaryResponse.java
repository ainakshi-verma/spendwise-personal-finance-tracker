package com.spendwise.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class DashboardSummaryResponse {

    private String month;
    private BigDecimal totalIncome = BigDecimal.ZERO;
    private BigDecimal totalExpenses = BigDecimal.ZERO;
    private BigDecimal netSavings = BigDecimal.ZERO;
    private BigDecimal allTimeBalance = BigDecimal.ZERO;
    private BigDecimal totalBudgetLimit = BigDecimal.ZERO;
    private BigDecimal totalBudgetSpent = BigDecimal.ZERO;
    private BigDecimal remainingBudget = BigDecimal.ZERO;
    private long transactionCount = 0;

    private List<CategorySpendingDto> categoryBreakdown = new ArrayList<>();
    private List<TransactionResponse> recentTransactions = new ArrayList<>();
    private List<BudgetResponse> budgetSummaries = new ArrayList<>();

    public DashboardSummaryResponse() {
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public BigDecimal getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(BigDecimal totalIncome) {
        this.totalIncome = totalIncome;
    }

    public BigDecimal getTotalExpenses() {
        return totalExpenses;
    }

    public void setTotalExpenses(BigDecimal totalExpenses) {
        this.totalExpenses = totalExpenses;
    }

    public BigDecimal getNetSavings() {
        return netSavings;
    }

    public void setNetSavings(BigDecimal netSavings) {
        this.netSavings = netSavings;
    }

    public BigDecimal getAllTimeBalance() {
        return allTimeBalance;
    }

    public void setAllTimeBalance(BigDecimal allTimeBalance) {
        this.allTimeBalance = allTimeBalance;
    }

    public BigDecimal getTotalBudgetLimit() {
        return totalBudgetLimit;
    }

    public void setTotalBudgetLimit(BigDecimal totalBudgetLimit) {
        this.totalBudgetLimit = totalBudgetLimit;
    }

    public BigDecimal getTotalBudgetSpent() {
        return totalBudgetSpent;
    }

    public void setTotalBudgetSpent(BigDecimal totalBudgetSpent) {
        this.totalBudgetSpent = totalBudgetSpent;
    }

    public BigDecimal getRemainingBudget() {
        return remainingBudget;
    }

    public void setRemainingBudget(BigDecimal remainingBudget) {
        this.remainingBudget = remainingBudget;
    }

    public long getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(long transactionCount) {
        this.transactionCount = transactionCount;
    }

    public List<CategorySpendingDto> getCategoryBreakdown() {
        return categoryBreakdown;
    }

    public void setCategoryBreakdown(List<CategorySpendingDto> categoryBreakdown) {
        this.categoryBreakdown = categoryBreakdown;
    }

    public List<TransactionResponse> getRecentTransactions() {
        return recentTransactions;
    }

    public void setRecentTransactions(List<TransactionResponse> recentTransactions) {
        this.recentTransactions = recentTransactions;
    }

    public List<BudgetResponse> getBudgetSummaries() {
        return budgetSummaries;
    }

    public void setBudgetSummaries(List<BudgetResponse> budgetSummaries) {
        this.budgetSummaries = budgetSummaries;
    }
}
