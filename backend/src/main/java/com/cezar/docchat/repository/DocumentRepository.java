package com.cezar.docchat.repository;

import com.cezar.docchat.model.Document;
import com.cezar.docchat.model.DocumentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    Page<Document> findByUserId(Long userId, Pageable pageable);
    List<Document> findByUserIdAndStatus(Long userId, DocumentStatus status);
}