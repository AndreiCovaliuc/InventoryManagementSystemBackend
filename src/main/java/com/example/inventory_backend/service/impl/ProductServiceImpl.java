package com.example.inventory_backend.service.impl;

import com.example.inventory_backend.model.Category;
import com.example.inventory_backend.model.Company;
import com.example.inventory_backend.model.Product;
import com.example.inventory_backend.model.Supplier;
import com.example.inventory_backend.repository.InventoryRepository;
import com.example.inventory_backend.repository.ProductRepository;
import com.example.inventory_backend.repository.InventoryHistoryRepository;
import com.example.inventory_backend.repository.TransactionRepository;
import com.example.inventory_backend.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {
    
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryHistoryRepository inventoryHistoryRepository;
    private final TransactionRepository transactionRepository;
    
    @Autowired
    public ProductServiceImpl(ProductRepository productRepository, 
                              InventoryRepository inventoryRepository,
                              InventoryHistoryRepository inventoryHistoryRepository,
                              TransactionRepository transactionRepository) {
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.inventoryHistoryRepository = inventoryHistoryRepository;
        this.transactionRepository = transactionRepository;
    }
    
    @Override
    public List<Product> getAllProducts(Company company) {
        return productRepository.findByCompany(company);
    }
    
    @Override
    public Product getProductById(Long id, Company company) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        
        if (!product.getCompany().getId().equals(company.getId())) {
            throw new RuntimeException("Product not found with id: " + id);
        }
        
        return product;
    }
    
    @Override
    public Product getProductBySku(String sku, Company company) {
        return productRepository.findBySkuAndCompany(sku, company)
                .orElseThrow(() -> new RuntimeException("Product not found with SKU: " + sku));
    }
    
    @Override
    public List<Product> getProductsByCategory(Category category, Company company) {
        return productRepository.findByCategoryAndCompany(category, company);
    }
    
    @Override
    public List<Product> getProductsBySupplier(Supplier supplier, Company company) {
        return productRepository.findBySupplierAndCompany(supplier, company);
    }
    
    @Override
    public List<Product> searchProductsByName(String name, Company company) {
        return productRepository.findByNameContainingIgnoreCaseAndCompany(name, company);
    }
    
    @Override
    public List<Product> getProductsByMaxPrice(BigDecimal maxPrice, Company company) {
        return productRepository.findByPriceLessThanEqualAndCompany(maxPrice, company);
    }
    
    @Override
    public Product saveProduct(Product product, Company company) {
        product.setCompany(company);
        return productRepository.save(product);
    }
    
    @Override
    @Transactional
    public void deleteProduct(Long id, Company company) {
        Product product = getProductById(id, company);
        
        // Delete all related records first
        inventoryHistoryRepository.deleteByProductId(product.getId());
        transactionRepository.deleteByProductId(product.getId());
        inventoryRepository.deleteByProductId(product.getId());
        
        // Now delete the product
        productRepository.deleteById(product.getId());
    }
}