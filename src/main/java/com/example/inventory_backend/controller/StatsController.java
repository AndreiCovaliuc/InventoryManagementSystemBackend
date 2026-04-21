package com.example.inventory_backend.controller;

import com.example.inventory_backend.dto.InventoryHistoryDTO;
import com.example.inventory_backend.dto.StatsSnapshotDTO;
import com.example.inventory_backend.model.Company;
import com.example.inventory_backend.model.Inventory;
import com.example.inventory_backend.model.StatsSnapshot;
import com.example.inventory_backend.security.SecurityUtils;
import com.example.inventory_backend.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
    private CompanyService companyService;

    @Autowired
    private StatsSnapshotService statsSnapshotService;

    private Company getCurrentCompany() {
        return companyService.getCompanyById(SecurityUtils.getCurrentCompanyId());
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

    @GetMapping("/previous")
    public ResponseEntity<StatsSnapshotDTO> getPreviousStats() {
        Company company = getCurrentCompany();
        return statsSnapshotService.getPreviousSnapshot(company)
                .map(s -> ResponseEntity.ok(convertToDTO(s)))
                .orElse(ResponseEntity.noContent().build());
    }

    @PostMapping("/snapshot")
    public ResponseEntity<StatsSnapshotDTO> recordCurrentSnapshot() {
        Company company = getCurrentCompany();
        StatsSnapshot snapshot = new StatsSnapshot();
        snapshot.setCompany(company);
        snapshot.setSnapshotDate(LocalDate.now());
        updateSnapshotData(snapshot, company);
        StatsSnapshot saved = statsSnapshotService.saveOrUpdateTodaySnapshot(company, snapshot);
        return ResponseEntity.ok(convertToDTO(saved));
    }

    @GetMapping("/history")
    public ResponseEntity<List<StatsSnapshotDTO>> getStatsHistory() {
        Company company = getCurrentCompany();
        LocalDate endDate = LocalDate.now();
        List<StatsSnapshotDTO> dtos = statsSnapshotService
                .getSnapshotHistory(company, endDate.minusDays(30), endDate)
                .stream().map(this::convertToDTO).toList();
        return ResponseEntity.ok(dtos);
    }

    private void updateSnapshotData(StatsSnapshot snapshot, Company company) {
        snapshot.setTotalProducts(productService.getAllProducts(company).size());
        snapshot.setTotalCategories(categoryService.getAllCategories(company).size());
        snapshot.setTotalSuppliers(supplierService.getAllSuppliers(company).size());
        snapshot.setLowStockItems(inventoryService.getLowStockItems(company).size());

        int totalQuantity = inventoryService.getAllInventory(company).stream()
                .mapToInt(Inventory::getQuantity)
                .sum();
        snapshot.setTotalInventoryQuantity(totalQuantity);
    }

    private StatsSnapshotDTO convertToDTO(StatsSnapshot snapshot) {
        StatsSnapshotDTO dto = new StatsSnapshotDTO();
        dto.setId(snapshot.getId());
        dto.setSnapshotDate(snapshot.getSnapshotDate());
        dto.setTotalProducts(snapshot.getTotalProducts());
        dto.setTotalCategories(snapshot.getTotalCategories());
        dto.setTotalSuppliers(snapshot.getTotalSuppliers());
        dto.setLowStockItems(snapshot.getLowStockItems());
        dto.setTotalInventoryQuantity(snapshot.getTotalInventoryQuantity());
        return dto;
    }
}
