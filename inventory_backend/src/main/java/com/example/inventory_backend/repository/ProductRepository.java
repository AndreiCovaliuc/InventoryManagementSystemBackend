package com.example.inventory_backend.repository;

import com.example.inventory_backend.model.Category;
import com.example.inventory_backend.model.Company;
import com.example.inventory_backend.model.Product;
import com.example.inventory_backend.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCompany(Company company);
    List<Product> findByCompanyId(Long companyId);
    
    List<Product> findByCategoryAndCompany(Category category, Company company);
    List<Product> findBySupplierAndCompany(Supplier supplier, Company company);
    
    Optional<Product> findBySkuAndCompany(String sku, Company company);
    
    List<Product> findByNameContainingIgnoreCaseAndCompany(String name, Company company);
    
    @Query("SELECT p FROM Product p WHERE p.price <= :maxPrice AND p.company = :company")
    List<Product> findByPriceLessThanEqualAndCompany(@Param("maxPrice") BigDecimal maxPrice, @Param("company") Company company);
    
    // Keep old methods for backward compatibility but they should be deprecated
    List<Product> findByCategory(Category category);
    List<Product> findBySupplier(Supplier supplier);
    Optional<Product> findBySku(String sku);
    List<Product> findByNameContainingIgnoreCase(String name);
    
    @Query("SELECT p FROM Product p WHERE p.price <= :maxPrice")
    List<Product> findByPriceLessThanEqual(BigDecimal maxPrice);
}
