package com.example.inventory_backend.controller;

import com.example.inventory_backend.dto.AIQueryRequest;
import com.example.inventory_backend.dto.AIQueryResponse;
import com.example.inventory_backend.model.User;
import com.example.inventory_backend.repository.UserRepository;
import com.example.inventory_backend.security.SecurityUtils;
import com.example.inventory_backend.service.AIAssistantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "http://localhost:3000")
public class AIAssistantController {

    @Autowired
    private AIAssistantService aiAssistantService;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        Long userId = SecurityUtils.getCurrentUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @PostMapping("/ask")
    public ResponseEntity<AIQueryResponse> askQuestion(@RequestBody AIQueryRequest request) {
        try {
            User currentUser = getCurrentUser();

            if (request.getQuestion() == null || request.getQuestion().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            String answer = aiAssistantService.askQuestion(
                    request.getQuestion().trim(),
                    currentUser.getCompany()
            );

            AIQueryResponse response = new AIQueryResponse(request.getQuestion(), answer);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            AIQueryResponse errorResponse = new AIQueryResponse(
                    request.getQuestion(),
                    "I'm sorry, I encountered an error processing your question. Please make sure Ollama is running and try again. Error: " + e.getMessage()
            );
            return ResponseEntity.ok(errorResponse);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("AI Assistant is available");
    }
}
