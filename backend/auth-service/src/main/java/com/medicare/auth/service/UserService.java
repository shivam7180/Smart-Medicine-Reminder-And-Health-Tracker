package com.medicare.auth.service;

import com.medicare.auth.dto.UpdateProfileRequest;
import com.medicare.auth.dto.UserProfileResponse;

import java.util.List;

public interface UserService {
    UserProfileResponse getUserProfile(Long userId);
    UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request);
    List<UserProfileResponse> getAllUsers();
    void toggleUserStatus(Long userId, boolean active);
}
