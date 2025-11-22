package com.cezar.docchat.controller;

import com.cezar.docchat.dto.ChatRequest;
import com.cezar.docchat.dto.ChatResponse;
import com.cezar.docchat.model.User;
import com.cezar.docchat.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@Tag(name = "Chat", description = "Conversar com documentos usando IA")
@SecurityRequirement(name = "Bearer Authentication")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    @Operation(summary = "Enviar pergunta sobre um documento")
    public ResponseEntity<ChatResponse> chat(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ChatRequest request) {
        ChatResponse response = chatService.chat(request, user);
        return ResponseEntity.ok(response);
    }
}