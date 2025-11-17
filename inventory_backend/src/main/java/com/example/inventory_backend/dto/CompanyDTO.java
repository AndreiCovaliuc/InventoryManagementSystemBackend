package com.example.inventory_backend.dto;

import java.time.LocalDateTime;

public class CompanyDTO {
    private Long id;
    private String name;
    private String cui;
    private LocalDateTime createdAt;
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getCui() {
        return cui;
    }
    
    public void setCui(String cui) {
        this.cui = cui;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
