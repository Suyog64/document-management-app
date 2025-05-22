package com.example.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.example.entity.Role;
import com.example.entity.Role.ERole;
import com.example.entity.User;
import com.example.repository.UserRepository;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserDetailsServiceImplTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private UserDetailsServiceImpl userDetailsService;

	private User testUser;

	@BeforeEach
	public void setup() {
		// Initialize test user
		testUser = new User();
		testUser.setId(1L);
		testUser.setUsername("testuser");
		testUser.setEmail("testuser@example.com");
		testUser.setPassword("password123");

		// Add roles to test user
		Set<Role> roles = new HashSet<>();
		Role viewerRole = new Role();
		viewerRole.setId(1);
		viewerRole.setName(ERole.ROLE_VIEWER);
		roles.add(viewerRole);
		testUser.setRoles(roles);
	}

	@Test
	public void testLoadUserByUsername_Success() {
		// Setup
		when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

		// Execute
		UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

		// Verify
		assertNotNull(userDetails);
		assertEquals("testuser", userDetails.getUsername());
		assertEquals("password123", userDetails.getPassword());
		assertEquals(1, userDetails.getAuthorities().size());

		verify(userRepository).findByUsername("testuser");
	}

	@Test
	public void testLoadUserByUsername_UserNotFound() {
		// Setup
		when(userRepository.findByUsername("nonexistentuser")).thenReturn(Optional.empty());

		// Execute & Verify
		assertThrows(UsernameNotFoundException.class, () -> {
			userDetailsService.loadUserByUsername("nonexistentuser");
		});

		verify(userRepository).findByUsername("nonexistentuser");
	}

	@Test
	public void testUserDetailsImpl_Equals() {
		// Setup
		UserDetailsImpl userDetails1 = UserDetailsImpl.build(testUser);
		UserDetailsImpl userDetails2 = UserDetailsImpl.build(testUser);

		// Verify
		assertEquals(userDetails1, userDetails2);
		assertEquals(userDetails1.hashCode(), userDetails2.hashCode());
	}

	@Test
	public void testUserDetailsImpl_NotEquals() {
		// Setup
		UserDetailsImpl userDetails1 = UserDetailsImpl.build(testUser);

		User anotherUser = new User();
		anotherUser.setId(2L);
		anotherUser.setUsername("anotheruser");
		anotherUser.setEmail("another@example.com");
		anotherUser.setPassword("password456");
		anotherUser.setRoles(testUser.getRoles());

		UserDetailsImpl userDetails2 = UserDetailsImpl.build(anotherUser);

		// Verify
		assertNotEquals(userDetails1, userDetails2);
	}

	@Test
	public void testUserDetailsImpl_AccountMethods() {
		// Setup
		UserDetailsImpl userDetails = UserDetailsImpl.build(testUser);

		// Verify
		assertTrue(userDetails.isAccountNonExpired());
		assertTrue(userDetails.isAccountNonLocked());
		assertTrue(userDetails.isCredentialsNonExpired());
		assertTrue(userDetails.isEnabled());
	}
}
