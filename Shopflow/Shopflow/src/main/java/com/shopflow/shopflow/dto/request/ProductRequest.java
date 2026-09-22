package com.shopflow.shopflow.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ProductRequest {

    @NotNull(message = "Seller id is required")
    private Long sellerId;

    @NotBlank(message = "Product name is required")
    private String name;

    @NotBlank(message = "Product description is required")
    private String description;

    @NotNull(message = "Product price is required")
    @Min(value = 0, message = "Price must be >= 0")
    private Double price;

    /*
     * Peut être null si pas de promo.
     */
    private Double promoPrice;

    @NotNull(message = "Product stock is required")
    @Min(value = 0, message = "Stock must be >= 0")
    private Integer stock;

    /*
     * Au moins une catégorie.
     */
    @NotEmpty(message = "At least one category is required")
    private List<Long> categoryIds;

    private List<String> images = new ArrayList<>();

    @Valid
    private List<ProductVariantRequest> variants = new ArrayList<>();
}