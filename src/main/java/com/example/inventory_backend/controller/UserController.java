package com.example.inventory_backend.controller;

import com.example.inventory_backend.dto.UserDTO;
import com.example.inventory_backend.model.Company;
import com.example.inventory_backend.model.User;
import com.example.inventory_backend.security.SecurityUtils;
import com.example.inventory_backend.service.CompanyService;
import com.example.inventory_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Company getCurrentCompany() {
        return companyService.getCompanyById(SecurityUtils.getCurrentCompanyId());
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        Company company = getCurrentCompany();
        List<UserDTO> userDTOs = userService.getAllUsers(company).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userDTOs);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser() {
        User user = userService.getUserById(SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok(convertToDTO(user));
    }

    @PutMapping("/me")
    public ResponseEntity<UserDTO> updateCurrentUser(@RequestBody UserDTO userDTO) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userService.getUserById(userId);

        if (userDTO.getName() != null && !userDTO.getName().trim().isEmpty()) {
            user.setName(userDTO.getName().trim());
        }
        if (userDTO.getEmail() != null && !userDTO.getEmail().trim().isEmpty()) {
            try {
                User existing = userService.getUserByEmail(userDTO.getEmail().trim());
                if (!existing.getId().equals(userId)) {
                    return ResponseEntity.badRequest().build();
                }
            } catch (RuntimeException ignored) {}
            user.setEmail(userDTO.getEmail().trim());
        }

        User updatedUser = userService.createUser(user);
        return ResponseEntity.ok(convertToDTO(updatedUser));
    }

    @PutMapping("/me/password")
    public ResponseEntity<?> updatePassword(@RequestBody Map<String, String> passwordRequest) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userService.getUserById(userId);

        String currentPassword = passwordRequest.get("currentPassword");
        String newPassword = passwordRequest.get("newPassword");

        if (currentPassword == null || newPassword == null) {
            return ResponseEntity.badRequest().body("Current and new password are required");
        }

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return ResponseEntity.badRequest().body("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userService.createUser(user);

        return ResponseEntity.ok().body("Password updated successfully");
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        try {
            Company company = getCurrentCompany();
            User user = userService.getUserById(id);
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
        if (!currentUserId.equals(id)) {
            return ResponseEntity.status(403).build();
        }

        User user = userService.getUserById(id);

        if (userDTO.getName() != null && !userDTO.getName().trim().isEmpty()) {
            user.setName(userDTO.getName().trim());
        }
        if (userDTO.getEmail() != null && !userDTO.getEmail().trim().isEmpty()) {
            try {
                User existing = userService.getUserByEmail(userDTO.getEmail().trim());
                if (!existing.getId().equals(id)) {
                    return ResponseEntity.badRequest().build();
                }
            } catch (RuntimeException ignored) {}
            user.setEmail(userDTO.getEmail().trim());
        }

        User updatedUser = userService.createUser(user);
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
