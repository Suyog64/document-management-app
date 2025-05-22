package com.example.controller;

import com.example.dto.DocumentDto;
import com.example.dto.DocumentUploadRequest;
import com.example.dto.SearchRequest;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DocumentController.class)
public class DocumentControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private DocumentService documentService;

	private Document testDocument;
	private User testUser;
	private DocumentUploadRequest uploadRequest;
	private DocumentDto documentDto;
	private List<Document> documentList;
	private Page<Document> documentPage;
	private SearchRequest searchRequest;

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
				.contentText("Test document content text for searching").author(testUser).createdAt(LocalDateTime.now())
				.indexed(true).build();

		// Initialize document list and page
		documentList = Arrays.asList(testDocument);
		documentPage = new PageImpl<>(documentList, PageRequest.of(0, 10), 1);

		// Initialize upload request
		uploadRequest = new DocumentUploadRequest();
		uploadRequest.setTitle("Test Document");
		uploadRequest.setDescription("This is a test document");
		Set<String> tags = new HashSet<>();
		tags.add("test");
		tags.add("document");
		uploadRequest.setTags(tags);

		// Initialize document DTO
		documentDto = new DocumentDto();
		documentDto.setTitle("Updated Document");
		documentDto.setDescription("This is an updated document");
		documentDto.setTags(tags);

		// Initialize search request
		searchRequest = new SearchRequest();
		searchRequest.setTitle("Test");
		searchRequest.setFileType("pdf");
		searchRequest.setStartDate(LocalDateTime.now().minusDays(7));
		searchRequest.setEndDate(LocalDateTime.now());
		searchRequest.setAuthorId(1L);
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	public void testGetDocumentById_Success() throws Exception {
		// Setup
		when(documentService.getDocumentById(1L)).thenReturn(testDocument);

		// Execute and Verify
		mockMvc.perform(get("/api/documents/1")).andExpect(status().isOk()).andExpect(jsonPath("$.id", is(1)))
				.andExpect(jsonPath("$.title", is("Test Document")))
				.andExpect(jsonPath("$.description", is("This is a test document")))
				.andExpect(jsonPath("$.fileType", is("application/pdf")));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	public void testGetAllDocuments() throws Exception {
		// Setup
		when(documentService.getAllDocuments(any(Pageable.class))).thenReturn(documentPage);

		// Execute and Verify
		mockMvc.perform(get("/api/documents").param("page", "0").param("size", "10").param("sortBy", "createdAt")
				.param("sortDir", "desc")).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(1)))
				.andExpect(jsonPath("$.content[0].title", is("Test Document")))
				.andExpect(jsonPath("$.totalElements", is(1)));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	public void testGetUserDocuments() throws Exception {
		// Setup
		when(documentService.getDocumentsByAuthor(anyString(), any(Pageable.class))).thenReturn(documentPage);

		// Execute and Verify
		mockMvc.perform(get("/api/documents/user").param("page", "0").param("size", "10").param("sortBy", "createdAt")
				.param("sortDir", "desc")).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(1)))
				.andExpect(jsonPath("$.content[0].title", is("Test Document")));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	public void testSearchDocuments() throws Exception {
		// Setup
		when(documentService.searchDocuments(any(SearchRequest.class), any(Pageable.class))).thenReturn(documentPage);

		// Execute and Verify
		mockMvc.perform(post("/api/documents/search").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(searchRequest)).param("page", "0").param("size", "10"))
				.andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(1)))
				.andExpect(jsonPath("$.content[0].title", is("Test Document")));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	public void testSearchByKeyword() throws Exception {
		// Setup
		when(documentService.searchByKeyword(anyString(), any(Pageable.class))).thenReturn(documentPage);

		// Execute and Verify
		mockMvc.perform(get("/api/documents/search").param("keyword", "test").param("page", "0").param("size", "10"))
				.andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(1)))
				.andExpect(jsonPath("$.content[0].title", is("Test Document")));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	public void testUpdateDocument() throws Exception {
		// Setup
		Document updatedDocument = Document.builder().id(1L).title("Updated Document")
				.description("This is an updated document").filePath("test/path/document.pdf")
				.fileType("application/pdf").fileSize(1024L).author(testUser).build();

		when(documentService.updateDocument(anyLong(), any(DocumentDto.class))).thenReturn(updatedDocument);

		// Execute and Verify
		mockMvc.perform(put("/api/documents/1").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(documentDto))).andExpect(status().isOk())
				.andExpect(jsonPath("$.title", is("Updated Document")))
				.andExpect(jsonPath("$.description", is("This is an updated document")));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	public void testDeleteDocument() throws Exception {
		// Setup
		doNothing().when(documentService).deleteDocument(1L);

		// Execute and Verify
		mockMvc.perform(delete("/api/documents/1")).andExpect(status().isOk())
				.andExpect(jsonPath("$.message", is("Document deleted successfully")));

		verify(documentService).deleteDocument(1L);
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	public void testGetUnprocessedDocuments() throws Exception {
		// Setup
		when(documentService.getUnprocessedDocuments()).thenReturn(documentList);

		// Execute and Verify
		mockMvc.perform(get("/api/documents/unprocessed")).andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1))).andExpect(jsonPath("$[0].title", is("Test Document")));
	}

	@Test
	@WithMockUser(roles = "EDITOR")
	public void testUploadDocument() throws Exception {
		// Setup
		MockMultipartFile file = new MockMultipartFile("file", "test.pdf", MediaType.APPLICATION_PDF_VALUE,
				"PDF content".getBytes());

		MockMultipartFile metadata = new MockMultipartFile("metadata", "", MediaType.APPLICATION_JSON_VALUE,
				objectMapper.writeValueAsString(uploadRequest).getBytes());

		when(documentService.uploadDocument(any(DocumentUploadRequest.class), any(MockMultipartFile.class),
				anyString())).thenReturn(testDocument);

		// Execute and Verify - Note: This is a simplified test as multipart file
		// uploads are complex to test
		// In a real test environment, you might need to configure additional components
		mockMvc.perform(multipart("/api/documents/upload").file(file).file(metadata)).andExpect(status().isCreated());
	}
}
