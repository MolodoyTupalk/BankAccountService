package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

// Реализация сервиса для работы с банковскими счетами
public class BankAccountServiceImpl implements BankAccountService {
    private static final Logger logger = LoggerFactory.getLogger(BankAccountServiceImpl.class);

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public BankAccountServiceImpl(AccountRepository accountRepository,
                                  TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public void applyInterest(String accountId, double rate) {
        if (rate <= 0) throw new IllegalArgumentException("Rate must be positive");

        logger.debug("Applying interest of {}% to account {}", rate, accountId);

        BankAccount account = accountRepository.findById(accountId);
        double interest = account.getBalance() * rate / 100;
        account.setBalance(account.getBalance() + interest);

        Transaction transaction = new Transaction(
                UUID.randomUUID().toString(),
                accountId,
                Transaction.TransactionType.INTEREST,
                interest
        );
        transactionRepository.save(transaction);
    }
    @Override
    public void transfer(String fromAccountId, String toAccountId, double amount)
            throws InsufficientFundsException {
        logger.debug("Initiating transfer of {} from account {} to account {}",
                amount, fromAccountId, toAccountId);


        Objects.requireNonNull(fromAccountId, "Source account ID cannot be null");
        Objects.requireNonNull(toAccountId, "Target account ID cannot be null");

        if (fromAccountId.equals(toAccountId)) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }

        BankAccount fromAccount = accountRepository.findById(fromAccountId);
        BankAccount toAccount = accountRepository.findById(toAccountId);

        if (fromAccount.getBalance() < amount) {
            throw new InsufficientFundsException("Insufficient funds for transfer");
        }

        fromAccount.setBalance(fromAccount.getBalance() - amount);
        toAccount.setBalance(toAccount.getBalance() + amount);

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);
    }

    @Override
    public BankAccount createAccount(String ownerName, double initialBalance) {
        if (initialBalance < 0) {
            throw new IllegalArgumentException("Initial balance cannot be negative");
        }

        String accountId = UUID.randomUUID().toString();
        BankAccount account = new BankAccount(accountId, ownerName, initialBalance);
        accountRepository.save(account);
        return account;
    }

    @Override
    public void deposit(String accountId, double amount) {
        Objects.requireNonNull(accountId, "Account ID cannot be null");
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive, got: " + amount);
        }

        BankAccount account = accountRepository.findById(accountId);
        if (account == null) {
            throw new IllegalArgumentException("Account not found");
        }

        if (account.isFrozen()) {
            throw new IllegalStateException("Cannot deposit to frozen account");
        }

        account.setBalance(account.getBalance() + amount);
        accountRepository.save(account);
    }


    @Override
    public void withdraw(String accountId, double amount) throws InsufficientFundsException {
        Objects.requireNonNull(accountId, "Account ID cannot be null");
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive, got: " + amount);
        }

        BankAccount account = accountRepository.findById(accountId);
        if (account == null) {
            throw new IllegalArgumentException("Account not found");
        }

        if (account.isFrozen()) {
            throw new IllegalStateException("Cannot withdraw from frozen account");
        }

        if (account.getBalance() < amount) {
            throw new InsufficientFundsException("Insufficient funds for withdrawal");
        }

        account.setBalance(account.getBalance() - amount);
        accountRepository.save(account);
    }

    @Override
    public double getBalance(String accountId) {
        BankAccount account = accountRepository.findById(accountId);
        if (account == null) {
            throw new IllegalArgumentException("Account not found");
        }
        return account.getBalance();
    }
    @Override
    public List<BankAccount> getAllAccounts() {
        return accountRepository.findAll();
    }

    @Override
    public void freezeAccount(String accountId) {
        BankAccount account = accountRepository.findById(accountId);
        account.setFrozen(true);
        accountRepository.save(account);
    }

    @Override
    public void unfreezeAccount(String accountId) {
        BankAccount account = accountRepository.findById(accountId);
        account.setFrozen(false);
        accountRepository.save(account);
    }

    @Override
    public List<Transaction> getTransactionHistory(String accountId) {
        return transactionRepository.findByAccountId(accountId);
    }
}
