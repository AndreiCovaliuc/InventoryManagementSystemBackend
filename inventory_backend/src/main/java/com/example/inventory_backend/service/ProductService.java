package com.example.inventory_backend.service;

import com.example.inventory_backend.model.Category;
import com.example.inventory_backend.model.Company;
import com.example.inventory_backend.model.Product;
import com.example.inventory_backend.model.Supplier;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {
    List<Product> getAllProducts(Company company);
    
    Product getProductById(Long id, Company company);
    
    Product getProductBySku(String sku, Company company);
    
    List<Product> getProductsByCategory(Category category, Company company);
    
    List<Product> getProductsBySupplier(Supplier supplier, Company company);
    
    List<Product> searchProductsByName(String name, Company company);
    
    List<Product> getProductsByMaxPrice(BigDecimal maxPrice, Company company);
    
    Product saveProduct(Product product, Company company);
    
    void deleteProduct(Long id, Company company);
}
