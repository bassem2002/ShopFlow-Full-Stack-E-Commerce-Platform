package com.shopflow.shopflow.mapper;

import com.shopflow.shopflow.dto.response.*;
import com.shopflow.shopflow.entity.*;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class OrderMapper {

    public OrderResponse toResponse(Order order) {

        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus())
                .shippingAddress(order.getShippingAddress())
                .subTotal(order.getSubTotal())
                .shippingCost(order.getShippingCost())
                .total(order.getTotal())
                .createdAt(order.getCreatedAt())
                .customerId(order.getCustomer().getId())
                .customerEmail(order.getCustomer().getEmail())
                .items(order.getItems().stream()
                        .map(this::toItemResponse)
                        .collect(Collectors.toList()))
                .productImage(order.getItems() != null && !order.getItems().isEmpty() 
                        && order.getItems().get(0).getProduct().getImages() != null 
                        && !order.getItems().get(0).getProduct().getImages().isEmpty()
                        ? order.getItems().get(0).getProduct().getImages().get(0)
                        : null)
                .build();
    }

    private OrderItemResponse toItemResponse(OrderItem item) {

        return OrderItemResponse.builder()
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .variantId(item.getVariant() != null ? item.getVariant().getId() : null)
                .variant(item.getVariant() != null
                        ? item.getVariant().getAttribute() + ":" + item.getVariant().getValue()
                        : null)
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .productImage(item.getProduct().getImages() != null && !item.getProduct().getImages().isEmpty()
                        ? item.getProduct().getImages().get(0)
                        : null)
                .build();
    }
}