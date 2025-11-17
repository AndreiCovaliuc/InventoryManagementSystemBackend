package com.example.inventory_backend.repository;

import com.example.inventory_backend.model.Company;
import com.example.inventory_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByCompany(Company company);
    List<User> findByCompanyId(Long companyId);
}
