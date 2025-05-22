package com.example.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

@Service
public class DocumentContentExtractor {
    private static final Logger logger = LoggerFactory.getLogger(DocumentContentExtractor.class);

    public String extractText(Path filePath) throws IOException {
        try (InputStream stream = Files.newInputStream(filePath)) {
            return extractText(stream);
        } catch (Exception e) {
            logger.error("Error extracting text from file: {}", filePath, e);
            return "";
        }
    }

    public String extractText(InputStream stream) {
        try {
            // Set up Tika parser
            Parser parser = new AutoDetectParser();
            BodyContentHandler handler = new BodyContentHandler(-1); // -1 for unlimited text
            Metadata metadata = new Metadata();
            ParseContext context = new ParseContext();
            
            // Parse the document
            parser.parse(stream, handler, metadata, context);
            
            return handler.toString();
        } catch (IOException | SAXException | TikaException e) {
            logger.error("Error extracting text from stream", e);
            return "";
        }
    }
    
    public String preprocessTextForSearch(String text) {
        if (text == null) {
            return "";
        }
        
        // Normalize text for search
        String normalized = text.toLowerCase()
                .replaceAll("[\\p{Punct}&&[^_]]", " ") // Replace punctuation with spaces
                .replaceAll("\\s+", " ")               // Replace multiple spaces with single space
                .trim();
        
        return normalized;
    }
    
    // Method to generate a brief summary of the document content
    public String generateSummary(String content, int maxLength) {
        if (content == null || content.isEmpty()) {
            return "";
        }
        
        // Simple truncation for now, in a real app this would use NLP
        if (content.length() <= maxLength) {
            return content;
        }
        
        // Find the nearest sentence end after maxLength/2 characters
        int endPos = content.indexOf(".", maxLength / 2);
        if (endPos == -1 || endPos > maxLength) {
            endPos = maxLength;
        } else {
            endPos++; // Include the period
        }
        
        return content.substring(0, endPos).trim() + "...";
    }
}
