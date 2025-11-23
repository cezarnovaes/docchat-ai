package com.cezar.docchat.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class PdfService {

    private static final int CHUNK_SIZE = 800; // Reduzido de 1000 para 800
    
    public String extractText(MultipartFile file) {
        PDDocument document = null;
        try {
            if (file.getSize() > 10 * 1024 * 1024) {
                throw new RuntimeException("Arquivo muito grande. Maximo 10MB");
            }

            document = Loader.loadPDF(file.getBytes());
            
            int pageCount = document.getNumberOfPages();
            if (pageCount > 50) {
                throw new RuntimeException("PDF muito grande. Maximo 50 paginas");
            }

            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            
            return text;
            
        } catch (IOException e) {
            throw new RuntimeException("Erro ao extrair texto do PDF: " + e.getMessage(), e);
        } finally {
            if (document != null) {
                try {
                    document.close();
                } catch (IOException e) {
                    // Ignora
                }
            }
        }
    }

    public int countPages(MultipartFile file) {
        PDDocument document = null;
        try {
            document = Loader.loadPDF(file.getBytes());
            int pages = document.getNumberOfPages();
            return pages;
        } catch (IOException e) {
            throw new RuntimeException("Erro ao contar paginas do PDF: " + e.getMessage(), e);
        } finally {
            if (document != null) {
                try {
                    document.close();
                } catch (IOException e) {
                    // Ignora
                }
            }
        }
    }

    /**
     * Divide texto em chunks SIMPLES - sem sobreposicao
     */
    public List<String> splitIntoChunks(String text) {
        List<String> chunks = new ArrayList<>();
        
        if (text == null || text.isEmpty()) {
            return chunks;
        }

        // Limpa espacos extras
        text = text.replaceAll("\\s+", " ").trim();
        
        // Divide em pedacos de CHUNK_SIZE caracteres
        int textLength = text.length();
        int start = 0;
        
        while (start < textLength) {
            int end = Math.min(start + CHUNK_SIZE, textLength);
            
            // Pega o pedaco
            String chunk = text.substring(start, end).trim();
            
            if (!chunk.isEmpty() && chunk.length() > 50) { // Ignora chunks muito pequenos
                chunks.add(chunk);
            }
            
            start = end;
        }

        return chunks;
    }

    public int estimateTokens(String text) {
        return text.length() / 3;
    }
}