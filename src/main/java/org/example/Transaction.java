package org.example;

import java.time.LocalDateTime;

public class Transaction {
    private String id;
    private String accountId;
    private TransactionType type;
    private double amount;
    private LocalDateTime timestamp;

    public enum TransactionType { DEPOSIT, WITHDRAWAL, TRANSFER, INTEREST }

    // Полный конструктор
    public Transaction(String id, String accountId, TransactionType type,
                       double amount, LocalDateTime timestamp) {
        this.id = id;
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    // Упрощенный конструктор (устанавливает текущее время автоматически)
    public Transaction(String id, String accountId, TransactionType type, double amount) {
        this(id, accountId, type, amount, LocalDateTime.now());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}