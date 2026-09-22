package com.shopflow.shopflow.repository;

import com.shopflow.shopflow.entity.Order;
import com.shopflow.shopflow.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByCustomer(User customer);

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT oi.order FROM OrderItem oi WHERE oi.product.seller.id = :sellerId")
    List<Order> findOrdersBySellerId(@org.springframework.data.repository.query.Param("sellerId") Long sellerId);

    List<Order> findTop10ByOrderByCreatedAtDesc();

    List<Order> findByCustomerIdAndStatusIn(Long customerId, List<com.shopflow.shopflow.enums.OrderStatus> statuses);

    @org.springframework.data.jpa.repository.Query("SELECT SUM(o.total) FROM Order o WHERE o.status IN :statuses")
    Double getGlobalRevenue(@org.springframework.data.repository.query.Param("statuses") List<com.shopflow.shopflow.enums.OrderStatus> statuses);

    @org.springframework.data.jpa.repository.Query("SELECT SUM(oi.unitPrice * oi.quantity) FROM OrderItem oi WHERE oi.product.seller.id = :sellerId AND oi.order.status IN :statuses")
    Double getSellerRevenue(@org.springframework.data.repository.query.Param("sellerId") Long sellerId, @org.springframework.data.repository.query.Param("statuses") List<com.shopflow.shopflow.enums.OrderStatus> statuses);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(DISTINCT oi.order.id) FROM OrderItem oi WHERE oi.product.seller.id = :sellerId AND oi.order.status = :status")
    Integer countSellerOrdersByStatus(@org.springframework.data.repository.query.Param("sellerId") Long sellerId, @org.springframework.data.repository.query.Param("status") com.shopflow.shopflow.enums.OrderStatus status);

    @org.springframework.data.jpa.repository.Query("SELECT SUM(o.total) FROM Order o WHERE o.customer.id = :customerId AND o.status IN :statuses")
    Double getCustomerSpent(@org.springframework.data.repository.query.Param("customerId") Long customerId, @org.springframework.data.repository.query.Param("statuses") List<com.shopflow.shopflow.enums.OrderStatus> statuses);

    Long countByCustomerId(Long customerId);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(o) > 0 FROM Order o JOIN o.items i WHERE o.customer.id = :userId AND i.product.id = :productId AND o.status = com.shopflow.shopflow.enums.OrderStatus.DELIVERED")
    boolean hasBeenDeliveredToUser(@org.springframework.data.repository.query.Param("userId") Long userId, @org.springframework.data.repository.query.Param("productId") Long productId);

}