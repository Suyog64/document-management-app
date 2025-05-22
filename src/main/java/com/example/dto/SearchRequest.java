package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequest {
    private String title;
    private String fileType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Long authorId;
    private String keyword;
}
