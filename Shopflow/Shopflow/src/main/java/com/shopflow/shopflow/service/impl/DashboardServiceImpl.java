package com.shopflow.shopflow.service.impl;

import com.shopflow.shopflow.dto.response.DashboardDto;
import com.shopflow.shopflow.entity.Order;
import com.shopflow.shopflow.enums.OrderStatus;
import com.shopflow.shopflow.repository.OrderRepository;
import com.shopflow.shopflow.repository.ProductRepository;
import com.shopflow.shopflow.repository.ReviewRepository;
import com.shopflow.shopflow.repository.UserRepository;
import com.shopflow.shopflow.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    private final List<OrderStatus> revenueStatuses = Arrays.asList(
            OrderStatus.PAID, OrderStatus.SHIPPED, OrderStatus.DELIVERED
    );

    @Override
    public DashboardDto.AdminDashboard getAdminDashboard() {
        Double globalRevenue = orderRepository.getGlobalRevenue(revenueStatuses);
        Long totalOrders = orderRepository.count();
        Long totalProducts = productRepository.count();
        Long totalUsers = userRepository.count();

        List<Object[]> popularProducts = productRepository.findTopProducts(revenueStatuses, PageRequest.of(0, 5));
        List<DashboardDto.TopProductDto> topProducts = popularProducts.stream().map(obj -> 
                DashboardDto.TopProductDto.builder()
                        .name((String) obj[0])
                        .price((Double) obj[1])
                        .stock((Integer) obj[2])
                        .quantitySold(obj[3] == null ? 0L : ((Number) obj[3]).longValue())
                        .build()
        ).collect(Collectors.toList());

        List<Object[]> topSales = productRepository.findTopSellers(revenueStatuses, PageRequest.of(0, 5));
        List<DashboardDto.TopSellerDto> topSellers = topSales.stream().map(obj ->
                DashboardDto.TopSellerDto.builder()
                        .sellerName((String) obj[0])
                        .revenue(obj[1] == null ? 0.0 : ((Number) obj[1]).doubleValue())
                        .build()
        ).collect(Collectors.toList());

        List<DashboardDto.OrderSummaryDto> recentOrders = orderRepository.findTop10ByOrderByCreatedAtDesc()
                .stream().map(o -> DashboardDto.OrderSummaryDto.builder()
                        .orderNumber(o.getOrderNumber())
                        .total(o.getTotal())
                        .status(o.getStatus().name())
                        .createdAt(o.getCreatedAt() != null ? o.getCreatedAt().toString() : "N/A")
                        .productImage(getFirstProductImage(o))
                        .build())
                .collect(Collectors.toList());

        return DashboardDto.AdminDashboard.builder()
                .totalRevenue(globalRevenue == null ? 0.0 : globalRevenue)
                .totalOrders(totalOrders)
                .totalProducts(totalProducts)
                .totalUsers(totalUsers)
                .topProducts(topProducts)
                .topSellers(topSellers)
                .recentOrders(recentOrders)
                .build();
    }

    @Override
    public DashboardDto.SellerDashboard getSellerDashboard(Long sellerId) {
        Double totalRevenue = orderRepository.getSellerRevenue(sellerId, revenueStatuses);
        Integer pendingOrders = orderRepository.countSellerOrdersByStatus(sellerId, OrderStatus.PENDING);
        Long totalProducts = productRepository.countBySellerId(sellerId);

        List<DashboardDto.LowStockProductDto> lowStockAlerts = productRepository.findBySellerIdAndStockLessThan(sellerId, 10)
                .stream().map(p -> DashboardDto.LowStockProductDto.builder()
                        .productName(p.getName())
                        .currentStock(p.getStock())
                        .build())
                .collect(Collectors.toList());

        List<DashboardDto.OrderSummaryDto> recentOrders = orderRepository.findOrdersBySellerId(sellerId)
                .stream()
                .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
                .limit(5)
                .map(o -> DashboardDto.OrderSummaryDto.builder()
                        .orderNumber(o.getOrderNumber())
                        .total(o.getTotal())
                        .status(o.getStatus().name())
                        .createdAt(o.getCreatedAt() != null ? o.getCreatedAt().toString() : "N/A")
                        .productImage(getFirstProductImage(o))
                        .build())
                .collect(Collectors.toList());

        return DashboardDto.SellerDashboard.builder()
                .totalRevenue(totalRevenue == null ? 0.0 : totalRevenue)
                .totalProducts(totalProducts == null ? 0L : totalProducts)
                .pendingOrders(pendingOrders == null ? 0 : pendingOrders)
                .lowStockAlerts(lowStockAlerts)
                .recentOrders(recentOrders)
                .build();
    }

    @Override
    public DashboardDto.CustomerDashboard getCustomerDashboard(Long customerId) {
        Double totalSpent = orderRepository.getCustomerSpent(customerId, revenueStatuses);
        Long totalOrdersCount = orderRepository.countByCustomerId(customerId);

        List<DashboardDto.OrderSummaryDto> currentOrders = orderRepository.findByCustomerIdAndStatusIn(
                customerId, Arrays.asList(OrderStatus.PENDING, OrderStatus.PROCESSING, OrderStatus.PAID, OrderStatus.SHIPPED)
        ).stream().map(o -> DashboardDto.OrderSummaryDto.builder()
                .orderNumber(o.getOrderNumber())
                .total(o.getTotal())
                .status(o.getStatus().name())
                .createdAt(o.getCreatedAt() != null ? o.getCreatedAt().toString() : "N/A")
                .productImage(getFirstProductImage(o))
                .build()
        ).collect(Collectors.toList());

        List<DashboardDto.ReviewSummaryDto> recentReviews = reviewRepository.findTop5ByUserIdOrderByCreatedAtDesc(customerId)
                .stream().map(r -> DashboardDto.ReviewSummaryDto.builder()
                        .productName(r.getProduct().getName())
                        .rating(r.getRating())
                        .comment(r.getComment())
                        .build()
                ).collect(Collectors.toList());

        return DashboardDto.CustomerDashboard.builder()
                .totalSpent(totalSpent == null ? 0.0 : totalSpent)
                .totalOrders(totalOrdersCount == null ? 0L : totalOrdersCount)
                .currentOrders(currentOrders)
                .recentReviews(recentReviews)
                .build();
    }

    private String getFirstProductImage(Order order) {
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            return order.getItems().get(0).getProduct().getImages().stream()
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }
}
