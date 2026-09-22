package com.shopflow.shopflow.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

public class DashboardDto {

    @Data
    @Builder
    public static class AdminDashboard {
        private Double totalRevenue;
        private Long totalOrders;
        private Long totalProducts;
        private Long totalUsers;
        private List<TopProductDto> topProducts;
        private List<TopSellerDto> topSellers;
        private List<OrderSummaryDto> recentOrders;
    }

    @Data
    @Builder
    public static class SellerDashboard {
        private Double totalRevenue;
        private Long totalProducts;
        private Integer pendingOrders;
        private List<LowStockProductDto> lowStockAlerts;
        private List<OrderSummaryDto> recentOrders;
    }

    @Data
    @Builder
    public static class CustomerDashboard {
        private Double totalSpent;
        private Long totalOrders;
        private List<OrderSummaryDto> currentOrders;
        private List<ReviewSummaryDto> recentReviews;
    }

    @Data
    @Builder
    public static class TopProductDto {
        private String name;
        private Double price;
        private Integer stock;
        private Long quantitySold;
    }

    @Data
    @Builder
    public static class TopSellerDto {
        private String sellerName;
        private Double revenue;
    }

    @Data
    @Builder
    public static class OrderSummaryDto {
        private String orderNumber;
        private Double total;
        private String status;
        private String createdAt;
        private String productImage;
    }

    @Data
    @Builder
    public static class LowStockProductDto {
        private String productName;
        private Integer currentStock;
    }

    @Data
    @Builder
    public static class ReviewSummaryDto {
        private String productName;
        private Integer rating;
        private String comment;
    }
}
