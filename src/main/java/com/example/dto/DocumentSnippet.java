package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentSnippet {
    private Long documentId;
    private String title;
    private String textSnippet;
    private String author;
    private String createdAt;
}
