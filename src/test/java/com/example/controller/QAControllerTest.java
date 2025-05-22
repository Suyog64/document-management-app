package com.example.controller;

import com.example.dto.QuestionRequest;
import com.example.entity.Document;
import com.example.entity.User;
import com.example.service.DocumentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(QAController.class)
public class QAControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private DocumentService documentService;

	private Document testDocument;
	private User testUser;
	private QuestionRequest questionRequest;
	private Page<Document> documentPage;

	@BeforeEach
	public void setup() {
		// Initialize test user
		testUser = new User();
		testUser.setId(1L);
		testUser.setUsername("testuser");
		testUser.setEmail("testuser@example.com");

		// Initialize test document
		testDocument = Document.builder().id(1L).title("Test Document").description("This is a test document")
				.filePath("test/path/document.pdf").fileType("application/pdf").fileSize(1024L)
				.contentText("This is the content of the test document. It contains information about testing.")
				.author(testUser).createdAt(LocalDateTime.now()).indexed(true).build();

		// Initialize document page
		documentPage = new PageImpl<>(Arrays.asList(testDocument));

		// Initialize question request
		questionRequest = new QuestionRequest();
		questionRequest.setQuestion("What is testing?");
	}

	@Test
	@WithMockUser(roles = "VIEWER")
	public void testAskQuestion() throws Exception {
		// Setup
		when(documentService.searchByKeyword(anyString(), any(Pageable.class))).thenReturn(documentPage);

		// Execute and Verify
		mockMvc.perform(post("/api/qa/question").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(questionRequest))).andExpect(status().isOk())
				.andExpect(jsonPath("$.question", is("What is testing?"))).andExpect(jsonPath("$.snippets", hasSize(1)))
				.andExpect(jsonPath("$.snippets[0].title", is("Test Document")))
				.andExpect(jsonPath("$.totalResults", is(1)));
	}

	@Test
	@WithMockUser(roles = "VIEWER")
	public void testGetRecentDocuments() throws Exception {
		// Setup
		when(documentService.getAllDocuments(any(Pageable.class))).thenReturn(documentPage);

		// Execute and Verify
		mockMvc.perform(get("/api/qa/recent").param("page", "0").param("size", "10")).andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(1)))
				.andExpect(jsonPath("$.content[0].title", is("Test Document")));
	}

	@Test
	@WithMockUser(roles = "VIEWER")
	public void testGetPopularTerms() throws Exception {
		// Execute and Verify
		mockMvc.perform(get("/api/qa/popular-terms")).andExpect(status().isOk())
				.andExpect(jsonPath("$.document", is(120))).andExpect(jsonPath("$.management", is(98)))
				.andExpect(jsonPath("$.search", is(87))).andExpect(jsonPath("$.upload", is(65)))
				.andExpect(jsonPath("$.user", is(42)));
	}
}
