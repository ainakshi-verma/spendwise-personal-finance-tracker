package com.spendwise;

import com.spendwise.dto.RegisterRequest;
import com.spendwise.dto.UserProfileResponse;
import com.spendwise.entity.User;
import com.spendwise.exception.BadRequestException;
import com.spendwise.exception.DuplicateResourceException;
import com.spendwise.repository.UserRepository;
import com.spendwise.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should successfully register a new user with hashed password")
    void testRegisterSuccess() {
        RegisterRequest request = new RegisterRequest("Alice Student", "alice@test.com", "Password@123", "Password@123");
        UserProfileResponse response = authService.register(request);

        assertNotNull(response.getId());
        assertEquals("Alice Student", response.getName());
        assertEquals("alice@test.com", response.getEmail());

        User saved = userRepository.findByEmailIgnoreCase("alice@test.com").orElse(null);
        assertNotNull(saved);
        assertTrue(passwordEncoder.matches("Password@123", saved.getPassword()));
        assertNotEquals("Password@123", saved.getPassword());
    }

    @Test
    @DisplayName("Should reject registration when password and confirm password do not match")
    void testRegisterPasswordMismatch() {
        RegisterRequest request = new RegisterRequest("Alice Student", "alice@test.com", "Password@123", "Mismatch@456");
        assertThrows(BadRequestException.class, () -> authService.register(request));
    }

    @Test
    @DisplayName("Should reject registration with duplicate email (case-insensitive)")
    void testRegisterDuplicateEmail() {
        RegisterRequest req1 = new RegisterRequest("Alice", "alice@test.com", "Password@123", "Password@123");
        authService.register(req1);

        RegisterRequest req2 = new RegisterRequest("Alice Twin", "ALICE@TEST.COM", "Password@123", "Password@123");
        assertThrows(DuplicateResourceException.class, () -> authService.register(req2));
    }

    @Test
    @DisplayName("Should retrieve current authenticated user from SecurityContext")
    void testGetCurrentUser() {
        RegisterRequest request = new RegisterRequest("Bob Builder", "bob@test.com", "Secret@123", "Secret@123");
        authService.register(request);

        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("bob@test.com", "Secret@123", java.util.Collections.emptyList())
        );

        User current = authService.getCurrentUser();
        assertNotNull(current);
        assertEquals("Bob Builder", current.getName());
        assertEquals("bob@test.com", current.getEmail());
    }
}
