package com.example.inventory_backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "stats_snapshot")
@Data
public class StatsSnapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate snapshotDate;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private Integer totalProducts;

    @Column(nullable = false)
    private Integer totalCategories;

    @Column(nullable = false)
    private Integer totalSuppliers;

    @Column(nullable = false)
    private Integer lowStockItems;

    @Column(nullable = false)
    private Integer totalInventoryQuantity;

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (snapshotDate == null) {
            snapshotDate = LocalDate.now();
        }
    }
}
