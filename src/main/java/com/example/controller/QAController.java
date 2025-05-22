package com.example.controller;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.DocumentSnippet;
import com.example.dto.QAResponse;
import com.example.dto.QuestionRequest;
import com.example.entity.Document;
import com.example.service.DocumentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/qa")
@Tag(name = "Q&A", description = "Question answering APIs")
@SecurityRequirement(name = "bearerAuth")
public class QAController {
    
    private static final Logger logger = LoggerFactory.getLogger(QAController.class);
    
    @Autowired
    private DocumentService documentService;
    
    @PostMapping("/question")
    @PreAuthorize("hasRole('VIEWER') or hasRole('EDITOR') or hasRole('ADMIN')")
    @Operation(summary = "Ask a question", description = "Search for documents matching the question")
    public CompletableFuture<ResponseEntity<QAResponse>> askQuestion(@RequestBody QuestionRequest questionRequest) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Processing question: {}", questionRequest.getQuestion());
            
            // Search for documents by keyword (the question)
            Pageable pageable = PageRequest.of(0, 5); // Get top 5 results
            Page<Document> matchingDocs = documentService.searchByKeyword(questionRequest.getQuestion(), pageable);
            
            // Extract snippets from matching documents
            List<DocumentSnippet> snippets = matchingDocs.getContent().stream()
                .map(doc -> {
                    // Extract relevant text sections (simplified implementation)
                    String contentText = doc.getContentText();
                    String relevantText = contentText != null ? 
                        extractRelevantSnippet(contentText, questionRequest.getQuestion()) : 
                        doc.getDescription();
                        
                    return new DocumentSnippet(
                        doc.getId(),
                        doc.getTitle(),
                        relevantText,
                        doc.getAuthor().getUsername(),
                        doc.getCreatedAt().toString()
                    );
                })
                .collect(Collectors.toList());
            
            // Construct response
            QAResponse response = new QAResponse();
            response.setQuestion(questionRequest.getQuestion());
            response.setSnippets(snippets);
            response.setTotalResults(matchingDocs.getTotalElements());
            
            return ResponseEntity.ok(response);
        });
    }
    
    @GetMapping("/recent")
    @PreAuthorize("hasRole('VIEWER') or hasRole('EDITOR') or hasRole('ADMIN')")
    @Operation(summary = "Get recent documents", description = "Retrieve recently created documents")
    public CompletableFuture<ResponseEntity<Page<Document>>> getRecentDocuments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Fetching recent documents");
            Pageable pageable = PageRequest.of(page, size);
            Page<Document> documents = documentService.getAllDocuments(pageable);
            return ResponseEntity.ok(documents);
        });
    }
    
    @GetMapping("/popular-terms")
    @PreAuthorize("hasRole('VIEWER') or hasRole('EDITOR') or hasRole('ADMIN')")
    @Operation(summary = "Get popular search terms", description = "Retrieve commonly searched terms")
    public CompletableFuture<ResponseEntity<Map<String, Integer>>> getPopularTerms() {
        // In a real application, this would query a search term tracking system
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Fetching popular search terms");
            // Sample data - in a real app, this would be retrieved from a database
            Map<String, Integer> popularTerms = Map.of(
                "document", 120,
                "management", 98,
                "search", 87,
                "upload", 65,
                "user", 42
            );
            return ResponseEntity.ok(popularTerms);
        });
    }
    
    /**
     * Simple method to extract relevant text snippet around keywords
     * In a production system, this would be more sophisticated
     */
    private String extractRelevantSnippet(String content, String question) {
        if (content == null || content.isEmpty()) {
            return "";
        }
        
        // Convert question to keywords
        String[] keywords = question.toLowerCase().split("\\s+");
        String contentLower = content.toLowerCase();
        
        // Find first occurrence of any keyword
        int bestPosition = -1;
        for (String keyword : keywords) {
            if (keyword.length() < 3) continue; // Skip short words
            
            int pos = contentLower.indexOf(keyword);
            if (pos >= 0 && (bestPosition == -1 || pos < bestPosition)) {
                bestPosition = pos;
            }
        }
        
        // If no keyword found, return the beginning of the content
        if (bestPosition == -1) {
            return content.length() <= 200 ? content : content.substring(0, 200) + "...";
        }
        
        // Extract snippet around the keyword (100 chars before, 300 after)
        int start = Math.max(0, bestPosition - 100);
        int end = Math.min(content.length(), bestPosition + 300);
        
        String snippet = content.substring(start, end);
        
        // Add ellipsis if we're not at the beginning/end
        if (start > 0) snippet = "..." + snippet;
        if (end < content.length()) snippet = snippet + "...";
        
        return snippet;
    }
}