package com.example.inventory_backend.controller;

import com.example.inventory_backend.dto.CategoryDTO;
import com.example.inventory_backend.model.Category;
import com.example.inventory_backend.model.Company;
import com.example.inventory_backend.repository.CompanyRepository;
import com.example.inventory_backend.security.SecurityUtils;
import com.example.inventory_backend.service.CategoryService;
import com.example.inventory_backend.service.NotificationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "http://localhost:3000")
public class CategoryController {

    private final CategoryService categoryService;

    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }
    
    private Company getCurrentCompany() {
        Long companyId = SecurityUtils.getCurrentCompanyId();
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));
    }

    @GetMapping
    public List<CategoryDTO> getAllCategories() {
        Company company = getCurrentCompany();
        List<Category> categories = categoryService.getAllCategories(company);
        return categories.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable Long id) {
        try {
            Company company = getCurrentCompany();
            Category category = categoryService.getCategoryById(id, company);
            return ResponseEntity.ok(convertToDTO(category));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/name/{name}")
    public ResponseEntity<CategoryDTO> getCategoryByName(@PathVariable String name) {
        try {
            Company company = getCurrentCompany();
            Category category = categoryService.getCategoryByName(name, company);
            return ResponseEntity.ok(convertToDTO(category));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<CategoryDTO> createCategory(@RequestBody CategoryDTO categoryDTO) {
        Company company = getCurrentCompany();
        Category category = convertToEntity(categoryDTO);
        Category savedCategory = categoryService.saveCategory(category, company);

        notificationService.notifyNewCategory(savedCategory.getId(), savedCategory.getName(), company);
        
        return new ResponseEntity<>(convertToDTO(savedCategory), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryDTO> updateCategory(@PathVariable Long id, @RequestBody CategoryDTO categoryDTO) {
        try {
            Company company = getCurrentCompany();
            Category existingCategory = categoryService.getCategoryById(id, company);
            
            existingCategory.setName(categoryDTO.getName());
            existingCategory.setDescription(categoryDTO.getDescription());
            
            Category updatedCategory = categoryService.saveCategory(existingCategory, company);
            return ResponseEntity.ok(convertToDTO(updatedCategory));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        try {
            Company company = getCurrentCompany();
            categoryService.deleteCategory(id, company);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    private CategoryDTO convertToDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setProductCount(category.getProducts() != null ? category.getProducts().size() : 0);
        return dto;
    }
    
    private Category convertToEntity(CategoryDTO dto) {
        Category category = new Category();
        category.setId(dto.getId());
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        return category;
    }
}
