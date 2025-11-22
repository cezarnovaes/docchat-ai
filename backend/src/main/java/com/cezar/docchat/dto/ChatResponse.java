package com.cezar.docchat.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ChatResponse {

    private String answer;
    private List<String> sources;
    private LocalDateTime timestamp;

    public ChatResponse(String answer, List<String> sources) {
        this.answer = answer;
        this.sources = sources;
        this.timestamp = LocalDateTime.now();
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public List<String> getSources() {
        return sources;
    }

    public void setSources(List<String> sources) {
        this.sources = sources;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}