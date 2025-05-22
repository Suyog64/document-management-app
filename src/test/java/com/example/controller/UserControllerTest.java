package com.example.controller;

import com.example.entity.Role;
import com.example.entity.Role.ERole;
import com.example.entity.User;
import com.example.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private UserRepository userRepository;

	private User testUser;
	private User adminUser;
	private List<User> userList;

	@BeforeEach
	public void setup() {
		// Initialize test user
		testUser = new User();
		testUser.setId(1L);
		testUser.setUsername("testuser");
		testUser.setEmail("testuser@example.com");
		testUser.setPassword("encodedPassword");

		Set<Role> viewerRoles = new HashSet<>();
		Role viewerRole = new Role();
		viewerRole.setId(1);
		viewerRole.setName(ERole.ROLE_VIEWER);
		viewerRoles.add(viewerRole);
		testUser.setRoles(viewerRoles);

		// Initialize admin user
		adminUser = new User();
		adminUser.setId(2L);
		adminUser.setUsername("adminuser");
		adminUser.setEmail("admin@example.com");
		adminUser.setPassword("encodedPassword");

		Set<Role> adminRoles = new HashSet<>();
		Role adminRole = new Role();
		adminRole.setId(3);
		adminRole.setName(ERole.ROLE_ADMIN);
		adminRoles.add(adminRole);
		adminUser.setRoles(adminRoles);

		// Initialize user list
		userList = Arrays.asList(testUser, adminUser);
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	public void testGetAllUsers() throws Exception {
		// Setup
		when(userRepository.findAll()).thenReturn(userList);

		// Execute and Verify
		mockMvc.perform(get("/api/users")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].username", is("testuser")))
				.andExpect(jsonPath("$[1].username", is("adminuser")))
				// Password should be masked
				.andExpect(jsonPath("$[0].password").doesNotExist());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	public void testGetUserById_Success() throws Exception {
		// Setup
		when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

		// Execute and Verify
		mockMvc.perform(get("/api/users/1")).andExpect(status().isOk()).andExpect(jsonPath("$.id", is(1)))
				.andExpect(jsonPath("$.username", is("testuser")))
				.andExpect(jsonPath("$.email", is("testuser@example.com")))
				// Password should be masked
				.andExpect(jsonPath("$.password").doesNotExist());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	public void testGetUserById_NotFound() throws Exception {
		// Setup
		when(userRepository.findById(99L)).thenReturn(Optional.empty());

		// Execute and Verify
		mockMvc.perform(get("/api/users/99")).andExpect(status().isNotFound());
	}

	@Test
	@WithMockUser(username = "testuser", roles = "VIEWER")
	public void testGetUserProfile() throws Exception {
		// Setup
		when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

		// Execute and Verify
		mockMvc.perform(get("/api/users/profile")).andExpect(status().isOk())
				.andExpect(jsonPath("$.username", is("testuser")))
				.andExpect(jsonPath("$.email", is("testuser@example.com")))
				// Password should be masked
				.andExpect(jsonPath("$.password").doesNotExist());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	public void testDeleteUser_Success() throws Exception {
		// Setup
		when(userRepository.existsById(1L)).thenReturn(true);
		doNothing().when(userRepository).deleteById(1L);

		// Execute and Verify
		mockMvc.perform(delete("/api/users/1")).andExpect(status().isOk())
				.andExpect(jsonPath("$.message", is("User deleted successfully")));

		verify(userRepository).deleteById(1L);
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	public void testDeleteUser_NotFound() throws Exception {
		// Setup
		when(userRepository.existsById(99L)).thenReturn(false);

		// Execute and Verify
		mockMvc.perform(delete("/api/users/99")).andExpect(status().isNotFound());

		verify(userRepository, never()).deleteById(anyLong());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	public void testUpdateUserRole() throws Exception {
		// Setup
		List<String> roles = Arrays.asList("admin", "editor");

		// Execute and Verify
		mockMvc.perform(put("/api/users/1/role").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(roles))).andExpect(status().isOk())
				.andExpect(jsonPath("$.message", is("Roles updated successfully")));
	}
}
