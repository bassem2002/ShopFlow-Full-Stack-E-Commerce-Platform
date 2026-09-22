package com.shopflow.shopflow.dto.request;

import com.shopflow.shopflow.enums.CouponType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CouponRequest {

    @NotBlank(message = "Coupon code is required")
    @Size(max = 50, message = "Code must be less than 50 characters")
    private String code;

    @NotNull(message = "Coupon type is required")
    private CouponType type;

    @NotNull(message = "Value is required")
    @Positive(message = "Value must be positive")
    private Double value;

    @NotNull(message = "Expiration date is required")
    @Future(message = "Expiration date must be in the future")
    private LocalDateTime expirationDate;

    @NotNull(message = "Max usages is required")
    @Min(value = 1, message = "Max usages must be at least 1")
    private Integer maxUsages;

    @NotNull(message = "Active status is required")
    private Boolean active;
}
