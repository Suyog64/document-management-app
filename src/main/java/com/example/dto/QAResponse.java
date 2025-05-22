package com.example.dto;

import java.util.List;

import lombok.Data;

@Data
public class QAResponse {
    private String question;
    private List<DocumentSnippet> snippets;
    private long totalResults;
}
