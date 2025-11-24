package com.example.inventory_backend.controller;

import com.example.inventory_backend.model.User;
import com.example.inventory_backend.repository.UserRepository;
import com.example.inventory_backend.security.UserDetailsImpl;
import com.example.inventory_backend.service.UserPresenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/presence")
public class PresenceRestController {

    @Autowired
    private UserPresenceService userPresenceService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/online")
    public ResponseEntity<?> getOnlineUsers(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User currentUser = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Set<Long> onlineUsers = userPresenceService.getOnlineUsersForCompany(currentUser.getCompany().getId());

        return ResponseEntity.ok(onlineUsers);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserPresence(@PathVariable Long userId, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User currentUser = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Target user not found"));

        // Verify same company
        if (!currentUser.getCompany().getId().equals(targetUser.getCompany().getId())) {
            return ResponseEntity.badRequest().body("Cannot view presence of user from different company");
        }

        Map<String, Object> presence = new HashMap<>();
        presence.put("userId", userId);
        presence.put("online", userPresenceService.isUserOnline(userId));
        presence.put("lastSeen", targetUser.getLastSeen());

        return ResponseEntity.ok(presence);
    }

    @PostMapping("/heartbeat")
    public ResponseEntity<?> heartbeat(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User currentUser = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        userPresenceService.updateHeartbeat(currentUser);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "ok");
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(response);
    }
}
