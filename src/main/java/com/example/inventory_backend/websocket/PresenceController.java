package com.example.inventory_backend.websocket;

import com.example.inventory_backend.repository.UserRepository;
import com.example.inventory_backend.service.UserPresenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class PresenceController {

    @Autowired
    private UserPresenceService userPresenceService;

    @Autowired
    private UserRepository userRepository;

    @MessageMapping("/presence/heartbeat")
    public void heartbeat(Principal principal) {
        if (principal != null) {
            String email = principal.getName();
            userRepository.findByEmail(email).ifPresent(userPresenceService::updateHeartbeat);
        }
    }
}
