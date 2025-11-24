package com.example.inventory_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CompanyRegistrationRequest {
    
    // Company fields
    @NotBlank(message = "Company name is required")
    @Size(min = 2, max = 100)
    private String companyName;
    
    @NotBlank(message = "CUI is required")
    @Size(min = 2, max = 50)
    private String cui;
    
    // Admin user fields
    @NotBlank(message = "Admin name is required")
    @Size(min = 3, max = 50)
    private String adminName;
    
    @NotBlank(message = "Admin email is required")
    @Size(max = 50)
    @Email
    private String adminEmail;
    
    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100)
    private String password;
    
    // Getters and setters
    public String getCompanyName() {
        return companyName;
    }
    
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
    
    public String getCui() {
        return cui;
    }
    
    public void setCui(String cui) {
        this.cui = cui;
    }
    
    public String getAdminName() {
        return adminName;
    }
    
    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }
    
    public String getAdminEmail() {
        return adminEmail;
    }
    
    public void setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
}
