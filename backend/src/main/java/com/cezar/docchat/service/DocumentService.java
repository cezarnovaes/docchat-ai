package com.cezar.docchat.service;

import com.cezar.docchat.dto.DocumentResponse;
import com.cezar.docchat.model.Document;
import com.cezar.docchat.model.DocumentChunk;
import com.cezar.docchat.model.DocumentStatus;
import com.cezar.docchat.model.User;
import com.cezar.docchat.repository.DocumentChunkRepository;
import com.cezar.docchat.repository.DocumentRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository chunkRepository;
    private final PdfService pdfService;
    private final OpenAIService openAIService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DocumentService(DocumentRepository documentRepository,
                          DocumentChunkRepository chunkRepository,
                          PdfService pdfService,
                          OpenAIService openAIService) {
        this.documentRepository = documentRepository;
        this.chunkRepository = chunkRepository;
        this.pdfService = pdfService;
        this.openAIService = openAIService;
    }

    @Transactional
    public DocumentResponse uploadAndProcess(MultipartFile file, User user) {
        System.out.println("=== INICIO UPLOAD ===");
        System.out.println("Tamanho arquivo: " + file.getSize() + " bytes");
        
        // Validar arquivo
        if (file.isEmpty()) {
            throw new RuntimeException("Arquivo vazio");
        }
        
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new RuntimeException("Arquivo muito grande. Maximo 5MB");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("application/pdf")) {
            throw new RuntimeException("Apenas arquivos PDF sao aceitos");
        }

        // Criar documento
        System.out.println("Criando documento no banco...");
        Document document = new Document();
        document.setFilename(UUID.randomUUID().toString() + ".pdf");
        document.setOriginalFilename(file.getOriginalFilename());
        document.setContentType(contentType);
        document.setFileSize(file.getSize());
        document.setStatus(DocumentStatus.PROCESSING);
        document.setUser(user);

        System.out.println("Contando paginas...");
        int pageCount = pdfService.countPages(file);
        document.setPageCount(pageCount);
        System.out.println("Paginas: " + pageCount);

        document = documentRepository.save(document);
        System.out.println("Documento salvo ID: " + document.getId());

        try {
            System.out.println("Extraindo texto do PDF...");
            String text = pdfService.extractText(file);
            System.out.println("Texto extraido, tamanho: " + text.length() + " caracteres");

            if (text.length() > 100000) {
                throw new RuntimeException("Documento muito longo. Tente um PDF menor");
            }

            System.out.println("Dividindo em chunks...");
            List<String> chunks = pdfService.splitIntoChunks(text);
            System.out.println("Total de chunks: " + chunks.size());
            
            if (chunks.size() > 50) {
                throw new RuntimeException("Documento gerou muitos chunks. Tente um PDF menor");
            }

            System.out.println("Salvando chunks no banco...");
            for (int i = 0; i < chunks.size(); i++) {
                String chunkText = chunks.get(i);
                
                DocumentChunk chunk = new DocumentChunk();
                chunk.setContent(chunkText);
                chunk.setChunkIndex(i);
                chunk.setTokenCount(pdfService.estimateTokens(chunkText));
                chunk.setEmbedding(null);
                chunk.setDocument(document);
                
                chunkRepository.save(chunk);
                System.out.println("Chunk " + (i+1) + "/" + chunks.size() + " salvo");
            }

            System.out.println("Atualizando status do documento...");
            document.setStatus(DocumentStatus.READY);
            document.setProcessedAt(java.time.LocalDateTime.now());
            documentRepository.save(document);
            System.out.println("=== PROCESSAMENTO CONCLUIDO ===");

        } catch (Exception e) {
            System.err.println("ERRO: " + e.getMessage());
            e.printStackTrace();
            document.setStatus(DocumentStatus.ERROR);
            documentRepository.save(document);
            throw new RuntimeException("Erro ao processar documento: " + e.getMessage(), e);
        }

        return mapToResponse(document);
    }

    public Page<DocumentResponse> findAllByUser(Long userId, Pageable pageable) {
        return documentRepository.findByUserId(userId, pageable)
                .map(this::mapToResponse);
    }

    public DocumentResponse findById(Long documentId, Long userId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Documento nao encontrado"));

        if (!document.getUser().getId().equals(userId)) {
            throw new RuntimeException("Acesso negado");
        }

        return mapToResponse(document);
    }

    @Transactional
    public void delete(Long documentId, Long userId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Documento nao encontrado"));

        if (!document.getUser().getId().equals(userId)) {
            throw new RuntimeException("Acesso negado");
        }

        chunkRepository.deleteByDocumentId(documentId);
        documentRepository.delete(document);
    }

    public List<DocumentChunk> getChunksByDocumentId(Long documentId) {
        return chunkRepository.findByDocumentId(documentId);
    }

    private String embeddingToJson(List<Double> embedding) {
        try {
            return objectMapper.writeValueAsString(embedding);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erro ao converter embedding para JSON", e);
        }
    }

    private DocumentResponse mapToResponse(Document document) {
        return new DocumentResponse(
            document.getId(),
            document.getFilename(),
            document.getOriginalFilename(),
            document.getFileSize(),
            document.getPageCount(),
            document.getStatus(),
            document.getCreatedAt(),
            document.getProcessedAt()
        );
    }
}