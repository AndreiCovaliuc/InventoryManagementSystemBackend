package com.example.inventory_backend.controller;

import com.example.inventory_backend.dto.InventoryHistoryDTO;
import com.example.inventory_backend.model.Company;
import com.example.inventory_backend.model.Inventory;
import com.example.inventory_backend.repository.CompanyRepository;
import com.example.inventory_backend.security.SecurityUtils;
import com.example.inventory_backend.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@CrossOrigin(origins = "http://localhost:3000")
public class StatsController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InventoryHistoryService historyService;
    
    @Autowired
    private CompanyRepository companyRepository;
    
    private Company getCurrentCompany() {
        Long companyId = SecurityUtils.getCurrentCompanyId();
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummaryStats() {
        Company company = getCurrentCompany();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalProducts", productService.getAllProducts(company).size());
        stats.put("totalCategories", categoryService.getAllCategories(company).size());
        stats.put("totalSuppliers", supplierService.getAllSuppliers(company).size());
        stats.put("lowStockItems", inventoryService.getLowStockItems(company).size());
        
        int totalInventory = inventoryService.getAllInventory(company).stream()
                .mapToInt(Inventory::getQuantity)
                .sum();
        stats.put("totalInventoryValue", totalInventory);
        
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/inventory-history")
    public ResponseEntity<List<InventoryHistoryDTO>> getInventoryHistory() {
        Company company = getCurrentCompany();
        List<InventoryHistoryDTO> history = historyService.getRecentHistory(company);
        return ResponseEntity.ok(history);
    }
}
