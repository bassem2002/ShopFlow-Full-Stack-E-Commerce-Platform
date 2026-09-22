package com.shopflow.shopflow.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/*
 * DTO retourné au client.
 *
 * On expose seulement les données utiles pour l'API,
 * pas l'Entity brute.
 */
@Data
@Builder
public class CategoryResponse {

    private Long id;
    private String name;
    private String description;
    private String imageUrl;

    /*
     * On renvoie seulement l'id du parent
     * pour éviter des boucles infinies JSON.
     */
    private Long parentId;

    /*
     * Pour afficher un arbre de catégories,
     * on peut inclure les enfants en récursif.
     */
    @Builder.Default
    private List<CategoryResponse> children = new ArrayList<>();
}