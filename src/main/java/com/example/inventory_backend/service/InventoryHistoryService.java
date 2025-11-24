package com.example.inventory_backend.service;

import com.example.inventory_backend.dto.InventoryHistoryDTO;
import com.example.inventory_backend.model.Company;
import com.example.inventory_backend.model.Product;

import java.util.List;

public interface InventoryHistoryService {
    void recordInventoryState(Company company);
    void recordProductChange(Product product, Integer quantityChange, String reason, Company company);
    List<InventoryHistoryDTO> getRecentHistory(Company company);
}
