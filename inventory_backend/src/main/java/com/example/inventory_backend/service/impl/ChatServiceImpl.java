package com.example.inventory_backend.service.impl;

import com.example.inventory_backend.dto.ChatDTO;
import com.example.inventory_backend.dto.ChatMessageDTO;
import com.example.inventory_backend.dto.UserDTO;
import com.example.inventory_backend.model.Chat;
import com.example.inventory_backend.model.ChatMessage;
import com.example.inventory_backend.model.ChatParticipant;
import com.example.inventory_backend.model.User;
import com.example.inventory_backend.repository.ChatMessageRepository;
import com.example.inventory_backend.repository.ChatParticipantRepository;
import com.example.inventory_backend.repository.ChatRepository;
import com.example.inventory_backend.repository.UserRepository;
import com.example.inventory_backend.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private ChatRepository chatRepository;
    
    @Autowired
    private ChatMessageRepository chatMessageRepository;
    
    @Autowired
    private ChatParticipantRepository chatParticipantRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public List<ChatDTO> getAllUserChats(User currentUser) {
        List<Chat> chats = chatRepository.findByParticipant(currentUser);
        return chats.stream()
                .map(chat -> convertToDTO(chat, currentUser))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ChatDTO> getRecentChats(User currentUser) {
        List<Chat> chats = chatRepository.findRecentByParticipant(currentUser);
        return chats.stream()
                .map(chat -> convertToDTO(chat, currentUser))
                .collect(Collectors.toList());
    }
    
    @Override
    public ChatDTO getChatById(Long chatId, User currentUser) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found with id: " + chatId));
        
        if (!chat.getParticipants().contains(currentUser)) {
            throw new RuntimeException("You are not a participant in this chat");
        }
        
        return convertToDTO(chat, currentUser);
    }
    
    @Override
    public List<ChatMessageDTO> getChatMessages(Long chatId, User currentUser) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found with id: " + chatId));
        
        if (!chat.getParticipants().contains(currentUser)) {
            throw new RuntimeException("You are not a participant in this chat");
        }
        
        List<ChatMessage> messages = chatMessageRepository.findByChatOrderByTimestampAsc(chat);
        return messages.stream()
                .map(this::convertToMessageDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public ChatMessageDTO sendMessage(Long chatId, String content, User sender) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found with id: " + chatId));
        
        if (!chat.getParticipants().contains(sender)) {
            throw new RuntimeException("You are not a participant in this chat");
        }
        
        ChatMessage message = new ChatMessage();
        message.setChat(chat);
        message.setSender(sender);
        message.setContent(content);
        message.setTimestamp(LocalDateTime.now());
        message.setRead(false);
        
        ChatMessage savedMessage = chatMessageRepository.save(message);
        
        chat.setUpdatedAt(LocalDateTime.now());
        chatRepository.save(chat);
        
        return convertToMessageDTO(savedMessage);
    }
    
    @Override
    @Transactional
    public void markChatAsRead(Long chatId, User currentUser) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found with id: " + chatId));
        
        if (!chat.getParticipants().contains(currentUser)) {
            throw new RuntimeException("You are not a participant in this chat");
        }
        
        ChatMessage latestMessage = chatMessageRepository.findLatestMessageByChat(chat)
                .orElse(null);
                
        if (latestMessage == null) {
            return;
        }
        
        List<ChatParticipant> participants = chatParticipantRepository.findByChatAndUser(chat, currentUser);
        if (!participants.isEmpty()) {
            ChatParticipant participant = participants.get(0);
            participant.setLastReadMessageId(latestMessage.getId());
            chatParticipantRepository.save(participant);
        }
        
        List<ChatMessage> unreadMessages = chatMessageRepository.findByChatOrderByTimestampAsc(chat)
                .stream()
                .filter(msg -> !msg.getSender().equals(currentUser) && !msg.isRead())
                .collect(Collectors.toList());
                
        for (ChatMessage message : unreadMessages) {
            message.setRead(true);
            chatMessageRepository.save(message);
        }
    }
    
    @Override
    public int countUnreadMessages(User currentUser) {
        return chatMessageRepository.countTotalUnreadMessages(currentUser);
    }
    
    @Override
    @Transactional
    public ChatDTO createNewChat(User currentUser, Long otherUserId) {
        User otherUser = userRepository.findById(otherUserId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + otherUserId));
        
        // Verify both users are from the same company
        if (!currentUser.getCompany().getId().equals(otherUser.getCompany().getId())) {
            throw new RuntimeException("Cannot create chat with user from different company");
        }
        
        List<Chat> existingChats = chatRepository.findByTwoParticipants(currentUser, otherUser);
        if (!existingChats.isEmpty()) {
            return convertToDTO(existingChats.get(0), currentUser);
        }
        
        Chat chat = new Chat();
        chat.setCompany(currentUser.getCompany());
        Chat savedChat = chatRepository.save(chat);
        
        ChatParticipant currentUserParticipant = new ChatParticipant();
        currentUserParticipant.setChat(savedChat);
        currentUserParticipant.setUser(currentUser);
        chatParticipantRepository.save(currentUserParticipant);
        
        ChatParticipant otherUserParticipant = new ChatParticipant();
        otherUserParticipant.setChat(savedChat);
        otherUserParticipant.setUser(otherUser);
        chatParticipantRepository.save(otherUserParticipant);
        
        return convertToDTO(savedChat, currentUser);
    }
    
    private ChatDTO convertToDTO(Chat chat, User currentUser) {
        ChatDTO dto = new ChatDTO();
        dto.setId(chat.getId());
        dto.setCreatedAt(chat.getCreatedAt());
        dto.setUpdatedAt(chat.getUpdatedAt());
        
        User otherParticipant = chat.getParticipants().stream()
                .filter(user -> !user.equals(currentUser))
                .findFirst()
                .orElse(null);
                
        if (otherParticipant != null) {
            UserDTO otherParticipantDTO = new UserDTO();
            otherParticipantDTO.setId(otherParticipant.getId());
            otherParticipantDTO.setName(otherParticipant.getName());
            otherParticipantDTO.setEmail(otherParticipant.getEmail());
            otherParticipantDTO.setRole(otherParticipant.getRole().name());
            dto.setOtherParticipant(otherParticipantDTO);
        }
        
        ChatMessage latestMessage = chatMessageRepository.findLatestMessageByChat(chat)
                .orElse(null);
                
        if (latestMessage != null) {
            dto.setLastMessage(convertToMessageDTO(latestMessage));
        }
        
        int unreadCount = chatMessageRepository.countUnreadMessagesByChat(chat, currentUser);
        dto.setHasUnread(unreadCount > 0);
        
        return dto;
    }
    
    private ChatMessageDTO convertToMessageDTO(ChatMessage message) {
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setId(message.getId());
        dto.setContent(message.getContent());
        dto.setTimestamp(message.getTimestamp());
        dto.setRead(message.isRead());
        
        if (message.getSender() != null) {
            dto.setSenderId(message.getSender().getId());
            dto.setSenderName(message.getSender().getName());
        }
        
        return dto;
    }
}
