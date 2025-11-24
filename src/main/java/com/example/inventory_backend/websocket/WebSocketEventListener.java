package com.example.inventory_backend.websocket;

import com.example.inventory_backend.model.User;
import com.example.inventory_backend.repository.UserRepository;
import com.example.inventory_backend.security.UserDetailsImpl;
import com.example.inventory_backend.service.UserPresenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

@Component
public class WebSocketEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    @Autowired
    private UserPresenceService userPresenceService;

    @Autowired
    private UserRepository userRepository;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = headerAccessor.getUser();

        logger.debug("WebSocket connect event received. Principal: {}", principal);

        if (principal != null) {
            String email = null;

            // Extract email from the authentication token
            if (principal instanceof UsernamePasswordAuthenticationToken) {
                UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) principal;
                Object principalObj = auth.getPrincipal();
                if (principalObj instanceof UserDetailsImpl) {
                    email = ((UserDetailsImpl) principalObj).getEmail();
                } else {
                    email = principal.getName();
                }
            } else {
                email = principal.getName();
            }

            final String userEmail = email;
            if (userEmail != null) {
                userRepository.findByEmail(userEmail).ifPresentOrElse(
                    user -> {
                        logger.info("User connected via WebSocket: {} (ID: {})", user.getName(), user.getId());
                        userPresenceService.userConnected(user);
                    },
                    () -> logger.warn("User not found for email: {}", userEmail)
                );
            }
        } else {
            logger.warn("WebSocket connection without principal (unauthenticated)");
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = headerAccessor.getUser();

        logger.debug("WebSocket disconnect event received. Principal: {}", principal);

        if (principal != null) {
            String email = null;

            // Extract email from the authentication token
            if (principal instanceof UsernamePasswordAuthenticationToken) {
                UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) principal;
                Object principalObj = auth.getPrincipal();
                if (principalObj instanceof UserDetailsImpl) {
                    email = ((UserDetailsImpl) principalObj).getEmail();
                } else {
                    email = principal.getName();
                }
            } else {
                email = principal.getName();
            }

            final String userEmail = email;
            if (userEmail != null) {
                userRepository.findByEmail(userEmail).ifPresentOrElse(
                    user -> {
                        logger.info("User disconnected from WebSocket: {} (ID: {})", user.getName(), user.getId());
                        userPresenceService.userDisconnected(user);
                    },
                    () -> logger.warn("User not found for email: {}", userEmail)
                );
            }
        }
    }
}
