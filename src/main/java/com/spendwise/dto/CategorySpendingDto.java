package com.spendwise.dto;

import java.math.BigDecimal;

public class CategorySpendingDto {

    private String category;
    private BigDecimal amount;
    private double percentage;
    private long count;

    public CategorySpendingDto() {
    }

    public CategorySpendingDto(String category, BigDecimal amount) {
        this.category = category;
        this.amount = amount != null ? amount : BigDecimal.ZERO;
        this.percentage = 0.0;
        this.count = 0;
    }

    public CategorySpendingDto(String category, BigDecimal amount, long count) {
        this.category = category;
        this.amount = amount != null ? amount : BigDecimal.ZERO;
        this.percentage = 0.0;
        this.count = count;
    }

    public CategorySpendingDto(String category, BigDecimal amount, double percentage, long count) {
        this.category = category;
        this.amount = amount != null ? amount : BigDecimal.ZERO;
        this.percentage = percentage;
        this.count = count;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
