package com.example.controller;

import com.example.config.JwtUtils;
import com.example.dto.LoginRequest;
import com.example.dto.SignupRequest;
import com.example.entity.Role;
import com.example.entity.Role.ERole;
import com.example.entity.User;
import com.example.repository.RoleRepository;
import com.example.repository.UserRepository;
import com.example.service.UserDetailsImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
public class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private AuthenticationManager authenticationManager;

	@MockBean
	private UserRepository userRepository;

	@MockBean
	private RoleRepository roleRepository;

	@MockBean
	private PasswordEncoder passwordEncoder;

	@MockBean
	private JwtUtils jwtUtils;

	private LoginRequest validLoginRequest;
	private SignupRequest validSignupRequest;
	private User validUser;
	private Role viewerRole;
	private Role editorRole;
	private Role adminRole;
	private Authentication authentication;
	private UserDetailsImpl userDetails;

	@BeforeEach
	public void setup() {
		// Initialize valid login request
		validLoginRequest = new LoginRequest();
		validLoginRequest.setUsername("testuser");
		validLoginRequest.setPassword("password123");

		// Initialize valid signup request
		validSignupRequest = new SignupRequest();
		validSignupRequest.setUsername("newuser");
		validSignupRequest.setEmail("newuser@example.com");
		validSignupRequest.setPassword("password123");
		Set<String> roles = new HashSet<>();
		roles.add("viewer");
		validSignupRequest.setRoles(roles);

		// Initialize valid user
		validUser = new User();
		validUser.setId(1L);
		validUser.setUsername("testuser");
		validUser.setEmail("testuser@example.com");
		validUser.setPassword("encodedPassword");

		// Initialize roles
		viewerRole = new Role();
		viewerRole.setId(1);
		viewerRole.setName(ERole.ROLE_VIEWER);

		editorRole = new Role();
		editorRole.setId(2);
		editorRole.setName(ERole.ROLE_EDITOR);

		adminRole = new Role();
		adminRole.setId(3);
		adminRole.setName(ERole.ROLE_ADMIN);

		// Initialize user details
		List<SimpleGrantedAuthority> authorities = new ArrayList<>();
		authorities.add(new SimpleGrantedAuthority("ROLE_VIEWER"));
		userDetails = new UserDetailsImpl(1L, "testuser", "testuser@example.com", "encodedPassword", authorities);

		// Initialize authentication
		authentication = mock(Authentication.class);
		when(authentication.getPrincipal()).thenReturn(userDetails);
	}

	@Test
	public void testAuthenticateUser_Success() throws Exception {
		// Setup
		when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenReturn(authentication);
		when(jwtUtils.generateJwtToken(authentication)).thenReturn("mocked.jwt.token");
		when(authentication.getPrincipal()).thenReturn(userDetails);

		// Execute and Verify
		mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(validLoginRequest))).andExpect(status().isOk())
				.andExpect(jsonPath("$.token", is("mocked.jwt.token")))
				.andExpect(jsonPath("$.username", is("testuser")))
				.andExpect(jsonPath("$.email", is("testuser@example.com")))
				.andExpect(jsonPath("$.roles[0]", is("ROLE_VIEWER")));

		verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
		verify(jwtUtils).generateJwtToken(any(Authentication.class));
	}

	@Test
	public void testRegisterUser_Success() throws Exception {
		// Setup
		when(userRepository.existsByUsername(anyString())).thenReturn(false);
		when(userRepository.existsByEmail(anyString())).thenReturn(false);
		when(roleRepository.findByName(ERole.ROLE_VIEWER)).thenReturn(Optional.of(viewerRole));
		when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
		when(userRepository.save(any(User.class))).thenReturn(validUser);

		// Execute and Verify
		mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(validSignupRequest))).andExpect(status().isOk())
				.andExpect(jsonPath("$.message", is("User registered successfully!")));

		verify(userRepository).existsByUsername(validSignupRequest.getUsername());
		verify(userRepository).existsByEmail(validSignupRequest.getEmail());
		verify(roleRepository).findByName(ERole.ROLE_VIEWER);
		verify(passwordEncoder).encode(validSignupRequest.getPassword());
		verify(userRepository).save(any(User.class));
	}

	@Test
	public void testRegisterUser_UsernameAlreadyExists() throws Exception {
		// Setup
		when(userRepository.existsByUsername(anyString())).thenReturn(true);

		// Execute and Verify
		mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(validSignupRequest))).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message", is("Error: Username is already taken!")));

		verify(userRepository).existsByUsername(validSignupRequest.getUsername());
		verify(userRepository, never()).save(any(User.class));
	}

	@Test
	public void testRegisterUser_EmailAlreadyExists() throws Exception {
		// Setup
		when(userRepository.existsByUsername(anyString())).thenReturn(false);
		when(userRepository.existsByEmail(anyString())).thenReturn(true);

		// Execute and Verify
		mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(validSignupRequest))).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message", is("Error: Email is already in use!")));

		verify(userRepository).existsByUsername(validSignupRequest.getUsername());
		verify(userRepository).existsByEmail(validSignupRequest.getEmail());
		verify(userRepository, never()).save(any(User.class));
	}

	@Test
	public void testRegisterUser_WithAdminRole() throws Exception {
		// Setup
		Set<String> roles = new HashSet<>();
		roles.add("admin");
		validSignupRequest.setRoles(roles);

		when(userRepository.existsByUsername(anyString())).thenReturn(false);
		when(userRepository.existsByEmail(anyString())).thenReturn(false);
		when(roleRepository.findByName(ERole.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));
		when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
		when(userRepository.save(any(User.class))).thenReturn(validUser);

		// Execute and Verify
		mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(validSignupRequest))).andExpect(status().isOk())
				.andExpect(jsonPath("$.message", is("User registered successfully!")));

		verify(roleRepository).findByName(ERole.ROLE_ADMIN);
		verify(userRepository).save(any(User.class));
	}

	@Test
	public void testLogoutUser() throws Exception {
		// Execute and Verify
		mockMvc.perform(post("/api/auth/logout")).andExpect(status().isOk())
				.andExpect(jsonPath("$.message", is("User logged out successfully!")));
	}
}
