package com.spendwise.dto;

import java.math.BigDecimal;

public class MonthlyTrendDto {

    private String month;       // Format: YYYY-MM
    private String monthLabel;  // e.g. "May 2026"
    private BigDecimal income;
    private BigDecimal expense;
    private BigDecimal net;

    public MonthlyTrendDto() {
    }

    public MonthlyTrendDto(String month, String monthLabel, BigDecimal income, BigDecimal expense) {
        this.month = month;
        this.monthLabel = monthLabel;
        this.income = income != null ? income : BigDecimal.ZERO;
        this.expense = expense != null ? expense : BigDecimal.ZERO;
        this.net = this.income.subtract(this.expense);
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getMonthLabel() {
        return monthLabel;
    }

    public void setMonthLabel(String monthLabel) {
        this.monthLabel = monthLabel;
    }

    public BigDecimal getIncome() {
        return income;
    }

    public void setIncome(BigDecimal income) {
        this.income = income;
    }

    public BigDecimal getExpense() {
        return expense;
    }

    public void setExpense(BigDecimal expense) {
        this.expense = expense;
    }

    public BigDecimal getNet() {
        return net;
    }

    public void setNet(BigDecimal net) {
        this.net = net;
    }
}
