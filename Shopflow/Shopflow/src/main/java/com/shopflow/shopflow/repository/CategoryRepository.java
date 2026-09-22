package com.shopflow.shopflow.repository;

import com.shopflow.shopflow.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/*
 * Repository = couche d'accès à la base de données.
 *
 * En héritant de JpaRepository<Category, Long>,
 * on récupère automatiquement :
 * - save(...)
 * - findAll()
 * - findById(...)
 * - delete(...)
 * etc.
 */
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /*
     * Cherche une catégorie par son nom.
     * Spring génère automatiquement la requête.
     */
    Optional<Category> findByName(String name);

    /*
     * Vérifie si une catégorie existe déjà par son nom.
     * Très utile pour éviter les doublons.
     */
    boolean existsByName(String name);

    /*
     * Retourne les catégories racines,
     * c'est-à-dire celles qui n'ont pas de parent.
     *
     * parent IS NULL
     */
    List<Category> findByParentIsNull();
}