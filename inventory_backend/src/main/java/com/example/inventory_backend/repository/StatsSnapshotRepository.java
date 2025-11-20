package com.example.inventory_backend.repository;

import com.example.inventory_backend.model.Company;
import com.example.inventory_backend.model.StatsSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StatsSnapshotRepository extends JpaRepository<StatsSnapshot, Long> {

    // Get the most recent snapshot for a company (yesterday or earlier)
    Optional<StatsSnapshot> findTopByCompanyAndSnapshotDateBeforeOrderBySnapshotDateDesc(
            Company company, LocalDate date);

    // Get snapshot for a specific date
    Optional<StatsSnapshot> findByCompanyAndSnapshotDate(Company company, LocalDate date);

    // Get all snapshots for a company ordered by date
    List<StatsSnapshot> findByCompanyOrderBySnapshotDateDesc(Company company);

    // Get snapshots within a date range
    List<StatsSnapshot> findByCompanyAndSnapshotDateBetweenOrderBySnapshotDateAsc(
            Company company, LocalDate startDate, LocalDate endDate);

    // Check if snapshot exists for today
    boolean existsByCompanyAndSnapshotDate(Company company, LocalDate date);
}
