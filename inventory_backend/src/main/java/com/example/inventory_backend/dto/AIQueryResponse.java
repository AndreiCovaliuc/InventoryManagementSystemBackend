package com.example.inventory_backend.dto;

import java.time.LocalDateTime;

public class AIQueryResponse {
    private String answer;
    private String question;
    private LocalDateTime timestamp;

    public AIQueryResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public AIQueryResponse(String question, String answer) {
        this.question = question;
        this.answer = answer;
        this.timestamp = LocalDateTime.now();
    }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
