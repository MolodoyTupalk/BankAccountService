package org.example;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class InMemoryTransactionRepositoryTest {
    @Test
    void saveAndFindByAccountId_ReturnsCorrectTransactions() {
        TransactionRepository repository = new InMemoryTransactionRepository();
        Transaction t1 = new Transaction("id1", "acc1",
                Transaction.TransactionType.DEPOSIT, 100.0);
        Transaction t2 = new Transaction("id2", "acc1",
                Transaction.TransactionType.WITHDRAWAL, 50.0);

        repository.save(t1);
        repository.save(t2);

        List<Transaction> result = repository.findByAccountId("acc1");
        assertEquals(2, result.size());
        assertTrue(result.contains(t1));
        assertTrue(result.contains(t2));
    }

    @Test
    void findByAccountId_UnknownAccount_ReturnsEmptyList() {
        TransactionRepository repository = new InMemoryTransactionRepository();
        assertEquals(0, repository.findByAccountId("unknown").size());
    }
}