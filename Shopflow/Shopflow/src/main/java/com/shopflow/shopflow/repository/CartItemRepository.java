package com.shopflow.shopflow.repository;

import com.shopflow.shopflow.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}