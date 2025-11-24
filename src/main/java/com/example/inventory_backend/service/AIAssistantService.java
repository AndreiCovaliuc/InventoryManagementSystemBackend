package com.example.inventory_backend.service;

import com.example.inventory_backend.model.Company;

public interface AIAssistantService {
    String askQuestion(String question, Company company);
}
