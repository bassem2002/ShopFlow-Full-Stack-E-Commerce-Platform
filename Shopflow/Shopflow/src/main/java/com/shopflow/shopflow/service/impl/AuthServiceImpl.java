package com.shopflow.shopflow.service.impl;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;

import com.shopflow.shopflow.dto.request.LoginRequest;
import com.shopflow.shopflow.dto.request.LogoutRequest;
import com.shopflow.shopflow.dto.request.RefreshTokenRequest;
import com.shopflow.shopflow.dto.request.RegisterRequest;
import com.shopflow.shopflow.dto.response.AuthResponse;
import com.shopflow.shopflow.entity.RefreshToken;
import com.shopflow.shopflow.entity.SellerProfile;
import com.shopflow.shopflow.entity.User;
import com.shopflow.shopflow.enums.Role;
import com.shopflow.shopflow.exception.BusinessException;
import com.shopflow.shopflow.repository.CartRepository;
import com.shopflow.shopflow.repository.RefreshTokenRepository;
import com.shopflow.shopflow.repository.PasswordResetTokenRepository;
import com.shopflow.shopflow.repository.SellerProfileRepository;
import com.shopflow.shopflow.repository.UserRepository;
import com.shopflow.shopflow.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.shopflow.shopflow.entity.Cart;
import com.shopflow.shopflow.entity.PasswordResetToken;
import com.shopflow.shopflow.repository.CartRepository;
import com.shopflow.shopflow.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final SellerProfileRepository sellerProfileRepository;
    private final CartRepository cartRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    
    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Cet email est déjà utilisé.");
        }

        /*
         * Si aucun rôle n'est envoyé, on met CUSTOMER par défaut.
         */
        Role role = request.getRole() != null ? request.getRole() : Role.CUSTOMER;

        /*
         * Pour une inscription publique, on interdit ADMIN.
         */
        if (role == Role.ADMIN) {
            throw new BusinessException("ADMIN role cannot be assigned during public registration");
        }

        /*
         * Si le rôle est SELLER, le nom de boutique devient obligatoire.
         */
        if (role == Role.SELLER && (request.getStoreName() == null || request.getStoreName().isBlank())) {
            throw new BusinessException("Store name is required for seller registration");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))

                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(role)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);
        if (role == Role.CUSTOMER) {
            Cart cart = Cart.builder()
                    .customer(savedUser)
                    .updatedAt(LocalDateTime.now())
                    .build();
            cartRepository.save(cart);
        }
        /*
         * Si l'utilisateur est vendeur, on crée son profil vendeur.
         */
        if (role == Role.SELLER) {
            SellerProfile sellerProfile = SellerProfile.builder()
                    .user(savedUser)
                    .storeName(request.getStoreName())
                    .description(request.getStoreDescription())
                    .logo(request.getStoreLogo())
                    .rating(0.0)
                    .build();

            sellerProfileRepository.save(sellerProfile);
        }

        String accessToken = generateAccessToken(savedUser);
        String refreshToken = createAndSaveRefreshToken(savedUser);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .email(savedUser.getEmail())
                .role(savedUser.getRole().name())
                .userId(savedUser.getId())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("User not found"));

        if (!user.getActive()) {
            throw new BusinessException("Account is disabled");
        }

        String accessToken = generateAccessToken(user);
        String refreshToken = createAndSaveRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .email(user.getEmail())
                .role(user.getRole().name())
                .userId(user.getId())
                .build();
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new BusinessException("Refresh token not found"));

        if (Boolean.TRUE.equals(storedToken.getRevoked())) {
            throw new BusinessException("Refresh token is revoked");
        }

        if (storedToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Refresh token is expired");
        }

        User user = storedToken.getUser();

        String newAccessToken = generateAccessToken(user);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(storedToken.getToken())
                .email(user.getEmail())
                .role(user.getRole().name())
                .userId(user.getId())
                .build();
    }

    @Override
    public void logout(LogoutRequest request) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new BusinessException("Refresh token not found"));

        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            // Return silently to prevent user enumeration
            return;
        }

        // Supprimer l'ancien token s'il existe
        passwordResetTokenRepository.deleteByUser(user);

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(2))
                .build();

        passwordResetTokenRepository.save(resetToken);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new BusinessException("Invalid or expired token"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            passwordResetTokenRepository.delete(resetToken);
            throw new BusinessException("Token expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        passwordResetTokenRepository.delete(resetToken);
    }

    @Override
    public AuthResponse loginWithFirebase(String idToken, String requestedRole) {
        if (com.google.firebase.FirebaseApp.getApps().isEmpty()) {
            throw new BusinessException("Firebase authentication is not configured on this server.");
        }
        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            String email = decodedToken.getEmail();
            String uid = decodedToken.getUid();

            User user = userRepository.findByEmail(email).orElse(null);

            if (user == null) {
                // Determine role: use requestedRole if valid, else default to CUSTOMER
                Role role = Role.CUSTOMER;
                if (requestedRole != null) {
                    try {
                        role = Role.valueOf(requestedRole.toUpperCase());
                    } catch (IllegalArgumentException ignored) {}
                }

                // Protect against unauthorized ADMIN creation
                if (role == Role.ADMIN) role = Role.CUSTOMER;

                user = User.builder()
                        .email(email)
                        .firebaseUid(uid)
                        .firstName((String) decodedToken.getClaims().getOrDefault("name", ""))
                        .lastName("")
                        .role(role)
                        .active(true)
                        .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                        .createdAt(LocalDateTime.now())
                        .build();

                user = userRepository.save(user);

                if (role == Role.CUSTOMER) {
                    Cart cart = Cart.builder()
                            .customer(user)
                            .updatedAt(LocalDateTime.now())
                            .build();
                    cartRepository.save(cart);
                } else if (role == Role.SELLER) {
                    SellerProfile sellerProfile = SellerProfile.builder()
                            .user(user)
                            .storeName("Boutique de " + (user.getFirstName() != null ? user.getFirstName() : email))
                            .rating(0.0)
                            .build();
                    sellerProfileRepository.save(sellerProfile);
                }
            }

            // Update firebaseUid if not set
            if (user.getFirebaseUid() == null) {
                user.setFirebaseUid(uid);
                userRepository.save(user);
            }

            String accessToken = generateAccessToken(user);
            String refreshToken = createAndSaveRefreshToken(user);

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .email(user.getEmail())
                    .role(user.getRole().name())
                    .userId(user.getId())
                    .build();

        } catch (Exception e) {
            throw new BusinessException("Firebase authentication failed: " + e.getMessage());
        }
    }

    private String generateAccessToken(User user) {
        return jwtService.generateToken(user);
    }

    private String createAndSaveRefreshToken(User user) {
        String tokenValue = "refresh-token-" + UUID.randomUUID();

        RefreshToken refreshToken = RefreshToken.builder()
                .token(tokenValue)
                .user(user)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);

        return tokenValue;
    }
}