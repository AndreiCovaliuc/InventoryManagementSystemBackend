package com.example.inventory_backend.service;

import com.example.inventory_backend.model.Company;
import com.example.inventory_backend.model.StatsSnapshot;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StatsSnapshotService {
    Optional<StatsSnapshot> getPreviousSnapshot(Company company);
    StatsSnapshot saveOrUpdateTodaySnapshot(Company company, StatsSnapshot snapshot);
    List<StatsSnapshot> getSnapshotHistory(Company company, LocalDate startDate, LocalDate endDate);
}
