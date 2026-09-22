// ============================================================
// Modèles Coupon — synchronisés avec le backend ShopFlow
// CouponController : /api/coupons
// ============================================================

import { CouponType } from './enums';

// ─── Request DTO ─────────────────────────────────────────────

export interface CouponRequest {
  code: string;
  type: CouponType;
  value: number;
  expirationDate: string;   // ISO 8601
  maxUsages: number;
}

// ─── Response DTO ────────────────────────────────────────────

export interface CouponResponse {
  id: number;
  code: string;
  type: CouponType;
  value: number;
  expirationDate: string;
  maxUsages: number;
  currentUsages: number;
  active: boolean;
}
