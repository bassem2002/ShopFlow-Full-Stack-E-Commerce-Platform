package com.shopflow.shopflow.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderItemResponse {

    private Long productId;
    private String productName;

    private Long variantId;
    private String variant;

    private Integer quantity;
    private Double unitPrice;
    private String productImage;
}