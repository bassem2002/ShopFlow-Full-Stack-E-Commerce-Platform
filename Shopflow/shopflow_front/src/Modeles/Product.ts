// ============================================================
// Modèles Product — synchronisés avec le backend ShopFlow
// ProductController : /api/products
// ============================================================

import { ReviewResponse } from './Review';

// ─── Request DTOs (Angular → API) ────────────────────────────

export interface ProductVariantRequest {
  attribute: string;      // "Size", "Color"…
  value: string;          // "XL", "Rouge"…
  stockAdditional: number;
  priceDelta: number;     // +/- par rapport au prix de base
}

export interface ProductRequest {
  sellerId: number;
  name: string;
  description: string;
  price: number;
  promoPrice?: number | null;
  stock: number;
  categoryIds: number[];
  images?: string[];
  variants?: ProductVariantRequest[];
}

export interface ProductFilter {
  categoryId?: number;
  minPrice?: number;
  maxPrice?: number;
  sellerId?: number;
  promo?: boolean;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: 'asc' | 'desc';
}

// ─── Response DTOs (API → Angular) ───────────────────────────

export interface ProductVariantResponse {
  id: number;
  attribute: string;
  value: string;
  stockAdditional: number;
  priceDelta: number;
}

export interface ProductResponse {
  id: number;
  sellerId: number;
  sellerEmail: string;
  name: string;
  description: string;
  price: number;
  promoPrice: number | null;
  stock: number;
  active: boolean;
  createdAt: string;          // ISO 8601 → utiliser new Date(createdAt) ou le pipe date
  categoryIds: number[];
  images: string[];
  variants: ProductVariantResponse[];
  averageRating: number | null;
  reviews: ReviewResponse[];
}
