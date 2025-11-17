package com.example.inventory_backend.repository;

import com.example.inventory_backend.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByCui(String cui);
    boolean existsByCui(String cui);
}
