package org.example;

// Класс, представляющий банковский счет
public class BankAccount {
    private String accountId; // Идентификатор счета
    private String ownerName; // Имя владельца
    private double balance; // Текущий баланс
    private boolean frozen; // Флаг заморозки счета

    // Конструктор для создания нового счета
    public BankAccount(String accountId, String ownerName, double balance) {
        this.accountId = accountId;
        this.ownerName = ownerName;
        this.balance = balance;
        this.frozen = false; // По умолчанию счет не заморожен
    }

    // Геттеры и сеттеры
    public boolean isFrozen() {
        return frozen;
    }

    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        if (balance < 0) {
            throw new IllegalArgumentException("Баланс не может быть отрицательным");
        }
        this.balance = balance;
    }
}