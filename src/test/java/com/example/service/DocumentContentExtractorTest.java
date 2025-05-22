package com.example.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class DocumentContentExtractorTest {

	@InjectMocks
	private DocumentContentExtractor contentExtractor;

	@TempDir
	Path tempDir;

	private Path testFilePath;
	private String testFileContent;

	@BeforeEach
	public void setup() throws IOException {
		// Create a test file
		testFileContent = "This is a test document content for extraction.";
		testFilePath = tempDir.resolve("test.txt");
		Files.write(testFilePath, testFileContent.getBytes());
	}

	@Test
	public void testExtractText_FromPath() throws IOException {
		// Execute
		String result = contentExtractor.extractText(testFilePath);

		// Verify
		assertNotNull(result);
		assertTrue(result.contains(testFileContent));
	}

	@Test
	public void testExtractText_FromInputStream() {
		// Setup
		InputStream inputStream = new ByteArrayInputStream(testFileContent.getBytes());

		// Execute
		String result = contentExtractor.extractText(inputStream);

		// Verify
		assertNotNull(result);
		assertTrue(result.contains(testFileContent));
	}

	@Test
	public void testExtractText_FromPath_FileNotFound() throws IOException {
		// Setup
		Path nonExistentPath = tempDir.resolve("nonexistent.txt");

		// Execute
		String result = contentExtractor.extractText(nonExistentPath);

		// Verify
		assertEquals("", result);
	}

	@Test
	public void testPreprocessTextForSearch() {
		// Setup
		String inputText = "This is a test document! It contains UPPERCASE and punctuation.";

		// Execute
		String result = contentExtractor.preprocessTextForSearch(inputText);

		// Verify
		assertNotNull(result);
		assertEquals("this is a test document  it contains uppercase and punctuation", result);
	}

	@Test
	public void testPreprocessTextForSearch_NullInput() {
		// Execute
		String result = contentExtractor.preprocessTextForSearch(null);

		// Verify
		assertEquals("", result);
	}

	@Test
	public void testGenerateSummary_ShortContent() {
		// Setup
		String shortContent = "This is a short content that doesn't need truncation.";

		// Execute
		String result = contentExtractor.generateSummary(shortContent, 100);

		// Verify
		assertEquals(shortContent, result);
	}

	@Test
	public void testGenerateSummary_LongContent() {
		// Setup
		String longContent = "This is a long content paragraph. It contains multiple sentences. "
				+ "We want to truncate it properly at sentence boundaries. "
				+ "This should not be included in the summary.";

		// Execute
		String result = contentExtractor.generateSummary(longContent, 50);

		// Verify
		assertTrue(result.endsWith("..."));
		assertTrue(result.length() <= 53); // Max length + "..."
	}

	@Test
	public void testGenerateSummary_NoSentenceEnd() {
		// Setup
		String contentNoSentenceEnd = "This is content without any sentence end marks so it will be truncated";

		// Execute
		String result = contentExtractor.generateSummary(contentNoSentenceEnd, 20);

		// Verify
		assertEquals(contentNoSentenceEnd.substring(0, 20) + "...", result);
	}

	@Test
	public void testGenerateSummary_NullContent() {
		// Execute
		String result = contentExtractor.generateSummary(null, 100);

		// Verify
		assertEquals("", result);
	}
}
