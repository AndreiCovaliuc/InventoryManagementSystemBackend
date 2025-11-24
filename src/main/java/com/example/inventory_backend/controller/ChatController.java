package com.example.inventory_backend.controller;

import com.example.inventory_backend.dto.*;
import com.example.inventory_backend.model.User;
import com.example.inventory_backend.repository.UserRepository;
import com.example.inventory_backend.security.SecurityUtils;
import com.example.inventory_backend.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chats")
@CrossOrigin(origins = "http://localhost:3000")
public class ChatController {

    @Autowired
    private ChatService chatService;
    
    @Autowired
    private UserRepository userRepository;
    
    private User getCurrentUser() {
        Long userId = SecurityUtils.getCurrentUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAvailableUsers() {
        User currentUser = getCurrentUser();
        
        // Get all users from the same company except the current user
        List<User> companyUsers = userRepository.findByCompanyId(currentUser.getCompany().getId());
        
        List<UserDTO> availableUsers = companyUsers.stream()
                .filter(user -> !user.getId().equals(currentUser.getId()))
                .map(user -> {
                    UserDTO dto = new UserDTO();
                    dto.setId(user.getId());
                    dto.setName(user.getName());
                    dto.setEmail(user.getEmail());
                    dto.setRole(user.getRole().name());
                    return dto;
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(availableUsers);
    }

    @GetMapping
    public ResponseEntity<List<ChatDTO>> getAllUserChats() {
        User currentUser = getCurrentUser();
        List<ChatDTO> chats = chatService.getAllUserChats(currentUser);
        return ResponseEntity.ok(chats);
    }
    
    @GetMapping("/recent")
    public ResponseEntity<List<ChatDTO>> getRecentChats() {
        User currentUser = getCurrentUser();
        List<ChatDTO> chats = chatService.getRecentChats(currentUser);
        return ResponseEntity.ok(chats);
    }
    
    @GetMapping("/{chatId}")
    public ResponseEntity<ChatDTO> getChatById(@PathVariable Long chatId) {
        try {
            User currentUser = getCurrentUser();
            ChatDTO chat = chatService.getChatById(chatId, currentUser);
            return ResponseEntity.ok(chat);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/{chatId}/messages")
    public ResponseEntity<List<ChatMessageDTO>> getChatMessages(@PathVariable Long chatId) {
        try {
            User currentUser = getCurrentUser();
            List<ChatMessageDTO> messages = chatService.getChatMessages(chatId, currentUser);
            return ResponseEntity.ok(messages);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/{chatId}/messages")
    public ResponseEntity<ChatMessageDTO> sendMessage(
            @PathVariable Long chatId, 
            @RequestBody SendMessageRequestDTO request) {
        try {
            User currentUser = getCurrentUser();
            ChatMessageDTO message = chatService.sendMessage(chatId, request.getContent(), currentUser);
            return new ResponseEntity<>(message, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{chatId}/read")
    public ResponseEntity<Void> markChatAsRead(@PathVariable Long chatId) {
        try {
            User currentUser = getCurrentUser();
            chatService.markChatAsRead(chatId, currentUser);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/unread-count")
    public ResponseEntity<Integer> getUnreadCount() {
        User currentUser = getCurrentUser();
        int count = chatService.countUnreadMessages(currentUser);
        return ResponseEntity.ok(count);
    }
    
    @PostMapping
    public ResponseEntity<ChatDTO> createChat(@RequestBody CreateChatRequestDTO request) {
        try {
            User currentUser = getCurrentUser();
            ChatDTO chat = chatService.createNewChat(currentUser, request.getParticipantId());
            return new ResponseEntity<>(chat, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}