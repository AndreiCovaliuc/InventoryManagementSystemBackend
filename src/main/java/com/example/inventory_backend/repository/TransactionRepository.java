package com.example.inventory_backend.repository;

import com.example.inventory_backend.model.Company;
import com.example.inventory_backend.model.Transaction;
import com.example.inventory_backend.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByProduct(Product product);
    List<Transaction> findByType(Transaction.TransactionType type);
    List<Transaction> findByTransactionDateBetween(LocalDateTime start, LocalDateTime end);
    List<Transaction> findByProductAndTransactionDateBetween(Product product, LocalDateTime start, LocalDateTime end);
    
    // Company-scoped queries
    List<Transaction> findByCompany(Company company);
    List<Transaction> findByCompanyId(Long companyId);
    List<Transaction> findByTypeAndCompany(Transaction.TransactionType type, Company company);
    List<Transaction> findByTransactionDateBetweenAndCompany(LocalDateTime start, LocalDateTime end, Company company);
    List<Transaction> findByProductAndCompany(Product product, Company company);
    
    @Modifying
    @Query("DELETE FROM Transaction t WHERE t.product.id = :productId")
    void deleteByProductId(@Param("productId") Long productId);
}