package com.example.inventory_backend.controller;

import com.example.inventory_backend.dto.InventoryDTO;
import com.example.inventory_backend.model.Company;
import com.example.inventory_backend.model.Inventory;
import com.example.inventory_backend.model.Product;
import com.example.inventory_backend.repository.CompanyRepository;
import com.example.inventory_backend.security.SecurityUtils;
import com.example.inventory_backend.service.EntityBroadcastService;
import com.example.inventory_backend.service.InventoryService;
import com.example.inventory_backend.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = "http://localhost:3000")
public class InventoryController {

    private final InventoryService inventoryService;
    private final ProductService productService;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private EntityBroadcastService broadcastService;

    @Autowired
    public InventoryController(InventoryService inventoryService, ProductService productService) {
        this.inventoryService = inventoryService;
        this.productService = productService;
    }
    
    private Company getCurrentCompany() {
        Long companyId = SecurityUtils.getCurrentCompanyId();
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));
    }

    @GetMapping
    public List<InventoryDTO> getAllInventory() {
        Company company = getCurrentCompany();
        List<Inventory> inventoryList = inventoryService.getAllInventory(company);
        return inventoryList.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryDTO> getInventoryById(@PathVariable Long id) {
        try {
            Company company = getCurrentCompany();
            Inventory inventory = inventoryService.getInventoryById(id, company);
            return ResponseEntity.ok(convertToDTO(inventory));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/product/{productId}")
    public ResponseEntity<InventoryDTO> getInventoryByProduct(@PathVariable Long productId) {
        try {
            Company company = getCurrentCompany();
            Product product = productService.getProductById(productId, company);
            Inventory inventory = inventoryService.getInventoryByProduct(product, company);
            return ResponseEntity.ok(convertToDTO(inventory));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/low-stock")
    public List<InventoryDTO> getLowStockItems() {
        Company company = getCurrentCompany();
        List<Inventory> lowStockItems = inventoryService.getLowStockItems(company);
        return lowStockItems.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    
    @GetMapping("/check-stock")
    public ResponseEntity<Boolean> checkStock(@RequestParam Long productId, @RequestParam Integer quantity) {
        Company company = getCurrentCompany();
        boolean inStock = inventoryService.isInStock(productId, quantity, company);
        return ResponseEntity.ok(inStock);
    }

    @PostMapping
    public ResponseEntity<InventoryDTO> createInventory(@RequestBody InventoryDTO inventoryDTO) {
        try {
            Company company = getCurrentCompany();
            Inventory inventory = convertToEntity(inventoryDTO, company);
            Inventory savedInventory = inventoryService.saveInventory(inventory, company);
            InventoryDTO savedDTO = convertToDTO(savedInventory);
            broadcastService.broadcastCreate(company.getId(), "INVENTORY", savedDTO);
            return new ResponseEntity<>(savedDTO, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<InventoryDTO> updateInventory(@PathVariable Long id, @RequestBody InventoryDTO inventoryDTO) {
        try {
            Company company = getCurrentCompany();
            Inventory existingInventory = inventoryService.getInventoryById(id, company);

            existingInventory.setQuantity(inventoryDTO.getQuantity());
            existingInventory.setReorderLevel(inventoryDTO.getReorderLevel());
            existingInventory.setReorderQuantity(inventoryDTO.getReorderQuantity());
            existingInventory.setLocation(inventoryDTO.getLocation());

            Inventory updatedInventory = inventoryService.saveInventory(existingInventory, company);
            InventoryDTO updatedDTO = convertToDTO(updatedInventory);
            broadcastService.broadcastUpdate(company.getId(), "INVENTORY", updatedDTO);
            return ResponseEntity.ok(updatedDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/update-quantity/{productId}")
    public ResponseEntity<InventoryDTO> updateQuantity(
            @PathVariable Long productId,
            @RequestBody Map<String, Integer> request) {
        try {
            Company company = getCurrentCompany();
            Integer quantityChange = request.get("quantityChange");
            if (quantityChange == null) {
                return ResponseEntity.badRequest().build();
            }
            Inventory updatedInventory = inventoryService.updateQuantity(productId, quantityChange, company);
            InventoryDTO updatedDTO = convertToDTO(updatedInventory);
            broadcastService.broadcastUpdate(company.getId(), "INVENTORY", updatedDTO);
            return ResponseEntity.ok(updatedDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInventory(@PathVariable Long id) {
        try {
            Company company = getCurrentCompany();
            inventoryService.deleteInventory(id, company);
            broadcastService.broadcastDelete(company.getId(), "INVENTORY", id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    private InventoryDTO convertToDTO(Inventory inventory) {
        InventoryDTO dto = new InventoryDTO();
        dto.setId(inventory.getId());
        dto.setQuantity(inventory.getQuantity());
        dto.setReorderLevel(inventory.getReorderLevel());
        dto.setReorderQuantity(inventory.getReorderQuantity());
        dto.setLocation(inventory.getLocation());
        dto.setLastUpdated(inventory.getLastUpdated());
        
        if (inventory.getProduct() != null) {
            dto.setProductId(inventory.getProduct().getId());
            dto.setProductName(inventory.getProduct().getName());
            dto.setProductSku(inventory.getProduct().getSku());
            
            if (inventory.getProduct().getCategory() != null) {
                dto.setProductCategoryName(inventory.getProduct().getCategory().getName());
            }
            
            if (inventory.getProduct().getSupplier() != null) {
                dto.setProductSupplierName(inventory.getProduct().getSupplier().getName());
            }
        }
        
        return dto;
    }
    
    private Inventory convertToEntity(InventoryDTO dto, Company company) {
        Inventory inventory = new Inventory();
        inventory.setId(dto.getId());
        inventory.setQuantity(dto.getQuantity());
        inventory.setReorderLevel(dto.getReorderLevel());
        inventory.setReorderQuantity(dto.getReorderQuantity());
        inventory.setLocation(dto.getLocation());
        inventory.setCompany(company);
        
        if (dto.getProductId() != null) {
            Product product = productService.getProductById(dto.getProductId(), company);
            inventory.setProduct(product);
        }
        
        return inventory;
    }
}
