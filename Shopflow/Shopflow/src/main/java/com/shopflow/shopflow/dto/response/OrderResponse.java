package com.shopflow.shopflow.dto.response;

import com.shopflow.shopflow.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponse {

    private Long id;
    private String orderNumber;
    private OrderStatus status;

    private String shippingAddress;

    private Double subTotal;
    private Double shippingCost;
    private Double total;

    private LocalDateTime createdAt;

    private Long customerId;
    private String customerEmail;

    private List<OrderItemResponse> items;
    private String productImage;
}