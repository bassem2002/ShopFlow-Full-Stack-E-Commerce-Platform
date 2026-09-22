package com.shopflow.shopflow.controller;

import com.shopflow.shopflow.dto.request.AddCartItemRequest;
import com.shopflow.shopflow.dto.request.ApplyCouponRequest;
import com.shopflow.shopflow.dto.request.UpdateCartItemRequest;
import com.shopflow.shopflow.dto.response.CartResponse;
import com.shopflow.shopflow.entity.User;
import com.shopflow.shopflow.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponse> getCart(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(cartService.getCart(user));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(@Valid @RequestBody AddCartItemRequest request,
                                               Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(cartService.addItem(user, request));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> updateItem(@PathVariable Long itemId,
                                                   @Valid @RequestBody UpdateCartItemRequest request,
                                                   Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(cartService.updateItem(user, itemId, request));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> removeItem(@PathVariable Long itemId,
                                                   Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(cartService.removeItem(user, itemId));
    }

    @PostMapping("/coupon")
    public ResponseEntity<CartResponse> applyCoupon(@Valid @RequestBody ApplyCouponRequest request,
                                                    Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(cartService.applyCoupon(user, request));
    }

    @DeleteMapping("/coupon")
    public ResponseEntity<CartResponse> removeCoupon(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(cartService.removeCoupon(user));
    }
}