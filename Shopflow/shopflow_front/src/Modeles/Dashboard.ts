// ============================================================
// Modèles Dashboard — synchronisés avec le backend ShopFlow
// DashboardController : /api/dashboard
// ============================================================

import { OrderStatus } from './enums';

export interface TopProductDto {
  name: string;
  price: number;
  stock: number;
  quantitySold: number;
}

export interface TopSellerDto {
  sellerName: string;
  revenue: number;
}

export interface OrderSummaryDto {
  orderNumber: string;
  total: number;
  status: OrderStatus;
  createdAt: string;
  productImage?: string | null;
}

export interface AdminDashboard {
  totalRevenue: number;
  totalOrders: number;
  totalProducts: number;
  totalUsers: number;
  topProducts: TopProductDto[];
  topSellers: TopSellerDto[];
  recentOrders: OrderSummaryDto[];
}

export interface SellerDashboard {
  totalRevenue: number;
  pendingOrders: number;
  lowStockAlerts: { productName: string; currentStock: number }[];
}

export interface CustomerDashboard {
  totalSpent: number;
  totalOrders: number;
  currentOrders: OrderSummaryDto[];
  recentReviews: { productName: string; rating: number; comment: string }[];
}
