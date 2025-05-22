package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDto {
    private Long id;
    
    @NotBlank
    @Size(max = 255)
    private String title;
    
    private String description;
    
    private Set<String> tags;
    
    private String authorUsername;
    
    private String fileType;
    
    private Long fileSize;
    
    private String createdAt;
    
    private String updatedAt;
}
