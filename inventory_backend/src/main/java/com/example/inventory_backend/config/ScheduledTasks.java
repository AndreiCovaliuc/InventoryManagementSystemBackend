package com.example.inventory_backend.config;

import com.example.inventory_backend.model.Company;
import com.example.inventory_backend.model.Inventory;
import com.example.inventory_backend.model.StatsSnapshot;
import com.example.inventory_backend.repository.CompanyRepository;
import com.example.inventory_backend.repository.StatsSnapshotRepository;
import com.example.inventory_backend.service.CategoryService;
import com.example.inventory_backend.service.InventoryService;
import com.example.inventory_backend.service.ProductService;
import com.example.inventory_backend.service.SupplierService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class ScheduledTasks {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private StatsSnapshotRepository statsSnapshotRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private InventoryService inventoryService;

    // Run every day at midnight
    @Scheduled(cron = "0 0 0 * * ?")
    public void recordDailyStats() {
        logger.info("Starting daily stats snapshot recording...");

        List<Company> companies = companyRepository.findAll();
        LocalDate today = LocalDate.now();

        for (Company company : companies) {
            try {
                // Skip if snapshot already exists for today
                if (statsSnapshotRepository.existsByCompanyAndSnapshotDate(company, today)) {
                    logger.debug("Snapshot already exists for company {} on {}", company.getName(), today);
                    continue;
                }

                StatsSnapshot snapshot = new StatsSnapshot();
                snapshot.setCompany(company);
                snapshot.setSnapshotDate(today);
                snapshot.setTotalProducts(productService.getAllProducts(company).size());
                snapshot.setTotalCategories(categoryService.getAllCategories(company).size());
                snapshot.setTotalSuppliers(supplierService.getAllSuppliers(company).size());
                snapshot.setLowStockItems(inventoryService.getLowStockItems(company).size());

                int totalQuantity = inventoryService.getAllInventory(company).stream()
                        .mapToInt(Inventory::getQuantity)
                        .sum();
                snapshot.setTotalInventoryQuantity(totalQuantity);

                statsSnapshotRepository.save(snapshot);
                logger.info("Recorded stats snapshot for company: {}", company.getName());

            } catch (Exception e) {
                logger.error("Failed to record stats snapshot for company: {}", company.getName(), e);
            }
        }

        logger.info("Daily stats snapshot recording completed");
    }
}
