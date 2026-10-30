package com.spendwise.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MonthlyReportResponse {

    private String month;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalIncome = BigDecimal.ZERO;
    private BigDecimal totalExpenses = BigDecimal.ZERO;
    private BigDecimal netSavings = BigDecimal.ZERO;
    private double savingsRate = 0.0;

    // Previous month comparison
    private String previousMonth;
    private boolean hasPreviousMonthData = false;
    private BigDecimal prevTotalIncome = BigDecimal.ZERO;
    private BigDecimal prevTotalExpenses = BigDecimal.ZERO;
    private BigDecimal prevNetSavings = BigDecimal.ZERO;
    private BigDecimal incomeChange = BigDecimal.ZERO;
    private BigDecimal expenseChange = BigDecimal.ZERO;
    private double incomeChangePct = 0.0;
    private double expenseChangePct = 0.0;

    // Category insights
    private String highestSpendingCategory = "None";
    private BigDecimal highestSpendingAmount = BigDecimal.ZERO;
    private List<CategorySpendingDto> categoryBreakdown = new ArrayList<>();

    public MonthlyReportResponse() {
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
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

    public double getSavingsRate() {
        return savingsRate;
    }

    public void setSavingsRate(double savingsRate) {
        this.savingsRate = savingsRate;
    }

    public String getPreviousMonth() {
        return previousMonth;
    }

    public void setPreviousMonth(String previousMonth) {
        this.previousMonth = previousMonth;
    }

    public boolean isHasPreviousMonthData() {
        return hasPreviousMonthData;
    }

    public void setHasPreviousMonthData(boolean hasPreviousMonthData) {
        this.hasPreviousMonthData = hasPreviousMonthData;
    }

    public BigDecimal getPrevTotalIncome() {
        return prevTotalIncome;
    }

    public void setPrevTotalIncome(BigDecimal prevTotalIncome) {
        this.prevTotalIncome = prevTotalIncome;
    }

    public BigDecimal getPrevTotalExpenses() {
        return prevTotalExpenses;
    }

    public void setPrevTotalExpenses(BigDecimal prevTotalExpenses) {
        this.prevTotalExpenses = prevTotalExpenses;
    }

    public BigDecimal getPrevNetSavings() {
        return prevNetSavings;
    }

    public void setPrevNetSavings(BigDecimal prevNetSavings) {
        this.prevNetSavings = prevNetSavings;
    }

    public BigDecimal getIncomeChange() {
        return incomeChange;
    }

    public void setIncomeChange(BigDecimal incomeChange) {
        this.incomeChange = incomeChange;
    }

    public BigDecimal getExpenseChange() {
        return expenseChange;
    }

    public void setExpenseChange(BigDecimal expenseChange) {
        this.expenseChange = expenseChange;
    }

    public double getIncomeChangePct() {
        return incomeChangePct;
    }

    public void setIncomeChangePct(double incomeChangePct) {
        this.incomeChangePct = incomeChangePct;
    }

    public double getExpenseChangePct() {
        return expenseChangePct;
    }

    public void setExpenseChangePct(double expenseChangePct) {
        this.expenseChangePct = expenseChangePct;
    }

    public String getHighestSpendingCategory() {
        return highestSpendingCategory;
    }

    public void setHighestSpendingCategory(String highestSpendingCategory) {
        this.highestSpendingCategory = highestSpendingCategory;
    }

    public BigDecimal getHighestSpendingAmount() {
        return highestSpendingAmount;
    }

    public void setHighestSpendingAmount(BigDecimal highestSpendingAmount) {
        this.highestSpendingAmount = highestSpendingAmount;
    }

    public List<CategorySpendingDto> getCategoryBreakdown() {
        return categoryBreakdown;
    }

    public void setCategoryBreakdown(List<CategorySpendingDto> categoryBreakdown) {
        this.categoryBreakdown = categoryBreakdown;
    }
}
