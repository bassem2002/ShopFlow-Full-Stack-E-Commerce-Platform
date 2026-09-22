package com.shopflow.shopflow.service;

import com.shopflow.shopflow.dto.request.ProductRequest;
import com.shopflow.shopflow.dto.response.ProductResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductService {

    ProductResponse createProduct(ProductRequest request);

    ProductResponse updateProduct(Long id, ProductRequest request);

    void softDeleteProduct(Long id);

    ProductResponse getProductById(Long id);

    Page<ProductResponse> getProducts(Long categoryId,
                                      Double minPrice,
                                      Double maxPrice,
                                      Long sellerId,
                                      Boolean promo,
                                      int page,
                                      int size,
                                      String sortBy,
                                      String sortDirection);

    Page<ProductResponse> searchProducts(String q, int page, int size);

    List<ProductResponse> getTopSellingProducts();
}