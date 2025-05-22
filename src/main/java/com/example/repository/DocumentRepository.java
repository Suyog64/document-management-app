package com.example.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.entity.Document;
import com.example.entity.User;


@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByAuthor(User author);
    
    Optional<Document> findByTitle(String title);
    
    Page<Document> findByAuthor(User author, Pageable pageable);
    
    Page<Document> findByFileType(String fileType, Pageable pageable);
    
    @Query("SELECT d FROM Document d WHERE d.createdAt BETWEEN :startDate AND :endDate")
    Page<Document> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                   @Param("endDate") LocalDateTime endDate, 
                                   Pageable pageable);
    
    @Query("SELECT d FROM Document d WHERE LOWER(d.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(d.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(d.contentText) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Document> searchDocuments(@Param("keyword") String keyword, Pageable pageable);
    
    // More complex search query with multiple parameters
    @Query("SELECT d FROM Document d WHERE " +
           "(:title IS NULL OR LOWER(d.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
           "(:fileType IS NULL OR d.fileType = :fileType) AND " +
           "(:startDate IS NULL OR d.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR d.createdAt <= :endDate) AND " +
           "(:authorId IS NULL OR d.author.id = :authorId)")
    Page<Document> findByMultipleParameters(
            @Param("title") String title,
            @Param("fileType") String fileType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("authorId") Long authorId,
            Pageable pageable);
    
    List<Document> findByIndexed(boolean indexed);
}
