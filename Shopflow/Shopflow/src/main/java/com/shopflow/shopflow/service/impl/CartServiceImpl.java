package com.shopflow.shopflow.service.impl;

import com.shopflow.shopflow.dto.request.AddCartItemRequest;
import com.shopflow.shopflow.dto.request.ApplyCouponRequest;
import com.shopflow.shopflow.dto.request.UpdateCartItemRequest;
import com.shopflow.shopflow.dto.response.CartItemResponse;
import com.shopflow.shopflow.dto.response.CartResponse;
import com.shopflow.shopflow.entity.*;
import com.shopflow.shopflow.enums.CouponType;
import com.shopflow.shopflow.enums.Role;
import com.shopflow.shopflow.exception.BusinessException;
import com.shopflow.shopflow.repository.*;
import com.shopflow.shopflow.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CouponRepository couponRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;

    @Override
    public CartResponse getCart(User user) {
        validateCustomerRole(user);
        Cart cart = getOrCreateCart(user.getId());
        return mapToResponse(cart);
    }

    @Override
    public CartResponse addItem(User user, AddCartItemRequest request) {
        validateCustomerRole(user);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new BusinessException("Product not found"));

        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new BusinessException("Product is inactive");
        }

        ProductVariant variant = null;

        if (request.getVariantId() != null) {
            variant = productVariantRepository.findById(request.getVariantId())
                    .orElseThrow(() -> new BusinessException("Variant not found"));

            if (!variant.getProduct().getId().equals(product.getId())) {
                throw new BusinessException("Variant does not belong to the selected product");
            }

            if (request.getQuantity() > variant.getStockAdditional()) {
                throw new BusinessException("Requested quantity exceeds variant stock");
            }
        } else {
            if (request.getQuantity() > product.getStock()) {
                throw new BusinessException("Requested quantity exceeds product stock");
            }
        }

        Cart cart = getOrCreateCart(user.getId());

        final ProductVariant selectedVariant = variant;

        CartItem existingItem = cart.getItems()
                .stream()
                .filter(item ->
                        item.getProduct().getId().equals(product.getId()) &&
                                (
                                        (item.getVariant() == null && selectedVariant == null) ||
                                                (item.getVariant() != null && selectedVariant != null &&
                                                        item.getVariant().getId().equals(selectedVariant.getId()))
                                )
                )
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            int newQuantity = existingItem.getQuantity() + request.getQuantity();

            if (selectedVariant != null && newQuantity > selectedVariant.getStockAdditional()) {
                throw new BusinessException("Requested quantity exceeds variant stock");
            }

            if (selectedVariant == null && newQuantity > product.getStock()) {
                throw new BusinessException("Requested quantity exceeds product stock");
            }

            existingItem.setQuantity(newQuantity);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .variant(selectedVariant)
                    .quantity(request.getQuantity())
                    .build();

            cart.getItems().add(newItem);
        }

        cart.setUpdatedAt(LocalDateTime.now());

        Cart savedCart = cartRepository.save(cart);

        return mapToResponse(savedCart);
    }

    @Override
    public CartResponse updateItem(User user, Long itemId, UpdateCartItemRequest request) {
        validateCustomerRole(user);

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException("Cart item not found"));

        // Verify cart ownership
        if (!item.getCart().getCustomer().getId().equals(user.getId())) {
            throw new BusinessException("Access denied: You can only modify your own cart");
        }

        Product product = item.getProduct();
        ProductVariant variant = item.getVariant();

        if (variant != null) {
            if (request.getQuantity() > variant.getStockAdditional()) {
                throw new BusinessException("Requested quantity exceeds variant stock");
            }
        } else {
            if (request.getQuantity() > product.getStock()) {
                throw new BusinessException("Requested quantity exceeds product stock");
            }
        }

        item.setQuantity(request.getQuantity());
        item.getCart().setUpdatedAt(LocalDateTime.now());

        cartItemRepository.save(item);

        return mapToResponse(item.getCart());
    }

    @Override
    public CartResponse removeItem(User user, Long itemId) {
        validateCustomerRole(user);

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException("Cart item not found"));

        Cart cart = item.getCart();

        // Verify cart ownership
        if (!cart.getCustomer().getId().equals(user.getId())) {
            throw new BusinessException("Access denied: You can only modify your own cart");
        }

        cart.getItems().remove(item);
        cart.setUpdatedAt(LocalDateTime.now());

        cartRepository.save(cart);

        return mapToResponse(cart);
    }

    @Override
    public CartResponse applyCoupon(User user, ApplyCouponRequest request) {
        validateCustomerRole(user);

        Cart cart = getOrCreateCart(user.getId());

        Coupon coupon = couponRepository.findByCode(request.getCode())
                .orElseThrow(() -> new BusinessException("Coupon not found"));

        if (!Boolean.TRUE.equals(coupon.getActive())) {
            throw new BusinessException("Coupon is inactive");
        }

        if (coupon.getExpirationDate().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Coupon is expired");
        }

        if (coupon.getCurrentUsages() >= coupon.getMaxUsages()) {
            throw new BusinessException("Coupon usage limit reached");
        }

        cart.setCoupon(coupon);
        cart.setUpdatedAt(LocalDateTime.now());

        coupon.setCurrentUsages(coupon.getCurrentUsages() + 1);

        cartRepository.save(cart);
        couponRepository.save(coupon);

        return mapToResponse(cart);
    }

    @Override
    public CartResponse removeCoupon(User user) {
        validateCustomerRole(user);

        Cart cart = getOrCreateCart(user.getId());

        Coupon coupon = cart.getCoupon();
        if (coupon != null && coupon.getCurrentUsages() > 0) {
            coupon.setCurrentUsages(coupon.getCurrentUsages() - 1);
            couponRepository.save(coupon);
        }

        cart.setCoupon(null);
        cart.setUpdatedAt(LocalDateTime.now());

        Cart savedCart = cartRepository.save(cart);

        return mapToResponse(savedCart);
    }

    private void validateCustomerRole(User user) {
        if (user == null) {
            throw new BusinessException("User is required");
        }
        if (user.getRole() != Role.CUSTOMER) {
            throw new BusinessException("Only CUSTOMER can own a cart");
        }
    }

    private Cart getOrCreateCart(Long customerId) {
        return cartRepository.findByCustomer_Id(customerId)
                .orElseGet(() -> {
                    User customer = userRepository.findById(customerId)
                            .orElseThrow(() -> new BusinessException("Customer not found"));

                    if (customer.getRole() != Role.CUSTOMER) {
                        throw new BusinessException("Only CUSTOMER can own a cart");
                    }

                    Cart newCart = Cart.builder()
                            .customer(customer)
                            .items(new ArrayList<>())
                            .updatedAt(LocalDateTime.now())
                            .build();

                    return cartRepository.save(newCart);
                });
    }

    private CartResponse mapToResponse(Cart cart) {
        List<CartItemResponse> itemResponses = cart.getItems()
                .stream()
                .map(item -> {
                    double unitPrice = item.getVariant() != null
                            ? item.getProduct().getPrice() + item.getVariant().getPriceDelta()
                            : item.getProduct().getPrice();

                    double lineTotal = unitPrice * item.getQuantity();

                    String variantLabel = item.getVariant() != null
                            ? item.getVariant().getAttribute() + ": " + item.getVariant().getValue()
                            : null;

                    return CartItemResponse.builder()
                            .itemId(item.getId())
                            .productId(item.getProduct().getId())
                            .productName(item.getProduct().getName())
                            .variantId(item.getVariant() != null ? item.getVariant().getId() : null)
                            .variantLabel(variantLabel)
                            .quantity(item.getQuantity())
                            .unitPrice(unitPrice)
                            .lineTotal(lineTotal)
                            .productImage(item.getProduct().getImages() != null && !item.getProduct().getImages().isEmpty() 
                                    ? item.getProduct().getImages().get(0) 
                                    : null)
                            .build();
                })
                .toList();

        double subTotal = itemResponses.stream()
                .mapToDouble(CartItemResponse::getLineTotal)
                .sum();

        double discountedSubTotal = subTotal;

        if (cart.getCoupon() != null) {
            Coupon coupon = cart.getCoupon();

            if (coupon.getType() == CouponType.PERCENT) {
                discountedSubTotal = subTotal - (subTotal * coupon.getValue() / 100.0);
            } else if (coupon.getType() == CouponType.FIXED) {
                discountedSubTotal = subTotal - coupon.getValue();
            }

            if (discountedSubTotal < 0) {
                discountedSubTotal = 0;
            }
        }

        double shippingFees = discountedSubTotal >= 100 ? 0.0 : 7.0;

        double totalTTC = discountedSubTotal + shippingFees;

        return CartResponse.builder()
                .cartId(cart.getId())
                .customerId(cart.getCustomer().getId())
                .items(itemResponses)
                .couponCode(cart.getCoupon() != null ? cart.getCoupon().getCode() : null)
                .subTotal(subTotal)
                .shippingFees(shippingFees)
                .totalTTC(totalTTC)
                .build();
    }
}