package com.example.inventory_backend.repository;

import com.example.inventory_backend.model.Category;
import com.example.inventory_backend.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);
    Optional<Category> findByNameAndCompany(String name, Company company);
    List<Category> findByCompany(Company company);
    List<Category> findByCompanyId(Long companyId);
}
