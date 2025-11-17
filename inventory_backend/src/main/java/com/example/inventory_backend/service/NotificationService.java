package com.example.inventory_backend.service;

import com.example.inventory_backend.dto.NotificationDTO;
import com.example.inventory_backend.model.Company;

import java.util.List;

public interface NotificationService {
    List<NotificationDTO> getUserNotifications(Long userId);
    List<NotificationDTO> getUnreadNotifications(Long userId);
    int countUnreadNotifications(Long userId);
    void markAsRead(Long notificationId);
    void markAllAsRead(Long userId);
    void deleteNotification(Long notificationId);
    
    // Methods for creating new notifications - now with Company parameter
    void notifyNewProduct(Long productId, String productName, Company company);
    void notifyNewCategory(Long categoryId, String categoryName, Company company);
    void notifyNewSupplier(Long supplierId, String supplierName, Company company);
    void notifyNewUser(Long userId, String userName, String role, Company company);
}
