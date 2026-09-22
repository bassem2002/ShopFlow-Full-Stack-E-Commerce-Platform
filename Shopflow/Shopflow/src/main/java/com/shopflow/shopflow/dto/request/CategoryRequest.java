package com.shopflow.shopflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/*
 * DTO utilisé pour recevoir les données envoyées par le client
 * lors de la création ou de la modification d'une catégorie.
 *
 * Bonne pratique :
 * on ne reçoit pas directement l'Entity Category dans le controller.
 */
@Data
public class CategoryRequest {

    /*
     * Nom obligatoire
     */
    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name must not exceed 100 characters")
    private String name;

    /*
     * Description optionnelle
     */
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    private String imageUrl;

    /*
     * parentId est optionnel :
     * - null => catégorie racine
     * - valeur => sous-catégorie
     */
    private Long parentId;
}