package com.example.inventory_backend.dto;

import java.time.LocalDate;

public class StatsSnapshotDTO {
    private Long id;
    private LocalDate snapshotDate;
    private Integer totalProducts;
    private Integer totalCategories;
    private Integer totalSuppliers;
    private Integer lowStockItems;
    private Integer totalInventoryQuantity;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getSnapshotDate() { return snapshotDate; }
    public void setSnapshotDate(LocalDate snapshotDate) { this.snapshotDate = snapshotDate; }
    public Integer getTotalProducts() { return totalProducts; }
    public void setTotalProducts(Integer totalProducts) { this.totalProducts = totalProducts; }
    public Integer getTotalCategories() { return totalCategories; }
    public void setTotalCategories(Integer totalCategories) { this.totalCategories = totalCategories; }
    public Integer getTotalSuppliers() { return totalSuppliers; }
    public void setTotalSuppliers(Integer totalSuppliers) { this.totalSuppliers = totalSuppliers; }
    public Integer getLowStockItems() { return lowStockItems; }
    public void setLowStockItems(Integer lowStockItems) { this.lowStockItems = lowStockItems; }
    public Integer getTotalInventoryQuantity() { return totalInventoryQuantity; }
    public void setTotalInventoryQuantity(Integer totalInventoryQuantity) { this.totalInventoryQuantity = totalInventoryQuantity; }
}
