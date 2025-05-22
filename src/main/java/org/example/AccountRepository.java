package org.example;

import java.util.List;

// Интерфейс репозитория для работы с банковскими счетами
public interface AccountRepository {
    BankAccount findById(String accountId); // Найти счет по идентификатору
    void save(BankAccount account); // Сохранить или обновить счет
    void delete(String accountId); // Удалить счет
    List<BankAccount> findAll(); // Получить все счета
}
