package com.example.inventory_backend.service.impl;

import com.example.inventory_backend.model.Company;
import com.example.inventory_backend.model.Inventory;
import com.example.inventory_backend.model.Product;
import com.example.inventory_backend.repository.InventoryRepository;
import com.example.inventory_backend.repository.ProductRepository;
import com.example.inventory_backend.service.InventoryHistoryService;
import com.example.inventory_backend.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    
    @Autowired
    private InventoryHistoryService historyService;
    
    @Autowired
    public InventoryServiceImpl(InventoryRepository inventoryRepository, ProductRepository productRepository) {
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
    }
    
    @Override
    public List<Inventory> getAllInventory(Company company) {
        return inventoryRepository.findByCompany(company);
    }
    
    @Override
    public Inventory getInventoryById(Long id, Company company) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inventory not found with id: " + id));
        
        if (!inventory.getCompany().getId().equals(company.getId())) {
            throw new RuntimeException("Inventory not found with id: " + id);
        }
        
        return inventory;
    }
    
    @Override
    public Inventory getInventoryByProduct(Product product, Company company) {
        Inventory inventory = inventoryRepository.findByProduct(product)
                .orElseThrow(() -> new RuntimeException("Inventory not found for product: " + product.getName()));
        
        if (!inventory.getCompany().getId().equals(company.getId())) {
            throw new RuntimeException("Inventory not found for product: " + product.getName());
        }
        
        return inventory;
    }
    
    @Override
    public List<Inventory> getLowStockItems(Company company) {
        return inventoryRepository.findLowStockItemsByCompany(company);
    }
    
    @Override
    public Inventory saveInventory(Inventory inventory, Company company) {
        boolean isNewInventory = inventory.getId() == null;
        inventory.setCompany(company);
        inventory.setLastUpdated(LocalDateTime.now());
        Inventory savedInventory = inventoryRepository.save(inventory);
        
        if (isNewInventory) {
            historyService.recordProductChange(inventory.getProduct(), inventory.getQuantity(), "Initial inventory", company);
        } else {
            historyService.recordInventoryState(company);
        }
        
        return savedInventory;
    }
    
    @Override
    @Transactional
    public Inventory updateQuantity(Long productId, Integer quantityChange, Company company) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
        
        if (!product.getCompany().getId().equals(company.getId())) {
            throw new RuntimeException("Product not found with id: " + productId);
        }
        
        Inventory inventory = inventoryRepository.findByProduct(product)
                .orElseThrow(() -> new RuntimeException("Inventory not found for product: " + product.getName()));
        
        inventory.setQuantity(inventory.getQuantity() + quantityChange);
        inventory.setLastUpdated(LocalDateTime.now());
        
        Inventory updatedInventory = inventoryRepository.save(inventory);
        
        historyService.recordProductChange(product, quantityChange, 
                quantityChange > 0 ? "Stock increase" : "Stock decrease", company);
        
        return updatedInventory;
    }
    
    @Override
    public boolean isInStock(Long productId, Integer quantity, Company company) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
        
        if (!product.getCompany().getId().equals(company.getId())) {
            throw new RuntimeException("Product not found with id: " + productId);
        }
        
        Inventory inventory = inventoryRepository.findByProduct(product)
                .orElseThrow(() -> new RuntimeException("Inventory not found for product: " + product.getName()));
        
        return inventory.getQuantity() >= quantity;
    }
    
    @Override
    public void deleteInventory(Long id, Company company) {
        Inventory inventory = getInventoryById(id, company);
        Product product = inventory.getProduct();
        
        inventoryRepository.deleteById(id);
        
        historyService.recordProductChange(product, -inventory.getQuantity(), "Inventory deleted", company);
    }
}
