package com.spendwise.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BudgetResponse {

    private Long id;
    private String category;
    private String month;
    private BigDecimal limitAmount;
    private BigDecimal actualSpent;
    private BigDecimal remainingAmount;
    private double percentageUsed;
    private boolean isExceeded;
    private boolean isDemo;
    private String status; // NORMAL, WARNING, EXCEEDED

    public BudgetResponse() {
    }

    public BudgetResponse(Long id, String category, String month, BigDecimal limitAmount, BigDecimal actualSpent) {
        this(id, category, month, limitAmount, actualSpent, false);
    }

    public BudgetResponse(Long id, String category, String month, BigDecimal limitAmount, BigDecimal actualSpent, boolean isDemo) {
        this.id = id;
        this.category = category;
        this.month = month;
        this.limitAmount = limitAmount != null ? limitAmount : BigDecimal.ZERO;
        this.actualSpent = actualSpent != null ? actualSpent : BigDecimal.ZERO;
        this.remainingAmount = this.limitAmount.subtract(this.actualSpent);
        this.isDemo = isDemo;

        if (this.limitAmount.compareTo(BigDecimal.ZERO) > 0) {
            this.percentageUsed = this.actualSpent
                .divide(this.limitAmount, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
        } else {
            this.percentageUsed = 0.0;
        }

        this.isExceeded = this.actualSpent.compareTo(this.limitAmount) > 0;

        if (this.isExceeded) {
            this.status = "EXCEEDED";
        } else if (this.percentageUsed >= 80.0) {
            this.status = "WARNING";
        } else {
            this.status = "NORMAL";
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public BigDecimal getLimitAmount() {
        return limitAmount;
    }

    public void setLimitAmount(BigDecimal limitAmount) {
        this.limitAmount = limitAmount;
    }

    public BigDecimal getActualSpent() {
        return actualSpent;
    }

    public void setActualSpent(BigDecimal actualSpent) {
        this.actualSpent = actualSpent;
    }

    public BigDecimal getRemainingAmount() {
        return remainingAmount;
    }

    public void setRemainingAmount(BigDecimal remainingAmount) {
        this.remainingAmount = remainingAmount;
    }

    public double getPercentageUsed() {
        return percentageUsed;
    }

    public void setPercentageUsed(double percentageUsed) {
        this.percentageUsed = percentageUsed;
    }

    public boolean isExceeded() {
        return isExceeded;
    }

    public void setExceeded(boolean exceeded) {
        isExceeded = exceeded;
    }

    public boolean isDemo() {
        return isDemo;
    }

    public void setDemo(boolean demo) {
        isDemo = demo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
