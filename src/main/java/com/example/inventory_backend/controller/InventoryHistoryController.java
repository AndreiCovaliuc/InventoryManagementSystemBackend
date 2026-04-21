package com.example.inventory_backend.controller;

import com.example.inventory_backend.dto.InventoryHistoryDTO;
import com.example.inventory_backend.model.Company;
import com.example.inventory_backend.service.CompanyService;
import com.example.inventory_backend.security.SecurityUtils;
import com.example.inventory_backend.service.InventoryHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory-history")
@CrossOrigin(origins = "http://localhost:3000")
public class InventoryHistoryController {

    @Autowired
    private InventoryHistoryService historyService;

    @Autowired
    private CompanyService companyService;
    
    private Company getCurrentCompany() {
        Long companyId = SecurityUtils.getCurrentCompanyId();
        return companyService.getCompanyById(companyId);
    }

    @GetMapping
    public ResponseEntity<List<InventoryHistoryDTO>> getRecentHistory() {
        Company company = getCurrentCompany();
        List<InventoryHistoryDTO> history = historyService.getRecentHistory(company);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<InventoryHistoryDTO>> getRecentHistoryAlias() {
        Company company = getCurrentCompany();
        List<InventoryHistoryDTO> history = historyService.getRecentHistory(company);
        return ResponseEntity.ok(history);
    }

    @PostMapping("/record")
    public ResponseEntity<Void> recordCurrentState() {
        Company company = getCurrentCompany();
        historyService.recordInventoryState(company);
        return ResponseEntity.ok().build();
    }
}
