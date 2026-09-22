package com.shopflow.shopflow.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CartItemResponse {

    private Long itemId;
    private Long productId;
    private String productName;

    private Long variantId;
    private String variantLabel;

    private Integer quantity;
    private Double unitPrice;
    private Double lineTotal;
    private String productImage;
}