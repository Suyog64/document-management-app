package com.example.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.dto.DocumentDto;
import com.example.dto.DocumentUploadRequest;
import com.example.dto.SearchRequest;
import com.example.entity.Document;
import com.example.service.DocumentService;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/documents")
@Tag(name = "Document Management", description = "APIs for managing documents")
@SecurityRequirement(name = "bearerAuth")
public class DocumentController {

    private static final Logger logger = LoggerFactory.getLogger(DocumentController.class);

    @Autowired
    private DocumentService documentService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('EDITOR') or hasRole('ADMIN')")
    @Operation(summary = "Upload a new document", description = "Upload a document with metadata")
    @ApiResponse(responseCode = "201", description = "Document uploaded successfully",
            content = @Content(schema = @Schema(implementation = Document.class)))
    public CompletableFuture<ResponseEntity<Document>> uploadDocument(
            @Valid @RequestPart("metadata") DocumentUploadRequest metadata,
            @RequestPart("file") MultipartFile file,
            Authentication authentication) {
        
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Uploading document: {}", metadata.getTitle());
            Document document = documentService.uploadDocument(metadata, file, authentication.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(document);
        });
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('VIEWER') or hasRole('EDITOR') or hasRole('ADMIN')")
    @Operation(summary = "Get document by ID", description = "Retrieve a document by its ID")
    @ApiResponse(responseCode = "200", description = "Document found",
            content = @Content(schema = @Schema(implementation = Document.class)))
    @ApiResponse(responseCode = "404", description = "Document not found")
    public CompletableFuture<ResponseEntity<Document>> getDocument(
            @Parameter(description = "Document ID") @PathVariable Long id) {
        
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Fetching document with ID: {}", id);
            Document document = documentService.getDocumentById(id);
            return ResponseEntity.ok(document);
        });
    }

    @GetMapping
    @PreAuthorize("hasRole('VIEWER') or hasRole('EDITOR') or hasRole('ADMIN')")
    @Operation(summary = "Get all documents", description = "Retrieve all documents with pagination and sorting")
    public CompletableFuture<ResponseEntity<Page<Document>>> getAllDocuments(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {
        
        return CompletableFuture.supplyAsync(() -> {
            Sort sort = sortDir.equalsIgnoreCase("asc") ? 
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            logger.info("Fetching all documents, page: {}, size: {}", page, size);
            Page<Document> documents = documentService.getAllDocuments(pageable);
            return ResponseEntity.ok(documents);
        });
    }

    @GetMapping("/user")
    @PreAuthorize("hasRole('VIEWER') or hasRole('EDITOR') or hasRole('ADMIN')")
    @Operation(summary = "Get current user's documents", description = "Retrieve documents for the authenticated user")
    public CompletableFuture<ResponseEntity<Page<Document>>> getUserDocuments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Authentication authentication) {
        
        return CompletableFuture.supplyAsync(() -> {
            Sort sort = sortDir.equalsIgnoreCase("asc") ? 
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            logger.info("Fetching documents for user: {}", authentication.getName());
            Page<Document> documents = documentService.getDocumentsByAuthor(authentication.getName(), pageable);
            return ResponseEntity.ok(documents);
        });
    }

    @PostMapping("/search")
    @PreAuthorize("hasRole('VIEWER') or hasRole('EDITOR') or hasRole('ADMIN')")
    @Operation(summary = "Search documents by criteria", description = "Search documents using multiple parameters")
    public CompletableFuture<ResponseEntity<Page<Document>>> searchDocuments(
            @RequestBody SearchRequest searchRequest,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        return CompletableFuture.supplyAsync(() -> {
            Sort sort = sortDir.equalsIgnoreCase("asc") ? 
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            logger.info("Searching documents with criteria: {}", searchRequest);
            Page<Document> documents = documentService.searchDocuments(searchRequest, pageable);
            return ResponseEntity.ok(documents);
        });
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('VIEWER') or hasRole('EDITOR') or hasRole('ADMIN')")
    @Operation(summary = "Search documents by keyword", description = "Full-text search in documents")
    public CompletableFuture<ResponseEntity<Page<Document>>> searchByKeyword(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        return CompletableFuture.supplyAsync(() -> {
            Sort sort = sortDir.equalsIgnoreCase("asc") ? 
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            logger.info("Searching documents with keyword: {}", keyword);
            Page<Document> documents = documentService.searchByKeyword(keyword, pageable);
            return ResponseEntity.ok(documents);
        });
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('EDITOR') or hasRole('ADMIN')")
    @Operation(summary = "Update a document", description = "Update document metadata")
    @ApiResponse(responseCode = "200", description = "Document updated successfully")
    @ApiResponse(responseCode = "404", description = "Document not found")
    public CompletableFuture<ResponseEntity<Document>> updateDocument(
            @PathVariable Long id,
            @Valid @RequestBody DocumentDto documentDto) {
        
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Updating document with ID: {}", id);
            Document updatedDocument = documentService.updateDocument(id, documentDto);
            return ResponseEntity.ok(updatedDocument);
        });
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a document", description = "Delete a document by its ID")
    @ApiResponse(responseCode = "200", description = "Document deleted successfully")
    @ApiResponse(responseCode = "404", description = "Document not found")
    public CompletableFuture<ResponseEntity<Map<String, String>>> deleteDocument(@PathVariable Long id) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Deleting document with ID: {}", id);
            documentService.deleteDocument(id);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Document deleted successfully");
            return ResponseEntity.ok(response);
        });
    }

    @GetMapping("/unprocessed")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get unprocessed documents", description = "Get documents that are not yet indexed")
    public CompletableFuture<ResponseEntity<?>> getUnprocessedDocuments() {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Fetching unprocessed documents");
            return ResponseEntity.ok(documentService.getUnprocessedDocuments());
        });
    }
}
