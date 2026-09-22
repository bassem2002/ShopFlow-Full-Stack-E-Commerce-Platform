package com.shopflow.shopflow.entity;

import jakarta.persistence.*;
import lombok.*;

/*
 * Profil vendeur.
 *
 * L'énoncé demande :
 * - nom boutique
 * - logo
 * - description
 *
 * Ce profil est lié à un User ayant le rôle SELLER.
 */
@Entity
@Table(name = "seller_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * Relation OneToOne :
     * un vendeur possède un seul profil vendeur
     * et un profil vendeur appartient à un seul user.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, length = 150)
    private String storeName;

    @Column(length = 500)
    private String description;

    @Column(length = 500)
    private String logo;

    /*
     * Note moyenne du vendeur.
     * Pour l'instant initialisée à 0.
     */
    @Column(nullable = false)
    private Double rating;
}