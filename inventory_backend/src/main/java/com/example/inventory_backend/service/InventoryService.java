package com.example.inventory_backend.service;

import com.example.inventory_backend.model.Company;
import com.example.inventory_backend.model.Inventory;
import com.example.inventory_backend.model.Product;

import java.util.List;

public interface InventoryService {
    List<Inventory> getAllInventory(Company company);
    Inventory getInventoryById(Long id, Company company);
    Inventory getInventoryByProduct(Product product, Company company);
    List<Inventory> getLowStockItems(Company company);
    Inventory saveInventory(Inventory inventory, Company company);
    Inventory updateQuantity(Long productId, Integer quantityChange, Company company);
    boolean isInStock(Long productId, Integer quantity, Company company);
    void deleteInventory(Long id, Company company);
}
