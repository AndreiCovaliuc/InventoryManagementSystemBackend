package com.example.inventory_backend.service.impl;

import com.example.inventory_backend.model.Company;
import com.example.inventory_backend.model.StatsSnapshot;
import com.example.inventory_backend.repository.StatsSnapshotRepository;
import com.example.inventory_backend.service.StatsSnapshotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class StatsSnapshotServiceImpl implements StatsSnapshotService {

    private final StatsSnapshotRepository statsSnapshotRepository;

    @Autowired
    public StatsSnapshotServiceImpl(StatsSnapshotRepository statsSnapshotRepository) {
        this.statsSnapshotRepository = statsSnapshotRepository;
    }

    @Override
    public Optional<StatsSnapshot> getPreviousSnapshot(Company company) {
        return statsSnapshotRepository
                .findTopByCompanyAndSnapshotDateBeforeOrderBySnapshotDateDesc(company, LocalDate.now());
    }

    @Override
    public StatsSnapshot saveOrUpdateTodaySnapshot(Company company, StatsSnapshot snapshot) {
        LocalDate today = LocalDate.now();
        if (statsSnapshotRepository.existsByCompanyAndSnapshotDate(company, today)) {
            Optional<StatsSnapshot> existing = statsSnapshotRepository
                    .findByCompanyAndSnapshotDate(company, today);
            if (existing.isPresent()) {
                StatsSnapshot s = existing.get();
                s.setTotalProducts(snapshot.getTotalProducts());
                s.setTotalCategories(snapshot.getTotalCategories());
                s.setTotalSuppliers(snapshot.getTotalSuppliers());
                s.setLowStockItems(snapshot.getLowStockItems());
                s.setTotalInventoryQuantity(snapshot.getTotalInventoryQuantity());
                return statsSnapshotRepository.save(s);
            }
        }
        return statsSnapshotRepository.save(snapshot);
    }

    @Override
    public List<StatsSnapshot> getSnapshotHistory(Company company, LocalDate startDate, LocalDate endDate) {
        return statsSnapshotRepository
                .findByCompanyAndSnapshotDateBetweenOrderBySnapshotDateAsc(company, startDate, endDate);
    }
}
