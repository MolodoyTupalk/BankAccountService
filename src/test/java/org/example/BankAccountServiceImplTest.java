package org.example;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankAccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    private BankAccountService bankAccountService;

    @BeforeEach
    void setUp() {
        bankAccountService = new BankAccountServiceImpl(accountRepository, transactionRepository);
    }

    @Test
    void createAccount_ValidData_ReturnsAccountAndSavesIt() {
        // Arrange
        String ownerName = "John Doe";
        double initialBalance = 100.0;
        String expectedAccountId = UUID.randomUUID().toString();

        // Act
        BankAccount result = bankAccountService.createAccount(ownerName, initialBalance);

        // Assert
        assertNotNull(result);
        assertEquals(ownerName, result.getOwnerName());
        assertEquals(initialBalance, result.getBalance());
        verify(accountRepository).save(any(BankAccount.class));
    }

    @Test
    void createAccount_NegativeInitialBalance_ThrowsException() {
        // Arrange
        String ownerName = "John Doe";
        double initialBalance = -100.0;

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> bankAccountService.createAccount(ownerName, initialBalance));
        verify(accountRepository, never()).save(any());
    }

    @Test
    void deposit_ValidAmount_IncreasesBalance() {
        // Arrange
        String accountId = "acc123";
        double initialBalance = 100.0;
        double depositAmount = 50.0;
        BankAccount account = new BankAccount(accountId, "John Doe", initialBalance);

        when(accountRepository.findById(accountId)).thenReturn(account);

        // Act
        bankAccountService.deposit(accountId, depositAmount);

        // Assert
        assertEquals(initialBalance + depositAmount, account.getBalance());
        verify(accountRepository).save(account);
    }

    @Test
    void deposit_NonExistingAccount_ThrowsException() {
        // Arrange
        String accountId = "non-existing";
        when(accountRepository.findById(accountId)).thenReturn(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> bankAccountService.deposit(accountId, 50.0));
        verify(accountRepository, never()).save(any());
    }

    @Test
    void withdraw_ValidAmount_DecreasesBalance() throws InsufficientFundsException {
        // Arrange
        String accountId = "acc123";
        double initialBalance = 100.0;
        double withdrawAmount = 50.0;
        BankAccount account = new BankAccount(accountId, "John Doe", initialBalance);

        when(accountRepository.findById(accountId)).thenReturn(account);

        // Act
        bankAccountService.withdraw(accountId, withdrawAmount);

        // Assert
        assertEquals(initialBalance - withdrawAmount, account.getBalance());
        verify(accountRepository).save(account);
    }

    @Test
    void withdraw_InsufficientFunds_ThrowsException() {
        // Arrange
        String accountId = "acc123";
        double initialBalance = 30.0;
        double withdrawAmount = 50.0;
        BankAccount account = new BankAccount(accountId, "John Doe", initialBalance);

        when(accountRepository.findById(accountId)).thenReturn(account);

        // Act & Assert
        assertThrows(InsufficientFundsException.class,
                () -> bankAccountService.withdraw(accountId, withdrawAmount));
        verify(accountRepository, never()).save(any());
    }

    @Test
    void transfer_ValidAmount_TransfersBetweenAccounts() throws InsufficientFundsException {
        // Arrange
        BankAccount fromAccount = new BankAccount("acc1", "John", 100.0);
        BankAccount toAccount = new BankAccount("acc2", "Jane", 50.0);

        when(accountRepository.findById("acc1")).thenReturn(fromAccount);
        when(accountRepository.findById("acc2")).thenReturn(toAccount);

        // Act
        bankAccountService.transfer("acc1", "acc2", 30.0);

        // Assert
        assertEquals(70.0, fromAccount.getBalance()); // 100 - 30 = 70
        assertEquals(80.0, toAccount.getBalance());   // 50 + 30 = 80

        verify(accountRepository).save(fromAccount);
        verify(accountRepository).save(toAccount);
    }

    @Test
    void transfer_InsufficientFunds_ThrowsException() {
        // Arrange
        BankAccount fromAccount = new BankAccount("acc1", "John", 20.0);
        BankAccount toAccount = new BankAccount("acc2", "Jane", 50.0);

        when(accountRepository.findById("acc1")).thenReturn(fromAccount);
        when(accountRepository.findById("acc2")).thenReturn(toAccount);

        // Act & Assert
        assertThrows(InsufficientFundsException.class,
                () -> bankAccountService.transfer("acc1", "acc2", 30.0));

        // Проверяем, что балансы не изменились
        assertEquals(20.0, fromAccount.getBalance());
        assertEquals(50.0, toAccount.getBalance());
    }

    @Test
    void freezeAccount_SetsAccountToFrozen() {
        // Arrange
        String accountId = "acc1";
        BankAccount account = new BankAccount(accountId, "John", 100);
        when(accountRepository.findById(accountId)).thenReturn(account);

        // Act
        bankAccountService.freezeAccount(accountId);

        // Assert
        assertTrue(account.isFrozen());
        verify(accountRepository).save(account);
    }

    @Test
    void applyInterest_ValidRate_CreatesTransactionWithCorrectTimestamp() {
        // Arrange
        String accountId = "acc1";
        BankAccount account = new BankAccount(accountId, "John", 100);
        when(accountRepository.findById(accountId)).thenReturn(account);

        // Act
        bankAccountService.applyInterest(accountId, 5.0);

        // Assert
        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(transactionCaptor.capture());

        Transaction savedTransaction = transactionCaptor.getValue();
        assertEquals(Transaction.TransactionType.INTEREST, savedTransaction.getType());
        assertEquals(5.0, savedTransaction.getAmount());
        assertNotNull(savedTransaction.getTimestamp());
    }

    @Test
    void getAllAccounts_ReturnsAllAccounts() {
            // Arrange
            List<BankAccount> expectedAccounts = Arrays.asList(
                    new BankAccount("acc1", "John", 100),
                    new BankAccount("acc2", "Jane", 200)
            );
            when(accountRepository.findAll()).thenReturn(expectedAccounts);

            // Act
            List<BankAccount> result = bankAccountService.getAllAccounts();

            // Assert
            assertEquals(expectedAccounts, result);
        }

        @Test
        void applyInterest_ValidRate_AppliesInterest() {
            // Arrange
            String accountId = "acc1";
            BankAccount account = new BankAccount(accountId, "John", 100);
            when(accountRepository.findById(accountId)).thenReturn(account);

            // Act
            bankAccountService.applyInterest(accountId, 5.0);

            // Assert
            assertEquals(105.0, account.getBalance());
            verify(transactionRepository).save(any(Transaction.class));
        }

        @ParameterizedTest
        @ValueSource(doubles = {0.0, -1.0})
        void applyInterest_InvalidRate_ThrowsException(double invalidRate) {
            assertThrows(IllegalArgumentException.class,
                    () -> bankAccountService.applyInterest("acc1", invalidRate));
        }
    @Test
    void deposit_NullAccountId_ThrowsNullPointerException() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> bankAccountService.deposit(null, 50.0)
        );
        assertEquals("Account ID cannot be null", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, -10.0, -0.01})
    void deposit_NonPositiveAmount_ThrowsIllegalArgumentException(double invalidAmount) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bankAccountService.deposit("acc1", invalidAmount)
        );
        assertTrue(exception.getMessage().contains("must be positive"));
    }

    @Test
    void transfer_SameAccount_ThrowsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bankAccountService.transfer("acc1", "acc1", 50.0)
        );
        assertEquals("Cannot transfer to the same account", exception.getMessage());
    }

    @Test
    void applyInterest_InvalidRate_ThrowsException() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> bankAccountService.applyInterest("acc1", 0.0)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> bankAccountService.applyInterest("acc1", -5.0))
        );
    }

    // Тест для проверки логирования
    @Test
    void transfer_LogsTransactionAttempt() throws InsufficientFundsException {
        // Arrange
        BankAccount fromAccount = new BankAccount("acc1", "John", 100);
        BankAccount toAccount = new BankAccount("acc2", "Jane", 50);

        when(accountRepository.findById("acc1")).thenReturn(fromAccount);
        when(accountRepository.findById("acc2")).thenReturn(toAccount);

        // Получаем корневой логгер
        Logger logger = (Logger) LoggerFactory.getLogger(BankAccountServiceImpl.class);

        // Создаем и запускаем appender для сбора логов
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();

        // Добавляем appender к логгеру
        logger.addAppender(listAppender);

        try {
            // Act
            bankAccountService.transfer("acc1", "acc2", 30);

            // Assert
            List<ILoggingEvent> logs = listAppender.list;
            assertTrue(logs.stream().anyMatch(log ->
                            log.getFormattedMessage().contains("Initiating transfer")),
                    "Log message about transfer initiation not found");

        } finally {
            // Очищаем appender после теста
            logger.detachAppender(listAppender);
        }
    }
    @Test
    void setBalance_NegativeValue_ThrowsException() {
        BankAccount account = new BankAccount("acc1", "John", 100);
        assertThrows(IllegalArgumentException.class,
                () -> account.setBalance(-50));
    }
    @Test
    void transfer_NullArguments_ThrowsException() {
        assertAll(
                () -> assertThrows(NullPointerException.class,
                        () -> bankAccountService.transfer(null, "acc2", 50)),
                () -> assertThrows(NullPointerException.class,
                        () -> bankAccountService.transfer("acc1", null, 50))
        );
    }
    @Test
    void getTransactionHistory_ReturnsTransactionsForAccount() {
        // Arrange
        String accountId = "acc1";
        Transaction t1 = new Transaction("id1", accountId,
                Transaction.TransactionType.DEPOSIT, 100.0);
        Transaction t2 = new Transaction("id2", accountId,
                Transaction.TransactionType.WITHDRAWAL, 50.0);

        when(transactionRepository.findByAccountId(accountId))
                .thenReturn(List.of(t1, t2));

        // Act
        List<Transaction> result = bankAccountService.getTransactionHistory(accountId);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains(t1));
        assertTrue(result.contains(t2));
    }

    @Test
    void unfreezeAccount_SetsAccountToUnfrozen() {
        // Arrange
        String accountId = "acc1";
        BankAccount account = new BankAccount(accountId, "John", 100);
        account.setFrozen(true);
        when(accountRepository.findById(accountId)).thenReturn(account);

        // Act
        bankAccountService.unfreezeAccount(accountId);

        // Assert
        assertFalse(account.isFrozen());
        verify(accountRepository).save(account);
    }

    @Test
    void withdraw_FromFrozenAccount_ThrowsException() {
        // Arrange
        String accountId = "acc1";
        BankAccount account = new BankAccount(accountId, "John", 100);
        account.setFrozen(true);
        when(accountRepository.findById(accountId)).thenReturn(account);

        // Act & Assert
        assertThrows(IllegalStateException.class,
                () -> bankAccountService.withdraw(accountId, 50));
    }

    @Test
    void deposit_ToFrozenAccount_ThrowsException() {
        // Arrange
        String accountId = "acc1";
        BankAccount account = new BankAccount(accountId, "John", 100);
        account.setFrozen(true);
        when(accountRepository.findById(accountId)).thenReturn(account);

        // Act & Assert
        assertThrows(IllegalStateException.class,
                () -> bankAccountService.deposit(accountId, 50));
    }
    @Test
    void createAccount_ZeroInitialBalance_Success() {
        BankAccount account = bankAccountService.createAccount("John", 0);
        assertEquals(0, account.getBalance());
    }

    @Test
    void withdraw_EntireBalance_Success() throws InsufficientFundsException {
        // Arrange
        String accountId = "acc1";
        double initialBalance = 100.0;
        BankAccount account = new BankAccount(accountId, "John", initialBalance);
        when(accountRepository.findById(accountId)).thenReturn(account);

        // Act
        bankAccountService.withdraw(accountId, initialBalance);

        // Assert
        assertEquals(0, account.getBalance());
    }

    @Test
    void transfer_ExactlyAvailableBalance_Success() throws InsufficientFundsException {
        // Arrange
        BankAccount fromAccount = new BankAccount("acc1", "John", 100);
        BankAccount toAccount = new BankAccount("acc2", "Jane", 50);
        when(accountRepository.findById("acc1")).thenReturn(fromAccount);
        when(accountRepository.findById("acc2")).thenReturn(toAccount);

        // Act
        bankAccountService.transfer("acc1", "acc2", 100);

        // Assert
        assertEquals(0, fromAccount.getBalance());
        assertEquals(150, toAccount.getBalance());
    }
    @Test
    void applyInterest_LogsTransaction() {
        // Arrange
        String accountId = "acc1";
        BankAccount account = new BankAccount(accountId, "John", 100);
        when(accountRepository.findById(accountId)).thenReturn(account);

        Logger logger = (Logger) LoggerFactory.getLogger(BankAccountServiceImpl.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        try {
            // Act
            bankAccountService.applyInterest(accountId, 5.0);

            // Assert
            List<ILoggingEvent> logs = listAppender.list;
            assertTrue(logs.stream().anyMatch(log ->
                    log.getFormattedMessage().contains("Applying interest")));
        } finally {
            logger.detachAppender(listAppender);
        }
    }
    @Test
    void getBalance_NonExistingAccount_ThrowsException() {
        // Arrange
        String accountId = "non-existing";
        when(accountRepository.findById(accountId)).thenReturn(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> bankAccountService.getBalance(accountId));
    }



    @Test
    void getTransactionHistory_NonExistingAccount_ReturnsEmptyList() {
        // Arrange
        String accountId = "non-existing";
        when(transactionRepository.findByAccountId(accountId)).thenReturn(List.of());

        // Act
        List<Transaction> result = bankAccountService.getTransactionHistory(accountId);

        // Assert
        assertTrue(result.isEmpty());
    }
}