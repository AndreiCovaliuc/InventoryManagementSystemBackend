package com.example.inventory_backend.controller;

import com.example.inventory_backend.dto.ProductDTO;
import com.example.inventory_backend.model.Category;
import com.example.inventory_backend.model.Company;
import com.example.inventory_backend.model.Product;
import com.example.inventory_backend.model.Supplier;
import com.example.inventory_backend.repository.CompanyRepository;
import com.example.inventory_backend.security.SecurityUtils;
import com.example.inventory_backend.service.CategoryService;
import com.example.inventory_backend.service.NotificationService;
import com.example.inventory_backend.service.ProductService;
import com.example.inventory_backend.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final SupplierService supplierService;
    
    @Autowired
    private CompanyRepository companyRepository;
    
    @Autowired
    private NotificationService notificationService;

    @Autowired
    public ProductController(ProductService productService, CategoryService categoryService, SupplierService supplierService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.supplierService = supplierService;
    }
    
    private Company getCurrentCompany() {
        Long companyId = SecurityUtils.getCurrentCompanyId();
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));
    }
    
    @GetMapping
    public List<ProductDTO> getAllProducts() {
        Company company = getCurrentCompany();
        List<Product> products = productService.getAllProducts(company);
        return products.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        try {
            Company company = getCurrentCompany();
            Product product = productService.getProductById(id, company);
            return ResponseEntity.ok(convertToDTO(product));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/sku/{sku}")
    public ResponseEntity<ProductDTO> getProductBySku(@PathVariable String sku) {
        try {
            Company company = getCurrentCompany();
            Product product = productService.getProductBySku(sku, company);
            return ResponseEntity.ok(convertToDTO(product));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/category/{categoryId}")
    public List<ProductDTO> getProductsByCategory(@PathVariable Long categoryId) {
        Company company = getCurrentCompany();
        Category category = categoryService.getCategoryById(categoryId, company);
        return productService.getProductsByCategory(category, company)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @GetMapping("/supplier/{supplierId}")
    public List<ProductDTO> getProductsBySupplier(@PathVariable Long supplierId) {
        Company company = getCurrentCompany();
        Supplier supplier = supplierService.getSupplierById(supplierId, company);
        return productService.getProductsBySupplier(supplier, company)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @GetMapping("/search")
    public List<ProductDTO> searchProducts(@RequestParam String name) {
        Company company = getCurrentCompany();
        return productService.searchProductsByName(name, company)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @GetMapping("/price")
    public List<ProductDTO> getProductsByMaxPrice(@RequestParam BigDecimal maxPrice) {
        Company company = getCurrentCompany();
        return productService.getProductsByMaxPrice(maxPrice, company)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@RequestBody ProductDTO productDTO) {
        Company company = getCurrentCompany();
        Product product = convertToEntity(productDTO, company);
        Product savedProduct = productService.saveProduct(product, company);

        notificationService.notifyNewProduct(savedProduct.getId(), savedProduct.getName(), company);
        
        return new ResponseEntity<>(convertToDTO(savedProduct), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id, @RequestBody ProductDTO productDTO) {
        try {
            Company company = getCurrentCompany();
            Product existingProduct = productService.getProductById(id, company);
            
            existingProduct.setName(productDTO.getName());
            existingProduct.setDescription(productDTO.getDescription());
            existingProduct.setPrice(productDTO.getPrice());
            existingProduct.setSku(productDTO.getSku());
            
            if (productDTO.getCategoryId() != null) {
                Category category = categoryService.getCategoryById(productDTO.getCategoryId(), company);
                existingProduct.setCategory(category);
            } else {
                existingProduct.setCategory(null);
            }
            
            if (productDTO.getSupplierId() != null) {
                Supplier supplier = supplierService.getSupplierById(productDTO.getSupplierId(), company);
                existingProduct.setSupplier(supplier);
            } else {
                existingProduct.setSupplier(null);
            }
            
            Product updatedProduct = productService.saveProduct(existingProduct, company);
            return ResponseEntity.ok(convertToDTO(updatedProduct));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        try {
            Company company = getCurrentCompany();
            productService.deleteProduct(id, company);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setSku(product.getSku());
        dto.setPrice(product.getPrice());
        
        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }
        
        if (product.getSupplier() != null) {
            dto.setSupplierId(product.getSupplier().getId());
            dto.setSupplierName(product.getSupplier().getName());
        }
        
        return dto;
    }
    
    private Product convertToEntity(ProductDTO dto, Company company) {
        Product product = new Product();
        product.setId(dto.getId());
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setSku(dto.getSku());
        product.setPrice(dto.getPrice());
        product.setCompany(company);
        
        if (dto.getCategoryId() != null) {
            try {
                Category category = categoryService.getCategoryById(dto.getCategoryId(), company);
                product.setCategory(category);
            } catch (RuntimeException e) {
                // Category not found
            }
        }
        
        if (dto.getSupplierId() != null) {
            try {
                Supplier supplier = supplierService.getSupplierById(dto.getSupplierId(), company);
                product.setSupplier(supplier);
            } catch (RuntimeException e) {
                // Supplier not found
            }
        }
        
        return product;
    }
}
