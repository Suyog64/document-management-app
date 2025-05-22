package com.example.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class QuestionRequest {
    @NotBlank
    @Size(min = 2, max = 500)
    private String question;
    
    private String context; // Optional context to help with the search
}
