package com.example.inventory_backend.service.impl;

import com.example.inventory_backend.model.Company;
import com.example.inventory_backend.model.User;
import com.example.inventory_backend.repository.UserRepository;
import com.example.inventory_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    
    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Override
    public List<User> getAllUsers(Company company) {
        return userRepository.findByCompany(company);
    }
    
    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }
    
    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }
    
    @Override
    public User createUser(User user) {
        return userRepository.save(user);
    }
    
    @Override
    public User updateUser(User user) {
        User existingUser = getUserById(user.getId());
        
        existingUser.setName(user.getName());
        existingUser.setEmail(user.getEmail());
        
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            existingUser.setPassword(user.getPassword());
        }
        
        existingUser.setRole(user.getRole());
        
        return userRepository.save(existingUser);
    }
    
    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
