package com.shopflow.shopflow.service.impl;

import com.shopflow.shopflow.dto.request.ProductRequest;
import com.shopflow.shopflow.dto.request.ProductVariantRequest;
import com.shopflow.shopflow.dto.response.ProductResponse;
import com.shopflow.shopflow.dto.response.ProductVariantResponse;
import com.shopflow.shopflow.entity.Category;
import com.shopflow.shopflow.entity.Product;
import com.shopflow.shopflow.entity.ProductVariant;
import com.shopflow.shopflow.entity.User;
import com.shopflow.shopflow.enums.Role;
import com.shopflow.shopflow.exception.BusinessException;
import com.shopflow.shopflow.repository.CategoryRepository;
import com.shopflow.shopflow.repository.ProductRepository;
import com.shopflow.shopflow.repository.UserRepository;
import com.shopflow.shopflow.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Override
    public ProductResponse createProduct(ProductRequest request) {
        User seller = userRepository.findById(request.getSellerId())
                .orElseThrow(() -> new BusinessException("Seller not found"));

        if (seller.getRole() != Role.SELLER && seller.getRole() != Role.ADMIN) {
            throw new BusinessException("Only SELLER or ADMIN can create products");
        }

        List<Category> categories = categoryRepository.findAllById(request.getCategoryIds());
        if (categories.isEmpty()) {
            throw new BusinessException("At least one valid category is required");
        }

        if (categories.size() != request.getCategoryIds().size()) {
            throw new BusinessException("Some categories were not found");
        }

        Product product = Product.builder()
                .seller(seller)
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .promoPrice(request.getPromoPrice())
                .stock(request.getStock())
                .active(true)
                .createdAt(LocalDateTime.now())
                .categories(categories)
                .images(request.getImages())
                .build();

        /*
         * On transforme les DTOs variants en entités ProductVariant
         * puis on les rattache au produit.
         */
        List<ProductVariant> variants = request.getVariants()
                .stream()
                .map(variantRequest -> mapVariantRequestToEntity(variantRequest, product))
                .toList();

        product.setVariants(variants);

        Product savedProduct = productRepository.save(product);

        return mapToResponse(savedProduct);
    }

    @Override
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Product not found"));

        User seller = userRepository.findById(request.getSellerId())
                .orElseThrow(() -> new BusinessException("Seller not found"));

        if (seller.getRole() != Role.SELLER && seller.getRole() != Role.ADMIN) {
            throw new BusinessException("Only SELLER or ADMIN can update products");
        }

        List<Category> categories = categoryRepository.findAllById(request.getCategoryIds());
        if (categories.isEmpty()) {
            throw new BusinessException("At least one valid category is required");
        }

        if (categories.size() != request.getCategoryIds().size()) {
            throw new BusinessException("Some categories were not found");
        }

        existingProduct.setSeller(seller);
        existingProduct.setName(request.getName());
        existingProduct.setDescription(request.getDescription());
        existingProduct.setPrice(request.getPrice());
        existingProduct.setPromoPrice(request.getPromoPrice());
        existingProduct.setStock(request.getStock());
        existingProduct.setCategories(categories);
        existingProduct.setImages(request.getImages());

        /*
         * Remplacement complet des variantes.
         * orphanRemoval = true dans l'entité Product
         * donc les anciennes variantes supprimées de la liste
         * seront effacées en base au save().
         */
        existingProduct.getVariants().clear();

        List<ProductVariant> newVariants = request.getVariants()
                .stream()
                .map(variantRequest -> mapVariantRequestToEntity(variantRequest, existingProduct))
                .toList();

        existingProduct.getVariants().addAll(newVariants);

        Product updatedProduct = productRepository.save(existingProduct);

        return mapToResponse(updatedProduct);
    }

    @Override
    public void softDeleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Product not found"));

        /*
         * Soft delete :
         * on ne supprime pas physiquement la ligne en base.
         * On la désactive seulement.
         */
        product.setActive(false);
        productRepository.save(product);
    }

    @Override
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Product not found"));

        return mapToResponse(product);
    }

    @Override
    public Page<ProductResponse> getProducts(Long categoryId,
                                             Double minPrice,
                                             Double maxPrice,
                                             Long sellerId,
                                             Boolean promo,
                                             int page,
                                             int size,
                                             String sortBy,
                                             String sortDirection) {

        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Product> productPage = productRepository.filterProducts(categoryId, minPrice, maxPrice, sellerId, promo, pageable);

        return productPage.map(this::mapToResponse);
    }

    @Override
    public Page<ProductResponse> searchProducts(String q, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return productRepository
                .findByActiveTrueAndNameContainingIgnoreCaseOrActiveTrueAndDescriptionContainingIgnoreCase(
                        q, q, pageable
                )
                .map(this::mapToResponse);
    }

    @Override
    public List<ProductResponse> getTopSellingProducts() {
        /*
         * Temporairement :
         * comme on n'a pas encore OrderItem,
         * on retourne les 10 plus récents actifs.
         *
         * Plus tard, on remplacera par les vraies meilleures ventes.
         */
        return productRepository.findTop10ByActiveTrueOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private ProductVariant mapVariantRequestToEntity(ProductVariantRequest request, Product product) {
        return ProductVariant.builder()
                .attribute(request.getAttribute())
                .value(request.getValue())
                .stockAdditional(request.getStockAdditional())
                .priceDelta(request.getPriceDelta())
                .product(product)
                .build();
    }

    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .sellerId(product.getSeller().getId())
                .sellerEmail(product.getSeller().getEmail())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .promoPrice(product.getPromoPrice())
                .stock(product.getStock())
                .active(product.getActive())
                .createdAt(product.getCreatedAt())
                .categoryIds(
                        product.getCategories()
                                .stream()
                                .map(Category::getId)
                                .toList()
                )
                .images(product.getImages())
                .variants(
                        product.getVariants()
                                .stream()
                                .map(variant -> ProductVariantResponse.builder()
                                        .id(variant.getId())
                                        .attribute(variant.getAttribute())
                                        .value(variant.getValue())
                                        .stockAdditional(variant.getStockAdditional())
                                        .priceDelta(variant.getPriceDelta())
                                        .build())
                                .toList()
                )
                .averageRating(
                        product.getReviews() == null || product.getReviews().isEmpty()
                                ? 0.0
                                : product.getReviews().stream().mapToDouble(com.shopflow.shopflow.entity.Review::getRating).average().orElse(0.0)
                )
                .reviews(
                        product.getReviews() == null
                                ? List.of()
                                : product.getReviews().stream()
                                .map(r -> com.shopflow.shopflow.dto.response.ReviewResponse.builder()
                                        .id(r.getId())
                                        .productId(product.getId())
                                        .productName(product.getName())
                                        .userId(r.getUser().getId())
                                        .userName(r.getUser().getFirstName() + " " + r.getUser().getLastName())
                                        .rating(r.getRating())
                                        .comment(r.getComment())
                                        .createdAt(r.getCreatedAt())
                                        .build())
                                .toList()
                )
                .build();
    }
}