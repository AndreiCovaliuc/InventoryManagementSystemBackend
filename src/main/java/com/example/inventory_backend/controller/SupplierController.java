package com.example.inventory_backend.controller;

import com.example.inventory_backend.dto.SupplierDTO;
import com.example.inventory_backend.model.Company;
import com.example.inventory_backend.model.Supplier;
import com.example.inventory_backend.repository.CompanyRepository;
import com.example.inventory_backend.security.SecurityUtils;
import com.example.inventory_backend.service.EntityBroadcastService;
import com.example.inventory_backend.service.NotificationService;
import com.example.inventory_backend.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/suppliers")
@CrossOrigin(origins = "http://localhost:3000")
public class SupplierController {

    private final SupplierService supplierService;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private EntityBroadcastService broadcastService;
    
    private Company getCurrentCompany() {
        Long companyId = SecurityUtils.getCurrentCompanyId();
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));
    }

    @GetMapping
    public List<SupplierDTO> getAllSuppliers() {
        Company company = getCurrentCompany();
        List<Supplier> suppliers = supplierService.getAllSuppliers(company);
        return suppliers.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupplierDTO> getSupplierById(@PathVariable Long id) {
        try {
            Company company = getCurrentCompany();
            Supplier supplier = supplierService.getSupplierById(id, company);
            return ResponseEntity.ok(convertToDTO(supplier));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/search")
    public List<SupplierDTO> searchSuppliers(@RequestParam String name) {
        Company company = getCurrentCompany();
        return supplierService.searchSuppliersByName(name, company)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<SupplierDTO> createSupplier(@RequestBody SupplierDTO supplierDTO) {
        Company company = getCurrentCompany();
        Supplier supplier = convertToEntity(supplierDTO);
        Supplier savedSupplier = supplierService.saveSupplier(supplier, company);

        notificationService.notifyNewSupplier(savedSupplier.getId(), savedSupplier.getName(), company);

        SupplierDTO savedDTO = convertToDTO(savedSupplier);
        broadcastService.broadcastCreate(company.getId(), "SUPPLIER", savedDTO);

        return new ResponseEntity<>(savedDTO, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SupplierDTO> updateSupplier(@PathVariable Long id, @RequestBody SupplierDTO supplierDTO) {
        try {
            Company company = getCurrentCompany();
            Supplier existingSupplier = supplierService.getSupplierById(id, company);

            existingSupplier.setName(supplierDTO.getName());
            existingSupplier.setContactName(supplierDTO.getContactName());
            existingSupplier.setEmail(supplierDTO.getEmail());
            existingSupplier.setPhone(supplierDTO.getPhone());
            existingSupplier.setAddress(supplierDTO.getAddress());

            Supplier updatedSupplier = supplierService.saveSupplier(existingSupplier, company);
            SupplierDTO updatedDTO = convertToDTO(updatedSupplier);
            broadcastService.broadcastUpdate(company.getId(), "SUPPLIER", updatedDTO);
            return ResponseEntity.ok(updatedDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSupplier(@PathVariable Long id) {
        try {
            Company company = getCurrentCompany();
            supplierService.deleteSupplier(id, company);
            broadcastService.broadcastDelete(company.getId(), "SUPPLIER", id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    private SupplierDTO convertToDTO(Supplier supplier) {
        SupplierDTO dto = new SupplierDTO();
        dto.setId(supplier.getId());
        dto.setName(supplier.getName());
        dto.setContactName(supplier.getContactName());
        dto.setEmail(supplier.getEmail());
        dto.setPhone(supplier.getPhone());
        dto.setAddress(supplier.getAddress());
        dto.setProductCount(supplier.getProducts() != null ? supplier.getProducts().size() : 0);
        return dto;
    }
    
    private Supplier convertToEntity(SupplierDTO dto) {
        Supplier supplier = new Supplier();
        supplier.setId(dto.getId());
        supplier.setName(dto.getName());
        supplier.setContactName(dto.getContactName());
        supplier.setEmail(dto.getEmail());
        supplier.setPhone(dto.getPhone());
        supplier.setAddress(dto.getAddress());
        return supplier;
    }
}
