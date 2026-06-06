package com.example.inventory_backend.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.example.inventory_backend.dto.UserDTO;
import com.example.inventory_backend.model.Company;
import com.example.inventory_backend.model.User;
import com.example.inventory_backend.security.SecurityUtils;
import com.example.inventory_backend.service.CompanyService;
import com.example.inventory_backend.service.NotificationService;
import com.example.inventory_backend.service.UserService;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    @Autowired
    private UserService userService;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private NotificationService notificationService;

    private Company getCurrentCompany() {
        return companyService.getCompanyById(SecurityUtils.getCurrentCompanyId());
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        Company company = getCurrentCompany();
        List<User> users = userService.getAllUsers(company);
        
        List<UserDTO> userDTOs = users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(userDTOs);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            Company company = getCurrentCompany();
            User user = userService.getUserById(id);
            
            // Verify user belongs to same company
            if (!user.getCompany().getId().equals(company.getId())) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(convertToDTO(user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error retrieving user: " + e.getMessage());
        }
    }
    
    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        return ResponseEntity.ok("Admin User controller is accessible");
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        try {
            Company company = getCurrentCompany();
            User existingUser = userService.getUserById(id);
            
            // Verify user belongs to same company
            if (!existingUser.getCompany().getId().equals(company.getId())) {
                return ResponseEntity.notFound().build();
            }
            
            existingUser.setName(userDTO.getName());
            existingUser.setEmail(userDTO.getEmail());

            if (userDTO.getRole() != null && !userDTO.getRole().isEmpty()) {
                User.Role newRole = User.Role.valueOf(userDTO.getRole());
                if (existingUser.getRole() == User.Role.ADMIN && newRole != User.Role.ADMIN
                        && userService.countAdmins(company) <= 1) {
                    return ResponseEntity.badRequest().body(
                            "At least one administrator is required. Promote another user to admin before demoting this one.");
                }
                existingUser.setRole(newRole);
            }
            
            if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
                existingUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
            }
            
            User updatedUser = userService.createUser(existingUser);

            return ResponseEntity.ok(convertToDTO(updatedUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating user: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            Company company = getCurrentCompany();
            User user = userService.getUserById(id);
            
            // Verify user belongs to same company
            if (!user.getCompany().getId().equals(company.getId())) {
                return ResponseEntity.notFound().build();
            }
            
            // Prevent deleting yourself
            if (user.getId().equals(SecurityUtils.getCurrentUserId())) {
                return ResponseEntity.badRequest().body("Cannot delete your own account");
            }
            
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting user: " + e.getMessage());
        }
    }
    
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody UserDTO userDTO) {
        try {
            Company company = getCurrentCompany();
            
            // Check if email already exists
            if (userService.existsByEmail(userDTO.getEmail())) {
                return ResponseEntity.badRequest().body("Email already in use");
            }
            
            User user = new User();
            user.setName(userDTO.getName());
            user.setEmail(userDTO.getEmail());
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
            
            if (userDTO.getRole() != null && !userDTO.getRole().isEmpty()) {
                user.setRole(User.Role.valueOf(userDTO.getRole()));
            } else {
                user.setRole(User.Role.EMPLOYEE);
            }
            
            // CRUCIAL: Assign the new user to the admin's company
            user.setCompany(company);
            
            User savedUser = userService.createUser(user);

            notificationService.notifyNewUser(savedUser.getId(), savedUser.getName(), savedUser.getRole().name(), company);
            
            return ResponseEntity.ok(convertToDTO(savedUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating user: " + e.getMessage());
        }
    }
    
    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole().name());
        // Don't return password
        return dto;
    }
}
