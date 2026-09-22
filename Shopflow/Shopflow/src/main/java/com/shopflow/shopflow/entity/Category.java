package com.shopflow.shopflow.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/*
 * Cette classe représente la table "categories" dans la base de données.

 * Une catégorie peut avoir :
 * - un parent (ex: Vêtements)
 * - plusieurs sous-catégories enfants (ex: T-shirts, Pantalons)
 *
 * Cela s'appelle une relation auto-référencée :
 * la classe Category est liée à elle-même.
 */
@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    /*
     * Clé primaire de la table.
     * L'id sera généré automatiquement par PostgreSQL.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * Le nom de la catégorie.
     * nullable = false => obligatoire
     * unique = true => pas de doublon global sur le nom
     *
     * Remarque :
     * dans un vrai projet, on pourrait autoriser le même nom
     * dans des branches différentes, mais pour commencer,
     * unique=true simplifie la gestion.
     */
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    /*
     * Description de la catégorie.
     * Champ optionnel.
     */
    @Column(length = 500)
    private String description;

    @Column(length = 500)
    private String imageUrl;

    /*
     * Relation ManyToOne :
     * plusieurs catégories enfants peuvent avoir un même parent.
     *
     * Exemple :
     * - "T-shirts" a pour parent "Vêtements"
     * - "Pantalons" a aussi pour parent "Vêtements"
     *
     * @JoinColumn(name = "parent_id")
     * crée la colonne parent_id dans la table categories.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    /*
     * Relation inverse OneToMany :
     * une catégorie parent peut avoir plusieurs enfants.
     *
     * mappedBy = "parent"
     * signifie que la relation est pilotée par le champ "parent"
     * dans cette même classe.
     *
     * orphanRemoval = false ici, car supprimer un parent
     * ne doit pas supprimer automatiquement tous les enfants
     * sans décision métier explicite.
     *
     * On initialise avec new ArrayList<>() pour éviter les null.
     */
    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Category> children = new ArrayList<>();
}