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

    private static final int CHUNK_SIZE = 1000; // caracteres por chunk
    private static final int CHUNK_OVERLAP = 200; // sobreposicao entre chunks

    /**
     * Extrai texto de um PDF
     */
    public String extractText(MultipartFile file) {
        try {
            PDDocument document = Loader.loadPDF(file.getBytes());
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            document.close();
            return text;
        } catch (IOException e) {
            throw new RuntimeException("Erro ao extrair texto do PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Conta paginas do PDF
     */
    public int countPages(MultipartFile file) {
        try {
            PDDocument document = Loader.loadPDF(file.getBytes());
            int pages = document.getNumberOfPages();
            document.close();
            return pages;
        } catch (IOException e) {
            throw new RuntimeException("Erro ao contar paginas do PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Divide texto em chunks menores para processamento
     */
    public List<String> splitIntoChunks(String text) {
        List<String> chunks = new ArrayList<>();
        
        if (text == null || text.isEmpty()) {
            return chunks;
        }

        // Limpa o texto
        text = text.replaceAll("\\s+", " ").trim();

        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + CHUNK_SIZE, text.length());
            
            // Tenta terminar em um ponto final ou quebra de paragrafo
            if (end < text.length()) {
                int lastPeriod = text.lastIndexOf(". ", end);
                int lastNewline = text.lastIndexOf("\n", end);
                int breakPoint = Math.max(lastPeriod, lastNewline);
                
                if (breakPoint > start + CHUNK_SIZE / 2) {
                    end = breakPoint + 1;
                }
            }

            String chunk = text.substring(start, end).trim();
            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }

            start = end - CHUNK_OVERLAP;
            if (start < 0) start = 0;
            if (start >= text.length()) break;
        }

        return chunks;
    }

    /**
     * Estima quantidade de tokens (aproximado)
     */
    public int estimateTokens(String text) {
        // Aproximacao: 1 token ~ 4 caracteres em ingles, ~3 em portugues
        return text.length() / 3;
    }
}