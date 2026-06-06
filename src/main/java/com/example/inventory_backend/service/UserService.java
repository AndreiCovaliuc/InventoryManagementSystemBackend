package com.example.inventory_backend.service;

import com.example.inventory_backend.model.Company;
import com.example.inventory_backend.model.User;

import java.util.List;

public interface UserService {
    List<User> getAllUsers(Company company);
    List<User> getUsersByCompanyId(Long companyId);
    User getUserById(Long id);
    User getUserByEmail(String email);
    boolean existsByEmail(String email);
    User createUser(User user);
    User updateUser(User user);
    void deleteUser(Long id);
    long countAdmins(Company company);
}
