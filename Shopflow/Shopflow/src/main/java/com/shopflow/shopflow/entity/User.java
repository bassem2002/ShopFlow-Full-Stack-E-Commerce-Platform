package com.shopflow.shopflow.entity;



import com.shopflow.shopflow.enums.Role;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import lombok.*;

import java.time.LocalDateTime;

/*
 * @Entity=> Dit à JPA/Hibernate que cette classe correspond à une table en base de données.
 */
@Entity

/*
 * @Table(name = "users")=> Permet de préciser le nom exact de la table.
 */
@Table(name = "users")

/*
 * Lombok :
 * @Getter / @Setter : génère automatiquement getters et setters
 * @NoArgsConstructor : constructeur vide
 * @AllArgsConstructor : constructeur avec tous les champs
 * @Builder : permet de construire un objet avec le pattern Builder
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    /*
     * @Id=>  Indique la clé primaire de la table.
     */
    @Id

    /*
     * @GeneratedValue(strategy = GenerationType.IDENTITY)
     * Laisse la base générer automatiquement l'id.
     * Très utilisé avec PostgreSQL.
     */
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * @Column(...)
     * Configure la colonne dans la base.
     * unique = true  -> email doit être unique
     * nullable = false -> colonne obligatoire
     */
    @Column(unique = true)
    private String firebaseUid;

    @Column(unique = true, nullable = false)
    private String email;

    /*
     * Mot de passe de l'utilisateur.=>  En vrai projet, il faut le stocker hashé, jamais en clair.
     */
    @Column(nullable = false)
    private String password;

    private String firstName;
    private String lastName;

    /*
     * @Enumerated(EnumType.STRING)
     * Stocke l'enum comme texte en base ("ADMIN", "SELLER"...)
     * C'est mieux que ORDINAL (0,1,2) car plus lisible et plus sûr.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    /*
     * active = permet d'activer ou désactiver le compte
     */
    @Column(nullable = false)
    private Boolean active;

    /*
     * Date de création du compte
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active != null ? active : true;
    }
}