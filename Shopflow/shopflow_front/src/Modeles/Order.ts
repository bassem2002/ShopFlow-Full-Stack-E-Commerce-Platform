// ============================================================
// Modèles Order — synchronisés avec le backend ShopFlow
// OrderController : /api/orders
// ============================================================

import { OrderStatus } from './enums';

// ─── Request DTO ─────────────────────────────────────────────

export interface OrderRequest {
  shippingAddress: string;
}

// ─── Response DTOs ───────────────────────────────────────────

export interface OrderItemResponse {
  productId: number;
  productName: string;
  variantId: number | null;
  variant: string | null;
  quantity: number;
  unitPrice: number;
  productImage: string | null;
}

export interface OrderResponse {
  id: number;
  orderNumber: string;          // format: ORD-2026-XXXXX
  status: OrderStatus;
  shippingAddress: string;
  subTotal: number;
  shippingCost: number;
  total: number;
  createdAt: string;            // ISO 8601
  customerId: number;
  customerEmail?: string;
  items: OrderItemResponse[];
  productImage?: string | null;
}
