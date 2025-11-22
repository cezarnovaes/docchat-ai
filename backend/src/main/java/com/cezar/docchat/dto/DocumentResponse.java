package com.cezar.docchat.dto;

import com.cezar.docchat.model.DocumentStatus;
import java.time.LocalDateTime;

public class DocumentResponse {

    private Long id;
    private String filename;
    private String originalFilename;
    private Long fileSize;
    private Integer pageCount;
    private DocumentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;

    public DocumentResponse(Long id, String filename, String originalFilename, Long fileSize,
                           Integer pageCount, DocumentStatus status, LocalDateTime createdAt,
                           LocalDateTime processedAt) {
        this.id = id;
        this.filename = filename;
        this.originalFilename = originalFilename;
        this.fileSize = fileSize;
        this.pageCount = pageCount;
        this.status = status;
        this.createdAt = createdAt;
        this.processedAt = processedAt;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public Integer getPageCount() {
        return pageCount;
    }

    public void setPageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }

    public DocumentStatus getStatus() {
        return status;
    }

    public void setStatus(DocumentStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
}