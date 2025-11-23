package com.cezar.docchat.service;

import com.cezar.docchat.dto.ChatRequest;
import com.cezar.docchat.dto.ChatResponse;
import com.cezar.docchat.model.*;
import com.cezar.docchat.repository.ChatMessageRepository;
import com.cezar.docchat.repository.DocumentRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ChatService {

    private final DocumentRepository documentRepository;
    private final DocumentService documentService;
    private final OpenAIService openAIService;
    private final ChatMessageRepository chatMessageRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final int TOP_K_CHUNKS = 3; // Quantidade de chunks mais relevantes

    public ChatService(DocumentRepository documentRepository,
                       DocumentService documentService,
                       OpenAIService openAIService,
                       ChatMessageRepository chatMessageRepository) {
        this.documentRepository = documentRepository;
        this.documentService = documentService;
        this.openAIService = openAIService;
        this.chatMessageRepository = chatMessageRepository;
    }

    @Transactional
    public ChatResponse chat(ChatRequest request, User user) {
        System.out.println("=== INICIO CHAT ===");
        System.out.println("Document ID: " + request.getDocumentId());
        System.out.println("Message: " + request.getMessage());
        
        // Verificar se documento existe e pertence ao usuario
        Document document = documentRepository.findById(request.getDocumentId())
                .orElseThrow(() -> new RuntimeException("Documento nao encontrado"));

        if (!document.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Acesso negado");
        }

        if (document.getStatus() != DocumentStatus.READY) {
            throw new RuntimeException("Documento ainda esta sendo processado");
        }

        System.out.println("Documento encontrado: " + document.getOriginalFilename());
        
        // TEMPORARIO: Sem embeddings, usar todos os chunks
        List<DocumentChunk> allChunks = documentService.getChunksByDocumentId(document.getId());
        System.out.println("Total de chunks encontrados: " + allChunks.size());

        // Pega os 3 primeiros chunks por enquanto
        List<DocumentChunk> topChunks = allChunks.stream()
                .limit(TOP_K_CHUNKS)
                .toList();

        System.out.println("Usando " + topChunks.size() + " chunks");

        // Converte para ChunkWithScore com score fake
        List<ChunkWithScore> scoredChunks = topChunks.stream()
                .map(chunk -> new ChunkWithScore(chunk, 1.0))
                .toList();

        // Montar contexto
        StringBuilder context = new StringBuilder();
        List<String> sources = new ArrayList<>();
        
        for (ChunkWithScore scored : scoredChunks) {
            String chunkContent = scored.chunk().getContent();
            System.out.println("Chunk " + scored.chunk().getChunkIndex() + " tamanho: " + chunkContent.length() + " chars");
            System.out.println("Primeiros 100 chars: " + chunkContent.substring(0, Math.min(100, chunkContent.length())));
            
            context.append(chunkContent).append("\n\n---\n\n");
            sources.add("Trecho " + (scored.chunk().getChunkIndex() + 1));
        }

        System.out.println("Contexto montado, tamanho total: " + context.length() + " chars");
        System.out.println("Primeiros 200 chars do contexto:");
        System.out.println(context.substring(0, Math.min(200, context.length())));

        // Chamar OpenAI com contexto
        System.out.println("Chamando OpenAI...");
        String answer = openAIService.chatWithContext(context.toString(), request.getMessage());
        System.out.println("Resposta recebida: " + answer.substring(0, Math.min(100, answer.length())));

        // Salvar mensagens no historico
        saveMessage(user, document, MessageRole.USER, request.getMessage());
        saveMessage(user, document, MessageRole.ASSISTANT, answer);

        System.out.println("=== FIM CHAT ===");
        return new ChatResponse(answer, sources);
    }

    private void saveMessage(User user, Document document, MessageRole role, String content) {
        ChatMessage message = new ChatMessage();
        message.setUser(user);
        message.setDocument(document);
        message.setRole(role);
        message.setContent(content);
        chatMessageRepository.save(message);
    }

    private List<Double> jsonToEmbedding(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<Double>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Erro ao converter JSON para embedding", e);
        }
    }

    /**
     * Calcula similaridade de cosseno entre dois vetores
     */
    private double cosineSimilarity(List<Double> vec1, List<Double> vec2) {
        if (vec1.size() != vec2.size()) {
            throw new IllegalArgumentException("Vetores devem ter o mesmo tamanho");
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < vec1.size(); i++) {
            dotProduct += vec1.get(i) * vec2.get(i);
            norm1 += Math.pow(vec1.get(i), 2);
            norm2 += Math.pow(vec2.get(i), 2);
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    private record ChunkWithScore(DocumentChunk chunk, double score) {}
}