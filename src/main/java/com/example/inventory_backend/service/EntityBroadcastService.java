package com.example.inventory_backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class EntityBroadcastService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void broadcastCreate(Long companyId, String entityType, Object entity) {
        broadcast(companyId, entityType, "CREATE", entity);
    }

    public void broadcastUpdate(Long companyId, String entityType, Object entity) {
        broadcast(companyId, entityType, "UPDATE", entity);
    }

    public void broadcastDelete(Long companyId, String entityType, Long entityId) {
        Map<String, Object> deleteInfo = new HashMap<>();
        deleteInfo.put("id", entityId);
        broadcast(companyId, entityType, "DELETE", deleteInfo);
    }

    private void broadcast(Long companyId, String entityType, String action, Object data) {
        Map<String, Object> message = new HashMap<>();
        message.put("entityType", entityType);
        message.put("action", action);
        message.put("data", data);
        message.put("timestamp", LocalDateTime.now().toString());

        messagingTemplate.convertAndSend("/topic/updates/" + companyId, message);
    }
}
