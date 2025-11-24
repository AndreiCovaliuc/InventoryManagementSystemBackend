package com.example.inventory_backend.repository;

import com.example.inventory_backend.model.Company;
import com.example.inventory_backend.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    List<Supplier> findByNameContainingIgnoreCase(String name);
    List<Supplier> findByNameContainingIgnoreCaseAndCompany(String name, Company company);
    List<Supplier> findByCompany(Company company);
    List<Supplier> findByCompanyId(Long companyId);
}
