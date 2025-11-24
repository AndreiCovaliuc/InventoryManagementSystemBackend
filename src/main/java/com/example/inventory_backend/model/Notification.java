package com.example.inventory_backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;
    private String message;
    private String type;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    private String entityType;
    private Long entityId;
    
    private String iconName;
    private String iconColor;
    private String avatarColor;
    private String bgColor;
    private String borderColor;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;
    
    private boolean read;
}
