package com.example.inventory_backend.service.impl;

import com.example.inventory_backend.dto.InventoryHistoryDTO;
import com.example.inventory_backend.model.Company;
import com.example.inventory_backend.model.Inventory;
import com.example.inventory_backend.model.InventoryHistory;
import com.example.inventory_backend.model.Product;
import com.example.inventory_backend.repository.InventoryHistoryRepository;
import com.example.inventory_backend.repository.InventoryRepository;
import com.example.inventory_backend.service.InventoryHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryHistoryServiceImpl implements InventoryHistoryService {
    
    @Autowired
    private InventoryHistoryRepository historyRepository;
    
    @Autowired
    private InventoryRepository inventoryRepository;
    
    @Override
    public void recordInventoryState(Company company) {
        int totalQuantity = calculateTotalQuantity(company);
        
        InventoryHistory history = new InventoryHistory();
        history.setTimestamp(LocalDateTime.now());
        history.setTotalQuantity(totalQuantity);
        history.setCompany(company);
        
        historyRepository.save(history);
    }
    
    @Override
    public void recordProductChange(Product product, Integer quantityChange, String reason, Company company) {
        int totalQuantity = calculateTotalQuantity(company);
        
        InventoryHistory history = new InventoryHistory();
        history.setTimestamp(LocalDateTime.now());
        history.setTotalQuantity(totalQuantity);
        history.setProduct(product);
        history.setQuantityChange(quantityChange);
        history.setChangeReason(reason);
        history.setCompany(company);
        
        historyRepository.save(history);
    }
    
    @Override
    public List<InventoryHistoryDTO> getRecentHistory(Company company) {
        List<InventoryHistory> history = historyRepository.findTop100ByCompanyOrderByTimestampDesc(company);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return history.stream()
                .map(item -> {
                    InventoryHistoryDTO dto = new InventoryHistoryDTO();
                    dto.setId(item.getId());
                    dto.setTimestamp(item.getTimestamp());
                    dto.setTotalQuantity(item.getTotalQuantity());
                    dto.setFormattedTimestamp(item.getTimestamp().format(formatter));
                    dto.setQuantityChange(item.getQuantityChange());
                    dto.setChangeReason(item.getChangeReason());

                    // Include product information if available
                    if (item.getProduct() != null) {
                        dto.setProductId(item.getProduct().getId());
                        dto.setProductName(item.getProduct().getName());
                        dto.setProductSku(item.getProduct().getSku());
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }
    
    private int calculateTotalQuantity(Company company) {
        return inventoryRepository.findByCompany(company).stream()
                .mapToInt(Inventory::getQuantity)
                .sum();
    }
}
