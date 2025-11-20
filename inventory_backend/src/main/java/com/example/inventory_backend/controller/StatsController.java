package com.example.inventory_backend.controller;

import com.example.inventory_backend.dto.InventoryHistoryDTO;
import com.example.inventory_backend.dto.StatsSnapshotDTO;
import com.example.inventory_backend.model.Company;
import com.example.inventory_backend.model.Inventory;
import com.example.inventory_backend.model.StatsSnapshot;
import com.example.inventory_backend.repository.CompanyRepository;
import com.example.inventory_backend.repository.StatsSnapshotRepository;
import com.example.inventory_backend.security.SecurityUtils;
import com.example.inventory_backend.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    @Autowired
    private StatsSnapshotRepository statsSnapshotRepository;

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

    @GetMapping("/previous")
    public ResponseEntity<StatsSnapshotDTO> getPreviousStats() {
        Company company = getCurrentCompany();
        LocalDate today = LocalDate.now();

        // Try to get yesterday's snapshot first
        Optional<StatsSnapshot> snapshot = statsSnapshotRepository
                .findTopByCompanyAndSnapshotDateBeforeOrderBySnapshotDateDesc(company, today);

        if (snapshot.isEmpty()) {
            // No previous data available
            return ResponseEntity.noContent().build();
        }

        StatsSnapshotDTO dto = convertToDTO(snapshot.get());
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/snapshot")
    public ResponseEntity<StatsSnapshotDTO> recordCurrentSnapshot() {
        Company company = getCurrentCompany();
        LocalDate today = LocalDate.now();

        // Check if snapshot already exists for today
        if (statsSnapshotRepository.existsByCompanyAndSnapshotDate(company, today)) {
            // Update existing snapshot
            Optional<StatsSnapshot> existing = statsSnapshotRepository
                    .findByCompanyAndSnapshotDate(company, today);
            if (existing.isPresent()) {
                StatsSnapshot snapshot = existing.get();
                updateSnapshotData(snapshot, company);
                statsSnapshotRepository.save(snapshot);
                return ResponseEntity.ok(convertToDTO(snapshot));
            }
        }

        // Create new snapshot
        StatsSnapshot snapshot = new StatsSnapshot();
        snapshot.setCompany(company);
        snapshot.setSnapshotDate(today);
        updateSnapshotData(snapshot, company);
        statsSnapshotRepository.save(snapshot);

        return ResponseEntity.ok(convertToDTO(snapshot));
    }

    @GetMapping("/history")
    public ResponseEntity<List<StatsSnapshotDTO>> getStatsHistory() {
        Company company = getCurrentCompany();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);

        List<StatsSnapshot> snapshots = statsSnapshotRepository
                .findByCompanyAndSnapshotDateBetweenOrderBySnapshotDateAsc(company, startDate, endDate);

        List<StatsSnapshotDTO> dtos = snapshots.stream()
                .map(this::convertToDTO)
                .toList();

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
