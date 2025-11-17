package com.example.inventory_backend.service.impl;

import com.example.inventory_backend.dto.TransactionDTO;
import com.example.inventory_backend.model.Company;
import com.example.inventory_backend.model.Transaction;
import com.example.inventory_backend.model.Product;
import com.example.inventory_backend.model.User;
import com.example.inventory_backend.repository.TransactionRepository;
import com.example.inventory_backend.repository.ProductRepository;
import com.example.inventory_backend.repository.UserRepository;
import com.example.inventory_backend.service.TransactionService;
import com.example.inventory_backend.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private InventoryService inventoryService;

    @Override
    public List<Transaction> getAllTransactions(Company company) {
        return transactionRepository.findByCompany(company);
    }

    @Override
    public Transaction getTransactionById(Long id, Company company) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + id));
        
        if (!transaction.getCompany().getId().equals(company.getId())) {
            throw new RuntimeException("Transaction not found with id: " + id);
        }
        
        return transaction;
    }

    @Override
    public List<Transaction> getTransactionsByProductId(Long productId, Company company) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
        
        if (!product.getCompany().getId().equals(company.getId())) {
            throw new RuntimeException("Product not found with id: " + productId);
        }
        
        return transactionRepository.findByProductAndCompany(product, company);
    }

    @Override
    public List<Transaction> getTransactionsByType(Transaction.TransactionType type, Company company) {
        return transactionRepository.findByTypeAndCompany(type, company);
    }

    @Override
    public List<Transaction> getTransactionsByDateRange(LocalDateTime start, LocalDateTime end, Company company) {
        return transactionRepository.findByTransactionDateBetweenAndCompany(start, end, company);
    }

    @Override
    @Transactional
    public Transaction saveTransaction(TransactionDTO transactionDTO, Company company) {
        Transaction transaction = new Transaction();
        
        Product product = productRepository.findById(transactionDTO.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + transactionDTO.getProductId()));
        
        if (!product.getCompany().getId().equals(company.getId())) {
            throw new RuntimeException("Product not found with id: " + transactionDTO.getProductId());
        }
        
        transaction.setProduct(product);
        transaction.setType(Transaction.TransactionType.valueOf(transactionDTO.getTransactionType()));
        transaction.setQuantity(transactionDTO.getQuantity());
        transaction.setTransactionDate(transactionDTO.getTransactionDate() != null ? 
                transactionDTO.getTransactionDate() : LocalDateTime.now());
        transaction.setUnitPrice(transactionDTO.getUnitPrice());
        
        if (transactionDTO.getTotalAmount() == null && transactionDTO.getUnitPrice() != null) {
            BigDecimal total = transactionDTO.getUnitPrice().multiply(
                    new BigDecimal(transactionDTO.getQuantity()));
            transaction.setTotalAmount(total);
        } else {
            transaction.setTotalAmount(transactionDTO.getTotalAmount());
        }
        
        transaction.setNotes(transactionDTO.getNotes());
        transaction.setReferenceNumber(transactionDTO.getReferenceNumber());
        transaction.setCompany(company);
        
        if (transactionDTO.getUserId() != null) {
            User user = userRepository.findById(transactionDTO.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + transactionDTO.getUserId()));
            transaction.setCreatedBy(user);
        }
        
        updateInventory(transaction, company);
        
        return transactionRepository.save(transaction);
    }

    @Override
    public void deleteTransaction(Long id, Company company) {
        Transaction transaction = getTransactionById(id, company);
        transactionRepository.deleteById(transaction.getId());
    }
    
    private void updateInventory(Transaction transaction, Company company) {
        int quantityChange = 0;
        
        switch (transaction.getType()) {
            case PURCHASE:
            case RETURN:
                quantityChange = transaction.getQuantity();
                break;
            case SALE:
                quantityChange = -transaction.getQuantity();
                break;
            case ADJUSTMENT:
                quantityChange = transaction.getQuantity();
                break;
            case TRANSFER:
                break;
        }
        
        if (quantityChange != 0) {
            inventoryService.updateQuantity(transaction.getProduct().getId(), quantityChange, company);
        }
    }
}
