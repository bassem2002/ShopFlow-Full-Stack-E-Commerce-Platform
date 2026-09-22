package com.shopflow.shopflow.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductVariantRequest {

    @NotBlank(message = "Variant attribute is required")
    private String attribute;

    @NotBlank(message = "Variant value is required")
    private String value;

    @NotNull(message = "Variant stock is required")
    @Min(value = 0, message = "Variant stock must be >= 0")
    private Integer stockAdditional;

    @NotNull(message = "Variant price delta is required")
    private Double priceDelta;
}