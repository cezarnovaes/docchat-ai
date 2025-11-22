package com.cezar.docchat.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OpenAIService {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.model.chat}")
    private String chatModel;

    @Value("${openai.model.embedding}")
    private String embeddingModel;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String OPENAI_CHAT_URL = "https://api.openai.com/v1/chat/completions";
    private static final String OPENAI_EMBEDDING_URL = "https://api.openai.com/v1/embeddings";

    /**
     * Gera embedding (vetor) para um texto
     */
    public List<Double> generateEmbedding(String text) {
        try {
            HttpHeaders headers = createHeaders();

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", embeddingModel);
            requestBody.put("input", text);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                OPENAI_EMBEDDING_URL,
                HttpMethod.POST,
                request,
                String.class
            );

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode embeddingNode = root.path("data").get(0).path("embedding");

            List<Double> embedding = new ArrayList<>();
            for (JsonNode value : embeddingNode) {
                embedding.add(value.asDouble());
            }

            return embedding;

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar embedding: " + e.getMessage(), e);
        }
    }

    /**
     * Envia mensagem para o chat e retorna resposta
     */
    public String chat(String systemPrompt, String userMessage) {
        try {
            HttpHeaders headers = createHeaders();

            List<Map<String, String>> messages = new ArrayList<>();
            
            Map<String, String> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", systemPrompt);
            messages.add(systemMsg);

            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            messages.add(userMsg);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", chatModel);
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 1000);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                OPENAI_CHAT_URL,
                HttpMethod.POST,
                request,
                String.class
            );

            JsonNode root = objectMapper.readTree(response.getBody());
            return root.path("choices").get(0).path("message").path("content").asText();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao chamar chat: " + e.getMessage(), e);
        }
    }

    /**
     * Chat com contexto de documentos (RAG)
     */
    public String chatWithContext(String context, String question) {
        String systemPrompt = """
            Voce e um assistente especializado em responder perguntas baseadas em documentos.
            
            REGRAS:
            1. Responda APENAS com base no contexto fornecido
            2. Se a informacao nao estiver no contexto, diga "Nao encontrei essa informacao no documento"
            3. Seja claro e objetivo
            4. Cite trechos relevantes quando apropriado
            
            CONTEXTO DO DOCUMENTO:
            %s
            """.formatted(context);

        return chat(systemPrompt, question);
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        return headers;
    }
}