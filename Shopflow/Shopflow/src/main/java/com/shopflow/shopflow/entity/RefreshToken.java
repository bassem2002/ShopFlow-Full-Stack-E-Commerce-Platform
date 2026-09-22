package com.shopflow.shopflow.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/*
 * Cette table stocke les refresh tokens.
 *
 * Pourquoi ?
 * Parce qu'au logout, on doit pouvoir invalider un refresh token.
 * Si on ne stocke rien, on ne peut pas vraiment "logout".
 */
@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * Token unique.
     */
    @Column(nullable = false, unique = true, length = 500)
    private String token;

    /*
     * Plusieurs refresh tokens peuvent appartenir au même user
     * si plus tard tu veux permettre plusieurs sessions/appareils.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /*
     * Date d'expiration du refresh token.
     */
    @Column(nullable = false)
    private LocalDateTime expiryDate;

    /*
     * Permet l'invalidation logique.
     */
    @Column(nullable = false)
    private Boolean revoked;
}