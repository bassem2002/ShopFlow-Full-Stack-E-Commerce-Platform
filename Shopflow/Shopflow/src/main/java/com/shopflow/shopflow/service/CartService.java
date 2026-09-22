package com.shopflow.shopflow.service;

import com.shopflow.shopflow.dto.request.AddCartItemRequest;
import com.shopflow.shopflow.dto.request.ApplyCouponRequest;
import com.shopflow.shopflow.dto.request.UpdateCartItemRequest;
import com.shopflow.shopflow.dto.response.CartResponse;
import com.shopflow.shopflow.entity.User;

public interface CartService {

    CartResponse getCart(User user);

    CartResponse addItem(User user, AddCartItemRequest request);

    CartResponse updateItem(User user, Long itemId, UpdateCartItemRequest request);

    CartResponse removeItem(User user, Long itemId);

    CartResponse applyCoupon(User user, ApplyCouponRequest request);

    CartResponse removeCoupon(User user);
}