package com.shopflow.shopflow.repository;

import com.shopflow.shopflow.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByActiveTrue(Pageable pageable);

    Page<Product> findByActiveTrueAndCategories_Id(Long categoryId, Pageable pageable);

    Page<Product> findByActiveTrueAndPriceBetween(Double minPrice, Double maxPrice, Pageable pageable);

    Page<Product> findByActiveTrueAndSeller_Id(Long sellerId, Pageable pageable);

    Page<Product> findByActiveTrueAndPromoPriceIsNotNull(Pageable pageable);

    Page<Product> findByActiveTrueAndNameContainingIgnoreCaseOrActiveTrueAndDescriptionContainingIgnoreCase(
            String nameKeyword,
            String descriptionKeyword,
            Pageable pageable
    );

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN p.categories c " +
            "WHERE p.active = true " +
            "AND (:categoryId IS NULL OR c.id = :categoryId) " +
            "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
            "AND (:sellerId IS NULL OR p.seller.id = :sellerId) " +
            "AND (:promo IS NULL OR (:promo = true AND p.promoPrice IS NOT NULL) OR (:promo = false AND p.promoPrice IS NULL))")
    Page<Product> filterProducts(
            @Param("categoryId") Long categoryId,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("sellerId") Long sellerId,
            @Param("promo") Boolean promo,
            Pageable pageable
    );

    List<Product> findTop10ByActiveTrueOrderByCreatedAtDesc();

    @Query("SELECT p.name, p.price, p.stock, SUM(oi.quantity) " +
       "FROM OrderItem oi JOIN oi.product p " +
       "WHERE oi.order.status IN :statuses " +
       "GROUP BY p.id, p.name, p.price, p.stock ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> findTopProducts(@Param("statuses") List<com.shopflow.shopflow.enums.OrderStatus> statuses, Pageable pageable);

    @Query("SELECT CONCAT(p.seller.firstName, ' ', p.seller.lastName), SUM(oi.unitPrice * oi.quantity) " +
       "FROM OrderItem oi JOIN oi.product p " +
       "WHERE oi.order.status IN :statuses " +
       "GROUP BY p.seller.firstName, p.seller.lastName ORDER BY SUM(oi.unitPrice * oi.quantity) DESC")
    List<Object[]> findTopSellers(@Param("statuses") List<com.shopflow.shopflow.enums.OrderStatus> statuses, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.seller.id = :sellerId AND p.stock < :threshold")
    List<Product> findBySellerIdAndStockLessThan(@Param("sellerId") Long sellerId, @Param("threshold") Integer threshold);

    Long countBySellerId(Long sellerId);
}