package com.example.inventory_backend.service;

import com.example.inventory_backend.model.Company;
import com.example.inventory_backend.model.Supplier;

import java.util.List;

public interface SupplierService {
    List<Supplier> getAllSuppliers(Company company);
    
    Supplier getSupplierById(Long id, Company company);
    
    List<Supplier> searchSuppliersByName(String name, Company company);
    
    Supplier saveSupplier(Supplier supplier, Company company);
    
    void deleteSupplier(Long id, Company company);
}
