package com.shopflow.shopflow.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * Commande
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /*
     * Produit
     */
    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;

    /*
     * Variante (optionnelle)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    private ProductVariant variant;

    private Integer quantity;

    private Double unitPrice;
}