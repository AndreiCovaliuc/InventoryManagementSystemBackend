package com.example.inventory_backend.repository;

import com.example.inventory_backend.model.Company;
import com.example.inventory_backend.model.InventoryHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InventoryHistoryRepository extends JpaRepository<InventoryHistory, Long> {
    List<InventoryHistory> findTop30ByOrderByTimestampDesc();
    List<InventoryHistory> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    
    // Company-scoped queries
    List<InventoryHistory> findTop30ByCompanyOrderByTimestampDesc(Company company);
    List<InventoryHistory> findByTimestampBetweenAndCompany(LocalDateTime start, LocalDateTime end, Company company);
    List<InventoryHistory> findByCompany(Company company);
    List<InventoryHistory> findByCompanyId(Long companyId);
    
    @Modifying
    @Query("DELETE FROM InventoryHistory ih WHERE ih.product.id = :productId")
    void deleteByProductId(@Param("productId") Long productId);
}