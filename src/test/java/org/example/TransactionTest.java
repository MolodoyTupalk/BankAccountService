package org.example;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {
    @Test
    void transactionCreation_WithAllParameters_Success() {
        LocalDateTime now = LocalDateTime.now();
        Transaction transaction = new Transaction("id1", "acc1",
                Transaction.TransactionType.DEPOSIT, 100.0, now);

        assertEquals("id1", transaction.getId());
        assertEquals("acc1", transaction.getAccountId());
        assertEquals(Transaction.TransactionType.DEPOSIT, transaction.getType());
        assertEquals(100.0, transaction.getAmount());
        assertEquals(now, transaction.getTimestamp());
    }

    @Test
    void transactionCreation_WithoutTimestamp_SetsCurrentTime() {
        Transaction transaction = new Transaction("id1", "acc1",
                Transaction.TransactionType.DEPOSIT, 100.0);

        assertNotNull(transaction.getTimestamp());
    }
}