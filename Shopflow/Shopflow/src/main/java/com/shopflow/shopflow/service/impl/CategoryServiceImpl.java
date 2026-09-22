package com.shopflow.shopflow.service.impl;

import com.shopflow.shopflow.dto.request.CategoryRequest;
import com.shopflow.shopflow.dto.response.CategoryResponse;
import com.shopflow.shopflow.entity.Category;
import com.shopflow.shopflow.exception.BusinessException;
import com.shopflow.shopflow.repository.CategoryRepository;
import com.shopflow.shopflow.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/*
 * @Service indique que cette classe contient la logique métier.
 *
 * @RequiredArgsConstructor génère automatiquement un constructeur
 * avec tous les champs final.
 *
 * Spring injecte ensuite CategoryRepository automatiquement.
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public CategoryResponse createCategory(CategoryRequest request) {

        /*
         * Vérification métier :
         * le nom doit être unique.
         */
        if (categoryRepository.existsByName(request.getName())) {
            throw new BusinessException("A category with this name already exists");
        }

        /*
         * Si parentId est fourni, on récupère la catégorie parent.
         * Sinon, la catégorie sera racine.
         */
        Category parent = null;
        if (request.getParentId() != null) {
            parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new BusinessException("Parent category not found"));
        }

        /*
         * Construction de l'entité à partir du DTO.
         */
        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .parent(parent)
                .build();

        /*
         * Sauvegarde en base.
         */
        Category savedCategory = categoryRepository.save(category);

        /*
         * Conversion Entity -> DTO Response
         */
        return mapToResponse(savedCategory);
    }

    @Override
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {

        /*
         * On récupère la catégorie à modifier.
         */
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Category not found"));

        /*
         * Si le nom change, on vérifie qu'il n'existe pas déjà.
         */
        if (!existingCategory.getName().equals(request.getName())
                && categoryRepository.existsByName(request.getName())) {
            throw new BusinessException("A category with this name already exists");
        }

        /*
         * Gestion du parent :
         * - null => devient racine
         * - sinon, on charge le parent
         */
        Category parent = null;
        if (request.getParentId() != null) {
            parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new BusinessException("Parent category not found"));

            /*
             * Protection importante :
             * une catégorie ne peut pas être son propre parent.
             */
            if (parent.getId().equals(existingCategory.getId())) {
                throw new BusinessException("A category cannot be its own parent");
            }
        }

        existingCategory.setName(request.getName());
        existingCategory.setDescription(request.getDescription());
        existingCategory.setImageUrl(request.getImageUrl());
        existingCategory.setParent(parent);

        Category updatedCategory = categoryRepository.save(existingCategory);

        return mapToResponse(updatedCategory);
    }

    @Override
    public List<CategoryResponse> getAllCategories() {

        /*
         * Retourne toutes les catégories à plat.
         */
        return categoryRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<CategoryResponse> getCategoryTree() {

        /*
         * On récupère seulement les catégories racines,
         * puis on construit l'arbre récursivement.
         */
        return categoryRepository.findByParentIsNull()
                .stream()
                .map(this::mapToTreeResponse)
                .toList();
    }

    @Override
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Category not found"));

        return mapToResponse(category);
    }

    @Override
    public void deleteCategory(Long id) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Category not found"));

        /*
         * Bonne règle métier :
         * on empêche la suppression d'une catégorie
         * si elle possède des sous-catégories.
         *
         * Sinon, on risquerait de casser la hiérarchie.
         */
        if (category.getChildren() != null && !category.getChildren().isEmpty()) {
            throw new BusinessException("Cannot delete a category that has sub-categories");
        }

        categoryRepository.delete(category);
    }

    /*
     * Méthode utilitaire pour convertir une Entity en DTO simple.
     *
     * Ici on ne renvoie pas les enfants.
     */
    private CategoryResponse mapToResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .imageUrl(category.getImageUrl())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .build();
    }

    /*
     * Méthode récursive pour construire un arbre.
     *
     * Si la catégorie a des enfants,
     * on les convertit aussi en CategoryResponse.
     */
    private CategoryResponse mapToTreeResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .imageUrl(category.getImageUrl())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .children(
                        category.getChildren()
                                .stream()
                                .map(this::mapToTreeResponse)
                                .toList()
                )
                .build();
    }
}