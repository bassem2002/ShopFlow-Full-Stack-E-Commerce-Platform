package com.shopflow.shopflow.controller;

import com.shopflow.shopflow.dto.request.LoginRequest;
import com.shopflow.shopflow.dto.request.LogoutRequest;
import com.shopflow.shopflow.dto.request.RefreshTokenRequest;
import com.shopflow.shopflow.dto.request.RegisterRequest;
import com.shopflow.shopflow.dto.response.AuthResponse;
import com.shopflow.shopflow.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/*
 * @RestController
 * Combine :
 * - @Controller
 * - @ResponseBody
 *
 * Cela signifie que les méthodes retournent du JSON directement.
 */
@RestController

/*
 * Préfixe commun pour tous les endpoints de ce controller.
 */
@RequestMapping("/api/auth")

@RequiredArgsConstructor
public class AuthController {

    /*
     * On dépend de l'interface, pas de l'implémentation.
     * Très bonne pratique.
     */
    private final AuthService authService;

    /*
     * POST /api/auth/register
     *
     * @RequestBody -> lit le JSON envoyé par le client
     * @Valid -> déclenche la validation des annotations du DTO
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/firebase")
    public ResponseEntity<AuthResponse> loginWithFirebase(@RequestBody com.shopflow.shopflow.dto.request.FirebaseLoginRequest request) {
        AuthResponse response = authService.loginWithFirebase(request.getIdToken(), request.getRole());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestParam String email) {
        authService.forgotPassword(email);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        authService.resetPassword(token, newPassword);
        return ResponseEntity.ok().build();
    }
}
