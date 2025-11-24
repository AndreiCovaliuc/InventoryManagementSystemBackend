package com.example.inventory_backend.service.impl;

import com.example.inventory_backend.model.Company;
import com.example.inventory_backend.model.Supplier;
import com.example.inventory_backend.repository.SupplierRepository;
import com.example.inventory_backend.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;
    
    @Autowired
    public SupplierServiceImpl(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }
    
    @Override
    public List<Supplier> getAllSuppliers(Company company) {
        return supplierRepository.findByCompany(company);
    }
    
    @Override
    public Supplier getSupplierById(Long id, Company company) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found with id: " + id));
        
        if (!supplier.getCompany().getId().equals(company.getId())) {
            throw new RuntimeException("Supplier not found with id: " + id);
        }
        
        return supplier;
    }
    
    @Override
    public List<Supplier> searchSuppliersByName(String name, Company company) {
        return supplierRepository.findByNameContainingIgnoreCaseAndCompany(name, company);
    }
    
    @Override
    public Supplier saveSupplier(Supplier supplier, Company company) {
        supplier.setCompany(company);
        return supplierRepository.save(supplier);
    }
    
    @Override
    public void deleteSupplier(Long id, Company company) {
        Supplier supplier = getSupplierById(id, company);
        supplierRepository.deleteById(supplier.getId());
    }
}
