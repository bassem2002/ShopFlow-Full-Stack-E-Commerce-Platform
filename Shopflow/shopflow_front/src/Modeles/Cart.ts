// ============================================================
// Modèles Cart — synchronisés avec le backend ShopFlow
// CartController : /api/cart
// ============================================================

// ─── Request DTOs ────────────────────────────────────────────

export interface AddCartItemRequest {
  customerId?: number;
  productId: number;
  variantId?: number | null;
  quantity: number;
}

export interface UpdateCartItemRequest {
  quantity: number;
}

export interface ApplyCouponRequest {
  customerId?: number;
  code?: string;
  couponCode?: string;
}

// ─── Response DTOs ───────────────────────────────────────────

export interface CartItemResponse {
  itemId: number;
  productId: number;
  productName: string;
  variantId: number | null;
  variantLabel: string | null;
  quantity: number;
  unitPrice: number;
  lineTotal: number;
  productImage: string | null;
}

export interface CartResponse {
  cartId: number;
  customerId: number;
  items: CartItemResponse[];
  couponCode: string | null;
  subTotal: number;
  shippingFees: number;
  totalTTC: number;
}
