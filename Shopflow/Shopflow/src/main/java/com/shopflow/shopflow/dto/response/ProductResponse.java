package com.shopflow.shopflow.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class ProductResponse {

    private Long id;
    private Long sellerId;
    private String sellerEmail;

    private String name;
    private String description;
    private Double price;
    private Double promoPrice;
    private Integer stock;
    private Boolean active;
    private LocalDateTime createdAt;

    private List<Long> categoryIds;
    private List<String> images;

    private List<ProductVariantResponse> variants;

    /*
     * Préparé pour plus tard.
     */
    private Double averageRating;

    private List<ReviewResponse> reviews;
}