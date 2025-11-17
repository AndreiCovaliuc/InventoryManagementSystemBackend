package com.example.inventory_backend.service.impl;

import com.example.inventory_backend.model.Category;
import com.example.inventory_backend.model.Company;
import com.example.inventory_backend.repository.CategoryRepository;
import com.example.inventory_backend.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    
    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }
    
    @Override
    public List<Category> getAllCategories(Company company) {
        return categoryRepository.findByCompany(company);
    }
    
    @Override
    public Category getCategoryById(Long id, Company company) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        
        if (!category.getCompany().getId().equals(company.getId())) {
            throw new RuntimeException("Category not found with id: " + id);
        }
        
        return category;
    }
    
    @Override
    public Category getCategoryByName(String name, Company company) {
        return categoryRepository.findByNameAndCompany(name, company)
                .orElseThrow(() -> new RuntimeException("Category not found with name: " + name));
    }
    
    @Override
    public Category saveCategory(Category category, Company company) {
        category.setCompany(company);
        return categoryRepository.save(category);
    }
    
    @Override
    public void deleteCategory(Long id, Company company) {
        Category category = getCategoryById(id, company);
        categoryRepository.deleteById(category.getId());
    }
}
