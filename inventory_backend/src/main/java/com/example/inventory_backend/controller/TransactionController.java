package com.example.inventory_backend.controller;

import com.example.inventory_backend.dto.TransactionDTO;
import com.example.inventory_backend.model.Company;
import com.example.inventory_backend.model.Transaction;
import com.example.inventory_backend.repository.CompanyRepository;
import com.example.inventory_backend.security.SecurityUtils;
import com.example.inventory_backend.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "http://localhost:3000")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private CompanyRepository companyRepository;
    
    private Company getCurrentCompany() {
        Long companyId = SecurityUtils.getCurrentCompanyId();
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));
    }

    @GetMapping
    public List<TransactionDTO> getAllTransactions() {
        Company company = getCurrentCompany();
        List<Transaction> transactions = transactionService.getAllTransactions(company);
        return transactions.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionDTO> getTransactionById(@PathVariable Long id) {
        try {
            Company company = getCurrentCompany();
            Transaction transaction = transactionService.getTransactionById(id, company);
            return ResponseEntity.ok(convertToDTO(transaction));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/product/{productId}")
    public List<TransactionDTO> getTransactionsByProduct(@PathVariable Long productId) {
        Company company = getCurrentCompany();
        List<Transaction> transactions = transactionService.getTransactionsByProductId(productId, company);
        return transactions.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    
    @GetMapping("/type/{type}")
    public List<TransactionDTO> getTransactionsByType(@PathVariable String type) {
        Company company = getCurrentCompany();
        Transaction.TransactionType transactionType = Transaction.TransactionType.valueOf(type.toUpperCase());
        List<Transaction> transactions = transactionService.getTransactionsByType(transactionType, company);
        return transactions.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    
    @GetMapping("/date-range")
    public List<TransactionDTO> getTransactionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        Company company = getCurrentCompany();
        List<Transaction> transactions = transactionService.getTransactionsByDateRange(start, end, company);
        return transactions.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<TransactionDTO> createTransaction(@RequestBody TransactionDTO transactionDTO) {
        try {
            Company company = getCurrentCompany();
            
            if (transactionDTO.getUserId() == null) {
                transactionDTO.setUserId(SecurityUtils.getCurrentUserId());
            }
            
            Transaction savedTransaction = transactionService.saveTransaction(transactionDTO, company);
            return new ResponseEntity<>(convertToDTO(savedTransaction), HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        try {
            Company company = getCurrentCompany();
            transactionService.deleteTransaction(id, company);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    private TransactionDTO convertToDTO(Transaction transaction) {
        TransactionDTO dto = new TransactionDTO();
        dto.setId(transaction.getId());
        dto.setTransactionType(transaction.getType().name());
        dto.setQuantity(transaction.getQuantity());
        dto.setTransactionDate(transaction.getTransactionDate());
        dto.setUnitPrice(transaction.getUnitPrice());
        dto.setTotalAmount(transaction.getTotalAmount());
        dto.setNotes(transaction.getNotes());
        dto.setReferenceNumber(transaction.getReferenceNumber());
        
        if (transaction.getProduct() != null) {
            dto.setProductId(transaction.getProduct().getId());
            dto.setProductName(transaction.getProduct().getName());
        }
        
        if (transaction.getCreatedBy() != null) {
            dto.setUserId(transaction.getCreatedBy().getId());
            dto.setUserName(transaction.getCreatedBy().getName());
        }
        
        return dto;
    }
}
