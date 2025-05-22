package com.example.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.dto.DocumentDto;
import com.example.dto.DocumentUploadRequest;
import com.example.dto.SearchRequest;
import com.example.entity.Document;
import com.example.entity.Tag;
import com.example.entity.User;
import com.example.exception.ResourceNotFoundException;
import com.example.repository.DocumentRepository;
import com.example.repository.TagRepository;
import com.example.repository.UserRepository;


@Service
public class DocumentService {
    private static final Logger logger = LoggerFactory.getLogger(DocumentService.class);

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private DocumentContentExtractor contentExtractor;

    @Transactional
    public Document uploadDocument(DocumentUploadRequest request, MultipartFile file, String username) {
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        // Store file and get path
        String fileName = fileStorageService.storeFile(file);
        String filePath = fileName;
        String fileType = file.getContentType();
        long fileSize = file.getSize();

        // Process document tags
        Set<Tag> documentTags = request.getTags().stream()
                .map(tagName -> {
                    Optional<Tag> existingTag = tagRepository.findByName(tagName);
                    return existingTag.orElseGet(() -> {
                        Tag newTag = new Tag();
                        newTag.setName(tagName);
                        return tagRepository.save(newTag);
                    });
                })
                .collect(Collectors.toSet());

        // Create document entity
        Document document = Document.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .filePath(filePath)
                .fileType(fileType)
                .fileSize(fileSize)
                .author(author)
                .tags(documentTags)
                .build();

        Document savedDocument = documentRepository.save(document);
        
        // Asynchronously extract and index content
        processDocumentContent(savedDocument.getId());
        
        return savedDocument;
    }

    @Async
    public void processDocumentContent(Long documentId) {
        try {
            Document document = documentRepository.findById(documentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));
            
            // Extract text from document
            String extractedText = contentExtractor.extractText(
                    fileStorageService.getFilePath(document.getFilePath()));
            
            // Save extracted text
            document.setContentText(extractedText);
            
            // Prepare text for search indexing
            String searchVector = contentExtractor.preprocessTextForSearch(
                    document.getTitle() + " " + document.getDescription() + " " + extractedText);
            document.setSearchVector(searchVector);
            
            // Mark as indexed
            document.setIndexed(true);
            
            documentRepository.save(document);
            logger.info("Document processed successfully: {}", documentId);
        } catch (IOException e) {
            logger.error("Failed to process document content: {}", documentId, e);
        }
    }

    @Cacheable(value = "documentCache", key = "#id")
    public Document getDocumentById(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + id));
    }

    public Page<Document> getAllDocuments(Pageable pageable) {
        return documentRepository.findAll(pageable);
    }

    public Page<Document> getDocumentsByAuthor(String username, Pageable pageable) {
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        return documentRepository.findByAuthor(author, pageable);
    }

    public Page<Document> searchDocuments(SearchRequest searchRequest, Pageable pageable) {
        String title = searchRequest.getTitle();
        String fileType = searchRequest.getFileType();
        LocalDateTime startDate = searchRequest.getStartDate();
        LocalDateTime endDate = searchRequest.getEndDate();
        Long authorId = searchRequest.getAuthorId();

        return documentRepository.findByMultipleParameters(
                title, fileType, startDate, endDate, authorId, pageable);
    }

    public Page<Document> searchByKeyword(String keyword, Pageable pageable) {
        return documentRepository.searchDocuments(keyword, pageable);
    }

    @CacheEvict(value = "documentCache", key = "#id")
    @Transactional
    public Document updateDocument(Long id, DocumentDto documentDto) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + id));

        document.setTitle(documentDto.getTitle());
        document.setDescription(documentDto.getDescription());

        // Update tags if provided
        if (documentDto.getTags() != null && !documentDto.getTags().isEmpty()) {
            Set<Tag> updatedTags = documentDto.getTags().stream()
                    .map(tagName -> {
                        Optional<Tag> existingTag = tagRepository.findByName(tagName);
                        return existingTag.orElseGet(() -> {
                            Tag newTag = new Tag();
                            newTag.setName(tagName);
                            return tagRepository.save(newTag);
                        });
                    })
                    .collect(Collectors.toSet());
            document.setTags(updatedTags);
        }

        return documentRepository.save(document);
    }

    @CacheEvict(value = "documentCache", key = "#id")
    @Transactional
    public void deleteDocument(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + id));

        // Delete the file from storage
        fileStorageService.deleteFile(document.getFilePath());
        
        // Delete the document from database
        documentRepository.delete(document);
    }

    public List<Document> getUnprocessedDocuments() {
        return documentRepository.findByIndexed(false);
    }
}
