package com.shopflow.shopflow.repository;

import com.shopflow.shopflow.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/*
 * UserRepository hérite de JpaRepository<User, Long>
 *
 * Cela donne automatiquement beaucoup de méthodes :
 * - save()
 * - findById()
 * - findAll()
 * - deleteById()
 * etc.
 *
 * User  = type de l'entité
 * Long  = type de la clé primaire
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /*
     * Spring Data JPA comprend le nom de la méthode
     * et génère automatiquement la requête SQL/JPA.
     *
     * findByEmail -> cherche un utilisateur par email
     *
     * Optional<User> est une bonne pratique :
     * cela évite les nulls bruts
     */
    Optional<User> findByEmail(String email);
    /*
     * existsByEmail -> retourne true si email existe déjà
     * utile pour empêcher les doublons à l'inscription
     */
    boolean existsByEmail(String email);
}