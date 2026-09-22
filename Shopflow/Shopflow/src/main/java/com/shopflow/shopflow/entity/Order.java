package com.shopflow.shopflow.entity;

import com.shopflow.shopflow.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * Client
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    /*
     * Statut de la commande
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    /*
     * Numéro unique (ORD-2026-XXXXX)
     */
    @Column(nullable = false, unique = true)
    private String orderNumber;

    /*
     * Adresse de livraison (simple version)
     */
    @Column(nullable = false)
    private String shippingAddress;

    private Double subTotal;
    private Double shippingCost;
    private Double total;

    private LocalDateTime createdAt;

    /*
     * Lignes de commande
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();
}