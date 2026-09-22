package com.shopflow.shopflow.entity;

import com.shopflow.shopflow.enums.CouponType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CouponType type;

    /*
     * Si type = PERCENT -> ex: 10 = 10%
     * Si type = FIXED   -> ex: 20 = 20 DT
     */
    @Column(name = "coupon_value", nullable = false)
    private Double value;

    @Column(nullable = false)
    private LocalDateTime expirationDate;

    @Column(nullable = false)
    private Integer maxUsages;

    @Column(nullable = false)
    private Integer currentUsages;

    @Column(nullable = false)
    private Boolean active;
}