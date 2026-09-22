package com.shopflow.shopflow.repository;

import com.shopflow.shopflow.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
}