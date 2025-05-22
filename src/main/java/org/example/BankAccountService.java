package org.example;

import java.util.List;

// Интерфейс сервиса для работы с банковскими счетами
public interface BankAccountService {
    BankAccount createAccount(String ownerName, double initialBalance);
    void deposit(String accountId, double amount);
    void withdraw(String accountId, double amount) throws InsufficientFundsException;
    void transfer(String fromAccountId, String toAccountId, double amount) throws InsufficientFundsException;
    double getBalance(String accountId);
    List<BankAccount> getAllAccounts();
    void applyInterest(String accountId, double rate) throws IllegalArgumentException;
    void freezeAccount(String accountId);
    void unfreezeAccount(String accountId);
    List<Transaction> getTransactionHistory(String accountId);
}