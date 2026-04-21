package com.example.inventory_backend.service;

import com.example.inventory_backend.model.Company;

public interface CompanyService {
    Company getCompanyById(Long id);
    Company saveCompany(Company company);
    boolean existsByCui(String cui);
}
