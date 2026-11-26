package com.medicare.auth.service.impl;

import com.medicare.auth.dto.*;
import com.medicare.auth.entity.PasswordResetToken;
import com.medicare.auth.entity.RefreshToken;
import com.medicare.auth.entity.Role;
import com.medicare.auth.entity.User;
import com.medicare.auth.repository.PasswordResetTokenRepository;
import com.medicare.auth.repository.RoleRepository;
import com.medicare.auth.repository.UserRepository;
import com.medicare.auth.security.JwtTokenProvider;
import com.medicare.auth.security.UserPrincipal;
import com.medicare.auth.service.AuthService;
import com.medicare.auth.service.RefreshTokenService;
import com.medicare.common.enums.RoleType;
import com.medicare.common.exception.BadRequestException;
import com.medicare.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        String jwt = tokenProvider.generateToken(authentication);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userPrincipal.getId());

        List<String> roles = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return AuthResponse.builder()
                .accessToken(jwt)
                .refreshToken(refreshToken.getToken())
                .userId(userPrincipal.getId())
                .email(userPrincipal.getEmail())
                .fullName(userPrincipal.getFullName())
                .roles(roles)
                .expiresIn(tokenProvider.getExpirationInMs())
                .build();
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Error: Email is already registered!");
        }

        // Determine Role
        RoleType roleType = RoleType.ROLE_PATIENT;
        if (request.getRole() != null) {
            try {
                String normalizedRole = request.getRole().toUpperCase();
                if (!normalizedRole.startsWith("ROLE_")) {
                    normalizedRole = "ROLE_" + normalizedRole;
                }
                roleType = RoleType.valueOf(normalizedRole);
            } catch (IllegalArgumentException e) {
                roleType = RoleType.ROLE_PATIENT;
            }
        }

        Role userRole = roleRepository.findByName(roleType)
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name(RoleType.ROLE_PATIENT)
                        .description("Default Patient Role")
                        .build()));

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .bloodGroup(request.getBloodGroup())
                .address(request.getAddress())
                .emergencyContactName(request.getEmergencyContactName())
                .emergencyContactPhone(request.getEmergencyContactPhone())
                .isActive(true)
                .roles(roles)
                .build();

        User savedUser = userRepository.save(user);

        UserPrincipal userPrincipal = UserPrincipal.create(savedUser);
        String jwt = tokenProvider.generateTokenFromUser(userPrincipal);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(savedUser.getId());

        return AuthResponse.builder()
                .accessToken(jwt)
                .refreshToken(refreshToken.getToken())
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .roles(List.of(userRole.getName().name()))
                .expiresIn(tokenProvider.getExpirationInMs())
                .build();
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    UserPrincipal userPrincipal = UserPrincipal.create(user);
                    String token = tokenProvider.generateTokenFromUser(userPrincipal);
                    RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user.getId());

                    List<String> roles = userPrincipal.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .collect(Collectors.toList());

                    return AuthResponse.builder()
                            .accessToken(token)
                            .refreshToken(newRefreshToken.getToken())
                            .userId(user.getId())
                            .email(user.getEmail())
                            .fullName(user.getFullName())
                            .roles(roles)
                            .expiresIn(tokenProvider.getExpirationInMs())
                            .build();
                })
                .orElseThrow(() -> new BadRequestException("Refresh token is not in database!"));
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .token(token)
                .expiryDate(LocalDateTime.now().plusHours(2))
                .used(false)
                .build();

        passwordResetTokenRepository.save(resetToken);
        log.info("Password reset token generated for user {}: {}", user.getEmail(), token);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new BadRequestException("Invalid password reset token"));

        if (Boolean.TRUE.equals(resetToken.getUsed()) || resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Password reset token has expired or already been used");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
        log.info("Password successfully updated for user {}", user.getEmail());
    }
}
