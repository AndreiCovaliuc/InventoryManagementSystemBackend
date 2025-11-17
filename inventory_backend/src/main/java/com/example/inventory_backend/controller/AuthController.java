package com.example.inventory_backend.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.inventory_backend.dto.CompanyRegistrationRequest;
import com.example.inventory_backend.dto.JwtResponse;
import com.example.inventory_backend.dto.LoginRequest;
import com.example.inventory_backend.dto.MessageResponse;
import com.example.inventory_backend.model.Company;
import com.example.inventory_backend.model.User;
import com.example.inventory_backend.model.User.Role;
import com.example.inventory_backend.repository.CompanyRepository;
import com.example.inventory_backend.repository.UserRepository;
import com.example.inventory_backend.security.JwtUtils;
import com.example.inventory_backend.security.UserDetailsImpl;

import jakarta.validation.Valid;

@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600, allowCredentials = "true")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;
    
    @Autowired
    CompanyRepository companyRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(), 
                userDetails.getEmail(),
                roles,
                userDetails.getCompanyId(),
                userDetails.getCompanyName()));
    }
    
    @PostMapping("/register-company")
    public ResponseEntity<?> registerCompany(@Valid @RequestBody CompanyRegistrationRequest request) {
        logger.info("Company registration attempt: {} (CUI: {})", request.getCompanyName(), request.getCui());
        
        // Check if CUI already exists
        if (companyRepository.existsByCui(request.getCui())) {
            logger.warn("Registration failed: CUI already in use - {}", request.getCui());
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: A company with this CUI already exists!"));
        }
        
        // Check if admin email already exists
        if (userRepository.findByEmail(request.getAdminEmail()).isPresent()) {
            logger.warn("Registration failed: Email already in use - {}", request.getAdminEmail());
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }
        
        // Create the company
        Company company = new Company();
        company.setName(request.getCompanyName());
        company.setCui(request.getCui());
        Company savedCompany = companyRepository.save(company);
        logger.info("Company created: {} (ID: {})", savedCompany.getName(), savedCompany.getId());
        
        // Create the admin user for this company
        User adminUser = new User();
        adminUser.setName(request.getAdminName());
        adminUser.setEmail(request.getAdminEmail());
        adminUser.setPassword(encoder.encode(request.getPassword()));
        adminUser.setRole(Role.ADMIN);
        adminUser.setCompany(savedCompany);
        
        userRepository.save(adminUser);
        logger.info("Admin user created for company {}: {}", savedCompany.getName(), adminUser.getEmail());
        
        return ResponseEntity.ok(new MessageResponse("Company registered successfully! You can now login with your admin credentials."));
    }
}
