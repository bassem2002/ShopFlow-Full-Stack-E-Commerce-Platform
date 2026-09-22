package com.shopflow.shopflow.service;

import com.shopflow.shopflow.dto.request.CategoryRequest;
import com.shopflow.shopflow.dto.response.CategoryResponse;

import java.util.List;

/*
 * Interface = contrat métier.
 *
 * Le controller dépend de cette abstraction,
 * pas directement de l'implémentation.
 *
 * C'est une bonne pratique SOLID.
 */
public interface CategoryService {

    CategoryResponse createCategory(CategoryRequest request);

    CategoryResponse updateCategory(Long id, CategoryRequest request);

    List<CategoryResponse> getAllCategories();

    List<CategoryResponse> getCategoryTree();

    CategoryResponse getCategoryById(Long id);

    void deleteCategory(Long id);
}