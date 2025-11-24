package com.example.inventory_backend.service;

import com.example.inventory_backend.dto.TransactionDTO;
import com.example.inventory_backend.model.Company;
import com.example.inventory_backend.model.Transaction;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionService {
    List<Transaction> getAllTransactions(Company company);
    Transaction getTransactionById(Long id, Company company);
    List<Transaction> getTransactionsByProductId(Long productId, Company company);
    List<Transaction> getTransactionsByType(Transaction.TransactionType type, Company company);
    List<Transaction> getTransactionsByDateRange(LocalDateTime start, LocalDateTime end, Company company);
    Transaction saveTransaction(TransactionDTO transactionDTO, Company company);
    void deleteTransaction(Long id, Company company);
}
