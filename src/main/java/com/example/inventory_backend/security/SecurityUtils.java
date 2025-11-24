package com.example.inventory_backend.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.inventory_backend.model.Company;
import com.example.inventory_backend.model.User;
import com.example.inventory_backend.repository.CompanyRepository;
import com.example.inventory_backend.repository.UserRepository;

public class SecurityUtils {
    
    public static UserDetailsImpl getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            return (UserDetailsImpl) authentication.getPrincipal();
        }
        throw new RuntimeException("No authenticated user found");
    }
    
    public static Long getCurrentUserId() {
        return getCurrentUserDetails().getId();
    }
    
    public static Long getCurrentCompanyId() {
        return getCurrentUserDetails().getCompanyId();
    }
    
    public static String getCurrentCompanyName() {
        return getCurrentUserDetails().getCompanyName();
    }
}
