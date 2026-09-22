package com.shopflow.shopflow.dto.response;

import lombok.Builder;
import lombok.Data;

/*
 * DTO retourné au client après register/login
 * Bonne pratique :
 * retourner seulement ce qui est utile au frontend
 * et jamais des données sensibles comme le mot de passe.
 */
@Data
@Builder
public class AuthResponse {

    /*
     * Plus tard, ici on mettra le vrai JWT.
     */
    private String accessToken;
    private String refreshToken;
    private String email;
    private String role;
    private Long userId;
}
