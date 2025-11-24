package com.example.inventory_backend.controller;

import com.example.inventory_backend.dto.UserDTO;
import com.example.inventory_backend.model.Company;
import com.example.inventory_backend.model.User;
import com.example.inventory_backend.repository.CompanyRepository;
import com.example.inventory_backend.repository.UserRepository;
import com.example.inventory_backend.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    private Company getCurrentCompany() {
        Long companyId = SecurityUtils.getCurrentCompanyId();
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        Company company = getCurrentCompany();
        List<User> users = userRepository.findByCompany(company);
        
        List<UserDTO> userDTOs = users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(userDTOs);
    }
    
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser() {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(convertToDTO(user));
    }

    @PutMapping("/me")
    public ResponseEntity<UserDTO> updateCurrentUser(@RequestBody UserDTO userDTO) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update allowed fields
        if (userDTO.getName() != null && !userDTO.getName().trim().isEmpty()) {
            user.setName(userDTO.getName().trim());
        }
        if (userDTO.getEmail() != null && !userDTO.getEmail().trim().isEmpty()) {
            // Check if email is already taken by another user
            User existingUser = userRepository.findByEmail(userDTO.getEmail().trim()).orElse(null);
            if (existingUser != null && !existingUser.getId().equals(userId)) {
                return ResponseEntity.badRequest().build();
            }
            user.setEmail(userDTO.getEmail().trim());
        }

        User updatedUser = userRepository.save(user);
        return ResponseEntity.ok(convertToDTO(updatedUser));
    }

    @PutMapping("/me/password")
    public ResponseEntity<?> updatePassword(@RequestBody Map<String, String> passwordRequest) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String currentPassword = passwordRequest.get("currentPassword");
        String newPassword = passwordRequest.get("newPassword");

        if (currentPassword == null || newPassword == null) {
            return ResponseEntity.badRequest().body("Current and new password are required");
        }

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return ResponseEntity.badRequest().body("Current password is incorrect");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return ResponseEntity.ok().body("Password updated successfully");
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        try {
            Company company = getCurrentCompany();
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Verify user is from same company
            if (!user.getCompany().getId().equals(company.getId())) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(convertToDTO(user));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUserById(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        Long currentUserId = SecurityUtils.getCurrentUserId();

        // Users can only update their own profile (unless admin - handled by security config)
        if (!currentUserId.equals(id)) {
            return ResponseEntity.status(403).build();
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update allowed fields
        if (userDTO.getName() != null && !userDTO.getName().trim().isEmpty()) {
            user.setName(userDTO.getName().trim());
        }
        if (userDTO.getEmail() != null && !userDTO.getEmail().trim().isEmpty()) {
            // Check if email is already taken by another user
            User existingUser = userRepository.findByEmail(userDTO.getEmail().trim()).orElse(null);
            if (existingUser != null && !existingUser.getId().equals(id)) {
                return ResponseEntity.badRequest().build();
            }
            user.setEmail(userDTO.getEmail().trim());
        }

        User updatedUser = userRepository.save(user);
        return ResponseEntity.ok(convertToDTO(updatedUser));
    }
    
    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole().name());
        return dto;
    }
}
