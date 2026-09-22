package com.shopflow.shopflow.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_variants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * Exemple :
     * attribute = "Size"
     * value = "M"
     */
    @Column(nullable = false, length = 100)
    private String attribute;

    @Column(name = "variant_value", nullable = false, length = 100)
    private String value;

    /*
     * Stock spécifique à cette variante.
     */
    @Column(nullable = false)
    private Integer stockAdditional;

    /*
     * Différence de prix par rapport au produit principal.
     * Exemple : +10 DT pour XL
     */
    @Column(nullable = false)
    private Double priceDelta;

    /*
     * Plusieurs variantes appartiennent à un seul produit.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}