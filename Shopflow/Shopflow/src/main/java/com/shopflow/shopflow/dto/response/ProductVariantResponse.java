package com.shopflow.shopflow.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductVariantResponse {

    private Long id;
    private String attribute;
    private String value;
    private Integer stockAdditional;
    private Double priceDelta;
}