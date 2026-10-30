package com.spendwise.dto;

import com.spendwise.entity.Transaction;
import com.spendwise.entity.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class TransactionResponse {

    private Long id;
    private TransactionType type;
    private BigDecimal amount;
    private String category;
    private String description;
    private LocalDate transactionDate;
    private String paymentMethod;
    private boolean isDemo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TransactionResponse() {
    }

    public TransactionResponse(Long id, TransactionType type, BigDecimal amount, String category,
                               String description, LocalDate transactionDate, String paymentMethod,
                               boolean isDemo, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.type = type;
        this.amount = amount;
        this.category = category;
        this.description = description;
        this.transactionDate = transactionDate;
        this.paymentMethod = paymentMethod;
        this.isDemo = isDemo;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static TransactionResponse fromEntity(Transaction transaction) {
        return new TransactionResponse(
            transaction.getId(),
            transaction.getType(),
            transaction.getAmount(),
            transaction.getCategory(),
            transaction.getDescription(),
            transaction.getTransactionDate(),
            transaction.getPaymentMethod(),
            transaction.isDemo(),
            transaction.getCreatedAt(),
            transaction.getUpdatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public boolean isDemo() {
        return isDemo;
    }

    public void setDemo(boolean demo) {
        isDemo = demo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
