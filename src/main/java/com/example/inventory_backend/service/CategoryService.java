package com.example.inventory_backend.service;

import com.example.inventory_backend.model.Category;
import com.example.inventory_backend.model.Company;

import java.util.List;

public interface CategoryService {
    List<Category> getAllCategories(Company company);
    
    Category getCategoryById(Long id, Company company);
    
    Category getCategoryByName(String name, Company company);
    
    Category saveCategory(Category category, Company company);
    
    void deleteCategory(Long id, Company company);
}
