package com.shopflow.shopflow.service;

import com.shopflow.shopflow.dto.request.LoginRequest;
import com.shopflow.shopflow.dto.request.LogoutRequest;
import com.shopflow.shopflow.dto.request.RefreshTokenRequest;
import com.shopflow.shopflow.dto.request.RegisterRequest;
import com.shopflow.shopflow.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);

    void logout(LogoutRequest request);

    void forgotPassword(String email);

    void resetPassword(String token, String newPassword);

    AuthResponse loginWithFirebase(String idToken, String role);
}