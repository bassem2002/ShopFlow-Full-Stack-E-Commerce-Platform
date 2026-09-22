package com.shopflow.shopflow.repository;

import com.shopflow.shopflow.entity.SellerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SellerProfileRepository extends JpaRepository<SellerProfile, Long> {

    Optional<SellerProfile> findByUser_Id(Long userId);
}