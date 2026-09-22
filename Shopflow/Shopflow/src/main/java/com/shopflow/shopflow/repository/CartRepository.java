package com.shopflow.shopflow.repository;

import com.shopflow.shopflow.entity.Cart;
import com.shopflow.shopflow.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByCustomer_Id(Long customerId);
    Optional<Cart> findByCustomer(User customer);


}