package com.example.inventory_backend.service;

import com.example.inventory_backend.model.User;
import com.example.inventory_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserPresenceService {

    // Track online users by their ID
    private final Set<Long> onlineUsers = ConcurrentHashMap.newKeySet();

    // Track users by company for broadcasting
    private final Map<Long, Set<Long>> companyUsers = new ConcurrentHashMap<>();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void userConnected(User user) {
        Long userId = user.getId();
        Long companyId = user.getCompany().getId();

        // Add to online users
        onlineUsers.add(userId);

        // Add to company users map
        companyUsers.computeIfAbsent(companyId, k -> ConcurrentHashMap.newKeySet()).add(userId);

        // Update last seen
        user.setLastSeen(LocalDateTime.now());
        userRepository.save(user);

        // Broadcast to company members
        broadcastPresenceUpdate(companyId, userId, true);
    }

    public void userDisconnected(User user) {
        Long userId = user.getId();
        Long companyId = user.getCompany().getId();

        // Remove from online users
        onlineUsers.remove(userId);

        // Remove from company users
        Set<Long> users = companyUsers.get(companyId);
        if (users != null) {
            users.remove(userId);
        }

        // Update last seen
        user.setLastSeen(LocalDateTime.now());
        userRepository.save(user);

        // Broadcast to company members
        broadcastPresenceUpdate(companyId, userId, false);
    }

    public boolean isUserOnline(Long userId) {
        return onlineUsers.contains(userId);
    }

    public Set<Long> getOnlineUsersForCompany(Long companyId) {
        return companyUsers.getOrDefault(companyId, Set.of());
    }

    public LocalDateTime getLastSeen(Long userId) {
        return userRepository.findById(userId)
                .map(User::getLastSeen)
                .orElse(null);
    }

    private void broadcastPresenceUpdate(Long companyId, Long userId, boolean online) {
        // Create presence update message
        Map<String, Object> presenceUpdate = Map.of(
            "userId", userId,
            "online", online,
            "timestamp", LocalDateTime.now().toString()
        );

        // Broadcast to all users in the same company
        messagingTemplate.convertAndSend("/topic/presence/" + companyId, presenceUpdate);
    }

    public void updateHeartbeat(User user) {
        user.setLastSeen(LocalDateTime.now());
        userRepository.save(user);
    }
}
