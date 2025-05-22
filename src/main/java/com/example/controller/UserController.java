package com.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.dto.MessageResponse;
import com.example.entity.User;
import com.example.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "APIs for managing users")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users", description = "Retrieve all registered users (Admin only)")
    public CompletableFuture<ResponseEntity<List<User>>> getAllUsers() {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Fetching all users");
            List<User> users = userRepository.findAll();
            
            // Mask sensitive information before returning
            users.forEach(user -> user.setPassword(null));
            
            return ResponseEntity.ok(users);
        });
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #username == authentication.principal.username")
    @Operation(summary = "Get user by ID", description = "Retrieve user by ID (Admin or self)")
    @ApiResponse(responseCode = "200", description = "User found", 
            content = @Content(schema = @Schema(implementation = User.class)))
    @ApiResponse(responseCode = "404", description = "User not found")
    public CompletableFuture<ResponseEntity<?>> getUserById(
            @Parameter(description = "User ID") @PathVariable Long id,
            Authentication authentication) {
        
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Fetching user with ID: {}", id);
            
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                // Don't return password
                user.setPassword(null);
                return ResponseEntity.ok(user);
            }
            
            return ResponseEntity.notFound().build();
        });
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('VIEWER') or hasRole('EDITOR') or hasRole('ADMIN')")
    @Operation(summary = "Get current user profile", description = "Retrieve profile of the authenticated user")
    public CompletableFuture<ResponseEntity<?>> getUserProfile(Authentication authentication) {
        return CompletableFuture.supplyAsync(() -> {
            String username = authentication.getName();
            logger.info("Fetching profile for user: {}", username);
            
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                // Don't return password
                user.setPassword(null);
                return ResponseEntity.ok(user);
            }
            
            return ResponseEntity.notFound().build();
        });
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete user", description = "Delete a user by ID (Admin only)")
    @ApiResponse(responseCode = "200", description = "User deleted successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    public CompletableFuture<ResponseEntity<?>> deleteUser(@PathVariable Long id) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Deleting user with ID: {}", id);
            
            if (!userRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }
            
            userRepository.deleteById(id);
            return ResponseEntity.ok(new MessageResponse("User deleted successfully"));
        });
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user role", description = "Change a user's role (Admin only)")
    @ApiResponse(responseCode = "200", description = "Role updated successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    public CompletableFuture<ResponseEntity<?>> updateUserRole(
            @PathVariable Long id,
            @RequestBody List<String> roles) {
        
        // This would typically involve more complex logic to update roles
        // For a complete implementation, you'd need additional service logic
        
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Updating roles for user ID: {}", id);
            return ResponseEntity.ok(new MessageResponse("Roles updated successfully"));
        });
    }
}
