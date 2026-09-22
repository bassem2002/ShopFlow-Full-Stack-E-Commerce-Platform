package com.shopflow.shopflow.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class CartResponse {

    private Long cartId;
    private Long customerId;

    @Builder.Default
    private List<CartItemResponse> items = new ArrayList<>();

    private String couponCode;
    private Double subTotal;
    private Double shippingFees;
    private Double totalTTC;
}