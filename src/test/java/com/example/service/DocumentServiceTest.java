package com.example.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.example.dto.DocumentDto;
import com.example.dto.DocumentUploadRequest;
import com.example.dto.SearchRequest;
import com.example.entity.Document;
import com.example.entity.Tag;
import com.example.entity.User;
import com.example.exception.ResourceNotFoundException;
import com.example.repository.DocumentRepository;
import com.example.repository.TagRepository;
import com.example.repository.UserRepository;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DocumentServiceTest {

	@Mock
	private DocumentRepository documentRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private TagRepository tagRepository;

	@Mock
	private FileStorageService fileStorageService;

	@Mock
	private DocumentContentExtractor contentExtractor;

	@InjectMocks
	private DocumentService documentService;

	private User testUser;
	private Document testDocument;
	private DocumentUploadRequest uploadRequest;
	private DocumentDto documentDto;
	private Tag testTag;
	private MultipartFile testFile;
	private SearchRequest searchRequest;

	@BeforeEach
	public void setup() {
		// Initialize test user
		testUser = new User();
		testUser.setId(1L);
		testUser.setUsername("testuser");
		testUser.setEmail("testuser@example.com");

		// Initialize test tag
		testTag = new Tag();
		testTag.setId(1L);
		testTag.setName("test");

		// Initialize test document
		testDocument = Document.builder().id(1L).title("Test Document").description("This is a test document")
				.filePath("test/path/document.pdf").fileType("application/pdf").fileSize(1024L)
				.contentText("Test document content text for searching").author(testUser).createdAt(LocalDateTime.now())
				.indexed(true).build();

		Set<Tag> tags = new HashSet<>();
		tags.add(testTag);
		testDocument.setTags(tags);

		// Initialize upload request
		uploadRequest = new DocumentUploadRequest();
		uploadRequest.setTitle("Test Document");
		uploadRequest.setDescription("This is a test document");
		Set<String> tagNames = new HashSet<>();
		tagNames.add("test");
		tagNames.add("document");
		uploadRequest.setTags(tagNames);

		// Initialize document DTO
		documentDto = new DocumentDto();
		documentDto.setTitle("Updated Document");
		documentDto.setDescription("This is an updated document");
		documentDto.setTags(tagNames);

		// Initialize test file
		testFile = new MockMultipartFile("test.pdf", "test.pdf", "application/pdf", "Test PDF content".getBytes());

		// Initialize search request
		searchRequest = new SearchRequest();
		searchRequest.setTitle("Test");
		searchRequest.setFileType("pdf");
		searchRequest.setStartDate(LocalDateTime.now().minusDays(7));
		searchRequest.setEndDate(LocalDateTime.now());
		searchRequest.setAuthorId(1L);
	}

	@Test
	public void testUploadDocument_Success() throws IOException {
		// Setup
		when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
		when(fileStorageService.storeFile(any(MultipartFile.class))).thenReturn("stored-file-name.pdf");
		when(tagRepository.findByName("test")).thenReturn(Optional.of(testTag));
		when(tagRepository.findByName("document")).thenReturn(Optional.empty());
		when(tagRepository.save(any(Tag.class))).thenReturn(new Tag());
		when(documentRepository.save(any(Document.class))).thenReturn(testDocument);

		// Execute
		Document result = documentService.uploadDocument(uploadRequest, testFile, "testuser");

		// Verify
		assertNotNull(result);
		assertEquals("Test Document", result.getTitle());
		assertEquals("This is a test document", result.getDescription());
		assertEquals(testUser, result.getAuthor());

		verify(userRepository).findByUsername("testuser");
		verify(fileStorageService).storeFile(testFile);
		verify(tagRepository).findByName("test");
		verify(tagRepository).findByName("document");
		verify(tagRepository).save(any(Tag.class));
		verify(documentRepository).save(any(Document.class));
	}

	@Test
	public void testUploadDocument_UserNotFound() {
		// Setup
		when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

		// Execute & Verify
		assertThrows(ResourceNotFoundException.class, () -> {
			documentService.uploadDocument(uploadRequest, testFile, "nonexistentuser");
		});

		verify(userRepository).findByUsername("nonexistentuser");
		verify(fileStorageService, never()).storeFile(any(MultipartFile.class));
		verify(documentRepository, never()).save(any(Document.class));
	}

	@Test
	public void testGetDocumentById_Success() {
		// Setup
		when(documentRepository.findById(1L)).thenReturn(Optional.of(testDocument));

		// Execute
		Document result = documentService.getDocumentById(1L);

		// Verify
		assertNotNull(result);
		assertEquals(1L, result.getId());
		assertEquals("Test Document", result.getTitle());

		verify(documentRepository).findById(1L);
	}

	@Test
	public void testGetDocumentById_NotFound() {
		// Setup
		when(documentRepository.findById(99L)).thenReturn(Optional.empty());

		// Execute & Verify
		assertThrows(ResourceNotFoundException.class, () -> {
			documentService.getDocumentById(99L);
		});

		verify(documentRepository).findById(99L);
	}

	@Test
	public void testGetAllDocuments() {
		// Setup
		Pageable pageable = PageRequest.of(0, 10);
		Page<Document> documentPage = new PageImpl<>(Collections.singletonList(testDocument), pageable, 1);
		when(documentRepository.findAll(pageable)).thenReturn(documentPage);

		// Execute
		Page<Document> result = documentService.getAllDocuments(pageable);

		// Verify
		assertNotNull(result);
		assertEquals(1, result.getTotalElements());
		assertEquals("Test Document", result.getContent().get(0).getTitle());

		verify(documentRepository).findAll(pageable);
	}

	@Test
	public void testGetDocumentsByAuthor() {
		// Setup
		Pageable pageable = PageRequest.of(0, 10);
		Page<Document> documentPage = new PageImpl<>(Collections.singletonList(testDocument), pageable, 1);
		when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
		when(documentRepository.findByAuthor(testUser, pageable)).thenReturn(documentPage);

		// Execute
		Page<Document> result = documentService.getDocumentsByAuthor("testuser", pageable);

		// Verify
		assertNotNull(result);
		assertEquals(1, result.getTotalElements());
		assertEquals("Test Document", result.getContent().get(0).getTitle());

		verify(userRepository).findByUsername("testuser");
		verify(documentRepository).findByAuthor(testUser, pageable);
	}

	@Test
	public void testGetDocumentsByAuthor_UserNotFound() {
		// Setup
		Pageable pageable = PageRequest.of(0, 10);
		when(userRepository.findByUsername("nonexistentuser")).thenReturn(Optional.empty());

		// Execute & Verify
		assertThrows(ResourceNotFoundException.class, () -> {
			documentService.getDocumentsByAuthor("nonexistentuser", pageable);
		});

		verify(userRepository).findByUsername("nonexistentuser");
		verify(documentRepository, never()).findByAuthor(any(User.class), any(Pageable.class));
	}

	@Test
	public void testSearchDocuments() {
		// Setup
		Pageable pageable = PageRequest.of(0, 10);
		Page<Document> documentPage = new PageImpl<>(Collections.singletonList(testDocument), pageable, 1);
		when(documentRepository.findByMultipleParameters(anyString(), anyString(), any(LocalDateTime.class),
				any(LocalDateTime.class), anyLong(), any(Pageable.class))).thenReturn(documentPage);

		// Execute
		Page<Document> result = documentService.searchDocuments(searchRequest, pageable);

		// Verify
		assertNotNull(result);
		assertEquals(1, result.getTotalElements());
		assertEquals("Test Document", result.getContent().get(0).getTitle());

		verify(documentRepository).findByMultipleParameters(searchRequest.getTitle(), searchRequest.getFileType(),
				searchRequest.getStartDate(), searchRequest.getEndDate(), searchRequest.getAuthorId(), pageable);
	}

	@Test
	public void testSearchByKeyword() {
		// Setup
		Pageable pageable = PageRequest.of(0, 10);
		Page<Document> documentPage = new PageImpl<>(Collections.singletonList(testDocument), pageable, 1);
		when(documentRepository.searchDocuments("test", pageable)).thenReturn(documentPage);

		// Execute
		Page<Document> result = documentService.searchByKeyword("test", pageable);

		// Verify
		assertNotNull(result);
		assertEquals(1, result.getTotalElements());
		assertEquals("Test Document", result.getContent().get(0).getTitle());

		verify(documentRepository).searchDocuments("test", pageable);
	}

	@Test
	public void testUpdateDocument_Success() {
		// Setup
		when(documentRepository.findById(1L)).thenReturn(Optional.of(testDocument));
		when(tagRepository.findByName("test")).thenReturn(Optional.of(testTag));
		when(tagRepository.findByName("document")).thenReturn(Optional.empty());
		when(tagRepository.save(any(Tag.class))).thenReturn(new Tag());
		when(documentRepository.save(any(Document.class))).thenReturn(testDocument);

		// Execute
		Document result = documentService.updateDocument(1L, documentDto);

		// Verify
		assertNotNull(result);
		assertEquals("Updated Document", result.getTitle());
		assertEquals("This is an updated document", result.getDescription());

		verify(documentRepository).findById(1L);
		verify(tagRepository).findByName("test");
		verify(tagRepository).findByName("document");
		verify(documentRepository).save(any(Document.class));
	}

	@Test
	public void testUpdateDocument_NotFound() {
		// Setup
		when(documentRepository.findById(99L)).thenReturn(Optional.empty());

		// Execute & Verify
		assertThrows(ResourceNotFoundException.class, () -> {
			documentService.updateDocument(99L, documentDto);
		});

		verify(documentRepository).findById(99L);
		verify(documentRepository, never()).save(any(Document.class));
	}

	@Test
	public void testDeleteDocument_Success() {
		// Setup
		when(documentRepository.findById(1L)).thenReturn(Optional.of(testDocument));
		doNothing().when(fileStorageService).deleteFile(anyString());
		doNothing().when(documentRepository).delete(any(Document.class));

		// Execute
		documentService.deleteDocument(1L);

		// Verify
		verify(documentRepository).findById(1L);
		verify(fileStorageService).deleteFile(testDocument.getFilePath());
		verify(documentRepository).delete(testDocument);
	}

	@Test
	public void testDeleteDocument_NotFound() {
		// Setup
		when(documentRepository.findById(99L)).thenReturn(Optional.empty());

		// Execute & Verify
		assertThrows(ResourceNotFoundException.class, () -> {
			documentService.deleteDocument(99L);
		});

		verify(documentRepository).findById(99L);
		verify(fileStorageService, never()).deleteFile(anyString());
		verify(documentRepository, never()).delete(any(Document.class));
	}

	@Test
	public void testGetUnprocessedDocuments() {
		// Setup
		List<Document> unprocessedDocs = Collections.singletonList(testDocument);
		when(documentRepository.findByIndexed(false)).thenReturn(unprocessedDocs);

		// Execute
		List<Document> result = documentService.getUnprocessedDocuments();

		// Verify
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals("Test Document", result.get(0).getTitle());

		verify(documentRepository).findByIndexed(false);
	}

	@Test
	public void testProcessDocumentContent() throws IOException {
		// Setup
		when(documentRepository.findById(1L)).thenReturn(Optional.of(testDocument));
		when(fileStorageService.getFilePath(anyString())).thenReturn(Path.of("test/path/document.pdf"));
		when(contentExtractor.extractText(any(Path.class))).thenReturn("Extracted content from document");
		when(contentExtractor.preprocessTextForSearch(anyString())).thenReturn("preprocessed content");
		when(documentRepository.save(any(Document.class))).thenReturn(testDocument);

		// Execute
		documentService.processDocumentContent(1L);

		// Verify
		verify(documentRepository).findById(1L);
		verify(fileStorageService).getFilePath(testDocument.getFilePath());
		verify(contentExtractor).extractText(any(Path.class));
		verify(contentExtractor).preprocessTextForSearch(anyString());
		verify(documentRepository).save(any(Document.class));
	}

	@Test
	public void testProcessDocumentContent_DocumentNotFound() {
		// Setup
		when(documentRepository.findById(99L)).thenReturn(Optional.empty());

		// Execute & Verify
		assertThrows(ResourceNotFoundException.class, () -> {
			documentService.processDocumentContent(99L);
		});

		verify(documentRepository).findById(99L);
		verify(fileStorageService, never()).getFilePath(anyString());
		verify(documentRepository, never()).save(any(Document.class));
	}
}
