package com.medicare.auth.service;

import com.medicare.auth.dto.AuthResponse;
import com.medicare.auth.dto.LoginRequest;
import com.medicare.auth.dto.RegisterRequest;
import com.medicare.auth.entity.RefreshToken;
import com.medicare.auth.entity.Role;
import com.medicare.auth.entity.User;
import com.medicare.auth.repository.PasswordResetTokenRepository;
import com.medicare.auth.repository.RoleRepository;
import com.medicare.auth.repository.UserRepository;
import com.medicare.auth.security.JwtTokenProvider;
import com.medicare.auth.security.UserPrincipal;
import com.medicare.auth.service.impl.AuthServiceImpl;
import com.medicare.common.enums.RoleType;
import com.medicare.common.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    private User sampleUser;
    private Role patientRole;

    @BeforeEach
    void setUp() {
        patientRole = Role.builder().id(1L).name(RoleType.ROLE_PATIENT).build();
        sampleUser = User.builder()
                .id(1L)
                .email("john.doe@medicare.com")
                .password("encoded_pass")
                .fullName("John Doe")
                .isActive(true)
                .roles(Set.of(patientRole))
                .build();
    }

    @Test
    @DisplayName("Should successfully authenticate user and return JWT with refresh token")
    void testLoginSuccess() {
        LoginRequest request = new LoginRequest();
        request.setEmail("john.doe@medicare.com");
        request.setPassword("Password@123");

        UserPrincipal principal = UserPrincipal.create(sampleUser);
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(principal);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(tokenProvider.generateToken(auth)).thenReturn("sample_jwt_token");
        when(tokenProvider.getExpirationInMs()).thenReturn(86400000L);

        RefreshToken refreshToken = RefreshToken.builder().token("sample_refresh_token").build();
        when(refreshTokenService.createRefreshToken(1L)).thenReturn(refreshToken);

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("sample_jwt_token", response.getAccessToken());
        assertEquals("sample_refresh_token", response.getRefreshToken());
        assertEquals("john.doe@medicare.com", response.getEmail());
    }

    @Test
    @DisplayName("Should throw BadRequestException when registering an already registered email")
    void testRegisterDuplicateEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("john.doe@medicare.com");
        request.setFullName("John Duplicate");
        request.setPassword("Password@123");

        when(userRepository.existsByEmail("john.doe@medicare.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }
}
